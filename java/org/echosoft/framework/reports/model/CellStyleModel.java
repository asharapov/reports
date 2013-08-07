package org.echosoft.framework.reports.model;

import java.io.Serializable;

import org.apache.poi.ss.usermodel.CellStyle;

/**
 * Описывает стиль ячейки в отчете.
 *
 * @author Anton Sharapov
 */
public class CellStyleModel implements Serializable, Cloneable {

    private short id;
    private short alignment;
    private short verticalAlignment;
    private String dataFormat;
    private boolean hidden;
    private short indention;
    private boolean locked;
    private short rotation;
    private boolean wrapText;
    private short borderTop;
    private short borderRight;
    private short borderBottom;
    private short borderLeft;
    private ColorModel topBorderColor;
    private ColorModel rightBorderColor;
    private ColorModel bottomBorderColor;
    private ColorModel leftBorderColor;
    private short fillPattern;
    private ColorModel fillForegroundColor;
    private ColorModel fillBackgroundColor;
    private FontModel font;

    /**
     * Возвращает идентификатор стиля ячейки в шаблоне отчета.
     * Каждому стилю что используется в отчете соответствует свой идентификатор.
     *
     * @return идентификатор стиля ячейки в шаблоне отчета.
     */
    public short getId() {
        return id;
    }
    public void setId(final short id) {
        this.id = id;
    }

    public short getAlignment() {
        return alignment;
    }
    public void setAlignment(final short alignment) {
        this.alignment = alignment;
    }

    public short getVerticalAlignment() {
        return verticalAlignment;
    }
    public void setVerticalAlignment(final short verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

    public String getDataFormat() {
        return dataFormat;
    }
    public void setDataFormat(final String dataFormat) {
        this.dataFormat = dataFormat;
    }

    public boolean isHidden() {
        return hidden;
    }
    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }

    public short getIndention() {
        return indention;
    }
    public void setIndention(final short indention) {
        this.indention = indention;
    }

    public boolean isLocked() {
        return locked;
    }
    public void setLocked(final boolean locked) {
        this.locked = locked;
    }

    public short getRotation() {
        return rotation;
    }
    public void setRotation(final short rotation) {
        this.rotation = rotation;
    }

    public boolean isWrapText() {
        return wrapText;
    }
    public void setWrapText(final boolean wrapText) {
        this.wrapText = wrapText;
    }


    public short getBorderTop() {
        return borderTop;
    }
    public void setBorderTop(final short borderTop) {
        this.borderTop = borderTop;
    }

    public short getBorderRight() {
        return borderRight;
    }
    public void setBorderRight(final short borderRight) {
        this.borderRight = borderRight;
    }

    public short getBorderBottom() {
        return borderBottom;
    }
    public void setBorderBottom(final short borderBottom) {
        this.borderBottom = borderBottom;
    }

    public short getBorderLeft() {
        return borderLeft;
    }
    public void setBorderLeft(final short borderLeft) {
        this.borderLeft = borderLeft;
    }


    public ColorModel getTopBorderColor() {
        return topBorderColor;
    }
    public void setTopBorderColor(final ColorModel topBorderColor) {
        this.topBorderColor = topBorderColor;
    }

    public ColorModel getRightBorderColor() {
        return rightBorderColor;
    }
    public void setRightBorderColor(final ColorModel rightBorderColor) {
        this.rightBorderColor = rightBorderColor;
    }

    public ColorModel getBottomBorderColor() {
        return bottomBorderColor;
    }
    public void setBottomBorderColor(final ColorModel bottomBorderColor) {
        this.bottomBorderColor = bottomBorderColor;
    }

    public ColorModel getLeftBorderColor() {
        return leftBorderColor;
    }
    public void setLeftBorderColor(final ColorModel leftBorderColor) {
        this.leftBorderColor = leftBorderColor;
    }

    public short getFillPattern() {
        return fillPattern;
    }
    public void setFillPattern(final short fillPattern) {
        this.fillPattern = fillPattern;
    }

