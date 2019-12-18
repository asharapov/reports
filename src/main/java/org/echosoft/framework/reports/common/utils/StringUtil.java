package org.echosoft.framework.reports.common.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.Function;


/**
 * Содержит часто используемые методы для работы со строками.
 *
 * @author Andrey Ochirov
 * @author Anton Sharapov.
 */
public class StringUtil {

    public static final String EMPTY_STRING_ARRAY[] = new String[0];
    private static final char[] HEXDIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final char[] GEN_PWD_CHARS1 = {'a', 'e', 'i', 'o', 'u', 'y'};
    private static final char[] GEN_PWD_CHARS2 = {'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p', 'q', 'r', 's', 't', 'x', 'z', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private static final char[][] REPLACEMENT_XML_TEXTS;
    private static final char[][] REPLACEMENT_XML_ATTRS;

    static {
        REPLACEMENT_XML_TEXTS = new char[128][];
        final char[] empty = new char[0];
        for (int i = 0; i < 32; i++) {
            REPLACEMENT_XML_TEXTS[i] = empty;
        }
        REPLACEMENT_XML_TEXTS['\t'] = null;
        REPLACEMENT_XML_TEXTS['<'] = "&lt;".toCharArray();
        REPLACEMENT_XML_TEXTS['>'] = "&gt;".toCharArray();
        REPLACEMENT_XML_TEXTS['&'] = "&amp;".toCharArray();
        REPLACEMENT_XML_ATTRS = REPLACEMENT_XML_TEXTS.clone();
        REPLACEMENT_XML_ATTRS['\"'] = "&quot;".toCharArray();
        REPLACEMENT_XML_ATTRS['\''] = "&apos;".toCharArray();
        REPLACEMENT_XML_TEXTS['\r'] = null;
        REPLACEMENT_XML_TEXTS['\n'] = null;
    }


    private StringUtil() {
    }

    /**
     * Преобразовывает указанный в аргументе java объект в строку. Если аргумент содержит <code>null</code> то метод возвращает пустую строку
     * (в этом его единственное отличие от стандартного метода {@link String#valueOf(Object)} который в подобном случае возвращает строку <code>"null"</code>.
     *
     * @param obj объект который должен быть преобразован в строку.
     * @return Строковая форма переданного в аргументе объекта или строка нулевой длины если в аргументе передан <code>null</code>.
     */
    public static String valueOf(final Object obj) {
        return obj != null ? obj.toString() : "";
    }

    /**
     * Возвращает копию переданной в аргументе строки без начальных и завершающих пробелов (а также всех прочих символов с кодом меньше  <code>'&#92;u0020'</code>).
     * Если полученная строка имеет нулевую длину то метод возвращает <code>null</code>.
     *
     * @param text строка в которой надо избавиться от начальных и завершающих пробелов.
     * @return копия переданной в аргументе строки без начальных и завершающих пробелов или <code>null</code>.
     */
    public static String trim(String text) {
        if (text == null)
            return null;
        text = text.trim();
        return !text.isEmpty() ? text : null;
    }

    /**
     * Возвращает первый аргумент если он не null и не пустая строка, в противном случае возвращает второй аргумент.
     *
     * @param text        строковое значение
     * @param defaultText строка которая будет возвращать в случае если первым аргументом идет <code>null</code> или пустая строка или строка состоящая из одних пробелов.
     * @return первый аргумент (без лидирующих пробелов) если он не равен <code>null</code> и не является пустой строкой, в противном случае возвращается второй аргумент.
     */
    public static String getNonEmpty(String text, final String defaultText) {
        return text == null || (text = text.trim()).isEmpty() ? defaultText : text;
    }

    /**
     * Сравнивает строки, каждая из которых может быть <code>null</code>.
     *
     * @param str1 первая строка для сравнения.
     * @param str2 вторая строка для сравнения.
     * @return <ul>
     * <li> <code>-1</code>  если первая строка меньше второй или только первая строка равна <code>null</code>.
     * <li> <code>0</code>  если обе строки одинаковы или обе равны <code>null</code>.
     * <li> <code>1</code>  если первая строка больше второй или только вторая строка равна <code>null</code>.
     * </ul>
     */
    public static int compareNullableStrings(final String str1, final String str2) {
        if (str1 == null) {
            return str2 == null ? 0 : -1;
        } else {
            return str2 == null ? 1 : str1.compareTo(str2);
        }
    }


    /**
     * Дополняет переданную в аргументе строку до требуемой длины путем добавления в начало строки указанных символов.
     * Если длина исходной строки равна или больше указанной длины то метод не делает ничего и возвращает исходную строку.
     *
     * @param str            исходная строка
     * @param symbol         символ который используется до заполнения строки до требуемой длины.
     * @param requiredLength минимальная требуемая длина возвращаемой методом строки.
     * @return строка требуемой длины.
     */
    public static String leadLeft(final String str, final char symbol, final int requiredLength) {
        final char[] buf;
        final int strlen;
        if (str == null || (strlen = str.length()) == 0) {
            buf = new char[requiredLength];
            for (int i = 0; i < requiredLength; i++) buf[i] = symbol;
            return new String(buf);
        } else {
            final int mustBeAdded = requiredLength - strlen;
            if (mustBeAdded <= 0)
                return str;
            buf = new char[requiredLength];
            for (int i = 0; i < mustBeAdded; i++) buf[i] = symbol;
            str.getChars(0, strlen, buf, mustBeAdded);
            return new String(buf);
        }
    }

    /**
     * Дополняет переданную в аргументе строку до требуемой длины путем добавления в конец строки указанных символов.
     * Если длина исходной строки равна или больше указанной длины то метод не делает ничего и возвращает исходную строку.
     *
     * @param str            исходная строка
     * @param symbol         символ который используется до заполнения строки до требуемой длины.
     * @param requiredLength минимальная требуемая длина возвращаемой методом строки.
     * @return строка требуемой длины.
     */
    public static String leadRight(final String str, final char symbol, final int requiredLength) {
        final char[] buf;
        final int strlen;
        if (str == null || (strlen = str.length()) == 0) {
            buf = new char[requiredLength];
            for (int i = 0; i < requiredLength; i++) buf[i] = symbol;
            return new String(buf);
        } else {
            final int mustBeAdded = requiredLength - strlen;
            if (mustBeAdded <= 0)
                return str;
            buf = new char[requiredLength];
            str.getChars(0, strlen, buf, 0);
            for (int i = strlen; i < requiredLength; i++) buf[i] = symbol;
            return new String(buf);
        }
    }

    /**
     * В указанной строке начиная с определенной позиции ищет первое вхождение одного из перечисленных символов.
     *
     * @param string   строка в которой требуется найти первое вхождение одного из требуемых символов.
     * @param startPos неотрицательное число, определяет с какой позиции следует начинать поиск.
     * @param chars    непустой массив символов. Метод ищет первое вхождение одного из них.
     * @return позиция по которой находится один из перечисленных в аргументе символов или -1 если ни один символ в строке не найден.
     */
    public static int indexOf(final CharSequence string, final int startPos, final char... chars) {
        for (int i = startPos, len = string.length(); i < len; i++) {
            final char c = string.charAt(i);
            for (int j = chars.length - 1; j >= 0; j--) {
                if (c == chars[j])
                    return i;
            }
        }
        return -1;
    }

    /**
     * Возвращает начало строки переданной в аргументе до позиции первого вхождения в нее символа-разделителя (исключая его).
     *
     * @param text      исходная строка. Должна быть указана обязательно.
     * @param delimiter символ-разделитель.
     * @return Начало исходной строки до разделителя.
     * Если в исходной строке символ-разделитель отсутствует, то возвращается вся строка целиком.
     * Если в исходной строке символ-разделитель стоит первым символом то метод возвращает пустую строку.
     */
    public static String getHead(final String text, final char delimiter) {
        if (text == null)
            return null;
        final int p = text.indexOf(delimiter);
        return p >= 0 ? text.substring(0, p) : text;
    }

    /**
     * Возвращает окончание строки переданной в аргументе начиная с позиции, следующей за первым вхождением символа-разделителя.
     *
     * @param text      исходная строка. Должна быть указана обязательно.
     * @param delimiter символ-разделитель.
     * @return Окончание исходной строки начиная с позиции следующей за первым вхождением символа-разделителя.
     * Если в исходной строке символ-разделитель отсутствует, то возвращается <code>null</code>.
     * Если в исходной строке единственное вхождение символа-разделителя стоит последним символом то метод возвращает пустую строку.
     */
    public static String getTail(final String text, final char delimiter) {
        if (text == null)
            return null;
        final int p = text.indexOf(delimiter);
        return p >= 0 ? text.substring(p + 1) : null;
    }

    /**
     * Удаляет избыточные повторяющиеся пробелы в тексте а также заменяет все символы с кодом меньшим 0x20 на символ 0x20 (пробел).
     *
     * @param text исходный текст
     * @return текст без лишних пробелов и прочих спец. символов.
     */
    public static String skipRedundantSpaces(final String text) {
        final int len0;
        if (text == null || (len0 = text.length()) == 0)
            return null;
        final StringBuilder buf = new StringBuilder(len0);
        char cc = ' ';
        for (int i = 0; i < len0; i++) {
            char c = text.charAt(i);
            if (c <= ' ') {
                if (cc == ' ')
                    continue;
                c = ' ';
            }
            buf.append(c);
            cc = c;
        }
        final int len1 = buf.length();
        if (len1 == 0)
            return null;
        return cc != ' ' ? buf.toString() : buf.substring(0, len1 - 1);
    }

    /**
     * Заменяет в строке все вхождения одной указанной подстроки на другую подстроку.
     *
     * @param text    текст в котором должна быть выполнена замена одной подстроки на другую. Не может быть <code>null</code>.
     * @param pattern подстрока которая должна быть заменена на другую. Не может быть <code>null</code>.
     * @param value   подстрока на которую должна быть заменена исходная подстрока.
     * @return результат замены одной подстроки на другую подстроку.
     */
    public static String replace(final String text, final String pattern, final String value) {
        if (text == null || pattern == null || value == null)
            return null;

        final int textSize = text.length();
        final int patternSize = pattern.length();
        int oldPos = 0;
        final StringBuilder result = new StringBuilder(textSize);
        for (int pos = text.indexOf(pattern, 0); pos >= 0; pos = text.indexOf(pattern, oldPos)) {
            result.append(text.substring(oldPos, pos));
            result.append(value);
            oldPos = pos + patternSize;
        }
        result.append(text.substring(oldPos, textSize));
        return result.toString();
    }

    /**
     * Заменяет все вхождения указанных в атрибуте <code>attrs</code> выражений на соответствующие им значения.
     *
     * @param text         строка в которой требуется провести серию замен. Если аргумент равен <code>null</code> то метод завершает работу возвращая <code>null</code>.
     * @param attrs        перечень шаблонов в строке и соответствующих им реальным значениям на которые эти шаблоны должны быть заменены..
     * @param prefix       специальный маркерный символ предшествующий началу очередного шаблона для замены. Не может быть пустым.
     * @param suffix       специальный маркерный символ завершающий текст шаблона для замены. Не может быть пустым или быть идентичным префиксу.
     * @param defaultValue значение по умолчанию используемое если требуемый в строке атрибут отсутствует в мапе или равен <code>null</code>
     * @return Итоговая строка, в которой все шаблоны заменены соответствующими значениями.
     */
    public static String replace(final String text, Map<String, ? extends CharSequence> attrs, final char prefix, final char suffix, final String defaultValue) {
        if (text == null)
            return null;
        if (attrs == null)
            attrs = Collections.emptyMap();
        final int textSize = text.length();
        final StringBuilder result = new StringBuilder(textSize);
        int pos = 0;
        for (int sp = text.indexOf(prefix, 0); sp >= 0; sp = text.indexOf(prefix, pos)) {
            int ep = text.indexOf(suffix, sp);
            if (ep < 0)
                break;
            for (int spp = text.indexOf(prefix, sp + 1); spp > 0 && spp < ep; spp = text.indexOf(prefix, sp + 1)) sp = spp;
            result.append(text.substring(pos, sp));
            final String key = text.substring(sp + 1, ep);
            final Object value = attrs.get(key);
            if (value != null) {
                result.append(value.toString());
            } else if (defaultValue != null) {
                result.append(defaultValue);
            }
            pos = ep + 1;
        }
        result.append(text.substring(pos, textSize));
        return result.toString();
    }

    /**
     * Заменяет все вхождения указанных в атрибуте <code>attrs</code> выражений на соответствующие им значения.
     *
     * @param text     строка в которой требуется провести серию замен. Если аргумент равен <code>null</code> то метод завершает работу возвращая <code>null</code>.
     * @param prefix   специальный маркерный символ предшествующий началу очередного шаблона для замены. Не может быть пустым.
     * @param suffix   специальный маркерный символ завершающий текст шаблона для замены. Не может быть пустым или быть идентичным префиксу.
     * @param supplier функция возращающая по имени шаблона соответствующее ему значение.
     * @return Итоговая строка, в которой все шаблоны заменены или соответствующими им значениями или пустой строкой.
     */
    public static String replace(final String text, final String prefix, final String suffix, final Function<String, ?> supplier) {
        return replace(text, prefix, suffix, supplier, "");
    }

    /**
     * Заменяет все вхождения указанных в атрибуте <code>attrs</code> выражений на соответствующие им значения.
     *
     * @param text         строка в которой требуется провести серию замен. Если аргумент равен <code>null</code> то метод завершает работу возвращая <code>null</code>.
     * @param prefix       специальный маркерный символ предшествующий началу очередного шаблона для замены. Не может быть пустым.
     * @param suffix       специальный маркерный символ завершающий текст шаблона для замены. Не может быть пустым или быть идентичным префиксу.
     * @param supplier     функция возращающая по имени шаблона соответствующее ему значение.
     * @param defaultValue значение по умолчанию используемое если требуемый в строке атрибут отсутствует в мапе или равен <code>null</code>
     * @return Итоговая строка, в которой все шаблоны заменены соответствующими значениями.
     */
    public static String replace(final String text, final String prefix, final String suffix, final Function<String, ?> supplier, final String defaultValue) {
        int sp;
        if (text == null || (sp = text.indexOf(prefix, 0)) < 0)
            return text;
        final int textSize = text.length();
        final int prefixSize = prefix.length();
        final int suffixSize = suffix.length();
        final StringBuilder result = new StringBuilder(textSize);
        int pos = 0;
        for (; sp >= 0; sp = text.indexOf(prefix, pos)) {
            int ep = text.indexOf(suffix, sp);
            if (ep < 0)
                break;
            final int spp = text.lastIndexOf(prefix, ep - 1);
            if (spp > sp) {
                sp = spp;
            }
            result.append(text.substring(pos, sp));
            final String key = text.substring(sp + prefixSize, ep);
            final Object value = supplier.apply(key);
            if (value != null) {
                result.append(value.toString());
            } else if (defaultValue != null) {
                result.append(defaultValue);
            }
            pos = ep + suffixSize;
        }
        result.append(text.substring(pos, textSize));
        return result.toString();
    }


    /**
     * Объединяет список строк в одну строку, используя в качестве разделителя между исходными частями ее специальный символ.
     * Как правило используется в паре с методом {@link StringUtil#split(String, char, char)}, выполняющим обратную операцию.
     * <table border="1" style="border:1px solid black; white-space:nowrap;">
     * <caption>Примеры использования</caption>
     * <tr><th colspan="3">Аргументы вызова</th><th rowspan="2">Результат</th></tr>
     * <tr><th>mask</th><th>separator</th><th>Объединяемые строки</th></tr>
     * <tr><td>'&amp;'</td><td>':'</td><td>"aaa", "bbb", "ccc"</td><td>"aaa:bbb:ccc"</td></tr>
     * <tr><td>'&amp;'</td><td>':'</td><td>"", "bbb", ""</td><td>":bbb:"</td></tr>
     * <tr><td>'&amp;'</td><td>':'</td><td>"a:1", "b:2", "x&amp;y"</td><td>"a&amp;:1:b&amp;:2:x&amp;&amp;y"</td></tr>
     * </table>
     *
     * @param mask      символ при помощи которого будет маскироваться символ-разделитель встречающийся в объединяемых строках.
     * @param separator символ, которым будут разделяться объединяемые строки в итоговой строке.
     * @param parts     массив объединяемых строк. Может быть пустым но ни одна из этих строк не может быть <code>null</code>.
     * @return Строка состоящая из объединяемых строк, разделенных символом-разделителем.
     */
    public static String join(final char mask, final char separator, final String... parts) {
        if (parts == null)
            return null;
        final StringBuilder buf = new StringBuilder(32);
        boolean first = true;
        for (String part : parts) {
            if (first) {
                first = false;
            } else {
                buf.append(separator);
            }
            for (int i = 0, len = part.length(); i < len; i++) {
                final char c = part.charAt(i);
                if (c == mask) {
                    buf.append(mask).append(mask);
                } else if (c == separator) {
                    buf.append(mask).append(separator);
                } else {
                    buf.append(c);
                }
            }
        }
        return buf.toString();
    }

    /**
     * Выполняет операцию, обратную той что делает метод {@link StringUtil#join(char, char, String...)}
     *
     * @param text      Строка которую требуется разбить на подстроки. Может быть пустой строкой.
     * @param separator символ, по которому будут разделяться подстроки в исходной строке.
     * @param mask      символ которым маскируется символ-разделитель в строке, которую требуется разбить на подстроки.
     * @return список строк полученных путем разбиения исходной строки на подстроки, где в качестве разделителя используется указанный во втором аргументе символ.
     * Список может быть пустым если исходная строка была нулевой длины. Метод возвращает <code>null</code> если и исходная строка была равна <code>null</code>.
     */
    public static List<String> split(final String text, final char separator, final char mask) {
        if (text == null)
            return null;
        final ArrayList<String> parts = new ArrayList<>();
        if (text.length() == 0)
            return parts;
        final StringBuilder buf = new StringBuilder(32);
        boolean masked = false;
        for (int i = 0, len = text.length(); i < len; i++) {
            final char c = text.charAt(i);
            if (c == mask) {
                if (masked) {
                    buf.append(mask);
                    masked = false;
                } else {
                    masked = true;
                }
            } else if (c == separator) {
                if (masked) {
                    buf.append(separator);
                    masked = false;
                } else {
                    parts.add(buf.toString());
                    buf.setLength(0);
                }
            } else {
                buf.append(c);
            }
        }
        parts.add(buf.toString());
        return parts;
    }

    /**
     * Разбивает исходную строку на несколько подстрок трактуя указанный символ как разделитель.
     * Примеры использования метода когда в качестве подстроки используется символ '_' :
     * <ol>
     * <li> исходная строка <code>a_b_c</code> будет разбита на три подстроки: {"a", "b", "c"}.
     * <li> исходная строка <code>a_b_</code> будет разбита на две подстроки: {"a", "b"}.
     * <li> исходная строка <code>a__c</code> будет разбита на три подстроки: {"a", "", "c"}.
     * <li> исходная строка <code>_b_c</code> будет разбита на три подстроки: {"", "b", "c"}.
     * <li> исходная строка <code>a</code> будет оставлена как есть: {"a"}.
     * </ol>
     *
     * @param text      исходная строка.
     * @param separator символ используемый в качестве разделителя.
     * @return список строк полученных в результате разделения исходной строки на подстроки используя указанный символ-разделитель.
     * Метод возвращает пустой список если исходная строка равна <code>null</code>.
     */
    public static List<String> split(final String text, final char separator) {
        if (text == null)
            return Collections.emptyList();
        final ArrayList<String> buf = new ArrayList<>();
        final int length = text.length();
        int pos = 0;
        for (int i = 0; i < length; i++) {
            if (text.charAt(i) == separator) {
                buf.add(text.substring(pos, i));
                pos = i + 1;
            }
        }
        if (pos < length)
            buf.add(text.substring(pos, length));
        return buf;
    }

    /**
     * Разбивает исходную строку на несколько непустых подстрок допуская в качестве разделителей:
     * <ul>
     * <li>любые символы с ascii кодом меньшим или равным 0x20 (включая пробелы, табуляции и переводы строк)</li>
     * <li>запятые (<code>,</code>)</li>
     * <li>точки с запятой (<code>;</code>)</li>
     * </ul>
     * Примеры
     * <ol>
     * <li> исходная строка <code>a b c</code> будет разбита на три подстроки: {"a", "b", "c"}.
     * <li> исходная строка <code>a b ;; c,d</code> будет разбита на четыре подстроки: {"a", "b", "c", "d"}.
     * <li> исходная строка <code>a_b_</code> будет разбита на две подстроки: {"a", "b"}.
     * <li> исходная строка <code>a</code> будет оставлена как есть: {"a"}.
     * <li> если исходная строка равна пустой строке или состоит только из символов-разделителей метод вернет пустой список.
     * <li> если исходная строка равна <code>null</code> - метод вернет <code>null</code>
     * </ol>
     *
     * @param text исходная строка.
     * @return список строк полученных в результате разделения исходной строки на подстроки используя указанный символ-разделитель.
     * Метод возвращает пустой список если исходная строка равна <code>null</code>.
     */
    public static List<String> splitIgnoringEmpty(final String text) {
        if (text == null)
            return Collections.emptyList();
        final int len0 = text.length();
        final ArrayList<String> result = new ArrayList<>();
        int start = -1;
        for (int i = 0; i < len0; i++) {
            char c = text.charAt(i);
            if (c <= ' ' || c == ',' || c == ';') {
                if (start >= 0) {
                    result.add(text.substring(start, i));
                    start = -1;
                }
            } else {
                if (start < 0)
                    start = i;
            }
        }
        if (start >= 0)
            result.add(text.substring(start));
        return result;
    }

    /**
     * Разбивает исходную строку на несколько непустых подстрок трактуя указанный символ как разделитель.
     * В полученных после разбиения подстроках удаляются концевые пробелы и если полученная подстрока будет не пустой то она будет включена в результат данной функции.
     * Примеры использования метода когда в качестве подстроки используется символ '_' :
     * <ol>
     * <li> исходная строка <code>a_b_c</code> будет разбита на три подстроки: {"a", "b", "c"}.
     * <li> исходная строка <code>a_b_</code> будет разбита на две подстроки: {"a", "b"}.
     * <li> исходная строка <code>a__c</code> будет разбита на три подстроки: {"a", "c"}.
     * <li> исходная строка <code>_ b _c</code> будет разбита на две подстроки: {"b", "c"}.
     * <li> исходная строка <code>a</code> будет оставлена как есть: {"a"}.
     * <li> исходная строка <code>__</code> будет представлена в виде пустого множества: {}.
     * <li> исходная строка <code>  </code></code> будет представлена в виде пустого множества: {}.
     * </ol>
     *
     * @param text      исходная строка.
     * @param separator символ используемый в качестве разделителя.
     * @return список непустых строк полученных в результате разделения исходной строки на подстроки используя указанный символ-разделитель.
     * Метод возвращает <code>null</code> если исходная строка равна <code>null</code>.
     */
    public static List<String> splitIgnoringEmpty(final String text, final char separator) {
        if (text == null)
            return Collections.emptyList();
        final ArrayList<String> result = new ArrayList<>();
        final int length = text.length();
        int pos = 0;
        for (int i = 0; i < length; i++) {
            if (text.charAt(i) == separator) {
                final String token = text.substring(pos, i).trim();
                if (!token.isEmpty())
                    result.add(token);
                pos = i + 1;
            }
        }
        if (pos < length) {
            final String token = text.substring(pos, length).trim();
            if (!token.isEmpty())
                result.add(token);
        }
        return result;
    }

    /**
     * Маскирует использование символа указанного в аргументе <code>maskedSymbol</code> при помощи некоторого другого символа <code>maskingSymbol</code>.<br/>
     * пример: вызов метода: <b><code>mask("abc-def\iklmn",'-','\')</code></b>
     * вернет строку: <b><code>"abc\-def\\iklmn"</code></b>.
     *
     * @param text          исходная строка.
     * @param maskedSymbol  символ, использование которого требуется замаскировать в строке с использованием другого символа.
     * @param maskingSymbol символ, которым требуется замаскировать использование некоторого другого символа.
     * @return измененная исходная строка в которой перед каждым вхождением маскируемого символа <code>maskedSymbol</code> установлен маскирующий символ <code>maskingSymbol</code>.
     * Если в исходной строке маскируемый символ не встречается то метод вернет исходную строку без изменений.
     */
    public static String mask(final String text, final char maskedSymbol, final char maskingSymbol) {
        if (text == null || text.isEmpty() || text.indexOf(maskedSymbol, 0) < 0 && text.indexOf(maskingSymbol, 0) < 0)
            return text;
        final int length = text.length();
        final StringBuilder buf = new StringBuilder(text.length() + 2);
        for (int i = 0; i < length; i++) {
            final char c = text.charAt(i);
            if (c == maskedSymbol || c == maskingSymbol) {
                buf.append(maskingSymbol);
            }
            buf.append(c);
        }
        return buf.toString();
    }

    /**
     * Выполняет операцию, обратную той что делает метод {@link StringUtil#mask(String, char, char)}, т.е всегда выполняется условие:<br/>
     * <code>StringUtil.unmask(StringUtil.mask(str,c,mask), mask) == str</code>.
     *
     * @param text          строка в которой требуется убрать маскировку с символов.
     * @param maskedSymbol  символ, использование которого маскировалось в строке с использованием другого символа.
     * @param maskingSymbol символ, используемый для маскировки других символов.
     * @return измененная исходная строка в которой была убрана маскировка
     */
    public static String unmask(final String text, final char maskedSymbol, final char maskingSymbol) {
        if (text == null || text.isEmpty() || text.indexOf(maskingSymbol, 0) < 0)
            return text;
        final int length = text.length();
        final StringBuilder buf = new StringBuilder(text.length());
        boolean masked = false;
        for (int i = 0; i < length; i++) {
            final char c = text.charAt(i);
            if (masked) {
                if (c != maskedSymbol && c != maskingSymbol) {
                    buf.append(maskingSymbol);
                }
                buf.append(c);
                masked = false;
            } else {
                if (c == maskingSymbol) {
                    masked = true;
                } else {
                    buf.append(c);
                }
            }
        }
        if (masked)
            buf.append(maskingSymbol);
        return buf.toString();
    }


    /**
     * Конвертирует недопустимые в HTML тексте символы в соответствующие кодовые обозначения принятые в HTML.<br/>
     * Заменяет символы '&amp;', '&lt;', '&gt;' на соответствующие обозначения принятые в HTML: "&amp;amp;", "&amp;lt;", "&amp;gt;".
     *
     * @param text оригинальный текст HTML.
     * @return исходный текст в котором символы
     */
    public static String encodeXMLText(final CharSequence text) {
        final int length;
        if (text == null || (length = text.length()) == 0)
            return "";
        final StringBuilder dst = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char ch = text.charAt(i);
            final char[] replacement;
            if (ch >= 128 || (replacement = REPLACEMENT_XML_TEXTS[ch]) == null) {
                dst.append(ch);
            } else {
                dst.append(replacement);
            }
        }
        return dst.toString();
    }

