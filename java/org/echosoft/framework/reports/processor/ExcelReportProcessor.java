package org.echosoft.framework.reports.processor;

import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.POIXMLProperties;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.MutableProperty;
import org.apache.poi.hpsf.MutablePropertySet;
import org.apache.poi.hpsf.MutableSection;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.Variant;
import org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.apache.poi.hpsf.wellknown.SectionIDMap;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.util.Nullable;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.echosoft.common.data.misc.TreeNode;
import org.echosoft.framework.reports.macros.Macros;
import org.echosoft.framework.reports.model.AreaModel;
import org.echosoft.framework.reports.model.CellModel;
import org.echosoft.framework.reports.model.ColumnGroupModel;
import org.echosoft.framework.reports.model.CompositeSection;
import org.echosoft.framework.reports.model.GroupStyle;
import org.echosoft.framework.reports.model.GroupingSection;
import org.echosoft.framework.reports.model.PageSettingsModel;
import org.echosoft.framework.reports.model.PlainSection;
import org.echosoft.framework.reports.model.PrintSetupModel;
import org.echosoft.framework.reports.model.Report;
import org.echosoft.framework.reports.model.RowModel;
import org.echosoft.framework.reports.model.Section;
import org.echosoft.framework.reports.model.SheetModel;
import org.echosoft.framework.reports.model.el.ELContext;
import org.echosoft.framework.reports.model.events.CellEvent;
import org.echosoft.framework.reports.model.events.CellEventListener;
import org.echosoft.framework.reports.model.events.ReportEventListener;
import org.echosoft.framework.reports.model.events.SectionEventListener;
import org.echosoft.framework.reports.model.providers.DataProvider;
import org.echosoft.framework.reports.model.providers.ProviderUsage;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheets;

/**
 * Формирует итоговый отчет по его модели и на основании данных указанных пользователем в качестве параметров.<br/>
 * Результатом работы данного построителя является экземпляр {@link Workbook} соответствующий формату не ниже Excel-2003.
 *
 * @author Anton Sharapov
 */
public class ExcelReportProcessor implements ReportProcessor {

    public static final String VAR_CONTEXT = "context";
    public static final String VAR_RECORD = "record";
    public static final String VAR_PREV_ROW = "prevrow";
    public static final String VAR_ROW = "row";
    public static final String VAR_NEXT_ROW = "nextrow";
    private static final String FORMULA = "$F=";
    private static final int FORMULA_LENGTH = FORMULA.length();
    private static final String MACROS = "$M=";
    private static final int MACROS_LENGTH = MACROS.length();


    public ExcelReportProcessor() {
    }

    /**
     * Формирует отчет на основании его модели и указанных пользователем в контексте параметров.
     *
     * @param report модель формируемого отчета.
     * @param ctx    данные необходимые для формирования данного отчета.
     * @return сформированный отчет.
     * @throws ReportProcessingException в случае каких-либо проблем.
     */
    @Override
    public Workbook process(Report report, final ELContext ctx) throws ReportProcessingException {
        ExecutionContext ectx = null;
        try {
            report = new Report(null, report); // копируем модель отчета, т.к. в процессе формирования отчета она может измениться.
            final Workbook wb = makeWorkbook(report, ctx);
            final Map<Short, CellStyle> styles = applyStyles(report, wb);
            ectx = new ExecutionContext(report, ctx, wb, styles);
            ctx.getVariables().put(VAR_CONTEXT, ectx);
            for (final ReportEventListener listener : ectx.listeners) {
                listener.beforeReport(ectx);
            }
            for (final SheetModel sheet : report.getSheets()) {
                processSheet(ectx, sheet);
            }
            boolean activeSheetSpecified = false;
            for (int i = 0, cnt = wb.getNumberOfSheets(); i < cnt; i++) {
                if (!wb.isSheetHidden(i) && !wb.isSheetVeryHidden(i)) {
                    wb.setActiveSheet(i);
                    wb.setSelectedTab(i);
                    activeSheetSpecified = true;
                    break;
                }
            }
            if (!activeSheetSpecified) {
                final Sheet sheet = wb.createSheet();
                final int index = wb.getSheetIndex(sheet);
                wb.setActiveSheet(index);
                wb.setSelectedTab(index);
            }
            for (final ReportEventListener listener : ectx.listeners) {
                listener.afterReport(ectx);
            }
            return wb;
        } catch (Exception e) {
            throw new ReportProcessingException(e.getMessage() + "\n" + ectx, e, ectx);
        }
    }

