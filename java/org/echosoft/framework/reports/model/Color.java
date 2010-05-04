package org.echosoft.framework.reports.model;

import java.io.Serializable;

/**
 * Описывает используемые в документе excel цвета.
 *
 * @author Anton Sharapov
 */
public class Color implements Serializable, Cloneable {

    private final short id;
    private final byte red;
    private final byte green;
    private final byte blue;

    public Color(short id, byte red, byte green, byte blue) {
        this.id = id;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    /**
     * Возвращает идентификатор цвета в шаблоне отчета.
     * Каждому цвету который используется в отчете соответствует свой идентификатор.
     *
     * @return  идентификатор цвета в шаблоне отчета.
     */
    public short getId() {
        return id;
    }


    public byte getRed() {
        return red;
    }

    public byte getGreen() {
        return green;
    }

    public byte getBlue() {
        return blue;
    }


    public String toHexString() {
        final char[] result = new char[6];
        result[0] = HEXDIGITS[(0xF0 & red) >>> 4];
        result[1] = HEXDIGITS[0x0F & red];
        result[2] = HEXDIGITS[(0xF0 & green) >>> 4];
        result[3] = HEXDIGITS[0x0F & green];
        result[4] = HEXDIGITS[(0xF0 & blue) >>> 4];
        result[5] = HEXDIGITS[0x0F & blue];
        return new String(result);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int hashCode() {
        return id;
    }
    public boolean equals(Object obj) {
        if (obj==null || !(Color.class.equals(obj.getClass())))
            return false;
        final Color other = (Color)obj;
        return id==other.id && red==other.red && green==other.green && blue==other.blue;
    }
    public String toString() {
        final StringBuilder out = new StringBuilder(32);
        out.append("[Color{id:");
        out.append(id);
        out.append(", rgb:#");
        out.append(toHexString());
        out.append("}]");
        return out.toString();
    }

    private static final char[] HEXDIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

}
