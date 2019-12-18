package org.echosoft.framework.reports.common.collections.issuers;

/**
 * Итератор по объектам обладающий следующими отличиями от стандартного {@link java.util.Iterator}:
 * <ul>
 * <li>По завершении работы с итератором требуется явное освобождение используемых им ресурсов путем вызова метода {@link #close()}</li>
 * <li>Все методы интерфейса могут поднимать любые исключительные ситуации</li>
 * <li>Отсутствует метод <code>remove</code></li>
 * </ul>
 *
 * @author Anton Sharapov
 */
public interface Issuer<T> extends AutoCloseable {

    /**
     * Выполняет проверку наличия следующего элемента в коллекции.
     *
     * @return <code>true</code> в случае наличия как минимум одного не прочитанного элемента коллекции;
     *         <code>false</code> в случае достижения конца коллекции.
     * @throws Exception в случае любых ошибок.
     */
    public boolean hasNext() throws Exception;

    /**
     * Возвращает следующий элемент коллекции
     *
     * @return очередной элемент коллекции.
     * @throws java.util.NoSuchElementException
     *                   в случае попытки прочитать следующий элемент коллекции когда уже был достигнут ее конец.
     * @throws Exception в случае любых других ошибок.
     */
    public T next() throws Exception;

    /**
     * Вызывается по окончании работы с данным итератором для корректного освобождения всех занятых им ресурсов.
     *
     * @throws Exception в случае любых ошибок.
     */
    @Override
    public void close() throws Exception;
}
