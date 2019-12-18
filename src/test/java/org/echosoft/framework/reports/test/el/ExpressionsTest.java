package org.echosoft.framework.reports.test.el;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import org.echosoft.framework.reports.common.utils.StringUtil;
import org.echosoft.framework.reports.model.el.BaseExpression;
import org.echosoft.framework.reports.model.el.ELContext;
import org.echosoft.framework.reports.model.el.Expression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Anton Sharapov
 */
public class ExpressionsTest {

    private static final Company[] COMPANIES = new Company[]{
            new Company("RAO EES", "Moscow", new Employee[]{
                    new Employee("ivanov", "accountant", asDate("12.03.1967")),
                    new Employee("petrov", "sale manager", asDate("27.06.1973")),
                    new Employee("sidorov", "director", asDate("28.12.1971")),
                    new Employee("pupkin", null, asDate("15.09.1979"))
            }),
            new Company("FSK", "Moscow, 1", new Employee[]{
                    new Employee("chuvaev", "programmer", asDate("23.09.1976")),
                    new Employee("sigaev", "programmer", asDate("06.11.1983")),
            })
    };

    private static final Object[][] VALID_TESTS = {
            // test static content
            {null, null},
            {true, true},
            {23, 23},
            {asDate("01.01.2008"), asDate("01.01.2008")},
            {"bla-bla-bla", "bla-bla-bla"},
            {"@{row:name", "@{row:name"},
            // test expressions evaluations
            {"${row:name}", "RAO EES"},
            {"my first company - ${row:name}", "my first company - RAO EES"},
            {"${var:row}:${env:host}:${row:name}:${const:forever!}", "1:localhost:RAO EES:forever!"},
            {"${employeeCount}", 4},
            {"${row:employee[2].name}", "sidorov"},
            {"${row:getEmployeeByName(petrov).title}", "sale manager"},
            {"${row:getEmployeeByName(petrov).born}", asDate("27.06.1973")},
            {"Petrov's born date: ${(date,dd.MM.yyyy)row:getEmployeeByName(petrov).born}", "Petrov's born date: 27.06.1973"},
            {"Ivanov's title: ${row:getEmployeeByName(ivanov).title | const:<unknown>}", "Ivanov's title: accountant"},
            {"Pupkin's title: ${row:employeeByName(pupkin).title | const:<unknown>}", "Pupkin's title: <unknown>"},
            {"${name}", "RAO EES"},
            {"${row}", 1},
            {"${env:row}", "-"},
            {"${host}", "localhost"},
            {"${bla-bla-bla}", null},
            {"${(date,dd.MM.yyyy)env:from} - ${(date,dd.MM.yyyy)env:to}", "01.01.2008 - 25.12.2008"}
    };

    private static final String[] INVALID_TESTS = {
            //"${}", "${()}",
            "${(row:name}", "${xxxx:name}", "${:}", "${row:}"
    };

    private ELContext context;

    @BeforeEach
    void setUp() {
        context = new ELContext(new Locale("ru", "RU"), null);
        context.getVariables().put("row", 1);
        context.getEnvironment().put("host", "localhost");
        context.getEnvironment().put("row", "-");
        context.getEnvironment().put("from", asDate("01.01.2008"));
        context.getEnvironment().put("to", asDate("25.12.2008"));
        context.setRowModel(COMPANIES[0]);
    }


    @Test
    void testValidTests() throws Exception {
        for (Object[] tests : VALID_TESTS) {
            final Expression expr = new BaseExpression(tests[0]);
            final Object result = expr.getValue(context);
            assertEquals(tests[1], result);
        }
    }

    @Test
    void testInvalidTests() throws Exception {
        for (String pattern : INVALID_TESTS) {
            try {
                final Expression expr = new BaseExpression(pattern);
                fail("pattern  " + pattern + " is invalid and exception should be throwed");
            } catch (Exception e) {
                // do nothing
            }
        }
    }

    private static Date asDate(final String str) throws RuntimeException {
        try {
            return StringUtil.parseDate(str);
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
