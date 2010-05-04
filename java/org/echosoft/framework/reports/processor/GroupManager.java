package org.echosoft.framework.reports.processor;

import java.util.ArrayList;
import java.util.List;

import org.echosoft.framework.reports.model.GroupModel;

/**
 * Данный класс отвечает за то как будет группироваться содержимое секции {@link org.echosoft.framework.reports.model.GroupingSection}.
 * Будет ли включаться итоговая группировка, как реагировать на группировочные поля которые являются ссылками на объекты
 * в иерархическом (а не линейном как обычно) классификаторе, и многое другое.
 *
 * @author Anton Sharapov
 */
public abstract class GroupManager {

    private static final GroupModel[] EMPTY_GROUP_MODEL = new GroupModel[0];

    /**
     * Информация из шаблона отчета о том по каким полям следует группировать данные в рамках текущей секции.
     */
    protected final GroupModel[] models;

    /**
     * Отражает иерархию групп в секции между собой.
     */
    protected final ArrayList<Group> groups;

    /**
     * Следует ли использовать тотальную группировку (когда в группе верхнего уровня не указано свойство 'discriminatorField ').
     */
    protected final boolean useTotalGrouping;

    /**
     * Содержит номер первой строки обрабатываемой в настоящее время записи (выставляется в методе {@link #initRecord(ExecutionContext, Object)} ).
     */
    private Integer recordFirstRow;

    /**
     * Индикатор определяющий что в настоящий момент рендерится - группировочная строка или простая строка с данными.
     * Использование данного флага может быть полезным при написании обработчиков событий.
     */
    private boolean groupRendering;

    /**
     * @param groups  список критериев группировки
     */
    public GroupManager(final List<GroupModel> groups) {
        this.models = groups!=null ? groups.toArray(new GroupModel[groups.size()]) : EMPTY_GROUP_MODEL;
        this.groups = new ArrayList<Group>();

        if (models.length>0) {
            useTotalGrouping = models[0].getDiscriminatorField()==null;
            for (int i=1; i<models.length; i++) {
                if (models[i].getDiscriminatorField()==null)
                    throw new RuntimeException("All group models (except first) MUST have non empty 'discriminatorField' property!");
            }
        } else {
            useTotalGrouping = false;
        }
    }

    /**
     * Возвращает список всех обрабатываемых в настоящий момент групп.
     * Последней в списке идет текущая группа.
     * @return список обрабатываемых в настойщий момент групп. Никогда не возвращает <code>null</code>.
     */
    public List<Group> getProcessingGroups() {
        return groups;
    }

    /**
     * Возвращает текущую обрабатываемую группу.
     * @return текущая обрабатываемая группа или <code>null</code> если никаких групп на данный момент нет в обработке.
     */
    public Group getCurrentGroup() {
        final int size = groups.size();
        return size>0 ? groups.get(size-1) : null;
    }

    /**
     * Возвращает <code>true</code> если менеджер групп сейчас обрабатывает отрисовку группировочной строки.
     * @return <code>true</code> если менеджер групп сейчас обрабатывает отрисовку группировочной строки.
     */
    public boolean isGroupRendering() {
        return groupRendering;
    }

    /**
     * <p>Данный метод вызывается каждый раз перед отрисовкой в секции очередного бина из поставщика данных
     * (т.е. очередной строки в отчете).  Метод занимается обработкой группировок в секции.
     * Это включает в себя поддержание внутренней иерархии групп а также принимает решение о том когда настало время
     * отобразить в отчете ту или иную группу (в виде соотв. группировочной строки в итоговом отчете).
     * </p>
     *
     * Последовательность действий при отрисовке такой секции приблизительно такова:
     * <pre>
     *  final ExecutionContext ctx = ...
     *  final GroupManager gm = ...
     *  // итерируемся по всем объектам полученным от поставщика данных секции ...
     *  for (BeanIterator it = provider.execute(query); it.hasNext(); ) {
     *      final Object bean = it.next();      // получаем данные для следующей строки.
     *      gm.initRecord(ctx, bean);           // обрабатываем все связанное с группировкой данных.
     *      renderArea(ctx, bean);              // отрисовываем саму строку с данными.
     *      gm.finalizeRecord(ctx);             // завершаем обработку строки.
     *  }
     * </pre>
     *
     * @param ctx  контекст выполнения задачи.
     * @param bean  данные для очередной строки в секции.
     * @throws Exception  в случае каких-либо проблем.
     */
    public void initRecord(final ExecutionContext ctx, final Object bean) throws Exception {
        final int idx = checkValidGroups(bean);
        for (int i=groups.size()-1; i>=idx; i--) {
            finalizeGroup(ctx);
        }
        for (int i=idx; i<models.length; i++) {
            final boolean initialized = initGroup(ctx, models[i], bean);
            if (!initialized)
                break;
        }

        recordFirstRow = ctx.getNewRowNum();
        final Group group = getCurrentGroup();
        if (group!=null) {
            group.records.add(recordFirstRow);
        }
    }

