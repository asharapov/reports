package org.echosoft.framework.reports.model.events;

import org.echosoft.framework.reports.processor.ExecutionContext;
import org.echosoft.framework.reports.util.POIUtils;

/**
 * <p>Описывает событие происходящее каждый раз перед установкой значения в текущую ячейку формируемого отчета.
 * Всю информацию о текущей ячейку можно получить из свойства {@link ExecutionContext#cell}.
 * Обработчики событий данного типа регистрируются тех секциях отчета для которых это необходимо.</p>
 * <p>Обработчик данного события может выполнять следующие задачи:
 * <li> Измение объекта который будет вставлен в текущую ячейку. Тип ячейки будет установлен автоматически в зависимости
 * от типа объекта используемого в качестве значения ячейки.
 * <li> Изменение любых свойств текущей ячейки. Если при этом стоит запретить генератору отчета самому устанавливать
 * значение ячейки (то есть это сделает сам обработчик события) то для этого в событии надо установить свойство
 * {@link #isRendered()} в <code>true</code>.</p>
 * <p>Генератор отчета гарантирует что перед вызовом первого обработчика данного типа для каждой ячейки секции
 * эта ячейка уже создана (в текущей строке создан соответствующий объект {@link org.apache.poi.ss.usermodel.Cell})
 * и эта ячейка имеет пустое значение (с типом {@link org.apache.poi.ss.usermodel.Cell#CELL_TYPE_BLANK}).</p>
 *
 * @author Anton Sharapov
 */
public class CellEvent {

    private final ExecutionContext context;
    private Object cellValue;
    private boolean rendered;

    public CellEvent(ExecutionContext context) {
        if (context==null)
            throw new IllegalArgumentException("Execution context must be specified");
        this.context = context;
    }

    /**
     * Возвращает контекст выполнения для формируемого отчета. Данная структура содержит максимально полную информацию
     * о текущем состоянии процесса формирования отчета. Обработчик может изменять некоторые данные в ней но следует
     * соблюдать при этом осторожность.
     *
     * @return контекст выполнения для данного отчета.
     */
    public ExecutionContext getContext() {
        return context;
    }


    /**
     * Возвращает объект который должен быть использован генератором как значение для текущей обрабатываемой ячейки
     * (см {@link ExecutionContext#cell}).<br/>
     * По умолчанию метод возвращает то значение которое использовалось бы генератором отчета если бы не было никаких
     * обработчиков событий.
     *
     * @return объект который будет использован в качестве значения ячейки.
     */
    public Object getCellValue() {
        return cellValue;
    }

    /**
     * Указывает объект который будет использоваться как значение для текущей обрабатываемой ячейки
     * (см {@link ExecutionContext#cell}).
     *
     * @param value  новое значение ячейки.
     */
    public void setCellValue(Object value) {
        this.cellValue = value;
    }


    /**
     * <p>Определяет установлено ли значение текущей ячейки обработчиком самостоятельно или генератору отчета придется самому
     * устанавливать в эту ячейку ее значение (в этом случае будет браться значение свойства {@link #getCellValue()}).<br/>
     * По умолчанию значением данного свойства является <code>false</code>.</p>
     * <p> Если для секции указано более одного обработчика событий данного типа то не взирая на значение данного свойства
     * будут последовательно вызваны все из них.</p>
     *
     * @return <code>true</code> если обработчик события самостоятельно сформировал значение ячейки и генератору отчета
     * не требуется самостоятельно устанавливать значение этой ячейки.
     */
    public boolean isRendered() {
        return rendered;
    }

    /**
     * Указывает признак, определяющий закончена ли обработка текущей ячейки обработчиком события самостоятельно или
     * генератор отчетов должен продолжать вызывать следующие обработчики событий по цепочке и, в перспективе,
     * самостоятельно установить значение ячейки.
     *
     * @param rendered  признак завершенности обработки данной ячейки.
     */
    public void setRendered(boolean rendered) {
        this.rendered = rendered;
    }


    public String toString() {
        final StringBuilder buf = new StringBuilder(64);
        buf.append("[CellEvent{cell:");
        buf.append(POIUtils.getColumnName(context.cell.getColumnIndex()));
        buf.append(context.cell.getRowIndex());
        buf.append(", value:");
        buf.append(cellValue);
        buf.append(", rendered:");
        buf.append(rendered);
        buf.append("}]");
        return buf.toString();
    }
}
