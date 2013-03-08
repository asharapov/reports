package org.echosoft.framework.reports.model;

import java.io.Serializable;

import org.apache.poi.ss.usermodel.Cell;
import org.echosoft.framework.reports.model.el.Expression;
import org.echosoft.framework.reports.model.el.ExpressionFactory;

/**
 * Содержит информацию об одной ячейке отчета.
 *
 * @author Anton Sharapov
 */
public class CellModel implements Serializable, Cloneable {

    /**
     * Выражение, на основе которого вычисляется содержимое ячейки.
     */
    private Expression expr;

    /**
     * Тип данных в ячейке.
     * @see org.apache.poi.ss.usermodel.Cell#getCellType()
     */
    private int type;

    /**
     * Стиль отображения ячейки.
     */
    private short style;

    private transient String dataFormat;

    /**
     * Клонирует массив ячеек отчета.
     * @param src  исходный массив ячеек.
     * @return  массив каждый элемент которого является клоном элемента с тем же индексом в массиве переданном на вход данного метода.
     * @throws CloneNotSupportedException  поднимается в случае возникновения каких-либо проблем с клонированием объектов.
     */
    public static CellModel[] cloneArray(final CellModel[] src) throws CloneNotSupportedException {
        final CellModel[] dst = new CellModel[src.length];
        for (int i=src.length-1; i>=0; i--) {
            final CellModel c = src[i];
            dst[i] = c!=null ? (CellModel)c.clone() : null;
        }
        return dst;
    }

    /**
     * @param cell  ячейка из шаблона отчета.
     * @param palette  реестр всех стилей используемых в данном отчете.
     */
    public CellModel(final Cell cell, final StylePalette palette) {
        expr = ExpressionFactory.makeExpression(cell);
        type = cell.getCellType();
        final CellStyleModel s = palette.getStyleModel(cell.getCellStyle().getIndex());
        style = s.getId();
        dataFormat = s.getDataFormat();
    }

    /**
     * Пытаемся создать новую пустую ячейку
     */
    public CellModel() {
        expr = ExpressionFactory.EMPTY_EXPRESSION;
        type = Cell.CELL_TYPE_BLANK;
        style = -1; // используем стиль по умолчанию ...
    }

    /**
     * @return  Выражение, на основе которого вычисляется содержимое данной ячейки.
     */
    public Expression getExpression() {
        return expr;
    }

    /**
     * Устанавливает новое содержимое в ячейке.
     * @param expr  выражение, определяющее содержимое данной ячейки. Не может быть <code>null</code>.
     */
    public void setExpression(Expression expr) {
        this.expr = expr!=null ? expr : ExpressionFactory.EMPTY_EXPRESSION;
    }

    /**
     * @return Тип данных в ячейке.
     * @see org.apache.poi.ss.usermodel.Cell#getCellType()
     */
    public int getType() {
        return type;
    }

    /**
     * Устанавливает новый тип содержимого ячейки.
     * @param type  новый тип содержимого ячейки.
     * @see org.apache.poi.ss.usermodel.Cell#getCellType()
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Стиль отображения ячейки.
     * @return  Стиль отображения ячейки.
     * @see StylePalette
     */
    public short getStyle() {
        return style;
    }
    public void setStyle(short style) {
        this.style = style;
        this.dataFormat = null;
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "[Cell{expr:"+expr+", type:"+type+", style:"+style+", fmt:"+dataFormat+"}]";
    }

}
