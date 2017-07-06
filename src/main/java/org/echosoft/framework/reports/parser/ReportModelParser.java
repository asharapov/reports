package org.echosoft.framework.reports.parser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.echosoft.common.utils.Any;
import org.echosoft.common.utils.StringUtil;
import org.echosoft.common.utils.XMLUtil;
import org.echosoft.framework.reports.model.AreaModel;
import org.echosoft.framework.reports.model.ColumnGroupModel;
import org.echosoft.framework.reports.model.CompositeSection;
import org.echosoft.framework.reports.model.GroupModel;
import org.echosoft.framework.reports.model.GroupStyle;
import org.echosoft.framework.reports.model.GroupingSection;
import org.echosoft.framework.reports.model.NamedRegion;
import org.echosoft.framework.reports.model.PageSettingsModel;
import org.echosoft.framework.reports.model.PlainSection;
import org.echosoft.framework.reports.model.PrintSetupModel;
import org.echosoft.framework.reports.model.Report;
import org.echosoft.framework.reports.model.ReportDescription;
import org.echosoft.framework.reports.model.Section;
import org.echosoft.framework.reports.model.SheetModel;
import org.echosoft.framework.reports.model.el.BaseExpression;
import org.echosoft.framework.reports.model.events.CellEventListenerHolder;
import org.echosoft.framework.reports.model.events.ReportEventListenerHolder;
import org.echosoft.framework.reports.model.events.SectionEventListenerHolder;
import org.echosoft.framework.reports.model.providers.ClassDataProvider;
import org.echosoft.framework.reports.model.providers.DataProvider;
import org.echosoft.framework.reports.model.providers.FilteredDataProvider;
import org.echosoft.framework.reports.model.providers.ListDataProvider;
import org.echosoft.framework.reports.model.providers.ProviderUsage;
import org.echosoft.framework.reports.model.providers.ProxyDataProvider;
import org.echosoft.framework.reports.model.providers.SQLDataProvider;
import org.echosoft.framework.reports.util.Logs;
import org.echosoft.framework.reports.util.POIUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Предназначен для сбора информации об отчете (его модели) на основании двух конфигурационных файлов:
 * <li> excel шаблона отчета.
 * <li> xml файла с описанием структуры отчета.
 *
 * @author Anton Sharapov
 */
public class ReportModelParser {

    /**
     * Выполняет операцию чтения структуры информации и ее последующей компиляции.
     *
     * @param template  шаблон отчета в формате Excel.
     * @param structure описание структуры шаблона в формате XML.
     * @return полная структура отчета.
     * @throws Exception в случае каких-либо проблем.
     */
    public static Report parse(final InputStream template, final InputStream structure) throws Exception {
        return parse(template, structure, null);
    }

