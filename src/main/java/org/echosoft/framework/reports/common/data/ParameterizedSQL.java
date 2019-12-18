package org.echosoft.framework.reports.common.data;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.echosoft.framework.reports.common.utils.StringUtil;

/**
 * Преобразует SQL выражения вида
 * <pre>
 *   SELECT a, b, c FROM table WHERE d = &amp;param1 AND e = &amp;param2
 * </pre>
 * к следующему виду:
 * <pre>
 *     SELECT a, b, c FROM table WHERE d = ? AND e = ?
 * </pre>
 * Класс корректно обработает содержимое текстовых идентификаторов и примечаний разных видов.
 * В качестве символа-указателя предшествующего имени параметра могут использоваться символы <b><code>&amp;</code></b> или <b><code>:</code></b>.
 *
 * @author Anton Sharapov
 */
public class ParameterizedSQL implements Serializable {

    private final String sql;
    private final List<String> paramNames;

    /**
     * @param namedSql текст SQL запроса в котором возможно есть именованные параметры. Параметр не может быть <code>null</code>.
     */
    public ParameterizedSQL(final String namedSql) {
        this.paramNames = new ArrayList<>();
        this.sql = transform(namedSql, paramNames);
    }

    /**
     * Возвращает текст запроса в котором все вхождения именованных параметров были заменены на символ <code>'?'</code>.
     *
     * @return текст запроса полученного после замены именованных параметров на анонимные.
     *         Метод никогда не возвращает <code>null</code>.
     */
    public String getQuery() {
        return sql;
    }

    /**
     * Возвращает список имен параметров в том порядке в котором они встречаются в запросе. Если какой-то параметр
     * встречается два и более раза то в возвращаемом списке он будет встречаться соответствующее количество раз в
     * тех позициях которые соответствовали вхождению данного параметра в исходном запросе.
     *
     * @return список имен параметров в том порядке в котором они встречаются в запросе.
     *         Метод никогда не возвращает <code>null</code>.
     */
    public List<String> getParamNames() {
        return paramNames;
    }

    public void applyParams(final PreparedStatement pstmt, final Map<String, Object> params) throws SQLException {
        int num = 1;
        for (String paramName : paramNames) {
            final Object value = params.get(paramName);
            if (value == null && !params.containsKey(paramName))
                throw new SQLException("Parameter '" + paramName + "' not specified in the arguments");
            pstmt.setObject(num++, value);
        }
    }

    /**
     * Формирует текст запрос с включенными в него значениями параметров.
     * Используется исключительно в целях отладки.
     *
     * @param params значения параметров запроса.
     * @return текст запроса с параметрами подставленными в нужных местах.
     */
    public String compileNonParameterizedQuery(final Map<String, Object> params) throws SQLException {
        final StringBuilder buf = new StringBuilder(sql.length() + 64);
        buf.append('\n');
        int idx = 0, lastPos = -1;
        int pos;
        while ((pos = sql.indexOf('?', lastPos + 1)) >= 0) {
            buf.append(sql.substring(lastPos + 1, pos));
            if (idx < paramNames.size()) {
                final String paramName = paramNames.get(idx++);
                Object param = params.get(paramName);
                if (param instanceof CharSequence) {
                    buf.append('\'').append(param).append('\'');
                } else if (param instanceof Date) {
                    buf.append("TO_DATE('");
                    StringUtil.formatISODateTime2(buf, (Date) param);
                    buf.append("', 'YYYY-MM-DD HH24:MI:SS')");
                } else
                    buf.append(param);
            } else {
                buf.append('?');
            }
            lastPos = pos;
        }
        buf.append(sql.substring(lastPos + 1));
        return buf.toString();
    }


    @Override
    public int hashCode() {
        return sql.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !getClass().equals(obj.getClass()))
            return false;
        final ParameterizedSQL other = (ParameterizedSQL) obj;
        return sql.equals(other.sql) && paramNames.equals(other.paramNames);
    }


    private static String transform(final String namedSql, final List<String> paramNames) {
        if (namedSql.indexOf('&') < 0 && namedSql.indexOf(':') < 0)
            return namedSql;
        int state = 0;
        final StringBuilder buf = new StringBuilder(namedSql.length());
        final StringBuilder pbuf = new StringBuilder();
        char pc = 0;
        for (int i = 0, len = namedSql.length(), lastPos = len - 1; i < len; i++) {
            final char c = namedSql.charAt(i);
            switch (state) {
                case 0: {
                    buf.append(c);
                    switch (c) {
                        case '\'': {
                            state = 1;
                            break;
                        }
                        case '\"': {
                            state = 2;
                            break;
                        }
                        case '-': {
                            if (pc == '-')
                                state = 3;
                            break;
                        }
                        case '*': {
                            if (pc == '/')
                                state = 4;
                            break;
                        }
                        case ':':
                        case '&': {
                            if ((!Character.isLetterOrDigit(pc)) && i < lastPos && Character.isJavaIdentifierStart(namedSql.charAt(i + 1))) {
                                state = 5;
                                buf.setCharAt(buf.length() - 1, '?');
                            }
                            break;
                        }
                        default: {
                        }
                    }
                    break;
                }
                case 1: {
                    if (c == '\'')
                        state = 0;
                    buf.append(c);
                    break;
                }
                case 2: {
                    if (c == '\"')
                        state = 0;
                    buf.append(c);
                    break;
                }
                case 3: {
                    if (c == '\n')
                        state = 0;
                    buf.append(c);
                    break;
                }
                case 4: {
                    if (c == '/' && pc == '*')
                        state = 0;
                    buf.append(c);
                    break;
                }
                case 5: {
                    if (Character.isJavaIdentifierPart(c)) {
                        pbuf.append(c);
                    } else {
                        paramNames.add(pbuf.toString());
                        pbuf.setLength(0);
                        buf.append(c);
                        state = 0;
                    }
                    break;
                }
            }
            pc = c;
        }
        if (state == 5)
            paramNames.add(pbuf.toString());
        return buf.toString();
    }
}
