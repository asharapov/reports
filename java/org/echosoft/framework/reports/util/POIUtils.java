package org.echosoft.framework.reports.util;

import java.util.Calendar;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.echosoft.framework.reports.model.ColorModel;
import org.echosoft.framework.reports.model.FontModel;
import org.echosoft.framework.reports.processor.ExecutionContext;
import org.echosoft.framework.reports.processor.Group;
import org.echosoft.framework.reports.processor.SectionContext;

/**
 * Класс содержит ряд часто использующихся методов, которые оперируют над рабочей книгой Excel.
 *
 * @author Anton Sharapov
 */
public class POIUtils {

    private static final String FORMULA = "$F=";
    private static final int FORMULA_LENGTH = FORMULA.length();

    /**
     * Возвращает содержимое ячейки в зависимости от ее типа.
     *
     * @param cell ячейка документа Excel.
     * @return содержимое ячейки.
     */
    public static Object getCellValue(final Cell cell) {
        if (cell == null)
            return null;
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                return DateUtil.isCellDateFormatted(cell) ? cell.getDateCellValue() : cell.getNumericCellValue();
            case Cell.CELL_TYPE_STRING: {
                final RichTextString rt = cell.getRichStringCellValue();
                return rt.numFormattingRuns() == 0 ? rt.getString() : rt;
            }
            case Cell.CELL_TYPE_FORMULA:
                return cell.getCellFormula();
            case Cell.CELL_TYPE_BLANK:
                return null;
            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue();
            case Cell.CELL_TYPE_ERROR:
                return "#ERR" + cell.getErrorCellValue();
            default:
                return null;
        }
    }

    /**
     * Устанавливает значение ячейки.
     * <p><strong>Внимание!</strong> Это усеченная по функциональности версия, в которой не работают вызовы макрофункций.
     *
     * @param cell  ячейка в которую надо установить значение.
     * @param value объект на основании которого устанавливается значение ячейки.
     * @see org.echosoft.framework.reports.processor.ExcelReportProcessor#renderCell(ExecutionContext, Object)
     */
    public static void setCellValue(final Cell cell, final Object value) {
        if (value == null) {
            cell.setCellType(Cell.CELL_TYPE_BLANK);
        } else
        if (value instanceof Date) {
            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
            cell.setCellValue((Date) value);
        } else
        if (value instanceof Calendar) {
            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
            cell.setCellValue((Calendar) value);
        } else
        if (value instanceof Double) {
            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
            cell.setCellValue((Double) value);
        } else
        if (value instanceof Number) {
            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
            cell.setCellValue(((Number) value).doubleValue());
        } else
        if (value instanceof Boolean) {
            cell.setCellType(Cell.CELL_TYPE_BOOLEAN);
            cell.setCellValue((Boolean) value);
        } else
        if (value instanceof RichTextString) {
            cell.setCellType(Cell.CELL_TYPE_STRING);
            cell.setCellValue((RichTextString) value);
        } else {
            final String text = value.toString();
            if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                cell.setCellFormula(text);
            } else
            if (text.startsWith(FORMULA)) {
                cell.setCellType(Cell.CELL_TYPE_FORMULA);
                cell.setCellFormula(text.substring(FORMULA_LENGTH));
            } else {
                cell.setCellType(Cell.CELL_TYPE_STRING);
                final CreationHelper helper = cell.getSheet().getWorkbook().getCreationHelper();
                cell.setCellValue(helper.createRichTextString(text));
            }
        }
    }

    /**
     * Возвращает <code>true</code> если аргумент соответствует всем требованиям предъявляемым к имени колонки.
     *
     * @param columnName наименование колонки в Excel.
     * @return <code>true</code> если аргумент соответствует всем требованиям предъявляемым к имени колонки.
     */
    public static boolean isValidColumnName(final String columnName) {
        if (columnName == null || columnName.length() == 0)
            return false;
        for (int i = columnName.length() - 1; i >= 0; i--) {
            final char c = columnName.charAt(i);
            if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))) {
                return false;  // обнаружен недопустимый символ.
            }
        }
        return true;
    }

    /**
     * Конвертирует наименование колонки из формата ALPHA-26 в десятичные числа.
     *
     * @param columnName наименование колонки (A, B, C, ... Z, AA, ... AZ, ...)
     * @return порядковый номер колонки (начиная с 0).
     *         Если наименование колонки содержит недопустимые символы то метод вернет <code>-1</code>.
     */
    public static int getColumnNumber(final String columnName) {
        int result = 0;
        int pos = 0;
        for (int i = columnName.length() - 1; i >= 0; i--) {
            final char c = columnName.charAt(i);
            if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))) {
                return -1;  // обнаружен недопустимый символ.
            }
            // Character.getNumericValue() возвращает значения 10-35 для символов A-Z
            final int shift = (int) Math.pow(26, pos++);
            result += (Character.getNumericValue(c) - 9) * shift;
        }
        return result - 1;
    }

    /**
     * Конвертирует массив наименований колонок указанных в формате ALPHA-26 в массив порядковых номеров (начиная с 0) этих колонок.
     *
     * @param columnsNames массив имен перечня колонок
     * @return массив порядковых номеров соответствующих колонок (начиная с 0).
     */
    public static int[] getColumnsNumbers(final String[] columnsNames) {
        if (columnsNames == null)
            return null;
        final int[] result = new int[columnsNames.length];
        for (int i = 0; i < columnsNames.length; i++) {
            result[i] = getColumnNumber(columnsNames[i]);
        }
        return result;
    }

    /**
     * Конвертирует порядковый номер колонки (начиная с 0) в формат ALPHA-26.
     *
     * @param col порядковый номер колонки, начиная с 0.
     * @return наименование колонки в формате  A, B, C, ... Z, AA, ... AZ, ...
     */
    public static String getColumnName(final int col) {
        // Excel counts column A as the 1st column, we treat it as the 0th one
        final int excelColNum = col + 1;
        String colRef = "";
        int colRemain = excelColNum;
        while (colRemain > 0) {
            int thisPart = colRemain % 26;
            if (thisPart == 0) {
                thisPart = 26;
            }
            colRemain = (colRemain - thisPart) / 26;
            // The letter A is at 65
            final char colChar = (char) (thisPart + 64);
            colRef = colChar + colRef;
        }
        return colRef;
    }

    /**
     * Для указанной ячейки возвращает ее координаты вида: <code><u>&lt;название колонки&gt;&lt;номер строки&gt;</u></code>.<br/>
     * например:  <code>A1</code>, <code>BY234</code>, ...
     *
     * @param cell ячейка таблицы.
     * @return строка с адресом ячейки на листе отчета.
     */
    public static String getCellName(final Cell cell) {
        return cell != null
                ? getColumnName(cell.getColumnIndex()) + (cell.getRowIndex() + 1)
                : null;
    }

    /**
     * Компилирует вызов указанной функции Excel для текущей группы.<br/>
     * <strong>Внимание!</strong> При вызове данной функции следует самостоятельно контролировать количество аргументов формируемой функции.
     * Если их количество будет слишком большим то в процессе формирования отчета возникнет ошибка. Для справки: в версии POI 3.2 большинство
     * функций ограничены 30 аргументами.
     *
     * @param ectx     контекст выполнения.
     * @param function название функции.
     * @return строка с вызовом функции или <code>null</code> если вызов требуемой функции в данный момент невозможен.
     */
    public static String makeGroupFormula(final ExecutionContext ectx, final String function) {
        final SectionContext sctx = ectx.sectionContext;
        final Group group = sctx.gm.getCurrentGroup();
        if (group == null)
            return null;

        final String colname = POIUtils.getColumnName(ectx.cell.getColumnIndex());
        final StringBuilder formula = new StringBuilder(16);
        formula.append(function);
        formula.append('(');

        final int chsize = group.children.size();
        if (chsize > 0) {
            for (int i = 0; i < chsize; i++) {
                if (i > 0)
                    formula.append(',');
                final Group child = group.children.get(i);
                formula.append(colname);
                formula.append(child.startRow + 1);
            }
        } else
        if (group.recordsHeight != null && group.recordsHeight == 1) {
            final int start = group.startRow + group.model.getRowsCount();
            final int end = start + group.records.size();
            formula.append(colname);
            formula.append(start + 1);
            formula.append(':');
            formula.append(colname);
            formula.append(end);
        } else {
            for (int i = 0, rsize = group.records.size(); i < rsize; i++) {
                if (i > 0)
                    formula.append(',');
                formula.append(colname);
                formula.append(group.records.get(i) + 1);
            }
        }
        formula.append(')');
        return formula.toString();
    }



    /**
     * Декодирует информацию о цвете в случае использования XSSF формата. POI 3.9 все еще содержит какие-то ошибки при работе с цветностью
     * поэтому нам приходится добавлять свои хаки чтобы получить более-менее приемлемые значения.
     *
     * @param wb    документ
     * @param color описание цвета в формате POI.
     * @return описание цвета в классическом RGB формате.
     */
    public static byte[] decodeXSSFColor(final XSSFWorkbook wb, XSSFColor color) {
        if (color == null)
            return null;

        if (color.getCTColor().isSetTheme()) {
            final int theme = color.getTheme();
            switch (theme) {
                case 0:
                case 1:
                case 2:
                case 3:
                    //handle bug: MSExcel does not follow own definitions
                    double tint = color.getTint();
                    final XSSFColor clr = wb.getTheme().getThemeColor(theme ^ 1);
                    if (clr != null) {
                        clr.setTint(tint);
                        color = clr;
                    }
                    break;
            }
        }

        byte[] rgb = color.getRgbWithTint();
        // ниже рассмотрена спец. обработка случая когда обрабатываемый .xlsx документ был получен
        // простым сохранением в новом формате из старого .xls документа без каких-либо изменений ...
        // В других случаях, по моим наблюдениям, индексированные цвета на которых бы не работали стандартные методы POI не используются.
        if (rgb == null && color.getCTColor().isSetIndexed()) {
            final int  index = color.getIndexed();
            final HSSFColor clr = HSSFColor.getIndexHash().get(index);
            if (clr != null && IndexedColors.AUTOMATIC.index != index) {
                final short[] rgb2 = clr.getTriplet();
                rgb = new byte[]{(byte)rgb2[0], (byte)rgb2[1], (byte)rgb2[2]};
            }
        }
        return rgb;
    }

    /**
     * Метод проверяет наличие в рабочей книге зарегистрированного фонта с указанными характеристиками и если он отсутствует то регистрирует его.
     *
     * @param wb   рабочая книга в которой должен быть зарегистрирован требуемый фонт.
     * @param font характеристики требуемого фонта.
     * @return зарегистрированный в рабочей книге фонт с требуемыми характеристиками.
     */
    public static Font ensureFontExists(final HSSFWorkbook wb, final FontModel font) {
        final short colorId = font.getColor() != null ? font.getColor().getId() : 0;
        Font f = wb.findFont(font.getBoldWeight(), colorId, font.getFontHeight(),
                font.getFontName(), font.isItalic(), font.isStrikeout(), font.getTypeOffset(), font.getUnderline());
        if (f == null) {
            f = wb.createFont();
            f.setBoldweight(font.getBoldWeight());
            f.setCharSet(font.getCharSet());
            f.setColor(colorId);
            f.setFontHeight(font.getFontHeight());
            f.setFontName(font.getFontName());
            f.setItalic(font.isItalic());
            f.setStrikeout(font.isStrikeout());
            f.setTypeOffset(font.getTypeOffset());
            f.setUnderline(font.getUnderline());
        }
        return f;
    }

    /**
     * Конструирует экземпляр {@link XSSFColor} на основе сведений из нашей модели.
     * Этот "гениальный" метод служит только для одной цели - обойти криво работающую "заплатку" в коде POI.
     *
     * @param color описание требуемого цвета.
     * @return соответствующий экземпляр XSSFColor.
     */
    public static XSSFColor makeXSSFColor(final ColorModel color) {
        if (color == null)
            return null;
        final int hash = color.getPackedValue();
        final byte[] rgb;
        if (hash == 0) {
            rgb = new byte[]{-1, -1, -1};
        } else
        if (hash == 0xFFFFFF) {
            rgb = new byte[]{0, 0, 0};
        } else
            rgb = color.toByteArray();
        return new XSSFColor(rgb);
    }

    /**
     * Метод проверяет наличие в рабочей книге зарегистрированного фонта с указанными характеристиками и если он отсутствует то регистрирует его.
     *
     * @param wb   рабочая книга в которой должен быть зарегистрирован требуемый фонт.
     * @param font характеристики требуемого фонта.
     * @return зарегистрированный в рабочей книге фонт с требуемыми характеристиками.
     */
    public static Font ensureFontExists(final XSSFWorkbook wb, final FontModel font) {
        final int rgbHash = font.getColor() != null ? font.getColor().getPackedValue() : -1;
        final StylesTable styles = wb.getStylesSource();
        for (XSSFFont f : styles.getFonts()) {
            if (f.getFontName().equals(font.getFontName())
                    && f.getFontHeight() == font.getFontHeight()
                    && f.getBoldweight() == font.getBoldWeight()
                    && f.getItalic() == font.isItalic()
                    && f.getStrikeout() == font.isStrikeout()
                    && f.getTypeOffset() == font.getTypeOffset()
                    && f.getUnderline() == font.getUnderline()
                    && ColorModel.getHash(decodeXSSFColor(wb, f.getXSSFColor())) == rgbHash) {
                return f;
            }
        }
        final XSSFFont f = wb.createFont();
        f.setFontName(font.getFontName());
        f.setFontHeight(font.getFontHeight());
        f.setCharSet(font.getCharSet());
        f.setBoldweight(font.getBoldWeight());
        f.setItalic(font.isItalic());
        f.setStrikeout(font.isStrikeout());
        f.setTypeOffset(font.getTypeOffset());
        f.setUnderline(font.getUnderline());
        if (font.getColor() != null)
            f.setColor(makeXSSFColor(font.getColor()));
        return f;
    }

    /**
     * Копирует все свойства исходного фонта в целевой. И исходный и целевой фонты должны относиться к одному и тому же документу Excel.
     * Данный метод используется для того чтобы в последствии поменять в новом фонте один или несколько свойств не затрагивая при этом свойства исходного фонта.
     *
     * @param wb  документ Excel.
     * @param index индекс фонта в документе который должен быть взят в качестве шаблона для копирования.
     * @return созданная в этом же документе копия шрифта.
     */
    public static Font copyFont(final Workbook wb, final short index) {
        if (wb instanceof HSSFWorkbook) {
            return copyFont((HSSFWorkbook)wb, index);
        } else
        if (wb instanceof XSSFWorkbook) {
            return copyFont((XSSFWorkbook)wb, index);
        } else
            throw new IllegalArgumentException("Unsupported document type: " + wb);
    }

    /**
     * Копирует все свойства исходного фонта в целевой. И исходный и целевой фонты должны относиться к одному и тому же документу Excel.
     * Данный метод используется для того чтобы в последствии поменять в новом фонте один или несколько свойств не затрагивая при этом свойства исходного фонта.
     *
     * @param wb  документ Excel.
     * @param index индекс фонта в документе который должен быть взят в качестве шаблона для копирования.
     * @return созданная в этом же документе копия шрифта.
     */
    public static HSSFFont copyFont(final HSSFWorkbook wb, final short index) {
        final HSSFFont src = wb.getFontAt(index);
        final HSSFFont dst = wb.createFont();
        dst.setFontName(src.getFontName());
        dst.setFontHeight(src.getFontHeight());
        dst.setCharSet(src.getCharSet());
        dst.setBoldweight(src.getBoldweight());
        dst.setItalic(src.getItalic());
        dst.setStrikeout(src.getStrikeout());
        dst.setTypeOffset(src.getTypeOffset());
        dst.setUnderline(src.getUnderline());
        dst.setColor(src.getColor());
        return dst;
    }

    /**
     * Копирует все свойства исходного фонта в целевой. И исходный и целевой фонты должны относиться к одному и тому же документу Excel.
     * Данный метод используется для того чтобы в последствии поменять в новом фонте один или несколько свойств не затрагивая при этом свойства исходного фонта.
     *
     * @param wb  документ Excel.
     * @param index индекс фонта в документе который должен быть взят в качестве шаблона для копирования.
     * @return созданная в этом же документе копия шрифта.
     */
    public static XSSFFont copyFont(final XSSFWorkbook wb, final short index) {
        final XSSFFont src = wb.getFontAt(index);
        final XSSFFont dst = wb.createFont();
        dst.setFontName(src.getFontName());
        dst.setFontHeight(src.getFontHeight());
        dst.setCharSet(src.getCharSet());
        dst.setBoldweight(src.getBoldweight());
        dst.setItalic(src.getItalic());
        dst.setStrikeout(src.getStrikeout());
        dst.setTypeOffset(src.getTypeOffset());
        dst.setUnderline(src.getUnderline());
        dst.setColor(src.getXSSFColor());
        return dst;
    }

    /**
     * Создает новый стиль в документе Excel который полностью копирует свойства некоторого исходного стиля этого же документа.
     * Данный метод используется для того чтобы в последствии поменять в новом стиле один или несколько свойств не затрагивая при этом свойства исходного стиля.
     *
     * @param wb  документ Excel.
     * @param index индекс стиля в документе который должен быть взят в качестве шаблона для копирования.
     * @return созданная копия исходного стиля в этом же документе.
     */
    public static CellStyle copyStyle(final Workbook wb, final short index) {
        if (wb instanceof HSSFWorkbook) {
            return copyStyle((HSSFWorkbook)wb, index);
        } else
        if (wb instanceof XSSFWorkbook) {
            return copyStyle((XSSFWorkbook)wb, index);
        } else
            throw new IllegalArgumentException("Unsupported document type: " + wb);
    }

    /**
     * Создает новый стиль в документе Excel который полностью копирует свойства некоторого исходного стиля этого же документа.
     * Данный метод используется для того чтобы в последствии поменять в новом стиле один или несколько свойств не затрагивая при этом свойства исходного стиля.
     *
     * @param wb  документ Excel.
     * @param index индекс стиля в документе который должен быть взят в качестве шаблона для копирования.
     * @return созданная копия исходного стиля в этом же документе.
     */
    public static HSSFCellStyle copyStyle(final HSSFWorkbook wb, final short index) {
        final HSSFCellStyle src = wb.getCellStyleAt(index);
        final HSSFCellStyle dst = wb.createCellStyle();
        dst.setAlignment(src.getAlignment());
        dst.setVerticalAlignment(src.getVerticalAlignment());
        dst.setDataFormat(src.getDataFormat());
        dst.setHidden(src.getHidden());
        dst.setIndention(src.getIndention());
        dst.setLocked(src.getLocked());
        dst.setRotation(src.getRotation());
        dst.setWrapText(src.getWrapText());
        dst.setBorderTop(src.getBorderTop());
        dst.setBorderRight(src.getBorderRight());
        dst.setBorderBottom(src.getBorderBottom());
        dst.setBorderLeft(src.getBorderLeft());
        dst.setTopBorderColor(src.getTopBorderColor());
        dst.setRightBorderColor(src.getRightBorderColor());
        dst.setBottomBorderColor(src.getBottomBorderColor());
        dst.setLeftBorderColor(src.getLeftBorderColor());
        dst.setFillPattern(src.getFillPattern());
        dst.setFillForegroundColor(src.getFillForegroundColor());
        dst.setFillBackgroundColor(src.getFillBackgroundColor());
        dst.setFont(wb.getFontAt(src.getFontIndex()));
        return dst;
    }

    /**
     * Создает новый стиль в документе Excel который полностью копирует свойства некоторого исходного стиля этого же документа.
     * Данный метод используется для того чтобы в последствии поменять в новом стиле один или несколько свойств не затрагивая при этом свойства исходного стиля.
     *
     * @param wb  документ Excel.
     * @param index индекс стиля в документе который должен быть взят в качестве шаблона для копирования.
     * @return созданная копия исходного стиля в этом же документе.
     */
    public static XSSFCellStyle copyStyle(final XSSFWorkbook wb, final short index) {
        final XSSFCellStyle src = wb.getCellStyleAt(index);
        final XSSFCellStyle dst = wb.createCellStyle();
        dst.setAlignment(src.getAlignment());
        dst.setVerticalAlignment(src.getVerticalAlignment());
        dst.setDataFormat(src.getDataFormat());
        dst.setHidden(src.getHidden());
        dst.setIndention(src.getIndention());
        dst.setLocked(src.getLocked());
        dst.setRotation(src.getRotation());
        dst.setWrapText(src.getWrapText());
        dst.setBorderTop(src.getBorderTop());
        dst.setBorderRight(src.getBorderRight());
        dst.setBorderBottom(src.getBorderBottom());
        dst.setBorderLeft(src.getBorderLeft());
        dst.setTopBorderColor(src.getTopBorderXSSFColor());
        dst.setRightBorderColor(src.getRightBorderXSSFColor());
        dst.setBottomBorderColor(src.getBottomBorderXSSFColor());
        dst.setLeftBorderColor(src.getLeftBorderXSSFColor());
        final short fp = src.getFillPattern();
        dst.setFillPattern(fp);
        if (fp != CellStyle.NO_FILL) {
            final XSSFColor ffc = src.getFillForegroundXSSFColor();
            if (ffc != null)
                dst.setFillForegroundColor(ffc);
            final XSSFColor fbc = src.getFillBackgroundXSSFColor();
            if (fbc != null)
                dst.setFillBackgroundColor(fbc);
        }
        dst.setFont(wb.getFontAt(src.getFontIndex()));
        return dst;
    }

    /**
     * Находит или создает новый стиль на основе исходного стиля примененного к текущей обрабатываемой ячейке в котором были изменены цвета шрифта и/или фона.
     * Информация о всех созданных данным методом стилях сохраняется в контексте выполнения в пространстве имен {@link org.echosoft.framework.reports.model.el.ELContext.Scope#VAR}.
     *
     * @param ectx    контекст выполнения. Используется для получения информации о текущей ячейке и для кэширования информации о вновь созданных в документе стилях.
     * @param fnColor Определяет цвет шрифта в данной ячейке. Аргумент содержит идентификатор цвета в документе или <code>-1</code> если цвет шрифта должен остаться без изменений.
     * @param bgColor Определяет цвет фона в данной ячейке. Аргумент содержит идентификатор цвета в документе или <code>-1</code> если цвет шрифта должен остаться без изменений.
     * @return копия оригинального стиля указанной ячейки с измененными цветами фона и шрифта. Этот стиль уже зарегистрирован в рабочей книге.
     */
    public static CellStyle getAltColorStyle(final ExecutionContext ectx, final short fnColor, final short bgColor) {
        return getAltColorStyle(ectx, ectx.cell.getCellStyle().getIndex(), fnColor, bgColor);
    }

    /**
     * Находит или создает новый стиль на основе указанного в аргументе исходного стиля в котором были изменены цвета шрифта и/или фона.
     * Информация о всех созданных данным методом стилях сохраняется в контексте выполнения в пространстве имен {@link org.echosoft.framework.reports.model.el.ELContext.Scope#VAR}.
     *
     * @param ectx           контекст выполнения. Используется для получения информации о текущей ячейке и для кэширования информации о вновь созданных в документе стилях.
     * @param cellStyleIndex исходный стиль, который должен послужить основой для создаваемого стиля.
     * @param fnColor        Определяет цвет шрифта в данной ячейке. Аргумент содержит идентификатор цвета в документе или <code>-1</code> если цвет шрифта должен остаться без изменений.
     * @param bgColor        Определяет цвет фона в данной ячейке. Аргумент содержит идентификатор цвета в документе или <code>-1</code> если цвет шрифта должен остаться без изменений.
     * @return копия оригинального стиля указанной ячейки с измененными цветами фона и шрифта. Этот стиль уже зарегистрирован в рабочей книге.
     */
    public static CellStyle getAltColorStyle(final ExecutionContext ectx, final short cellStyleIndex, final short fnColor, final short bgColor) {
        final String styleName = "alt.color.style." + cellStyleIndex + ':' + fnColor + '/' + bgColor;
        CellStyle dst = (CellStyle) ectx.elctx.getVariables().get(styleName);
        if (dst == null) {
            dst = POIUtils.copyStyle(ectx.wb, cellStyleIndex);
            if (fnColor >= 0) {
                final Font of = ectx.wb.getFontAt(dst.getFontIndex());
                Font nf = ectx.wb.findFont(of.getBoldweight(), (short) fnColor, of.getFontHeight(),
                        of.getFontName(), of.getItalic(), of.getStrikeout(), of.getTypeOffset(), of.getUnderline());
                if (nf == null) {
                    nf = POIUtils.copyFont(ectx.wb, of.getIndex());
                    nf.setColor((short) fnColor);
                }
                dst.setFont(nf);
            }
            if (bgColor >= 0) {
                dst.setFillPattern(CellStyle.SOLID_FOREGROUND);
                dst.setFillForegroundColor((short) bgColor);
            }
            ectx.elctx.getVariables().put(styleName, dst);
        }
        return dst;
    }
}
