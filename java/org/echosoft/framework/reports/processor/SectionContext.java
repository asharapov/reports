package org.echosoft.framework.reports.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.echosoft.common.query.BeanIterator;
import org.echosoft.framework.reports.model.Section;
import org.echosoft.framework.reports.model.el.ELContext;
import org.echosoft.framework.reports.model.events.CellEventListener;
import org.echosoft.framework.reports.model.events.CellEventListenerHolder;
import org.echosoft.framework.reports.model.events.SectionEventListener;
import org.echosoft.framework.reports.model.events.SectionEventListenerHolder;

/**
 * Контекст обработки секции
 *
 * @author Anton Sharapov
 */
public final class SectionContext {

    /**
     * Контекст обработки родительской секции (если таковая имеется) или <code>null</code>.
     */
    public final SectionContext parent;

    /**
     * Обрабатываемая в настоящее время секция отчета (модель).
     */
    public final Section section;

    /**
     * Ассоциированный с текущей секцией список подписчиков на событие "отрисовка ячейки".
     */
    public final List<SectionEventListener> sectionListeners;

    /**
     * Ассоциированный с текущей секцией список подписчиков на событие "отрисовка ячейки".
     */
    public final List<CellEventListener> cellListeners;

    /**
     * Итератор данных, полученных из поставщика данных ассоциированных с этой секцией.
     * Если к секции не подключен ни один поставщик данных то во все время обработки этой секции это поле будет равно <code>null</code>.<br/>
     * Данный итератор может активно использоваться при конструировании поставщика данных для дочерних секций (см. композитные секции).
     */
    public BeanIterator beanIterator;

    /**
     *  Текущая обрабатываемая запись полученная от поставщика данных.
     */
    public Object bean;

    /**
     * Ассоциированный с текущей секцией контроллер по управлению группировкой данных в секции.
     */
    public GroupManager gm;

    /**
     * Индекс первой строки (начиная с 0) которую занимает данная секция в формируемом отчете.
     */
    public final int sectionFirstRow;

    /**
     * Индекс первой строки (начиная с 0) которую занимает текущая запись из источника данных в формируемом отчете.
     * В случае когда секция не ассоциирована с источником данных значение данного поля будет совпадать с полем {@link #sectionFirstRow}.
     */
    public int recordFirstRow;

    /**
     * Порядковый номер (начиная с 0) обрабатываемой записи в источнике данных.
     * В случае когда секция не ассоциирована с источником данных данное поле всегда будет равно 0.
     * Во время обработки события 'after-section' содержит количество обработанных записей (0 если в секции небыло ни одной записи).
     */
    public int record;

    /**
     * Переменные окружения время жизни которых ограничено временем обработки данной секции.
     */
    private Map<String,Object> env;

    public SectionContext(final SectionContext parent, final Section section, final int firstRow, final ELContext elctx) {
        this.parent = parent;
        this.section = section;
        this.sectionListeners = new ArrayList<SectionEventListener>();
        for (SectionEventListenerHolder holder : section.getSectionListeners()) {
            final SectionEventListener listener = holder.getListener(elctx);
            if (listener != null)
                this.sectionListeners.add(listener);
        }
        this.cellListeners = new ArrayList<CellEventListener>();
        for (CellEventListenerHolder holder : section.getCellListeners()) {
            final CellEventListener listener = holder.getListener(elctx);
            if (listener != null)
                this.cellListeners.add(listener);
        }
        this.sectionFirstRow = firstRow;
        this.recordFirstRow = firstRow;
    }

    /**
     * Возвращает значение переменной окружения время жизни которой ограничено временем обработки данной секции.
     * @param name  имя переменной.
     * @return  значение переменной или <code>null</code> если она не определена.
     */
    @SuppressWarnings("unchecked")
    public <T> T getVariable(final String name) {
        return env!=null ? (T)env.get(name) : null;
    }

    /**
     * Устанавливает значение переменной окружения время жизни которой ограничено временем обработки данной секции.
     * @param name  имя переменной.
     * @param value  значение переменной.
     */
    public void putVariable(final String name, final Object value) {
        if (env==null)
            env = new HashMap<String,Object>();
        env.put(name, value);
    }

}