    /**
     * Выполняет операцию чтения структуры информации и ее последующей компиляции.
     *
     * @param template  шаблон отчета в формате Excel.
     * @param structure описание структуры шаблона в формате XML.
     * @param extensions список расширений к которым парсер может обращаться если встретит незнакомый ему элемент описания структуры отчета. Может быть null.
     * @return полная структура отчета.
     * @throws Exception в случае каких-либо проблем.
     */
    public static Report parse(final InputStream template, final InputStream structure, final List<ReportExtension> extensions) throws Exception {
        // загружаем шаблон отчета ...
        final Workbook wb = WorkbookFactory.create(template);
        // загружаем информацию о структуре отчета
        // (подробное описание структуры шаблона отчета)
        final Document doc = XMLUtil.loadDocument(structure);
        final Element root = doc.getDocumentElement();
        // формируем заголовок отчета и начинаем подгружать его содержимое ...
        final Report report = new Report(StringUtil.trim(root.getAttribute("id")), wb);
        try {
            report.setTitle(StringUtil.trim(root.getAttribute("title")));
            final String targetName = StringUtil.trim(root.getAttribute("target"));
            final Report.TargetType target = targetName != null
                    ? Report.TargetType.findByName(targetName, null)
                    : Report.TargetType.HSSF;
            if (target == null)
                throw new RuntimeException("Unknown target type: " + targetName);
            report.setTarget(target);
            report.setUser(new BaseExpression(StringUtil.getNonEmpty(root.getAttribute("user"), "user")));
            report.setPassword(new BaseExpression(StringUtil.trim(root.getAttribute("password"))));
            report.setStreamWindowSize(Any.asInt(StringUtil.trim(root.getAttribute("stream-window-size")), 1000));
            report.setStreamUseCompression(Any.asBoolean(StringUtil.trim(root.getAttribute("stream-use-compression")), false));

            for (Element element : XMLUtil.getChildElements(root)) {
                final String tagName = element.getTagName();
                switch (tagName) {
                    case "description":
                        parseDescription(report, element);
                        break;
                    case "proxy-data-provider":
                        parseProxyDataProvider(report, element);
                        break;
                    case "filtered-data-provider":
                        parseFilteredDataProvider(report, element);
                        break;
                    case "list-data-provider":
                        parseListDataProvider(report, element);
                        break;
                    case "sql-data-provider":
                        parseSQLDataProvider(report, element);
                        break;
                    case "class-data-provider":
                        parseClassDataProvider(report, element);
                        break;
                    case "report-listener":
                        parseReportEventListener(report, element);
                        break;
                    case "sheet":
                        final SheetModel sheet = parseSheet(wb, report, element);
                        if (report.findSheetById(sheet.getId()) != null)
                            throw new RuntimeException("Sheet " + sheet.getId() + " already exists in report " + report.getId());
                        report.getSheets().add(sheet);
                        break;
                    default:
                        parseUnknownElement(report, extensions, element);
                }
            }

            if (Boolean.valueOf(root.getAttribute("preserveTemplate"))) {
                preserveTemplate(report, wb);
            }
            if (extensions != null) {
                for (ReportExtension extension : extensions) {
                    extension.onConfig(report);
                }
            }

            return report;
        } catch (Exception e) {
            throw new Exception("Unable to parse report [" + report.getId() + "] model: " + e.getMessage(), e);
        }
    }

    private static void parseUnknownElement(final Report report, final List<ReportExtension> extensions, final Element element) throws Exception {
        if (extensions != null) {
            for (ReportExtension extension : extensions) {
                final boolean ok = extension.handleConfigElement(report, element);
                if (ok)
                    return;
            }
        }
        throw new RuntimeException("Unknown element: " + element.getTagName());
    }

    private static void parseDescription(final Report report, final Element element) {
        final ReportDescription desc = report.getDescription();
        for (Element el : XMLUtil.getChildElements(element)) {
            final String tagName = el.getTagName();
            switch (tagName) {
                case "company":
                    desc.setCompany(new BaseExpression(XMLUtil.getNodeText(el)));
                    break;
                case "category":
                    desc.setCategory(new BaseExpression(XMLUtil.getNodeText(el)));
                    break;
                case "application":
                    desc.setApplication(new BaseExpression(XMLUtil.getNodeText(el)));
                    break;
                case "author":
                    desc.setAuthor(new BaseExpression(XMLUtil.getNodeText(el)));
                    break;
                case "version":
                    desc.setVersion(new BaseExpression(XMLUtil.getNodeText(el)));
                    break;
                case "title":
                    desc.setTitle(new BaseExpression(XMLUtil.getNodeText(el)));
                    break;
                case "subject":
                    desc.setSubject(new BaseExpression(XMLUtil.getNodeText(el)));
                    break;
                case "comments":
                    desc.setComments(new BaseExpression(XMLUtil.getNodeText(el)));
                    break;
                default:
                    throw new RuntimeException("Unknown element: " + tagName);
            }
        }
    }

    private static void parseProxyDataProvider(final Report report, final Element element) {
        final String id = StringUtil.trim(element.getAttribute("id"));
        final String ref = StringUtil.trim(element.getAttribute("ref"));
        if (id == null || ref == null)
            throw new RuntimeException("Mandatory attributes not specified: " + element);
        final DataProvider provider = new ProxyDataProvider(id, new BaseExpression(ref));
        report.getProviders().put(id, provider);
    }

    private static void parseFilteredDataProvider(final Report report, final Element element) {
        final String id = StringUtil.trim(element.getAttribute("id"));
        final String predicate = StringUtil.trim(element.getAttribute("predicate"));
        if (id == null || predicate == null)
            throw new RuntimeException("Mandatory attributes not specified: " + element);
        final FilteredDataProvider result = new FilteredDataProvider(id);
        result.setPredicate(new BaseExpression(predicate));
        report.getProviders().put(id, result);
    }

