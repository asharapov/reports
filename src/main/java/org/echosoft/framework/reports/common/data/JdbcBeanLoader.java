package org.echosoft.framework.reports.common.data;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.echosoft.framework.reports.common.utils.StreamUtil;

/**
 * @author Anton Sharapov
 */
public final class JdbcBeanLoader<T> implements Loader<Map<String, Object>> {

    private final ResultSetMetaData meta;
    private final int cols;

    public JdbcBeanLoader(final ResultSet rs) throws SQLException {
        this.meta = rs.getMetaData();
        this.cols = meta.getColumnCount();
    }

    @Override
    public Map<String, Object> load(final ResultSet rs) throws SQLException, IOException {
        final HashMap<String, Object> result = new HashMap<>(cols);
        for (int i = 1; i <= cols; i++) {
            final String name = meta.getColumnLabel(i).toUpperCase();
            final Object value;
            switch (meta.getColumnType(i)) {
                case Types.BLOB: {
                    byte[] data = null;
                    final InputStream in = rs.getBinaryStream(i);
                    if (in != null) {
                        try {
                            data = StreamUtil.streamToBytes(in);
                        } finally {
                            in.close();
                        }
                    }
                    value = data;
                    break;
                }
                case Types.DATE:
                case Types.TIMESTAMP:
                case Types.TIMESTAMP_WITH_TIMEZONE:
                    value = rs.getTimestamp(i);
                    break;
                case Types.TIME:
                case Types.TIME_WITH_TIMEZONE:
                    value = rs.getTime(i);
                    break;
                default: {
                    value = rs.getObject(i);
                }
            }
            result.put(name, value);
        }
        return result;
    }
}
