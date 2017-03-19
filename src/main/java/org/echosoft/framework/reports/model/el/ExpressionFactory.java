package org.echosoft.framework.reports.model.el;

import org.apache.poi.ss.usermodel.Cell;
import org.echosoft.framework.reports.util.Logs;
import org.echosoft.framework.reports.util.POIUtils;

/**
 * @author Anton Sharapov
 */
public final class ExpressionFactory {

    public static final Expression EMPTY_EXPRESSION = new BaseExpression(null);

    public static Expression makeExpression(final Cell cell) {
        final Object value = POIUtils.getCellValue(cell);
        try {
            return new BaseExpression( value );
        } catch (Exception e) {
            if (cell!=null) {
                Logs.reports.error("unable to make expression from cell "+POIUtils.getCellName(cell)+" value: ["+value+"]", e);
            }
            throw (RuntimeException)e;
        }
    }

}
