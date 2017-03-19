package org.echosoft.framework.reports.macros;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.echosoft.common.collections.iterators.ArrayIterator;
import org.echosoft.common.collections.iterators.EnumerationIterator;
import org.echosoft.common.collections.iterators.ObjectArrayIterator;
import org.echosoft.common.utils.StringUtil;
import org.echosoft.framework.reports.processor.ExecutionContext;
import org.echosoft.framework.reports.util.POIUtils;

/**
 * <p>Данный макрос дает возможность в обрабатываемой в текущий момент построителем отчета ячейке указать суммы по определенным строкам из ранее обработанных секций листа.
 * Результат работы макроса оформляется в виде формулы Excel если указанная в аргументе секция содержит хотя бы одну строку данных. В противном случае текущая ячейка останется пустой.<br/>
 * <strong>Важно!</strong> Данная секции с данными должны находиться на том же листе что и ячейка с макросом и должны быть расположены выше этой ячейки
 * (то есть секции должны быть уже обработаны).</p>
 * Макрос требует для работы следующие аргументы:
 * <ol>
 * <li> Название колонки</li>
 * <li> Название атрибута в пространстве имен <code>var</code> который содержит либо массив либо перечисление номеров строк</li>
 * </ol>
 * <p>Примеры использования в ячейках:
 * <ol>
 * <li> <span style="border:1px solid black;padding:6px"><code>$M=fsum(rows, A)</code></span>
 * <p> Суммирует значения в строках полученных из переменной <code>rows</code> в колонке <code>A</code>.</p>
 * </li>
 * <li> <span style="border:1px solid black;padding:6px"><code>$M=fsum(rows)</code></span>
 * <p> Суммирует значения в строках полученных из переменной <code>rows</code> в текущей колонке (определяется по текущей ячейке с макросом).</p>
 * </li>
 * <li> <span style="border:1px solid black;padding:6px"><code>$M=fsum(rows,,first)</code></span>
 * <p> Берет значение из первой строки (из списка строк в первом аргументе) и текущей колонки и помещает его в текущую обрабатываемую ячейку.</p>
 * </li>
 * </ol>
 * </p>
 *
 * @author Anton Sharapov
 * @see MacrosRegistry
 */
public class FSum implements Macros {

    public FSum() {
    }

    @Override
    public void call(final ExecutionContext ectx, final String arg) {
        final List<String> args = StringUtil.split(arg, ',');
        if (args.size() < 1)
            throw new IllegalArgumentException("Incorrect arguments count for macro fnrowsum " + args + " at " + POIUtils.getCellName(ectx.cell));

        // 1. Найдем итератор номеров строк участвующих в формуле ...
        final String attrName = args.get(0);
        Object v = ectx.elctx.getVariables().get(attrName);
        if (v == null) {
            v = ectx.elctx.getEnvironment().get(attrName);
            if (v == null)
                throw new IllegalArgumentException("Can't resolve rows for attribute '" + attrName + "'.");
        }
        final Iterator<Integer> rows = resolveNumbers(v);

        // определим имя колонки участвующей в формуле (если она не задана то используется текущая обрабатываемая колонка) ...
        String colname = args.size() > 1 ? args.get(1).trim() : "";
        if (colname.length() == 0) {
            colname = POIUtils.getColumnName(ectx.cell.getColumnIndex());
        }

        // уточним специальный режим использования (опционально) ...
        final String mode = args.size() > 2 ? args.get(2).trim().toLowerCase() : "";

        if (rows == null || !rows.hasNext()) {
            // исключительная ситуация: нет данных для обработки ...
            ectx.cell.setCellType(CellType.BLANK);
            return;
        }

        if ("first".equals(mode)) {
            calculateFirstRow(ectx.cell, colname, rows);
        } else {
            calculateRowsSummary(ectx.cell, colname, rows);
        }
    }

    @SuppressWarnings("unchecked")
    private Iterator<Integer> resolveNumbers(final Object v) {
        if (v instanceof Iterable) {
            return ((Iterable<Integer>) v).iterator();
        } else
        if (v instanceof Iterator) {
            return (Iterator<Integer>) v;
        } else
        if (v instanceof Enumeration) {
            return new EnumerationIterator<>((Enumeration<Integer>) v);
        } else
        if (v instanceof Integer[]) {
            return new ObjectArrayIterator<>((Integer[]) v);
        } else
        if (v instanceof int[]) {
            return new ArrayIterator(v);
        } else
        if (v == null) {
            return Collections.emptyIterator();
        } else
            throw new IllegalArgumentException("Couldn't cast to integers iterator: " + v);
    }

    private void calculateRowsSummary(final Cell cell, final String colname, final Iterator<Integer> rows) {
        final StringBuilder buf = new StringBuilder(50);
        while (rows.hasNext()) {
            if (buf.length() > 0) {
                buf.append('+');
            }
            buf.append(colname);
            buf.append(rows.next());
        }
        cell.setCellType(CellType.FORMULA);
        cell.setCellFormula(buf.toString());
    }

    private void calculateFirstRow(final Cell cell, final String colname, final Iterator<Integer> rows) {
        cell.setCellType(CellType.FORMULA);
        cell.setCellFormula(colname + rows.next());
    }
}