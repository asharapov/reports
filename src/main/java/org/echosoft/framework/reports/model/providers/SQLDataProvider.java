package org.echosoft.framework.reports.model.providers;

import javax.sql.DataSource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.echosoft.framework.reports.common.collections.issuers.ReadAheadIssuer;
import org.echosoft.framework.reports.common.data.JdbcBeanLoader;
import org.echosoft.framework.reports.common.data.JdbcIssuer;
import org.echosoft.framework.reports.common.data.ParameterizedSQL;
import org.echosoft.framework.reports.common.utils.StreamUtil;
import org.echosoft.framework.reports.model.el.ELContext;
import org.echosoft.framework.reports.model.el.Expression;
import org.echosoft.framework.reports.processor.ReportProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Предназначен для динамического конструирования поставщиков данных на основе контекста выполнения отчета.
 *
 * @author Anton Sharapov
 */
public class SQLDataProvider implements DataProvider {

    private static final Logger log = LoggerFactory.getLogger(SQLDataProvider.class);
    public static final int DEFAULT_FETCH_SIZE = 1000;

    private final String id;
    private Expression datasource;
    private Expression sql;
    private Expression sqlref;
    private Expression paramsMap;
    private Map<Expression, Expression> params;


    public SQLDataProvider(final String id) {
        this.id = id;
        params = new HashMap<>();
    }

    @Override
    public String getId() {
        return id;
    }

    public Expression getDataSource() {
        return datasource;
    }

    public void setDataSource(final Expression datasource) {
        this.datasource = datasource;
    }

    public Expression getSQL() {
        return sql;
    }

    public void setSQL(final Expression sql) {
        this.sql = sql;
    }

    public Expression getSQLReference() {
        return sqlref;
    }

    public void setSQLReference(final Expression sqlref) {
        this.sqlref = sqlref;
    }


    public Expression getParamsMap() {
        return paramsMap;
    }

    public void setParamsMap(final Expression paramsMap) {
        this.paramsMap = paramsMap;
    }

    public void addParam(final Expression name, final Expression value) {
        if (name == null || value == null)
            throw new IllegalArgumentException("parameter key and value must be specified");
        this.params.put(name, value);
    }


    @Override
    public ReadAheadIssuer getIssuer(final ELContext ctx) throws Exception {
        Object tmp = this.datasource != null ? this.datasource.getValue(ctx) : null;
        if (!(tmp instanceof DataSource))
            throw new ReportProcessingException("Invalid datasource type: " + tmp);
        final DataSource ds = (DataSource) tmp;

        final String sql;
        tmp = this.sql != null ? this.sql.getValue(ctx) : null;
        if (tmp instanceof String) {
            sql = (String) tmp;
        } else if (tmp != null) {
            throw new ReportProcessingException("Can't resolve SQL from object: " + tmp);
        } else {
            tmp = this.sqlref != null ? this.sqlref.getValue(ctx) : null;
            if (!(tmp instanceof String))
                throw new ReportProcessingException("Invalid sql URL: " + tmp);
            final String url = (String) tmp;
            final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(url);
            if (in == null)
                throw new ReportProcessingException("Can't resolve sql by URL: " + url);
            try {
                final byte[] data = StreamUtil.streamToBytes(in);
                sql = new String(data, StandardCharsets.UTF_8);
            } finally {
                in.close();
            }
        }

        final Map<String, Object> params;
        tmp = this.paramsMap != null ? this.paramsMap.getValue(ctx) : null;
        if (tmp != null && !(tmp instanceof Map))
            throw new ReportProcessingException("Can't resolve sql params (should be java.util.Map) from: " + tmp);
        params = tmp != null ? (Map<String, Object>) tmp : new HashMap<String, Object>();
        for (Map.Entry<Expression, Expression> e : this.params.entrySet()) {
            tmp = e.getKey() != null ? e.getKey().getValue(ctx) : null;
            final String name = tmp != null ? tmp.toString() : null;
            tmp = e.getValue() != null ? e.getValue().getValue(ctx) : null;
            params.put(name, tmp);
        }

        return getIssuer(ds, sql, params);
    }

    private ReadAheadIssuer getIssuer(final DataSource ds, final String sql, final Map<String, Object> params) throws Exception {
        final ParameterizedSQL psql = new ParameterizedSQL(sql);
        if (log.isDebugEnabled()) {
            log.debug("Issuer query: \n" + psql.compileNonParameterizedQuery(params));
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = ds.getConnection();
            pstmt = conn.prepareStatement(psql.getQuery(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            pstmt.setFetchDirection(ResultSet.FETCH_FORWARD);
            pstmt.setFetchSize(DEFAULT_FETCH_SIZE);
            psql.applyParams(pstmt, params);
            rs = pstmt.executeQuery();
            return new JdbcIssuer<>(conn, pstmt, rs, new JdbcBeanLoader<>(rs));
        } catch (Exception e) {
            if (rs != null)
                try {
                    rs.close();
                } catch (Throwable th) {
                    log.error(th.getMessage(), th);
                }
            if (pstmt != null)
                try {
                    pstmt.close();
                } catch (Throwable th) {
                    log.error(th.getMessage(), th);
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (Throwable th) {
                    log.error(th.getMessage(), th);
                }
            throw e;
        }
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        final SQLDataProvider result = (SQLDataProvider) super.clone();
        result.params = new HashMap<>(params.size());
        result.params.putAll(params);
        return result;
    }
}
