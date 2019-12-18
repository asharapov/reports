package org.echosoft.framework.reports.common.collections.iterators;

import java.util.NoSuchElementException;

/**
 * Итератор с поддержкой упреждающего чтения поверх массива объектов.
 * Данная реализация не поддерживает удаление элементов итераторов, соответственно метод {@link #remove()} всегда поднимает исключение.
 *
 * @author Anton Sharapov
 */
public class ObjectArrayIterator<T> implements ReadAheadIterator<T> {

    private final T[] array;
    private final int length;
    private int index;

    /**
     * Создает новый экземпляр {@link ObjectArrayIterator} для итерирования по всем элементам массива переданного в аргументе метода.
     *
     * @param array массив объектов или примитивов для которых требуется создать итератор.
     * @throws NullPointerException если <code>array</code> равен <code>null</code>.
     */
    public ObjectArrayIterator(final T... array) {
        this.array = array;
        this.length = array.length;
        this.index = 0;
    }


    @Override
    public boolean hasNext() {
        return index < length;
    }

    @Override
    public T next() {
        if (index >= length)
            throw new NoSuchElementException();
        return array[index++];
    }

    /**
     * В данной реализации метод всегда бросает исключение {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException при каждом вызове метода
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove() method is not supported for an ObjectArrayIterator");
    }

    @Override
    public T readAhead() {
        if (index >= length)
            throw new NoSuchElementException();
        return array[index];
    }

}
