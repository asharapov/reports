package org.echosoft.framework.reports.macros;

import java.util.Collections;
import java.util.List;

import org.echosoft.framework.reports.processor.ExecutionContext;
import org.echosoft.framework.reports.processor.Group;
import org.echosoft.framework.reports.processor.GroupManager;
import org.echosoft.framework.reports.util.POIUtils;

/**
 * <p>Данный макрос возвращает количество записей в группировке.
 * Пример использования в ячейках:
 * <ol>
 *  <li> <span style="border:1px solid black;padding:6px"><code>$M=gcnt</code></span>
 *  <li> <span style="border:1px solid black;padding:6px"><code>$M=gcnt(sectionid)</code></span>
 * </ol>
 *
 * @author Anton Sharapov
 * @see MacrosRegistry
 */
public class GroupItemsCount implements Macros {

    public GroupItemsCount() {
    }

    /**
     * {@inheritDoc}
     */
    public void call(final ExecutionContext ectx, final String arg) {
        final List<Group> children;
        final List<Integer> records;
        if (arg == null) {
            final Group parent = ectx.sectionContext.gm.getCurrentGroup();
            if (parent == null) {
                return;
            }
            children = parent.children;
            records = parent.records;
        } else {
            final GroupManager gm = ectx.history.get(arg).gm;
            if (gm == null) {
                return;
            }
            children = gm.getCompletedRootGroups();
            records = Collections.emptyList();
        }

        if (children.size() > 0) {
            final String colname = POIUtils.getColumnName(ectx.cell.getColumnIndex());
            final StringBuilder out = new StringBuilder(128);
            for (int i = 0, csize = children.size(); i < csize; i++) {
                if (i > 0)
                    out.append('+');
                final Group child = children.get(i);
                out.append(colname);
                out.append(child.startRow + 1);
            }
            final String formula = out.toString();
            ectx.cell.setCellFormula(formula);
        } else {
            final int cnt = records.size();
            ectx.cell.setCellValue(cnt);
        }
    }

}