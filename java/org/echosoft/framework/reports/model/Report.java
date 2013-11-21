package org.echosoft.framework.reports.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.echosoft.common.utils.StringUtil;
import org.echosoft.framework.reports.macros.Macros;
import org.echosoft.framework.reports.macros.MacrosRegistry;
import org.echosoft.framework.reports.model.el.Expression;
import org.echosoft.framework.reports.model.events.ReportEventListenerHolder;
import org.echosoft.framework.reports.model.providers.DataProvider;

/**
 * <p>Описывает структуру одного отчета.</p>
 * <p>Каждый отчет может состоять из произвольного количества листов,
 * на каждом из которых может присутствовать произвольное количество разделов.
 * Каждый из таких разделов работает со своим источником данных и может представлять данные в виде
 * некоторых списочных форм различной сложности.</p>
 *
 * @author Anton Sharapov
 */
public class Report implements Serializable {

    /**
     * Перечень возможных форматов в которых может генерироваться отчет.
     */
    public static enum TargetType {
        HSSF, XSSF, SXSSF;
        public static TargetType findByName(final String name, final TargetType defaultType) {
            for (TargetType type : values()) {
                if (type.name().equals(name))
                    return type;
            }
            return defaultType;
        }
    }

    /**
     * Идентификатор отчета.
     */
    private final String id;

    /**
     * Краткое название отчета.
     */
    private String title;

    /**
     * Формат в котором требуется сгенерировать данный отчет.
     */
    private TargetType target;

    /**
     * Логин, который надо указать пользователю работающему с отчетом построенным на основе данного шаблона чтобы изменить данные
     * на тех листах отчета которые были помечены как защищенные (см. {@link SheetModel#isProtected()}).
     * <strong>Внимание!</strong> защита от внесения изменений работает не на всех процессорах электронных таблиц!.
     */
    private Expression user;

    /**
     * Пароль, который может указываться для защиты листов сгенерированного отчета от несанкционированного изменения.
     * <strong>Внимание!</strong> защита от внесения изменений работает не на всех процессорах электронных таблиц!.
     */
    private Expression password;

    /**
     * Шаблон отчета сериализованный в массив байт. Может быть <code>null</code>.
     * Если при построении экземпляра отчета это свойство не равно <code>null</code> то в выходной файл Excel будут
     * включаться все элементы файловой системы POI присутствующие в шаблоне отчета за исключением собственно секций отвечающих
     * за рабочую книгу (Workbook) документа и ее свойства.<br/>
     * Включение или не включение этой информации в итоговый отчет регулируется свойством <code>preserveTemplateData</code> в дескрипторе отчета.
     * По умолчанию эта информация в итоговый отчет не включается.
     */
    private byte[] template;

    /**
     * Дополнительное описание отчета.
     */
    private final ReportDescription description;

    /**
     * Информация по всем листам отчета.
     */
    private final List<SheetModel> sheets;

    /**
     * Перечень всех используемых в отчете стилей оформлений ячеек.
     */
    private final StylePalette palette;

    /**
     * Содержит функции определяемые пользователем которые были объявлены специально для данного отчета.
     */
    private final Map<String, Macros> macros;

    /**
     * Обработчики которые должны вызваться перед началом формирования экземпляра отчета.
     */
    private final List<ReportEventListenerHolder> listeners;

    /**
     * Перечень всех поставщиков данных, которые используются в отчете.
     */
    private final Map<String, DataProvider> providers;


    public Report(String id, final Workbook wb) {
        id = StringUtil.trim(id);
        if (id == null)
            throw new IllegalArgumentException("Report identifier must be specified");
        this.id = id;
        this.target = TargetType.HSSF;
        this.description = new ReportDescription();
        this.sheets = new ArrayList<>();
        this.palette = new StylePalette(wb);
        this.macros = new HashMap<>();
        this.listeners = new ArrayList<>();
        this.providers = new HashMap<>();
    }

