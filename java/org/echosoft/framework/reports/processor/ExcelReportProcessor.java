package org.echosoft.framework.reports.processor;

import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.MutableProperty;
import org.apache.poi.hpsf.MutablePropertySet;
import org.apache.poi.hpsf.MutableSection;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.Variant;
import org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.apache.poi.hpsf.wellknown.SectionIDMap;
import org.apache.poi.hssf.record.PaletteRecord;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.util.CellRangeAddress;
import org.echosoft.common.model.TreeNode;
import org.echosoft.common.query.Query;
import org.echosoft.common.query.providers.DataProvider;
import org.echosoft.framework.reports.macros.Macros;
import org.echosoft.framework.reports.model.Area;
import org.echosoft.framework.reports.model.Cell;
import org.echosoft.framework.reports.model.CellStyle;
import org.echosoft.framework.reports.model.Color;
import org.echosoft.framework.reports.model.ColumnGroup;
import org.echosoft.framework.reports.model.CompositeSection;
import org.echosoft.framework.reports.model.Font;
import org.echosoft.framework.reports.model.GroupStyle;
import org.echosoft.framework.reports.model.GroupingSection;
import org.echosoft.framework.reports.model.PlainSection;
import org.echosoft.framework.reports.model.PrintSetup;
import org.echosoft.framework.reports.model.Report;
import org.echosoft.framework.reports.model.Row;
import org.echosoft.framework.reports.model.Section;
import org.echosoft.framework.reports.model.Sheet;
import org.echosoft.framework.reports.model.StylePalette;
import org.echosoft.framework.reports.model.el.ELContext;
import org.echosoft.framework.reports.model.events.CellEvent;
import org.echosoft.framework.reports.model.events.CellEventListener;
import org.echosoft.framework.reports.model.events.ReportEventListener;
import org.echosoft.framework.reports.model.events.SectionEventListener;
import org.echosoft.framework.reports.model.providers.ProviderUsage;
import org.echosoft.framework.reports.util.POIUtils;

/**
 * Формирует итоговый отчет по его модели и на основании данных указанных пользователем в качестве параметров.
 *
 * @author Anton Sharapov
 */
public class ExcelReportProcessor {

    public static final String VAR_CONTEXT = "context";
    public static final String VAR_RECORD = "record";
    public static final String VAR_PREV_ROW = "prevrow";
    public static final String VAR_ROW = "row";
    public static final String VAR_NEXT_ROW = "nextrow";
    private static final String FORMULA = "$F=";
    private static final int FORMULA_LENGTH = FORMULA.length();
    private static final String MACROS = "$M=";
    private static final int MACROS_LENGTH = MACROS.length();

    /**
     * Формирует отчет на основании его модели и указанных пользователем в контексте параметров.
     *
     * @param report модель формируемого отчета.
     * @param ctx    данные необходимые для формирования данного отчета.
     * @return сформированный отчет.
     * @throws ReportProcessingException в случае каких-либо проблем.
     */
    public HSSFWorkbook process(Report report, final ELContext ctx) throws ReportProcessingException {
        ExecutionContext ectx = null;
        try {
            report = new Report(null, report); // копируем модель отчета, т.к. в процессе формирования отчета она может измениться.
            final HSSFWorkbook wb = makeWorkbook(report, ctx);
            final Map<Short, HSSFCellStyle> styles = applyStyles(report, wb);
            ectx = new ExecutionContext(report, ctx, wb, styles);
            ctx.getVariables().put(VAR_CONTEXT, ectx);
            final String user = report.getUser()!=null ? (String)report.getUser().getValue(ctx) : null;
            final String password = report.getPassword()!= null ?(String)report.getPassword().getValue(ctx) : null;
            if (user!=null && password!=null) {
                wb.writeProtectWorkbook(password, user);
            }
            for (final ReportEventListener listener : ectx.listeners) {
                listener.beforeReport(ectx);
            }
            for (final Sheet sheet : report.getSheets()) {
                processSheet(ectx, sheet);
            }
            boolean activeSheetSpecified = false;
            for (int i=0, cnt=wb.getNumberOfSheets(); i<cnt; i++) {
                if (!wb.isSheetHidden(i) && !wb.isSheetVeryHidden(i)) {
                    wb.setActiveSheet(i);
                    wb.setSelectedTab(i);
                    activeSheetSpecified = true;
                    break;
                }
            }
            if (!activeSheetSpecified) {
                final HSSFSheet sheet = wb.createSheet();
                final int index = wb.getSheetIndex(sheet);
                wb.setActiveSheet(index);
                wb.setSelectedTab(index);
            }
            for (final ReportEventListener listener : ectx.listeners) {
                listener.afterReport(ectx);
            }
            return wb;
        } catch (Exception e) {
            throw new ReportProcessingException(e.getMessage()+"\n"+ectx, e, ectx);
        }
    }

