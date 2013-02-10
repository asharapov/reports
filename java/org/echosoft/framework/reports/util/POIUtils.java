package org.echosoft.framework.reports.util;

import java.util.Calendar;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.echosoft.framework.reports.model.FontModel;
import org.echosoft.framework.reports.processor.ExecutionContext;
import org.echosoft.framework.reports.processor.Group;
import org.echosoft.framework.reports.processor.SectionContext;

/**
 * Класс содержит ряд часто использующихся методов, которые оперируют над рабочей книгой Excel.
 * @author Anton Sharapov
 */
public class POIUtils {

    private static final String FORMULA = "$F=";
    private static final int FORMULA_LENGTH = FORMULA.length();

    /**
     * Возвращает содержимое ячейки в зависимости от ее типа.
     * 
     * @param cell  ячейка документа Excel.
     * @return  содержимое ячейки.
     */
    public static Object getCellValue(final HSSFCell cell) {
        if (cell==null)
            return null;
        switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_NUMERIC : {
                                return HSSFDateUtil.isCellDateFormatted(cell)
                                    ? cell.getDateCellValue() : cell.getNumericCellValue();
                            }
            case HSSFCell.CELL_TYPE_STRING : {
                                final HSSFRichTextString rt = cell.getRichStringCellValue();
                                return rt.numFormattingRuns()==0 ? rt.getString() : rt;
                            }
            case HSSFCell.CELL_TYPE_FORMULA : return cell.getCellFormula();
            case HSSFCell.CELL_TYPE_BLANK : return null;
            case HSSFCell.CELL_TYPE_BOOLEAN : return cell.getBooleanCellValue();
            case HSSFCell.CELL_TYPE_ERROR : return "#ERR"+cell.getErrorCellValue();
            default: return null;
        }
    }

    /**
     * Устанавливает значение ячейки.
     * <p><strong>Внимание!</strong> Это усеченная по функциональности версия, в которой не работают вызовы макрофункций.
     *
     * @param cell  ячейка в которую надо установить значение.
     * @param value  объект на основании которого устанавливается значение ячейки.
     * @see org.echosoft.framework.reports.processor.ExcelReportProcessor#renderCell(ExecutionContext, Object)
     */
    public static void setCellValue(final HSSFCell cell, final Object value) {
        if (value == null) {
            cell.setCellType(HSSFCell.CELL_TYPE_BLANK);
        } else
        if (value instanceof Date) {
            cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            cell.setCellValue( (Date)value );
        } else
        if (value instanceof Calendar) {
            cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            cell.setCellValue( (Calendar)value );
        } else
        if (value instanceof Double) {
            cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            cell.setCellValue((Double)value);
        } else
        if (value instanceof Number) {
            cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            cell.setCellValue( new Double(((Number)value).doubleValue()) );
        } else
        if (value instanceof Boolean) {
            cell.setCellType(HSSFCell.CELL_TYPE_BOOLEAN);
            cell.setCellValue( (Boolean)value );
        } else
        if (value instanceof HSSFRichTextString) {
            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
            cell.setCellValue( (HSSFRichTextString)value );
        } else {
            final String text = value.toString();
            if (cell.getCellType()==HSSFCell.CELL_TYPE_FORMULA) {
                cell.setCellFormula(text);
            } else
            if (text.startsWith(FORMULA)) {
                cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
                cell.setCellFormula( text.substring(FORMULA_LENGTH) );
            } else {
                cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                cell.setCellValue( new HSSFRichTextString(text) );
            }
        }
    }

    /**
     * Возвращает <code>true</code> если аргумент соответствует всем требованиям предъявляемым к имени колонки.
     * @param columnName  наименование колонки в Excel.
     * @return <code>true</code> если аргумент соответствует всем требованиям предъявляемым к имени колонки.
     */
    public static boolean isValidColumnName(final String columnName) {
        if (columnName==null || columnName.length()==0)
            return false;
        for (int i = columnName.length()-1; i>=0; i--) {
            final char c = columnName.charAt(i);
            if ( !( (c>='A' && c<='Z') || (c>='a' && c<='z') ) ) {
                return false;  // обнаружен недопустимый символ.
            }
        }
        return true;
    }

    /**
     * Конвертирует наименование колонки из формата ALPHA-26 в десятичные числа.
     * 
     * @param columnName  наименование колонки (A, B, C, ... Z, AA, ... AZ, ...)
     * @return порядковый номер колонки (начиная с 0).
     *   Если наименование колонки содержит недопустимые символы то метод вернет <code>-1</code>.
     */
    public static int getColumnNumber(final String columnName) {
        int result = 0;
        int pos = 0;
        for (int i = columnName.length()-1; i>=0; i--) {
            final char c = columnName.charAt(i);
            if ( !( (c>='A' && c<='Z') || (c>='a' && c<='z') ) ) {
                return -1;  // обнаружен недопустимый символ.
            }
            // Character.getNumericValue() возвращает значения 10-35 для символов A-Z
            final int shift = (int)Math.pow(26, pos++);
            result += (Character.getNumericValue(c)-9) * shift;
        }
        return result - 1;
    }

    /**
     * Конвертирует массив наименований колонок указанных в формате ALPHA-26 в массив порядковых номеров (начиная с 0) этих колонок.
     *
     * @param columnsNames  массив имен перечня колонок
     * @return массив порядковых номеров соответствующих колонок (начиная с 0).
     */
    public static int[] getColumnsNumbers(final String[] columnsNames) {
        if (columnsNames==null)
            return null;
        final int[] result = new int[columnsNames.length];
        for (int i=0; i<columnsNames.length; i++) {
            result[i] = getColumnNumber(columnsNames[i]);
        }
        return result;
    }

    /**
     * Конвертирует порядковый номер колонки (начиная с 0) в формат ALPHA-26.
     *
     * @param col  порядковый номер колонки, начиная с 0.
     * @return  наименование колонки в формате  A, B, C, ... Z, AA, ... AZ, ...
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
            final char colChar = (char)(thisPart+64);
            colRef = colChar + colRef;
        }
        return colRef;
    }

    /**
     * Для указанной ячейки возвращает ее координаты вида: <code><u>&lt;название колонки&gt;&lt;номер строки&gt;</u></code>.<br/>
     * например:  <code>A1</code>, <code>BY234</code>, ...
     *
     * @param cell  ячейка таблицы.
     * @return  строка с адресом ячейки на листе отчета.
     */
    public static String getCellName(final HSSFCell cell) {
        return cell!=null
                ? getColumnName(cell.getColumnIndex()) + (cell.getRowIndex()+1) 
                : null;
    }

    /**
     * Компилирует вызов указанной функции Excel для текущей группы.<br/>
     * <strong>Внимание!</strong> При вызове данной функции следует самостоятельно контролировать количество аргументов формируемой функции.
     * Если их количество будет слишком большим то в процессе формирования отчета возникнет ошибка. Для справки: в версии POI 3.2 большинство
     * функций ограничены 30 аргументами. 
     *
     * @param ectx  контекст выполнения.
     * @param function  название функции.
     * @return  строка с вызовом функции или <code>null</code> если вызов требуемой функции в данный момент невозможен.
     */
    public static String makeGroupFormula(final ExecutionContext ectx, final String function) {
        final SectionContext sctx = ectx.sectionContext;
        final Group group = sctx.gm.getCurrentGroup();
        if (group==null)
            return null;

        final String colname = POIUtils.getColumnName(ectx.cell.getColumnIndex());
        final StringBuilder formula = new StringBuilder(16);
        formula.append(function);
        formula.append('(');

        final int chsize = group.children.size();
        if (chsize>0) {
            for (int i=0; i<chsize; i++) {
                if (i>0)
                    formula.append(',');
                final Group child = group.children.get(i);
                formula.append(colname);
                formula.append(child.startRow+1);
            }
        } else
        if (group.recordsHeight!=null && group.recordsHeight==1) {
            final int start = group.startRow + group.model.getRowsCount();
            final int end = start + group.records.size();
            formula.append(colname);
            formula.append(start+1);
            formula.append(':');
            formula.append(colname);
            formula.append(end);
        } else {
            for (int i=0, rsize=group.records.size(); i<rsize; i++) {
                if (i>0)
                    formula.append(',');
                formula.append(colname);
                formula.append(group.records.get(i)+1);
            }
        }
        formula.append(')');
        return formula.toString();
    }

    /**
     * Метод проверяет наличие в рабочей книге зарегистрированного фонта с указанными характеристиками и если он отсутствует то регистрирует его.
     * @param wb  рабочая книга в которой должен быть зарегистрирован требуемый фонт.
     * @param font  характеристики требуемого фонта.
     * @return  зарегистрированный в рабочей книге фонт с требуемыми характеристиками.
     */
    public static HSSFFont ensureFontExists(final HSSFWorkbook wb, final FontModel font) {
        final short colorId = font.getColor()!=null ? font.getColor().getId() : 0;
        HSSFFont f = wb.findFont(font.getBoldWeight(), colorId, font.getFontHeight(),
                font.getFontName(), font.isItalic(), font.isStrikeout(), font.getTypeOffset(), font.getUnderline());
        if (f==null) {
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
     * Копирует все свойства исходного фонта в целевой. И исходный и целевой фонты должны относиться к одному и тому же документу Excel.
     * Данный метод используется для того чтобы в последствии поменять в новом фонте один или несколько свойств не затрагивая при этом свойства исходного фонта.
     * @param wb   документ Excel.
     * @param src  фонт взятый в качестве шаблона.
     * @return созданная в этом же документе копия шрифта.
     */
    public static HSSFFont copyFont(final HSSFWorkbook wb, final HSSFFont src) {
        final HSSFFont dst = wb.createFont();
        dst.setBoldweight(src.getBoldweight());
        dst.setCharSet(src.getCharSet());
        dst.setColor(src.getColor());
        dst.setFontHeight(src.getFontHeight());
        dst.setFontName(src.getFontName());
        dst.setItalic(src.getItalic());
        dst.setStrikeout(src.getStrikeout());
        dst.setTypeOffset(src.getTypeOffset());
        dst.setUnderline(src.getUnderline());
        return dst;
    }

    /**
     * Создает новый стиль в документе Excel который полностью копирует свойства некоторого исходного стиля этого же документа.
     * Данный метод используется для того чтобы в последствии поменять в новом стиле один или несколько свойств не затрагивая при этом свойства исходного стиля.
     * @param wb   документ Excel.
     * @param src  стиль ячейки взятый в качестве шаблона.
     * @return  созданная копия исходного стиля в этом же документе.
     */
    public static HSSFCellStyle copyStyle(final HSSFWorkbook wb, final HSSFCellStyle src) {
        final HSSFCellStyle dst = wb.createCellStyle();
        dst.setAlignment( src.getAlignment() );
        dst.setBorderBottom( src.getBorderBottom() );
        dst.setBorderLeft( src.getBorderLeft() );
        dst.setBorderRight( src.getBorderRight() );
        dst.setBorderTop( src.getBorderTop() );
        dst.setBottomBorderColor( src.getBottomBorderColor() );
        dst.setDataFormat( src.getDataFormat() );
        dst.setFillForegroundColor( src.getFillForegroundColor() );
        dst.setFillBackgroundColor( src.getFillBackgroundColor() );
        dst.setFillPattern( src.getFillPattern() );
        dst.setFont( src.getFont(wb) );
        dst.setHidden( src.getHidden() );
        dst.setIndention( src.getIndention() );
        dst.setLeftBorderColor( src.getLeftBorderColor() );
        dst.setLocked( src.getLocked() );
        dst.setRightBorderColor( src.getRightBorderColor() );
        dst.setRotation( src.getRotation() );
        dst.setTopBorderColor( src.getTopBorderColor() );
        dst.setVerticalAlignment( src.getVerticalAlignment() );
        dst.setWrapText( src.getWrapText() );
        return dst;
    }

    /**
     * Находит или создает новый стиль на основе исходного стиля примененного к текущей обрабатываемой ячейке в котором были изменены цвета шрифта и/или фона.
     * Информация о всех созданных данным методом стилях сохраняется в контексте выполнения в пространстве имен {@link org.echosoft.framework.reports.model.el.ELContext.Scope#VAR}.
     * @param ectx  контекст выполнения. Используется для получения информации о текущей ячейке и для кэширования информации о вновь созданных в документе стилях.
     * @param color  Определяет цвет шрифта в данной ячейке. Аргумент содержит идентификатор цвета в документе или <code>-1</code> если цвет шрифта должен остаться без изменений.
     * @param bgColor Определяет цвет фона в данной ячейке. Аргумент содержит идентификатор цвета в документе или <code>-1</code> если цвет шрифта должен остаться без изменений.
     * @return копия оригинального стиля указанной ячейки с измененными цветами фона и шрифта. Этот стиль уже зарегистрирован в рабочей книге.
     */
    public static HSSFCellStyle getAltColorStyle(final ExecutionContext ectx, final int color, final int bgColor) {
        return getAltColorStyle(ectx, ectx.cell.getCellStyle(), color, bgColor);
    }

    /**
     * Находит или создает новый стиль на основе указанного в аргументе исходного стиля в котором были изменены цвета шрифта и/или фона.
     * Информация о всех созданных данным методом стилях сохраняется в контексте выполнения в пространстве имен {@link org.echosoft.framework.reports.model.el.ELContext.Scope#VAR}.
     * @param ectx  контекст выполнения. Используется для получения информации о текущей ячейке и для кэширования информации о вновь созданных в документе стилях.
     * @param originalStyle  исходный стиль, который должен послужить основой для создаваемого стиля.
     * @param color  Определяет цвет шрифта в данной ячейке. Аргумент содержит идентификатор цвета в документе или <code>-1</code> если цвет шрифта должен остаться без изменений.
     * @param bgColor Определяет цвет фона в данной ячейке. Аргумент содержит идентификатор цвета в документе или <code>-1</code> если цвет шрифта должен остаться без изменений.
     * @return копия оригинального стиля указанной ячейки с измененными цветами фона и шрифта. Этот стиль уже зарегистрирован в рабочей книге.
     */
    public static HSSFCellStyle getAltColorStyle(final ExecutionContext ectx, final HSSFCellStyle originalStyle, final int color, final int bgColor) {
        final String styleName = "alt.color.style:"+originalStyle.getIndex()+':'+color+':'+bgColor;
        HSSFCellStyle style = (HSSFCellStyle)ectx.elctx.getVariables().get(styleName);
        if (style==null) {
            style = POIUtils.copyStyle(ectx.wb, originalStyle);
            if (color>=0) {
                final HSSFFont of = ectx.wb.getFontAt( originalStyle.getFontIndex() );
                HSSFFont nf = ectx.wb.findFont(of.getBoldweight(), (short)color, of.getFontHeight(),
                        of.getFontName(), of.getItalic(), of.getStrikeout(), of.getTypeOffset(), of.getUnderline());
                if (nf==null) {
                    nf = POIUtils.copyFont(ectx.wb, of);
                    nf.setColor( (short)color );
                }
                style.setFont(nf);
            }
            if (bgColor>=0) {
                style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                style.setFillForegroundColor( (short)bgColor );
            }
            ectx.elctx.getVariables().put(styleName, style);
        }
        return style;
    }

}