    public ColorModel getFillForegroundColor() {
        return fillForegroundColor;
    }
    public void setFillForegroundColor(final ColorModel fillForegroundColor) {
        this.fillForegroundColor = fillForegroundColor;
    }

    public ColorModel getFillBackgroundColor() {
        return fillBackgroundColor;
    }
    public void setFillBackgroundColor(final ColorModel fillBackgroundColor) {
        this.fillBackgroundColor = fillBackgroundColor;
    }

    public FontModel getFont() {
        return font;
    }
    public void setFont(final FontModel font) {
        this.font = font;
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        final CellStyleModel result = (CellStyleModel) super.clone();
        if (topBorderColor != null)
            result.topBorderColor = (ColorModel) topBorderColor.clone();
        if (rightBorderColor != null)
            result.rightBorderColor = (ColorModel) rightBorderColor.clone();
        if (bottomBorderColor != null)
            result.bottomBorderColor = (ColorModel) bottomBorderColor.clone();
        if (leftBorderColor != null)
            result.leftBorderColor = (ColorModel) leftBorderColor.clone();
        if (fillForegroundColor != null)
            result.fillForegroundColor = (ColorModel) fillForegroundColor.clone();
        if (fillBackgroundColor != null)
            result.fillBackgroundColor = (ColorModel) fillBackgroundColor.clone();
        if (font != null)
            result.font = (FontModel) font.clone();
        return result;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !(CellStyleModel.class.equals(obj.getClass())))
            return false;
        final CellStyleModel other = (CellStyleModel) obj;
        return id == other.id &&
                alignment == other.alignment &&
                verticalAlignment == other.verticalAlignment &&
                (dataFormat != null ? dataFormat.equals(other.dataFormat) : other.dataFormat == null) &&
                hidden == other.hidden &&
                indention == other.indention &&
                locked == other.locked &&
                rotation == other.rotation &&
                wrapText == other.wrapText &&
                borderTop == other.borderTop &&
                borderRight == other.borderRight &&
                borderBottom == other.borderBottom &&
                borderLeft == other.borderLeft &&
                (topBorderColor != null ? topBorderColor.equals(other.topBorderColor) : other.topBorderColor == null) &&
                (rightBorderColor != null ? rightBorderColor.equals(other.rightBorderColor) : other.rightBorderColor == null) &&
                (bottomBorderColor != null ? bottomBorderColor.equals(other.bottomBorderColor) : other.bottomBorderColor == null) &&
                (leftBorderColor != null ? leftBorderColor.equals(other.leftBorderColor) : other.leftBorderColor == null) &&
                fillPattern == other.fillPattern &&
                (fillForegroundColor != null ? fillForegroundColor.equals(other.fillForegroundColor) : other.fillForegroundColor == null) &&
                (fillBackgroundColor != null ? fillBackgroundColor.equals(other.fillBackgroundColor) : other.fillBackgroundColor == null) &&
                (font != null ? font.equals(other.font) : other.font == null);
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(64);
        buf.append("[Style{id:").append(id);
        switch (alignment) {
            case CellStyle.ALIGN_LEFT:
                buf.append(", align:left");
                break;
            case CellStyle.ALIGN_CENTER:
                buf.append(", align:center");
                break;
            case CellStyle.ALIGN_RIGHT:
                buf.append(", align:right");
                break;
            case CellStyle.ALIGN_FILL:
                buf.append(", align:fill");
                break;
            case CellStyle.ALIGN_JUSTIFY:
                buf.append(", align:justify");
                break;
            case CellStyle.ALIGN_CENTER_SELECTION:
                buf.append(", align:center-sel");
                break;
        }
        switch (verticalAlignment) {
            case CellStyle.VERTICAL_TOP:
                buf.append(", valign:top");
                break;
            case CellStyle.VERTICAL_CENTER:
                buf.append(", valign:center");
                break;
            case CellStyle.VERTICAL_BOTTOM:
                buf.append(", valign:bottom");
                break;
            case CellStyle.VERTICAL_JUSTIFY:
                buf.append(", valign:justify");
                break;
        }
        if (dataFormat != null)
            buf.append(", format:").append(dataFormat);
        if (hidden)
            buf.append(", hidden");
        if (indention > 0)
            buf.append(", indent:").append(indention);
        if (locked)
            buf.append(", locked");
        if (rotation != 0)
            buf.append(", rot:").append(rotation);
        if (wrapText)
            buf.append(", wrap");
        if (borderTop != 0 || borderRight != 0 || borderBottom != 0 || borderLeft != 0) {
            final int tc = getColorHash(topBorderColor);
            final int rc = getColorHash(rightBorderColor);
            final int bc = getColorHash(bottomBorderColor);
            final int lc = getColorHash(leftBorderColor);
            if (borderTop == borderRight && borderTop == borderBottom && borderTop == borderLeft && tc == rc && tc == bc && tc == lc) {
                buf.append(", border:").append(getBorderPattern(borderTop)).append(' ').append(tc >= 0 ? Integer.toHexString(tc) : "auto");
            } else {
                if (borderTop > 0 || tc >= 0)
                    buf.append(", border-top:").append(getBorderPattern(borderTop)).append(' ').append(tc >= 0 ? Integer.toHexString(tc) : "auto");
                if (borderRight > 0 || rc >= 0)
                    buf.append(", border-right:").append(getBorderPattern(borderRight)).append(' ').append(rc >= 0 ? Integer.toHexString(rc) : "auto");
                if (borderBottom > 0 || bc >= 0)
                    buf.append(", border-bottom:").append(getBorderPattern(borderBottom)).append(' ').append(bc >= 0 ? Integer.toHexString(bc) : "auto");
                if (borderLeft > 0 || lc >= 0)
                    buf.append(", border-left:").append(getBorderPattern(borderLeft)).append(' ').append(lc >= 0 ? Integer.toHexString(lc) : "auto");
            }
        }
        if (fillForegroundColor != null || fillBackgroundColor != null || fillPattern > 0) {
            buf.append(", bgcolor:");
            switch (fillPattern) {
                case CellStyle.NO_FILL:
                    buf.append("none");
                    break;
                case CellStyle.SOLID_FOREGROUND:
                    buf.append("solid");
                    break;
                default:
                    buf.append("?something?");  // может быть когда-нибудь мне будет не лень выписать все возможные значения этого параметра ...
                    break;
            }
            buf.append(' ').append(fillForegroundColor != null ? fillForegroundColor.toHexString() : "auto");
            if (fillPattern != CellStyle.NO_FILL && fillPattern != CellStyle.SOLID_FOREGROUND && fillBackgroundColor != null)
                buf.append(" / ").append(fillBackgroundColor.toHexString());
        }
        if (font != null) {
            buf.append(", ").append(font);
        }

