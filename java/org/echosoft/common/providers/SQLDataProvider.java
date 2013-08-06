package org.echosoft.common.providers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import javax.sql.DataSource;

import org.echosoft.common.collections.issuers.ReadAheadIssuer;
import org.echosoft.common.data.db.ParameterizedSQL;
import org.echosoft.common.data.db.Query;
import org.echosoft.common.data.db.SortCriterion;
import org.echosoft.framework.reports.model.providers.JdbcIssuer;

/**
 * Реализация {@link DataProvider} которая использует прямые обращения в реляционную БД за запрошенными данными.
 *
 * @author Anton Sharapov
 */
public class SQLDataProvider<T, Q extends Query> implements DataProvider<T, Q> {

    public static final int DEFAULT_FETCH_SIZE = 1000;

    private final DataSource dataSource;
    private final ParameterizedSQL psql;

    public SQLDataProvider(final DataSource dataSource, final String baseSQL) {
        if (dataSource == null)
            throw new NullPointerException("DataSource must be specified");
        if (baseSQL == null || baseSQL.trim().isEmpty())
            throw new NullPointerException("Query must be specified");

        this.dataSource = dataSource;
        this.psql = new ParameterizedSQL(baseSQL);
    }


    @Override
    public ReadAheadIssuer<T> execute(final Q query) throws Exception {
        String sql = psql.getQuery();
        final Map<String, Object> params;
        if (query != null) {
            if (query.hasSortCriteria()) {
                final StringBuilder buf = new StringBuilder(sql.length() + 64);
                buf.append("SELECT * FROM ( \n").append(sql).append(") \nORDER BY ");
                for (Iterator<SortCriterion> it = query.getSortCriteria().iterator(); it.hasNext(); ) {
                    final SortCriterion sc = it.next();
                    buf.append(sc.getField());
                    if (!sc.isAscending())
                        buf.append(" DESC");
                    if (it.hasNext())
                        buf.append(", ");
                }
                sql = buf.toString();
            }
            params = query.hasParams() ? query.getParams() : Collections.<String, Object>emptyMap();
        } else {
            params = Collections.emptyMap();
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            pstmt.setFetchDirection(ResultSet.FETCH_FORWARD);
            pstmt.setFetchSize(DEFAULT_FETCH_SIZE);

            psql.applyParams(pstmt, params);

            rs = pstmt.executeQuery();

            return new JdbcIssuer<T>(conn, pstmt, rs);
        } catch (Exception e) {
            if (rs != null)
                try {
                    rs.close();
                } catch (Throwable th) {
                    th.printStackTrace(System.err);
                }
            if (pstmt != null)
                try {
                    pstmt.close();
                } catch (Throwable th) {
                    th.printStackTrace(System.err);
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (Throwable th) {
                    th.printStackTrace(System.err);
                }

            throw e;
        }
    }
}
