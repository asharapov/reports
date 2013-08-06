package org.echosoft.framework.reports.model.providers;

import org.echosoft.common.collections.issuers.ReadAheadIssuer;
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

    public ListDataProviderHolder(final String id) {
        this.id = id;
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

    @Override
    public ReadAheadIssuer getIssuer(final ELContext ctx) throws Exception {
        final Object data = this.data != null ? this.data.getValue(ctx) : null;
        return Issuers.asIssuer(data);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