    protected Workbook makeWorkbook(final Report report, final ELContext ctx) throws Exception {
        switch (report.getTarget()) {
            case XSSF: {
                final XSSFWorkbook wb;
                if (report.getTemplate() != null) {
                    final OPCPackage pkg = OPCPackage.open(new ByteArrayInputStream(report.getTemplate()));
                    wb = new XSSFWorkbook(pkg);
                    final CTSheets sheets = wb.getCTWorkbook().getSheets();
                    while (sheets.sizeOfSheetArray() > 0)
                        sheets.removeSheet(0);
                } else {
                    wb = new XSSFWorkbook();
                }
                final POIXMLProperties props = wb.getProperties();
                props.getCoreProperties().setCreated(new Nullable<Date>(new Date()));
                final String application = report.getDescription().getApplication(ctx);
                if (application != null) {
                    props.getExtendedProperties().getUnderlyingProperties().setApplication(application);
                }
                final String author = report.getDescription().getAuthor(ctx);
                if (author != null) {
                    props.getCoreProperties().setCreator(author);
                }
                final String version = report.getDescription().getVersion(ctx);
                if (version != null) {
                    props.getExtendedProperties().getUnderlyingProperties().setAppVersion(version);
                }
                final String title = report.getDescription().getTitle(ctx);
                if (title != null) {
                    props.getCoreProperties().setTitle(title);
                }
                final String subject = report.getDescription().getSubject(ctx);
                if (subject != null) {
                    props.getCoreProperties().setSubjectProperty(subject);
                }
                final String comments = report.getDescription().getComments(ctx);
                if (comments != null) {
                    props.getCoreProperties().setDescription(comments);
                }
                final String company = report.getDescription().getCompany(ctx);
                if (company != null) {
                    props.getExtendedProperties().getUnderlyingProperties().setCompany(company);
                }
                final String category = report.getDescription().getCategory(ctx);
                if (category != null) {
                    props.getCoreProperties().setCategory(category);
                }
                // специфичные для XSSF настройки ...
                final String user = report.getUser() != null ? (String) report.getUser().getValue(ctx) : null;
                final String password = report.getPassword() != null ? (String) report.getPassword().getValue(ctx) : null;
                if (user != null && password != null) {
                    wb.lockWindows();
                    wb.lockRevision();
                    wb.lockStructure();
                }
                return wb;
            }
            case HSSF:
            default: {
                final POIFSFileSystem fs;
                if (report.getTemplate() != null) {
                    fs = new POIFSFileSystem(new ByteArrayInputStream(report.getTemplate()));
                } else {
                    final byte[] emptyWorkbookData = new HSSFWorkbook().getBytes();
                    fs = new POIFSFileSystem();
                    fs.createDocument(new ByteArrayInputStream(emptyWorkbookData), "Workbook");
                }

                final MutablePropertySet siProperties = new MutablePropertySet();
                final MutableSection siSection = (MutableSection) siProperties.getSections().get(0);
                siSection.setFormatID(SectionIDMap.SUMMARY_INFORMATION_ID);
                final MutableProperty p0 = new MutableProperty();
                p0.setID(PropertyIDMap.PID_CREATE_DTM);
                p0.setType(Variant.VT_FILETIME);
                p0.setValue(new Date());
                siSection.setProperty(p0);

                final String application = report.getDescription().getApplication(ctx);
                if (application != null) {
                    final MutableProperty p = new MutableProperty();
                    p.setID(PropertyIDMap.PID_APPNAME);
                    p.setType(Variant.VT_LPWSTR);
                    p.setValue(application);
                    siSection.setProperty(p);
                }
                final String author = report.getDescription().getAuthor(ctx);
                if (author != null) {
                    final MutableProperty p = new MutableProperty();
                    p.setID(PropertyIDMap.PID_AUTHOR);
                    p.setType(Variant.VT_LPWSTR);
                    p.setValue(author);
                    siSection.setProperty(p);
                }
                final String version = report.getDescription().getVersion(ctx);
                if (version != null) {
                    final MutableProperty p = new MutableProperty();
                    p.setID(PropertyIDMap.PID_REVNUMBER);
                    p.setType(Variant.VT_LPWSTR);
                    p.setValue(version);
                    siSection.setProperty(p);
                }
                final String title = report.getDescription().getTitle(ctx);
                if (title != null) {
                    final MutableProperty p = new MutableProperty();
                    p.setID(PropertyIDMap.PID_TITLE);
                    p.setType(Variant.VT_LPWSTR);
                    p.setValue(title);
                    siSection.setProperty(p);
                }
                final String subject = report.getDescription().getSubject(ctx);
                if (subject != null) {
                    final MutableProperty p = new MutableProperty();
                    p.setID(PropertyIDMap.PID_SUBJECT);
                    p.setType(Variant.VT_LPWSTR);
                    p.setValue(subject);
                    siSection.setProperty(p);
                }
                final String comments = report.getDescription().getComments(ctx);
                if (comments != null) {
                    final MutableProperty p = new MutableProperty();
                    p.setID(PropertyIDMap.PID_COMMENTS);
                    p.setType(Variant.VT_LPWSTR);
                    p.setValue(comments);
                    siSection.setProperty(p);
                }

                final MutablePropertySet dsiProperties = new MutablePropertySet();
                final MutableSection dsiSection = (MutableSection) dsiProperties.getSections().get(0);
                dsiSection.setFormatID(SectionIDMap.DOCUMENT_SUMMARY_INFORMATION_ID[0]);
                final String company = report.getDescription().getCompany(ctx);
                if (company != null) {
                    final MutableProperty p = new MutableProperty();
                    p.setID(PropertyIDMap.PID_COMPANY);
                    p.setType(Variant.VT_LPWSTR);
                    p.setValue(company);
                    dsiSection.setProperty(p);
                }
                final String category = report.getDescription().getCategory(ctx);
                if (category != null) {
                    final MutableProperty p = new MutableProperty();
                    p.setID(PropertyIDMap.PID_CATEGORY);
                    p.setType(Variant.VT_LPWSTR);
                    p.setValue(category);
                    dsiSection.setProperty(p);
                }

                fs.createDocument(siProperties.toInputStream(), SummaryInformation.DEFAULT_STREAM_NAME);
                fs.createDocument(dsiProperties.toInputStream(), DocumentSummaryInformation.DEFAULT_STREAM_NAME);
                final HSSFWorkbook wb = new HSSFWorkbook(fs, true);
                // специфичные для HSSF настройки ...
                final String user = report.getUser() != null ? (String) report.getUser().getValue(ctx) : null;
                final String password = report.getPassword() != null ? (String) report.getPassword().getValue(ctx) : null;
                if (user != null && password != null) {
                    wb.writeProtectWorkbook(password, user);
                }
                return wb;
            }
        }
    }

