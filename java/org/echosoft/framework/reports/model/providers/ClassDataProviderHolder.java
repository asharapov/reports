package org.echosoft.framework.reports.model.providers;

import java.util.HashMap;
import java.util.Map;

import org.echosoft.common.data.Query;
import org.echosoft.common.providers.ClassDataProvider;
import org.echosoft.common.providers.DataProvider;
import org.echosoft.common.utils.ObjectUtil;
import org.echosoft.framework.reports.model.el.ELContext;
import org.echosoft.framework.reports.model.el.Expression;

/**
 * Предназначен для динамического конструирования поставщиков данных на основе контекста выполнения отчета.
 *
 * @author Anton Sharapov
 */
public class ClassDataProviderHolder implements DataProviderHolder {

    private final String id;
    private Expression object;
    private Expression methodName;
    private Expression filter;
    private Expression paramsMap;
    private Map<Expression, Expression> params;

    public ClassDataProviderHolder(final String id) {
        this.id = id;
        params = new HashMap<Expression, Expression>();
    }

    @Override
    public String getId() {
        return id;
    }

    public Expression getObject() {
        return object;
    }
    public void setObject(final Expression object) {
        this.object = object;
    }

    public Expression getMethodName() {
        return methodName;
    }
    public void setMethodName(final Expression methodName) {
        this.methodName = methodName;
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


    @Override
    public DataProvider getProvider(final ELContext ctx) {
        try {
            Object object = this.object.getValue(ctx);
            if (object instanceof String) {
                object = ObjectUtil.makeInstance((String) object, Object.class);
            }
            String method = (String) this.methodName.getValue(ctx);
            return new ClassDataProvider(object, method);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        final ClassDataProviderHolder result = (ClassDataProviderHolder) super.clone();
        result.params = new HashMap<Expression, Expression>(params.size());
        result.params.putAll(params);
        return result;
    }
}
