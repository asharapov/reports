package org.echosoft.framework.reports.processor;

import java.util.ArrayList;
import java.util.List;

import org.echosoft.common.utils.BeanUtil;
import org.echosoft.framework.reports.model.GroupModel;

/**
 * <p>Данная структура содержит всю информацию, требуемую в процессе генерации группировочной строки
 * в секции {@link org.echosoft.framework.reports.model.GroupingSection}.
 * В отличие от структуры {@link GroupModel}, которая используется для описания модели отчета,
 * данный класс используется непосредственно в фазе генерации конкретного отчета на основе его шаблона (модели).</p>
 * <p>В сферу компетенции данного класса входит привязка к конкретным строкам листа отчета для конкретной группировочной
 * строки.</p>
 * В итоговом excel документе т.н. группировки позволяют наглядно разделять разные группы строк в документе.
 * При этом каждая такая группа строк предваряется отдельной строкой-заголовком (реально такая строка-заголовок может
 * занимать более одной строки на листе excel документа. Пример:
 * <table border="1" align="center">
 *  <tr><td colspan="3">группа&nbsp;1</td></tr>
 *  <tr><td>r1</td><td></td><td></td></tr>
 *  <tr><td>r2</td><td></td><td></td></tr>
 *  <tr><td colspan="3">группа&nbsp;2</td></tr>
 *  <tr><td>r3</td><td></td><td></td></tr>
 * </table>
 * Вышеприведенный пример иллюстрирует разбиение трех попавших в отчет строк на две группы в соответствии с некоторым критерием.
 * Часто используется группировка одновременно по двум и более критериям:
 * <table border="1" align="center">
 *  <tr><td colspan="3">группа&nbsp;1</td></tr>
 *  <tr><td colspan="3">&nbsp;подгруппа&nbsp;1.1</td></tr>
 *  <tr><td>r1</td><td></td><td></td></tr>
 *  <tr><td>r2</td><td></td><td></td></tr>
 *  <tr><td colspan="3">&nbsp;подгруппа&nbsp;1.2</td></tr>
 *  <tr><td>r3</td><td></td><td></td></tr>
 *  <tr><td>r4</td><td></td><td></td></tr>
 *  <tr><td colspan="3">группа&nbsp;2</td></tr>
 *  <tr><td colspan="3">&nbsp;подгруппа&nbsp;2.1</td></tr>
 *  <tr><td>r5</td><td></td><td></td></tr>
 * </table>
 * @see org.echosoft.framework.reports.model.GroupingSection
 * @see org.echosoft.framework.reports.model.GroupModel
 * @author Anton Sharapov
 */
public final class Group {

    /**
     * Часть модели отчета, содержащая описание данной группировки. Особый интерес для нас там представляет
     * описание шаблона для данной группировочной "строки". Не забываем следующие моменты:
     * <ul>
     * <li> Группировочная "строка" может описываться более чем одним шаблоном (стилем представления)</li>
     * <li>
     *  Группировочные "строки" могут реально занимать более одной строки на листе итогового excel документа,
     *  но все стили для данной группировки (если их задано более одного) должны использовать одинаковое количество строк в excel документе.
     * </li>
     * </ul>
     * Не может быть <code>null</code>.
     */
    public final GroupModel model;

    /**
     * Первая запись из источника данных, попавшая в данную группировку.
     * Ее свойства используются для заполнения ячеек в группировочной строке а также для
     * сравнения с аналогичными свойствами всех последующих бинов из источника данных до тех пор пока
     * не найдется такой бин, который не входит в данную группу.
     */
    public final Object bean;

    /**
     * Номер строки в генерируемом документе excel с которой начинается данная группировка.
     */
    public final int startRow;

    /**
     * Глубина вложенности данной группы.
     */
    public final int depth;

    /**
     * Используется в случае если под данной группой в отчете лежат не реальные записи а дочерние группы
     * (то есть когда группировка строк с данными осуществляется по более чем одному критерию).
     * Поле никогда не равно null.
     */
    public final List<Group> children;

    /**
     * Используется в случае если под данной группой лежат реальные записи а не дочерние группы.
     * В таком случае каждый элемент списка содержит номер первой строки занимаемой записью.
     * Поле никогда не равно null.
     */
    public final List<Integer> records;

    /**
     * Поле содержит количество строк в отчете которые занимает каждая запись в группе, при условии что все они занимают одинаковое количество строк.
     * Если же разные записи занимают разное количество строк то поле будет равно <code>null</code>.
     */
    public Integer recordsHeight;

    /**
     * Значение поля {@link GroupModel#discriminatorField}, общее для всех объектов из источника данных входящих в
     * данную группировку.
     */
    public final Object discriminator;

    /**
     * Значение поля {@link GroupModel#levelField}, общее для всех объектов из источника данных входящих в
     * данную группировку. Значение поля должно приводиться к типу {@link Number} без ошибок.
     */
    public final Integer level;


    public Group(GroupModel model, Object bean, int startRow, int depth) throws Exception {
        this.model = model;
        this.bean = bean;
        this.startRow = startRow;
        this.depth = depth;
        this.children = new ArrayList<Group>();
        this.records = new ArrayList<Integer>();
        this.discriminator = model.getDiscriminatorField()!=null
                                ? BeanUtil.getProperty(bean, model.getDiscriminatorField())
                                : null;
        final Object lev = model.getLevelField()!=null ? BeanUtil.getProperty(bean, model.getLevelField()) : null;
        this.level = lev!=null ? ((Number)lev).intValue() : null;
    }

    /**
     * Проверяет указанный объект с данными (полученный из источника данных секции) на вхождение в данную группу.
     *
     * @param bean очередной бин полученный из источника данных секции. Содержит данные для отрисовки соответствующей строки в отчете.
     * @return  <code>true</code> если указанный бин, полученный из источника данных секции входит в указанную группу.
     *  (предполагается что проверка на вхождение этого бина во все родительские группы была уже успешно выполнена).
     * @throws Exception в случае каких-либо проблем.
     */
    public boolean acceptBean(final Object bean) throws Exception {
        if (model.getDiscriminatorField()!=null) {
            final Object value = BeanUtil.getProperty(bean, model.getDiscriminatorField());
            return this.discriminator!=null ? this.discriminator.equals(value) : value==null;
        } else
            return true;  // эта группа - итоговая позиция, она включает в себя все объекты из источника данных.
    }

    /**
     * Влияет на вычисление поля {@link #depth} у дочерних групп. Если это свойство равно <code>false</code> то у дочерних
     * групп оно не увеличивается по сравнению с этим же свойством данной (родительской) группы.
     *
     * @return <code>true</code> если для всех дочерних групп значение поля {@link #depth} должно быть больше значения
     * этого поля на 1.
     */
    public boolean incrementsDepth() {
        return model.getDiscriminatorField()!=null;
    }

    /**
     * Возвращает <code>true</code> если данная группа удовлетворяет всем критериям указанным в ее модели.
     * На данный момент к таковым относится проверка значения дискриминатора группы.
     * @return <code>true</code> если данная группа удовлетворяет критериям указанным в ее модели.
     * @see GroupModel#isSkipEmptyGroups() 
     */
    public boolean isValid() {
        return discriminator!=null || model.getDiscriminatorField()==null || !model.isSkipEmptyGroups();
    }


    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(64);
        buf.append("[Group{model:");
        buf.append(model.getDiscriminatorField());
        buf.append(", discriminator:");
        buf.append(discriminator);
        buf.append(", row:");
        buf.append(startRow);
        buf.append(", children:");
        buf.append(children.size());
        buf.append("}]");
        return buf.toString();
    }
}
