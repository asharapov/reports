package org.echosoft.framework.reports.model;

import java.io.Serializable;

/**
 * Описывает используемые в документе excel цвета.
 *
 * @author Anton Sharapov
 */
public class ColorModel implements Serializable, Cloneable {

    private static final char[] HEXDIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static int getHash(final byte[] rgb) {
        assert rgb != null && rgb.length == 3;
        return ((rgb[0] & 0xFF) << 16) + ((rgb[1] & 0xFF) << 8) + (rgb[2] & 0xFF);
    }

    private final short id;
    private final byte red;
    private final byte green;
    private final byte blue;

    public ColorModel(final short id, final short[] rgb) {
        this.id = id;
        this.red = (byte) rgb[0];
        this.green = (byte) rgb[1];
        this.blue = (byte) rgb[2];
    }

    public ColorModel(final short id, final byte[] rgb) {
        this.id = id;
        this.red = rgb[0];
        this.green = rgb[1];
        this.blue = rgb[2];
    }

    /**
     * Возвращает идентификатор цвета в шаблоне отчета.
     * Каждому цвету который используется в отчете соответствует свой идентификатор.
     *
     * @return идентификатор цвета в шаблоне отчета.
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

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !(ColorModel.class.equals(obj.getClass())))
            return false;
        final ColorModel other = (ColorModel) obj;
        return id == other.id && red == other.red && green == other.green && blue == other.blue;
    }

    @Override
    public String toString() {
        final StringBuilder out = new StringBuilder(32);
        out.append("[Color{id:");
        out.append(id);
        out.append(", rgb:#");
        out.append(toHexString());
        out.append("}]");
        return out.toString();
    }
}
