package org.echosoft.framework.reports.macros;

import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.echosoft.common.utils.StringUtil;
import org.echosoft.framework.reports.processor.ExecutionContext;
import org.echosoft.framework.reports.processor.SectionContext;
import org.echosoft.framework.reports.util.POIUtils;

/**
 * <p>Данный макрос дает возможность в обрабатываемой в текущий момент построителем отчета ячейке указать суммы по определенным строкам из другой, обработанной ранее, секции.
 * Результат работы макроса оформляется в виде формулы Excel если указанная в аргументе секция содержит хотя бы одну строку данных. В противном случае текущая ячейка останется пустой.<br/>
 * <strong>Важно!</strong> Данная секция уже должна находиться на том же листе что и ячейка с макросом и должна быть расположена
 * выше этой ячейки (то есть секция должна быть уже обработана).</p>
 * Макрос требует для работы следующие аргументы:
 * <ol>
 *  <li> Идентификатор ранее обработанной секции в которой требуется просуммировать значения в определенных строках определенной колонки. Обязательный параметр.</li>
 *  <li> Наименование колонки (формат Alpha-26 используемый в Excel) в которой должно происходить суммирование значений.<br/>
 *      Если аргумент не указан, то используется колонка, В которой расположен обрабатываемый в настоящий момент макрос.</li>
 *  <li> Целочисленный аргумент, определяющий строки в секции значения которых участвуют в вычислении результата.<br/>
 *      Если в операции суммирования должны участвовать <u>все</u> строки секции то в этом аргументе следует указать значений <b>1</b>;<br/>
 *      если в операции суммирования должны участвовать <u>каждая вторая</u> строки секции то в этом аргументе следует указать значений <b>2</b>;<br/>
 *      если в операции суммирования должны участвовать <u>каждая третья</u> строки секции то в этом аргументе следует указать значений <b>3</b>;<br/>
 *      и так далее. Аргумент может принимать значения в диапазоне от 1 до кол-ва строк в секции где происходит суммирование.<br/>
 *      Данный аргумент является опциональным, значение по умолчанию - количество строк в шаблоне секции.</li>
 *  <li> Целочисленный аргумент, определяющий используемое смещение при определении строк которые должны участвовать в суммировании.
 *      Аргумент может принимать значения в диапазоне от 0 до кол-ва строк в секции-1.<br/>
 *      Данный аргумент является опциональным, значение по умолчанию - 0.</li>
 * </ol>
 * <p>Примеры использования в ячейках:
 * <ol>
 *  <li> <span style="border:1px solid black;padding:6px"><code>$M=fnrowsum(data,AC,3,0)</code></span>
 *  <p>Суммирует значения в каждой третьей строке в колонке "AC" ранее обработанной секции "data". Отсчет суммируемых строк начинается с самой первой строки секции.</p>
 *  </li>
 *  <li> <span style="border:1px solid black;padding:6px"><code>$M=fnrowsum(mysheet.totals,G,2,1)</code></span>
 *  <p>Суммирует значения в каждой второй строке в колонке "G" ранее обработанной секции "mysheet.totals". Отсчет суммируемых строк начинается со второй строки секции.</p>
 *  </li>
 *  <li> <span style="border:1px solid black;padding:6px"><code>$M=fnrowsum(stat,,3)</code></span> <i>(допустим макрос указан в ячейке "C7"...)</i>
 *  <p>Суммирует значения в каждой третьей строке в колонке "C" ранее обработанной секции "stat". Отсчет суммируемых строк начинается с первой строки секции (по умолчанию).</p>
 *  </li>
 *  <li> <span style="border:1px solid black;padding:6px"><code>$M=fnrowsum(stat)</code></span> <i>(допустим макрос указан в ячейке "V88" и секция "stat" в шаблоне состоит из 5 строк...)</i>
 *  <p>Суммирует значения в каждой пятой строке в колонке "V" ранее обработанной секции "stat". Отсчет суммируемых строк начинается с первой строки секции (по умолчанию).</p>
 *  </li>
 * </ol>
 * </p>
 *
 * @author Anton Sharapov
 * @see org.echosoft.framework.reports.macros.NRowsSum
 * @see org.echosoft.framework.reports.macros.MacrosRegistry
 * @see org.echosoft.framework.reports.processor.ExecutionContext#history
 */
public class FNRowsSum implements Macros {

    public FNRowsSum() {
    }

    /**
     * {@inheritDoc}
     */
    public void call(final ExecutionContext ectx, final String arg) {
        final List<String> args = StringUtil.split(arg, ',');
        if (args.size() < 1)
            throw new IllegalArgumentException("Incorrect arguments count for macro fnrowsum " + args + " at " + POIUtils.getCellName(ectx.cell));
        // определим секцию в которой надо просуммировать значения в определенных строках ...
        final SectionContext sctx = ectx.history.get(args.get(0));
        if (sctx == null)
            throw new IllegalArgumentException("Unknown section for macro fnrowsum " + args + " at " + POIUtils.getCellName(ectx.cell));
        if (sctx.record == 0) {
            ectx.cell.setCellValue(0);
            return;
        }
        // определим имя колонки в которой надо просуммировать значения ...
        String colname = args.size() > 1 ? args.get(1).trim() : "";
        if (colname.length() == 0) {
            colname = POIUtils.getColumnName(ectx.cell.getColumnIndex());
        }
        // определим строки в которых надо суммировать значения ...
        final String nthstr = args.size() > 2 ? args.get(2).trim() : "";
        final int nth = nthstr.length() > 0 ? Integer.parseInt(nthstr, 10) : sctx.section.getTemplateRowsCount();
        // определим смещение с которого начинается отсчет используемых макросом строк в секции ...
        final String ofstr = args.size() > 3 ? args.get(3).trim() : "";
        final int offset = ofstr.length() > 0 ? Integer.parseInt(ofstr, 10) : 0;

        process(ectx.cell, sctx, colname, nth, offset);
    }

    public void process(final Cell cell, final SectionContext sctx, final String colname, final int nth, final int offset) {
        final StringBuilder formula = new StringBuilder(32);
        final int top = sctx.sectionFirstRow + 1;
        final int bottom = sctx.sectionFirstRow + sctx.record * sctx.section.getTemplateRowsCount();
        if (top > bottom) {
            cell.setCellValue(0);
            return;
        } else if (nth == 1) {
            formula.append("SUM(").append(colname).append(top + offset).append(':').append(colname).append(bottom).append(')');
        } else {
            formula.append("SUMPRODUCT(ABS(MOD(ROW(").append(top).append(':').append(bottom).append(")-ROW(")
                    .append(colname).append(top).append("),").append(nth).append(")=").append(offset).append("),")
                    .append(colname).append(top).append(':').append(colname).append(bottom).append(")");
        }
        cell.setCellFormula(formula.toString());
    }
}