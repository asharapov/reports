package org.echosoft.framework.reports.model.el;

import org.apache.poi.ss.usermodel.Cell;
import org.echosoft.framework.reports.util.POIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Anton Sharapov
 */
public final class ExpressionFactory {

    private static final Logger log = LoggerFactory.getLogger(ExpressionFactory.class);
    public static final Expression EMPTY_EXPRESSION = new BaseExpression(null);

    public static Expression makeExpression(final Cell cell) {
        final Object value = POIUtils.getCellValue(cell);
        try {
            return new BaseExpression(value);
        } catch (Exception e) {
            if (cell != null) {
                log.error("unable to make expression from cell " + POIUtils.getCellName(cell) + " value: [" + value + "]", e);
            }
            throw (RuntimeException) e;
        }
    }

}
