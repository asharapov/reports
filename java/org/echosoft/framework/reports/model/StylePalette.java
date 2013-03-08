package org.echosoft.framework.reports.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Содержит полный реестр стилей ячеек используемых в макете отчета.
 *
 * @author Anton Sharapov
 */
public class StylePalette implements Serializable, Cloneable {

    private Map<Short, ColorModel> colors;
    private Map<Short, FontModel> fonts;
    private Map<Short, CellStyleModel> styles;

    public StylePalette(final Workbook wb) {
        colors = new HashMap<Short, ColorModel>();
        fonts = new HashMap<Short, FontModel>();
        styles = new HashMap<Short, CellStyleModel>();

        if (wb instanceof HSSFWorkbook) {
            init((HSSFWorkbook) wb);
        } else
        if (wb instanceof XSSFWorkbook) {
            init((XSSFWorkbook) wb);
        } else
            throw new IllegalArgumentException("Unknown workbook implementation: " + wb);
    }

    private void init(final XSSFWorkbook wb) {
        final Map<Integer, ColorModel> hashedColors = new HashMap<Integer, ColorModel>();

        for (short i = 0, cnt = wb.getNumberOfFonts(); i < cnt; i++) {
            final XSSFFont f = wb.getFontAt(i);
            if (f == null)
                throw new IllegalStateException("Reference to nonexistent font at index: " + i);
            final FontModel font = new FontModel();
            font.setId(f.getIndex());
            font.setBoldWeight(f.getBoldweight());
            font.setCharSet(f.getCharSet());
            font.setFontHeight(f.getFontHeight());
            font.setFontName(f.getFontName());
            font.setItalic(f.getItalic());
            font.setStrikeout(f.getStrikeout());
            font.setTypeOffset(f.getTypeOffset());
            font.setUnderline(f.getUnderline());
            font.setColor(ensureColorRegistered(f.getXSSFColor(), wb, hashedColors));
            fonts.put(f.getIndex(), font);
        }
        for (short i = 0, cnt = wb.getNumCellStyles(); i < cnt; i++) {
            final XSSFCellStyle s = wb.getCellStyleAt(i);
            final CellStyleModel style = new CellStyleModel();
            style.setId(s.getIndex());
            style.setAlignment(s.getAlignment());
            style.setBorderBottom(s.getBorderBottom());
            style.setBorderLeft(s.getBorderLeft());
            style.setBorderRight(s.getBorderRight());
            style.setBorderTop(s.getBorderTop());
            style.setBottomBorderColor(ensureColorRegistered(s.getBottomBorderXSSFColor(), wb, hashedColors));
            style.setDataFormat(s.getDataFormatString());
            style.setFillBackgroundColor(ensureColorRegistered(s.getFillBackgroundXSSFColor(), wb, hashedColors));
            style.setFillForegroundColor(ensureColorRegistered(s.getFillForegroundXSSFColor(), wb, hashedColors));
            style.setFillPattern(s.getFillPattern());
            style.setHidden(s.getHidden());
            style.setIndention(s.getIndention());
            style.setLeftBorderColor(ensureColorRegistered(s.getLeftBorderXSSFColor(), wb, hashedColors));
            style.setLocked(s.getLocked());
            style.setRightBorderColor(ensureColorRegistered(s.getRightBorderXSSFColor(), wb, hashedColors));
            style.setRotation(s.getRotation());
            style.setTopBorderColor(ensureColorRegistered(s.getTopBorderXSSFColor(), wb, hashedColors));
            style.setVerticalAlignment(s.getVerticalAlignment());
            style.setWrapText(s.getWrapText());
            final FontModel font = fonts.get(s.getFontIndex());
            if (font == null)
                throw new IllegalStateException("Reference to nonexistent font at index: " + s.getFontIndex());
            style.setFont(font);
            styles.put(s.getIndex(), style);
        }

        for (ColorModel color : hashedColors.values()) {
            colors.put(color.getId(), color);
        }
    }

    private ColorModel ensureColorRegistered(final XSSFColor color, final XSSFWorkbook wb, final Map<Integer, ColorModel> hashedColors) {
        final byte[] rgb = resolveColor0(wb, color);
        if (rgb == null)
            return null;
        final int hash = ColorModel.getHash(rgb);
        ColorModel result = hashedColors.get(hash);
        if (result == null) {
            result = new ColorModel((short)(hashedColors.size() + 1), rgb);
            hashedColors.put(hash, result);
        }
        return result;
    }

    private static byte[] resolveColor0(final XSSFWorkbook wb, XSSFColor color) {
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

        return color.getRgbWithTint();
    }

