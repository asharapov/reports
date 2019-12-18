package org.echosoft.framework.reports.common.collections.issuers;

import java.util.NoSuchElementException;

/**
 * Простая реализация интерфейса {@link ReadAheadIssuer}, адаптирующая уже существующие объекты типа {@link Issuer}.
 *
 * @author Anton Sharapov
 */
public class SimpleReadAheadIssuer<T> implements ReadAheadIssuer<T> {

    private final Issuer<T> issuer;
    private boolean nextCalculated;
    private boolean hasNext;
    private T next;

    public SimpleReadAheadIssuer(final Issuer<T> issuer) {
        this.issuer = issuer;
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
            hasNext = issuer.hasNext();
            next = hasNext ? issuer.next() : null;
            nextCalculated = true;
        }
    }
}