    private static void parseListDataProvider(final Report report, final Element element) {
        final String id = StringUtil.trim(element.getAttribute("id"));
        final String data = StringUtil.trim(element.getAttribute("data"));
        if (id == null || data == null)
            throw new RuntimeException("Mandatory attributes not specified: " + element);
        final ListDataProvider result = new ListDataProvider(id);
        result.setData(new BaseExpression(data));
        final Iterator<Element> children = XMLUtil.getChildElements(element).iterator();
        if (children.hasNext()) {
            throw new RuntimeException("Unsupported element: " + children.next().getTagName());
        }
        report.getProviders().put(id, result);
    }

    private static void parseSQLDataProvider(final Report report, final Element element) {
        final String id = StringUtil.trim(element.getAttribute("id"));
        final String ds = StringUtil.trim(element.getAttribute("datasource"));
        if (id == null || ds == null)
            throw new RuntimeException("Mandatory attributes not specified: " + element);
        final SQLDataProvider result = new SQLDataProvider(id);
        result.setDataSource(new BaseExpression(ds));
        for (Element el : XMLUtil.getChildElements(element)) {
            final String tagName = el.getTagName();
            switch (tagName) {
                case "sql":
                    final String sql = StringUtil.trim(XMLUtil.getNodeText(el));
                    result.setSQL(new BaseExpression(sql));
                    break;
                case "sql-ref":
                    final String sqlref = StringUtil.trim(XMLUtil.getNodeText(el));
                    result.setSQLReference(new BaseExpression(sqlref));
                    break;
                case "params":
                    final String paramsMap = StringUtil.trim(XMLUtil.getNodeText(el));
                    result.setParamsMap(new BaseExpression(paramsMap));
                    break;
                case "param":
                    final String name = StringUtil.trim(el.getAttribute("name"));
                    final String value = StringUtil.trim(el.getAttribute("value"));
                    if (name != null)
                        result.addParam(new BaseExpression(name), new BaseExpression(value));
                    break;
                default:
                    throw new RuntimeException("Unknown element: " + tagName);
            }
        }
        report.getProviders().put(id, result);
    }

    private static void parseClassDataProvider(final Report report, final Element element) {
        final String id = StringUtil.trim(element.getAttribute("id"));
        final String object = StringUtil.trim(element.getAttribute("object"));
        final String method = StringUtil.trim(element.getAttribute("method"));
        if (id == null || object == null || method == null)
            throw new RuntimeException("Mandatory attributes not specified: " + element);
        final ClassDataProvider result = new ClassDataProvider(id);
        result.setObject(new BaseExpression(object));
        result.setMethodName(new BaseExpression(method));
        for (Element el : XMLUtil.getChildElements(element)) {
            final String tagName = el.getTagName();
            switch (tagName) {
                case "arg":
                    final String arg = StringUtil.trim(XMLUtil.getNodeText(el));
                    result.setArg(new BaseExpression(arg));
                    break;
                case "arg-class":
                    final String argClass = StringUtil.trim(XMLUtil.getNodeText(el));
                    result.setArgType(new BaseExpression(argClass));
                    break;
                default:
                    throw new RuntimeException("Unknown element: " + tagName);
            }
        }
        report.getProviders().put(id, result);
    }


