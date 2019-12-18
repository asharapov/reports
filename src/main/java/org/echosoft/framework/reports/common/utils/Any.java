package org.echosoft.framework.reports.common.utils;

import org.echosoft.framework.reports.common.types.Type;
import org.echosoft.framework.reports.common.types.Types;

/**
 * Данный класс содержит методы для быстрого преобразования строк в ряд общеупотребительных классов.
 *
 * @author Anton Sharapov
 */
public class Any {

    public static <T> T as(final Type<T> type, final String str) throws ClassCastException {
        try {
            return type.decode(str);
        } catch (Exception e) {
            final ClassCastException ee = new ClassCastException(e.getMessage());
            ee.initCause(e);
            throw ee;
        }
    }

    public static <T> T as(final Type<T> type, final String str, final T defaultValue) {
        try {
            final T result = type.decode(str);
            return result != null ? result : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static char asChar(final String str) throws ClassCastException {
        return as(Types.CHARACTER, str);
    }
    public static char asChar(final String str, final char defaultValue) {
        return as(Types.CHARACTER, str, defaultValue);
    }

    public static boolean asBoolean(final String str) throws ClassCastException {
        return as(Types.BOOLEAN, str);
    }
    public static boolean asBoolean(final String str, final boolean defaultValue) {
        return as(Types.BOOLEAN, str, defaultValue);
    }

    public static byte asByte(final String str) throws ClassCastException {
        return as(Types.BYTE, str);
    }
    public static byte asByte(final String str, final byte defaultValue) {
        return as(Types.BYTE, str, defaultValue);
    }

    public static short asShort(final String str) throws ClassCastException {
        return as(Types.SHORT, str);
    }
    public static short asShort(final String str, final short defaultValue) {
        return as(Types.SHORT, str, defaultValue);
    }

    public static int asInt(final String str) throws ClassCastException {
        return as(Types.INTEGER, str);
    }
    public static int asInt(final String str, final int defaultValue) {
        return as(Types.INTEGER, str, defaultValue);
    }

    public static long asLong(final String str) throws ClassCastException {
        return as(Types.LONG, str);
    }
    public static long asLong(final String str, final long defaultValue) {
        return as(Types.LONG, str, defaultValue);
    }

    public static float asFloat(final String str) throws ClassCastException {
        return as(Types.FLOAT, str);
    }
    public static float asFloat(final String str, final float defaultValue) {
        return as(Types.FLOAT, str, defaultValue);
    }

    public static double asDouble(final String str) throws ClassCastException {
        return as(Types.DOUBLE, str);
    }
    public static double asDouble(final String str, final double defaultValue) {
        return as(Types.DOUBLE, str, defaultValue);
    }

    public static String[] asStringArray(final String str) throws ClassCastException {
        return as(Types.STRING_ARRAY, str);
    }
    public static String[] asStringArray(final String str, final String[] defaultValue) {
        return as(Types.STRING_ARRAY, str, defaultValue);
    }

    public static Integer[] asIntegerArray(final String str) throws ClassCastException {
        return as(Types.INTEGER_ARRAY, str);
    }
    public static Integer[] asIntegerArray(final String str, final Integer[] defaultValue) {
        return as(Types.INTEGER_ARRAY, str, defaultValue);
    }

    public static Long[] asLongArray(final String str) throws ClassCastException {
        return as(Types.LONG_ARRAY, str);
    }
    public static Long[] asLongArray(final String str, final Long[] defaultValue) {
        return as(Types.LONG_ARRAY, str, defaultValue);
    }
}
