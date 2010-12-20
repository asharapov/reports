package org.echosoft.framework.reports.model;

import java.io.Serializable;

/**
 * Описывает шрифт в отчете.
 *
 * @author Anton Sharapov
 */
public class Font implements Serializable, Cloneable {

    private short id;
    private short boldWeight;
    private int charSet;
    private Color color;
    private short fontHeight;
    private String fontName;
    private boolean italic;
    private boolean strikeout;
    private short typeOffset;
    private byte underline;

    public Font() {
        fontName = "Arial";
    }

    /**
     * Возвращает идентификатор шрифта в шаблоне отчета.
     * Каждому шрифту что используется в отчете соответствует свой идентификатор.
     *
     * @return  идентификатор шрифта в шаблоне отчета.
     */
    public short getId() {
        return id;
    }
    public void setId(short id) {
        this.id = id;
    }

    public short getBoldWeight() {
        return boldWeight;
    }
    public void setBoldWeight(short boldWeight) {
        this.boldWeight = boldWeight;
    }

    public int getCharSet() {
        return charSet;
    }
    public void setCharSet(int charSet) {
        this.charSet = charSet;
    }

    public Color getColor() {
        return color;
    }
    public void setColor(Color color) {
        this.color = color;
    }

    public short getFontHeight() {
        return fontHeight;
    }
    public void setFontHeight(short fontHeight) {
        this.fontHeight = fontHeight;
    }

    public String getFontName() {
        return fontName;
    }
    public void setFontName(String fontName) {
        if (fontName==null)
            throw new RuntimeException("font name must be specified");
        this.fontName = fontName;
    }

    public boolean isItalic() {
        return italic;
    }
    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    public boolean isStrikeout() {
        return strikeout;
    }
    public void setStrikeout(boolean strikeout) {
        this.strikeout = strikeout;
    }

    public short getTypeOffset() {
        return typeOffset;
    }
    public void setTypeOffset(short typeOffset) {
        this.typeOffset = typeOffset;
    }

    public byte getUnderline() {
        return underline;
    }
    public void setUnderline(byte underline) {
        this.underline = underline;
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        final Font result = (Font)super.clone();
        if (color!=null)
            result.color = (Color)color.clone();
        return result;
    }

    public int hashCode() {
        return id;
    }
    public boolean equals(Object obj) {
        if (obj==null || !(Font.class.equals(obj.getClass())))
            return false;
        final Font other = (Font)obj;
        return id==other.id &&
                (color!=null ? color.equals(other.color) : other.color==null) &&
                boldWeight==other.boldWeight && charSet==other.charSet &&
                fontHeight==other.fontHeight && fontName.equals(other.fontName) && italic==other.italic &&
                strikeout==other.strikeout && typeOffset==other.typeOffset && underline==other.underline;
    }
    public String toString() {
        final StringBuilder out = new StringBuilder(128);
        out.append("[Font{id:").append(id).append(", font:").append(fontName).append(" ").append(fontHeight)
                .append(", color:").append(color != null ? color.toHexString() : "null")
                .append(", weight:").append(boldWeight).append(", italic:").append(italic).append(", strikeout:")
                .append(strikeout).append(", underline:").append(underline).append("}]");
        return out.toString();
    }

}
