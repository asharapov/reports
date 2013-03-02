package org.echosoft.framework.reports.model;

import java.io.Serializable;

/**
 * Описывает альтернативные представления одной группировочной строки.
 * Как правило используется при оформлении сложных иерархических группировок.
 *
 * @author Anton Sharapov
 */
public class GroupStyle implements Serializable, Cloneable {

    /**
     * Числовой признак, позволяющий отличить один стиль оформления группы от другого.
     * В группе не может быть зарегистрировано более одного стиля с одинаковым уровнем.
     */
    private int level;

    /**
     * Флаг определяет что данный стиль будет использоваться по умолчанию. В группе может быть максимум один стиль с установленным этим флагом.
     */
    private boolean defaultStyle;

    /**
     * Собственно шаблон оформления.
     */
    private AreaModel template;


    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isDefault() {
        return defaultStyle;
    }
    public void setDefault(boolean defaultStyle) {
        this.defaultStyle = defaultStyle;
    }

    public AreaModel getTemplate() {
        return template;
    }
    public void setTemplate(AreaModel template) {
        this.template = template;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final GroupStyle result = (GroupStyle)super.clone();
        if (template!=null)
            result.template = (AreaModel)template.clone();
        return result;
    }

    @Override
    public int hashCode() {
        return level;
    }

    public boolean equals(final Object obj) {
        if (obj==null || !getClass().equals(obj.getClass()))
            return false;
        final GroupStyle other = (GroupStyle)obj;
        return level==other.level && defaultStyle==other.defaultStyle;
    }

    public String toString() {
        return "[GroupStyle{level:"+level+", default:"+defaultStyle+", height:"+(template!=null ? template.getRowsCount() : 0)+"}]";
    }
}
