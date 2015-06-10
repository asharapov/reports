package org.echosoft.framework.reports;

import java.util.ArrayList;
import java.util.List;

import org.echosoft.framework.reports.data.beans.Company;
import org.echosoft.framework.reports.model.Report;
import org.echosoft.framework.reports.model.el.BaseExpression;
import org.echosoft.framework.reports.model.providers.ListDataProvider;
import org.echosoft.framework.reports.parser.ReportExtension;
import org.w3c.dom.Element;

/**
 * Пример расширения.
 */
public class TestReportExtension implements ReportExtension {

    @Override
    public boolean handleConfigElement(final Report report, final Element configElement) throws Exception {
        final String tagName = configElement.getTagName();
        if ("test-provider".equals(tagName)) {
            final ListDataProvider provider = new ListDataProvider("test");
            final List<Company> list = new ArrayList<>();
            list.add(new Company("1", "Intel, ink.", "John Doe"));
            list.add(new Company("2", "Apple, ink.", "Stive"));
            provider.setData(BaseExpression.makeExpression(list));
            report.getProviders().put("test", provider);
            return true;
        }
        return false;
    }

    @Override
    public void onConfig(final Report report) {
        System.out.println("Complete report " + report.getId() + " configuration with extension " + this);
    }
}
