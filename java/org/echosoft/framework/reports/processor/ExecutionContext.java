package org.echosoft.framework.reports.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.echosoft.framework.reports.model.Report;
import org.echosoft.framework.reports.model.SheetModel;
import org.echosoft.framework.reports.model.el.ELContext;
import org.echosoft.framework.reports.model.events.ReportEventListener;
import org.echosoft.framework.reports.model.events.ReportEventListenerHolder;
import org.echosoft.framework.reports.util.POIUtils;

/**
 * Объединяет группу часто используемых при построении отчета объектов чтобы
 * избежать необходимости их передачи во все методы по отдельности.
 *
 * @author Anton Sharapov
 */
public final class ExecutionContext {

    /**
     * Содержит информацию используемую при вычислении выражений в ячейках отчета.
     */
    public final ELContext elctx;

    /**
     * Модель формируемого отчета.
     */
    public final Report report;

    /**
     * Обработчики связанные с построением отчета в целом.
     */
    public final List<ReportEventListener> listeners;

    /**
     * Обрабатываемый в настоящее время лист отчета (модель)
     */
    public SheetModel sheet;

    /**
     * Контекст обработки текущей секции.
     */
    public SectionContext sectionContext;

    /**
     * Контексты секций которые были обработаны в этом отчете ранее.
     * Дает возможность сослаться к содержимому уже отрисованных секций из последующих секций.
     * Если какая-то секция была обработана более одного раза (если она была например вложена в какую-либо композитную секцию с источником данных)
     * то в истории сохраняется контекст последнего ее вызова.
     */
    public final Map<String, SectionContext> history;

    /**
     * Таблица трансляции номеров стилей ячеек шаблона в стили итогового отчета.
     */
    public final Map<Short, HSSFCellStyle> styles;

    /**
     * Формируемый итоговый отчет.
     */
    public final HSSFWorkbook wb;

    /**
     * Обрабатываемый в настоящее время лист итогового отчета.
     */
    public Sheet wsheet;

    /**
     * Обрабатываемая в настоящее время ячейка итогового отчета.
     */
    public Cell cell;


    public ExecutionContext(Report report, ELContext ctx, HSSFWorkbook wb, Map<Short,HSSFCellStyle> styles) {
        this.elctx = ctx;
        this.report = report;
        this.sectionContext = null;
        this.wb = wb;
        this.styles = styles;
        this.history = new HashMap<String,SectionContext>();
        this.listeners = new ArrayList<ReportEventListener>();
        for (final ReportEventListenerHolder holder : report.getListeners()) {
            final ReportEventListener listener = holder.getListener(ctx);
            if (listener != null)
                listeners.add(listener);
        }
    }

    /**
     * Возвращает индекс (начиная с 0) последней строки созданной на текущем листе.
     *
     * @return индекс последней строки или -1 если лист не имеет ни одной строчки.
     */
    public int getLastRowNum() {
        return wsheet.getPhysicalNumberOfRows() > 0 ? wsheet.getLastRowNum() : -1;
    }

    /**
     * Возвращает индекс (начиная с 0) последней строки + 1.
     * Используется как индекс строки которая должна быть добавлена в конец текущего листа.
     *
     * @return индекс последней строки + 1.
     */
    public int getNewRowNum() {
        return wsheet.getPhysicalNumberOfRows() > 0 ? wsheet.getLastRowNum() + 1 : 0;
    }


    @Override
    public String toString() {
        final StringBuilder out = new StringBuilder(256);
        out.append("[ExecutionContext{report:");
        out.append(report.getId());
        out.append(", sheet:");
        out.append(sheet!=null?sheet.getId():"null");
        if (sectionContext!=null) {
            out.append(", section:");
            out.append(sectionContext.section.getId());
            out.append(", record:");
            out.append(sectionContext.record);
            out.append(", cell:");
            out.append(POIUtils.getCellName(cell));
        }
        out.append("}]");
        return out.toString();
    }
}