    protected Map<Short, CellStyle> applyStyles(final Report report, final Workbook wb) {
        if (report.getTemplate() != null) {
            final Map<Short, CellStyle> styles = new HashMap<Short, CellStyle>();
            for (final short styleIndex : report.getPalette().getStyles().keySet()) {
                final CellStyle style = wb.getCellStyleAt(styleIndex);
                if (style == null)
                    throw new RuntimeException("Inconsistent report template. Style not found: " + styleIndex);
                styles.put(styleIndex, style);
            }
            return styles;
        } else {
            return report.getPalette().applyTo(wb);
        }
    }

    protected void processSheet(final ExecutionContext ectx, final SheetModel sheet) throws Exception {
        ectx.sheet = sheet;
        for (final ReportEventListener listener : ectx.listeners) {
            listener.beforeSheet(ectx);
        }
        if (ectx.sheet.isRendered()) {
            ectx.wsheet = ectx.wb.createSheet((String) sheet.getTitle().getValue(ectx.elctx));
            ectx.wsheet.setRowSumsBelow(false);
            //ectx.wsheet.setAlternativeExpression(false);  //мы использовали этот метод т.к. setRowSumBelow() не работал в должной мере, но судя по коду, в POI это исправили еще 4 года назад

            final int sheetIdx = ectx.wb.getSheetIndex(ectx.wsheet);
            ectx.wb.setSheetHidden(sheetIdx, sheet.isHidden());
            if (sheet.isProtected() && ectx.report.getPassword() != null /*&& ectx.wb.isWriteProtected()*/) {
                ectx.wsheet.protectSheet((String) ectx.report.getPassword().getValue(ectx.elctx));
            }
            for (final Section section : sheet.getSections()) {
                processSection(ectx, section);
            }
            final int[] widths = sheet.getColumnWidths();
            for (int i = 0; i < widths.length; i++) {
                ectx.wsheet.setColumnWidth((short) i, widths[i]);
            }
            final boolean[] hidden = sheet.getColumnHidden();
            for (int i = 0; i < hidden.length; i++) {
                ectx.wsheet.setColumnHidden(i, hidden[i]);
            }
            for (TreeNode<String, ColumnGroupModel> grpNode : sheet.getColumnGroups().traverseNodes(false)) {
                final ColumnGroupModel group = grpNode.getData();
                ectx.wsheet.groupColumn(group.getFirstColumn(), (short) group.getLastColumn());
            }
            processPageSettings(ectx.wsheet, sheet.getPageSettings());
        }
        for (final ReportEventListener listener : ectx.listeners) {
            listener.afterSheet(ectx);
        }
        ectx.sheet = null;
        ectx.wsheet = null;
    }

