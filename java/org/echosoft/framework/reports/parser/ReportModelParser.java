package org.echosoft.framework.reports.parser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.echosoft.common.io.FastStringTokenizer;
import org.echosoft.common.utils.Any;
import org.echosoft.common.utils.StringUtil;
import org.echosoft.common.utils.XMLUtil;
import org.echosoft.framework.reports.model.AreaModel;
import org.echosoft.framework.reports.model.ColumnGroupModel;
import org.echosoft.framework.reports.model.CompositeSection;
import org.echosoft.framework.reports.model.GroupModel;
import org.echosoft.framework.reports.model.GroupStyle;
import org.echosoft.framework.reports.model.GroupingSection;
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
import org.echosoft.framework.reports.model.providers.ClassDataProviderHolder;
import org.echosoft.framework.reports.model.providers.FilteredDataProviderHolder;
import org.echosoft.framework.reports.model.providers.ListDataProviderHolder;
import org.echosoft.framework.reports.model.providers.ProviderUsage;
import org.echosoft.framework.reports.model.providers.SQLDataProviderHolder;
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
            for (Iterator<Element> i = XMLUtil.getChildElements(root); i.hasNext(); ) {
                final Element element = i.next();
                final String tagName = element.getTagName();
                if ("description".equals(tagName)) {
                    parseDescription(report, element);
                } else
                if ("filtered-data-provider".equals(tagName)) {
                    parseFilteredDataProvider(report, element);
                } else
                if ("list-data-provider".equals(tagName)) {
                    parseListDataProvider(report, element);
                } else
                if ("sql-data-provider".equals(tagName)) {
                    parseSQLDataProvider(report, element);
                } else
                if ("class-data-provider".equals(tagName)) {
                    parseClassDataProvider(report, element);
                } else
                if ("report-listener".equals(tagName)) {
                    parseReportEventListener(report, element);
                } else
                if ("sheet".equals(tagName)) {
                    final SheetModel sheet = parseSheet(wb, report, element);
                    if (report.findSheetById(sheet.getId()) != null)
                        throw new RuntimeException("Sheet " + sheet.getId() + " already exists in report " + report.getId());
                    report.getSheets().add(sheet);
                } else
                    throw new RuntimeException("Unknown element: " + tagName);
            }

            final boolean preserveTemplate = Any.asBoolean(StringUtil.trim(root.getAttribute("preserveTemplate")), false);
            if (preserveTemplate) {
                for (int i = wb.getNumberOfSheets(); i > 0; i--) {
                    wb.removeSheetAt(wb.getNumberOfSheets() - 1);
                }
                if (wb instanceof HSSFWorkbook) {
                    final HSSFWorkbook hwb = (HSSFWorkbook) wb;
                    final byte[] data = hwb.getBytes();
                    final String[] shouldBeDropped = {"Workbook", "WORKBOOK", SummaryInformation.DEFAULT_STREAM_NAME, DocumentSummaryInformation.DEFAULT_STREAM_NAME};
                    for (String entryName : shouldBeDropped) {
                        try {
                            final Entry entry = hwb.getRootDirectory().getEntry(entryName);
                            if (entry != null) {
                                if (!entry.delete())
                                    Logs.reports.warn("unable to delete POIFS section: '" + entryName + "'  (" + entry + ")");
                            }
                        } catch (FileNotFoundException ffe) {
                            // Секция с указанным именем отсутствует в иерархии. Просто перейдем к следующей в списке ...
                        }
                    }
                    hwb.getRootDirectory().createDocument("Workbook", new ByteArrayInputStream(data));
                    final ByteArrayOutputStream buf = new ByteArrayOutputStream(4096);
                    hwb.getRootDirectory().getFileSystem().writeFilesystem(buf);
                    report.setTemplate(buf.toByteArray());
                } else
                if (wb instanceof XSSFWorkbook) {
                    final XSSFWorkbook xwb = (XSSFWorkbook)wb;
                    final OPCPackage pkg = xwb.getPackage();
                    final ByteArrayOutputStream buf = new ByteArrayOutputStream(8192);
                    pkg.save(buf);
                    report.setTemplate(buf.toByteArray());
                } else
                    throw new IllegalArgumentException("Unknown workbook implementation: " + wb);
            }
            return report;
        } catch (Exception e) {
            throw new Exception("Unable to parse report [" + report.getId() + "] model: " + e.getMessage(), e);
        }
    }


    private static void parseDescription(final Report report, final Element element) {
        final ReportDescription desc = report.getDescription();
        for (Iterator<Element> i = XMLUtil.getChildElements(element); i.hasNext(); ) {
            final Element el = i.next();
            final String tagName = el.getTagName();
            if ("company".equals(tagName)) {
                desc.setCompany(new BaseExpression(XMLUtil.getNodeText(el)));
            } else
            if ("category".equals(tagName)) {
                desc.setCategory(new BaseExpression(XMLUtil.getNodeText(el)));
            } else
            if ("application".equals(tagName)) {
                desc.setApplication(new BaseExpression(XMLUtil.getNodeText(el)));
            } else
            if ("author".equals(tagName)) {
                desc.setAuthor(new BaseExpression(XMLUtil.getNodeText(el)));
            } else
            if ("version".equals(tagName)) {
                desc.setVersion(new BaseExpression(XMLUtil.getNodeText(el)));
            } else
            if ("title".equals(tagName)) {
                desc.setTitle(new BaseExpression(XMLUtil.getNodeText(el)));
            } else
            if ("subject".equals(tagName)) {
                desc.setSubject(new BaseExpression(XMLUtil.getNodeText(el)));
            } else
            if ("comments".equals(tagName)) {
                desc.setComments(new BaseExpression(XMLUtil.getNodeText(el)));
            } else
                throw new RuntimeException("Unknown element: " + tagName);
        }
    }

    private static void parseFilteredDataProvider(final Report report, final Element element) {
        final String id = StringUtil.trim(element.getAttribute("id"));
        final String predicate = StringUtil.trim(element.getAttribute("predicate"));
        if (id == null || predicate == null)
            throw new RuntimeException("Mandatory attributes not specified: " + element);
        final FilteredDataProviderHolder result = new FilteredDataProviderHolder(id);
        result.setPredicate(new BaseExpression(predicate));
        report.getProviders().put(id, result);
    }

    private static void parseListDataProvider(final Report report, final Element element) {
        final String id = StringUtil.trim(element.getAttribute("id"));
        final String data = StringUtil.trim(element.getAttribute("data"));
        if (id == null || data == null)
            throw new RuntimeException("Mandatory attributes not specified: " + element);
        final ListDataProviderHolder result = new ListDataProviderHolder(id);
        result.setData(new BaseExpression(data));
        for (Iterator<Element> i = XMLUtil.getChildElements(element); i.hasNext(); ) {
            final Element el = i.next();
            final String tagName = el.getTagName();
            if ("filter".equals(tagName)) {
                final String filter = StringUtil.trim(XMLUtil.getNodeText(el));
                result.setFilter(new BaseExpression(filter));
            } else
            if ("params".equals(tagName)) {
                final String paramsMap = StringUtil.trim(XMLUtil.getNodeText(el));
                result.setParamsMap(new BaseExpression(paramsMap));
            } else
            if ("param".equals(tagName)) {
                final String name = StringUtil.trim(el.getAttribute("name"));
                final String value = StringUtil.trim(el.getAttribute("value"));
                if (name != null)
                    result.addParam(new BaseExpression(name), new BaseExpression(value));
            } else
                throw new RuntimeException("Unknown element: " + tagName);
        }
        report.getProviders().put(id, result);
    }

    private static void parseSQLDataProvider(final Report report, final Element element) {
        final String id = StringUtil.trim(element.getAttribute("id"));
        final String ds = StringUtil.trim(element.getAttribute("datasource"));
        if (id == null || ds == null)
            throw new RuntimeException("Mandatory attributes not specified: " + element);
        final SQLDataProviderHolder result = new SQLDataProviderHolder(id);
        result.setDataSource(new BaseExpression(ds));
        for (Iterator<Element> i = XMLUtil.getChildElements(element); i.hasNext(); ) {
            final Element el = i.next();
            final String tagName = el.getTagName();
            if ("filter".equals(tagName)) {
                final String filter = StringUtil.trim(XMLUtil.getNodeText(el));
                result.setFilter(new BaseExpression(filter));
            } else
            if ("params".equals(tagName)) {
                final String paramsMap = StringUtil.trim(XMLUtil.getNodeText(el));
                result.setParamsMap(new BaseExpression(paramsMap));
            } else
            if ("param".equals(tagName)) {
                final String name = StringUtil.trim(el.getAttribute("name"));
                final String value = StringUtil.trim(el.getAttribute("value"));
                if (name != null)
                    result.addParam(new BaseExpression(name), new BaseExpression(value));
            } else
            if ("sql".equals(tagName)) {
                final String sql = StringUtil.trim(XMLUtil.getNodeText(el));
                result.setSQL(new BaseExpression(sql));
            } else
            if ("sql-ref".equals(tagName)) {
                final String sqlref = StringUtil.trim(XMLUtil.getNodeText(el));
                result.setSQLReference(new BaseExpression(sqlref));
            } else
                throw new RuntimeException("Unknown element: " + tagName);
        }
        report.getProviders().put(id, result);
    }

    private static void parseClassDataProvider(final Report report, final Element element) {
        final String id = StringUtil.trim(element.getAttribute("id"));
        final String object = StringUtil.trim(element.getAttribute("object"));
        final String method = StringUtil.trim(element.getAttribute("method"));
        if (id == null || object == null || method == null)
            throw new RuntimeException("Mandatory attributes not specified: " + element);
        final ClassDataProviderHolder result = new ClassDataProviderHolder(id);
        result.setObject(new BaseExpression(object));
        result.setMethodName(new BaseExpression(method));
        for (Iterator<Element> i = XMLUtil.getChildElements(element); i.hasNext(); ) {
            final Element el = i.next();
            final String tagName = el.getTagName();
            if ("filter".equals(tagName)) {
                final String filter = StringUtil.trim(XMLUtil.getNodeText(el));
                result.setFilter(new BaseExpression(filter));
            } else
            if ("params".equals(tagName)) {
                final String paramsMap = StringUtil.trim(XMLUtil.getNodeText(el));
                result.setParamsMap(new BaseExpression(paramsMap));
            } else
            if ("param".equals(tagName)) {
                final String name = StringUtil.trim(el.getAttribute("name"));
                final String value = StringUtil.trim(el.getAttribute("value"));
                if (name != null)
                    result.addParam(new BaseExpression(name), new BaseExpression(value));
            } else
                throw new RuntimeException("Unknown element: " + tagName);
        }
        report.getProviders().put(id, result);
    }


    private static SheetModel parseSheet(final Workbook wb, final Report report, final Element element) {
        final String id = StringUtil.trim(element.getAttribute("id"));
        final Sheet esheet = wb.getSheet(id);
        if (esheet == null)
            throw new RuntimeException("Template doesn't contains sheet " + id);
        final SheetModel sheet = new SheetModel(id);
        sheet.setTitle(new BaseExpression(StringUtil.trim(element.getAttribute("title"))));
        sheet.setHidden(Any.asBoolean(StringUtil.trim(element.getAttribute("hidden")), false));
        sheet.setRendered(Any.asBoolean(StringUtil.trim(element.getAttribute("rendered")), true));
        sheet.setProtected(Any.asBoolean(StringUtil.trim(element.getAttribute("protected")), false));
        final String cgs = StringUtil.trim(element.getAttribute("group-columns"));
        if (cgs != null) {
            for (Iterator<String> it = new FastStringTokenizer(cgs, ','); it.hasNext(); ) {
                final String token = StringUtil.trim(it.next());
                if (token == null)
                    continue;
                final String[] cnames = StringUtil.split(token, '-');
                if (cnames.length != 2 || cnames[0].length() == 0 || cnames[1].length() == 0)
                    throw new IllegalArgumentException("Incorrect value for attribute 'column-groups': " + cgs);
                final int c1 = POIUtils.getColumnNumber(cnames[0]);
                final int c2 = POIUtils.getColumnNumber(cnames[1]);
                sheet.addColumnGroup(new ColumnGroupModel(c1, c2));
            }
        }
        copyPageSettings(sheet.getPageSettings(), esheet);
        final String zoomstr = StringUtil.trim(element.getAttribute("zoom"));
        if (zoomstr != null)
            sheet.getPageSettings().setZoom(Any.asInt(zoomstr, 100));
        int offset = 0;
        for (Iterator<Element> i = XMLUtil.getChildElements(element); i.hasNext(); ) {
            final Element el = i.next();
            final String tagName = el.getTagName();
            if ("plain-section".equals(tagName)) {
                final Section section = parsePlainSection(esheet, offset, report, el);
                if (sheet.findSectionById(section.getId()) != null)
                    throw new RuntimeException("Section " + section.getId() + " already exists in sheet " + sheet.getId());
                sheet.getSections().add(section);
                offset += section.getTemplateRowsCount();
            } else
            if ("grouping-section".equals(tagName)) {
                final Section section = parseGroupingSection(esheet, offset, report, el);
                if (sheet.findSectionById(section.getId()) != null)
                    throw new RuntimeException("Section " + section.getId() + " already exists in sheet " + sheet.getId());
                sheet.getSections().add(section);
                offset += section.getTemplateRowsCount();
            } else
            if ("composite-section".equals(tagName)) {
                final Section section = parseCompositeSection(esheet, offset, report, el);
                if (sheet.findSectionById(section.getId()) != null)
                    throw new RuntimeException("Section " + section.getId() + " already exists in sheet " + sheet.getId());
                sheet.getSections().add(section);
                offset += section.getTemplateRowsCount();
            } else
                throw new RuntimeException("Unknown element: " + tagName);
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
        final String pid = StringUtil.trim(element.getAttribute("provider"));
        if (pid != null)
            section.setDataProvider(report.getProviders().get(pid));
        final int height = Any.asInt(element.getAttribute("height"), 1);
        section.setTemplate(new AreaModel(sheet, offset, height, report.getPalette()));
        section.getTemplate().setHidden(section.isHidden());
        for (Iterator<Element> i = XMLUtil.getChildElements(element); i.hasNext(); ) {
            final Element el = i.next();
            final String tagName = el.getTagName();
            if ("section-listener".equals(tagName)) {
                parseSectionEventListener(section, el);
            } else
            if ("cell-listener".equals(tagName)) {
                parseCellEventListener(section, el);
            } else
                throw new RuntimeException("Unknown element: " + tagName);
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
        final String pid = StringUtil.trim(element.getAttribute("provider"));
        if (pid != null)
            section.setDataProvider(report.getProviders().get(pid));
        final String[] colnames = Any.asStringArray(StringUtil.trim(element.getAttribute("indentColumns")), null);
        section.setIndentedColumns(colnames);
        final int rowHeight = Any.asInt(element.getAttribute("rowHeight"), 1);
        int height = 0;
        for (Iterator<Element> i = XMLUtil.getChildElements(element); i.hasNext(); ) {
            final Element el = i.next();
            final String tagName = el.getTagName();
            if ("section-listener".equals(tagName)) {
                parseSectionEventListener(section, el);
            } else
            if ("cell-listener".equals(tagName)) {
                parseCellEventListener(section, el);
            } else
            if ("group".equals(tagName)) {
                final GroupModel group = parseGroup(sheet, offset + height, report, el);
                section.getGroups().add(group);
                height += group.getStylesCount() * group.getRowsCount();
            } else
                throw new RuntimeException("Unknown element: " + tagName);
        }
        section.setRowTemplate(new AreaModel(sheet, offset + height, rowHeight, report.getPalette()));
        return section;
    }

    private static Section parseCompositeSection(final Sheet sheet, final int offset, final Report report, final Element element) {
        final String id = StringUtil.trim(element.getAttribute("id"));
        final CompositeSection section = new CompositeSection(id);
        section.setCollapsible(Any.asBoolean(StringUtil.trim(element.getAttribute("collapsible")), false));
        section.setCollapsed(Any.asBoolean(StringUtil.trim(element.getAttribute("collapsed")), false));
        section.setHidden(Any.asBoolean(StringUtil.trim(element.getAttribute("hidden")), false));
        section.setRendered(Any.asBoolean(StringUtil.trim(element.getAttribute("rendered")), true));
        final String pid = StringUtil.trim(element.getAttribute("provider"));
        if (pid != null)
            section.setDataProvider(report.getProviders().get(pid));
        final String pu = StringUtil.trim(element.getAttribute("provider-usage"));
        section.setProviderUsage(pu != null ? ProviderUsage.valueOf(pu.toUpperCase()) : null);
        final String[] colnames = Any.asStringArray(StringUtil.trim(element.getAttribute("indentColumns")), null);
        section.setIndentedColumns(colnames);
        int height = 0;
        for (Iterator<Element> i = XMLUtil.getChildElements(element); i.hasNext(); ) {
            final Element el = i.next();
            final String tagName = el.getTagName();
            if ("section-listener".equals(tagName)) {
                parseSectionEventListener(section, el);
            } else
            if ("cell-listener".equals(tagName)) {
                parseCellEventListener(section, el);
            } else
            if ("group".equals(tagName)) {
                final GroupModel group = parseGroup(sheet, offset + height, report, el);
                section.getGroups().add(group);
                height += group.getStylesCount() * group.getRowsCount();
            } else
            if ("plain-section".equals(tagName)) {
                final Section child = parsePlainSection(sheet, offset + height, report, el);
                section.getSections().add(child);
                height += child.getTemplateRowsCount();
            } else
            if ("grouping-section".equals(tagName)) {
                final Section child = parseGroupingSection(sheet, offset + height, report, el);
                section.getSections().add(child);
                height += child.getTemplateRowsCount();
            } else
            if ("composite-section".equals(tagName)) {
                final Section child = parseCompositeSection(sheet, offset + height, report, el);
                section.getSections().add(child);
                height += child.getTemplateRowsCount();
            } else
                throw new RuntimeException("Unknown element: " + tagName);
        }
        return section;
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
        for (Iterator<Element> i = XMLUtil.getChildElements(element); i.hasNext(); ) {
            final Element el = i.next();
            final String tagName = el.getTagName();
            if ("group-style".equals(tagName)) {
                final GroupStyle style = new GroupStyle();
                style.setLevel(Any.asInt(StringUtil.trim(el.getAttribute("level")), 0));
                style.setDefault(Any.asBoolean(StringUtil.trim(el.getAttribute("default")), false));
                style.setTemplate(new AreaModel(sheet, offset, height, report.getPalette()));
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
            style.setTemplate(new AreaModel(sheet, offset, height, report.getPalette()));
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
}
