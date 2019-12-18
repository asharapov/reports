package org.echosoft.framework.reports.test;

import java.io.File;

import org.echosoft.framework.reports.registry.ReportsRegistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Anton Sharapov
 */
public class RegistryTest {

    @Test
    void registerFromDirTest() throws Exception {
        final File dir = new File("src/test/java/org/echosoft/framework/reports/test/");
        assertTrue(dir.isDirectory());
        ReportsRegistry.registerReportsFromDirectory(dir, true, null);
        assertEquals(3, ReportsRegistry.getReports().size());
        assertNotNull(ReportsRegistry.getReport("excel-01"));
        assertNotNull(ReportsRegistry.getReport("excel-02"));
        assertNotNull(ReportsRegistry.getReport("excel-03"));
    }

    @Test
    void registerFromResourcesTest() throws Exception {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final String prefix = "org/echosoft/framework/reports/test/";
        ReportsRegistry.registerReports(
                loader.getResource(prefix + "excel-01.xlsx"),
                loader.getResource(prefix + "excel-02.xlsx"),
                loader.getResource(prefix + "excel-03.xlsx")
        );
        assertEquals(3, ReportsRegistry.getReports().size());
        assertNotNull(ReportsRegistry.getReport("excel-01"));
        assertNotNull(ReportsRegistry.getReport("excel-02"));
        assertNotNull(ReportsRegistry.getReport("excel-03"));
    }
}
