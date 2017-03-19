package org.echosoft.framework.reports.model.providers;

import org.echosoft.common.collections.issuers.ReadAheadIssuer;
import org.echosoft.framework.reports.model.el.ELContext;
import org.echosoft.framework.reports.model.el.Expression;
import org.echosoft.framework.reports.processor.ExcelReportProcessor;
import org.echosoft.framework.reports.processor.ExecutionContext;
import org.echosoft.framework.reports.processor.ReportProcessingException;

/**
 * Поставщик данных, динамически определяемый в момент исполнения отчета.
 *
 * @author Anton Sharapov
 */
public class ProxyDataProvider implements DataProvider {

    private final String id;
    private final Expression ref;
    private transient DataProvider provider;

    public ProxyDataProvider(final String id, final Expression ref) {
        this.id = id;
        this.ref = ref;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ReadAheadIssuer getIssuer(final ELContext ctx) throws Exception {
        return resolveProvider(ctx).getIssuer(ctx);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    private DataProvider resolveProvider(final ELContext elctx) throws Exception {
        if (provider == null) {
            final Object v = ref.getValue(elctx);
            if (v instanceof DataProvider) {
                provider = (DataProvider)v;
            } else
            if (v instanceof String) {
                final String id = (String)v;
                final ExecutionContext ectx = (ExecutionContext)elctx.getVariables().get(ExcelReportProcessor.VAR_CONTEXT);
                provider = ectx.report.getProviders().get(id);
                if (provider == null)
                    throw new ReportProcessingException("Can't resolve data provider by id '" + id + "' from refrence " + ref);
            } else
                throw new Exception("Can't resolve data provider by reference " + ref);
        }
        return provider;
    }

}
