package org.echosoft.framework.reports.common.collections;

/**
 * Выполняет некоторые проверки возвращающие <code>true</code> или <code>false</code>
 * над переданным в аргументе вызова объектом.<br/>
 * Как правило, данный интерфейс используется для организации сложных запросов и фильтрации содержимого разного рода коллекций.
 *
 * @author Anton Sharapov
 */
public interface Predicate<T> {

    /**
     * Возвращает <code>true</code> если переданный в аргументе объект удовлетворяет условиям данного предиката.
     *
     * @param input входящий объект
     * @return <code>true</code> если переданный в аргументе объект удовлетворяет условиям данного предиката.
     *         В противном случае метод возвращает <code>false</code>.
     */
    public boolean accept(final T input);
}
