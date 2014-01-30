package org.echosoft.framework.reports.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.echosoft.common.utils.StringUtil;
import org.echosoft.framework.reports.model.events.CellEventListenerHolder;
import org.echosoft.framework.reports.model.events.SectionEventListenerHolder;
import org.echosoft.framework.reports.model.providers.DataProvider;

/**
 * <p>Базовое описание одного секции на отчетном листе.</p>
 * В каждом типе секций используются свои алгоритмы построения отчета.
 *
 * @author Anton Sharapov
 * @see PlainSection
 * @see GroupingSection
 * @see CompositeSection
 */
public abstract class Section implements Serializable, Cloneable {

    /**
     * Уникальный идентификатор секции.
     */
    private final String id;

    /**
     * Если true, то все строки данной секции могут быть свернутыми.
     * Значение по умолчанию - <code>false</code>.
     */
    private boolean collapsible;

    /**
     * Если true, то все строки данной секции будут по умолчанию отображаться свернутыми.
     * Имеет смысл только при когда {@link #collapsible} = true.
     * Значение по умолчанию - <code>false</code>.
     */
    private boolean collapsed;

    /**
     * Если true то все строки отчета в рамках данной секции будут скрытыми.
     * Т.е. соответствующие строки в отчете будут по умолчанию создаваться невидимыми и единственным полезным
     * эффектом от них будет участие в формулах.
     * Значение по умолчанию - <code>false</code>.
     */
    private boolean hidden;

    /**
     * Определяет должна ли данная секция как-либо быть представленной в итоговом отчете. Секция у которой значение
     * данного свойства = <code>false</code> будет просто проигнорирована при построении отчета.
     * Значение по умолчанию - <code>true</code>.
     */
    private boolean rendered;

    /**
     * Описывает обработчики которые вызываются перед началом обработки секции и после окончания обработки секции.
     */
    private List<SectionEventListenerHolder> sectionListeners;

    /**
     * Обработчики которые должны вызваться перед установкой значения в каждую ячейку секции.
     */
    private List<CellEventListenerHolder> cellListeners;

    /**
     * Источник данных для данной секции.
     */
    private DataProvider provider;


    public Section(String id) {
        id = StringUtil.trim(id);
        if (id == null)
            throw new IllegalArgumentException("Report section identifier must be specified");
        this.id = id;
        this.rendered = true;
        this.sectionListeners = new ArrayList<>();
        this.cellListeners = new ArrayList<>();
    }

    /**
     * @return идентификатор секции.
     */
    public String getId() {
        return id;
    }

    /**
     * Имеет смысл только при когда {@link #collapsible} = true.
     *
     * @return true  если все строки данной секции могут быть свернутыми.
     */
    public boolean isCollapsible() {
        return collapsible;
    }
    public void setCollapsible(final boolean collapsible) {
        this.collapsible = collapsible;
    }

    /**
     * @return true  если данная секция должна быть свернутой в итоговом отчете.
     */
    public boolean isCollapsed() {
        return collapsed;
    }
    public void setCollapsed(final boolean collapsed) {
        this.collapsed = collapsed;
    }

    /**
     * @return true если данная секция должна быть невидимой в итоговом отчете.
     */
    public boolean isHidden() {
        return hidden;
    }
    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * Определяет должна ли данная секция как-либо быть представленной в итоговом отчете.
     *
     * @return true если данная секция должна быть отображена в итоговом отчете (значение по умолчанию), false -
     *         если секция должна быть проигнорирована при построении отчета.
     */
    public boolean isRendered() {
        return rendered;
    }
    public void setRendered(final boolean rendered) {
        this.rendered = rendered;
    }

    /**
     * Описывает обработчики которые вызываются перед началом обработки секции и после окончания обработки секции.
     *
     * @return список всех зарегистрированных обработчиков соответствующего события.
     */
    public List<SectionEventListenerHolder> getSectionListeners() {
        return sectionListeners;
    }

    /**
     * Возвращает список всех обработчиков которые вызываются перед установкой значения в каждой новой ячейке секции.
     * Если в отчете не зарегистрировано ни одного обработчика события то метод возвращает пустой список.
     *
     * @return список всех зарегистрированных обработчиков соответствующего события.
     */
    public List<CellEventListenerHolder> getCellListeners() {
        return cellListeners;
    }

    /**
     * @return Источник данных для секции.
     */
    public DataProvider getDataProvider() {
        return provider;
    }
    public void setDataProvider(final DataProvider provider) {
        this.provider = provider;
    }

    /**
     * @return Количество строк шаблона отведенных на описание данной секции.
     */
    public abstract int getTemplateRowsCount();

    /**
     * Возвращает количество колонок отведенных на описание данной секции.
     *
     * @return количество колонок отведенных на описание секции.
     */
    public abstract int getTemplateColumnsCount();

    /**
     * Выполняет глубокое копирование данной секции.
     *
     * @param target ссылка на модель отчета в который будет импортирована создаваемая копия секции.
     * Необходим для выполнения данной операции во избежание избыточного клонирования тех структур отчета, которые могут
     * использоваться разными секциями одного и того же отчета одновременно. К таковым структурам можно отнести:
     * <li> поставщики данных
     * <li> обработчики ошибок
     * <li> таблицы стилей ячеек секций.
     * @return глубокую копию данной секции.
     * @throws CloneNotSupportedException в случае проблем с клонированием какого-нибудь элемента секции.
     */
    public Section cloneSection(final Report target) throws CloneNotSupportedException {
        if (target == null)
            throw new IllegalArgumentException("Target report model must be specified");
        final Section result = (Section) super.clone();
        result.sectionListeners = new ArrayList<>();
        for (SectionEventListenerHolder listener : sectionListeners) {
            result.sectionListeners.add((SectionEventListenerHolder) listener.clone());
        }
        result.cellListeners = new ArrayList<>();
        for (CellEventListenerHolder listener : cellListeners) {
            result.cellListeners.add((CellEventListenerHolder) listener.clone());
        }
        if (provider != null)
            result.provider = target.getProviders().get(provider.getId());
        return result;
    }

    @Override
    public String toString() {
        return "[Section{id:" + id + "}]";
    }
}
