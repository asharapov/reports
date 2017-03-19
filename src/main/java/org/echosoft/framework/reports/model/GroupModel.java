package org.echosoft.framework.reports.model;

import java.io.Serializable;
import java.util.TreeMap;

/**
 * Позволяет группировать в отчете строки по некоторым полям. Использование группировок в модели отчета дает нам
 * ряд возможностей:
 * <li> указывать отдельное стилевое оформление соответствующих строк отчета.
 * <li> использовать разного рода агрегатные функции в ячейках отчета (формулы в Excel).
 *
 * @author Anton Sharapov
 */
public class GroupModel implements Serializable, Cloneable {

    /**
     * По какому полю источника данных осуществляется группировка.
     * Значение данного поля должно быть всегда указано за исключением если надо использовать т.н. группировку
     * верхнего уровня, когда группируются все строки вместе взятые.
     */
    private String discriminatorField;

    /**
     * Используется в случае когда данную группировку требуется представить в иерархическом стиле.
     * Должно указывать на числовое поле (унаследованное от {@link Number}.
     * После приведения к целому положительному числу это поле служит основанием для выбора одного из
     * альтернативных стилей оформления строки с группировкой.
     */
    private String levelField;


    /**
     * Если true то строка с данной группировкой (и все нижележащие в группе строки) будут считаться скрытой.
     * Т.е. соответствующие строки в отчете будут по умолчанию создаваться невидимыми и единственным полезным
     * эффектом от них будет участие в формулах.
     */
    private boolean hidden;

    /**
     * Если <code>true</code> (по умолчанию) то строки относящиеся к данной секции можно сворачивать.
     */
    private boolean collapsible;

    /**
     * Если <code>true</code>, то строка с данной группировкой (и все нижележащие в группе строки) будут по умолчанию
     * отображаться свернутыми.
     */
    private boolean collapsed;

    /**
     * Определяет реакцию построителя отчета на ситуацию когда значение дискриминатора в группе равно <code>null</code>
     * (при непустом значении свойства {@link #discriminatorField}).
     * Если свойство равно <code>false</code> (по умолчанию) то группы со значением дискриминатора равным <code>null</code> будут
     * включаться в отчет без какой-либо специальной обработки, если свойство равно <code>true</code> то такие группы будут пропускаться и не попадать в отчет.
     * Также в отчет не попадут и все их дочерние группы (если таковые будут объявлены в модели отчета).
     */
    private boolean skipEmptyGroups;

    /**
     * Определяет количество строк в итоговом отчете, отведенных на представление заголовка этой группировки.
     * Должно быть 1 (по умолчанию) или более. Все стили оформления данной группировки в отчете должны состоять
     * из одинакового количества строк.
     */
    private int rowsCount;

    /**
     * Полный перечень всех стилей отображения для данной группировочной строки. Должен содержать как минимум
     * один стиль. Все стили оформления данной группировки в отчете должны состоять
     * из одинакового количества строк.
     */
    private TreeMap<Integer, GroupStyle> styles;


    private transient GroupStyle defaultStyle;
    private transient int columnsCount;


    public GroupModel() {
        collapsible = true;
        styles = new TreeMap<>();
    }


    public String getDiscriminatorField() {
        return discriminatorField;
    }
    public void setDiscriminatorField(String discriminatorField) {
        this.discriminatorField = discriminatorField;
    }


    public String getLevelField() {
        return levelField;
    }
    public void setLevelField(String levelField) {
        this.levelField = levelField;
    }


    public boolean isHidden() {
        return hidden;
    }
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isCollapsible() {
        return collapsible;
    }
    public void setCollapsible(boolean collapsible) {
        this.collapsible = collapsible;
    }

    public boolean isCollapsed() {
        return collapsed;
    }
    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    public boolean isSkipEmptyGroups() {
        return skipEmptyGroups;
    }
    public void setSkipEmptyGroups(boolean skipEmptyGroups) {
        this.skipEmptyGroups = skipEmptyGroups;
    }

    /**
     * Возвращает количество строк в отчете отведенных на описание данной группы.
     *
     * @return количество строк в итоговом отчете занятых одной группой.
     */
    public int getRowsCount() {
        if (rowsCount == 0) {
            rowsCount = getDefaultStyle().getTemplate().getRowsCount();
        }
        return rowsCount;
    }

    /**
     * Возвращает количество колонок отведенных на описание данной группы.
     *
     * @return количество колонок отведенных на описание группы.
     */
    public int getColumnsCount() {
        if (columnsCount <= 0) {
            int result = 0;
            for (GroupStyle style : styles.values()) {
                result = Math.max(result, style.getTemplate().getColumnsCount());
            }
            columnsCount = result;
        }
        return columnsCount;
    }

    /**
     * Возвращает количество зарегистрированных вариантов оформления данной группировки.
     *
     * @return количество вариантов оформления группировки.
     */
    public int getStylesCount() {
        return styles.size();
    }

    /**
     * Регистрирует новый стиль оформления данной группы.
     *
     * @param style новый стиль оформления.
     */
    public void addStyle(final GroupStyle style) {
        if (style == null)
            throw new IllegalArgumentException("Group style not specified");
        if (styles.containsKey(style.getLevel()))
            throw new IllegalArgumentException("Style with same level already registered");
        if (style.isDefault()) {
            for (GroupStyle st : styles.values()) {
                if (st.isDefault())
                    throw new IllegalArgumentException("Default style alreadt registered");
            }
            defaultStyle = style;
        }
        columnsCount = 0;
        styles.put(style.getLevel(), style);
    }

    /**
     * Отбирает стиль оформления по его уровню.
     *
     * @param level отличительный признак искомого стиля оформления группировки. Не может быть меньше нуля.
     * @return стиль с указанным признаком (если таковой зарегистрирован в группе) или, стиль по умолчанию если искомый стиль не был найден в группе.
     */
    public GroupStyle getStyleByLevel(final int level) {
        GroupStyle style = styles.get(level);
        if (style == null) {
            style = getDefaultStyle();
        }
        return style;
    }

    /**
     * Возвращает стиль по умолчанию для представления данной группировочной строки.
     *
     * @return стиль представления по умолчанию.
     */
    public GroupStyle getDefaultStyle() {
        if (defaultStyle == null) {
            for (GroupStyle style : styles.values()) {
                if (style.isDefault()) {
                    defaultStyle = style;
                    break;
                }
            }
            if (defaultStyle == null && styles.size() > 0) {
                defaultStyle = styles.values().iterator().next();
            }
        }
        return defaultStyle;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final GroupModel result = (GroupModel) super.clone();
        result.styles = new TreeMap<>();
        for (GroupStyle style : styles.values()) {
            result.styles.put(style.getLevel(), (GroupStyle) style.clone());
        }
        return result;
    }

    @Override
    public String toString() {
        return "[GroupModel{discriminator:" + discriminatorField + ", level:" + levelField + ", styles:" + styles.size() + ", collapsed:" + collapsed + ", hidden:" + hidden + "}]";
    }
}
