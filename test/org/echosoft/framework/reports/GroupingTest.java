package org.echosoft.framework.reports;

import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.echosoft.framework.reports.model.Report;
import org.echosoft.framework.reports.model.el.ELContext;
import org.echosoft.framework.reports.registry.ReportsRegistry;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class GroupingTest {

    @Test
    public void testReport5() throws Exception {
        final Report report = TestUtils.loadReport("test-grp-1");

        final ELContext ctx = new ELContext();
        ctx.getEnvironment().put("invoices", TestUtils.loadInvoices("report1-ds1.xml"));
        ctx.getEnvironment().put("payments", TestUtils.loadPayments("report1-ds2.xml"));

        final long started = System.currentTimeMillis();
        final Workbook result = ReportsRegistry.getDefaultProcessor().process(report, ctx);
        System.out.println("report " + report.getId() + " generated for a " + (System.currentTimeMillis() - started) + " ms.");

        final FileOutputStream out = new FileOutputStream("result-grp-1.xlsx");
        result.write(out);
        out.close();
    }

}

