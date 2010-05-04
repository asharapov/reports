package org.echosoft.framework.reports.model.providers;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.echosoft.common.query.Query;
import org.echosoft.common.query.QueryProcessor;
import org.echosoft.common.query.processors.JdbcBeanLoader;
import org.echosoft.common.query.processors.QueryProcessorFactory;
import org.echosoft.common.query.providers.DataProvider;
import org.echosoft.common.query.providers.SQLDataProvider;
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
    private Expression processor;
    private Expression sql;
    private Expression sqlref;
    private Expression filter;
    private Expression paramsMap;
    private Map<Expression, Expression> params;


    public SQLDataProviderHolder(String id) {
        this.id = id;
        params = new HashMap<Expression,Expression>();
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return id;
    }

    public Expression getDataSource() {
        return datasource;
    }
    public void setDataSource(Expression datasource) {
        this.datasource = datasource;
    }

    public Expression getProcessor() {
        return processor;
    }
    public void setProcessor(Expression processor) {
        this.processor = processor;
    }

    public Expression getSQL() {
        return sql;
    }
    public void setSQL(Expression sql) {
        this.sql = sql;
    }

    public Expression getSQLReference() {
        return sqlref;
    }
    public void setSQLReference(Expression sqlref) {
        this.sqlref = sqlref;
    }


    public Expression getFilter() {
        return filter;
    }
    public void setFilter(Expression filter) {
        this.filter = filter;
    }

    public Expression getParamsMap() {
        return paramsMap;
    }
    public void setParamsMap(Expression paramsMap) {
        this.paramsMap = paramsMap;
    }

    public void addParam(Expression name, Expression value) {
        if (name==null || value==null)
            throw new IllegalArgumentException("parameter key and value must be specified");
        this.params.put(name, value);
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public DataProvider getProvider(ELContext ctx) {
        try {
            final DataSource datasource = (DataSource)this.datasource.getValue(ctx);

            final QueryProcessor processor;
            final Object p = this.processor.getValue(ctx);
            if (p instanceof QueryProcessor) {
                processor = (QueryProcessor)p;
            } else
            if (p instanceof String) {
                processor = QueryProcessorFactory.getInstance().getProcessor( (String)p );
            } else
                throw new RuntimeException("Illegal processor type: "+p);

            final String sql;
            if (this.sql!=null) {
                sql = (String)this.sql.getValue(ctx);
            } else
            if (this.sqlref!=null) {
                final String ref = (String)this.sqlref.getValue(ctx);
                final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(ref);  //StreamUtil.getInputStream(ref);
                if (in==null)
                    throw new RuntimeException("Invalid sql reference: "+ref);
                try {
                    final ByteArrayOutputStream out = new ByteArrayOutputStream(128);
                    final byte[] c = new byte[1024];
                    for (int size=in.read(c); size>0; size=in.read(c))
                        out.write(c, 0, size);
                    sql = out.toString("utf-8");
                } finally {
                    in.close();
                }
            } else
                throw new RuntimeException("SQL not specified");

            return new SQLDataProvider(datasource, processor, new JdbcBeanLoader(), sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Query getQuery(ELContext ctx) {
        try {
            Query query = filter!=null ? (Query)filter.getValue(ctx) : null;
            if (query!=null) {
                return query;
            }

            query = new Query();
            if (paramsMap!=null) {
                final Map<String,Object> params = (Map<String,Object>)paramsMap.getValue(ctx);
                if (params!=null) {
                    query.getNamedParams().putAll(params);
                }
            }
            for (Map.Entry<Expression,Expression> e : params.entrySet()) {
                query.getNamedParams().put((String)e.getKey().getValue(ctx), e.getValue().getValue(ctx));
            }
            return query;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        final SQLDataProviderHolder result = (SQLDataProviderHolder)super.clone();
        result.params = new HashMap<Expression, Expression>(params.size());
        result.params.putAll(params);
        return result;
    }

}
