package org.echosoft.framework.reports.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.record.PaletteRecord;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.echosoft.framework.reports.util.POIUtils;

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
            font.setFontName(f.getFontName());
            font.setCharSet(f.getCharSet());
            font.setFontHeight(f.getFontHeight());
            font.setBoldWeight(f.getBoldweight());
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
            style.setVerticalAlignment(s.getVerticalAlignment());
            style.setDataFormat(s.getDataFormatString());
            style.setHidden(s.getHidden());
            style.setIndention(s.getIndention());
            style.setLocked(s.getLocked());
            style.setRotation(s.getRotation());
            style.setWrapText(s.getWrapText());
            style.setBorderTop(s.getBorderTop());
            style.setBorderRight(s.getBorderRight());
            style.setBorderBottom(s.getBorderBottom());
            style.setBorderLeft(s.getBorderLeft());
            style.setTopBorderColor(ensureColorRegistered(s.getTopBorderXSSFColor(), wb, hashedColors));
            style.setRightBorderColor(ensureColorRegistered(s.getRightBorderXSSFColor(), wb, hashedColors));
            style.setBottomBorderColor(ensureColorRegistered(s.getBottomBorderXSSFColor(), wb, hashedColors));
            style.setLeftBorderColor(ensureColorRegistered(s.getLeftBorderXSSFColor(), wb, hashedColors));
            style.setFillPattern(s.getFillPattern());
            style.setFillForegroundColor(ensureColorRegistered(s.getFillForegroundXSSFColor(), wb, hashedColors));
            style.setFillBackgroundColor(ensureColorRegistered(s.getFillBackgroundXSSFColor(), wb, hashedColors));
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
        final byte[] rgb = POIUtils.decodeXSSFColor(wb, color);
        if (rgb == null)
            return null;
        final int hash = ColorModel.getHash(rgb);
        ColorModel result = hashedColors.get(hash);
        if (result == null) {
            result = new ColorModel((short) (hashedColors.size() + 8), rgb);    // 0x08 - минимально допустимый индекс цвета в формате HSSF (а 0x40 - максимально допустимый)
            hashedColors.put(hash, result);
        }
        return result;
    }

    private void init(final HSSFWorkbook wb) {
        final HSSFPalette palette = wb.getCustomPalette();
        for (short i = 0, cnt = wb.getNumCellStyles(); i < cnt; i++) {
            final HSSFCellStyle s = wb.getCellStyleAt(i);
            final CellStyleModel style = new CellStyleModel();
            style.setId(s.getIndex());
            style.setAlignment(s.getAlignment());
            style.setVerticalAlignment(s.getVerticalAlignment());
            style.setDataFormat(s.getDataFormatString());
            style.setHidden(s.getHidden());
            style.setIndention(s.getIndention());
            style.setLocked(s.getLocked());
            style.setRotation(s.getRotation());
            style.setWrapText(s.getWrapText());
            style.setBorderTop(s.getBorderTop());
            style.setBorderRight(s.getBorderRight());
            style.setBorderBottom(s.getBorderBottom());
            style.setBorderLeft(s.getBorderLeft());
            style.setTopBorderColor(ensureColorRegistered(s.getTopBorderColor(), palette));
            style.setRightBorderColor(ensureColorRegistered(s.getRightBorderColor(), palette));
            style.setBottomBorderColor(ensureColorRegistered(s.getBottomBorderColor(), palette));
            style.setLeftBorderColor(ensureColorRegistered(s.getLeftBorderColor(), palette));
            style.setFillPattern(s.getFillPattern());
            style.setFillForegroundColor(ensureColorRegistered(s.getFillForegroundColor(), palette));
            style.setFillBackgroundColor(ensureColorRegistered(s.getFillBackgroundColor(), palette));
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


    /**
     * Копирует все стили из данной модели в целевой документ.
     *
     * @param wb целевой документ куда должны быть перенесены все цвета, шрифты и стили данной модели.
     * @return таблица трансляции кодой стилей модели в стили целевого документа (в процессе создания стилей в целевом документе у них меняется индекс).
     */
    public Map<Short, CellStyle> applyTo(final Workbook wb) {
        if (wb instanceof HSSFWorkbook) {
            return applyTo((HSSFWorkbook) wb);
        } else
        if (wb instanceof XSSFWorkbook) {
            return applyTo((XSSFWorkbook) wb);
        } else
            throw new IllegalArgumentException("Unknown workbook implementation: " + wb);
    }

    private Map<Short, CellStyle> applyTo(final HSSFWorkbook wb) {
        final Map<Short, CellStyle> result = new HashMap<Short, CellStyle>();

        if (colors.size() > PaletteRecord.STANDARD_PALETTE_SIZE)
            throw new RuntimeException("Too many colors in report for HSSF format");
        final HSSFPalette pal = wb.getCustomPalette();
        for (final ColorModel color : colors.values()) {
            pal.setColorAtIndex(color.getId(), color.getRed(), color.getGreen(), color.getBlue());
        }

        final Map<Short, Font> fontsmap = new HashMap<Short, Font>();
        for (final FontModel font : fonts.values()) {
            final Font f = POIUtils.ensureFontExists(wb, font);
            fontsmap.put(font.getId(), f);
        }

        final DataFormat formatter = wb.createDataFormat();
        for (final CellStyleModel style : styles.values()) {
            final short tbc = style.getTopBorderColor() != null ? style.getTopBorderColor().getId() : 0;
            final short rbc = style.getRightBorderColor() != null ? style.getRightBorderColor().getId() : 0;
            final short bbc = style.getBottomBorderColor() != null ? style.getBottomBorderColor().getId() : 0;
            final short lbc = style.getLeftBorderColor() != null ? style.getLeftBorderColor().getId() : 0;
            final short fbc = style.getFillBackgroundColor() != null ? style.getFillBackgroundColor().getId() : 0;
            final short ffc = style.getFillForegroundColor() != null ? style.getFillForegroundColor().getId() : 0;

            final CellStyle s = wb.createCellStyle();
            s.setAlignment(style.getAlignment());
            s.setVerticalAlignment(style.getVerticalAlignment());
            s.setDataFormat(formatter.getFormat(style.getDataFormat()));
            s.setHidden(style.isHidden());
            s.setIndention(style.getIndention());
            s.setLocked(style.isLocked());
            s.setRotation(style.getRotation());
            s.setWrapText(style.isWrapText());
            s.setBorderTop(style.getBorderTop());
            s.setBorderRight(style.getBorderRight());
            s.setBorderBottom(style.getBorderBottom());
            s.setBorderLeft(style.getBorderLeft());
            s.setFillPattern(style.getFillPattern());
            s.setFillForegroundColor(ffc);
            s.setFillBackgroundColor(fbc);
            s.setTopBorderColor(tbc);
            s.setRightBorderColor(rbc);
            s.setBottomBorderColor(bbc);
            s.setLeftBorderColor(lbc);
            s.setFont(fontsmap.get(style.getFont().getId()));
            result.put(style.getId(), s);
        }

        return result;
    }

    private Map<Short, CellStyle> applyTo(final XSSFWorkbook wb) {
        final Map<Short, CellStyle> result = new HashMap<Short, CellStyle>();

        final Map<Short, Font> fontsmap = new HashMap<Short, Font>();
        for (final FontModel font : fonts.values()) {
            final Font f = POIUtils.ensureFontExists(wb, font);
            fontsmap.put(font.getId(), f);
        }

        final DataFormat formatter = wb.createDataFormat();
        for (final CellStyleModel style : styles.values()) {
            final XSSFColor tbc = style.getTopBorderColor() != null ? POIUtils.makeXSSFColor(style.getTopBorderColor()) : null;
            final XSSFColor rbc = style.getRightBorderColor() != null ? POIUtils.makeXSSFColor(style.getRightBorderColor()) : null;
            final XSSFColor bbc = style.getBottomBorderColor() != null ? POIUtils.makeXSSFColor(style.getBottomBorderColor()) : null;
            final XSSFColor lbc = style.getLeftBorderColor() != null ? POIUtils.makeXSSFColor(style.getLeftBorderColor()) : null;
            final XSSFColor ffc = style.getFillForegroundColor() != null ? POIUtils.makeXSSFColor(style.getFillForegroundColor()) : null;
            final XSSFColor fbc = style.getFillBackgroundColor() != null ? POIUtils.makeXSSFColor(style.getFillBackgroundColor()) : null;

            final XSSFCellStyle s = wb.createCellStyle();
            s.setAlignment(style.getAlignment());
            s.setVerticalAlignment(style.getVerticalAlignment());
            s.setDataFormat(formatter.getFormat(style.getDataFormat()));
            s.setHidden(style.isHidden());
            s.setIndention(style.getIndention());
            s.setLocked(style.isLocked());
            s.setRotation(style.getRotation());
            s.setWrapText(style.isWrapText());
            s.setBorderTop(style.getBorderTop());
            s.setBorderRight(style.getBorderRight());
            s.setBorderBottom(style.getBorderBottom());
            s.setBorderLeft(style.getBorderLeft());
            s.setFillPattern(style.getFillPattern());
            if (ffc != null)
                s.setFillForegroundColor(ffc);
            if (fbc != null)
                s.setFillBackgroundColor(fbc);
            if (tbc != null)
                s.setTopBorderColor(tbc);
            if (rbc != null)
                s.setRightBorderColor(rbc);
            if (bbc != null)
                s.setBottomBorderColor(bbc);
            if (lbc != null)
                s.setLeftBorderColor(lbc);
            s.setFont(fontsmap.get(style.getFont().getId()));
            result.put(style.getId(), s);
        }

        return result;
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
