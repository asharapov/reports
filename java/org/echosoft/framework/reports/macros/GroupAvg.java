package org.echosoft.framework.reports.macros;

import org.apache.poi.ss.usermodel.CellType;
import org.echosoft.framework.reports.processor.ExecutionContext;
import org.echosoft.framework.reports.processor.Group;
import org.echosoft.framework.reports.util.POIUtils;

/**
 * <p>Возвращает среднее значение из всех ячеек текущей колонки в строках, являющихся дочерними для текущей группировки.
 * Оформляется в виде формулы Excel.</p>
 * Пример использования в ячейках:
 * <ol>
 *  <li> <span style="border:1px solid black;padding:6px"><code>$M=gavg</code></span>
 * </ol>
 * @see MacrosRegistry
 * @author Anton Sharapov
 */
public class GroupAvg implements Macros {

    public GroupAvg() {
    }

    /**
     * {@inheritDoc}
     */
    public void call(final ExecutionContext ectx, final String arg) {
        final Group group = ectx.sectionContext.gm.getCurrentGroup();
        if (group==null)
            return;

        final String formula;
        final int csize = group.children.size();
        final int rsize = group.records.size();
        if (csize>30) {
            final String colname = POIUtils.getColumnName(ectx.cell.getColumnIndex());
            final StringBuilder out = new StringBuilder(128);
            out.append('(');
            for (int i=0; i<csize; i++) {
                if (i>0)
                    out.append('+');
                final Group child = group.children.get(i);
                out.append(colname);
                out.append(child.startRow+1);
            }
            out.append(")/");
            out.append(csize);
            formula = out.toString();
        } else
        if (rsize>30 && (group.recordsHeight==null || group.recordsHeight!=1)) {
            final String colname = POIUtils.getColumnName(ectx.cell.getColumnIndex());
            final StringBuilder out = new StringBuilder(128);
            out.append('(');
            for (int i=0; i<rsize; i++) {
                if (i>0)
                    out.append('+');
                out.append(colname);
                out.append(group.records.get(i)+1);
            }
            out.append(")/");
            out.append(rsize);
            formula = out.toString();
        } else {
            formula = POIUtils.makeGroupFormula(ectx, "AVERAGE");
        }

        if (formula!=null) {
            ectx.cell.setCellType(CellType.FORMULA);
            ectx.cell.setCellFormula( formula );
        }
    }

}