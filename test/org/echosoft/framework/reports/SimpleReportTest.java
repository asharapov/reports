package org.echosoft.framework.reports;

import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.echosoft.common.utils.StringUtil;
import org.echosoft.framework.reports.model.ColumnGroupModel;
import org.echosoft.framework.reports.model.Report;
import org.echosoft.framework.reports.model.SheetModel;
import org.echosoft.framework.reports.model.el.ELContext;
import org.echosoft.framework.reports.registry.ReportsRegistry;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class SimpleReportTest {

    @Test
    public void testReport1() throws Exception {
        final Report report = TestUtils.loadReport("report1");
        final SheetModel sheet1 = report.findSheetById("sheet1");
        sheet1.addColumnGroup(new ColumnGroupModel(4, 7));
        sheet1.addColumnGroup(new ColumnGroupModel(5, 6));

        final ELContext ctx = new ELContext();
        ctx.getEnvironment().put("company", "Рога и Копыта");
        ctx.getEnvironment().put("fromDate", StringUtil.parseDate("01.04.2008"));
        ctx.getEnvironment().put("toDate", StringUtil.parseDate("25.08.2008"));
        ctx.getEnvironment().put("author", "Anton Sharapov");
        ctx.getEnvironment().put("invoices", TestUtils.loadInvoices("report1-ds1.xml"));
        ctx.getEnvironment().put("payments", TestUtils.loadPayments("report1-ds2.xml"));

        long started = System.currentTimeMillis();
        final Workbook result1 = ReportsRegistry.getDefaultProcessor().process(report, ctx);
        System.out.println(report.getId() + " generated for a " + (System.currentTimeMillis() - started) + " ms. in XLS format");
        final FileOutputStream out1 = new FileOutputStream("result-1.xls");
        result1.write(out1);
        out1.close();

        started = System.currentTimeMillis();
        report.setTarget(Report.TargetType.XSSF);
        final Workbook result2 = ReportsRegistry.getDefaultProcessor().process(report, ctx);
        System.out.println(report.getId() + " generated for a " + (System.currentTimeMillis() - started) + " ms. in XLSX format");
        final FileOutputStream out2 = new FileOutputStream("result-1.xlsx");
        result2.write(out2);
        out2.close();
    }

    @Test
    public void testReport2() throws Exception {
        final Report report = TestUtils.loadReport("report2");
        final ELContext ctx = new ELContext();
        ctx.getEnvironment().put("company", "Сукинъ и Сыновья");
        ctx.getEnvironment().put("fromDate", StringUtil.parseDate("01.04.2008"));
        ctx.getEnvironment().put("toDate", StringUtil.parseDate("25.08.2008"));
        ctx.getEnvironment().put("activities", TestUtils.loadPayments("report1-ds3.xml"));

        final Workbook result = ReportsRegistry.getDefaultProcessor().process(report, ctx);
        final FileOutputStream out = new FileOutputStream("result-2.xls");
        result.write(out);
        out.close();
    }

    @Test
    public void testReport3() throws Exception {
        final Report report = TestUtils.loadReport("report3");
        final ELContext ctx = new ELContext();
        ctx.getEnvironment().put("company", "Сукинъ и Сыновья");
        ctx.getEnvironment().put("activities", TestUtils.loadPayments("report1-ds3.xml"));

        final Workbook result = ReportsRegistry.getDefaultProcessor().process(report, ctx);
        final FileOutputStream out = new FileOutputStream("result-3.xls");
        result.write(out);
        out.close();
    }

    @Test
    public void testReport4() throws Exception {
        final Report report = TestUtils.loadReport("report4");

        final ELContext ctx = new ELContext();
        ctx.getEnvironment().put("company", "Рога и Копыта");
        ctx.getEnvironment().put("fromDate", StringUtil.parseDate("01.04.2008"));
        ctx.getEnvironment().put("toDate", StringUtil.parseDate("25.08.2008"));
        ctx.getEnvironment().put("author", "Anton Sharapov");
        ctx.getEnvironment().put("invoices", TestUtils.loadInvoices("report1-ds1.xml"));
        ctx.getEnvironment().put("payments", TestUtils.loadPayments("report1-ds2.xml"));

        final long started = System.currentTimeMillis();
        final Workbook result = ReportsRegistry.getDefaultProcessor().process(report, ctx);
        System.out.println(report.getId() + " generated for a " + (System.currentTimeMillis() - started) + " ms.");

        final FileOutputStream out = new FileOutputStream("result-4.xlsx");
        result.write(out);
        out.close();
    }

    @Test
    public void testImageWithMergedRegions() throws Exception {
        final Report report = TestUtils.loadReport("imageWithMergedRegions");

        final ELContext ctx = new ELContext();

        final long started = System.currentTimeMillis();
        final Workbook result = ReportsRegistry.getDefaultProcessor().process(report, ctx);
        System.out.println(report.getId() + " generated for a " + (System.currentTimeMillis() - started) + " ms.");

        final FileOutputStream out = new FileOutputStream("imageWithMergedRegionsResult.xlsx");
        result.write(out);
        out.close();
    }

    @Test
    public void testReport5() throws Exception {
        final Report report = TestUtils.loadReport("test-grp-1");

        final ELContext ctx = new ELContext();
        ctx.getEnvironment().put("invoices", TestUtils.loadInvoices("report1-ds1.xml"));
        ctx.getEnvironment().put("payments", TestUtils.loadPayments("report1-ds2.xml"));

        final long started = System.currentTimeMillis();
        final Workbook result = ReportsRegistry.getDefaultProcessor().process(report, ctx);
        System.out.println(report.getId() + " generated for a " + (System.currentTimeMillis() - started) + " ms.");

        final FileOutputStream out = new FileOutputStream("result-grp-1.xlsx");
        result.write(out);
        out.close();
    }

}