    private static SheetModel parseSheet(final Workbook wb, final Report report, final Element element) {
        final String id = StringUtil.trim(element.getAttribute("id"));
        final Sheet esheet = wb.getSheet(id);
        if (esheet == null)
            throw new RuntimeException("Template doesn't contains sheet " + id);
        final SheetModel sheet = new SheetModel(id);
        sheet.setTitle(BaseExpression.makeExpression(StringUtil.trim(element.getAttribute("title"))));
        sheet.setHidden(Any.asBoolean(StringUtil.trim(element.getAttribute("hidden")), false));
        sheet.setRendered(Any.asBoolean(StringUtil.trim(element.getAttribute("rendered")), true));
        sheet.setProtected(Any.asBoolean(StringUtil.trim(element.getAttribute("protected")), false));
        final String cgs = StringUtil.trim(element.getAttribute("group-columns"));
        if (cgs != null) {
            for (StringTokenizer it = new StringTokenizer(cgs, ","); it.hasMoreElements(); ) {
                final String token = StringUtil.trim(it.nextToken());
                if (token == null)
                    continue;
                final List<String> cnames = StringUtil.split(token, '-');
                if (cnames.size() != 2 || cnames.get(0).length() == 0 || cnames.get(1).length() == 0)
                    throw new IllegalArgumentException("Incorrect value for attribute 'column-groups': " + cgs);
                final int c1 = POIUtils.getColumnNumber(cnames.get(0));
                final int c2 = POIUtils.getColumnNumber(cnames.get(1));
                sheet.addColumnGroup(new ColumnGroupModel(c1, c2));
            }
        }
        copyPageSettings(sheet.getPageSettings(), esheet);
        final String zoomstr = StringUtil.trim(element.getAttribute("zoom"));
        if (zoomstr != null)
            sheet.getPageSettings().setZoom(Any.asInt(zoomstr, 100));
        int offset = 0;
        for (Element el : XMLUtil.getChildElements(element)) {
            final String tagName = el.getTagName();
            switch (tagName) {
                case "plain-section": {
                    final Section section = parsePlainSection(esheet, offset, report, el);
                    if (sheet.findSectionById(section.getId()) != null)
                        throw new RuntimeException("Section " + section.getId() + " already exists in sheet " + sheet.getId());
                    sheet.getSections().add(section);
                    offset += section.getTemplateRowsCount();
                    break;
                }
                case "grouping-section": {
                    final Section section = parseGroupingSection(esheet, offset, report, el);
                    if (sheet.findSectionById(section.getId()) != null)
                        throw new RuntimeException("Section " + section.getId() + " already exists in sheet " + sheet.getId());
                    sheet.getSections().add(section);
                    offset += section.getTemplateRowsCount();
                    break;
                }
                case "composite-section": {
                    final Section section = parseCompositeSection(esheet, offset, report, el);
                    if (sheet.findSectionById(section.getId()) != null)
                        throw new RuntimeException("Section " + section.getId() + " already exists in sheet " + sheet.getId());
                    sheet.getSections().add(section);
                    offset += section.getTemplateRowsCount();
                    break;
                }
                default:
                    throw new RuntimeException("Unknown element: " + tagName);
            }
        }
        final int colcount = sheet.getColumnsCount();
        final int[] width = new int[colcount];
        final boolean[] hidden = new boolean[colcount];
        for (int i = 0; i < colcount; i++) {
            width[i] = esheet.getColumnWidth(i);
            hidden[i] = esheet.isColumnHidden(i);
        }
        sheet.setColumnWidths(width);
        sheet.setColumnHidden(hidden);
        return sheet;
    }

    private static void copyPageSettings(final PageSettingsModel pageSettings, final Sheet esheet) {
        pageSettings.getHeader().setLeft(esheet.getHeader().getLeft());
        pageSettings.getHeader().setCenter(esheet.getHeader().getCenter());
        pageSettings.getHeader().setRight(esheet.getHeader().getRight());
        pageSettings.getFooter().setLeft(esheet.getFooter().getLeft());
        pageSettings.getFooter().setCenter(esheet.getFooter().getCenter());
        pageSettings.getFooter().setRight(esheet.getFooter().getRight());
        pageSettings.getMargins().setTop(esheet.getMargin(Sheet.TopMargin));
        pageSettings.getMargins().setRight(esheet.getMargin(Sheet.RightMargin));
        pageSettings.getMargins().setBottom(esheet.getMargin(Sheet.BottomMargin));
        pageSettings.getMargins().setLeft(esheet.getMargin(Sheet.LeftMargin));
        copyPrintSetup(pageSettings.getPrintSetup(), esheet.getPrintSetup());
        pageSettings.setFitToPage(esheet.getFitToPage());
        pageSettings.setHorizontallyCenter(esheet.getHorizontallyCenter());
        pageSettings.setVerticallyCenter(esheet.getVerticallyCenter());
    }
    private static void copyPrintSetup(final PrintSetupModel modelPrintSetup, final PrintSetup printSetup) {
        modelPrintSetup.setPaperSize(printSetup.getPaperSize());
        modelPrintSetup.setScale(printSetup.getScale());
        modelPrintSetup.setFitWidth(printSetup.getFitWidth());
        modelPrintSetup.setPageStart(printSetup.getPageStart());
        modelPrintSetup.setFitHeight(printSetup.getFitHeight());
        modelPrintSetup.setFooterMargin(printSetup.getFooterMargin());
        modelPrintSetup.setLandscape(printSetup.getLandscape());
        modelPrintSetup.setLeftToRight(printSetup.getLeftToRight());
        modelPrintSetup.setNoColor(printSetup.getNoColor());
        modelPrintSetup.setDraft(printSetup.getDraft());
        modelPrintSetup.setHResolution(printSetup.getHResolution());
        modelPrintSetup.setNotes(printSetup.getNotes());
        modelPrintSetup.setUsePage(printSetup.getUsePage());
        modelPrintSetup.setVResolution(printSetup.getVResolution());
        modelPrintSetup.setCopies(printSetup.getCopies());
        modelPrintSetup.setHeaderMargin(printSetup.getHeaderMargin());
        modelPrintSetup.setValidSettings(printSetup.getValidSettings());
        modelPrintSetup.setNoOrientation(printSetup.getNoOrientation());
    }

