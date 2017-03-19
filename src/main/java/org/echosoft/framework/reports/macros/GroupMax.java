package org.echosoft.framework.reports.macros;

import org.apache.poi.ss.usermodel.CellType;
import org.echosoft.framework.reports.processor.ExecutionContext;
import org.echosoft.framework.reports.util.POIUtils;

/**
 * <p>Возвращает максимальное значение из всех ячеек текущей колонки в строках, являющихся дочерними для текущей группировки.
 * Оформляется в виде формулы Excel.</p>
 * Пример использования в ячейках:
 * <ol>
 *  <li> <span style="border:1px solid black;padding:6px"><code>$M=gmax</code></span>
 * </ol>
 * @see MacrosRegistry
 * @author Anton Sharapov
 */
public class GroupMax implements Macros {

    public GroupMax() {
    }

    /**
     * {@inheritDoc}
     */
    public void call(final ExecutionContext ectx, final String arg) {
        final String formula = POIUtils.makeGroupFormula(ectx, "MAX");
        if (formula!=null) {
            ectx.cell.setCellType(CellType.FORMULA);
            ectx.cell.setCellFormula( formula );
        }
    }

}