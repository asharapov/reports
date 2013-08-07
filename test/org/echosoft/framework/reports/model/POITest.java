package org.echosoft.framework.reports.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Locale;

import org.apache.poi.hpsf.MutableProperty;
import org.apache.poi.hpsf.MutablePropertySet;
import org.apache.poi.hpsf.MutableSection;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.Variant;
import org.apache.poi.hpsf.WritingNotSupportedException;
import org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.apache.poi.hpsf.wellknown.SectionIDMap;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.echosoft.framework.reports.TestUtils;
import org.echosoft.framework.reports.util.POIUtils;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class POITest {

    @Test
    public void testParsing() throws IOException {
        final HSSFWorkbook wb = TestUtils.loadExcelDocument("poitest1.xls");
        final SummaryInformation si = wb.getSummaryInformation();
        System.out.println("author: " + si.getAuthor());
        System.out.println("application: " + si.getApplicationName());
        System.out.println("subject: " + si.getSubject());
        System.out.println("title: " + si.getTitle());
        System.out.println("keywords: " + si.getKeywords());
        System.out.println("comments: " + si.getComments());
        System.out.println("created: " + si.getCreateDateTime());
        System.out.println();
        System.out.println(si);
    }

    @Test
    public void testHeadParsing() throws IOException {
        final InputStream in = TestUtils.openFileStream("poitest1.xls");
        final POIFSReader reader = new POIFSReader();
        reader.registerListener(new POIFSReaderListener() {
            public void processPOIFSReaderEvent(POIFSReaderEvent event) {
                System.out.println("name: " + event.getName() + "\npath: " + event.getPath());
                try {
                    SummaryInformation si = (SummaryInformation) PropertySetFactory.create(event.getStream());
                    System.out.println("author: " + si.getAuthor());
                    System.out.println("application: " + si.getApplicationName());
                    System.out.println("subject: " + si.getSubject());
                    System.out.println("title: " + si.getTitle());
                    System.out.println("keywords: " + si.getKeywords());
                    System.out.println("comments: " + si.getComments());
                    System.out.println("created: " + si.getCreateDateTime());
                    //System.out.println(si);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, "\005SummaryInformation");
        reader.read(in);
    }

    @Test
    public void testSummaryInfoWriting() throws IOException, WritingNotSupportedException {
        final ByteArrayOutputStream out1 = new ByteArrayOutputStream(4096);
        final ByteArrayOutputStream out2 = new ByteArrayOutputStream(4096);
        HSSFWorkbook wb = new HSSFWorkbook();
        /*
        final HSSFSheet sheet = wb.createSheet("test sheet");
        final HSSFRow row = sheet.createRow(0);
        final HSSFCell cell1 = row.createCell((short)0, HSSFCell.CELL_TYPE_STRING);
        cell1.setCellValue(new HSSFRichTextString("cell1"));
        final HSSFCell cell2 = row.createCell((short)1, HSSFCell.CELL_TYPE_STRING);
        cell2.setCellValue(new HSSFRichTextString("cell2"));
        */
        wb.write(out1);
        final MutablePropertySet mps = new MutablePropertySet();
        final MutableSection ms = (MutableSection) mps.getSections().get(0);
        ms.setFormatID(SectionIDMap.SUMMARY_INFORMATION_ID);
        final MutableProperty p1 = new MutableProperty();
        p1.setID(PropertyIDMap.PID_TITLE);
        p1.setType(Variant.VT_LPWSTR);
        p1.setValue("Sample title");
        ms.setProperty(p1);
        final MutableProperty p2 = new MutableProperty();
        p2.setID(PropertyIDMap.PID_AUTHOR);
        p2.setType(Variant.VT_LPWSTR);
        p2.setValue("Anton Sharapov & Co");
        ms.setProperty(p2);
        final MutableProperty p3 = new MutableProperty();
        p3.setID(PropertyIDMap.PID_CREATE_DTM);
        p3.setType(Variant.VT_FILETIME);
        p3.setValue(new Date());
        ms.setProperty(p3);
        final POIFSFileSystem fs = new POIFSFileSystem(new ByteArrayInputStream(out1.toByteArray()));
        fs.createDocument(mps.toInputStream(), SummaryInformation.DEFAULT_STREAM_NAME);
        fs.writeFilesystem(out2);
        wb = new HSSFWorkbook(new ByteArrayInputStream(out2.toByteArray()));
        System.out.println(wb.getSheetName(0));
        final SummaryInformation si = wb.getSummaryInformation();
        System.out.println("author: " + si.getAuthor());
        System.out.println("application: " + si.getApplicationName());
        System.out.println("subject: " + si.getSubject());
        System.out.println("title: " + si.getTitle());
        System.out.println("keywords: " + si.getKeywords());
        System.out.println("comments: " + si.getComments());
        System.out.println("created: " + si.getCreateDateTime());
    }

    @Test
    public void testNumericFields() throws IOException {
        System.out.println(Locale.getDefault());
        final HSSFWorkbook wb = new HSSFWorkbook();
        final HSSFSheet sheet = wb.createSheet("test sheet");
        final HSSFRow row1 = sheet.createRow(0);
        row1.setHeight(sheet.getDefaultRowHeight());
        final HSSFCell[] cells1 = new HSSFCell[3];
        cells1[0] = row1.createCell(0, HSSFCell.CELL_TYPE_STRING);
        cells1[0].setCellValue(new HSSFRichTextString("test1"));
        cells1[1] = row1.createCell(1, HSSFCell.CELL_TYPE_STRING);
        cells1[1].setCellValue(new HSSFRichTextString("test2"));
        cells1[2] = row1.createCell(2, HSSFCell.CELL_TYPE_STRING);
        cells1[2].setCellValue(new HSSFRichTextString("test3"));
        final HSSFRow row2 = sheet.createRow(1);
        row2.setHeight(sheet.getDefaultRowHeight());
        final HSSFCell[] cells2 = new HSSFCell[3];
        cells2[0] = row2.createCell(0, HSSFCell.CELL_TYPE_NUMERIC);
        cells2[0].setCellValue(1.0);
        cells2[1] = row2.createCell(1, HSSFCell.CELL_TYPE_NUMERIC);
        cells2[1].setCellValue(2.5);
        cells2[2] = row2.createCell(2, HSSFCell.CELL_TYPE_BLANK);
        cells2[2].setCellType(HSSFCell.CELL_TYPE_FORMULA);
        cells2[2].setCellFormula("A2+B2");
        final HSSFRow row3 = sheet.createRow(2);
        row3.setHeight(sheet.getDefaultRowHeight());
        final HSSFCell[] cells3 = new HSSFCell[3];
        cells3[0] = row3.createCell(0, HSSFCell.CELL_TYPE_NUMERIC);
        POIUtils.setCellValue(cells3[0], 1.2);
        cells3[1] = row3.createCell(1, HSSFCell.CELL_TYPE_NUMERIC);
        POIUtils.setCellValue(cells3[1], 2.6);
        cells3[2] = row3.createCell(2, HSSFCell.CELL_TYPE_STRING);
        POIUtils.setCellValue(cells3[2], "$F=A3+B3");
        final FileOutputStream out = new FileOutputStream("poitest.xls");
        wb.write(out);
        out.flush();
        out.close();
    }

    @Test
    public void testColumnGroups() throws IOException {
        final HSSFWorkbook wb = new HSSFWorkbook();
        final HSSFSheet sheet = wb.createSheet("test");
//        for (int r=0; r<20; r++) {
//            final HSSFRow row = sheet.createRow(r);
//            row.setHeight(sheet.getDefaultRowHeight());
//            for (int c=0; c<30; c++) {
//                final HSSFCell cell = row.createCell(c, HSSFCell.CELL_TYPE_NUMERIC);
//                cell.setCellValue( c );
//            }
//        }
//        sheet.groupColumn(1, 15);
//        sheet.groupColumn(4, 12);
//        sheet.groupColumn(7, 9);
        sheet.groupColumn(7, 9);
        sheet.groupColumn(4, 12);
        sheet.groupColumn(1, 15);  // POI bug here: 45639
        final FileOutputStream out = new FileOutputStream("poitest.xls");
        wb.write(out);
        out.flush();
        out.close();
    }

    @Test
    public void testRowGroups() throws IOException {
        final HSSFWorkbook wb = new HSSFWorkbook();
        final HSSFSheet sheet1 = wb.createSheet("sumstop");
        sheet1.setRowSumsBelow(false);
        for (int i = 0; i < 22; i++) {
            sheet1.createRow(i);
        }
        sheet1.groupRow(6, 7);
        sheet1.groupRow(9, 10);
        sheet1.groupRow(5, 10);
//        sheet1.groupRow(13, 16);
//        sheet1.groupRow(12, 16);
//        sheet1.groupRow(4, 16);
//        sheet1.groupRow(20, 21);
//        sheet1.groupRow(19, 21);
//        sheet1.groupRow(18, 21);
//        final HSSFSheet sheet2 = wb.createSheet("sumsbelow");
//        sheet2.setRowSumsBelow(true);
//        for (int i=0; i<22; i++) {
//            sheet2.createRow(i);
//        }
//        sheet2.groupRow(6, 7);
////        sheet2.setRowGroupCollapsed(6, true);
//        sheet2.groupRow(9, 10);
////        sheet2.setRowGroupCollapsed(9, true);
//        sheet2.groupRow(5, 10);
//        sheet2.groupRow(13, 16);
////        sheet2.setRowGroupCollapsed(13, true);
//        sheet2.groupRow(12, 16);
//        sheet2.groupRow(4, 16);
//        sheet2.groupRow(20, 21);
////        sheet2.setRowGroupCollapsed(20, true);
//        sheet2.groupRow(19, 21);
//        sheet2.groupRow(18, 21);
        final FileOutputStream out = new FileOutputStream("poitest.xls");
        wb.write(out);
        out.flush();
        out.close();
    }

    @Test
    public void testRowGroups2() throws IOException {
        final HSSFWorkbook wb = TestUtils.loadExcelDocument("poitest2.xls");
        final HSSFSheet sheet1 = wb.getSheetAt(0);
        final HSSFSheet sheet2 = wb.getSheetAt(1);
        final HSSFSheet sheet3 = wb.getSheetAt(2);
        System.out.println("done");
    }
}
