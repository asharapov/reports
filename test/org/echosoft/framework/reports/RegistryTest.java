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
        final Enumeration<URL> e = getClass().getClassLoader().getResources("org/echosoft/framework/reports/data");
        while (e.hasMoreElements()) {
            final URL url = e.nextElement();
            System.out.println(url);
        }
    }
}
