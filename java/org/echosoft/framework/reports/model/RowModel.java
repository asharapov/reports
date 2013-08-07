package org.echosoft.framework.reports.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Описывает шаблон одной строки в отчете.
 *
 * @author Anton Sharapov
 */
public class RowModel implements Serializable, Cloneable {

    /**
     * Список ячеек в строке.
     * Некоторые элементы этого массива могут быть равны <code>null</code>. Это нормально для т.н. "пустых" ячеек.
     */
    private List<CellModel> cells;

    /**
     * Высота строки.
     */
    private short height;

    /**
     * <code>true</code> если строка должна быть скрыта от пользователя.
     */
    private boolean hidden;


    public RowModel() {
        cells = new ArrayList<CellModel>();
        height = -1;
    }

    /**
     * Возвращает информацию о ячейках данной строки. Если какая-то ячейка отсутствует в шаблоне то
     * вместо нее возвращается <code>null</code>.
     *
     * @return Список ячеек строки. Список не может быть <code>null</code>.
     */
    public List<CellModel> getCells() {
        return cells;
    }

    /**
     * Возвращает высоту строки в twips (1/20 point) или -1 если следует использовать высоту по умолчанию.
     *
     * @return высота строки.
     */
    public short getHeight() {
        return height;
    }

    /**
     * Устанавливает высоту строки в 1/20 точки (point).
     *
     * @param height для установки высоты по умолчанию следут указать -1.
     */
    public void setHeight(final short height) {
        this.height = height;
    }

    /**
     * Определяет следует ли показывать эту строку пользователю или она должна быть скрытой.
     *
     * @return Возвращает <code>true</code> если строка должна быть скрытой от пользователя.
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Устанавливает признак видимости данной строки пользователю.
     *
     * @param hidden <code>true</code> если строка должна быть скрытой от пользователя.
     */
    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final RowModel result = (RowModel) super.clone();
        result.cells = new ArrayList<CellModel>(cells.size());
        for (CellModel cm : cells) {
            result.cells.add(cm != null ? (CellModel) cm.clone() : null);
        }
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(1024);
        buf.append("[ \n");
        for (CellModel cell : cells) {
            buf.append("  ").append(cell).append('\n');
        }
        buf.append("]\n");
        return buf.toString();
    }
}
