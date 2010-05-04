package org.echosoft.framework.reports.macros;

import java.io.Serializable;

import org.echosoft.framework.reports.processor.ExecutionContext;

/**
 * Функция определяемая пользователем.
 *
 * @author Anton Sharapov
 */
public interface Macros extends Serializable {

    /**
     * Вызывает данную функцию.
     *
     * @param ectx контекст выполнения задачи.
     * @param arg  текстовый аргумент переданный в данную функцию. Может быть <code>null</code>. Представляет собой произвольную строку, которую вызываемый макрос
     * может трактовать как хочет.
     */
    public void call(ExecutionContext ectx, String arg);

}
