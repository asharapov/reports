package org.echosoft.framework.reports.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.echosoft.common.model.TreeNode;
import org.echosoft.common.utils.StringUtil;
import org.echosoft.framework.reports.model.el.Expression;

/**
 * <p>Описывает один из листов отчета.</p>
 *
 * @author Anton Sharapov
 */
public class SheetModel implements Serializable {

    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];

    /**
     * Внутренний идентификатор листа отчета.
     */
    private final String id;

    /**
     * Название данного листа.
     */
    private Expression title;

    /**
     * Определяет должен ли данный лист быть спрятан от пользователя или нет.
     */
    private boolean hidden;

    /**
     * Определяет должна ли данный лист быть отображен в итоговом отчете. Лист у которого значение
     * данного свойства = <code>false</code> будет просто проигнорирована при построении отчета.
     * Значение по умолчанию - <code>true</code>.
     */
    private boolean rendered;

    /**
     * Определяет заблокирован ли данный лист отчета для внесения исправлений или нет.
     * Чтобы изменить данные расположенные на этом листе, пользователь должен будет ввести определенный логин и пароль
     * который должен быть "прошит" в заголовке отчета.
     */
    private boolean locked;

    /**
     * Определяет ширину колонок на листе начиная с самой первой колонки (индекс = 0).
     * Таким образом colwidth[0] = ширина первой колонки, colwidth[1] = ширина второй колонки и т. д.
     * Строго говоря на листе может быть много больше колонок чем указано в данном массиве. В этом случае
     * для всех неуказанных здесь колонок будет использоваться ширина колонки по умолчанию.
     */
    private int[] colwidths;

    /**
     * Определяет какие колонки на листе должны быть скрыты от просмотра начиная с самой первой колонки (индекс=0).
     * Таким образом colhidden[0] - определяет должна ли отображаться первая колонка, colhidden[1] - вторая колонка и т.д.
     * Строго говоря на листе может быть много больше колонок чем указано в данном массиве. В этом случае
     * для все прочие колонки будут отображаться по умолчанию.
     */
    private boolean[] colhidden;

    /**
     * Описывает иерархию группировок колонок.
     */
    private final TreeNode<String, ColumnGroupModel> colgroups;

    /**
     * Группа свойств, относящихся к описанию представления страницы.
     */
    private PageSettingsModel pageSettings;

    /**
     * Упорядоченный список разделов, присутствующих на данном листе отчета.
     */
    private final List<Section> sections;


    public SheetModel(String id) {
        id = StringUtil.trim(id);
        if (id == null)
            throw new IllegalArgumentException("Report sheet identifier must be specified");
        this.id = id;
        this.hidden = false;
        this.rendered = true;
        this.locked = false;
        this.sections = new ArrayList<Section>();
        this.colwidths = EMPTY_INT_ARRAY;
        this.colhidden = EMPTY_BOOLEAN_ARRAY;
        this.colgroups = new TreeNode<String, ColumnGroupModel>("", null);
        this.pageSettings = new PageSettingsModel();
    }

    /**
     * @return идентификатор шаблона листа отчета.
     */
    public String getId() {
        return id;
    }


    /**
     * @return Название листа.
     */
    public Expression getTitle() {
        return title;
    }

    /**
     * Устанавливает название листа.
     * <p><strong>Внимание!</strong> Все листы отчета должны иметь уникальные названия. Хотя имя название листа может быть произволной длины
     * но в проверке на уникальность используются только первые <b>30</b> символов из названия.</p>
     * <p>Имя листа не должно содержать следующие символы:<br/><center><code>'/', '\\', '?', '*', '[', ']'</code></center></p>
     *
     * @param title новое название листа.
     */
    public void setTitle(final Expression title) {
        if (title == null)
            throw new IllegalArgumentException("Sheet title can't be an empty");
        this.title = title;
    }


    /**
     * Определяет следует ли показывать этот лист пользователю или он должна быть от него скрыт.
     *
     * @return Возвращает <code>true</code> если лист должна быть скрыт от пользователя.
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Устанавливает признак видимости данного листа.
     *
     * @param hidden <code>true</code> если лист должна быть скрыт от пользователя.
     */
    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }


    /**
     * Определяет должен ли данный лист быть отображен в итоговом отчете. Лист у которого значение
     * данного свойства = <code>false</code> будет просто проигнорирована при построении отчета.
     * Значение по умолчанию - <code>true</code>.
     *
     * @return <code>true</code> если данный лист должен быть отображен в итоговом отчете, в противном случае он должен быть пропущен.
     */
    public boolean isRendered() {
        return rendered;
    }

    /**
     * Определяет должен ли данный лист быть отображен в итоговом отчете. Как правило, данное свойство изменяется обработчиками событий.
     *
     * @param rendered <code>true</code> если лист должен быть отображен в итоговом отчете.
     */
    public void setRendered(final boolean rendered) {
        this.rendered = rendered;
    }


    /**
     * Определяет доступен ли данный лист для внесения изменений пользователем или нет.
     *
     * @return <code>true</code> если данный лист защищен от внесения изменений.
     */
    public boolean isProtected() {
        return locked;
    }

    /**
     * Устанавливает признак доступности данного листа для внесения изменений пользователем.
     *
     * @param locked <code>true</code> если данный лист защищен от внесения изменений.
     */
    public void setProtected(final boolean locked) {
        this.locked = locked;
    }


    /**
     * Возвращает количество колонок используемых в отчете.
     *
     * @return количество колонок задействованных в отчете.
     */
    public int getColumnsCount() {
        int result = 0;
        for (Section section : sections) {
            result = Math.max(result, section.getTemplateColumnsCount());
        }
        return result;
    }

    /**
     * Возвращает информацию о ширине колонок на листе начиная с самой первой колонки (индекс = 0).
     * Таким образом colwidth[0] = ширина первой колонки, colwidth[1] = ширина второй колонки и т. д.
     * Строго говоря на листе может быть много больше колонок чем указано в данном массиве. В этом случае
     * для всех неуказанных здесь колонок будет использоваться ширина колонки по умолчанию.
     *
     * @return Возвращает ширину всех колонок отчета. Никогда не может быть null.
     */
    public int[] getColumnWidths() {
        return colwidths;
    }

    /**
     * Позволяет установить ширину колонок на листе.
     *
     * @param colwidths информация о ширине колонок на листе начиная с самой первой колонки. Если значение какого-либо
     *                  элемента меньше нуля то будет использоваться значение по умолчанию.
     */
    public void setColumnWidths(final int[] colwidths) {
        this.colwidths = colwidths != null ? colwidths : EMPTY_INT_ARRAY;
    }

    /**
     * Добавляет информацию о новой колонке на данный лист отчета.
     *
     * @param column порядковый индекс добавляемой колонки.
     * @param width  ширина добавляемой колонки.
     * @param hidden признак видимости добавляемой колонки.
     *               TODO: добавить механизм пересчета группировок колонок ...
     */
    public void insertColumn(final int column, final int width, final boolean hidden) {
        final int length = Math.max(column + 1, colwidths.length + 1);
        final int[] ncolwidths = new int[length];
        final boolean[] ncolhidden = new boolean[length];
        if (column < colwidths.length) {
            System.arraycopy(colwidths, 0, ncolwidths, 0, column);
            System.arraycopy(colwidths, column, ncolwidths, column + 1, colwidths.length - column);
            ncolwidths[column] = width;
            System.arraycopy(colhidden, 0, ncolhidden, 0, column);
            System.arraycopy(colhidden, column, ncolhidden, column + 1, colhidden.length - column);
            ncolhidden[column] = hidden;
        } else {
            System.arraycopy(colwidths, 0, ncolwidths, 0, colwidths.length);
            Arrays.fill(ncolwidths, colwidths.length, ncolwidths.length, width);
            System.arraycopy(colhidden, 0, ncolhidden, 0, colhidden.length);
            Arrays.fill(ncolhidden, colhidden.length, ncolhidden.length, hidden);
        }
        this.colwidths = ncolwidths;
        this.colhidden = ncolhidden;
    }

    /**
     * Определяет какие колонки на листе должны быть скрыты от просмотра начиная с самой первой колонки (индекс=0).
     * Таким образом colhidden[0] - определяет должна ли отображаться первая колонка, colhidden[1] - вторая колонка и т.д.
     * Строго говоря на листе может быть много больше колонок чем указано в данном массиве. В этом случае
     * для все прочие колонки будут отображаться по умолчанию.
     *
     * @return возвращает информацию о видимости каждой колонки отчета. Никогда не может быть null.
     */
    public boolean[] getColumnHidden() {
        return colhidden;
    }

    /**
     * Позволяет установить признак невидимости для ряда колонок отчета.
     *
     * @param colhidden информация о видимости каждой колонки отчета.
     */
    public void setColumnHidden(final boolean[] colhidden) {
        this.colhidden = colhidden != null ? colhidden : EMPTY_BOOLEAN_ARRAY;
    }

    /**
     * Описывает иерархию группировок колонок.
     *
     * @return корневой (фиктивный) узел дерева группировок. Метод никогда не возвращает null.
     */
    public TreeNode<String, ColumnGroupModel> getColumnGroups() {
        return colgroups;
    }

    /**
     * Добавляет информацию о группировке колонок на листе.
     *
     * @param group очередная группа колонок.
     */
    public void addColumnGroup(final ColumnGroupModel group) {
        addColumnGroup(colgroups, group);
    }

    protected TreeNode<String, ColumnGroupModel> addColumnGroup(final TreeNode<String, ColumnGroupModel> parent, final ColumnGroupModel group) {
        for (TreeNode<String, ColumnGroupModel> node : parent.getChildren()) {
            if (group.insideOf(node.getData()))
                return addColumnGroup(node, group);
            if (group.intersected(node.getData()))
                throw new RuntimeException("Column group intersected with earlier registered groups");
        }
        final String id = "[" + group.getFirstColumn() + "-" + group.getLastColumn() + "]";
        return parent.addChildNode(id, group);
    }

    /**
     * Настройки отображения страницы.
     *
     * @return группа свойств описывающая настройки отображения страницы. Никогда не возвращает <code>null</code>.
     */
    public PageSettingsModel getPageSettings() {
        return pageSettings;
    }


    /**
     * Осуществляет поиск секции на листе по ее идентификатору.
     *
     * @param sectionId идентификатор секции.
     * @return информация об указанной секции или null если такая секция отсутствует на листе.
     */
    public Section findSectionById(final String sectionId) {
        for (Section section : sections) {
            if (section.getId().equals(sectionId))
                return section;
            if (section instanceof CompositeSection) {
                final Section s = ((CompositeSection) section).findSectionById(sectionId);
                if (s != null)
                    return s;
            }
        }
        return null;
    }

    /**
     * Возвращает список всех секций строго в той последовательсности в которой они должны быть
     * представлены в итоговом отчете.
     *
     * @return список всех секций на листе.
     */
    public List<Section> getSections() {
        return sections;
    }


    /**
     * Выполняет глубокое копирование данного листа отчета.
     *
     * @param target ссылка на модель отчета в который будет импортирована создаваемая копия листа отчета.
     *               Необходим для выполнения данной операции во избежание избыточного клонирования тех структур отчета, которые могут
     *               использоваться на разных листах одного и того же отчета одновременно. К таковым структурам можно отнести:
     *               <li> поставщики данных
     *               <li> обработчики ошибок
     *               <li> таблицы стилей ячеек секций.
     * @return глубокую копию данного листа отчета.
     * @throws CloneNotSupportedException в случае проблем с клонированием какого-нибудь элемента листа.
     */
    public SheetModel cloneSheet(final Report target) throws CloneNotSupportedException {
        if (target == null)
            throw new IllegalArgumentException("Target report model must be specified");
        final SheetModel result = new SheetModel(id);
        result.title = title;
        result.hidden = hidden;
        result.locked = locked;
        result.rendered = rendered;
        result.colwidths = new int[colwidths.length];
        System.arraycopy(colwidths, 0, result.colwidths, 0, colwidths.length);
        result.colhidden = new boolean[colhidden.length];
        System.arraycopy(colhidden, 0, result.colhidden, 0, colhidden.length);

        for (TreeNode<String, ColumnGroupModel> node : colgroups.traverseNodes(false)) {
            final TreeNode<String, ColumnGroupModel> parent = result.colgroups.findNodeById(node.getParent().getId(), true);
            parent.addChildNode(node.getId(), node.getData());
        }

        result.pageSettings = (PageSettingsModel) pageSettings.clone();

        for (Section section : sections) {
            result.sections.add(section.cloneSection(target));
        }
        return result;
    }

    @Override
    public String toString() {
        return "[Sheet{id:" + id + ", title:" + title + ", hidden:" + hidden + ", sections:" + sections.size() + "}]";
    }
}