        buf.append("}]");
        return buf.toString();
    }

    private static int getColorHash(final ColorModel color) {
        return color == null ? -1 : color.getPackedValue();
    }
    private static String getBorderPattern(final short pattern) {
        switch (pattern) {
            case CellStyle.BORDER_THIN:
                return "thin";
            case CellStyle.BORDER_MEDIUM:
                return "medium";
            case CellStyle.BORDER_DASHED:
                return "dashed";
            case CellStyle.BORDER_DOTTED:
                return "dotted";
            case CellStyle.BORDER_THICK:
                return "thick";
            case CellStyle.BORDER_DOUBLE:
                return "double";
            case CellStyle.BORDER_HAIR :
                return "hair";
            case CellStyle.BORDER_MEDIUM_DASHED:
                return "medium_dashed";
            case CellStyle.BORDER_DASH_DOT:
                return "dash_dot";
            case CellStyle.BORDER_MEDIUM_DASH_DOT:
                return "medium_dash_dot";
            case CellStyle.BORDER_DASH_DOT_DOT:
                return "dash_dot_dot";
            case CellStyle.BORDER_MEDIUM_DASH_DOT_DOT:
                return "medium_dash_dot_dot";
            case CellStyle.BORDER_SLANTED_DASH_DOT:
                return "slanted_dash_dot";
            default:
                return "none";
        }
    }
}
