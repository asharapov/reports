package org.echosoft.framework.reports.model.providers;

import java.io.Serializable;

import org.echosoft.common.query.Query;
import org.echosoft.common.query.providers.DataProvider;
import org.echosoft.framework.reports.model.el.ELContext;

/**
 * <p>Предназначен для динамического конструирования поставщиков данных на основе контекста выполнения отчета.</p>
 * <p>Как правило, информация о поддерживаемых приложением отчетах собирается один раз при старте приложения.
 * Вместе с тем при каждом запуске отчета, источники данных и их аргументы вызова  могут быть совершенно различными.
 * Чтобы не перечитывать всю конфигурацию отчета каждый раз перед его построением был введен данный интерфейс.
 * Он отвечает за конструирование по требованию конкретных экземпляров поставщиков данных основываясь на текущем
 * контексте выполнения отчета {@link ELContext} в котором может находиться любая информация требуемая для построения
 * отчета (см. пространство имен <code>environment</code>).
 * 
 * @author Anton Sharapov
 */
public interface DataProviderHolder extends Serializable, Cloneable {

    /**
     * Возвращает уникальный идентификатор поставщика данных (в рамках одного отчета).
     *
     * @return  идентификатор поставщика данных.
     */
    public String getId();

    /**
     * Возвращает источник данных полученный на основании контекста выполнения отчета.
     *
     * @param ctx  контекст выполнения отчета.
     * @return  источник данных используемый при построении отчета.
     */
    public DataProvider getProvider(ELContext ctx);

    /**
     * Возвращает структуру, содержашую доп. аргументы для обращения к источнику данных.
     * 
     * @param ctx  контекст выполнения отчета.
     * @return  доп. информация, на основании которой источник данных выдаст требуемые для отчета данные.
     */
    public Query getQuery(ELContext ctx);

    public Object clone() throws CloneNotSupportedException;
}
