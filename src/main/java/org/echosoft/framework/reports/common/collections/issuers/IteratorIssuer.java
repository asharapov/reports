package org.echosoft.framework.reports.common.collections.issuers;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Адаптирует класс реализующий интерфейс {@link Iterable} или {@link Iterator} до класса реализующего интерфейс {@link ReadAheadIssuer}.
 *
 * @author Anton Sharapov
 */
public class IteratorIssuer<T> implements ReadAheadIssuer<T> {

    private final Iterator<T> iter;
    private boolean nextCalculated;
    private boolean hasNext;
    private T next;

    public IteratorIssuer(final Iterable<T> iterable) {
        this(iterable.iterator());
    }

    public IteratorIssuer(final Iterator<T> iterator) {
        this.iter = iterator;
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
