package org.echosoft.framework.reports.model.providers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.echosoft.common.data.Query;
import org.echosoft.common.providers.DataProvider;
import org.echosoft.common.providers.ListDataProvider;
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


    public ListDataProviderHolder(final String id) {
        this.id = id;
        params = new HashMap<Expression, Expression>();
    }

    @Override
    public String getId() {
        return id;
    }

    public Expression getData() {
        return data;
    }
    public void setData(final Expression data) {
        this.data = data;
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
    public DataProvider getProvider(final ELContext ctx) {
        Object data;
        try {
            data = this.data.getValue(ctx);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (data instanceof Object[]) {
            return new ListDataProvider<Object>((Object[]) data);
        } else
        if (data instanceof Iterable) {
            return new ListDataProvider<Object>((List) data);
        } else
        if (data instanceof Iterator) {
            return new ListDataProvider<Object>((Iterator) data);
        } else
        if (data == null) {
            return new ListDataProvider();
        } else
            throw new RuntimeException("Invalid data type: " + data);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Query getQuery(final ELContext ctx) {
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
        final ListDataProviderHolder result = (ListDataProviderHolder) super.clone();
        result.params = new HashMap<Expression, Expression>(params.size());
        result.params.putAll(params);
        return result;
    }
}