    private static Section parsePlainSection(final Sheet sheet, final int offset, final Report report, final Element element) {
        final String id = StringUtil.trim(element.getAttribute("id"));
        final PlainSection section = new PlainSection(id);
        section.setCollapsible(Any.asBoolean(element.getAttribute("collapsible"), false));
        section.setCollapsed(Any.asBoolean(element.getAttribute("collapsed"), false));
        section.setHidden(Any.asBoolean(element.getAttribute("hidden"), false));
        section.setRendered(Any.asBoolean(StringUtil.trim(element.getAttribute("rendered")), true));
        section.setFiltering(Any.asBoolean(StringUtil.trim(element.getAttribute("filtering")), false));
        final String pid = StringUtil.trim(element.getAttribute("provider"));
        if (pid != null)
            section.setDataProvider(report.getProviders().get(pid));
        final int height = Any.asInt(element.getAttribute("height"), 1);
        final int lastColumn = POIUtils.getColumnNumber(StringUtil.trim(element.getAttribute("lastColumn")));
        final boolean autoRowHeight = Any.asBoolean(StringUtil.trim(element.getAttribute("autoRowHeight")), false);
        section.setTemplate(new AreaModel(sheet, offset, height, lastColumn, autoRowHeight, report));
        section.getTemplate().setHidden(section.isHidden());
        for (Element el : XMLUtil.getChildElements(element)) {
            final String tagName = el.getTagName();
            switch (tagName) {
                case "named-region":
                    parseNamedRegion(section, el);
                    break;
                case "section-listener":
                    parseSectionEventListener(section, el);
                    break;
                case "cell-listener":
                    parseCellEventListener(section, el);
                    break;
                default:
                    throw new RuntimeException("Unknown element: " + tagName);
            }
        }
        return section;
    }

    private static Section parseGroupingSection(final Sheet sheet, final int offset, final Report report, final Element element) {
        final String id = StringUtil.trim(element.getAttribute("id"));
        final GroupingSection section = new GroupingSection(id);
        section.setCollapsible(Any.asBoolean(element.getAttribute("collapsible"), false));
        section.setCollapsed(Any.asBoolean(element.getAttribute("collapsed"), false));
        section.setHidden(Any.asBoolean(element.getAttribute("hidden"), false));
        section.setRendered(Any.asBoolean(StringUtil.trim(element.getAttribute("rendered")), true));
        section.setFiltering(Any.asBoolean(StringUtil.trim(element.getAttribute("filtering")), false));
        final String pid = StringUtil.trim(element.getAttribute("provider"));
        if (pid != null)
            section.setDataProvider(report.getProviders().get(pid));
        final String[] colnames = Any.asStringArray(StringUtil.trim(element.getAttribute("indentColumns")), null);
        section.setIndentedColumns(colnames);
        final int rowHeight = Any.asInt(element.getAttribute("rowHeight"), 1);
        final int lastColumn = POIUtils.getColumnNumber(StringUtil.trim(element.getAttribute("lastColumn")));
        final boolean autoRowHeight = Any.asBoolean(StringUtil.trim(element.getAttribute("autoRowHeight")), false);
        int height = 0;
        for (Element el : XMLUtil.getChildElements(element)) {
            final String tagName = el.getTagName();
            switch (tagName) {
                case "named-region":
                    parseNamedRegion(section, el);
                    break;
                case "section-listener":
                    parseSectionEventListener(section, el);
                    break;
                case "cell-listener":
                    parseCellEventListener(section, el);
                    break;
                case "group":
                    final GroupModel group = parseGroup(sheet, offset + height, report, el);
                    section.getGroups().add(group);
                    height += group.getStylesCount() * group.getRowsCount();
                    break;
                default:
                    throw new RuntimeException("Unknown element: " + tagName);
            }
        }
        section.setRowTemplate(new AreaModel(sheet, offset + height, rowHeight, lastColumn, autoRowHeight, report));
        return section;
    }

