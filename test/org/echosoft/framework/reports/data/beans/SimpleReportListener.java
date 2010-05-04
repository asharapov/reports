package org.echosoft.framework.reports.data.beans;

import org.echosoft.framework.reports.model.events.ReportEventListener;
import org.echosoft.framework.reports.processor.ExecutionContext;

/**
 * @author Anton Sharapov
 */
public class SimpleReportListener implements ReportEventListener {

    public void beforeReport(ExecutionContext ectx) {
        System.out.println("before report "+ectx.report.getId());
    }

    public void beforeSheet(ExecutionContext ectx) {
        System.out.println("\tbefore sheet "+ectx.sheet.getId());
    }

    public void afterSheet(ExecutionContext ectx) {
        System.out.println("\tafter sheet "+ectx.sheet.getId());
    }

    public void afterReport(ExecutionContext ectx) {
        System.out.println("after report "+ectx.report.getId());
    }


}
