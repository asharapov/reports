package org.echosoft.framework.reports.model;

import java.io.Serializable;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

/**
 * Описывает стиль ячейки в отчете.
 *
 * @author Anton Sharapov
 */
public class CellStyleModel implements Serializable, Cloneable {

    private short id;
    private HorizontalAlignment alignment;
    private VerticalAlignment verticalAlignment;
    private String dataFormat;
    private boolean hidden;
    private short indention;
    private boolean locked;
    private short rotation;
    private boolean wrapText;
    private BorderStyle borderTop;
    private BorderStyle borderRight;
    private BorderStyle borderBottom;
    private BorderStyle borderLeft;
    private ColorModel topBorderColor;
    private ColorModel rightBorderColor;
    private ColorModel bottomBorderColor;
    private ColorModel leftBorderColor;
    private FillPatternType fillPattern;
    private ColorModel fillForegroundColor;
    private ColorModel fillBackgroundColor;
    private FontModel font;

    public CellStyleModel() {
        alignment = HorizontalAlignment.GENERAL;
        verticalAlignment = VerticalAlignment.TOP;
        borderTop = BorderStyle.NONE;
        borderRight = BorderStyle.NONE;
        borderBottom = BorderStyle.NONE;
        borderLeft = BorderStyle.NONE;
        fillPattern = FillPatternType.NO_FILL;
    }

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

    public HorizontalAlignment getAlignment() {
        return alignment;
    }
    public void setAlignment(final HorizontalAlignment alignment) {
        this.alignment = alignment != null ? alignment : HorizontalAlignment.GENERAL;
    }

    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }
    public void setVerticalAlignment(final VerticalAlignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment != null ? verticalAlignment : VerticalAlignment.TOP;
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


    public BorderStyle getBorderTop() {
        return borderTop;
    }
    public void setBorderTop(final BorderStyle borderTop) {
        this.borderTop = borderTop != null ? borderTop : BorderStyle.NONE;
    }

    public BorderStyle getBorderRight() {
        return borderRight;
    }
    public void setBorderRight(final BorderStyle borderRight) {
        this.borderRight = borderRight != null ? borderRight : BorderStyle.NONE;
    }

    public BorderStyle getBorderBottom() {
        return borderBottom;
    }
    public void setBorderBottom(final BorderStyle borderBottom) {
        this.borderBottom = borderBottom != null ? borderBottom : BorderStyle.NONE;
    }

    public BorderStyle getBorderLeft() {
        return borderLeft;
    }
    public void setBorderLeft(final BorderStyle borderLeft) {
        this.borderLeft = borderLeft != null ? borderLeft : BorderStyle.NONE;
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

    public FillPatternType getFillPattern() {
        return fillPattern;
    }
    public void setFillPattern(final FillPatternType fillPattern) {
        this.fillPattern = fillPattern != null ? fillPattern : FillPatternType.NO_FILL;
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
            case LEFT:
                buf.append(", align:left");
                break;
            case CENTER:
                buf.append(", align:center");
                break;
            case RIGHT:
                buf.append(", align:right");
                break;
            case FILL:
                buf.append(", align:fill");
                break;
            case JUSTIFY:
                buf.append(", align:justify");
                break;
            case CENTER_SELECTION:
                buf.append(", align:center-sel");
                break;
        }
        switch (verticalAlignment) {
            case TOP:
                buf.append(", valign:top");
                break;
            case CENTER:
                buf.append(", valign:center");
                break;
            case BOTTOM:
                buf.append(", valign:bottom");
                break;
            case JUSTIFY:
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
        if (borderTop != BorderStyle.NONE || borderRight != BorderStyle.NONE || borderBottom != BorderStyle.NONE || borderLeft != BorderStyle.NONE) {
            final int tc = getColorHash(topBorderColor);
            final int rc = getColorHash(rightBorderColor);
            final int bc = getColorHash(bottomBorderColor);
            final int lc = getColorHash(leftBorderColor);
            if (borderTop == borderRight && borderTop == borderBottom && borderTop == borderLeft && tc == rc && tc == bc && tc == lc) {
                buf.append(", border:").append(getBorderPattern(borderTop)).append(' ').append(tc >= 0 ? Integer.toHexString(tc) : "auto");
            } else {
                if (borderTop != BorderStyle.NONE || tc >= 0)
                    buf.append(", border-top:").append(getBorderPattern(borderTop)).append(' ').append(tc >= 0 ? Integer.toHexString(tc) : "auto");
                if (borderRight != BorderStyle.NONE || rc >= 0)
                    buf.append(", border-right:").append(getBorderPattern(borderRight)).append(' ').append(rc >= 0 ? Integer.toHexString(rc) : "auto");
                if (borderBottom != BorderStyle.NONE || bc >= 0)
                    buf.append(", border-bottom:").append(getBorderPattern(borderBottom)).append(' ').append(bc >= 0 ? Integer.toHexString(bc) : "auto");
                if (borderLeft != BorderStyle.NONE || lc >= 0)
                    buf.append(", border-left:").append(getBorderPattern(borderLeft)).append(' ').append(lc >= 0 ? Integer.toHexString(lc) : "auto");
            }
        }
        if (fillForegroundColor != null || fillBackgroundColor != null || fillPattern != FillPatternType.NO_FILL) {
            buf.append(", bgcolor:");
            switch (fillPattern) {
                case NO_FILL:
                    buf.append("none");
                    break;
                case SOLID_FOREGROUND:
                    buf.append("solid");
                    break;
                default:
                    buf.append("?something?");  // может быть когда-нибудь мне будет не лень выписать все возможные значения этого параметра ...
                    break;
            }
            buf.append(' ').append(fillForegroundColor != null ? fillForegroundColor.toHexString() : "auto");
            if (fillPattern != FillPatternType.NO_FILL && fillPattern != FillPatternType.SOLID_FOREGROUND && fillBackgroundColor != null)
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

    private static String getBorderPattern(final BorderStyle pattern) {
        if (pattern == null)
            return "none";
        switch (pattern) {
            case NONE:
                return "none";
            case THIN:
                return "thin";
            case MEDIUM:
                return "medium";
            case DASHED:
                return "dashed";
            case DOTTED:
                return "dotted";
            case THICK:
                return "thick";
            case DOUBLE:
                return "double";
            case HAIR :
                return "hair";
            case MEDIUM_DASHED:
                return "medium_dashed";
            case DASH_DOT:
                return "dash_dot";
            case MEDIUM_DASH_DOT:
                return "medium_dash_dot";
            case DASH_DOT_DOT:
                return "dash_dot_dot";
            case MEDIUM_DASH_DOT_DOT:
                return "medium_dash_dot_dot";
            case SLANTED_DASH_DOT:
                return "slanted_dash_dot";
            default:
                return "none";
        }
    }
}
