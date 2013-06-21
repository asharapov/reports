package org.echosoft.framework.reports.model.providers;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

import org.echosoft.common.data.Query;
import org.echosoft.common.providers.DataProvider;
import org.echosoft.common.providers.SQLDataProvider;
import org.echosoft.framework.reports.model.el.ELContext;
import org.echosoft.framework.reports.model.el.Expression;

/**
 * Предназначен для динамического конструирования поставщиков данных на основе контекста выполнения отчета.
 *
 * @author Anton Sharapov
 */
public class SQLDataProviderHolder implements DataProviderHolder {

    private final String id;
    private Expression datasource;
    private Expression sql;
    private Expression sqlref;
    private Expression filter;
    private Expression paramsMap;
    private Map<Expression, Expression> params;


    public SQLDataProviderHolder(final String id) {
        this.id = id;
        params = new HashMap<Expression, Expression>();
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


    public Expression getFilter() {
        return filter;
    }
    public void setFilter(final Expression filter) {
        this.filter = filter;
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


    @SuppressWarnings("unchecked")
    @Override
    public DataProvider getProvider(ELContext ctx) {
        try {
            final DataSource datasource = (DataSource) this.datasource.getValue(ctx);

            final String sql;
            if (this.sql != null) {
                sql = (String) this.sql.getValue(ctx);
            } else if (this.sqlref != null) {
                final String ref = (String) this.sqlref.getValue(ctx);
                final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(ref);  //StreamUtil.getInputStream(ref);
                if (in == null)
                    throw new RuntimeException("Invalid sql reference: " + ref);
                try {
                    final ByteArrayOutputStream out = new ByteArrayOutputStream(128);
                    final byte[] c = new byte[1024];
                    for (int size = in.read(c); size > 0; size = in.read(c))
                        out.write(c, 0, size);
                    sql = out.toString("utf-8");
                } finally {
                    in.close();
                }
            } else
                throw new RuntimeException("SQL not specified");

            return new SQLDataProvider(datasource, sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Query getQuery(ELContext ctx) {
        try {
            Query query = filter != null ? (Query) filter.getValue(ctx) : null;
            if (query != null) {
                return query;
            }

            query = new Query();
            if (paramsMap != null) {
                final Map<String, Object> params = (Map<String, Object>) paramsMap.getValue(ctx);
                if (params != null) {
                    query.addParams(params);
                }
            }
            for (Map.Entry<Expression, Expression> e : params.entrySet()) {
                query.addParam((String) e.getKey().getValue(ctx), e.getValue().getValue(ctx));
            }
            return query;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        final SQLDataProviderHolder result = (SQLDataProviderHolder) super.clone();
        result.params = new HashMap<Expression, Expression>(params.size());
        result.params.putAll(params);
        return result;
    }
}