    /**
     * Конвертирует недопустимые в HTML тексте символы в соответствующие кодовые обозначения принятые в HTML.<br/>
     * Заменяет символы '&amp;', '&lt;', '&gt;' на соответствующие обозначения принятые в HTML: "&amp;amp;", "&amp;lt;", "&amp;gt;".
     *
     * @param out  выходной поток куда будет помещена отконвертированная версия входной строки.
     * @param text оригинальный текст HTML.
     * @throws IOException в случае каких-либо проблем вывода данных в поток.
     */
    public static void encodeXMLText(final Writer out, final String text) throws IOException {
        final int length;
        if (text == null || (length = text.length()) == 0)
            return;
        int last = 0;
        for (int i = 0; i < length; i++) {
            final char ch = text.charAt(i);
            final char[] replacement;
            if (ch >= 128 || (replacement = REPLACEMENT_XML_TEXTS[ch]) == null)
                continue;
            if (last < i)
                out.write(text, last, i - last);
            out.write(replacement);
            last = i + 1;
        }
        if (last < length)
            out.write(text, last, length - last);
    }

    /**
     * Конвертирует недопустимые в HTML тексте символы в соответствующие кодовые обозначения принятые в HTML.<br/>
     * Заменяет символы '&amp;', '&lt;', '&gt;' на соответствующие обозначения принятые в HTML: "&amp;amp;", "&amp;lt;", "&amp;gt;".
     *
     * @param out  выходной поток куда будет помещена отконвертированная версия входной строки.
     * @param text оригинальный текст HTML.
     */
    public static void encodeXMLText(final StringBuilder out, final String text) {
        final int length;
        if (text == null || (length = text.length()) == 0)
            return;
        int last = 0;
        for (int i = 0; i < length; i++) {
            final char ch = text.charAt(i);
            final char[] replacement;
            if (ch >= 128 || (replacement = REPLACEMENT_XML_TEXTS[ch]) == null)
                continue;
            if (last < i)
                out.append(text, last, i);
            out.append(replacement);
            last = i + 1;
        }
        if (last < length)
            out.append(text, last, length);
    }

