package org.echosoft.framework.reports.model.events;

import java.io.Serializable;
import java.util.EventListener;

/**
 * Обработчик события вызывающегося каждый раз перед установкой генератором отчетов значения в очередную ячейку секции.
 *
 * @see CellEvent
 * @author Anton Sharapov
 */
public interface CellEventListener extends EventListener, Serializable {

    /**
     * Вызывает обработчик события.
     *
     * @param event вся информация описывающая данное событие.
     * @throws Exception вызывается в случае каких-либо проблем. Генератором отчетов не предпринимается каких-либо мер по
     * обработке подобных исключений и поднятие исключения каким-либо обработчиком ведет к прекращению формирования 
     * данного отчета.
     */
    public void handle(CellEvent event) throws Exception;
}
