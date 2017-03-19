package org.echosoft.framework.reports.model.providers;

/**
 * @author Anton Sharapov
 */
public interface ComparablePredicate<T> {

    /**
     * Возвращает <code>true</code> если объект переданный во втором аргументе надлежащим образом соответствует объекту переданному в первом аргументе.<br/>
     * Сами правила соответствия задаются реализацией данного интерфейсного метода.
     *
     * @param parent  эталонный объект, полученный из родительской секции в которой был определен исходный поставщик данных.
     * @param current текущий объект.
     * @return <code>true</code> второй аргумент соответствует первому согласно определенным правилам, заданным реализацией данного метода.
     * @throws Exception В случае каких-либо проблем.
     */
    public boolean evaluate(final T parent, final T current) throws Exception;
}
