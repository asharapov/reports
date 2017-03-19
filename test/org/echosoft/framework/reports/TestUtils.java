package org.echosoft.framework.reports;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.echosoft.common.utils.Any;
import org.echosoft.common.utils.StringUtil;
import org.echosoft.common.utils.XMLUtil;
import org.echosoft.framework.reports.data.beans.Activity;
import org.echosoft.framework.reports.data.beans.Company;
import org.echosoft.framework.reports.data.beans.Invoice;
import org.echosoft.framework.reports.data.beans.Payment;
import org.echosoft.framework.reports.data.beans.Project;
import org.echosoft.framework.reports.model.Report;
import org.echosoft.framework.reports.parser.ReportExtension;
import org.echosoft.framework.reports.parser.ReportModelParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Ряд вспомогательных методов используемых при тестировании отчетов.
 *
 * @author Anton Sharapov
 */
public class TestUtils {

    private static final String DOCS_PATH = "/org/echosoft/framework/reports/data/";

    public static InputStream openFileStream(final String docName) {
        return TestUtils.class.getResourceAsStream(DOCS_PATH + docName);
    }

    public static HSSFWorkbook loadExcelDocument(final String docName) throws IOException {
        final InputStream in = openFileStream(docName);
        return in != null ? new HSSFWorkbook(in) : null;
    }

    public static Report loadReport(final String reportName) throws Exception {
        final List<ReportExtension> extensions = new ArrayList<>();
        extensions.add(new TestReportExtension());

        InputStream wb = TestUtils.openFileStream(reportName + ".xls");
        if (wb == null)
            wb = TestUtils.openFileStream(reportName + ".xlsx");
        final InputStream cfg = TestUtils.openFileStream(reportName + ".xml");
        return ReportModelParser.parse(wb, cfg, extensions);
    }

    public static List<Invoice> loadInvoices(final String dsName) throws Exception {
        final List<Invoice> result = new ArrayList<Invoice>();
        final Document doc = XMLUtil.loadDocument(openFileStream(dsName));
        final Element root = doc.getDocumentElement();
        for (Element element : XMLUtil.getChildElements(root)) {
            final String tagName = element.getTagName();
            if ("invoice".equals(tagName)) {
                result.add(parseInvoice(element));
            } else
                throw new RuntimeException("Unsupported element: " + tagName);
        }
        return result;
    }

    public static List<Payment> loadPayments(final String dsName) throws Exception {
        final List<Payment> result = new ArrayList<Payment>();
        final Document doc = XMLUtil.loadDocument(openFileStream(dsName));
        final Element root = doc.getDocumentElement();
        for (Element element : XMLUtil.getChildElements(root)) {
            final String tagName = element.getTagName();
            if ("activity".equals(tagName)) {
                parseActivity(result, element);
            } else
                throw new RuntimeException("Unsupported element: " + tagName);
        }
        return result;
    }

    private static void parseActivity(final List<Payment> result, final Element element) {
        final String id = StringUtil.trim(element.getAttribute("id"));
        final String pid = StringUtil.trim(element.getAttribute("pid"));
        final String name = StringUtil.trim(element.getAttribute("name"));
        final int level = Any.asInt(StringUtil.trim(element.getAttribute("level")));
        final Activity activity = new Activity(id, pid, name, level);
        for (Element el : XMLUtil.getChildElements(element)) {
            final String tagName = el.getTagName();
            if ("activity".equals(tagName)) {
                parseActivity(result, el);
            } else if ("company".equals(tagName)) {
                parseCompany(result, activity, el);
            } else
                throw new RuntimeException("Unsupported element: " + tagName);
        }
    }

    private static void parseCompany(final List<Payment> result, final Activity activity, final Element element) {
        final String id = StringUtil.trim(element.getAttribute("id"));
        final String name = StringUtil.trim(element.getAttribute("name"));
        final String director = StringUtil.trim(element.getAttribute("director"));
        final Company company = new Company(id, name, director);
        for (Element el : XMLUtil.getChildElements(element)) {
            final String tagName = el.getTagName();
            if ("project".equals(tagName)) {
                parseProject(result, activity, company, el);
            } else
                throw new RuntimeException("Unsupported element: " + tagName);
        }
    }

    private static void parseProject(final List<Payment> result, final Activity activity, final Company company, final Element element) {
        final String id = StringUtil.trim(element.getAttribute("id"));
        final String name = StringUtil.trim(element.getAttribute("name"));
        final boolean active = Any.asBoolean(StringUtil.trim(element.getAttribute("active")));
        final Date started = asDate(StringUtil.trim(element.getAttribute("started")));
        final Project project = new Project(id, name, active, started);
        for (Element el : XMLUtil.getChildElements(element)) {
            final String tagName = el.getTagName();
            if ("invoice".equals(tagName)) {
                final Invoice invoice = parseInvoice(el);
                result.add(new Payment(activity, company, project, invoice));
            } else
                throw new RuntimeException("Unsupported element: " + tagName);
        }
    }

    private static Invoice parseInvoice(final Element element) {
        final String id = StringUtil.trim(element.getAttribute("id"));
        final String name = StringUtil.trim(element.getAttribute("name"));
        final String contragent = StringUtil.trim(element.getAttribute("contragent"));
        final String invoice = StringUtil.trim(element.getAttribute("invoice"));
        final int amount = Any.asInt(StringUtil.trim(element.getAttribute("amount")), 0);
        final double unitcost = Any.asDouble(StringUtil.trim(element.getAttribute("unitcost")), 0d);
        return new Invoice(id, name, contragent, invoice, amount, unitcost);
    }

    private static Date asDate(final String str) {
        try {
            return StringUtil.parseDate(str);
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