    private static Section parseCompositeSection(final Sheet sheet, final int offset, final Report report, final Element element) {
        final String id = StringUtil.trim(element.getAttribute("id"));
        final CompositeSection section = new CompositeSection(id);
        section.setCollapsible(Any.asBoolean(StringUtil.trim(element.getAttribute("collapsible")), false));
        section.setCollapsed(Any.asBoolean(StringUtil.trim(element.getAttribute("collapsed")), false));
        section.setHidden(Any.asBoolean(StringUtil.trim(element.getAttribute("hidden")), false));
        section.setRendered(Any.asBoolean(StringUtil.trim(element.getAttribute("rendered")), true));
        section.setFiltering(Any.asBoolean(StringUtil.trim(element.getAttribute("filtering")), false));
        final String pid = StringUtil.trim(element.getAttribute("provider"));
        if (pid != null)
            section.setDataProvider(report.getProviders().get(pid));
        final String pu = StringUtil.trim(element.getAttribute("provider-usage"));
        section.setProviderUsage(pu != null ? ProviderUsage.valueOf(pu.toUpperCase()) : null);
        final String[] colnames = Any.asStringArray(StringUtil.trim(element.getAttribute("indentColumns")), null);
        section.setIndentedColumns(colnames);
        int height = 0;
        for (Element el : XMLUtil.getChildElements(element)) {
            final String tagName = el.getTagName();
            switch (tagName) {
                case "named-region":
                    parseNamedRegion(section, el);
                    break;
                case "section-listener":
                    parseSectionEventListener(section, el);
                    break;
                case "cell-listener":
                    parseCellEventListener(section, el);
                    break;
                case "group":
                    final GroupModel group = parseGroup(sheet, offset + height, report, el);
                    section.getGroups().add(group);
                    height += group.getStylesCount() * group.getRowsCount();
                    break;
                case "plain-section": {
                    final Section child = parsePlainSection(sheet, offset + height, report, el);
                    section.getSections().add(child);
                    height += child.getTemplateRowsCount();
                    break;
                }
                case "grouping-section": {
                    final Section child = parseGroupingSection(sheet, offset + height, report, el);
                    section.getSections().add(child);
                    height += child.getTemplateRowsCount();
                    break;
                }
                case "composite-section": {
                    final Section child = parseCompositeSection(sheet, offset + height, report, el);
                    section.getSections().add(child);
                    height += child.getTemplateRowsCount();
                    break;
                }
                default:
                    throw new RuntimeException("Unknown element: " + tagName);
            }
        }
        return section;
    }

    private static NamedRegion parseNamedRegion(final Section section, final Element element) {
        final String name = StringUtil.trim(element.getAttribute("name"));
        final int firstColumn = POIUtils.getColumnNumber(StringUtil.trim(element.getAttribute("firstColumn")), 0);
        final int lastColumn = POIUtils.getColumnNumber(StringUtil.trim(element.getAttribute("lastColumn")), section.getTemplateColumnsCount() - 1);
        final NamedRegion namedRegion = new NamedRegion(name, firstColumn, lastColumn);
        namedRegion.setComment(StringUtil.trim(element.getAttribute("comment")));
        section.getNamedRegions().add(namedRegion);
        return namedRegion;
    }

