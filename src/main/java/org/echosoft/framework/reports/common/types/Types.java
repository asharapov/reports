package org.echosoft.framework.reports.common.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.echosoft.framework.reports.common.utils.StringUtil;

/**
 * Содержит ссылки на объекты, реализации интерфейса {@link Type} для базового набора классов.
 *
 * @author Anton Sharapov
 */
public class Types {

    private Types() {
    }

    public static final Type<String> STRING =
            new Type<String>() {
                public Class<String> getTarget() {
                    return String.class;
                }
                public boolean instanceOf(final Object obj) {
                    return String.class.isInstance(obj);
                }
                public String encode(final String value) {
                    return value;
                }
                public String decode(final String str) {
                    return str;
                }
                public String toString() {
                    return "[Type<String>]";
                }
            };

    public static final Type<Character> CHARACTER =
            new Type<Character>() {
                public Class<Character> getTarget() {
                    return Character.class;
                }
                public boolean instanceOf(final Object obj) {
                    return Character.class.isInstance(obj);
                }
                public String encode(final Character value) {
                    return value != null ? value.toString() : null;
                }
                public Character decode(final String str) throws ClassCastException {
                    if (str == null)
                        return null;
                    switch (str.length()) {
                        case 0:
                            return null;
                        case 1:
                            return str.charAt(0);
                        default:
                            throw new ClassCastException("Unable to cast \"" + str + "\" to Character");
                    }
                }
                public String toString() {
                    return "[Type<Character>]";
                }
            };

    public static final Type<Boolean> BOOLEAN =
            new Type<Boolean>() {
                public Class<Boolean> getTarget() {
                    return Boolean.class;
                }
                public boolean instanceOf(final Object obj) {
                    return Boolean.class.isInstance(obj);
                }
                public String encode(final Boolean value) {
                    return value != null ? value.toString() : null;
                }
                public Boolean decode(final String str) {
                    if (str == null || str.isEmpty())
                        return null;
                    return "true".equals(str.toLowerCase());
                }
                public String toString() {
                    return "[Type<Boolean>]";
                }
            };

    public static final Type<Byte> BYTE =
            new Type<Byte>() {
                public Class<Byte> getTarget() {
                    return Byte.class;
                }
                public boolean instanceOf(final Object obj) {
                    return Integer.class.isInstance(obj);
                }
                public String encode(final Byte value) {
                    return value != null ? Integer.toString(value, 10) : null;
                }
                public Byte decode(final String str) throws NumberFormatException {
                    if (str == null || str.isEmpty())
                        return null;
                    return Byte.parseByte(str, 10);
                }
                public String toString() {
                    return "[Type<Byte>]";
                }
            };

    public static final Type<Short> SHORT =
            new Type<Short>() {
                public Class<Short> getTarget() {
                    return Short.class;
                }
                public boolean instanceOf(final Object obj) {
                    return Short.class.isInstance(obj);
                }
                public String encode(final Short value) {
                    return value != null ? Integer.toString(value, 10) : null;
                }
                public Short decode(final String str) throws NumberFormatException {
                    if (str == null || str.isEmpty())
                        return null;
                    return Short.parseShort(str, 10);
                }
                public String toString() {
                    return "[Type<Short>]";
                }
            };

    public static final Type<Integer> INTEGER =
            new Type<Integer>() {
                public Class<Integer> getTarget() {
                    return Integer.class;
                }
                public boolean instanceOf(final Object obj) {
                    return Integer.class.isInstance(obj);
                }
                public String encode(final Integer value) {
                    return value != null ? Integer.toString(value, 10) : null;
                }
                public Integer decode(final String str) throws NumberFormatException {
                    if (str == null || str.isEmpty())
                        return null;
                    return Integer.parseInt(str, 10);
                }
                public String toString() {
                    return "[Type<Integer>]";
                }
            };

