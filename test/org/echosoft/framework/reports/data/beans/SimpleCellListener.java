package org.echosoft.framework.reports.data.beans;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.echosoft.framework.reports.model.events.CellEvent;
import org.echosoft.framework.reports.model.events.CellEventListener;
import org.echosoft.framework.reports.processor.ExecutionContext;

/**
 * @author Anton Sharapov
 */
public class SimpleCellListener implements CellEventListener {

    public void handle(final CellEvent event) throws Exception {
        if (event.getCellValue() instanceof Number) {
            final Number value = (Number) event.getCellValue();
            final Cell cell = event.getContext().cell;
            if (value.intValue() < 10) {
//                System.out.println("row: "+cell.getRowIndex()+", cell:"+cell.getColumnIndex());
                cell.setCellStyle(getRedStyle(event.getContext()));
            }
        }
    }

    public CellStyle getRedStyle(final ExecutionContext ctx) {
        CellStyle style = (CellStyle) ctx.elctx.getVariables().get("SimpleCellListener.red");
        if (style == null) {
            style = ctx.wb.createCellStyle();
            style.setFillPattern(CellStyle.SOLID_FOREGROUND);
            style.setFillForegroundColor(IndexedColors.CORAL.getIndex());
            style.setBorderBottom(CellStyle.BORDER_THIN);
            style.setBorderTop(CellStyle.BORDER_THIN);
            style.setBorderLeft(CellStyle.BORDER_THIN);
            style.setBorderRight(CellStyle.BORDER_THIN);
        }
        return style;
    }
}
