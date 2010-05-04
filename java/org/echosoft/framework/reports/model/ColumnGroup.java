package org.echosoft.framework.reports.model;

import java.io.Serializable;

/**
 * Используется для описания группировок колонок на листе отчета.
 *
 * @author Anton Sharapov
 */
public class ColumnGroup implements Serializable, Cloneable {

    /**
     * Порядковый номер первой колонки в группе (начиная с 0).
     */
    private final int from;

    /**
     * Порядковый номер последней колонки в группе (начиная с 0).
     */
    private final int to;

    public ColumnGroup(int from, int to) {
        if (from<0 || to<0 || from==to)
            throw new IllegalArgumentException("Invalid arguments");

        if (from>to) {
            int col = from;
            from = to;
            to = col;
        }
        this.from = from;
        this.to = to;
    }

    public int getFirstColumn() {
        return from;
    }

    public int getLastColumn() {
        return to;
    }

    /**
     *
     * @param group  группа колонок с которой выполняется сравнение.
     * @return  true если данная группа колонок находится внутри группы, переданной в параметрах.
     */
    public boolean insideOf(ColumnGroup group) {
        return (from!=group.from || to!=group.to) &&
               (from>=group.from && to<=group.to);
    }

    /**
     * Возвращает true если данная группа пересекается с указанной в параметрах.
     * @param group  группа колонок для которой выполняется проверка на пересечение.
     * @return  true если данная группа и группа из параметра вызова пересекаются.
     */
    public boolean intersected(ColumnGroup group) {
        return (from<group.from && to>=group.from && to<group.to) ||
               (from>group.from && from<=group.to && to>group.to);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    public int hashCode() {
        return from<<8 + to;
    }
    public boolean equals(Object obj) {
        if (obj==null || !ColumnGroup.class.equals(obj.getClass()))
            return false;
        final ColumnGroup other = (ColumnGroup)obj;
        return from==other.from && to==other.to;
    }
    public String toString() {
        return "[CellGroup{from:"+from+", to:"+to+"}]";
    }
}
