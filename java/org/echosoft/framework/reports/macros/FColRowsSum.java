package org.echosoft.framework.reports.macros;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
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
 * <li> <span style="border:1px solid black;padding:6px"><code>$M=fcolrowssum(A,rows)</code></span>
 * <p> Суммирует значения в строках полученных из переменной <code>rows</code> в колонке <code>A</code>.</p>
 * </li>
 * </ol>
 * </p>
 *
 * @author Anton Sharapov
 * @see MacrosRegistry
 */
public class FColRowsSum implements Macros {

    public FColRowsSum() {
    }

    @Override
    public void call(final ExecutionContext ectx, final String arg) {
        final String[] args = StringUtil.split(arg, ',');
        if (args == null || args.length < 2)
            throw new IllegalArgumentException("Incorrect arguments count for macro fnrowsum " + Arrays.toString(args) + " at " + POIUtils.getCellName(ectx.cell));
        final String attrName = args[1];
        Object v = ectx.elctx.getVariables().get(attrName);
        if (v == null) {
            v = ectx.elctx.getEnvironment().get(attrName);
            if (v == null)
                throw new IllegalArgumentException("Can't resolve rows for attribute '" + attrName + "'.");
        }
        final Iterator<Integer> rows = resolveNumbers(v);
        process(ectx.cell, args[0], rows);
    }

    @SuppressWarnings("unchecked")
    private Iterator<Integer> resolveNumbers(final Object v) {
        if (v instanceof Iterable) {
            return ((Iterable<Integer>)v).iterator();
        } else
        if (v instanceof Iterator) {
            return (Iterator<Integer>)v;
        } else
        if (v instanceof Enumeration) {
            return new EnumerationIterator<>((Enumeration<Integer>)v);
        } else
        if (v instanceof Integer[]) {
            return new ObjectArrayIterator<>((Integer[])v);
        } else
        if (v instanceof int[]) {
            return new ArrayIterator(v);
        } else
        if (v == null) {
            return Collections.emptyIterator();
        } else
            throw new IllegalArgumentException("Couldn't cast to integers iterator: " + v);
    }

    public void process(final Cell cell, final String colname, final Iterator<Integer> rows) {
        final StringBuilder buf = new StringBuilder(50);
        while (rows.hasNext()) {
            if (buf.length() > 0) {
                buf.append('+');
            }
            buf.append(colname);
            buf.append(rows.next());
        }
        cell.setCellType(Cell.CELL_TYPE_FORMULA);
        cell.setCellFormula(buf.toString());
    }
}