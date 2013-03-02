package org.echosoft.framework.reports.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.echosoft.common.utils.StringUtil;

/**
 * Преобразовывает первую страницу документа Excel в HTML файл.
 * 
 * @author Dmitry Smirnov
 */
public final class ConvertExcelToHTML {

    private static final ConvertExcelToHTML instance = new ConvertExcelToHTML();
    private static final Locale LOCALE = new Locale("ru", "RU");

    public static ConvertExcelToHTML getInstance() {
        return instance;
    }

    private ConvertExcelToHTML() {
    }

    /**
     * Converts Excel into HTML
     *
     * @param path Excel file path
     * @return HTML representation of workbook
     * @throws IOException an exception
     */
    public String process(final String path) throws IOException, InvalidFormatException {
        final InputStream in = new FileInputStream(path);
        try {
            final Workbook wb = WorkbookFactory.create(in);
            return process(wb);
        } finally {
            in.close();
        }
    }

    /**
     * Converts Excel into HTML
     *
     * @param stream Excel file as stream
     * @return HTML representation of workbook
     * @throws IOException an exception
     */
    public String process(final InputStream stream) throws IOException, InvalidFormatException {
        final Workbook wb = WorkbookFactory.create(stream);
        return process(wb);
    }

    /**
     * Converts Excel into HTML
     *
     * @param workbook HSSFWorkbook
     * @return HTML representation of workbook
     * @throws IOException an exception
     */
    public String process(final Workbook workbook) throws IOException {
        //get first  sheet
        Sheet sheet = workbook.getSheetAt(0);
        final StringBuilder sb = new StringBuilder(1024);

        //load merged cells
        final List<CellRangeAddress> regions = new ArrayList<CellRangeAddress>(sheet.getNumMergedRegions());
        for (int k = 0; k < sheet.getNumMergedRegions(); k++) {
            regions.add(sheet.getMergedRegion(k));
        }

        sb.append("<html>\n");
        sb.append("<head>\n");
        sb.append(" <meta http-equiv=\"Content-Type\" content=\"text/html; charset=cp1251\">\n");
        final Map<Integer,String> styles = styleGenerator(sb, workbook);
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("<table  border=0>\n");

        final int lastRowNum = sheet.getLastRowNum();
        for (int i = 0; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                row = sheet.createRow(i);
            }
            int lastColNum = row.getPhysicalNumberOfCells();

            // before <tr> write special tag <col>
            if (i == 0) {
                for (short j = 0; j < lastColNum; j++) {
                    sb.append(" <col style=\"width:");
                    sb.append(getHTMLWidth(j, sheet));
                    sb.append("px;\"/>\n");
                }
            }

            // skip hidden rows
            if (row.getZeroHeight() || row.getHeight() == 0) {
                continue;
            }

            sb.append("<tr>\n");
            for (int j = 0; j < lastColNum; j++) {
                final Cell cell = row.getCell(j);
                if (cell == null) {
                    sb.append("<td></td>\n");
                    continue;
                }
                final String msg = getMergedString(sheet, cell, i, regions);
                if ("_FALSE_".equals(msg)) {  //skip cells which belonged to merged regions
                    continue;
                }

                sb.append(" <td");
                sb.append(" class=\"");
                sb.append(styles.get((int)cell.getCellStyle().getIndex()));
                sb.append("\"");
                if ("".equals(msg)) {
                    sb.append(" style=\"min-width:");
                    sb.append(getHTMLWidth(j, sheet));
                    sb.append("px;\"");
                }
                sb.append(msg);
                sb.append(">");

                final DataFormat dataFormat = workbook.createDataFormat();
                final String format = dataFormat.getFormat(cell.getCellStyle().getDataFormat());
                final boolean isWrap = cell.getCellStyle().getWrapText();
                switch (cell.getCellType()) {
                    case Cell.CELL_TYPE_NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell)) {
                            sb.append(mask(getFormattedDate(format, cell.getDateCellValue()), isWrap));
                        } else {
                            sb.append(mask(getFormattedNumber(format, cell.getNumericCellValue()), isWrap));
                        }
                        break;
                    case Cell.CELL_TYPE_STRING:
                        sb.append(mask(cell.getRichStringCellValue().getString(), isWrap));
                        break;
                    case Cell.CELL_TYPE_FORMULA:
                        sb.append(mask(getFormattedNumber(format, cell.getNumericCellValue()), isWrap));
                        break;
                    case Cell.CELL_TYPE_BLANK:
                        sb.append("&nbsp;");
                        break;
                }
                sb.append("</td>\n");
            }
            sb.append("</tr>\n");
        }
        sb.append("</table>\n");
        sb.append("</body>\n");
        sb.append("</html>\n");
        return sb.toString();
    }


    private String getFormattedNumber(String format, double num) {
        final DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(LOCALE);
        df.applyPattern(FormatConstants.getJavaFormatString(FormatConstants.NUMBER, format));
        return df.format(num);
    }

    private String getFormattedDate(String format, Date date) {
        final DateFormat df = new SimpleDateFormat(FormatConstants.getJavaFormatString(FormatConstants.DATE, format), LOCALE);
        return df.format(date);
    }

    private String getMergedString(Sheet sheet, Cell cell, int rowNum, List<CellRangeAddress> regions) {
        final StringBuilder sb = new StringBuilder();
        int colNum = cell.getColumnIndex();
        for (CellRangeAddress region : regions) {
            final int startCol = region.getFirstColumn();
            final int finishCol = region.getLastColumn();
            final int startRow = region.getFirstRow();
            final int finishRow = region.getLastRow();

            if (colNum == startCol && rowNum == startRow) {
                final int colspan = finishCol - startCol;
                final int rowspan = finishRow - startRow;
                //right and bottom border for colspan cells
                final Cell cellRight = sheet.getRow(startRow).getCell(finishCol);
                final Cell cellBottom = sheet.getRow(finishRow).getCell(startCol);
                sb.append(" style=\"");
                addBorder(sb, "border-right:", cellRight.getCellStyle().getBorderRight(), cellRight.getCellStyle().getRightBorderColor());
                addBorder(sb, "border-bottom:", cellBottom.getCellStyle().getBorderBottom(), cellBottom.getCellStyle().getBottomBorderColor());
                sb.append("\"");
                sb.append(" width=\"*\"");
                if (colspan == 0 && rowspan != 0) {
                    sb.append(" rowspan=\"").append(rowspan + 1).append("\"");
                    return sb.toString();
                } else
                if (colspan != 0 && rowspan == 0) {
                    sb.append(" colspan=\"").append(colspan + 1).append("\"");
                    return sb.toString();
                } else
                if (colspan != 0) {
                    sb.append(" colspan=\"").append(colspan + 1).append("\" rowspan=\"").append(rowspan + 1).append("\"");
                    return sb.toString();
                } else {
                    return "";
                }
            } else
            if (colNum >= startCol && colNum <= finishCol && rowNum >= startRow && rowNum <= finishRow) {
                return "_FALSE_";
            }
        }
        return "";
    }

    private Map<Integer,String> styleGenerator(final StringBuilder sb, final Workbook book) {
        final Map<Integer,String> styles = new HashMap<Integer,String>(book.getNumCellStyles());
        sb.append(" <style type=\"text/css\">\n");
        sb.append(" table {\n");
        sb.append("   empty-cell: show;\n");
        sb.append("   border-collapse:collapse;\n");
        sb.append(" }\n");
        for (short i = 0; i < book.getNumCellStyles(); i++) {
            final CellStyle style = book.getCellStyleAt(i);
            sb.append(" .class_").append(i).append(" {\n");
            //border
            addBorder(sb, "  border-top:", style.getBorderTop(), style.getTopBorderColor());
            sb.append(";\n");
            addBorder(sb, "  border-left:", style.getBorderLeft(), style.getLeftBorderColor());
            sb.append(";\n");
            addBorder(sb, "  border-right:", style.getBorderRight(), style.getRightBorderColor());
            sb.append(";\n");
            addBorder(sb, "  border-bottom:", style.getBorderBottom(), style.getBottomBorderColor());
            sb.append(";\n");
            //aligment
            addHorizontalAlignment(sb, style.getAlignment());
            addVerticalAlignment(sb, style.getVerticalAlignment());
            //bg color
            sb.append("  background-color:").
                    append(getHEXColor(style.getFillForegroundColor(), "white")).
                    append(";\n");
            //font
            final Font font = book.getFontAt(style.getFontIndex());
            sb.append("  font-size:").append(font.getFontHeightInPoints()).append(".0pt;\n");
            sb.append("  font-family:").append(font.getFontName()).append(", sans-serif;\n");
            sb.append("  font-weight:").append(font.getBoldweight()).append(";\n");
            sb.append("  color:").append(getHEXColor(font.getColor())).append(";\n");
            //wrap
            sb.append("  white-spacing:").append(style.getWrapText() ? "normal;" : "nowrap;\n");
            //indent
            sb.append("  padding-left:").append(style.getIndention() * 12).append("px;\n");
            sb.append(" }\n");
            styles.put((int)style.getIndex(), "class_"+i);
        }
        sb.append(" </style>\n");
        return styles;
    }


    private void addBorder(StringBuilder sb, String cssKey, short thickness, short color) {
        sb.append(cssKey);
        switch (thickness) {
            case CellStyle.BORDER_NONE:
                sb.append("none;");
                break;
            case CellStyle.BORDER_THIN:
                sb.append("0.5pt solid ").append(getHEXColor(color)).append(";");
                break;
            case CellStyle.BORDER_MEDIUM:
                sb.append("1.0pt solid ").append(getHEXColor(color)).append(";");
                break;
            case CellStyle.BORDER_THICK:
                sb.append("1.5pt solid ").append(getHEXColor(color)).append(";");
                break;
            case CellStyle.BORDER_DASHED:
                sb.append("0.5pt dashed ").append(getHEXColor(color)).append(";");
                break;
            case CellStyle.BORDER_MEDIUM_DASHED:
                sb.append("1.0pt dashed ").append(getHEXColor(color)).append(";");
                break;
            case CellStyle.BORDER_DOTTED:
                sb.append("0.5pt dotted ").append(getHEXColor(color)).append(";");
                break;
            case CellStyle.BORDER_DOUBLE:
                sb.append("0.5pt double ").append(getHEXColor(color)).append(";");
                break;
            default:
                sb.append("0.5pt solid ").append(getHEXColor(color)).append(";");
        }
    }


    /**
     * Returns cell width. Used in tag width in HTML format
     *
     * @param colNum column number
     * @param sheet  current Excel sheet
     * @return cell width
     */
    private int getHTMLWidth(int colNum, Sheet sheet) {
        int calcWidth = 0;
        int width = sheet.getColumnWidth(colNum);
        if (width != sheet.getDefaultColumnWidth()) {
            width /= 256;
        }
        switch (width) {
            case 0:
                break;
            case 1:
                calcWidth = 12;
                break;
            case 2:
                calcWidth = 19;
                break;
            case 3:
                calcWidth = 26;
                break;
            case 4:
                calcWidth = 33;
                break;
            case 5:
                calcWidth = 40;
                break;
            case 6:
                calcWidth = 47;
                break;
            case 7:
                calcWidth = 54;
                break;
            case 8:
                calcWidth = 61;
                break;
            case 9:
                calcWidth = 68;
                break;
            default:
                calcWidth = (width - 10) * 7 + 75;
        }
        return calcWidth;
    }

    /**
     * Converts POI horizontal alignment into css view.
     *
     * @param sb        result StringBuffer
     * @param alignment This is POI horizontal alignment presentation
     */
    private void addHorizontalAlignment(StringBuilder sb, short alignment) {
        sb.append("  text-align:");
        switch (alignment) {
            case CellStyle.ALIGN_CENTER:
                sb.append("center;\n");
                break;
            case CellStyle.ALIGN_CENTER_SELECTION:
                sb.append("center-across;\n");
                break;
            case CellStyle.ALIGN_FILL:
                sb.append("fill;\n");
                break;
            case CellStyle.ALIGN_GENERAL:
                sb.append("general;\n");
                break;
            case CellStyle.ALIGN_JUSTIFY:
                sb.append("justify;\n");
                break;
            case CellStyle.ALIGN_LEFT:
                sb.append("left;\n");
                break;
            case CellStyle.ALIGN_RIGHT:
                sb.append("right;\n");
                break;
            default:
                sb.append("general;\n");
        }
    }

    /**
     * Converts POI vertical alignment into css view.
     *
     * @param sb        result StringBuffer
     * @param alignment This is POI avertical lignment presentation
     */
    private void addVerticalAlignment(StringBuilder sb, short alignment) {
        sb.append("  vertical-align:");
        switch (alignment) {
            case CellStyle.VERTICAL_BOTTOM:
                sb.append("bottom;\n");
                break;
            case CellStyle.VERTICAL_CENTER:
                sb.append("middle;\n");
                break;
            case CellStyle.VERTICAL_JUSTIFY:
                sb.append("justify;\n");
                break;
            case CellStyle.VERTICAL_TOP:
                sb.append("top;\n");
                break;
            default:
                sb.append("bottom;\n");
        }
    }



    /**
     * Маскирует символы которые не допустимы в документах XML.
     * @param value  текстовое значение.
     * @param wrap  допускается ли перевод строк.
     * @return маскированное значение.
     */
    private static String mask(String value, boolean wrap) {
        if (value == null) {
            return ""; // to hide "null" string
        }
        final char content[] = new char[value.length()];
        value.getChars(0, value.length(), content, 0);
        final StringBuilder result = new StringBuilder(content.length + 50);
        for (char c : content) {
            switch (c) {
                case '\n': result.append("\n"); break;
                case '\r': result.append("\r"); break;
                case '<': result.append("&lt;"); break;
                case '>': result.append("&gt;"); break;
                case '&': result.append("&amp;"); break;
                case '"': result.append("&quot;"); break;
//                case '©': result.append("&copy;"); break;
                case ' ':
                    if (!wrap) {
                        result.append("&nbsp;");
                        break;
                    }
                default: result.append(c);
            }
        }
        return (result.toString());
    }


    /**
     * Конвертирует цвет в формате POI в строку вида #FFFFFF  (RGB формат).
     *
     * @param color  индекс цвета в документе.
     * @return  код цвета в формате RGB в шестнадцатиричном формате.
     */
    private static String getHEXColor(short color) {
        return getHEXColor(color, "windowtext");
    }


    /**
     * Конвертирует цвет в формате POI в строку вида #FFFFFF  (RGB формат).
     *
     * @param color  индекс цвета в документе.
     * @param defaultColor  строка с цветом по умолчанию если индекс цвета был указан неверно.
     * @return  код цвета в формате RGB в шестнадцатиричном формате.
     */
    private static String getHEXColor(short color, String defaultColor) {
//        IndexedColors.
        final HSSFColor cl = HSSFColor.getIndexHash().get((int) color);
        return cl == null ? defaultColor : ("#"
                + StringUtil.leadLeft(Integer.toHexString(cl.getTriplet()[0]), '0', 2)
                + StringUtil.leadLeft(Integer.toHexString(cl.getTriplet()[1]), '0', 2)
                + StringUtil.leadLeft(Integer.toHexString(cl.getTriplet()[2]), '0', 2));
    }

}