    private static GroupModel parseGroup(final Sheet sheet, int offset, final Report report, final Element element) {
        final GroupModel group = new GroupModel();
        group.setDiscriminatorField(StringUtil.trim(element.getAttribute("discriminatorField")));
        group.setLevelField(StringUtil.trim(element.getAttribute("levelField")));
        group.setCollapsible(Any.asBoolean(StringUtil.trim(element.getAttribute("collapsible")), true));
        group.setCollapsed(Any.asBoolean(StringUtil.trim(element.getAttribute("collapsed")), false));
        group.setHidden(Any.asBoolean(StringUtil.trim(element.getAttribute("hidden")), false));
        group.setSkipEmptyGroups(Any.asBoolean(StringUtil.trim(element.getAttribute("skipEmptyGroups")), false));
        final int height = Any.asInt(StringUtil.trim(element.getAttribute("height")), 1);
        final int lastColumn = POIUtils.getColumnNumber(StringUtil.trim(element.getAttribute("lastColumn")));
        final boolean autoRowHeight = Any.asBoolean(StringUtil.trim(element.getAttribute("autoRowHeight")), false);
        for (Element el : XMLUtil.getChildElements(element)) {
            final String tagName = el.getTagName();
            if ("group-style".equals(tagName)) {
                final GroupStyle style = new GroupStyle();
                style.setLevel(Any.asInt(StringUtil.trim(el.getAttribute("level")), 0));
                style.setDefault(Any.asBoolean(StringUtil.trim(el.getAttribute("default")), false));
                style.setTemplate(new AreaModel(sheet, offset, height, lastColumn, autoRowHeight, report));
                style.getTemplate().setHidden(group.isHidden());
                group.addStyle(style);
                offset += height;
            } else
                throw new RuntimeException("Unknown element: " + tagName);
        }
        if (group.getStylesCount() == 0) {
            final GroupStyle style = new GroupStyle();
            style.setLevel(0);
            style.setDefault(true);
            style.setTemplate(new AreaModel(sheet, offset, height, lastColumn, autoRowHeight, report));
            style.getTemplate().setHidden(group.isHidden());
            group.addStyle(style);
        }
        return group;
    }


    private static void parseReportEventListener(final Report report, final Element element) {
        final String className = StringUtil.trim(element.getAttribute("class"));
        final String instance = StringUtil.trim(element.getAttribute("instance"));
        if (className == null && instance == null)
            throw new RuntimeException("Listener's class or instance must be specified");
        report.getListeners().add(new ReportEventListenerHolder(new BaseExpression(className), new BaseExpression(instance)));
    }

    private static void parseSectionEventListener(final Section section, final Element element) {
        final String className = StringUtil.trim(element.getAttribute("class"));
        final String instance = StringUtil.trim(element.getAttribute("instance"));
        if (className == null && instance == null)
            throw new RuntimeException("Listener's class or instance must be specified");
        section.getSectionListeners().add(new SectionEventListenerHolder(new BaseExpression(className), new BaseExpression(instance)));
    }

    private static void parseCellEventListener(final Section section, final Element element) {
        final String className = StringUtil.trim(element.getAttribute("class"));
        final String instance = StringUtil.trim(element.getAttribute("instance"));
        if (className == null && instance == null)
            throw new RuntimeException("Listener's class or instance must be specified");
        section.getCellListeners().add(new CellEventListenerHolder(new BaseExpression(className), new BaseExpression(instance)));
    }

    private static void preserveTemplate(final Report report, final Workbook wb) throws IOException {
        for (SheetModel sm : report.getSheets()) {
            if (sm.getTitle() != null) {
                final int idx = wb.getSheetIndex(sm.getId());
                wb.removeSheetAt(idx);
            } else {
                final Sheet sheet = wb.getSheet(sm.getId());
                POIUtils.removeAllRows(sheet);
            }
        }
        if (wb instanceof HSSFWorkbook) {
            final HSSFWorkbook hwb = (HSSFWorkbook) wb;
            final byte[] data = hwb.getBytes();
            final String[] shouldBeDropped = {"Workbook", "WORKBOOK", SummaryInformation.DEFAULT_STREAM_NAME, DocumentSummaryInformation.DEFAULT_STREAM_NAME};
            final DirectoryNode directoryNode = hwb.getDirectory();
            for (String entryName : shouldBeDropped) {
                try {
                    final Entry entry = directoryNode.getEntry(entryName);
                    if (entry != null) {
                        if (!entry.delete())
                            Logs.reports.warn("unable to delete POIFS section: '" + entryName + "'  (" + entry + ")");
                    }
                } catch (FileNotFoundException ffe) {
                    // Секция с указанным именем отсутствует в иерархии. Просто перейдем к следующей в списке ...
                }
            }
            directoryNode.createDocument("Workbook", new ByteArrayInputStream(data));
            final ByteArrayOutputStream buf = new ByteArrayOutputStream(4096);
            if (directoryNode.getFileSystem() != null) {
                directoryNode.getFileSystem().writeFilesystem(buf);
            } else
            if (directoryNode.getNFileSystem() != null) {
                directoryNode.getNFileSystem().writeFilesystem(buf);
            }
            report.setTemplate(buf.toByteArray());
        } else {
            final ByteArrayOutputStream buf = new ByteArrayOutputStream(8192);
            wb.write(buf);
            report.setTemplate(buf.toByteArray());
        }
    }
}
