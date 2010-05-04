package org.echosoft.framework.reports.model;

import java.io.Serializable;

/**
 * Описывает стиль ячейки в отчете.
 * 
 * @author Anton Sharapov
 */
public class CellStyle implements Serializable, Cloneable {

    private short id;
    private short alignment;
    private short borderBottom;
    private short borderLeft;
    private short borderRight;
    private short borderTop;
    private Color bottomBorderColor;
    private String dataFormat;
    private Color fillBackgroundColor;
    private Color fillForegroundColor;
    private short fillPattern;
    private Font font;
    private boolean hidden;
    private short indention;
    private Color leftBorderColor;
    private boolean locked;
    private Color rightBorderColor;
    private short rotation;
    private Color topBorderColor;
    private short verticalAlignment;
    private boolean wrapText;

    /**
     * Возвращает идентификатор стиля ячейки в шаблоне отчета.
     * Каждому стилю что используется в отчете соответствует свой идентификатор.
     *
     * @return  идентификатор стиля ячейки в шаблоне отчета.
     */
    public short getId() {
        return id;
    }
    public void setId(short id) {
        this.id = id;
    }

    public short getAlignment() {
        return alignment;
    }
    public void setAlignment(short alignment) {
        this.alignment = alignment;
    }

    public short getBorderBottom() {
        return borderBottom;
    }
    public void setBorderBottom(short borderBottom) {
        this.borderBottom = borderBottom;
    }

    public short getBorderLeft() {
        return borderLeft;
    }
    public void setBorderLeft(short borderLeft) {
        this.borderLeft = borderLeft;
    }

    public short getBorderRight() {
        return borderRight;
    }
    public void setBorderRight(short borderRight) {
        this.borderRight = borderRight;
    }

    public short getBorderTop() {
        return borderTop;
    }
    public void setBorderTop(short borderTop) {
        this.borderTop = borderTop;
    }

    public Color getBottomBorderColor() {
        return bottomBorderColor;
    }
    public void setBottomBorderColor(Color bottomBorderColor) {
        this.bottomBorderColor = bottomBorderColor;
    }

    public String getDataFormat() {
        return dataFormat;
    }
    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }

    public Color getFillBackgroundColor() {
        return fillBackgroundColor;
    }
    public void setFillBackgroundColor(Color fillBackgroundColor) {
        this.fillBackgroundColor = fillBackgroundColor;
    }

    public Color getFillForegroundColor() {
        return fillForegroundColor;
    }
    public void setFillForegroundColor(Color fillForegroundColor) {
        this.fillForegroundColor = fillForegroundColor;
    }

    public short getFillPattern() {
        return fillPattern;
    }
    public void setFillPattern(short fillPattern) {
        this.fillPattern = fillPattern;
    }

    public Font getFont() {
        return font;
    }
    public void setFont(Font font) {
        this.font = font;
    }

    public boolean isHidden() {
        return hidden;
    }
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public short getIndention() {
        return indention;
    }
    public void setIndention(short indention) {
        this.indention = indention;
    }

    public Color getLeftBorderColor() {
        return leftBorderColor;
    }
    public void setLeftBorderColor(Color leftBorderColor) {
        this.leftBorderColor = leftBorderColor;
    }

    public boolean isLocked() {
        return locked;
    }
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Color getRightBorderColor() {
        return rightBorderColor;
    }
    public void setRightBorderColor(Color rightBorderColor) {
        this.rightBorderColor = rightBorderColor;
    }

    public short getRotation() {
        return rotation;
    }
    public void setRotation(short rotation) {
        this.rotation = rotation;
    }

    public Color getTopBorderColor() {
        return topBorderColor;
    }
    public void setTopBorderColor(Color topBorderColor) {
        this.topBorderColor = topBorderColor;
    }

    public short getVerticalAlignment() {
        return verticalAlignment;
    }
    public void setVerticalAlignment(short verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

    public boolean isWrapText() {
        return wrapText;
    }
    public void setWrapText(boolean wrapText) {
        this.wrapText = wrapText;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final CellStyle result = (CellStyle)super.clone();
        if (bottomBorderColor!=null)
            result.bottomBorderColor = (Color)bottomBorderColor.clone();
        if (fillBackgroundColor!=null)
            result.fillBackgroundColor = (Color)fillBackgroundColor.clone();
        if (leftBorderColor!=null)
            result.leftBorderColor = (Color)leftBorderColor.clone();
        if (rightBorderColor!=null)
            result.rightBorderColor = (Color)rightBorderColor.clone();
        if (topBorderColor!=null)
            result.topBorderColor = (Color)topBorderColor.clone();
        if (font!=null)
            result.font = (Font)font.clone();
        return result;
    }

    public int hashCode() {
        return id;
    }
    public boolean equals(Object obj) {
        if (obj==null || !(CellStyle.class.equals(obj.getClass())))
            return false;
        final CellStyle other = (CellStyle)obj;
        return  id==other.id &&
                alignment==other.alignment &&
                borderBottom==other.borderBottom &&
                borderLeft==other.borderLeft &&
                borderRight==other.borderRight &&
                borderTop==other.borderTop &&
                (bottomBorderColor!=null ? bottomBorderColor.equals(other.bottomBorderColor) : other.bottomBorderColor==null) &&
                (dataFormat!=null ? dataFormat.equals(other.dataFormat) : other.dataFormat==null) &&
                (fillBackgroundColor!=null ? fillBackgroundColor.equals(other.fillBackgroundColor) : other.fillBackgroundColor==null) &&
                (fillForegroundColor!=null ? fillForegroundColor.equals(other.fillForegroundColor) : other.fillForegroundColor==null) &&
                fillPattern==other.fillPattern &&
                (font!=null ? font.equals(other.font) : other.font==null) &&
                hidden==other.hidden &&
                indention==other.indention &&
                (leftBorderColor!=null ? leftBorderColor.equals(other.leftBorderColor) : other.leftBorderColor==null) &&
                (rightBorderColor!=null ? rightBorderColor.equals(other.rightBorderColor) : other.rightBorderColor==null) &&
                rotation==other.rotation &&
                (topBorderColor!=null ? topBorderColor.equals(other.topBorderColor) : other.topBorderColor==null) &&
                verticalAlignment==other.verticalAlignment &&
                wrapText==other.wrapText;
    }
}
