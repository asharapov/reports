package org.echosoft.framework.reports.common.collections.iterators;

import java.util.NoSuchElementException;

import org.echosoft.framework.reports.common.collections.issuers.Issuer;

/**
 * Адаптирует класс реализующий интерфейс {@link Issuer} до класса реализующего интерфейс {@link ReadAheadIterator}.
 *
 * @author Anton Sharapov
 */
public class IssuerIterator<T> implements ReadAheadIterator<T> {

    private final Issuer<T> issuer;
    private boolean nextCalculated;
    private boolean hasNext;
    private T next;

    public IssuerIterator(final Issuer<T> issuer) {
        this.issuer = issuer;
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
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private void ensureNextCalculated() {
        if (!nextCalculated) {
            try {
                hasNext = issuer.hasNext();
                next = hasNext ? issuer.next() : null;
                nextCalculated = true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