    public static final Type<Long> LONG =
            new Type<Long>() {
                public Class<Long> getTarget() {
                    return Long.class;
                }
                public boolean instanceOf(final Object obj) {
                    return Long.class.isInstance(obj);
                }
                public String encode(final Long value) {
                    return value != null ? Long.toString(value, 10) : null;
                }
                public Long decode(final String str) throws NumberFormatException {
                    if (str == null || str.isEmpty())
                        return null;
                    return Long.parseLong(str, 10);
                }
                public String toString() {
                    return "[Type<Long>]";
                }
            };

    public static final Type<Float> FLOAT =
            new Type<Float>() {
                public Class<Float> getTarget() {
                    return Float.class;
                }
                public boolean instanceOf(final Object obj) {
                    return Float.class.isInstance(obj);
                }
                public String encode(final Float value) {
                    return value != null ? Float.toString(value) : null;
                }
                public Float decode(final String str) throws NumberFormatException {
                    if (str == null || str.isEmpty())
                        return null;
                    return Float.parseFloat(str);
                }
                public String toString() {
                    return "[Type<Float>]";
                }
            };

    public static final Type<Double> DOUBLE =
            new Type<Double>() {
                public Class<Double> getTarget() {
                    return Double.class;
                }
                public boolean instanceOf(final Object obj) {
                    return Double.class.isInstance(obj);
                }
                public String encode(final Double value) {
                    return value != null ? Double.toString(value) : null;
                }
                public Double decode(final String str) throws NumberFormatException {
                    if (str == null || str.isEmpty())
                        return null;
                    return Double.parseDouble(str);
                }
                public String toString() {
                    return "[Type<Double>]";
                }
            };

    public static final Type<BigInteger> BIGINTEGER =
            new Type<BigInteger>() {
                public Class<BigInteger> getTarget() {
                    return BigInteger.class;
                }
                public boolean instanceOf(final Object obj) {
                    return BigInteger.class.isInstance(obj);
                }
                public String encode(final BigInteger value) {
                    return value != null ? value.toString(10) : null;
                }
                public BigInteger decode(final String str) throws NumberFormatException {
                    if (str == null || str.isEmpty())
                        return null;
                    return new BigInteger(str, 10);
                }
                public String toString() {
                    return "[Type<BigInteger>]";
                }
            };

    public static final Type<BigDecimal> BIGDECIMAL =
            new Type<BigDecimal>() {
                public Class<BigDecimal> getTarget() {
                    return BigDecimal.class;
                }
                public boolean instanceOf(final Object obj) {
                    return BigDecimal.class.isInstance(obj);
                }
                public String encode(final BigDecimal value) {
                    return value != null ? value.toPlainString() : null;
                }
                public BigDecimal decode(final String str) throws NumberFormatException {
                    if (str == null || str.isEmpty())
                        return null;
                    return new BigDecimal(str);
                }
                public String toString() {
                    return "[Type<BigDecimal>]";
                }
            };

    public static final Type<Date> DATE =
            new Type<Date>() {
                public Class<Date> getTarget() {
                    return Date.class;
                }
                public boolean instanceOf(final Object obj) {
                    return Date.class.isInstance(obj);
                }
                public String encode(final Date value) {
                    return value != null ? StringUtil.formatDate(value) : null;
                }
                public Date decode(final String str) throws ParseException {
                    return StringUtil.parseDate(str);
                }
                public String toString() {
                    return "[Type<Date>]";
                }
            };

    public static final Type<Date> DATETIME =
            new Type<Date>() {
                public Class<Date> getTarget() {
                    return Date.class;
                }
                public boolean instanceOf(final Object obj) {
                    return Date.class.isInstance(obj);
                }
                public String encode(final Date value) {
                    return value != null ? StringUtil.formatDateTime(value) : null;
                }
                public Date decode(final String str) throws ParseException {
                    return StringUtil.parseDateTime(str);
                }
                public String toString() {
                    return "[Type<DateTime>]";
                }
            };

