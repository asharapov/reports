package org.echosoft.framework.reports.common.collections.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.echosoft.framework.reports.common.collections.Predicate;
import org.echosoft.framework.reports.common.collections.Predicates;


/**
 * Итератор - прокси выполняющий фильтрацию содержимого исходного итератора.
 *
 * @author Anton Sharapov
 */
public class FilteredIterator<T> implements ReadAheadIterator<T> {

    private final Iterator<T> iter;
    private final Predicate<T> predicate;
    private boolean nextCalculated;
    private boolean hasNext;
    private T next;

    /**
     * @param iter      исходный итератор из которого надо отбирать только те элементы что соответствуют задаваемому предикату.
     * @param predicate предикат для отбора элементов из исходного итератора.
     */
    public FilteredIterator(final Iterator<T> iter, final Predicate<T> predicate) {
        this.iter = iter;
        this.predicate = predicate != null ? predicate : Predicates.<T>all();
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
            nextCalculated = true;
            while (iter.hasNext()) {
                final T object = iter.next();
                if (predicate.accept(object)) {
                    next = object;
                    hasNext = true;
                    return;
                }
            }
            hasNext = false;
            next = null;
        }
    }
}
