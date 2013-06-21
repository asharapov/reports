package org.echosoft.common.providers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.NoSuchElementException;

/**
 * @author Anton Sharapov
 */
public final class JdbcBeanIterator<T> implements BeanIterator<T> {

    private final Connection conn;
    private final Statement stmt;
    private final ResultSet rs;
    private final BeanLoader<T> loader;
    private boolean nextCalculated;
    private boolean hasNextBean;
    private T nextBean;

    public JdbcBeanIterator(final Connection conn, final Statement stmt, final ResultSet rs, final BeanLoader<T> loader) {
        if (conn==null || stmt==null || rs==null || loader==null)
            throw new IllegalArgumentException("All arguments must be specified");
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
        if (rs!=null)
        try {
            rs.close();
        } catch (Throwable th) { th.printStackTrace(System.err); }
        if (stmt!=null)
        try {
            stmt.close();
        } catch (Throwable th) { th.printStackTrace(System.err); }
        if (conn!=null)
        try {
            conn.close();
        } catch (Throwable th) { th.printStackTrace(System.err); }
    }

    protected void ensureNextCalcualated() throws Exception {
        if (!nextCalculated) {
            hasNextBean = rs.next();
            nextBean = hasNextBean ? loader.load(rs) : null;
            nextCalculated = true;
        }
    }
}