    private void processPageSettings(final Sheet sheet, final PageSettingsModel pageSettings) {
        sheet.getHeader().setLeft(pageSettings.getHeader().getLeft());
        sheet.getHeader().setCenter(pageSettings.getHeader().getCenter());
        sheet.getHeader().setRight(pageSettings.getHeader().getRight());
        sheet.getFooter().setLeft(pageSettings.getFooter().getLeft());
        sheet.getFooter().setCenter(pageSettings.getFooter().getCenter());
        sheet.getFooter().setRight(pageSettings.getFooter().getRight());
        sheet.setMargin(Sheet.TopMargin, pageSettings.getMargins().getTop());
        sheet.setMargin(Sheet.RightMargin, pageSettings.getMargins().getRight());
        sheet.setMargin(Sheet.BottomMargin, pageSettings.getMargins().getBottom());
        sheet.setMargin(Sheet.LeftMargin, pageSettings.getMargins().getLeft());
        processPrintSetup(sheet.getPrintSetup(), pageSettings.getPrintSetup());
        sheet.setFitToPage(pageSettings.isFitToPage());
        sheet.setHorizontallyCenter(pageSettings.isHorizontallyCenter());
        sheet.setVerticallyCenter(pageSettings.isVerticallyCenter());
        if (pageSettings.getZoom() != null)
            sheet.setZoom(pageSettings.getZoom(), 100);
    }
    private void processPrintSetup(final PrintSetup hps, final PrintSetupModel printSetup) {
        hps.setPaperSize(printSetup.getPaperSize());
        hps.setScale(printSetup.getScale());
        hps.setFitWidth(printSetup.getFitWidth());
        hps.setPageStart(printSetup.getPageStart());
        hps.setFitHeight(printSetup.getFitHeight());
        hps.setHeaderMargin(printSetup.getHeaderMargin());
        hps.setFooterMargin(printSetup.getFooterMargin());
        hps.setLandscape(printSetup.getLandscape());
        hps.setLeftToRight(printSetup.getLeftToRight());
        hps.setNoColor(printSetup.getNoColor());
        hps.setDraft(printSetup.getDraft());
        hps.setHResolution(printSetup.getHResolution());
        hps.setNotes(printSetup.getNotes());
        hps.setUsePage(printSetup.getUsePage());
        hps.setVResolution(printSetup.getVResolution());
        hps.setValidSettings(printSetup.getValidSettings());
        hps.setNoOrientation(printSetup.getNoOrientation());
        hps.setCopies(printSetup.getCopies());
    }

