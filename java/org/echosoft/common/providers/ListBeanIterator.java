package org.echosoft.common.providers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Anton Sharapov
 */
public final class ListBeanIterator<T> implements BeanIterator<T> {

    private final List<T> beans;
    private final int size;
    private int cursor;

    public ListBeanIterator(final List<T> beans) {
        this.beans = beans != null ? beans : Collections.<T>emptyList();
        this.size = this.beans.size();
    }

    public ListBeanIterator(final T[] beans) {
        this.beans = beans != null ? Arrays.asList(beans) : Collections.<T>emptyList();
        this.size = this.beans.size();
    }

    @Override
    public boolean hasNext() {
        return cursor < size;
    }

    @Override
    public T next() {
        if (cursor>=size)
            throw new NoSuchElementException();
        return beans.get(cursor++);
    }

    @Override
    public T readAhead() {
        if (cursor>=size)
            throw new NoSuchElementException();
        return beans.get(cursor);
    }

    @Override
    public void close() {
    }

}