    /**
     * Конвертирует недопустимые в атрибутах тегов HTML символы в соответствующие кодовые обозначения принятые в HTML.<br/>
     * Заменяет символы '&amp;', ' " ', ' ' ' на соответствующие обозначения принятые в HTML: "&amp;amp;", "&amp;quot;", "&amp;#39;".
     *
     * @param text оригинальный текст HTML.
     * @return исходный текст в котором символы
     */
    public static String encodeXMLAttribute(final CharSequence text) {
        final int length;
        if (text == null || (length = text.length()) == 0)
            return "";
        final StringBuilder dst = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char ch = text.charAt(i);
            final char[] replacement;
            if (ch >= 128 || (replacement = REPLACEMENT_XML_ATTRS[ch]) == null) {
                dst.append(ch);
            } else {
                dst.append(replacement);
            }
        }
        return dst.toString();
    }

    /**
     * Конвертирует недопустимые в атрибутах тегов HTML символы в соответствующие кодовые обозначения принятые в HTML.<br/>
     * Заменяет символы '&amp;', ' " ', ' ' ' на соответствующие обозначения принятые в HTML: "&amp;amp;", "&amp;quot;", "&amp;#39;".
     *
     * @param out  выходной поток куда будет помещена отконвертированная версия входной строки.
     * @param text оригинальный текст значения атрибута HTML.
     * @throws IOException в случае каких-либо проблем вывода данных в поток.
     */
    public static void encodeXMLAttribute(final Writer out, final String text) throws IOException {
        final int length;
        if (text == null || (length = text.length()) == 0)
            return;
        int last = 0;
        for (int i = 0; i < length; i++) {
            final char ch = text.charAt(i);
            final char[] replacement;
            if (ch >= 128 || (replacement = REPLACEMENT_XML_ATTRS[ch]) == null)
                continue;
            //if (ch == '&' && (i + 1) < length && text.charAt(i + 1) == '{') continue;             // HTML spec B.7.1 (reserved syntax for future script macros)
            if (last < i)
                out.write(text, last, i - last);
            out.write(replacement);
            last = i + 1;
        }
        if (last < length)
            out.write(text, last, length - last);
    }

    /**
     * Конвертирует недопустимые в атрибутах тегов HTML символы в соответствующие кодовые обозначения принятые в HTML.<br/>
     * Заменяет символы '&amp;', ' " ', ' ' ' на соответствующие обозначения принятые в HTML: "&amp;amp;", "&amp;quot;", "&amp;#39;".
     *
     * @param out  выходной поток куда будет помещена отконвертированная версия входной строки.
     * @param text оригинальный текст значения атрибута HTML.
     */
    public static void encodeXMLAttribute(final StringBuilder out, final String text) {
        final int length;
        if (text == null || (length = text.length()) == 0)
            return;
        int last = 0;
        for (int i = 0; i < length; i++) {
            final char ch = text.charAt(i);
            final char[] replacement;
            if (ch >= 128 || (replacement = REPLACEMENT_XML_ATTRS[ch]) == null)
                continue;
            //if (ch == '&' && (i + 1) < length && text.charAt(i + 1) == '{') continue;             // HTML spec B.7.1 (reserved syntax for future script macros)
            if (last < i)
                out.append(text, last, i);
            out.append(replacement);
            last = i + 1;
        }
        if (last < length)
            out.append(text, last, length);
    }


    /**
     * Преобразовывает строки вида <code>aa/bb/dd/../cc</code> в строки вида <code>aa/bb/cc</code>.<br/>
     * Данный метод может использоваться при вычисления абсолютных путей до файлов, ресурсов в ClassPath или ссылок на ресурсы в Web.
     * <table border="1" style="border:1px solid black; white-space:nowrap;">
     * <caption>Примеры использования</caption>
     * <tr><th colspan="2">Аргументы вызова</th><th rowspan="2">Результат</th></tr>
     * <tr><th>Контекст</th><th>Путь</th></tr>
     * <tr><td>/mycontext</td><td>null</td><td>/mycontext</td></tr>
     * <tr><td>/mycontext</td><td></td><td>/mycontext</td></tr>
     * <tr><td>/mycontext</td><td>/</td><td>/</td></tr>
     * <tr><td>/mycontext</td><td>/aa/bb/cc</td><td>/aa/bb/cc</td></tr>
     * <tr><td>/mycontext</td><td>/aa//bb/./cc/</td><td>/aa/bb/cc/</td></tr>
     * <tr><td>/mycontext</td><td>/aa/bb/../cc</td><td>/aa/cc</td></tr>
     * <tr><td>/mycontext</td><td>aa/bb/../cc</td><td>/mycontext/aa/cc</td></tr>
     * <tr><td>/mycontext</td><td>./aa/bb/../cc</td><td>/mycontext/aa/cc</td></tr>
     * </table>
     *
     * @param context контекст, относительно которого указывается во втором аргументе нормализуемый путь к ресурсу.
     *                Данный параметр используется исключительно в тех случаях когда путь НЕ начинается с символа '<code>/</code>'
     *                Значение данного аргемента не может быть <code>null</code> а также не может содержать символы, не допустимые в пути URL.
     * @param path    требующий нормализации путь к некоторому ресурсу. В качестве разделителя между элементами пути используется символ '<code>/</code>'.
     * @return нормализованный путь к некоторому ресурсу.
     * @throws IndexOutOfBoundsException В случае некорректно составленного url за счет избыточного использования токенов <code>..</code>
     */
    public static String normalizePath(final String context, final String path) {
        if (path == null)
            return context;
        final ArrayList<String> tokens = new ArrayList<>();
        final boolean addContext = !path.startsWith("/");
        for (StringTokenizer tokenizer = new StringTokenizer(path.trim(), "/", false); tokenizer.hasMoreTokens(); ) {
            final String token = tokenizer.nextToken();
            if (".".equals(token)) {
                // skip token ...
            } else if ("..".equals(token)) {
                tokens.remove(tokens.size() - 1);
            } else {
                tokens.add(token);
            }
        }
        final StringBuilder buf = new StringBuilder(32);
        if (addContext) {
            if (tokens.size() > 0 && context.endsWith("/")) {
                buf.append(context.substring(0, context.length() - 1));
            } else {
                buf.append(context);
            }
        } else {
            if (tokens.size() == 0)
                return "/";
        }
        for (String token : tokens) {
            buf.append('/');
            buf.append(token);
        }
        return buf.toString();
    }

    /**
     * Возвращает строку которая может послужить именем файла или каталога. Она образована от заданной в аргументе строки
     * путем вырезания всех недопустимых в имени файла/каталога символов.
     *
     * @param name предполагаемое имя файла. Допускается пустая строка или null.
     * @return производная от предполагаемого имени файла которое содержит только допустимые символы или null если после обрезания недопустимых осталась пустая строка.
     */
    public static String getFileName(String name) {
        if (name == null)
            return null;
        final int length = name.length();
        final StringBuilder buf = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char c = name.charAt(i);
            switch (c) {
                case '<':
                case '>':
                case ':':
                case '?':
                case '*':
                case '|':
                case '\\':
                case '/':
                case '\"':
                    continue;
                default:
                    buf.append(c);
            }
        }
        name = buf.toString().trim();
        return name.isEmpty() ? null : name;
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>dd.MM.yyyy</code>.
     *
     * @param date дата которую требуется преобразовать в строку.
     * @return результат форматирования даты или пустая строка если исходная дата равна <code>null</code>.
     */
    public static String formatDate(final Date date) {
        if (date == null)
            return "";
        final FormatData data = FMT_DATA_HOLDER.get();
        final StringBuilder buf = data.buf;
        buf.setLength(0);
        final Calendar cal = data.calendar;
        cal.setTime(date);
        int p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(p);
        buf.append('.');
        p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(p);
        buf.append('.');
        buf.append(cal.get(Calendar.YEAR));
        return buf.toString();
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>dd.MM.yyyy</code>.
     * Если исходная дата равна <code>null</code> то метод не делает ничего.
     *
     * @param buf  буффер, реализующий интерфейс {@link Appendable} в котором будет аккумулироваться результат.
     * @param date дата которую требуется преобразовать в строку.
     * @throws IOException в случае каких-либо проблем с сохранением данных в буфере.
     */
    public static void formatDate(final Appendable buf, final Date date) throws IOException {
        if (date == null)
            return;
        final Calendar cal = FMT_DATA_HOLDER.get().calendar;
        cal.setTime(date);
        int p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('.');
        p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('.');
        buf.append(Integer.toString(cal.get(Calendar.YEAR)));
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>dd.MM.yyyy</code>.
     * Если исходная дата равна <code>null</code> то метод не делает ничего.
     *
     * @param buf  буффер, реализующий интерфейс {@link Appendable} в котором будет аккумулироваться результат.
     * @param date дата которую требуется преобразовать в строку.
     */
    public static void formatDate(final StringBuilder buf, final Date date) {
        if (date == null)
            return;
        final Calendar cal = FMT_DATA_HOLDER.get().calendar;
        cal.setTime(date);
        int p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('.');
        p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('.');
        buf.append(Integer.toString(cal.get(Calendar.YEAR)));
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>dd.MM.yyyy HH:mm:ss</code>.
     *
     * @param date дата которую требуется преобразовать в строку.
     * @return результат форматирования даты или пустая строка если исходная дата равна <code>null</code>.
     */
    public static String formatDateTime(final Date date) {
        if (date == null)
            return "";
        final FormatData data = FMT_DATA_HOLDER.get();
        final StringBuilder buf = data.buf;
        buf.setLength(0);
        final Calendar cal = data.calendar;
        cal.setTime(date);
        int p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(p);
        buf.append('.');
        p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(p);
        buf.append('.');
        buf.append(cal.get(Calendar.YEAR));
        buf.append(' ');
        p = cal.get(Calendar.HOUR_OF_DAY);
        if (p < 10)
            buf.append('0');
        buf.append(p);
        buf.append(':');
        p = cal.get(Calendar.MINUTE);
        if (p < 10)
            buf.append('0');
        buf.append(p);
        buf.append(':');
        p = cal.get(Calendar.SECOND);
        if (p < 10)
            buf.append('0');
        buf.append(p);
        return buf.toString();
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>dd.MM.yyyy HH:mm:ss</code>.
     * Если исходная дата равна <code>null</code> то метод не делает ничего.
     *
     * @param buf  буффер, реализующий интерфейс {@link Appendable} в котором будет аккумулироваться результат.
     * @param date дата которую требуется преобразовать в строку.
     * @throws IOException в случае каких-либо проблем с сохранением данных в буфере.
     */
    public static void formatDateTime(final Appendable buf, final Date date) throws IOException {
        if (date == null)
            return;
        final Calendar cal = FMT_DATA_HOLDER.get().calendar;
        cal.setTime(date);
        int p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('.');
        p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('.');
        buf.append(Integer.toString(cal.get(Calendar.YEAR)));
        buf.append(' ');
        p = cal.get(Calendar.HOUR_OF_DAY);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.MINUTE);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.SECOND);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>dd.MM.yyyy HH:mm:ss</code>.
     * Если исходная дата равна <code>null</code> то метод не делает ничего.
     *
     * @param buf  буффер, реализующий интерфейс {@link Appendable} в котором будет аккумулироваться результат.
     * @param date дата которую требуется преобразовать в строку.
     */
    public static void formatDateTime(final StringBuilder buf, final Date date) {
        if (date == null)
            return;
        final Calendar cal = FMT_DATA_HOLDER.get().calendar;
        cal.setTime(date);
        int p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('.');
        p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('.');
        buf.append(Integer.toString(cal.get(Calendar.YEAR)));
        buf.append(' ');
        p = cal.get(Calendar.HOUR_OF_DAY);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.MINUTE);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.SECOND);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>dd.MM.yyyy HH:mm</code>.
     *
     * @param date дата которую требуется преобразовать в строку.
     * @return результат форматирования даты или пустая строка если исходная дата равна <code>null</code>.
     */
    public static String formatDateTime2(final Date date) {
        if (date == null)
            return "";
        final FormatData data = FMT_DATA_HOLDER.get();
        final StringBuilder buf = data.buf;
        buf.setLength(0);
        final Calendar cal = data.calendar;
        cal.setTime(date);
        int p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(p);
        buf.append('.');
        p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(p);
        buf.append('.');
        buf.append(cal.get(Calendar.YEAR));
        buf.append(' ');
        p = cal.get(Calendar.HOUR_OF_DAY);
        if (p < 10)
            buf.append('0');
        buf.append(p);
        buf.append(':');
        p = cal.get(Calendar.MINUTE);
        if (p < 10)
            buf.append('0');
        buf.append(p);
        return buf.toString();
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>dd.MM.yyyy HH:mm</code>.
     * Если исходная дата равна <code>null</code> то метод не делает ничего.
     *
     * @param buf  буффер, реализующий интерфейс {@link Appendable} в котором будет аккумулироваться результат.
     * @param date дата которую требуется преобразовать в строку.
     * @throws IOException в случае каких-либо проблем с сохранением данных в буфере.
     */
    public static void formatDateTime2(final Appendable buf, final Date date) throws IOException {
        if (date == null)
            return;
        final Calendar cal = FMT_DATA_HOLDER.get().calendar;
        cal.setTime(date);
        int p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('.');
        p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('.');
        buf.append(Integer.toString(cal.get(Calendar.YEAR)));
        buf.append(' ');
        p = cal.get(Calendar.HOUR_OF_DAY);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.MINUTE);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>dd.MM.yyyy HH:mm</code>.
     * Если исходная дата равна <code>null</code> то метод не делает ничего.
     *
     * @param buf  буффер, реализующий интерфейс {@link Appendable} в котором будет аккумулироваться результат.
     * @param date дата которую требуется преобразовать в строку.
     */
    public static void formatDateTime2(final StringBuilder buf, final Date date) {
        if (date == null)
            return;
        final Calendar cal = FMT_DATA_HOLDER.get().calendar;
        cal.setTime(date);
        int p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('.');
        p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('.');
        buf.append(Integer.toString(cal.get(Calendar.YEAR)));
        buf.append(' ');
        p = cal.get(Calendar.HOUR_OF_DAY);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.MINUTE);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>yyyy-MM-dd</code>.
     *
     * @param date дата которую требуется преобразовать в строку.
     * @return результат форматирования даты или пустая строка если исходная дата равна <code>null</code>.
     */
    public static String formatISODate(final Date date) {
        if (date == null)
            return "";
        final FormatData data = FMT_DATA_HOLDER.get();
        final StringBuilder buf = data.buf;
        buf.setLength(0);
        final Calendar cal = data.calendar;
        cal.setTime(date);
        buf.append(cal.get(Calendar.YEAR));
        buf.append('-');
        int p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(p);
        buf.append('-');
        p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(p);
        return buf.toString();
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>yyyy-MM-dd</code>.
     * Если исходная дата равна <code>null</code> то метод не делает ничего.
     *
     * @param buf  буффер, реализующий интерфейс {@link Appendable} в котором будет аккумулироваться результат.
     * @param date дата которую требуется преобразовать в строку.
     * @throws IOException в случае каких-либо проблем с сохранением данных в буфере.
     */
    public static void formatISODate(final Appendable buf, final Date date) throws IOException {
        if (date == null)
            return;
        final Calendar cal = FMT_DATA_HOLDER.get().calendar;
        cal.setTime(date);
        buf.append(Integer.toString(cal.get(Calendar.YEAR)));
        buf.append('-');
        int p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('-');
        p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>yyyy-MM-dd</code>.
     * Если исходная дата равна <code>null</code> то метод не делает ничего.
     *
     * @param buf  буффер, реализующий интерфейс {@link Appendable} в котором будет аккумулироваться результат.
     * @param date дата которую требуется преобразовать в строку.
     */
    public static void formatISODate(final StringBuilder buf, final Date date) {
        if (date == null)
            return;
        final Calendar cal = FMT_DATA_HOLDER.get().calendar;
        cal.setTime(date);
        buf.append(Integer.toString(cal.get(Calendar.YEAR)));
        buf.append('-');
        int p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('-');
        p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>yyyy-MM-dd HH:mm:ss</code>.
     * Если исходная дата равна <code>null</code> то метод возвращает пустую строку.
     *
     * @param date дата которую требуется преобразовать в строку.
     * @return результат форматирования даты или пустая строка если исходная дата равна <code>null</code>.
     */
    public static String formatISODateTime(final Date date) {
        if (date == null)
            return "";
        final FormatData data = FMT_DATA_HOLDER.get();
        final Calendar cal = data.calendar;
        cal.setTime(date);
        final StringBuilder buf = data.buf;
        buf.setLength(0);
        buf.append(Integer.toString(cal.get(Calendar.YEAR)));
        buf.append('-');
        int p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('-');
        p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(' ');
        p = cal.get(Calendar.HOUR_OF_DAY);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.MINUTE);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.SECOND);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        return buf.toString();
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>yyyy-MM-dd HH:mm:ss</code>.
     * Если исходная дата равна <code>null</code> то метод не делает ничего.
     *
     * @param buf  буффер, реализующий интерфейс {@link Appendable} в котором будет аккумулироваться результат.
     * @param date дата которую требуется преобразовать в строку.
     * @throws IOException в случае каких-либо проблем с сохранением данных в буфере.
     */
    public static void formatISODateTime(final Appendable buf, final Date date) throws IOException {
        if (date == null)
            return;
        final Calendar cal = FMT_DATA_HOLDER.get().calendar;
        cal.setTime(date);
        buf.append(Integer.toString(cal.get(Calendar.YEAR)));
        buf.append('-');
        int p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('-');
        p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(' ');
        p = cal.get(Calendar.HOUR_OF_DAY);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.MINUTE);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.SECOND);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>yyyy-MM-dd HH:mm:ss</code>.
     * Если исходная дата равна <code>null</code> то метод не делает ничего.
     *
     * @param buf  буффер, реализующий интерфейс {@link Appendable} в котором будет аккумулироваться результат.
     * @param date дата которую требуется преобразовать в строку.
     */
    public static void formatISODateTime(final StringBuilder buf, final Date date) {
        if (date == null)
            return;
        final Calendar cal = FMT_DATA_HOLDER.get().calendar;
        cal.setTime(date);
        buf.append(Integer.toString(cal.get(Calendar.YEAR)));
        buf.append('-');
        int p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('-');
        p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(' ');
        p = cal.get(Calendar.HOUR_OF_DAY);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.MINUTE);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.SECOND);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>yyyy-MM-dd HH:mm</code>.
     * Если исходная дата равна <code>null</code> то метод возвращает пустую строку.
     *
     * @param date дата которую требуется преобразовать в строку.
     * @return результат форматирования даты или пустая строка если исходная дата равна <code>null</code>.
     */
    public static String formatISODateTime2(final Date date) {
        if (date == null)
            return "";
        final FormatData data = FMT_DATA_HOLDER.get();
        final Calendar cal = data.calendar;
        cal.setTime(date);
        final StringBuilder buf = data.buf;
        buf.setLength(0);
        buf.append(Integer.toString(cal.get(Calendar.YEAR)));
        buf.append('-');
        int p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('-');
        p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(' ');
        p = cal.get(Calendar.HOUR_OF_DAY);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.MINUTE);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        return buf.toString();
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>yyyy-MM-dd HH:mm</code>.
     * Если исходная дата равна <code>null</code> то метод не делает ничего.
     *
     * @param buf  буффер, реализующий интерфейс {@link Appendable} в котором будет аккумулироваться результат.
     * @param date дата которую требуется преобразовать в строку.
     * @throws IOException в случае каких-либо проблем с сохранением данных в буфере.
     */
    public static void formatISODateTime2(final Appendable buf, final Date date) throws IOException {
        if (date == null)
            return;
        final Calendar cal = FMT_DATA_HOLDER.get().calendar;
        cal.setTime(date);
        buf.append(Integer.toString(cal.get(Calendar.YEAR)));
        buf.append('-');
        int p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('-');
        p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(' ');
        p = cal.get(Calendar.HOUR_OF_DAY);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.MINUTE);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>yyyy-MM-dd HH:mm</code>.
     * Если исходная дата равна <code>null</code> то метод не делает ничего.
     *
     * @param buf  буффер, реализующий интерфейс {@link Appendable} в котором будет аккумулироваться результат.
     * @param date дата которую требуется преобразовать в строку.
     */
    public static void formatISODateTime2(final StringBuilder buf, final Date date) {
        if (date == null)
            return;
        final Calendar cal = FMT_DATA_HOLDER.get().calendar;
        cal.setTime(date);
        buf.append(Integer.toString(cal.get(Calendar.YEAR)));
        buf.append('-');
        int p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('-');
        p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(' ');
        p = cal.get(Calendar.HOUR_OF_DAY);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.MINUTE);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
    }


    /**
     * Осуществляет разбор даты из строки даты в формате <code>dd.MM.yyyy</code>. Если исходная строка равна null или имеет нулевую длину то метод вернет <code>null</code>.
     *
     * @param text строка в формате <code>dd.MM.yyyy</code>.
     * @return разобранную из строки дату или <code>null</code> если строковой аргумент равен <code>null</code> или имеет нулевую длину.
     * @throws ParseException в случае несоответствия переданной в аргументе строки требуемому формату.
     */
    public static Date parseDate(final CharSequence text) throws ParseException {
        final int length;
        if (text == null || (length = text.length()) == 0)
            return null;
        if (length < 10)
            throw new ParseException("length of the string in argument must be at least 10", 0);
        final Calendar cal = FMT_DATA_HOLDER.get().calendar;
        cal.clear();
        if (text.charAt(2) != '.')
            throw new ParseException("Delimiter not finded", 2);
        if (text.charAt(5) != '.')
            throw new ParseException("Delimiter not finded", 5);
        cal.set(Calendar.DAY_OF_MONTH, parseInt(text, 0, 2));
        cal.set(Calendar.MONTH, parseInt(text, 3, 5) - 1);
        cal.set(Calendar.YEAR, parseInt(text, 6, 10));
        return cal.getTime();
    }

    /**
     * Осуществляет разбор даты из строки даты в формате <code>dd.MM.yyyy HH:mm:ss</code>. Если исходная строка равна null или имеет нулевую длину то метод вернет <code>null</code>.
     *
     * @param text строка в формате <code>dd.MM.yyyy HH:mm:ss</code>.
     * @return разобранную из строки дату или <code>null</code> если строковой аргумент равен <code>null</code> или имеет нулевую длину.
     * @throws ParseException в случае несоответствия переданной в аргументе строки требуемому формату.
     */
    public static Date parseDateTime(final CharSequence text) throws ParseException {
        final int length;
        if (text == null || (length = text.length()) == 0)
            return null;
        if (length < 19)
            throw new ParseException("length of the string in argument must be at least 19", 0);
        final Calendar cal = FMT_DATA_HOLDER.get().calendar;
        cal.clear();
        if (text.charAt(2) != '.')
            throw new ParseException("Delimiter not finded", 2);
        if (text.charAt(5) != '.')
            throw new ParseException("Delimiter not finded", 5);
        if (text.charAt(10) != ' ')
            throw new ParseException("Delimiter not finded", 10);
        if (text.charAt(13) != ':')
            throw new ParseException("Delimiter not finded", 13);
        if (text.charAt(16) != ':')
            throw new ParseException("Delimiter not finded", 16);
        cal.set(Calendar.DAY_OF_MONTH, parseInt(text, 0, 2));
        cal.set(Calendar.MONTH, parseInt(text, 3, 5) - 1);
        cal.set(Calendar.YEAR, parseInt(text, 6, 10));
        cal.set(Calendar.HOUR_OF_DAY, parseInt(text, 11, 13));
        cal.set(Calendar.MINUTE, parseInt(text, 14, 16));
        cal.set(Calendar.SECOND, parseInt(text, 17, 19));
        return cal.getTime();
    }

    /**
     * Осуществляет разбор даты из строки даты в формате <code>dd.MM.yyyy HH:mm</code>. Если исходная строка равна null или имеет нулевую длину то метод вернет <code>null</code>.
     *
     * @param text строка в формате <code>dd.MM.yyyy HH:mm</code>.
     * @return разобранную из строки дату или <code>null</code> если строковой аргумент равен <code>null</code> или имеет нулевую длину.
     * @throws ParseException в случае несоответствия переданной в аргументе строки требуемому формату.
     */
    public static Date parseDateTime2(final CharSequence text) throws ParseException {
        final int length;
        if (text == null || (length = text.length()) == 0)
            return null;
        if (length < 16)
            throw new ParseException("length of the string in argument must be at least 16", 0);
        final Calendar cal = FMT_DATA_HOLDER.get().calendar;
        cal.clear();
        if (text.charAt(2) != '.')
            throw new ParseException("Delimiter not finded", 2);
        if (text.charAt(5) != '.')
            throw new ParseException("Delimiter not finded", 5);
        if (text.charAt(10) != ' ')
            throw new ParseException("Delimiter not finded", 10);
        if (text.charAt(13) != ':')
            throw new ParseException("Delimiter not finded", 13);
        cal.set(Calendar.DAY_OF_MONTH, parseInt(text, 0, 2));
        cal.set(Calendar.MONTH, parseInt(text, 3, 5) - 1);
        cal.set(Calendar.YEAR, parseInt(text, 6, 10));
        cal.set(Calendar.HOUR_OF_DAY, parseInt(text, 11, 13));
        cal.set(Calendar.MINUTE, parseInt(text, 14, 16));
        return cal.getTime();
    }

    /**
     * Осуществляет разбор даты из строки даты в формате <code>yyyy-MM-dd</code>. Если исходная строка равна null или имеет нулевую длину то метод вернет <code>null</code>.
     *
     * @param text строка в формате <code>yyyy-MM-dd</code>.
     * @return разобранную из строки дату или <code>null</code> если строковой аргумент равен <code>null</code> или имеет нулевую длину.
     * @throws ParseException в случае несоответствия переданной в аргументе строки требуемому формату.
     */
    public static Date parseISODate(final CharSequence text) throws ParseException {
        final int length;
        if (text == null || (length = text.length()) == 0)
            return null;
        if (length < 10)
            throw new ParseException("length of the string in argument must be at least 10", 0);
        final Calendar cal = FMT_DATA_HOLDER.get().calendar;
        cal.clear();
        if (text.charAt(4) != '-')
            throw new ParseException("Delimiter not finded", 4);
        if (text.charAt(7) != '-')
            throw new ParseException("Delimiter not finded", 7);
        cal.set(Calendar.YEAR, parseInt(text, 0, 4));
        cal.set(Calendar.MONTH, parseInt(text, 5, 7) - 1);
        cal.set(Calendar.DAY_OF_MONTH, parseInt(text, 8, 10));
        return cal.getTime();
    }

    /**
     * Осуществляет разбор даты из строки даты в одном из форматов: <code>yyyy-MM-dd HH:mm:ss</code>, <code>yyyy-MM-dd'T'HH:mm:ss</code>.
     * Если исходная строка равна null или имеет нулевую длину то метод вернет <code>null</code>.
     *
     * @param text строка в формате <code>yyyy-MM-dd HH:mm:ss</code> или <code>yyyy-MM-dd'T'HH:mm:ss</code>.
     * @return разобранную из строки дату или <code>null</code> если строковой аргумент равен <code>null</code> или имеет нулевую длину.
     * @throws ParseException в случае несоответствия переданной в аргументе строки требуемому формату.
     */
    public static Date parseISODateTime(final CharSequence text) throws ParseException {
        final int length;
        if (text == null || (length = text.length()) == 0)
            return null;
        if (length < 19)
            throw new ParseException("length of the string in argument must be at least 19", 0);
        final Calendar cal = FMT_DATA_HOLDER.get().calendar;
        cal.clear();
        if (text.charAt(4) != '-')
            throw new ParseException("Delimiter not finded", 4);
        if (text.charAt(7) != '-')
            throw new ParseException("Delimiter not finded", 7);
        final char dt = text.charAt(10);
        if (dt != ' ' && dt != 'T')
            throw new ParseException("Delimiter not finded", 10);
        if (text.charAt(13) != ':')
            throw new ParseException("Delimiter not finded", 13);
        if (text.charAt(16) != ':')
            throw new ParseException("Delimiter not finded", 16);
        cal.set(Calendar.YEAR, parseInt(text, 0, 4));
        cal.set(Calendar.MONTH, parseInt(text, 5, 7) - 1);
        cal.set(Calendar.DAY_OF_MONTH, parseInt(text, 8, 10));
        cal.set(Calendar.HOUR_OF_DAY, parseInt(text, 11, 13));
        cal.set(Calendar.MINUTE, parseInt(text, 14, 16));
        cal.set(Calendar.SECOND, parseInt(text, 17, 19));
        return cal.getTime();
    }


    /**
     * Возвращает строку со стеком вызова в случае возникновения исключительной ситуации.
     *
     * @param th исключение для которого надо вернуть строку со стеком вызова.
     * @return стек вызова.
     */
    public static String stackTrace(final Throwable th) {
        try {
            final StringWriter buf = new StringWriter(128);
            th.printStackTrace(new PrintWriter(buf, false));
            return buf.toString();
        } catch (Exception e) {
            return "Runtime error: " + e.getMessage();
        }
    }

    /**
     * Возвращает <code>true</code> если переданная в аргументе строка содержит только цифры.
     *
     * @param str строка
     * @return <code>false</code> если переданная в аргументе строка содержит какие-либо символы не являющиеся цифрами.
     * Для пустой строки или <code>null</code> метод возвращает <code>true</code>.
     */
    public static boolean hasDigitsOnly(final CharSequence str) {
        if (str == null)
            return true;
        final int length = str.length();
        for (int i = length - 1; i >= 0; i--) {
            if (!Character.isDigit(str.charAt(i)))
                return false;
        }
        return true;
    }

    /**
     * Возвращает <code>true</code> если переданная в аргументе строка удовлетворяет требованиям предъявляемым
     * к идентификаторам в языке java.
     *
     * @param str строка.
     * @return <code>true</code> если переданная в аргументе строка удовлетворяет требованиям предъявляемым к идентификаторам в языке java.
     */
    public static boolean isJavaIdentifier(final CharSequence str) {
        if (str == null || str.length() == 0)
            return false;
        if (!Character.isJavaIdentifierStart(str.charAt(0)))
            return false;
        for (int i = 1, len = str.length(); i < len; i++) {
            if (!Character.isJavaIdentifierPart(str.charAt(i)))
                return false;
        }
        return true;
    }

    /**
     * Форматирует указанный в аргументе массив байт в виде строки с 16-ричным представлением содержимого этого массива байт.
     *
     * @param data массив байт.
     * @return строка с шестнадцатиричным представлением содержимого исходного массива.
     */
    public static String formatBytes(final byte[] data) {
        if (data == null || data.length == 0)
            return null;
        final char[] chars = new char[data.length * 2];
        for (int i = 0, p = 0; i < data.length; i++) {
            final byte b = data[i];
            chars[p++] = HEXDIGITS[(0xF0 & b) >>> 4];
            chars[p++] = HEXDIGITS[0x0F & b];
        }
        return new String(chars);
    }

    /**
     * Простейший генериратор паролей состоящих из цифр и прописных латинских букв.
     *
     * @param length требуемая длина пароля.
     * @return новый пароль.
     */
    public static String generatePassword(final int length) {
        final Random r = new Random();
        final char[] result = new char[length];
        for (int i = 0; i < length; i++) {
            final char[] chars = r.nextInt(3) == 0 ? GEN_PWD_CHARS1 : GEN_PWD_CHARS2;
            final int p = r.nextInt(chars.length);
            result[i] = chars[p];
        }
        return new String(result);
    }

    public static Set<String> asUnmodifiableSet(final String... items) {
        final Set<String> set = new HashSet<>(items.length);
        set.addAll(Arrays.asList(items));
        return Collections.unmodifiableSet(set);
    }

    public static Set<String> asUnmodifiableSet(final Collection<String> items1, final String... items2) {
        final Set<String> set;
        if (items1 != null) {
            set = new HashSet<>(items1.size() + items2.length);
            set.addAll(items1);
        } else {
            set = new HashSet<>(items2.length);
        }
        set.addAll(Arrays.asList(items2));
        return Collections.unmodifiableSet(set);
    }


    private static int parseInt(final CharSequence text, final int beginIndex, final int endIndex) throws ParseException {
        int result = 0;
        for (int i = beginIndex; i < endIndex; i++) {
            result *= 10;
            final int d = Character.digit(text.charAt(i), 10);
            if (d < 0)
                throw new ParseException("Invalid number format", i);
            result += d;
        }
        return result;
    }

    /**
     * Возвращает некогда ранее созданный экземпляр класса {@link Calendar} с неопределенным на момент вызова этого метода значением.<br/>
     * Единственное (и самое главное!) что гарантирует данный метод это то что возвращаемый объект можно безопасно  использовать в текущем потоке (и только в нем!).<br/>
     * Данный метод используется в целях избежания потерь на избыточном создании новых экземпляров {@link Calendar} так как этот класс не является потокобезопасным
     * и инициализация объектов этого класса занимает довольно большое время.
     */
    private static final ThreadLocal<FormatData> FMT_DATA_HOLDER = new ThreadLocal<FormatData>() {
        protected FormatData initialValue() {
            return new FormatData();
        }
    };

    private static final class FormatData {
        final Calendar calendar;
        final StringBuilder buf;

        private FormatData() {
            this.calendar = Calendar.getInstance();
            this.buf = new StringBuilder(30);
        }
    }
}