    protected HSSFWorkbook makeWorkbook(final Report report, final ELContext ctx) throws Exception {
        final byte[] emptyWorkbookData = new HSSFWorkbook().getBytes();
        final POIFSFileSystem fs;
        if (report.getTemplate()!=null) {
            fs = new POIFSFileSystem( new ByteArrayInputStream(report.getTemplate()) );
        } else {
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
        final MutableSection dsiSection = (MutableSection)dsiProperties.getSections().get(0);
        dsiSection.setFormatID(SectionIDMap.DOCUMENT_SUMMARY_INFORMATION_ID[0]);
        final String company = report.getDescription().getCompany(ctx);
        if (company!=null) {
            final MutableProperty p = new MutableProperty();
            p.setID(PropertyIDMap.PID_COMPANY);
            p.setType(Variant.VT_LPWSTR);
            p.setValue(company);
            dsiSection.setProperty(p);
        }
        final String category = report.getDescription().getCategory(ctx);
        if (category!=null) {
            final MutableProperty p = new MutableProperty();
            p.setID(PropertyIDMap.PID_CATEGORY);
            p.setType(Variant.VT_LPWSTR);
            p.setValue(category);
            dsiSection.setProperty(p);
        }

        fs.createDocument(siProperties.toInputStream(), SummaryInformation.DEFAULT_STREAM_NAME);
        fs.createDocument(dsiProperties.toInputStream(), DocumentSummaryInformation.DEFAULT_STREAM_NAME);
        return new HSSFWorkbook(fs,true);
    }

    protected Map<Short, HSSFCellStyle> applyStyles(final Report report, final HSSFWorkbook wb) {
        final StylePalette palette = report.getPalette();
        final Map<Short, HSSFCellStyle> styles = new HashMap<Short, HSSFCellStyle>();

        if (report.getTemplate()!=null) {
            for (final short styleIndex : palette.getStyles().keySet()) {
                final HSSFCellStyle style = wb.getCellStyleAt(styleIndex);
                if (style==null)
                    throw new RuntimeException("Inconsistent report template. Style not found: "+styleIndex);
                styles.put(styleIndex, style);
            }
            return styles;
        }
        if (palette.getColors().size() > PaletteRecord.STANDARD_PALETTE_SIZE)
            throw new RuntimeException("too many colors on report");
        final HSSFPalette pal = wb.getCustomPalette();
        for (final Color color : palette.getColors().values()) {
            pal.setColorAtIndex(color.getId(), color.getRed(), color.getGreen(), color.getBlue());
        }

        final Map<Short, HSSFFont> fonts = new HashMap<Short, HSSFFont>();
        final HSSFDataFormat formatter = wb.createDataFormat();
        for (final Font font : palette.getFonts().values()) {
            final HSSFFont f = POIUtils.ensureFontExists(wb, font);
            fonts.put(font.getId(), f);
        }

        for (final CellStyle style : palette.getStyles().values()) {
            final short bbc = style.getBottomBorderColor() != null ? style.getBottomBorderColor().getId() : 0;
            final short fbc = style.getFillBackgroundColor() != null ? style.getFillBackgroundColor().getId() : 0;
            final short ffc = style.getFillForegroundColor() != null ? style.getFillForegroundColor().getId() : 0;
            final short lbc = style.getLeftBorderColor() != null ? style.getLeftBorderColor().getId() : 0;
            final short rbc = style.getRightBorderColor() != null ? style.getRightBorderColor().getId() : 0;
            final short tbc = style.getTopBorderColor() != null ? style.getTopBorderColor().getId() : 0;

            final HSSFCellStyle s = wb.createCellStyle();
            s.setAlignment(style.getAlignment());
            s.setBorderBottom(style.getBorderBottom());
            s.setBorderLeft(style.getBorderLeft());
            s.setBorderRight(style.getBorderRight());
            s.setBorderTop(style.getBorderTop());
            s.setBottomBorderColor(bbc);
            s.setDataFormat(formatter.getFormat(style.getDataFormat()));
            s.setFillBackgroundColor(fbc);
            s.setFillForegroundColor(ffc);
            s.setFillPattern(style.getFillPattern());
            s.setHidden(style.isHidden());
            s.setIndention(style.getIndention());
            s.setLeftBorderColor(lbc);
            s.setLocked(style.isLocked());
            s.setRightBorderColor(rbc);
            s.setRotation(style.getRotation());
            s.setTopBorderColor(tbc);
            s.setVerticalAlignment(style.getVerticalAlignment());
            s.setWrapText(style.isWrapText());
            s.setFont(fonts.get(style.getFont().getId()));
            styles.put(style.getId(), s);
        }
        return styles;
    }

    protected void processSheet(final ExecutionContext ectx, final Sheet sheet) throws Exception {
        ectx.sheet = sheet;
        for (final ReportEventListener listener : ectx.listeners) {
            listener.beforeSheet(ectx);
        }
        if (ectx.sheet.isRendered()) {
            ectx.wsheet = ectx.wb.createSheet( (String)sheet.getTitle().getValue(ectx.elctx) );
            //ctx.wsheet.setRowSumsBelow(false);
            ectx.wsheet.setAlternativeExpression(false);  // setAlternativeExpression делает то что должен делать метод setRowSumBelow() ...
            final int sheetIdx = ectx.wb.getSheetIndex(ectx.wsheet);
            ectx.wb.setSheetHidden(sheetIdx, sheet.isHidden());
            ectx.wsheet.setZoom(sheet.getZoom(), 100);
            if (sheet.isProtected() && ectx.report.getPassword()!=null && ectx.wb.isWriteProtected()) {
                ectx.wsheet.protectSheet( (String)ectx.report.getPassword().getValue(ectx.elctx) );
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
            for (Iterator<TreeNode<ColumnGroup>> i = sheet.getColumnGroups().traverseChildNodes(); i.hasNext();) {
                final ColumnGroup group = i.next().getData();
                ectx.wsheet.groupColumn(group.getFirstColumn(), (short) group.getLastColumn());
            }
            ectx.wsheet.getHeader().setLeft(sheet.getHeader().getLeft());
            ectx.wsheet.getHeader().setCenter(sheet.getHeader().getCenter());
            ectx.wsheet.getHeader().setRight(sheet.getHeader().getRight());
            ectx.wsheet.getFooter().setLeft(sheet.getFooter().getLeft());
            ectx.wsheet.getFooter().setCenter(sheet.getFooter().getCenter());
            ectx.wsheet.getFooter().setRight(sheet.getFooter().getRight());
            processPrintSetup(ectx.wsheet, sheet.getPrintSetup());
        }
        for (final ReportEventListener listener : ectx.listeners) {
            listener.afterSheet(ectx);
        }
        ectx.sheet = null;
        ectx.wsheet = null;
    }

    private void processPrintSetup(final HSSFSheet sheet, final PrintSetup printSetup) {
        sheet.getPrintSetup().setPaperSize(printSetup.getPaperSize());
        sheet.getPrintSetup().setScale(printSetup.getScale());
        sheet.getPrintSetup().setFitWidth(printSetup.getFitWidth());
        sheet.getPrintSetup().setPageStart(printSetup.getPageStart());
        sheet.getPrintSetup().setFitHeight(printSetup.getFitHeight());
        sheet.getPrintSetup().setFooterMargin(printSetup.getFooterMargin());
        sheet.getPrintSetup().setLandscape(printSetup.getLandscape());
        sheet.getPrintSetup().setLeftToRight(printSetup.getLeftToRight());
        sheet.getPrintSetup().setNoColor(printSetup.getNoColor());
        sheet.getPrintSetup().setOptions(printSetup.getOptions());
        sheet.getPrintSetup().setDraft(printSetup.getDraft());
        sheet.getPrintSetup().setHResolution(printSetup.getHResolution());
        sheet.getPrintSetup().setNotes(printSetup.getNotes());
        sheet.getPrintSetup().setUsePage(printSetup.getUsePage());
        sheet.getPrintSetup().setVResolution(printSetup.getVResolution());
        sheet.getPrintSetup().setValidSettings(printSetup.getValidSettings());
        sheet.getPrintSetup().setNoOrientation(printSetup.getNoOrientation());
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
                throw new RuntimeException("Unsupported section type: "+section.getClass());

            final int lastRow = ectx.getLastRowNum();
            if (section.isHidden()) {
                for (int i = firstRow; i <= lastRow; i++) {
                    ectx.wsheet.getRow(i).setZeroHeight(true);
                }
            }
            if (section.isCollapsible() && lastRow>firstRow) {
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
        final PlainSection section = (PlainSection)sctx.section;
        DataProvider provider = null;
        Query query = null;
        if (section.getDataProvider() != null) {
            provider = section.getDataProvider().getProvider(ectx.elctx);
            query = section.getDataProvider().getQuery(ectx.elctx);
        }

        if (provider != null) {
            sctx.beanIterator = provider.execute(query);
            try {
                while (sctx.beanIterator.hasNext()) {
                    sctx.bean = sctx.beanIterator.next();
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
                sctx.beanIterator.close();
                sctx.beanIterator = null;
                sctx.bean = null;
            }
        } else {
            renderArea(ectx, section.getTemplate(), -1);
        }
    }

    protected void processGroupingSection(final ExecutionContext ectx) throws Exception {
        final SectionContext sctx = ectx.sectionContext;
        final GroupingSection section = (GroupingSection)sctx.section;
        DataProvider provider = null;
        Query query = null;
        if (section.getDataProvider() != null) {
            provider = section.getDataProvider().getProvider(ectx.elctx);
            query = section.getDataProvider().getQuery(ectx.elctx);
        }

        if (provider != null) {
            sctx.gm = new GroupManager(section.getGroups()) {
                public void renderCurrentGroup(final ExecutionContext ctx) throws Exception {
                    renderGroup(ctx, getCurrentGroup());
                }
            };
            sctx.beanIterator = provider.execute(query);
            try {
                while (sctx.beanIterator.hasNext()) {
                    sctx.bean = sctx.beanIterator.next();
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
                sctx.beanIterator.close();
                sctx.beanIterator = null;
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
        final CompositeSection section = (CompositeSection)sctx.section;
        DataProvider provider = null;
        Query query = null;
        if (section.getDataProvider() != null) {
            provider = section.getDataProvider().getProvider(ectx.elctx);
            query = section.getDataProvider().getQuery(ectx.elctx);
        }

        final ProviderUsage providerUsage = section.getProviderUsage();
        if (provider!=null && providerUsage!=ProviderUsage.DECLARE_ONLY) {
            sctx.gm = new GroupManager(section.getGroups()) {
                public void renderCurrentGroup(final ExecutionContext ctx) throws Exception {
                    renderGroup(ctx, getCurrentGroup());
                }
            };
            sctx.beanIterator = provider.execute(query);
            try {
                while(sctx.beanIterator.hasNext()) {
                    sctx.bean = ProviderUsage.PREFETCH_RECORDS==providerUsage ? sctx.beanIterator.readAhead() : sctx.beanIterator.next();
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
                sctx.beanIterator.close();
                sctx.beanIterator = null;
                sctx.bean = null;
            }
            sctx.gm.finalizeAllGroups(ectx);
            sctx.gm = null;
        } else {
            sctx.beanIterator = provider!=null ? provider.execute(query) : null;
            try {
                for (final Section childSection : section.getSections()) {
                    processSection(ectx, childSection);
                }
            } finally {
                if (sctx.beanIterator!=null) {
                    sctx.beanIterator.close();
                    sctx.beanIterator = null;
                }
            }
        }
    }

    /**
     * Отрисовывает группировочную строку в отчете.
     *
     * @param group текущая группа, которая должна быть отображена в отчете.
     * @param ectx   контекст выполнения задачи.
     * @throws Exception в случае каких-либо проблем.
     */
    protected void renderGroup(final ExecutionContext ectx, final Group group) throws Exception {
        final Object prevBean = ectx.elctx.getRowModel();
        ectx.elctx.setRowModel(group.bean);

        final GroupStyle style = group.level!=null ? group.model.getStyleByLevel(group.level) : group.model.getDefaultStyle();
        renderArea(ectx, style.getTemplate(), group.startRow);

        final int firstRow = group.startRow + 1;
        final int lastRow = ectx.wsheet.getLastRowNum();
        if (group.model.isCollapsible() && lastRow>=firstRow) {
            ectx.wsheet.groupRow(firstRow, lastRow );
            if (group.model.isCollapsed()) {
                ectx.wsheet.setRowGroupCollapsed(firstRow, group.model.isCollapsed());
            }
        }

        ectx.elctx.setRowModel(prevBean);
    }

    /**
     * Отрисовывает группу строк на основе их шаблона в отчете.
     *
     * @param ectx      контекст выполнения задачи.
     * @param template шаблон группы строк.
     * @param startRow номер первой строки отчета (начиная с 0) в которую надо вставлять данный контент.
     *                 Если данный параметр меньше нуля, то будет осуществляться вставка новых строк в конец листа.
     * @return номер следующей строки после отображения данной группы строк.
     * @throws Exception в случае каких-либо проблем
     */
    protected int renderArea(final ExecutionContext ectx, final Area template, int startRow) throws Exception {
        final CellEvent event = new CellEvent(ectx);

        if (startRow < 0) {
            // работаем в режиме добавления новых записей в конец листа...
            startRow = ectx.wsheet.getPhysicalNumberOfRows() > 0 ? ectx.wsheet.getLastRowNum() + 1 : 0;
        }
        int r = startRow;
        final Map<String, Object> variables = ectx.elctx.getVariables();
        final boolean hidden = template.isHidden();
        for (final Row rm : template.getRows()) {
            HSSFRow row = ectx.wsheet.getRow(r);
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
            final List<Cell> cells = rm.getCells();
            for (int i = 0; i < cells.size(); i++) {
                final Cell cm = cells.get(i);
                if (cm == null)
                    continue;
                final HSSFCellStyle style = ectx.styles.get(cm.getStyle());
                ectx.cell = row.createCell(i, HSSFCell.CELL_TYPE_BLANK);
                if (style!=null)
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
     * @param ectx   контекст выполнения задачи.
     * @param value объект на основании которого устанавливается значение ячейки.
     */
    protected void renderCell(final ExecutionContext ectx, final Object value) {
        if (value == null) {
            ectx.cell.setCellType(HSSFCell.CELL_TYPE_BLANK);
        } else
        if (value instanceof Date) {
            ectx.cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            ectx.cell.setCellValue((Date) value);
        } else
        if (value instanceof Calendar) {
            ectx.cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            ectx.cell.setCellValue((Calendar) value);
        } else
        if (value instanceof Double) {
            ectx.cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            ectx.cell.setCellValue((Double)value);
        } else
        if (value instanceof Number) {
            ectx.cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            ectx.cell.setCellValue( new Double(((Number)value).doubleValue()) );
        } else
        if (value instanceof Boolean) {
            ectx.cell.setCellType(HSSFCell.CELL_TYPE_BOOLEAN);
            ectx.cell.setCellValue((Boolean) value);
        } else
        if (value instanceof HSSFRichTextString) {
            ectx.cell.setCellType(HSSFCell.CELL_TYPE_STRING);
            ectx.cell.setCellValue((HSSFRichTextString) value);
        } else {
            final String text = value.toString();
            if (ectx.cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
                ectx.cell.setCellFormula(text);
            } else
            if (text.startsWith(FORMULA)) {
                ectx.cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
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
                    args =  text.substring(si + 1, fi);
                } else {
                    name = text.substring(MACROS_LENGTH);
                    args = null;
                }
                final Macros func = ectx.report.getMacros(name);
                if (func == null)
                    throw new IllegalArgumentException("Unable to find custom function [" + name + "] at row:" + ectx.cell.getRowIndex() + ", cell:" + ectx.cell.getColumnIndex());
                func.call(ectx, args);
            } else {
                ectx.cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                ectx.cell.setCellValue(new HSSFRichTextString(text));
            }
        }
    }

}
