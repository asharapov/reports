package org.echosoft.framework.reports.registry;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.echosoft.framework.reports.model.Report;
import org.echosoft.framework.reports.parser.ReportModelParser;
import org.echosoft.framework.reports.processor.ExcelReportProcessor;
import org.echosoft.framework.reports.processor.ReportProcessor;
import org.echosoft.framework.reports.util.Logs;

/**
 * Реестр зарегистрированных в системе отчетов и построителей отчетов.
 *
 * @author Anton Sharapov
 */
public class ReportsRegistry {

    private static final ReportProcessor defaultProcessor;
    private static final Map<String, ReportProcessor> processors = new HashMap<String, ReportProcessor>();
    private static final Map<String, Report> reports = new HashMap<String, Report>();

    static {
        defaultProcessor = new ExcelReportProcessor();
        processors.put("excel2003", defaultProcessor);
    }

    /**
     * Возвращает используемый по умолчанию построитель отчетов.
     * @return  построитель отчетов по умолчанию. Никогда не возвращает <code>null</code>.
     */
    public static ReportProcessor getDefaultProcessor() {
        return defaultProcessor;
    }

    /**
     * Возвращает построитель отчетов идентификатору, под которым он был зарегистрирован в данном реестре.
     * @param processorId  идентификатор построителя отчетов.
     * @return  построитель отчетов или <code>null</code> если в реестре отсутствует зарегистрированный под таким именем построитель.
     */
    public static ReportProcessor getProcessor(final String processorId) {
        return processors.get(processorId);
    }

    /**
     * Регистрирует в реестре новый построитель отчетов под указанным именем.
     * @param processorId  идентификатор построителя отчетов. Не может быть <code>null</code>.
     * @param processor  регистрируемый построитель отчетов. Не может быть <code>null</code>.
     */
    public static void registerProcessor(final String processorId, final ReportProcessor processor) {
        if (processorId==null || processor==null)
            throw new IllegalArgumentException("Valid processor and processor id must be specified");
        processors.put(processorId, processor);
    }

    /**
     * Возвращает информацию по всем зарегистрированным в системе отчетам. Удаление элемента данной коллекции приведет
     * к исключению соответствующего отчета из списка зарегистрированных отчетов. Добавление элементов в коллекцию
     * не поддерживается.
     *
     * @return  перечень всех зарегистрированных отчетов.
     */
    public static Collection<Report> getReports() {
        return reports.values();
    }

    /**
     * Возвращает информацию об ранее зарегистрированном в системе отчете.
     *
     * @param id  идентификатор отчета.
     * @return  модель отчета или <code>null</code> если отчет с таким id не зарегистрирован в системе.
     */
    public static Report getReport(final String id) {
        return reports.get(id);
    }

    /**
     * Регистрирует новый отчет в системе. Если в системе уже был зарегистрирован отчет с таким идентификатором
     * то информация о старом отчете будет удалена.
     *
     * @param report  модель отчета. Не может быть <code>null</code>.
     */
    public static void registerReport(final Report report) {
        if (report==null)
            throw new IllegalArgumentException("Report not specified");
        reports.put(report.getId(), report);
    }


    /**
     * <p>Находит шаблоны и описания отчетов в указанном каталоге и автоматически регистрирует их в системе.</p>
     * <p>Каждый отчет описывается двумя файлами которые отличаются только расширением:
     * <ul>
     *   <li> Excel документ, содержащий шаблон отчета.
     *   <li> XML документ, содержащий  дополнительное описание и структурную разметку отчета.
     * </ul>
     * </p>
     *
     * @param dir  каталог, начиная с которого следует искать декларации отчетов.
     * @param recursive  следует ли искать информацию об отчетах только в указанном каталоге или еще и во всех его дочерних подкаталогах.
     * @param filter  позволяет дополнительно отфильтровать нежелательные файлы. Может быть <code>null</code>.
     * @return количество загруженных в результате выполнения данного метода отчетов.
     * @throws Exception  в случае каких-либо проблем при разборе отчетов.
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
                    result += registerReportsFromDirectory(file, recursive, filter);
                }
                continue;
            }
            final int s = file.getName().lastIndexOf('.');
            final String ext = s >= 0 ? file.getName().substring(s + 1).toLowerCase() : null;
            if (!"xls".equals(ext) && !"xml".equals(ext))
                continue;
            final String name = file.getName().substring(0, s);

            File[] tuple = files.get(name);
            if (tuple == null) {
                tuple = new File[2];
                files.put(name, tuple);
            }

            if ("xls".equals(ext)) {
                tuple[0] = file;
            } else {
                tuple[1] = file;
            }
        }

        Exception cause = null;
        for (File[] tuple : files.values()) {
            if (tuple[0] == null || tuple[1] == null)
                continue;

            Logs.reports.debug("registering report: " + tuple[0].getPath());
            final FileInputStream template = new FileInputStream(tuple[0]);
            try {
                final FileInputStream structure = new FileInputStream(tuple[1]);
                try {
                    final Report report = ReportModelParser.parse(template, structure);
                    reports.put(report.getId(), report);
                    result++;
                } catch (Exception e) {
                    cause = e;
                    Logs.reports.error(e.getMessage(), e);
                } finally {
                    structure.close();
                }
            } finally {
                template.close();
            }
        }

        if (cause != null)
            throw cause;
        return result;
    }

}
