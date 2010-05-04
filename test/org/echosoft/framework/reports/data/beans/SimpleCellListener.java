package org.echosoft.framework.reports.data.beans;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.echosoft.framework.reports.model.events.CellEvent;
import org.echosoft.framework.reports.model.events.CellEventListener;
import org.echosoft.framework.reports.processor.ExecutionContext;

/**
 * @author Anton Sharapov
 */
public class SimpleCellListener implements CellEventListener {

    public void handle(CellEvent event) throws Exception {
        if (event.getCellValue() instanceof Number) {
            final Number value = (Number)event.getCellValue();
            final HSSFCell cell = event.getContext().cell;
            if (value.intValue()<10) {
//                System.out.println("row: "+cell.getRowIndex()+", cell:"+cell.getColumnIndex());
                cell.setCellStyle( getRedStyle(event.getContext()) );
            }
        }
    }

    public HSSFCellStyle getRedStyle(ExecutionContext ctx) {
        HSSFCellStyle style = (HSSFCellStyle)ctx.elctx.getVariables().get("SimpleCellListener.red");
        if (style==null) {
            style = ctx.wb.createCellStyle();
            style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            style.setFillForegroundColor(HSSFColor.CORAL.index);
            style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
            style.setBorderTop(HSSFCellStyle.BORDER_THIN);
            style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
            style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        }
        return style;
    }
}
