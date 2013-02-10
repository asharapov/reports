package org.echosoft.framework.reports.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * Содержит описание некоторой области шаблона отчета.
 * В первую очередь включает в себя описание ячеек входящих в эту область.
 *
 * @author Anton Sharapov
 */
public class Area implements Serializable, Cloneable {

    /**
     * Список строк отчета, каждая из которых представлена списком ячеек.
     * Если какая-то из строк не представлена в отчете (метод <code>sheet.getRow()</code> возвращает <code>null</code>)
     * то этой строке соответствует пустой список.
     * Если какая-то из ячеек не представлена в отчете (метод <code>row.getCell()</code> возввращает <code>null</code>)
     * то этой ячейке соответствует <code>null</code> в списке.
     */
    private List<RowModel> rows;

    /**
     * Информация о регионах (некоторого множества ячеек объединенных в одно целое) в рамках данной секции.
     * Все координаты приведены относительно начала данной секции, так что координата 0,0 будет соответствовать
     * самой левой колонке в самой верхней строке секции.
     * Ситуация когда координаты в регионе выходят за границы секции - недопустима.
     */
    private Collection<Region> regions;

    /**
     * Номер последней (самой правой) колонки области шаблона.
     */
    private int columnsCount;

    /**
     * Определяет должна ли данная область быть скрытой от пользователя или нет.
     */
    private boolean hidden;


    /**
     * Собирает информацию о некоторой области из указанного листа шаблона excel.
     * @param sheet  шаблон листа отчета в котором находятся данные для данной области.
     * @param top  номер верхней строки (начиная с 0) относящейся к указанной области.
     * @param height  количество строк в области. Должно быть как минимум 1.
     * @param palette  реестр всех стилей используемых в данном отчете.
     */
    public Area(final HSSFSheet sheet, final int top, final int height, final StylePalette palette) {
        if (sheet==null || top<0 || height<1)
            throw new IllegalArgumentException("Illegal area arguments");
        rows = new ArrayList<RowModel>();
        regions = new ArrayList<Region>();

        final int bottom = top + height - 1;
        int lastColumn = 0;
        for (int i=top; i<=bottom; i++) {
            final RowModel rm = new RowModel();
            rows.add( rm );
            final HSSFRow row = sheet.getRow(i);
            if (row==null) {
                rm.setHeight( sheet.getDefaultRowHeight() );
                continue;
            }
            rm.setHeight( row.getHeight() );
            rm.setHidden( row.getZeroHeight() );
            lastColumn = Math.max(lastColumn, row.getLastCellNum());
            for (int j=0; j<=row.getLastCellNum(); j++) {
                final HSSFCell cell = row.getCell(j);
                if (cell==null) {
                    rm.getCells().add( null );
                } else {
                    rm.getCells().add( new CellModel(cell, palette) );
                }
            }
        }
        this.columnsCount = lastColumn+1;

        final int regcount = sheet.getNumMergedRegions();
        for (int i=0; i<regcount; i++) {
            final CellRangeAddress src = sheet.getMergedRegion(i);
            if (src.getFirstRow()>=top && src.getLastRow()<=bottom) {
                final Region dst =
                        new Region(this, src.getFirstColumn(), src.getFirstRow()-top, src.getLastColumn(), src.getLastRow()-top);
                regions.add( dst );
            } else
            if (src.getFirstRow()<top && src.getLastRow()>=top)
                throw new IllegalArgumentException("Illegal region {top:"+top+", height:"+height+"} bounds: conflict with region {top:"+src.getFirstRow()+", bottom:"+src.getLastRow()+"}");
            if (src.getFirstRow()>=top && src.getFirstRow()<=bottom && src.getLastRow()>bottom)
                throw new IllegalArgumentException("Illegal region {top:"+top+", height:"+height+"} bounds: conflict with region {top:"+src.getFirstRow()+", bottom:"+src.getLastRow()+"}");
        }

        this.hidden = false;
    }

    /**
     * Добавляет в область новый регион.
     * @param firstCol  номер левой колонки.
     * @param firstRow  номер верхней строки.
     * @param lastCol  номер правой колонки.
     * @param lastRow  номер нижней строки.
     * @return  новый регион.
     * @throws IndexOutOfBoundsException в случае некорректно подготовленных данных.
     */
    public Region addRegion(int firstCol, int firstRow, int lastCol, int lastRow) {
        final Region result = new Region(this, firstCol, firstRow, lastCol, lastRow);
        regions.add( result );
        return result;
    }


