package org.echosoft.framework.reports.macros;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.echosoft.common.utils.StringUtil;
import org.echosoft.framework.reports.processor.ExecutionContext;
import org.echosoft.framework.reports.processor.SectionContext;
import org.echosoft.framework.reports.util.POIUtils;

/**
 * <p>Данный макрос дает возможность в обрабатываемой в текущий момент построителем отчета ячейке указать сумму произведений значений из определенных колонок обработанной ранее, секции.
 * Результат работы макроса оформляется в виде формулы Excel если указанная в аргументе секция содержит хотя бы одну строку данных. В противном случае текущая ячейка останется пустой.<br/>
 * <strong>Важно!</strong> Данная секция уже должна находиться на том же листе что и ячейка с макросом и должна быть расположена
 * выше этой ячейки (то есть секция с данными должна быть уже обработана).</p>
 * Макрос требует для работы следующие аргументы:
 * <ol>
 *  <li> Идентификатор ранее обработанной секции в которой требуется просуммировать произведения ряда ячеек в определенных строках. Обязательный параметр.</li>
 *  <li> Наименования колонок через запятую (формат Alpha-26 используемый в Excel) которые должны участвовать в формуле. Макросу для работы требуется указание как минимум одной колонки.<br/>
 *      Если в аргументах макроса указана только одна колонка то второй участвующей в формуле колонкой будет колонка в которой находится данный макрос.</li>
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
 *  <li> <span style="border:1px solid black;padding:6px"><code>$M=fnrowsumprod(data,E,AC,AB,3,0)</code></span>
 *  <p>В каждой третьей строке секции "data" суммирует произведение значений колонок "E","AB","AC".
 *      Отсчет суммируемых строк начинается с самой первой строки секции.</p>
 *  </li>
 *  <li> <span style="border:1px solid black;padding:6px"><code>$M=fnrowsumprod(mysheet.totals,G,2,1)</code></span>
 *  <p>В каждой второй строке секции "mysheet.totals" суммирует произведение значений колонок "G" и текущей колонки (там где стоит данный макрос).
 *      Отсчет суммируемых строк начинается со второй строки секции (смещение от начала секции равно 1).</p>
 *  </li>
 * </ol>
 * </p>
 * @see FNRowsSum
 * @see MacrosRegistry
 * @see org.echosoft.framework.reports.processor.ExecutionContext#history
 * @author Anton Sharapov
 */
public class FNRowsSumProd implements Macros {

    public FNRowsSumProd() {
    }

    /**
     * {@inheritDoc}
     */
    public void call(final ExecutionContext ectx, final String arg) {
        final String[] args = StringUtil.split(arg, ',');
        if (args==null || args.length<2)
            throw new IllegalArgumentException("Incorrect arguments count for macro fnrowsumprod "+ Arrays.toString(args)+" at "+POIUtils.getCellName(ectx.cell));
        // определим секцию в которой надо просуммировать значения в определенных строках ...
        final SectionContext sctx = ectx.history.get(args[0]);
        if (sctx==null)
            throw new IllegalArgumentException("Unknown section for macro fnrowsumprod "+Arrays.toString(args)+" at "+POIUtils.getCellName(ectx.cell));
        if (sctx.record==0) {
            ectx.cell.setCellValue(0);
            return;
        }
        // определим имена колонок которые участвуют в формуле ...
        final ArrayList<String> colnames = new ArrayList<String>(4);
        int argnum = 1;
        for ( ; argnum<args.length; argnum++) {
            final String col = args[argnum];
            if ( POIUtils.isValidColumnName(args[argnum]) ) {
                colnames.add(col);
            } else
                break;
        }
        if (colnames.size()==0) {
            throw new IllegalArgumentException("Unknown column for macro fnrowsumprod "+Arrays.toString(args)+"at "+POIUtils.getCellName(ectx.cell));
        } else
        if (colnames.size()==1) {
            final String col1 = colnames.get(0);
            final String col2 = POIUtils.getColumnName( ectx.cell.getColumnIndex() );
            if (col2.equals(col1))
                throw new IllegalArgumentException("fnrowsumprod requires as minimum two columns: "+Arrays.toString(args)+"at "+POIUtils.getCellName(ectx.cell));
            colnames.add( col2 );
        }
        // определим строки в которых надо суммировать значения ...
        final String nthstr = args.length>argnum ? args[argnum++].trim() : "";
        final int nth = nthstr.length()>0 ? Integer.parseInt(nthstr,10) : sctx.section.getTemplateRowsCount();
        // определим смещение с которого начинается отсчет используемых макросом строк в секции ...
        final String ofstr = args.length>argnum ? args[argnum].trim() : "";
        final int offset = ofstr.length()>0 ? Integer.parseInt(ofstr,10) : 0;

        process(ectx.cell, sctx, colnames, nth, offset);
    }

    public void process(final HSSFCell cell, final SectionContext sctx, final ArrayList<String> colnames, final int nth, final int offset) {
        final StringBuilder formula = new StringBuilder(32);
        final int top = sctx.sectionFirstRow + 1;
        final int bottom = sctx.sectionFirstRow + sctx.record*sctx.section.getTemplateRowsCount();
        if (top>bottom) {
            cell.setCellValue(0);
            return;
        } else
        if (nth==1) {
            formula.append("SUMPRODUCT(");
            for (int i=0,cnt=colnames.size(); i<cnt; i++) {
                if (i>0)
                    formula.append(',');
                final String colname = colnames.get(i);
                formula.append(colname).append(top).append(':').append(colname).append(bottom);
            }
            formula.append(')');
        } else {
            formula.append("SUMPRODUCT(");
            formula.append("ABS(MOD(ROW(").append(top).append(':').append(bottom).append(")-ROW(A").append(top).append("),").append(nth).append(")=").append(offset).append(")");
            for (int i=0,cnt=colnames.size(); i<cnt; i++) {
                formula.append(',');
                final String colname = colnames.get(i);
                formula.append(colname).append(top).append(':').append(colname).append(bottom);
            }
            formula.append(')');
        }
        cell.setCellFormula(formula.toString());
    }
}