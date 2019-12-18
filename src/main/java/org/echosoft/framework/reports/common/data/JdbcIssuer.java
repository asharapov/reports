package org.echosoft.framework.reports.common.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.NoSuchElementException;

import org.echosoft.framework.reports.common.collections.issuers.ReadAheadIssuer;

/**
 * Итератор по курсору данных читаемых из базы данных.<br/>
 * <strong>Важно!</strong> Особенностью данного класса является то что ответственность за освобождение ресурсов обеспечивающих работу с базой
 * здесь возложена на пользовательский код.<br/>
 * пример. использования:
 * <pre>
 *   final Issuer&lt;T&gt; it = dao.execute();
 *   try {
 *       while (it.hasNext()) {
 *           T bean = it.next();
 *           // ...
 *       }
 *   } finally {
 *       it.close();    // здесь закрывается соединение с базой !
 *   }
 *
 *   или, начиная с java 7:
 *
 *   try (Issuer&lt;T&gt; it = dao.execute()) {
 *       while (it.hasNext()) {
 *           T bean = it.next();
 *           // ...
 *       }
 *   }
 * </pre>
 *
 * @author Anton Sharapov
 */
public class JdbcIssuer<T> implements ReadAheadIssuer<T> {

    private final Connection conn;
    private final Statement stmt;
    private final ResultSet rs;
    private final Loader<T> loader;
    private T next;
    private boolean scanned;

    public JdbcIssuer(final Connection conn, final Statement stmt, final ResultSet rs, final Loader<T> loader) {
        this.conn = conn;
        this.stmt = stmt;
        this.rs = rs;
        this.loader = loader;
        this.next = null;
        this.scanned = false;
    }

    @Override
    public boolean hasNext() throws Exception {
        ensureScanned();
        return next != null;
    }

    @Override
    public T next() throws Exception {
        ensureScanned();
        if (next == null)
            throw new NoSuchElementException();
        final T result = next;
        next = null;
        scanned = false;
        return result;
    }

    @Override
    public T readAhead() throws Exception {
        ensureScanned();
        if (next == null)
            throw new NoSuchElementException();
        return next;
    }

    @Override
    public void close() throws Exception {
        try {
            rs.close();
        } finally {
            try {
                stmt.close();
            } finally {
                conn.close();
            }
        }
    }

    private void ensureScanned() throws Exception {
        if (!scanned) {
            next = rs.next() ? loader.load(rs) : null;
            scanned = true;
        }
    }

}