    /**
     * Возвращает количество строк данной области.
     * @return  количество строк в области.
     */
    public int getRowsCount() {
        return rows.size();
    }

    /**
     * Возвращает максимальное количество колонок участвуюших в описании данной области.
     * @return  количество колонок области.
     */
    public int getColumnsCount() {
        return columnsCount;
    }

    /**
     * Возвращает список строк в данной области.
     * @return  список строк области.
     */
    public List<RowModel> getRows() {
        return rows;
    }

    /**
     * Возвращает список регионов которые присутствуют в данной области отчета.
     * @return  список регионов.
     */
    public Collection<Region> getRegions() {
        return regions;
    }

    /**
     * Возвращает шаблон конкретной ячейки в области.
     * @param colnum номер колонки в которой находится заданная ячейка.
     * @param rownum  номер строки в которой находится заданная ячейка. Отсчет номеров строк начинается с первой строки данной области строк.
     * @return  модель шаблона для указанной ячейки.
     */
    public CellModel getCell(final int colnum, final int rownum) {
        return rows.get(rownum).getCells().get(colnum);
    }

    /**
     * Возвращает подмножество регионов которые включают в себя указанную ячейку данной области отчета.
     * Подробная информация по заданной ячейке может быть получена при помощи вызова
     * <pre> final Cell cell = this.getRows().get(row).getCells().get(col); </pre>
     * @param colnum номер колонки в которой находится заданная ячейка.
     * @param rownum  номер строки в которой находится заданная ячейка. Отсчет номеров строк начинается с первой строки данной области строк.
     * @return  список всех регионов определенных в данной области которые включают в себя указанную ячейку области.
     */
    public Collection<Region> getRegionsWithCell(final int colnum, final int rownum) {
        final ArrayList<Region> result = new ArrayList<Region>();
        for (Region region : regions) {
            if (region.contains(colnum, rownum))
                result.add( region );
        }
        return result;
    }

    /**
     * Удаляет целую колонку из данной области. При этом пересчитываются координаты тех регионов которых затрагивает удаление данной колонки.
     * @param colnum  индекс удаляемой колонки.
     * @return  массив удаленных ячеек. первый элемент массива - удаленная ячейка из первой строки области, последний элемент массива - удаленная ячейка из последней строки области.
     *          Если в какой-то строке региона не было ячеек в указанной колонке то в соответствующий элемент массива сохраняется <code>null</code>.
     */
    public CellModel[] removeColumn(final int colnum) {
        if (colnum<0 || colnum>=columnsCount)
            throw new IndexOutOfBoundsException("Invalid range");
        // пересчитаем характеристики всех регионов области ...
        for (Iterator<Region> it=regions.iterator(); it.hasNext(); ) {
            final Region region = it.next();
            if (region.firstCol>colnum) {
                region.shiftColumns(-1);
            } else
            if (region.lastCol>=colnum) {
                if (region.lastCol>region.firstCol) {
                    region.lastCol--;
                } else {
                    it.remove();
                }
            }
        }
        // удалим колонку ...        
        final CellModel[] result = new CellModel[rows.size()];
        for (int i=rows.size()-1; i>=0; i--) {
            final List<CellModel> cells = rows.get(i).getCells();
            result[i] = colnum<cells.size() ? cells.remove(colnum) : null;
        }
        recalculateColumnsCount();
        return result;
    }

    /**
     * Удаляет целую строки из данной области. При этом пересчитываются координаты тех регионов которых затрагивает удаление данной области.
     * @param rownum индекс удаляемой строки.
     * @return  массив всех ячеек из заданной строки. Если в удаляемой строке в какой-то ячейке не было ничего указано то соответствующий элемент массива будет равен <code>null</code>.
     */
    public CellModel[] removeRow(final int rownum) {
        if (rownum<0 || rownum>=rows.size())
            throw new IndexOutOfBoundsException("Invalid range");
        // пересчитаем характеристики всех регионов области ...
        for (Iterator<Region> it=regions.iterator(); it.hasNext(); ) {
            final Region region = it.next();
            if (region.firstRow>rownum) {
                region.shiftRows(-1);
            } else
            if (region.lastRow>=rownum) {
                if (region.lastRow>region.firstRow) {
                    region.lastRow--;
                } else {
                    it.remove();
                }
            }
        }
        // удалим строку ...
        final RowModel r = rows.remove(rownum);
        recalculateColumnsCount();
        return r.getCells().toArray(new CellModel[columnsCount]);
    }