    protected void processSection(final ExecutionContext ectx, final Section section) throws Exception {
        if (!section.isRendered())
            return;

        final Object prevBean = ectx.elctx.getRowModel();
        final Object prevRecord = ectx.elctx.getVariables().get(VAR_RECORD);
        final int firstRow = ectx.getNewRowNum();
        ectx.sectionContext = new SectionContext(ectx.sectionContext, section, firstRow, ectx.elctx);

        for (final SectionEventListener listener : ectx.sectionContext.sectionListeners) {
            listener.beforeSection(ectx);
        }
        if (section.isRendered()) {
            if (section instanceof CompositeSection) {
                processCompositeSection(ectx);
            } else
            if (section instanceof PlainSection) {
                processPlainSection(ectx);
            } else
            if (section instanceof GroupingSection) {
                processGroupingSection(ectx);
            } else
                throw new RuntimeException("Unsupported section type: " + section.getClass());

            final int lastRow = ectx.getLastRowNum();
            if (section.isHidden()) {
                for (int i = firstRow; i <= lastRow; i++) {
                    ectx.wsheet.getRow(i).setZeroHeight(true);
                }
            }
            if (section.isCollapsible() && lastRow > firstRow) {
                ectx.wsheet.groupRow(firstRow, lastRow);
                ectx.wsheet.setRowGroupCollapsed(firstRow, section.isCollapsed());
            }
        }
        for (final SectionEventListener listener : ectx.sectionContext.sectionListeners) {
            listener.afterSection(ectx);
        }

        ectx.history.put(ectx.sectionContext.section.getId(), ectx.sectionContext);
        ectx.sectionContext = ectx.sectionContext.parent;
        ectx.elctx.setRowModel(prevBean);
        ectx.elctx.getVariables().put(VAR_RECORD, prevRecord);
    }

    protected void processPlainSection(final ExecutionContext ectx) throws Exception {
        final SectionContext sctx = ectx.sectionContext;
        final PlainSection section = (PlainSection) sctx.section;

        if (section.getDataProvider() != null) {
            sctx.issuer = section.getDataProvider().getIssuer(ectx.elctx);
            try {
                while (sctx.issuer.hasNext()) {
                    sctx.bean = sctx.issuer.next();
                    ectx.elctx.setRowModel(sctx.bean);
                    ectx.elctx.getVariables().put(VAR_RECORD, sctx.record);
                    renderArea(ectx, section.getTemplate(), -1);
                    for (SectionEventListener listener : sctx.sectionListeners) {
                        listener.afterRecord(ectx);
                    }
                    sctx.recordFirstRow = ectx.getNewRowNum();
                    sctx.record++;
                }
            } finally {
                sctx.issuer.close();
                sctx.issuer = null;
                sctx.bean = null;
            }
        } else {
            renderArea(ectx, section.getTemplate(), -1);
        }
    }

    protected void processGroupingSection(final ExecutionContext ectx) throws Exception {
        final SectionContext sctx = ectx.sectionContext;
        final GroupingSection section = (GroupingSection) sctx.section;

        if (section.getDataProvider() != null) {
            sctx.gm = new GroupManager(section.getGroups()) {
                public void renderCurrentGroup(final ExecutionContext ctx) throws Exception {
                    renderGroup(ctx, getCurrentGroup());
                }
            };
            sctx.issuer = section.getDataProvider().getIssuer(ectx.elctx);
            try {
                while (sctx.issuer.hasNext()) {
                    sctx.bean = sctx.issuer.next();
                    ectx.elctx.setRowModel(sctx.bean);
                    ectx.elctx.getVariables().put(VAR_RECORD, sctx.record);
                    sctx.gm.initRecord(ectx, sctx.bean);
                    renderArea(ectx, section.getRowTemplate(), -1);
                    sctx.gm.finalizeRecord(ectx);
                    for (final SectionEventListener listener : sctx.sectionListeners) {
                        listener.afterRecord(ectx);
                    }
                    sctx.recordFirstRow = ectx.getNewRowNum();
                    sctx.record++;
                }
            } finally {
                sctx.issuer.close();
                sctx.issuer = null;
                sctx.bean = null;
            }
            sctx.gm.finalizeAllGroups(ectx);
            sctx.gm = null;
        } else {
            renderArea(ectx, section.getRowTemplate(), -1);
        }
    }