    /**
     * Полностью копирует модель отчета взяв за образец модель, переданную в аргументе.
     *
     * @param id  идентификатор отчета.
     * @param src модель отчета используемая в качестве образца.
     * @throws CloneNotSupportedException в случае проблем с клонированием какого-нибудь элемента листа.
     */
    public Report(String id, final Report src) throws CloneNotSupportedException {
        id = StringUtil.trim(id);
        if (src == null)
            throw new IllegalArgumentException("All arguments must be specified");
        this.id = id != null ? id : src.id;
        title = src.title;
        target = src.target;
        user = src.user;
        password = src.password;
        template = src.template;
        description = (ReportDescription) src.description.clone();
        palette = (StylePalette) src.palette.clone();
        macros = new HashMap<>();
        listeners = new ArrayList<>();
        providers = new HashMap<>();
        macros.putAll(src.macros);
        for (ReportEventListenerHolder listener : src.listeners) {
            listeners.add((ReportEventListenerHolder) listener.clone());
        }
        for (Map.Entry<String, DataProvider> entry : src.providers.entrySet()) {
            providers.put(entry.getKey(), (DataProvider) entry.getValue().clone());
        }
        sheets = new ArrayList<>();
        for (SheetModel sheet : src.sheets) {
            sheets.add(sheet.cloneSheet(this));
        }
    }

    /**
     * Возвращает идентификатор отчета.
     *
     * @return идентификатор отчета.
     */
    public String getId() {
        return id;
    }

    /**
     * @return краткое название отчета.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Устанавливает краткое название отчета.
     *
     * @param title новое название отчета.
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * Возвращает формат в котором требуется сгенерировать отчет.
     *
     * @return целевой формат. По умолчанию используется {@link TargetType#HSSF}.
     */
    public TargetType getTarget() {
        return target;
    }

    /**
     * Позволяет указать формат в котором требуется сгенерировать данный отчет.
     *
     * @param target целевой формат. Если <code>null</code> то будет использоваться значение по умолчанию.
     */
    public void setTarget(final TargetType target) {
        this.target = target != null ? target : TargetType.HSSF;
    }


    /**
     * Возвращает логин, который надо указать пользователю перед измением данных в тех листах сгенерированного отчета которые были помечены как "защищенные"
     * (см. {@link SheetModel#isProtected()}).
     * <strong>Внимание!</strong> защита от внесения изменений работает не на всех процессорах электронных таблиц!.
     *
     * @return выражение, вычисленный результат которого используется для определения имени пользователя, которому разрешена правка защищенных листов отчета.
     */
    public Expression getUser() {
        return user;
    }

    /**
     * Определяет логин, который надо указать пользователю перед измением данных в тех листах сгенерированного отчета которые были помечены как "защищенные"
     * (см. {@link SheetModel#isProtected()}).
     * <strong>Внимание!</strong> защита от внесения изменений работает не на всех процессорах электронных таблиц!.
     *
     * @param user выражение, вычисленный результат которого используется для определения имени пользователя, которому разрешена правка защищенных листов отчета.
     */
    public void setUser(final Expression user) {
        this.user = user;
    }

    /**
     * Возвращает пароль, который надо указать пользователю перед измением данных в тех листах сгенерированного отчета которые были помечены как "защищенные"
     * (см. {@link SheetModel#isProtected()}).
     * <strong>Внимание!</strong> защита от внесения изменений работает не на всех процессорах электронных таблиц!.
     *
     * @return выражение, вычисленный результат которого используется для определения пароля, к логину пользователя которому разрешена правка защищенных листов отчета.
     */
    public Expression getPassword() {
        return password;
    }

    /**
     * Устанавливает пароль которым должен быть защищен сгенерированный документ.
     *
     * @param password строка пароля или <code>null</code>.
     */
    public void setPassword(final Expression password) {
        this.password = password;
    }

