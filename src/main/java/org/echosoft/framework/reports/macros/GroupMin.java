package org.echosoft.framework.reports.macros;

import org.apache.poi.ss.usermodel.CellType;
import org.echosoft.framework.reports.processor.ExecutionContext;
import org.echosoft.framework.reports.processor.GroupManager;
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
        final GroupManager gm = arg == null ? ectx.sectionContext.gm : ectx.history.get(arg).gm;
        if (gm == null) {
            return;
        }
        final String colname = POIUtils.getColumnName(ectx.cell.getColumnIndex());

        final String formula = POIUtils.makeGroupFormula(gm, colname, "MIN");
        if (formula!=null) {
            ectx.cell.setCellType(CellType.FORMULA);
            ectx.cell.setCellFormula( formula );
        } else {
            ectx.cell.setCellType(CellType.BLANK);
        }
    }

}