    protected void processCompositeSection(final ExecutionContext ectx) throws Exception {
        final SectionContext sctx = ectx.sectionContext;
        final CompositeSection section = (CompositeSection) sctx.section;
        final DataProvider provider = section.getDataProvider();
        final ProviderUsage providerUsage = section.getProviderUsage();

        if (provider != null && providerUsage != ProviderUsage.DECLARE_ONLY) {
            sctx.gm = new GroupManager(section.getGroups()) {
                public void renderCurrentGroup(final ExecutionContext ctx) throws Exception {
                    renderGroup(ctx, getCurrentGroup());
                }
            };
            sctx.issuer = provider.getIssuer(ectx.elctx);
            try {
                while (sctx.issuer.hasNext()) {
                    sctx.bean = ProviderUsage.PREFETCH_RECORDS == providerUsage ? sctx.issuer.readAhead() : sctx.issuer.next();
                    ectx.elctx.setRowModel(sctx.bean);
                    ectx.elctx.getVariables().put(VAR_RECORD, sctx.record);
                    sctx.gm.initRecord(ectx, sctx.bean);
                    for (final Section childSection : section.getSections()) {
                        processSection(ectx, childSection);
                    }
                    sctx.gm.finalizeRecord(ectx);
                    for (final SectionEventListener listener : sctx.sectionListeners) {
                        listener.afterRecord(ectx);
                    }
                    sctx.recordFirstRow = ectx.getNewRowNum();
                    sctx.record++;
                }
            } finally {
                sctx.issuer.close();
                sctx.issuer = null;
                sctx.bean = null;
            }
            sctx.gm.finalizeAllGroups(ectx);
            sctx.gm = null;
        } else {
            sctx.issuer = provider != null ? provider.getIssuer(ectx.elctx) : null;
            try {
                for (final Section childSection : section.getSections()) {
                    processSection(ectx, childSection);
                }
            } finally {
                if (sctx.issuer != null) {
                    sctx.issuer.close();
                    sctx.issuer = null;
                }
            }
        }
    }

    /**
     * Отрисовывает группировочную строку в отчете.
     *
     * @param group текущая группа, которая должна быть отображена в отчете.
     * @param ectx  контекст выполнения задачи.
     * @throws Exception в случае каких-либо проблем.
     */
    protected void renderGroup(final ExecutionContext ectx, final Group group) throws Exception {
        final Object prevBean = ectx.elctx.getRowModel();
        ectx.elctx.setRowModel(group.bean);

        final GroupStyle style = group.level != null ? group.model.getStyleByLevel(group.level) : group.model.getDefaultStyle();
        renderArea(ectx, style.getTemplate(), group.startRow);

        final int firstRow = group.startRow + 1;
        final int lastRow = ectx.wsheet.getLastRowNum();
        if (group.model.isCollapsible() && lastRow >= firstRow) {
            ectx.wsheet.groupRow(firstRow, lastRow);
            if (group.model.isCollapsed()) {
                ectx.wsheet.setRowGroupCollapsed(firstRow, group.model.isCollapsed());
            }
        }

        ectx.elctx.setRowModel(prevBean);
    }

