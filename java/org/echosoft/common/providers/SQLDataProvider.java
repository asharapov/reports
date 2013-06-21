package org.echosoft.common.providers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import javax.sql.DataSource;

import org.echosoft.common.data.Query;
import org.echosoft.common.data.SortCriterion;
import org.echosoft.common.data.sql.ParameterizedSQL;

/**
 * Implementation of the {@link DataProvider} which uses direct queries to a number of supported RDBMS.
 *
 * @author Anton Sharapov
 */
public class SQLDataProvider<T> implements DataProvider {

    public static final int DEFAULT_FETCH_SIZE = 1000;

    private final DataSource dataSource;
    private final ParameterizedSQL psql;

    /**
     * Creates new instance of the sql data provider.
     * @param dataSource  connections data source. Must be specified.
     * @param baseSQL  base sql expression.
     */
    public SQLDataProvider(final DataSource dataSource, final String baseSQL) {
        if (dataSource==null)
            throw new NullPointerException("DataSource must be specified");
        if (baseSQL == null || baseSQL.trim().isEmpty())
            throw new NullPointerException("Query must be specified");

        this.dataSource = dataSource;
        this.psql = new ParameterizedSQL(baseSQL);
    }


    /**
     * Returns queried rows from the data provider.
     * @param query  optional parameter which can be add additional constraints, sorting rules
     *               or paging support for retrieved data.
     * @return range of the sorted records from the data set.
     * @throws DataProviderException  in case if any errors occurs.
     */
    public BeanIterator<T> execute(final Query query) {
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

            final BeanLoader<T> loader = new JdbcBeanLoader(rs.getMetaData());

            return new JdbcBeanIterator<T>(conn, pstmt, rs, loader);

        } catch (SQLException e) {
            if (rs!=null)
            try {
                rs.close();
            } catch (Throwable th) { th.printStackTrace(System.err); }
            if (pstmt!=null)
            try {
                pstmt.close();
            } catch (Throwable th) { th.printStackTrace(System.err); }
            if (conn!=null)
            try {
                conn.close();
            } catch (Throwable th) { th.printStackTrace(System.err); }

            throw new DataProviderException(e.getMessage(), e);
        }
    }

}
