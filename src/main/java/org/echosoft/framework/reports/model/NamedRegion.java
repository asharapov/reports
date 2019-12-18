package org.echosoft.framework.reports.model;

import java.io.Serializable;

import org.echosoft.framework.reports.common.utils.StringUtil;

/**
 * Описание именованного региона, который будет создан по завершении рендеринга секции отчета.
 */
public class NamedRegion implements Serializable, Cloneable {

    private String name;
    private int firstColumn;
    private int lastColumn;
    private String comment;

    public NamedRegion() {
    }

    public NamedRegion(final String name, final int firstColumn, final int lastColumn) {
        this.name = name;
        this.firstColumn = firstColumn >= 0 ? firstColumn : 0;
        this.lastColumn = lastColumn >= 0 ? lastColumn : 0;
    }

    public String getName() {
        return name;
    }
    public void setName(final String name) {
        this.name = name;
    }

    public int getFirstColumn() {
        return firstColumn;
    }
    public void setFirstColumn(final int firstColumn) {
        this.firstColumn = firstColumn >= 0 ? firstColumn : 0;
    }

    public int getLastColumn() {
        return lastColumn;
    }
    public void setLastColumn(final int lastColumn) {
        this.lastColumn = lastColumn >= 0 ? lastColumn : 0;
    }

    public String getComment() {
        return comment;
    }
    public void setComment(final String comment) {
        this.comment = StringUtil.trim(comment);
    }

    @Override
    public String toString() {
        return "[Name{name:" + name + ", firstCol:" + firstColumn + ", lastCol:" + lastColumn + ", comment:" + comment + "}]";
    }

    @Override
    public NamedRegion clone() throws CloneNotSupportedException {
        return (NamedRegion)super.clone();
    }
}
