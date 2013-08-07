package org.echosoft.framework.reports.macros;

import org.apache.poi.ss.usermodel.Cell;
import org.echosoft.framework.reports.processor.ExecutionContext;
import org.echosoft.framework.reports.util.POIUtils;

/**
 * <p>Возвращает минимальное значение из всех ячеек текущей колонки в строках, являющихся дочерними для текущей группировки.
 * Оформляется в виде формулы Excel.</p>
 * Пример использования в ячейках:
 * <ol>
 *  <li> <span style="border:1px solid black;padding:6px"><code>$M=gmin</code></span>
 * </ol>
 * @see MacrosRegistry
 * @author Anton Sharapov
 */
public class GroupMin implements Macros {

    public GroupMin() {
    }

    /**
     * {@inheritDoc}
     */
    public void call(final ExecutionContext ectx, final String arg) {
        final String formula = POIUtils.makeGroupFormula(ectx, "MIN");
        if (formula!=null) {
            ectx.cell.setCellType(Cell.CELL_TYPE_FORMULA);
            ectx.cell.setCellFormula( formula );
        }
    }

}