    /**
     * Отрисовывает группу строк на основе их шаблона в отчете.
     *
     * @param ectx     контекст выполнения задачи.
     * @param template шаблон группы строк.
     * @param startRow номер первой строки отчета (начиная с 0) в которую надо вставлять данный контент.
     *                 Если данный параметр меньше нуля, то будет осуществляться вставка новых строк в конец листа.
     * @return номер следующей строки после отображения данной группы строк.
     * @throws Exception в случае каких-либо проблем
     */
    protected int renderArea(final ExecutionContext ectx, final AreaModel template, int startRow) throws Exception {
        final CellEvent event = new CellEvent(ectx);

        if (startRow < 0) {
            // работаем в режиме добавления новых записей в конец листа...
            startRow = ectx.wsheet.getPhysicalNumberOfRows() > 0 ? ectx.wsheet.getLastRowNum() + 1 : 0;
        }
        int r = startRow;
        final Map<String, Object> variables = ectx.elctx.getVariables();
        final boolean hidden = template.isHidden();
        for (final RowModel rm : template.getRows()) {
            Row row = ectx.wsheet.getRow(r);
            if (row == null) {
                row = ectx.wsheet.createRow(r);
            }
            row.setHeight(rm.getHeight());
            if (hidden || rm.isHidden()) {
                row.setZeroHeight(true);
            }
            r++;
            variables.put(VAR_PREV_ROW, r - 1);
            variables.put(VAR_ROW, r);
            variables.put(VAR_NEXT_ROW, r + 1);
            final List<CellModel> cells = rm.getCells();
            for (int i = 0; i < cells.size(); i++) {
                final CellModel cm = cells.get(i);
                if (cm == null)
                    continue;
                final CellStyle style = ectx.styles.get(cm.getStyle());
                ectx.cell = row.createCell(i, Cell.CELL_TYPE_BLANK);
                if (style != null)
                    ectx.cell.setCellStyle(style);
                event.setRendered(false);
                event.setCellValue(cm.getExpression().getValue(ectx.elctx));
                for (final CellEventListener listener : ectx.sectionContext.cellListeners) {
                    listener.handle(event);
                }
                if (!event.isRendered())
                    renderCell(ectx, event.getCellValue());
            }
        }
        for (final CellRangeAddress range : template.makePOIRegions(startRow)) {
            ectx.wsheet.addMergedRegion(range);
        }
        return r;
    }

    /**
     * Устанавливает значение ячейки.
     *
     * @param ectx  контекст выполнения задачи.
     * @param value объект на основании которого устанавливается значение ячейки.
     */
    protected void renderCell(final ExecutionContext ectx, final Object value) {
        if (value == null) {
            ectx.cell.setCellType(Cell.CELL_TYPE_BLANK);
        } else
        if (value instanceof Date) {
            ectx.cell.setCellType(Cell.CELL_TYPE_NUMERIC);
            ectx.cell.setCellValue((Date) value);
        } else
        if (value instanceof Calendar) {
            ectx.cell.setCellType(Cell.CELL_TYPE_NUMERIC);
            ectx.cell.setCellValue((Calendar) value);
        } else
        if (value instanceof Double) {
            ectx.cell.setCellType(Cell.CELL_TYPE_NUMERIC);
            ectx.cell.setCellValue((Double) value);
        } else
        if (value instanceof Number) {
            ectx.cell.setCellType(Cell.CELL_TYPE_NUMERIC);
            ectx.cell.setCellValue(((Number) value).doubleValue());
        } else
        if (value instanceof Boolean) {
            ectx.cell.setCellType(Cell.CELL_TYPE_BOOLEAN);
            ectx.cell.setCellValue((Boolean) value);
        } else
        if (value instanceof RichTextString) {
            ectx.cell.setCellType(Cell.CELL_TYPE_STRING);
            ectx.cell.setCellValue((RichTextString) value);
        } else {
            final String text = value.toString();
            if (ectx.cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                ectx.cell.setCellFormula(text);
            } else
            if (text.startsWith(FORMULA)) {
                ectx.cell.setCellType(Cell.CELL_TYPE_FORMULA);
                ectx.cell.setCellFormula(text.substring(FORMULA_LENGTH));
            } else
            if (text.startsWith(MACROS)) {
                final int si = text.indexOf('(', MACROS_LENGTH);
                final String name, args;
                if (si > 0) {
                    name = text.substring(MACROS_LENGTH, si);
                    final int fi = text.lastIndexOf(')');
                    if (fi < si)
                        throw new IllegalArgumentException("Illegal custom function call [" + text + "] at row:" + ectx.cell.getRowIndex() + ", cell:" + ectx.cell.getColumnIndex());
                    args = text.substring(si + 1, fi);
                } else {
                    name = text.substring(MACROS_LENGTH);
                    args = null;
                }
                final Macros func = ectx.report.getMacros(name);
                if (func == null)
                    throw new IllegalArgumentException("Unable to find custom function [" + name + "] at row:" + ectx.cell.getRowIndex() + ", cell:" + ectx.cell.getColumnIndex());
                func.call(ectx, args);
            } else {
                ectx.cell.setCellType(Cell.CELL_TYPE_STRING);
                ectx.cell.setCellValue(ectx.creationHelper.createRichTextString(text));
            }
        }
    }
}
