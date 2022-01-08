package org.echosoft.framework.reports.macros;

import java.util.List;

import org.apache.poi.ss.usermodel.CellType;
import org.echosoft.framework.reports.processor.ExecutionContext;
import org.echosoft.framework.reports.processor.Group;
import org.echosoft.framework.reports.processor.GroupManager;
import org.echosoft.framework.reports.util.POIUtils;

/**
 * <p>Выполняет суммирование всех ячеек текущей колонки в строках, являющихся дочерними для текущей группировки.
 * Оформляется в виде формулы Excel.</p>
 * Пример использования в ячейках:
 * <ol>
 *  <li> <span style="border:1px solid black;padding:6px"><code>$M=gsum</code></span>
 * </ol>
 * @see MacrosRegistry
 * @author Anton Sharapov
 */
public class GroupSum implements Macros {

    public GroupSum() {
    }

    /**
     * {@inheritDoc}
     */
    public void call(final ExecutionContext ectx, final String arg) {
        final GroupManager gm = arg == null ? ectx.sectionContext.gm : ectx.history.get(arg).gm;
        if (gm == null) {
            return;
        }
        final Group group = gm.getCurrentGroup();
        final List<Group> children = group != null ? group.children : gm.getCompletedRootGroups();
        final String colname = POIUtils.getColumnName(ectx.cell.getColumnIndex());

        final String formula;
        final int csize = children.size();
        final int rsize;
        if (csize>30) {
            final StringBuilder out = new StringBuilder(128);
            for (int i=0; i<csize; i++) {
                if (i>0)
                    out.append('+');
                final Group child = children.get(i);
                out.append(colname);
                out.append(child.startRow+1);
            }
            formula = out.toString();
        } else
        if (group != null && (rsize = group.records.size())>30 && (group.recordsHeight==null || group.recordsHeight!=1)) {
            final StringBuilder out = new StringBuilder(128);
            for (int i=0; i<rsize; i++) {
                if (i>0)
                    out.append('+');
                out.append(colname);
                out.append(group.records.get(i)+1);
            }
            formula = out.toString();
        } else {
            formula = POIUtils.makeGroupFormula(gm, colname, "SUM");
        }

        if (formula!=null) {
            ectx.cell.setCellFormula( formula );
        } else {
            ectx.cell.setBlank();
        }
    }

}
