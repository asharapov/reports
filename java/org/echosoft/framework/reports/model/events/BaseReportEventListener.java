package org.echosoft.framework.reports.model.events;

import org.echosoft.framework.reports.processor.ExecutionContext;

/**
 * @author Anton Sharapov
 */
public class BaseReportEventListener implements ReportEventListener {

    @Override
    public void beforeReport(final ExecutionContext ectx) throws Exception {
    }

    @Override
    public void beforeSheet(final ExecutionContext ectx) throws Exception {
    }

    @Override
    public void afterSheet(final ExecutionContext ectx) throws Exception {
    }

    @Override
    public void afterReport(final ExecutionContext ectx) throws Exception {
    }
}