    public static final Type<Date> DATETIME2 =
            new Type<Date>() {
                public Class<Date> getTarget() {
                    return Date.class;
                }
                public boolean instanceOf(final Object obj) {
                    return Date.class.isInstance(obj);
                }
                public String encode(final Date value) {
                    return value != null ? StringUtil.formatDateTime2(value) : null;
                }
                public Date decode(final String str) throws ParseException {
                    return StringUtil.parseDateTime2(str);
                }
                public String toString() {
                    return "[Type<DateTime2>]";
                }
            };

    public static final Type<String[]> STRING_ARRAY =
            new Type<String[]>() {
                public Class<String[]> getTarget() {
                    return String[].class;
                }
                public boolean instanceOf(final Object obj) {
                    return String[].class.isInstance(obj);
                }
                public String encode(final String[] value) {
                    return StringUtil.join('$', ',', value);
                }
                public String[] decode(final String str) {
                    final List<String> parts = StringUtil.split(str, ',', '$');
                    return parts != null ? parts.toArray(new String[parts.size()]) : null;
                }
                public String toString() {
                    return "[Type<String...>]";
                }
            };

    public static final Type<Integer[]> INTEGER_ARRAY =
            new Type<Integer[]>() {
                final Integer[] EMPTY_ARRAY = new Integer[0];
                public Class<Integer[]> getTarget() {
                    return Integer[].class;
                }
                public boolean instanceOf(final Object obj) {
                    return Integer[].class.isInstance(obj);
                }
                public String encode(final Integer[] value) {
                    if (value == null)
                        return null;
                    final StringBuilder buf = new StringBuilder(32);
                    for (int i = 0, len = value.length; i < len; i++) {
                        if (i > 0)
                            buf.append(',');
                        final Integer item = value[i];
                        if (item != null)
                            buf.append(Integer.toString(item, 10));
                    }
                    return buf.toString();
                }
                public Integer[] decode(final String str) throws NumberFormatException {
                    if (str == null)
                        return null;
                    if (str.isEmpty())
                        return EMPTY_ARRAY;
                    final ArrayList<Integer> result = new ArrayList<>();
                    for (StringTokenizer it = new StringTokenizer(str, "\t\r\n ,;", false); it.hasMoreTokens(); ) {
                        final String token = it.nextToken();
                        result.add(token != null ? Integer.parseInt(token, 10) : null);
                    }
                    return result.toArray(new Integer[result.size()]);
                }
                public String toString() {
                    return "[Type<Integer...>]";
                }
            };

    public static final Type<Long[]> LONG_ARRAY =
            new Type<Long[]>() {
                final Long[] EMPTY_ARRAY = new Long[0];
                public Class<Long[]> getTarget() {
                    return Long[].class;
                }
                public boolean instanceOf(final Object obj) {
                    return Long[].class.isInstance(obj);
                }
                public String encode(final Long[] value) {
                    if (value == null)
                        return null;
                    final StringBuilder buf = new StringBuilder(32);
                    for (int i = 0, len = value.length; i < len; i++) {
                        if (i > 0)
                            buf.append(',');
                        final Long item = value[i];
                        if (item != null)
                            buf.append(Long.toString(item, 10));
                    }
                    return buf.toString();
                }
                public Long[] decode(final String str) throws NumberFormatException {
                    if (str == null)
                        return null;
                    if (str.isEmpty())
                        return EMPTY_ARRAY;
                    final ArrayList<Long> result = new ArrayList<>();
                    for (StringTokenizer it = new StringTokenizer(str, "\t\r\n ,;"); it.hasMoreTokens(); ) {
                        final String token = it.nextToken();
                        result.add(token != null ? Long.parseLong(token, 10) : null);
                    }
                    return result.toArray(new Long[result.size()]);
                }
                public String toString() {
                    return "[Type<Long...>]";
                }
            };
}
