package org.echosoft.framework.reports.model.providers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.echosoft.common.query.Query;
import org.echosoft.common.query.providers.DataProvider;
import org.echosoft.common.query.providers.ListDataProvider;
import org.echosoft.framework.reports.model.el.ELContext;
import org.echosoft.framework.reports.model.el.Expression;

/**
 * Предназначен для динамического конструирования поставщиков данных на основе контекста выполнения отчета.
 * 
 * @author Anton Sharapov
 */
public class ListDataProviderHolder implements DataProviderHolder {

    private final String id;
    private Expression data;
    private Expression filter;
    private Expression paramsMap;
    private Map<Expression, Expression> params;


    public ListDataProviderHolder(String id) {
        this.id = id;
        params = new HashMap<Expression,Expression>();
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return id;
    }

    public Expression getData() {
        return data;
    }
    public void setData(Expression data) {
        this.data = data;
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
        Object data;
        try {
            data = this.data.getValue(ctx);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (data instanceof Object[]) {
            return new ListDataProvider<Object>((Object[])data);
        } else
        if (data instanceof List) {
            return new ListDataProvider<Object>((List)data);
        } else
        if (data instanceof Iterator) {
            return new ListDataProvider<Object>((Iterator)data);
        } else
        if (data == null) {
            return new ListDataProvider();
        } else
            throw new RuntimeException("Invalid data type: "+data);
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
        final ListDataProviderHolder result = (ListDataProviderHolder)super.clone();
        result.params = new HashMap<Expression, Expression>(params.size());
        result.params.putAll(params);
        return result;
    }

}
