package org.echosoft.framework.reports.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;

/**
 * Содержит полный реестр стилей ячеек используемых в макете отчета.
 *
 * @author Anton Sharapov
 */
public class StylePalette implements Serializable, Cloneable {

    private Map<Short,ColorModel> colors;
    private Map<Short,FontModel> fonts;
    private Map<Short,CellStyleModel> styles;

    private final transient HSSFWorkbook wb;
    private final transient HSSFPalette palette;

    public StylePalette(final HSSFWorkbook wb) {
        this.wb = wb;
        this.palette = wb!=null ? wb.getCustomPalette() : null;

        colors = new HashMap<Short,ColorModel>();
        fonts = new HashMap<Short,FontModel>();
        styles = new HashMap<Short,CellStyleModel>();
    }

    /**
     * Проверяет наличие указанного стиля в реестре.
     * @param style  стиль ячейки шаблона.
     * @return  true если данный стиль уже зарегистрирован в данном реестре.
     */
    public boolean containsStyle(final CellStyle style) {
        return styles.containsKey(style.getIndex());
    }

    /**
     * Регистрирует указанный стиль шаблона в реестре (если он не был зарегистрирован ранее)
     * и возвращает ключ доступа к нему в виде его порядкового номера в списке всех стилей.
     * @param s  стиль ячейки шаблона.
     * @return  модель стиля ячейки не привязанная к конкретному экземпляру HSSFWorkbook.
     */
    public CellStyleModel ensureStyleRegistered(final CellStyle s) {
        CellStyleModel style = styles.get(s.getIndex());
        if (style==null) {
            style = new CellStyleModel();
            style.setId( s.getIndex() );
            style.setAlignment( s.getAlignment() );
            style.setBorderBottom( s.getBorderBottom() );
            style.setBorderLeft( s.getBorderLeft() );
            style.setBorderRight( s.getBorderRight() );
            style.setBorderTop( s.getBorderTop() );
            style.setBottomBorderColor( getColorModel(s.getBottomBorderColor()) );
            style.setDataFormat( s.getDataFormatString() );
            style.setFillBackgroundColor( getColorModel(s.getFillBackgroundColor()) );
            style.setFillForegroundColor( getColorModel(s.getFillForegroundColor()) );
            style.setFillPattern( s.getFillPattern() );
            style.setHidden( s.getHidden() );
            style.setIndention( s.getIndention() );
            style.setLeftBorderColor( getColorModel(s.getLeftBorderColor()) );
            style.setLocked( s.getLocked() );
            style.setRightBorderColor( getColorModel(s.getRightBorderColor()) );
            style.setRotation( s.getRotation() );
            style.setTopBorderColor( getColorModel(s.getTopBorderColor()) );
            style.setVerticalAlignment( s.getVerticalAlignment() );
            style.setWrapText( s.getWrapText() );
            style.setFont( getFontModel(s.getFontIndex()) );
            styles.put(s.getIndex(), style);
        }
        return style;
    }

    private FontModel getFontModel(final short fontIndex) {
        FontModel font = fonts.get(fontIndex);
        if (font==null) {
            final Font f = wb.getFontAt(fontIndex);
            font = new FontModel();
            font.setId( f.getIndex() );
            font.setBoldWeight( f.getBoldweight() );
            font.setCharSet( f.getCharSet() );
            font.setFontHeight( f.getFontHeight() );
            font.setFontName( f.getFontName() );
            font.setItalic( f.getItalic() );
            font.setStrikeout( f.getStrikeout() );
            font.setTypeOffset( f.getTypeOffset() );
            font.setUnderline( f.getUnderline() );
            font.setColor( getColorModel(f.getColor()) );
            fonts.put(fontIndex, font);
        }
        return font;
    }

    private ColorModel getColorModel(final short colorIndex) {
        ColorModel color = colors.get(colorIndex);
        if (color==null) {
            final HSSFColor c = palette.getColor(colorIndex);
            if (c!=null) {
                final short[] triplet = c.getTriplet();
                color = new ColorModel(colorIndex, (byte)triplet[0], (byte)triplet[1], (byte)triplet[2]);
                colors.put(colorIndex, color);
            }
        }
        return color;
    }


    /**
     * Полный список всех зарегистрированных стилей ячеек которые используются при построении данного отчета.
     * @return  полный список всех зарегистрированных стилей ячеек.
     */
    public Map<Short,CellStyleModel> getStyles() {
        return styles;
    }

    /**
     * Возвращает полный список всех используемых в отчете шрифтов.
     * @return  полный список всех используемых в отчете шрифтов.
     */
    public Map<Short,FontModel> getFonts() {
        return fonts;
    }

    /**
     * Возвращает полный список всех используемых в отчете цветов.
     * @return  полный список всех используемых в отчете цветов.
     */
    public Map<Short,ColorModel> getColors() {
        return colors;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final StylePalette result = (StylePalette)super.clone();
        result.colors = new HashMap<Short,ColorModel>(colors.size());
        for (Map.Entry<Short,ColorModel> e : colors.entrySet()) {
            result.colors.put(e.getKey(), (ColorModel)e.getValue().clone());
        }
        result.fonts = new HashMap<Short,FontModel>(fonts.size());
        for (Map.Entry<Short,FontModel> e : fonts.entrySet()) {
            result.fonts.put(e.getKey(), (FontModel)e.getValue().clone());
        }
        result.styles = new HashMap<Short,CellStyleModel>(styles.size());
        for (Map.Entry<Short,CellStyleModel> e : styles.entrySet()) {
            result.styles.put(e.getKey(), (CellStyleModel)e.getValue().clone());
        }
        return result;
    }

    @Override
    public String toString() {
        return "[StylePalette{styles:"+styles.size()+", fonts:"+fonts.size()+"}]";
    }
}