    /**
     * Добавляет новую колонку в область. При этом перерасчитываются координаты всех регионов которых затрагивает вставка новой колонки в данной области.
     * @param colnum  индекс колонки которая будет вставлена.
     * @param cells  массив ячеек которые будут добавлены в соответствующие строки области.
     */
    public void addColumn(final int colnum, final CellModel[] cells) {
        if (colnum<0 || colnum>columnsCount)
            throw new IllegalArgumentException("Invalid range");
        // добавим колонку ...
        for (int i=rows.size()-1; i>=0; i--) {
            final CellModel cell = i<cells.length ? cells[i] : null;   // содержимое ячейки которое надо добавить в шаблон.
            final List<CellModel> rcells = rows.get(i).getCells();     // разреженный список ячеек в текущей строке.
            if (cell!=null || colnum<rcells.size()) {
                while (colnum>rcells.size()) rcells.add(null);
                rows.get(i).getCells().add(colnum, cell);
            }
        }
        recalculateColumnsCount();
        // пересчитаем характеристики всех регионов области ...
        for (Region region  : regions) {
            if (region.firstCol>=colnum) {
                region.shiftColumns(1);
            } else
            if (region.lastCol>=colnum) {
                region.lastCol++;
            }
        }
    }

    /**
     * Добавляет новую строку в данную область. При этом пересчитываются координаты тех регионов которых затрагивает вставка новой строки в данной области.
     * @param rownum  индекс строки которая будет вставлена в область.
     * @param cells  массив ячеек которые будут вставлены в добавленную строку области.
     */
    public void addRow(final int rownum, final CellModel[] cells) {
        if (rownum<0 || rownum>rows.size())
            throw new IndexOutOfBoundsException("row number is out of bounds");
        // добавим строку ...
        final RowModel row = new RowModel();
        row.getCells().addAll( Arrays.asList(cells) );
        rows.add(rownum, row);
        recalculateColumnsCount();
        // пересчитаем характеристики всех регионов области ...
        for (Region region  : regions) {
            if (region.firstRow>=rownum) {
                region.shiftRows(1);
            } else
            if (region.lastRow>=rownum) {
                region.lastRow++;
            }
        }
    }

    /**
     * Создает копии регионов шаблона, смещенные по высоте на указанное количество строк.
     * Данные
     * @param offset  смещение по высоте (кол-во строк). Минимальное значение = 0.
     * @return  копии регионов со смещением по высоте. Не может быть <code>null</code>.
     */
    public List<CellRangeAddress> makePOIRegions(final int offset) {
        if (offset<0)
            throw new IllegalArgumentException("Invalid offset: "+offset);
        final List<CellRangeAddress> result = new ArrayList<CellRangeAddress>(regions.size());
        for (Region src : regions) {
            final CellRangeAddress dst = new CellRangeAddress(src.getFirstRow()+offset, src.getLastRow()+offset, src.getFirstCol(), src.getLastCol());
            result.add( dst );
        }
        return result;
    }

    /**
     * Определяет следует ли показывать эту область пользователю или она должна быть скрытой.
     * @return  Возвращает <code>true</code> если область должна быть скрытой от пользователя.
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Устанавливает признак видимости данной области пользователю.
     * @param hidden  <code>true</code> если область должна быть скрытой от пользователя.
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * Пересчитывает максимальное количество колонок в областе.
     */
    protected void recalculateColumnsCount() {
        int count = 0;
        for (RowModel row : rows) {
            int ci = row.getCells().size();
            while (ci>0 && row.getCells().get(ci-1)==null) {
                ci--;
            }
            count = Math.max(count, ci);
        }
        this.columnsCount =  count;
    }