    /**
     * Шаблон отчета сериализованный в массив байт. Может быть <code>null</code>.
     * Если при построении экземпляра отчета это свойство не равно <code>null</code> то в выходной файл Excel будут
     * включаться все элементы файловой системы POI присутствующие в шаблоне отчета за исключением собственно секций отвечающих
     * за рабочую книгу (Workbook) документа и ее свойства.<br/>
     * Включение или не включение этой информации в итоговый отчет регулируется свойством <code>preserveTemplateData</code> в дескрипторе отчета.
     * По умолчанию эта информация в итоговый отчет не включается.
     *
     * @return Дополнительные секции данных из шаблона отчета которые должны включаться в каждый генерируемый экземпляр отчета.
     */
    public byte[] getTemplate() {
        return template;
    }
    public void setTemplate(final byte[] template) {
        this.template = template;
    }

    /**
     * Возвращает дополнительную информацию, которая при построении отчета будет транслирована в соответствующие
     * свойства документа excel.
     *
     * @return дополнительное описание отчета.
     */
    public ReportDescription getDescription() {
        return description;
    }

    /**
     * Возвращает информацию по всем стилям оформления ячеек используемым в отчете.
     *
     * @return информация по всем используемым стилям ячеек.
     */
    public StylePalette getPalette() {
        return palette;
    }


    /**
     * Осуществляет поиск секции по всему отчету.
     *
     * @param sectionId идентификатор секции.
     * @return информация об указанной секции или null если секция с таким идентификатором отсутствует в отчете.
     */
    public Section findSectionById(final String sectionId) {
        Section result;
        for (SheetModel sheet : sheets) {
            result = sheet.findSectionById(sectionId);
            if (result != null)
                return result;
        }
        return null;
    }

    /**
     * Осуществляет поиск листа отчета по его идентификатору.
     *
     * @param sheetId идентификатор отчета. Не может быть пустой строкой или null.
     * @return информация об искомом листе отчета или <code>null</code> если лист с таким идентификатором отсутствует в отчете.
     */
    public SheetModel findSheetById(final String sheetId) {
        for (SheetModel sheet : sheets) {
            if (sheet.getId().equals(sheetId))
                return sheet;
        }
        return null;
    }

    /**
     * Возвращает список всех листов в отчете строго в той последовательности в которой они
     * должны быть представлены в итоговом отчете.
     *
     * @return список листов отчета.
     */
    public List<SheetModel> getSheets() {
        return sheets;
    }

    /**
     * Возвращает макросы зарегистрированные локально только для данного отчета. Они могут также переопределять глобальные
     * макросы зарегистрированные под тем же именем.
     *
     * @return все локальные макро-функции зарегистрированные для данного вида отчетов.
     */
    public Map<String, Macros> getLocalMacros() {
        return macros;
    }

    /**
     * Возвращает макро-функцию по ее имени. Сначала поиск осуществляется среди функций зарегистрированных локально
     * только для данного отчета. Если таковая функция не была найдена то поиск осуществляется в глобальном
     * {@link MacrosRegistry реестре} макрофункций.
     *
     * @param name имя функции (чуствительно к регистру).
     * @return Соответствующая функция или <code>null</code>.
     */
    public Macros getMacros(final String name) {
        Macros result = macros.get(name);
        if (result == null) {
            result = MacrosRegistry.getMacros(name);
        }
        return result;
    }

    /**
     * Возвращает список всех обработчиков которые вызываются при начале формирования нового экземпляра отчета.
     * Если в отчете не зарегистрировано ни одного обработчика события то метод возвращает пустой список.
     *
     * @return список всех зарегистрированных обработчиков события "формирование нового экземпляра отчета".
     */
    public List<ReportEventListenerHolder> getListeners() {
        return listeners;
    }

    /**
     * Перечень всех поставщиков данных, которые используются в отчете.
     *
     * @return все поставщики данных используемые в отчете.
     */
    public Map<String, DataProvider> getProviders() {
        return providers;
    }


    @Override
    public String toString() {
        return "[Report{id:" + id + ", title:" + title + "}]";
    }
}
