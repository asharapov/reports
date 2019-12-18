package org.echosoft.framework.reports.common.collections.iterators;

import java.util.Iterator;

/**
 * Добавляет к итератору возможность упреждающего чтения следующего элемента без изменения текущего состояния объекта.
 *
 * @author Anton Sharapov
 */
public interface ReadAheadIterator<T> extends Iterator<T> {

    /**
     * Возвращает значение которое будет возвращено при следующем вызове метода {@link #next()}.
     * В отличие от метода {@link #next()} вызовы данного метода не меняет текущего состояния объекта.
     *
     * @return следующее значение итератора.
     * @throws java.util.NoSuchElementException
     *          в случае достижения конца потока данных.
     */
    public T readAhead();
}
