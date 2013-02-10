package org.echosoft.framework.reports.model;

import java.io.Serializable;

/**
 * Описывает стиль ячейки в отчете.
 * 
 * @author Anton Sharapov
 */
public class CellStyleModel implements Serializable, Cloneable {

    private short id;
    private short alignment;
    private short borderBottom;
    private short borderLeft;
    private short borderRight;
    private short borderTop;
    private ColorModel bottomBorderColor;
    private String dataFormat;
    private ColorModel fillBackgroundColor;
    private ColorModel fillForegroundColor;
    private short fillPattern;
    private FontModel font;
    private boolean hidden;
    private short indention;
    private ColorModel leftBorderColor;
    private boolean locked;
    private ColorModel rightBorderColor;
    private short rotation;
    private ColorModel topBorderColor;
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

    public ColorModel getBottomBorderColor() {
        return bottomBorderColor;
    }
    public void setBottomBorderColor(ColorModel bottomBorderColor) {
        this.bottomBorderColor = bottomBorderColor;
    }

    public String getDataFormat() {
        return dataFormat;
    }
    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }

    public ColorModel getFillBackgroundColor() {
        return fillBackgroundColor;
    }
    public void setFillBackgroundColor(ColorModel fillBackgroundColor) {
        this.fillBackgroundColor = fillBackgroundColor;
    }

    public ColorModel getFillForegroundColor() {
        return fillForegroundColor;
    }
    public void setFillForegroundColor(ColorModel fillForegroundColor) {
        this.fillForegroundColor = fillForegroundColor;
    }

    public short getFillPattern() {
        return fillPattern;
    }
    public void setFillPattern(short fillPattern) {
        this.fillPattern = fillPattern;
    }

    public FontModel getFont() {
        return font;
    }
    public void setFont(FontModel font) {
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

    public ColorModel getLeftBorderColor() {
        return leftBorderColor;
    }
    public void setLeftBorderColor(ColorModel leftBorderColor) {
        this.leftBorderColor = leftBorderColor;
    }

    public boolean isLocked() {
        return locked;
    }
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public ColorModel getRightBorderColor() {
        return rightBorderColor;
    }
    public void setRightBorderColor(ColorModel rightBorderColor) {
        this.rightBorderColor = rightBorderColor;
    }

    public short getRotation() {
        return rotation;
    }
    public void setRotation(short rotation) {
        this.rotation = rotation;
    }

    public ColorModel getTopBorderColor() {
        return topBorderColor;
    }
    public void setTopBorderColor(ColorModel topBorderColor) {
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
        final CellStyleModel result = (CellStyleModel)super.clone();
        if (bottomBorderColor!=null)
            result.bottomBorderColor = (ColorModel)bottomBorderColor.clone();
        if (fillBackgroundColor!=null)
            result.fillBackgroundColor = (ColorModel)fillBackgroundColor.clone();
        if (leftBorderColor!=null)
            result.leftBorderColor = (ColorModel)leftBorderColor.clone();
        if (rightBorderColor!=null)
            result.rightBorderColor = (ColorModel)rightBorderColor.clone();
        if (topBorderColor!=null)
            result.topBorderColor = (ColorModel)topBorderColor.clone();
        if (font!=null)
            result.font = (FontModel)font.clone();
        return result;
    }

    public int hashCode() {
        return id;
    }
    public boolean equals(Object obj) {
        if (obj==null || !(CellStyleModel.class.equals(obj.getClass())))
            return false;
        final CellStyleModel other = (CellStyleModel)obj;
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
