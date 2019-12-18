package org.echosoft.framework.reports.common.collections.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Простая реализация интерфейса {@link ReadAheadIterator}.<br/>
 *
 * @author Anton Sharapov
 */
public class SimpleReadAheadIterator<T> implements ReadAheadIterator<T> {

    private final Iterator<T> iter;
    private boolean nextCalculated;
    private boolean hasNext;
    private T next;

    public SimpleReadAheadIterator(final Iterator<T> iter) {
        this.iter = iter;
        this.nextCalculated = false;
    }

    @Override
    public boolean hasNext() {
        ensureNextCalculated();
        return hasNext;
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

    /**
     * Всегда поднимает исключение {@link UnsupportedOperationException}.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T readAhead() {
        ensureNextCalculated();
        if (!hasNext)
            throw new NoSuchElementException();
        return next;
    }

    protected void ensureNextCalculated() {
        if (!nextCalculated) {
            hasNext = iter.hasNext();
            next = hasNext ? iter.next() : null;
            nextCalculated = true;
        }
    }
}
