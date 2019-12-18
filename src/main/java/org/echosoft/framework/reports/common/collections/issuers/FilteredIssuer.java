package org.echosoft.framework.reports.common.collections.issuers;

import java.util.NoSuchElementException;

import org.echosoft.framework.reports.common.collections.Predicate;
import org.echosoft.framework.reports.common.collections.Predicates;


/**
 * Итератор - прокси выполняющий фильтрацию содержимого исходного итератора.
 *
 * @author Anton Sharapov
 */
public class FilteredIssuer<T> implements ReadAheadIssuer<T> {

    private final Issuer<T> issuer;
    private final Predicate<T> predicate;
    private boolean nextCalculated;
    private boolean hasNext;
    private T next;

    /**
     * @param issuer      исходный итератор из которого надо отбирать только те элементы что соответствуют задаваемому предикату.
     * @param predicate предикат для отбора элементов из исходного итератора.
     */
    public FilteredIssuer(final Issuer<T> issuer, final Predicate<T> predicate) {
        this.issuer = issuer;
        this.predicate = predicate != null ? predicate : Predicates.<T>all();
        this.nextCalculated = false;
    }

    @Override
    public boolean hasNext() throws Exception {
        ensureNextCalculated();
        return hasNext;
    }

    @Override
    public T next() throws Exception {
        ensureNextCalculated();
        if (!hasNext)
            throw new NoSuchElementException();
        final T result = next;
        nextCalculated = false;
        next = null;
        return result;
    }

    @Override
    public T readAhead() throws Exception {
        ensureNextCalculated();
        if (!hasNext)
            throw new NoSuchElementException();
        return next;
    }

    @Override
    public void close() throws Exception {
        issuer.close();
    }

    protected void ensureNextCalculated() throws Exception {
        if (!nextCalculated) {
            nextCalculated = true;
            while (issuer.hasNext()) {
                final T object = issuer.next();
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
