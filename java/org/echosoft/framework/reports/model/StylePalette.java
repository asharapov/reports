package org.echosoft.framework.reports.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

/**
 * Содержит полный реестр стилей ячеек используемых в макете отчета.
 *
 * @author Anton Sharapov
 */
public class StylePalette implements Serializable, Cloneable {

    private Map<Short,Color> colors;
    private Map<Short,Font> fonts;
    private Map<Short,CellStyle> styles;

    private final transient HSSFWorkbook wb;
    private final transient HSSFPalette palette;

    public StylePalette(final HSSFWorkbook wb) {
        this.wb = wb;
        this.palette = wb!=null ? wb.getCustomPalette() : null;

        colors = new HashMap<Short,Color>();
        fonts = new HashMap<Short,Font>();
        styles = new HashMap<Short,CellStyle>();
    }

    /**
     * Проверяет наличие указанного стиля в реестре.
     * @param style  стиль ячейки шаблона.
     * @return  true если данный стиль уже зарегистрирован в данном реестре.
     */
    public boolean containsStyle(final HSSFCellStyle style) {
        return styles.containsKey(style.getIndex());
    }

    /**
     * Регистрирует указанный стиль шаблона в реестре (если он не был зарегистрирован ранее)
     * и возвращает ключ доступа к нему в виде его порядкового номера в списке всех стилей.
     * @param s  стиль ячейки шаблона.
     * @return  модель стиля ячейки не привязанная к конкретному экземпляру HSSFWorkbook.
     */
    public CellStyle ensureStyleRegistered(final HSSFCellStyle s) {
        CellStyle style = styles.get(s.getIndex());
        if (style==null) {
            style = new CellStyle();
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

    private Font getFontModel(final short fontIndex) {
        Font font = fonts.get(fontIndex);
        if (font==null) {
            final HSSFFont f = wb.getFontAt(fontIndex);
            font = new Font();
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

    private Color getColorModel(final short colorIndex) {
        Color color = colors.get(colorIndex);
        if (color==null) {
            final HSSFColor c = palette.getColor(colorIndex);
            if (c!=null) {
                final short[] triplet = c.getTriplet();
                color = new Color(colorIndex, (byte)triplet[0], (byte)triplet[1], (byte)triplet[2]);
                colors.put(colorIndex, color);
            }
        }
        return color;
    }


    /**
     * Полный список всех зарегистрированных стилей ячеек которые используются при построении данного отчета.
     * @return  полный список всех зарегистрированных стилей ячеек.
     */
    public Map<Short,CellStyle> getStyles() {
        return styles;
    }

    /**
     * Возвращает полный список всех используемых в отчете шрифтов.
     * @return  полный список всех используемых в отчете шрифтов.
     */
    public Map<Short,Font> getFonts() {
        return fonts;
    }

    /**
     * Возвращает полный список всех используемых в отчете цветов.
     * @return  полный список всех используемых в отчете цветов.
     */
    public Map<Short,Color> getColors() {
        return colors;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final StylePalette result = (StylePalette)super.clone();
        result.colors = new HashMap<Short,Color>(colors.size());
        for (Map.Entry<Short,Color> e : colors.entrySet()) {
            result.colors.put(e.getKey(), (Color)e.getValue().clone());
        }
        result.fonts = new HashMap<Short,Font>(fonts.size());
        for (Map.Entry<Short,Font> e : fonts.entrySet()) {
            result.fonts.put(e.getKey(), (Font)e.getValue().clone());
        }
        result.styles = new HashMap<Short,CellStyle>(styles.size());
        for (Map.Entry<Short,CellStyle> e : styles.entrySet()) {
            result.styles.put(e.getKey(), (CellStyle)e.getValue().clone());
        }
        return result;
    }

    @Override
    public String toString() {
        return "[StylePalette{styles:"+styles.size()+", fonts:"+fonts.size()+"}]";
    }
}
