package org.echosoft.framework.reports.registry;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.echosoft.framework.reports.model.Report;
import org.echosoft.framework.reports.parser.ReportExtension;
import org.echosoft.framework.reports.parser.ReportModelParser;
import org.echosoft.framework.reports.processor.ExcelReportProcessor;
import org.echosoft.framework.reports.processor.ReportProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Реестр зарегистрированных в системе отчетов и построителей отчетов.
 *
 * @author Anton Sharapov
 */
public class ReportsRegistry {

    private static final Logger log = LoggerFactory.getLogger(ReportsRegistry.class);
    private static final ReportProcessor defaultProcessor;
    private static final Map<String, ReportProcessor> processors = new HashMap<>();
    private static final Map<String, Report> reports = new HashMap<>();
    private static final List<ReportExtension> extensions = new CopyOnWriteArrayList<>();

    static {
        defaultProcessor = new ExcelReportProcessor();
        processors.put("excel2003", defaultProcessor);
        processors.put("excel2007", defaultProcessor);
    }

    /**
     * Возвращает используемый по умолчанию построитель отчетов.
     *
     * @return построитель отчетов по умолчанию. Никогда не возвращает <code>null</code>.
     */
    public static ReportProcessor getDefaultProcessor() {
        return defaultProcessor;
    }

    /**
     * Возвращает построитель отчетов идентификатору, под которым он был зарегистрирован в данном реестре.
     *
     * @param processorId идентификатор построителя отчетов.
     * @return построитель отчетов или <code>null</code> если в реестре отсутствует зарегистрированный под таким именем построитель.
     */
    public static ReportProcessor getProcessor(final String processorId) {
        return processors.get(processorId);
    }

    /**
     * Регистрирует в реестре новый построитель отчетов под указанным именем.
     *
     * @param processorId идентификатор построителя отчетов. Не может быть <code>null</code>.
     * @param processor   регистрируемый построитель отчетов. Не может быть <code>null</code>.
     */
    public static void registerProcessor(final String processorId, final ReportProcessor processor) {
        if (processorId == null || processor == null)
            throw new IllegalArgumentException("Valid processor and processor id must be specified");
        processors.put(processorId.toLowerCase(), processor);
    }

    /**
     * Возвращает изменяемый список расширений для моделей отчетов.</br>
     * К расширениям библиотека будет обращаться в момент инициализации отчета когда будут встречаться незнакомые ей элемента описания отчета.
     */
    public static List<ReportExtension> getExtensions() {
        return extensions;
    }

    /**
     * Возвращает информацию по всем зарегистрированным в системе отчетам. Удаление элемента данной коллекции приведет
     * к исключению соответствующего отчета из списка зарегистрированных отчетов. Добавление элементов в коллекцию
     * не поддерживается.
     *
     * @return перечень всех зарегистрированных отчетов.
     */
    public static Collection<Report> getReports() {
        return reports.values();
    }

    /**
     * Возвращает информацию об ранее зарегистрированном в системе отчете.
     *
     * @param id идентификатор отчета.
     * @return модель отчета или <code>null</code> если отчет с таким id не зарегистрирован в системе.
     */
    public static Report getReport(final String id) {
        return reports.get(id);
    }

    /**
     * Сбрасывает сведения о всех отчетах.
     */
    public static void resetReports() {
        reports.clear();
    }

    /**
     * Регистрирует новый отчет в системе. Если в системе уже был зарегистрирован отчет с таким идентификатором
     * то информация о старом отчете будет удалена.
     *
     * @param report модель отчета. Не может быть <code>null</code>.
     */
    public static void registerReport(final Report report) {
        if (report == null)
            throw new IllegalArgumentException("Report not specified");
        reports.put(report.getId(), report);
    }

    /**
     * Регистрирует отчет
     *
     * @param tplUrl URL шаблона отчета
     * @param mdlUrl URL описание отчета
     * @return зарегистрированная модель отчета или null если либо шаблон либо описание отчета не были указаны.
     */
    public static Report registerReport(final URL tplUrl, final URL mdlUrl) throws Exception {
        if (mdlUrl == null || tplUrl == null)
            return null;

        log.debug("registering report: {}", tplUrl);
        try (InputStream template = tplUrl.openStream()) {
            try (InputStream model = mdlUrl.openStream()) {
                final Report report = ReportModelParser.parse(template, model, extensions);
                reports.put(report.getId(), report);
                return report;
            }
        }
    }

    public static void registerReports(final URL... templatesUrl) throws Exception {
        for (URL tplUrl : templatesUrl) {
            if (tplUrl == null)
                continue;
            String u = tplUrl.toExternalForm();
            final int d = u.lastIndexOf('.');
            if (d < 0)
                continue;
            final URL mdlUrl = new URL(u.substring(0, d) + ".xml");
            registerReport(tplUrl, mdlUrl);
        }
    }

    /**
     * <p>Находит шаблоны и описания отчетов в указанном каталоге и автоматически регистрирует их в системе.</p>
     * <p>Каждый отчет описывается двумя файлами которые отличаются только расширением:
     * <ul>
     * <li> Excel документ, содержащий шаблон отчета.
     * <li> XML документ, содержащий  дополнительное описание и структурную разметку отчета.
     * </ul>
     * </p>
     *
     * @param dir       каталог, начиная с которого следует искать декларации отчетов.
     * @param recursive следует ли искать информацию об отчетах только в указанном каталоге или еще и во всех его дочерних подкаталогах.
     * @param filter    позволяет дополнительно отфильтровать нежелательные файлы. Может быть <code>null</code>.
     * @return количество загруженных в результате выполнения данного метода отчетов.
     * @throws Exception в случае каких-либо проблем при разборе отчетов.
     */
    public static int registerReportsFromDirectory(final File dir, final boolean recursive, final FileFilter filter) throws Exception {
        if (dir == null || !dir.isDirectory()) {
            return 0;
        }

        int result = 0;
        final HashMap<String, File[]> files = new HashMap<String, File[]>();
        for (File file : dir.listFiles(filter)) {
            if (file.isDirectory()) {
                if (recursive) {
                    result += registerReportsFromDirectory(file, true, filter);
                }
                continue;
            }
            final int s = file.getName().lastIndexOf('.');
            final String ext = s >= 0 ? file.getName().substring(s + 1).toLowerCase() : null;
            if (!"xls".equals(ext) && !"xlsx".equals(ext) && !"xlsm".equals(ext) && !"xml".equals(ext))
                continue;
            final String name = file.getName().substring(0, s);

            File[] tuple = files.get(name);
            if (tuple == null) {
                tuple = new File[2];
                files.put(name, tuple);
            }

            if ("xls".equals(ext) || "xlsx".equals(ext) || "xlsm".equals(ext)) {
                tuple[0] = file;
            } else {
                tuple[1] = file;
            }
        }

        Exception cause = null;
        for (File[] tuple : files.values()) {
            if (tuple[0] == null || tuple[1] == null)
                continue;

            log.debug("registering report: {}", tuple[0].getPath());
            try (FileInputStream template = new FileInputStream(tuple[0])) {
                try (FileInputStream structure = new FileInputStream(tuple[1])) {
                    final Report report = ReportModelParser.parse(template, structure, extensions);
                    reports.put(report.getId(), report);
                    result++;
                } catch (Exception e) {
                    cause = e;
                    log.error(e.getMessage(), e);
                }
            }
        }

        if (cause != null)
            throw cause;
        return result;
    }
}
