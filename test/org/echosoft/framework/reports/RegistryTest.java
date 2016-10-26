package org.echosoft.framework.reports;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

import org.echosoft.framework.reports.registry.ReportsRegistry;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class RegistryTest {

    @Test
    public void registerFromDirTest() throws Exception {
        final File dir = new File("./test/org/echosoft/framework/reports/data/");
        Assert.assertTrue(dir.isDirectory());
        ReportsRegistry.registerReportsFromDirectory(dir, true, null);
        System.out.println(ReportsRegistry.getReports());
    }

    @Test
    public void registerFromResourcesTest() throws Exception {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final String prefix = "org/echosoft/framework/reports/data/";
        ReportsRegistry.registerReports(
                loader.getResource(prefix + "report1.xls"),
                loader.getResource(prefix + "report2.xls"),
                loader.getResource(prefix + "test-grp-1.xlsx")
        );
        System.out.println("done");
    }
}
