package org.echosoft.framework.reports.model;

/**
 * Описывает простую многострочную секцию в отчете.
 * Как правило такие секции используются для описания заголовков и подвалов на страницах отчетов.
 *
 * @author Anton Sharapov
 */
public class PlainSection extends Section {

    /**
     * Список строк из шаблона excel, который соответствует данной секции отчета.
     */
    private AreaModel template;


    public PlainSection(final String id) {
        super(id);
    }

    /**
     * Возвращает область шаблона отчета которая описывает данную секцию.
     *
     * @return область шаблона отчета с описанием данной секции.
     */
    public AreaModel getTemplate() {
        return template;
    }

    /**
     * Позволяет указать шаблонное описание данной секции.
     *
     * @param area фрагмент шаблона отчета с описанием данной секции.
     */
    public void setTemplate(final AreaModel area) {
        this.template = area;
    }

    /**
     * @return Количество строк шаблона отведенных на описание данной секции.
     */
    public int getTemplateRowsCount() {
        return template.getRowsCount();
    }

    /**
     * Возвращает количество колонок отведенных на описание данной секции.
     *
     * @return количество колонок отведенных на описание секции.
     */
    public int getTemplateColumnsCount() {
        return template.getColumnsCount();
    }


    /**
     * Выполняет глубокое копирование данной секции.
     *
     * @param target ссылка на модель отчета в который будет импортирована создаваемая копия секции.
     * Необходим для выполнения данной операции во избежание избыточного клонирования тех структур отчета, которые могут
     * использоваться разными секциями одного и того же отчета одновременно. К таковым структурам можно отнести:
     * <li> поставщики данных
     * <li> обработчики ошибок
     * <li> таблицы стилей ячеек секций.
     * @return глубокую копию данной секции.
     * @throws CloneNotSupportedException в случае проблем с клонированием какого-нибудь элемента секции.
     */
    @Override
    public Section cloneSection(final Report target) throws CloneNotSupportedException {
        final PlainSection result = (PlainSection) super.cloneSection(target);
        if (template != null)
            result.template = (AreaModel) template.clone();
        return result;
    }

    @Override
    public String toString() {
        return "[PlainSection{id:" + getId() + ", height:" + (template != null ? template.getRowsCount() : 0) + "}]";
    }
}
