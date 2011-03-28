package org.echosoft.framework.reports;

import javax.sql.DataSource;
import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.echosoft.common.utils.StringUtil;
import org.echosoft.framework.reports.model.ColumnGroup;
import org.echosoft.framework.reports.model.Report;
import org.echosoft.framework.reports.model.Sheet;
import org.echosoft.framework.reports.model.el.ELContext;
import org.echosoft.framework.reports.registry.ReportsRegistry;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.ds.PGPoolingDataSource;

/**
 * @author Anton Sharapov
 */
public class SimpleReportTest {

    private static DataSource ds;

    @BeforeClass
    public static void beforeClass() throws Exception {
        final PGPoolingDataSource pds = new PGPoolingDataSource();
        pds.setServerName( "localhost" );
        pds.setPortNumber( 5432 );
        pds.setDatabaseName( "webui_tests" );
        pds.setUser( "anton" );
        pds.setPassword( "anton" );
        ds = pds;
    }

    @Test
    public void testReport1() throws Exception {
        final Report report = TestUtils.loadReport("report1");
        final Sheet sheet1 = report.findSheetById("sheet1");
        sheet1.addColumnGroup(new ColumnGroup(4, 7));
        sheet1.addColumnGroup(new ColumnGroup(5, 6));

        final ELContext ctx = new ELContext();
        ctx.getEnvironment().put("company", "Рога и Копыта");
        ctx.getEnvironment().put("fromDate", StringUtil.parseDate("01.04.2008"));
        ctx.getEnvironment().put("toDate", StringUtil.parseDate("25.08.2008"));
        ctx.getEnvironment().put("author", "Anton Sharapov");
        ctx.getEnvironment().put("invoices", TestUtils.loadInvoices("report1-ds1.xml"));
        ctx.getEnvironment().put("payments", TestUtils.loadPayments("report1-ds2.xml"));

        final long started = System.currentTimeMillis();
        final Workbook result = ReportsRegistry.getDefaultProcessor().process(report, ctx);
        System.out.println("report " + report.getId() + " generated for a " + (System.currentTimeMillis() - started) + " ms.");

        for (int i = 0; i < result.getNumberOfSheets(); i++) {
            System.out.println("sheet <" + result.getSheetName(i) + "> has landscape orientation "
                    + result.getSheetAt(i).getPrintSetup().getLandscape());
        }


        final FileOutputStream out = new FileOutputStream("result-1.xls");
        result.write(out);
        out.close();
    }

    @Test
    public void testReport2() throws Exception {
        final Report report = TestUtils.loadReport("report2");
        final ELContext ctx = new ELContext();
        ctx.getEnvironment().put("datasource", ds);
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
        ctx.getEnvironment().put("datasource", ds);
        ctx.getEnvironment().put("company", "Сукинъ и Сыновья");
        ctx.getEnvironment().put("activities", TestUtils.loadPayments("report1-ds3.xml"));

        final Workbook result = ReportsRegistry.getDefaultProcessor().process(report, ctx);
        final FileOutputStream out = new FileOutputStream("result-3.xls");
        result.write(out);
        out.close();
    }
}