    /**
     * Данный метод вызывается по окончании отрисовки очередной записи в секции.
     * @param ctx  контекст выполнения задачи.
     * @throws Exception  в случае каких-либо проблем.
     */
    public void finalizeRecord(final ExecutionContext ctx) throws Exception {
        final Group group = getCurrentGroup();
        if (group!=null) {
            final int height = ctx.getNewRowNum() - recordFirstRow;
            if (group.records.size()==1) {
                group.recordsHeight = height;
            } else
            if (group.recordsHeight!=null) {
                if (group.recordsHeight!=height)
                    group.recordsHeight = null;
            }
        }
    }

    /**
     * Формирует завершение обработки всех открытых групп. Вызывается после обработки <u>последнего</u> полученного
     * бина от поставщика данных секции.
     * 
     * @param ctx  контекст выполнения задачи.
     * @throws Exception  в случае каких-либо проблем.
     */
    public void finalizeAllGroups(final ExecutionContext ctx) throws Exception {
        for (int i=groups.size()-1; i>=0; i--) {
            finalizeGroup(ctx);
        }
    }

    /**
     * Определяем сколько групп из списка по прежнему актуальны (текущая запись входит в них).
     *
     * @param bean  данные для очередной строки в секции.
     * @return количество групп из списка текущих групп в которые входит указанный бин с данными.
     * Если метод возвращает число равное количеству групп то текущая запись входит в последнюю объявленную группировку
     * и для нее не надо создавать новую группу.
     * @throws Exception  в случае каких-либо проблем.
     */
    protected int checkValidGroups(final Object bean) throws Exception {
        final int size = groups.size();
        for (int i=0; i<size; i++) {
            final Group group = groups.get(i);
            if (!group.acceptBean(bean)) {
                return i;
            }
        }
        return size;
    }

    /**
     * Инициализирует новую группу на основе указанных параметров, создает строки в выходном отчете в которых будет 
     * выведена информация о группировке. Позднее, во время вызова метода {@link #finalizeGroup(ExecutionContext)}
     * будет осуществлено заполнение этих строк данными согласно шаблону.
     * Эти методы были разделены так как выполняются они в разное время. Создание строк - как только появилась надобность
     * в новой группировке, а отрисовка группировочной строки - когда были обработаны все записи входящие в эту группировку,
     * то есть после окончания обработки последнего элемента этой группы.
     *
     * @param ctx  контекст выполнения задачи.
     * @param model  информация о группе для которой выполняется создание строк.
     * @param bean  объект из источника данных секции которым начинается новая группа.
     * @return <code>true</code> в случае когда группа и соответствующая ей строка отчета были успешно созданы. В случае же когда в
     *         процессе работы над новой группой было принято решение о нежелательности создания данной группы (равно как и всех нижележащих)
     *         то метод возвращает <code>false</code>.
     * @throws Exception  в случае каких-либо проблем.
     */
    protected boolean initGroup(final ExecutionContext ctx, final GroupModel model, final Object bean) throws Exception {
        int row = ctx.wsheet.getPhysicalNumberOfRows()>0 ? ctx.wsheet.getLastRowNum()+1 : 0;
        final Group parent = getCurrentGroup();
        final Group group = parent==null
                ? new Group(model, bean, row, 0)
                : new Group(model, bean, row, parent.depth + (parent.incrementsDepth() ? 1 : 0));
        if (!group.isValid())
            return false;

        if (parent!=null)
            parent.children.add( group );
        groups.add( group );

        final int rowcnt = model.getRowsCount();
        for (int i=0; i<rowcnt; i++) {
            ctx.wsheet.createRow(row++);
        }
        return true;
    }

    /**
     * Завершает обработку текущей группировки в отчете. Данная операция включает в себя два этапа:
     * отрисовку группировочной строки в итоговом отчете и, в заключение, удаление информации об этой группе из списка
     * обрабатываемых.
     * Сама строка в отчете под запись об этой группе была создана ранее во время вызова метода
     * {@link #initGroup(ExecutionContext, GroupModel, Object)}.
     * Эти методы были разделены так как выполняются они в разное время. Создание строк - как только появилась надобность
     * в новой группировке, а отрисовка группировочной строки - когда были обработаны все записи входящие в эту группировку,
     * то есть после окончания обработки последнего элемента этой группы.
     *
     * @param ctx  контекст выполнения задачи.
     * @throws Exception  в случае каких-либо проблем.
     */
    protected void finalizeGroup(final ExecutionContext ctx) throws Exception {
        groupRendering = true;
        renderCurrentGroup(ctx);
        groupRendering = false;
        groups.remove(groups.size()-1);
    }

    /**
     * Выполняет отрисовку группировочной строки для текущей группы (см. метод {@link #getCurrentGroup()}).
     * Поскольку данное действие не входит в сферу компетенции данного класса - {@link GroupManager} лишь указывает
     * когда настало время отобразить ту или иную группу) то этот метод был объявлен как абстрактный.
     *
     * @param ctx  контекст выполнения задачи.
     * @throws Exception  в случае каких-либо проблем.
     */
    protected abstract void renderCurrentGroup(ExecutionContext ctx) throws Exception;

}
