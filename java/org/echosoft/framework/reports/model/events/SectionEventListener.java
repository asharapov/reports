package org.echosoft.framework.reports.model.events;

import java.io.Serializable;
import java.util.EventListener;

import org.echosoft.framework.reports.processor.ExecutionContext;

/**
 * Обработчик событий связанный с обработкой отдельно взятой секции.
 * @author Anton Sharapov
 */
public interface SectionEventListener extends EventListener, Serializable {

    /**
     * Вызвается перед началом обработки первой строки секции.
     *
     * @param ectx  описывает текущее состояние обработки отчета.
     * @throws Exception  в случае каких-либо проблем.
     */
    public void beforeSection(ExecutionContext ectx) throws Exception;


    /**
     * Вызывается по окончании создания в секции строк связанных с очередной записью полученной от поставщика данных.
     *
     * @param ectx  описывает текущее состояние обработки отчета.
     * @throws Exception  в случае каких-либо проблем.
     */
    public void afterRecord(ExecutionContext ectx) throws Exception;

    /**
     * Вызвается по окончании обработки последнее строки секции.
     *
     * @param ectx  описывает текущее состояние обработки отчета.
     * @throws Exception  в случае каких-либо проблем.
     */
    public void afterSection(ExecutionContext ectx) throws Exception;

}
