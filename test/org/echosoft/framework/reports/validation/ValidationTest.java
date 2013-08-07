package org.echosoft.framework.reports.validation;

import java.io.InputStream;
import java.util.List;

import junit.framework.Assert;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class ValidationTest {

    @Test
    public void testReadValidations() throws Exception {
        final InputStream in = ValidationTest.class.getResourceAsStream("testcase.xlsx");
        try {
            final Workbook wb = WorkbookFactory.create(in);
            final XSSFWorkbook xwb = (XSSFWorkbook)wb;
            final XSSFSheet xsheet = xwb.getSheetAt(0);
            final List<XSSFDataValidation> rules = xsheet.getDataValidations();
            System.out.println("rules: " + rules.size());
            Assert.assertTrue("No validation rules", rules.size() > 0);
            final XSSFDataValidation rule = rules.get(0);
            System.out.println(rule);
        } finally {
            in.close();
        }
    }
}
