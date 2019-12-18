package org.echosoft.framework.reports.test.validation;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Anton Sharapov
 */
public class ValidationTest {

    @Test
    void testReadValidations() throws Exception {
        final URL url = ValidationTest.class.getResource("testcase.xlsx");
        final Workbook wb = WorkbookFactory.create(url.openStream());
        final XSSFWorkbook xwb = (XSSFWorkbook) wb;
        final XSSFSheet xsheet = xwb.getSheetAt(0);
        final List<XSSFDataValidation> rules = xsheet.getDataValidations();
        assertTrue(rules.size() > 0, "No validation rules");
        final XSSFDataValidation rule = rules.get(0);
        System.out.println(rule);
    }
}
