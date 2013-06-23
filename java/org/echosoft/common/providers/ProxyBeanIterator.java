package org.echosoft.common.providers;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Anton Sharapov
 */
final class ProxyBeanIterator<T> implements BeanIterator<T> {

    private final Iterator<T> iter;
    private boolean nextCalculated;
    private boolean hasNext;
    private T next;

    public ProxyBeanIterator(final Iterable<T> iterable) {
        this(iterable.iterator());
    }

    public ProxyBeanIterator(final Iterator<T> iterator) {
        this.iter = iterator;
        this.nextCalculated = false;
    }

    @Override
    public boolean hasNext() {
        ensureNextCalculated();
        return iter.hasNext();
    }

    @Override
    public T next() {
        ensureNextCalculated();
        if (!hasNext)
            throw new NoSuchElementException();
        final T result = next;
        nextCalculated = false;
        next = null;
        return result;
    }

    @Override
    public T readAhead() {
        ensureNextCalculated();
        if (!hasNext)
            throw new NoSuchElementException();
        return next;
    }

    @Override
    public void close() {
    }

    private void ensureNextCalculated() {
        if (!nextCalculated) {
            hasNext = iter.hasNext();
            next = hasNext ? iter.next() : null;
            nextCalculated = true;
        }
    }
}