    /**
     * Используется в целях отладки.
     * @return  описание структуры данной области.
     */
    public String dumpStructure() {
        final StringBuilder buf = new StringBuilder(2048);
        buf.append("=== template area:  rows=").append(rows.size()).append(", regions=").append(regions.size()).append(" ===\n");
        for (RowModel row : rows) {
            buf.append(row!=null ? row.toString() : "<empty>\n");
        }
        buf.append("=== end of template area ===\n");
        return buf.toString();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final Area result = (Area)super.clone();
        result.rows = new ArrayList<RowModel>(rows.size());
        for (RowModel rm : rows) {
            result.rows.add( (RowModel)rm.clone() );
        }
        result.regions = new ArrayList<Region>(regions.size());
        for (Region region : regions) {
            result.regions.add( new Region(result, region.firstCol, region.firstRow, region.lastCol, region.lastRow) );
        }
        return result;
    }

    @Override
    public String toString() {
        return "[Area{rows:"+ rows.size()+", regions:"+regions.size()+"}]";
    }



    /**
     * Определяет некоторое множество ячеек в области.
     * В отчетах используется как правило для объединения ячеек в формируемом отчете.
     * @author Anton Sharapov
     */
    public static class Region implements Serializable, Cloneable {

        private final Area owner;
        private int firstCol;
        private int firstRow;
        private int lastCol;
        private int lastRow;

        Region(final Area owner, final int firstCol, final int firstRow, final int lastCol, final int lastRow) {
            if (firstCol<0 || firstRow<0 || firstCol>lastCol || firstRow>lastRow || lastCol>=owner.getColumnsCount() || lastRow>=owner.getRows().size())
                throw new IndexOutOfBoundsException("Invalid range");
            this.owner = owner;
            this.firstCol = firstCol;
            this.firstRow = firstRow;
            this.lastCol = lastCol;
            this.lastRow = lastRow;
        }

        public int getFirstCol() {
            return firstCol;
        }
        public void setFirstCol(int col) {
            if (col<0 || col>lastCol)
                throw new IndexOutOfBoundsException("Invalid range");
            this.firstCol = col;
        }

        public int getFirstRow() {
            return firstRow;
        }
        public void setFirstRow(int row) {
            if (row<0 || row>lastRow)
                throw new IndexOutOfBoundsException("Invalid range");
            this.firstRow = row;
        }

        public int getLastCol() {
            return lastCol;
        }
        public void setLastCol(int col) {
            if (col<firstCol || col>=owner.getColumnsCount())
                throw new IndexOutOfBoundsException("Invalid range");
            this.lastCol = col;
        }

        public int getLastRow() {
            return lastRow;
        }
        public void setLastRow(int row) {
            if (row<firstRow || row>=owner.getRows().size())
                throw new IndexOutOfBoundsException("Invalid range");
            this.lastRow = row;
        }

        public int getCellsCount() {
            return (lastCol-firstCol+1) * (lastRow-firstRow+1);
        }

        /**
         * Сдвигает данный регион на <code>delta</code> колонок влево или вправо (в зависимости от знака аргумента).
         * @param delta  определяет величину и направление смещения региона.
         */
        public void shiftColumns(final int delta) {
            if (firstCol+delta<0 || lastCol+delta>=owner.getColumnsCount())
                throw new IndexOutOfBoundsException("Invalid range");
            firstCol += delta;
            lastCol += delta;
        }

        /**
         * Сдвигает данный регион на <code>delta</code> строк вверх или вниз (в зависимости от знака аргумента).
         * @param delta  определяет величину и направление смещения региона.
         */
        public void shiftRows(final int delta) {
            if (firstRow+delta<0 || lastRow+delta>=owner.getRows().size())
                throw new IndexOutOfBoundsException("Invalid range");
            firstRow += delta;
            lastRow += delta;
        }

        /**
         * Определяет входит ли указанная в аргументах ячейка в заданную область
         * @param col  номер колонки ячейки.
         * @param row  номер строки ячейки.
         * @return  <code>true</code> если
         */
        public boolean contains(final int col, final int row) {
            return firstCol<=col && col<=lastCol &&
                   firstRow<=row && row<=lastRow;
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
        @Override
        public int hashCode() {
            return firstCol << 8 + lastCol;
        }
        @Override
        public boolean equals(final Object obj) {
            if (obj==null || !getClass().equals(obj.getClass()))
                return false;
            final Region other = (Region)obj;
            return owner==other.owner &&
                   firstRow==other.firstRow && firstCol==other.firstCol &&
                   lastRow==other.lastRow && lastCol==other.lastCol;
        }
        @Override
        public String toString() {
            return "[Area.Region{(col,row): ("+firstCol+","+firstRow+")..("+lastCol+","+lastRow+")}]";
        }
    }
}
