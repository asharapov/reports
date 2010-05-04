package org.echosoft.framework.reports.macros;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.echosoft.framework.reports.processor.ExecutionContext;
import org.echosoft.framework.reports.processor.Group;

/**
 * <p>Если данный макрос используется в заголовке группировки то он возвращает количество дочерних записей в группировке.
 * Если данный макрос используется в одной из строк под группировкой то он возвращает порядковый номер данной записи в текущей группировке</p>
 * Пример использования в ячейках:
 * <ol>
 *  <li> <span style="border:1px solid black;padding:6px"><code>$M=gitemnum</code></span>
 * </ol>
 * @see MacrosRegistry
 * @author Anton Sharapov
 */
public class GroupItemNum implements Macros {

    public GroupItemNum() {
    }

    /**
     * {@inheritDoc}
     */
    public void call(final ExecutionContext ectx, final String arg) {
        final Group group = ectx.sectionContext.gm.getCurrentGroup();
        if (group==null)
            return;

        final int number;
        if (group.children.size()>0) {
            number = group.children.size();
        } else {
            number = group.records.size();
        }
        ectx.cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
        ectx.cell.setCellValue(number);
    }

}