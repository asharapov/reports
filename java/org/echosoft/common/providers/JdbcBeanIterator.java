package org.echosoft.common.providers;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.echosoft.common.utils.StreamUtil;

/**
 * @author Anton Sharapov
 */
final class JdbcBeanIterator<T> implements BeanIterator<T> {

    private final Connection conn;
    private final Statement stmt;
    private final ResultSet rs;
    private final BeanLoader<T> loader;
    private boolean nextCalculated;
    private boolean hasNextBean;
    private T nextBean;

    public JdbcBeanIterator(final Connection conn, final Statement stmt, final ResultSet rs) {
        this(conn, stmt, rs, new JdbcBeanLoader());
    }

    public JdbcBeanIterator(final Connection conn, final Statement stmt, final ResultSet rs, final BeanLoader<T> loader) {
        this.conn = conn;
        this.stmt = stmt;
        this.rs = rs;
        this.loader = loader;
        this.nextCalculated = false;
    }

    @Override
    public boolean hasNext() throws Exception {
        ensureNextCalcualated();
        return hasNextBean;
    }

    @Override
    public T next() throws Exception {
        ensureNextCalcualated();
        if (!hasNextBean)
            throw new NoSuchElementException();
        final T result = nextBean;
        nextBean = null;
        nextCalculated = false;
        return result;
    }

    @Override
    public T readAhead() throws Exception {
        ensureNextCalcualated();
        if (!hasNextBean)
            throw new NoSuchElementException();
        return nextBean;
    }

    @Override
    public void close() {
        if (rs != null)
            try {
                rs.close();
            } catch (Throwable th) {
                th.printStackTrace(System.err);
            }
        if (stmt != null)
            try {
                stmt.close();
            } catch (Throwable th) {
                th.printStackTrace(System.err);
            }
        if (conn != null)
            try {
                conn.close();
            } catch (Throwable th) {
                th.printStackTrace(System.err);
            }
    }

    protected void ensureNextCalcualated() throws Exception {
        if (!nextCalculated) {
            hasNextBean = rs.next();
            nextBean = hasNextBean ? loader.load(rs) : null;
            nextCalculated = true;
        }
    }


    public static final class JdbcBeanLoader implements BeanLoader {

        @Override
        public Map<String, Object> load(final ResultSet rs) throws SQLException, IOException {
            final ResultSetMetaData meta = rs.getMetaData();
            final int cols = meta.getColumnCount();
            final Map<String, Object> result = new HashMap<String, Object>(cols);
            for (int i = 1; i <= cols; i++) {
                final String name = meta.getColumnName(i).toUpperCase();
                final Object value;
                switch (meta.getColumnType(i)) {
                    case Types.BLOB: {
                        byte data[] = null;
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
                    case Types.DATE: {
                        value = rs.getTimestamp(i);
                        break;
                    }
                    default: {
                        value = rs.getObject(i);
                    }
                }
                result.put(name, value);
            }
            return result;
        }
    }
}
