package org.echosoft.framework.reports.model;

import java.util.ArrayList;
import java.util.List;

import org.echosoft.framework.reports.util.POIUtils;

/**
 * Секция в отчете, ориентированная на отображение данных с использованием возможных группировок по одному или более полям.
 * Главный критерий при использовании группировок в отчетах - данные для отчета должны быть уже отсортированы по
 * данным полям в соответствующей последовательности.
 *
 * @author Anton Sharapov
 */
public class GroupingSection extends Section {

    private static final int[] EMPTY_INT_ARRAY = new int[0];

    /**
     * Вся информация о группировках данных в секции.
     */
    private List<GroupModel> groups;

    /**
     * Перечень порядковых номеров колонок (начиная с 0) в ячейках которых необходимо делать отступы
     * согласно с текущим уровнем группировки.
     * <p>PS: пока не используется</p>
     */
    private int[] indentedColumns;

    /**
     * Шаблон представления каждой записи в секции. Может соответствовать одной или более строкам в итоговом отчете.
     */
    private AreaModel rowTemplate;


    public GroupingSection(String id) {
        super(id);
        groups = new ArrayList<GroupModel>(5);
        indentedColumns = EMPTY_INT_ARRAY;
    }

    /**
     * Возвращает информацию о том по каким полям и в какой последовательсности следует группировать строки в секции.
     *
     * @return  список объектов {@link GroupModel}.
     */
    public List<GroupModel> getGroups() {
        return groups;
    }

    /**
     * возвращает перечень порядковых номеров колонок (начиная с 0) в ячейках которых необходимо делать отступы
     * согласно текущему уровню группировки.
     *
     * @return перечень номеров колонок в ячейках которых надо делать дополнительные отступы.
     */
    public int[] getIndentedColumns() {
        return indentedColumns;
    }

    /**
     * Устанавливает перечень колонок в ячейках которых необходимо делать отступы согласно текущему уровню группировки.
     *
     * @param indentedColumns  перечень порядковых номеров колонок (начиная с 0).
     */
    public void setIndentedColumns(int[] indentedColumns) {
        this.indentedColumns = indentedColumns!=null ? indentedColumns : EMPTY_INT_ARRAY;
    }

    /**
     * Устанавливает перечень колонок в ячейках которых необходимо делать отступы согласно текущему уровню группировки.
     *
     * @param indentedColumns  перечень названий колонок в формате в том формате в котором они представлены в UI (A, B, C, .. Z, AA, AB, ..)
     */
    public void setIndentedColumns(String[] indentedColumns) {
        if (indentedColumns==null) {
            this.indentedColumns = EMPTY_INT_ARRAY;
        } else {
            this.indentedColumns = new int[indentedColumns.length];
            for (int i=0; i<indentedColumns.length; i++) {
                this.indentedColumns[i] = POIUtils.getColumnNumber(indentedColumns[i]);
            }
        }
    }


    /**
     * Возвращает шаблон представления одной записи из источника данных в секции. Может соответствовать
     * одной или более строкам в итоговом отчете.
     *
     * @return  шаблон представления одной записи.
     */
    public AreaModel getRowTemplate() {
        return rowTemplate;
    }

    public void setRowTemplate(AreaModel row) {
        this.rowTemplate = row;
    }


    /**
     * @return Количество строк шаблона отведенных на описание данной секции.
     */
    public int getTemplateRowsCount() {
        int result = rowTemplate.getRowsCount();
        for (GroupModel group : groups) {
            result += group.getStylesCount() * group.getRowsCount();
        }
        return result;
    }

    /**
     * Возвращает количество колонок отведенных на описание данной секции.
     * 
     * @return  количество колонок отведенных на описание секции.
     */
    public int getTemplateColumnsCount() {
        int result = rowTemplate.getColumnsCount();
        for (GroupModel group : groups) {
            result = Math.max(result, group.getColumnsCount());
        }
        return result;
    }


    /**
     * Выполняет глубокое копирование данной секции.
     *
     * @param target  ссылка на модель отчета в который будет импортирована создаваемая копия секции.
     * Необходим для выполнения данной операции во избежание избыточного клонирования тех структур отчета, которые могут
     * использоваться разными секциями одного и того же отчета одновременно. К таковым структурам можно отнести:
     * <li> поставщики данных
     * <li> обработчики ошибок
     * <li> таблицы стилей ячеек секций.
     * @return  глубокую копию данной секции.
     * @throws CloneNotSupportedException  в случае проблем с клонированием какого-нибудь элемента секции.
     */
    public Section cloneSection(final Report target) throws CloneNotSupportedException {
        final GroupingSection result = (GroupingSection)super.cloneSection(target);
        result.groups = new ArrayList<GroupModel>();
        for (GroupModel gm : groups) {
            result.groups.add( (GroupModel)gm.clone() );
        }
        result.indentedColumns = new int[indentedColumns.length];
        System.arraycopy(indentedColumns, 0, result.indentedColumns, 0, indentedColumns.length);
        if (rowTemplate!=null)
            result.rowTemplate = (AreaModel)rowTemplate.clone();
        return result;
    }

    public String toString() {
        return "[GroupingSection{id:"+getId()+", groups:"+groups.size()+"}]";
    }

}