    private void init(final HSSFWorkbook wb) {
        final HSSFPalette palette = wb.getCustomPalette();
        for (short i = 0, cnt = wb.getNumCellStyles(); i < cnt; i++) {
            final HSSFCellStyle s = wb.getCellStyleAt(i);
            final CellStyleModel style = new CellStyleModel();
            style.setId(s.getIndex());
            style.setAlignment(s.getAlignment());
            style.setBorderBottom(s.getBorderBottom());
            style.setBorderLeft(s.getBorderLeft());
            style.setBorderRight(s.getBorderRight());
            style.setBorderTop(s.getBorderTop());
            style.setBottomBorderColor(ensureColorRegistered(s.getBottomBorderColor(), palette));
            style.setDataFormat(s.getDataFormatString());
            style.setFillBackgroundColor(ensureColorRegistered(s.getFillBackgroundColor(), palette));
            style.setFillForegroundColor(ensureColorRegistered(s.getFillForegroundColor(), palette));
            style.setFillPattern(s.getFillPattern());
            style.setHidden(s.getHidden());
            style.setIndention(s.getIndention());
            style.setLeftBorderColor(ensureColorRegistered(s.getLeftBorderColor(), palette));
            style.setLocked(s.getLocked());
            style.setRightBorderColor(ensureColorRegistered(s.getRightBorderColor(), palette));
            style.setRotation(s.getRotation());
            style.setTopBorderColor(ensureColorRegistered(s.getTopBorderColor(), palette));
            style.setVerticalAlignment(s.getVerticalAlignment());
            style.setWrapText(s.getWrapText());
            style.setFont(ensureFontRegistered(s.getFontIndex(), wb, palette));
            styles.put(s.getIndex(), style);
        }
    }

    private ColorModel ensureColorRegistered(final short colorIndex, final HSSFPalette palette) {
        ColorModel color = colors.get(colorIndex);
        if (color == null) {
            final HSSFColor c = palette.getColor(colorIndex);       // допускается что метод вернет null  (т.е. используется цвет по умолчанию)
            if (c != null) {
                color = new ColorModel(colorIndex, c.getTriplet());
                colors.put(colorIndex, color);
            }
        }
        return color;
    }

    private FontModel ensureFontRegistered(final short fontIndex, final Workbook wb, final HSSFPalette palette) {
        FontModel font = fonts.get(fontIndex);
        if (font == null) {
            final Font f = wb.getFontAt(fontIndex);
            if (f == null)
                throw new IllegalStateException("Reference to nonexistent font at index: " + fontIndex);
            font = new FontModel();
            font.setId(f.getIndex());
            font.setBoldWeight(f.getBoldweight());
            font.setCharSet(f.getCharSet());
            font.setFontHeight(f.getFontHeight());
            font.setFontName(f.getFontName());
            font.setItalic(f.getItalic());
            font.setStrikeout(f.getStrikeout());
            font.setTypeOffset(f.getTypeOffset());
            font.setUnderline(f.getUnderline());
            font.setColor(ensureColorRegistered(f.getColor(), palette));
            fonts.put(fontIndex, font);
        }
        return font;
    }



    /**
     * Возвращает описание указанного стиля из шаблона документа.
     *
     * @param styleIndex порядковый номер стиля из шаблона документа.
     * @return описание стиля с заданным в аргументе индексом.
     */
    public CellStyleModel getStyleModel(final short styleIndex) {
        return styles.get(styleIndex);
    }

    /**
     * Полный список всех зарегистрированных стилей ячеек которые используются при построении данного отчета.
     *
     * @return полный список всех зарегистрированных стилей ячеек.
     */
    public Map<Short, CellStyleModel> getStyles() {
        return styles;
    }

    /**
     * Возвращает полный список всех используемых в отчете шрифтов.
     *
     * @return полный список всех используемых в отчете шрифтов.
     */
    public Map<Short, FontModel> getFonts() {
        return fonts;
    }

    /**
     * Возвращает полный список всех используемых в отчете цветов.
     *
     * @return полный список всех используемых в отчете цветов.
     */
    public Map<Short, ColorModel> getColors() {
        return colors;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final StylePalette result = (StylePalette) super.clone();
        result.colors = new HashMap<Short, ColorModel>(colors.size());
        for (Map.Entry<Short, ColorModel> e : colors.entrySet()) {
            result.colors.put(e.getKey(), (ColorModel) e.getValue().clone());
        }
        result.fonts = new HashMap<Short, FontModel>(fonts.size());
        for (Map.Entry<Short, FontModel> e : fonts.entrySet()) {
            result.fonts.put(e.getKey(), (FontModel) e.getValue().clone());
        }
        result.styles = new HashMap<Short, CellStyleModel>(styles.size());
        for (Map.Entry<Short, CellStyleModel> e : styles.entrySet()) {
            result.styles.put(e.getKey(), (CellStyleModel) e.getValue().clone());
        }
        return result;
    }

    @Override
    public String toString() {
        return "[StylePalette{styles:" + styles.size() + ", fonts:" + fonts.size() + ", colors:" + colors.size() + "}]";
    }
}
