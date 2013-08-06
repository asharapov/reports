package org.echosoft.framework.reports.model;

import java.io.Serializable;

import org.apache.poi.ss.usermodel.Font;

/**
 * Описывает шрифт в отчете.
 *
 * @author Anton Sharapov
 */
public class FontModel implements Serializable, Cloneable {

    private short id;
    private String fontName;
    private short fontHeight;
    private int charSet;
    private short boldWeight;
    private boolean italic;
    private boolean strikeout;
    private short typeOffset;
    private byte underline;
    private ColorModel color;

    public FontModel() {
        fontName = "Arial";
    }

    /**
     * Возвращает идентификатор шрифта в шаблоне отчета.
     * Каждому шрифту что используется в отчете соответствует свой идентификатор.
     *
     * @return идентификатор шрифта в шаблоне отчета.
     */
    public short getId() {
        return id;
    }
    public void setId(final short id) {
        this.id = id;
    }

    public short getBoldWeight() {
        return boldWeight;
    }
    public void setBoldWeight(final short boldWeight) {
        this.boldWeight = boldWeight;
    }

    public int getCharSet() {
        return charSet;
    }
    public void setCharSet(final int charSet) {
        this.charSet = charSet;
    }

    public ColorModel getColor() {
        return color;
    }
    public void setColor(final ColorModel color) {
        this.color = color;
    }

    public short getFontHeight() {
        return fontHeight;
    }
    public void setFontHeight(final short fontHeight) {
        this.fontHeight = fontHeight;
    }

    public String getFontName() {
        return fontName;
    }
    public void setFontName(final String fontName) {
        if (fontName == null)
            throw new RuntimeException("font name must be specified");
        this.fontName = fontName;
    }

    public boolean isItalic() {
        return italic;
    }
    public void setItalic(final boolean italic) {
        this.italic = italic;
    }

    public boolean isStrikeout() {
        return strikeout;
    }
    public void setStrikeout(final boolean strikeout) {
        this.strikeout = strikeout;
    }

    public short getTypeOffset() {
        return typeOffset;
    }
    public void setTypeOffset(final short typeOffset) {
        this.typeOffset = typeOffset;
    }

    public byte getUnderline() {
        return underline;
    }
    public void setUnderline(final byte underline) {
        this.underline = underline;
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        final FontModel result = (FontModel) super.clone();
        if (color != null)
            result.color = (ColorModel) color.clone();
        return result;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !(FontModel.class.equals(obj.getClass())))
            return false;
        final FontModel other = (FontModel) obj;
        return id == other.id &&
                (color != null ? color.equals(other.color) : other.color == null) &&
                fontName.equals(other.fontName) &&
                fontHeight == other.fontHeight &&
                charSet == other.charSet &&
                boldWeight == other.boldWeight && italic == other.italic &&
                strikeout == other.strikeout && typeOffset == other.typeOffset && underline == other.underline;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(64);
        buf.append("[Font{id:").append(id).append(", name:").append(fontName).append(' ').append(fontHeight/20).append("pt");
        if (boldWeight == Font.BOLDWEIGHT_BOLD)
            buf.append(", bold");
        if (italic)
            buf.append(", italic");
        if (strikeout)
            buf.append(", strikeout");
        switch (underline) {
            case 1:
                buf.append(", underline: single");
                break;
            case 2:
                buf.append(", underline: double");
                break;
            case 3:
                buf.append(", underline: single_acc");
                break;
            case 4:
                buf.append(", underline: double_acc");
                break;
            default:
        }
        switch (typeOffset) {
            case Font.SS_SUB:
                buf.append(", offset:sub");
                break;
            case Font.SS_SUPER:
                buf.append(", offset:super");
                break;
            default:
        }
        if (color != null)
            buf.append(", color:").append(color.toHexString());
        buf.append("}]");
        return buf.toString();
    }
}
