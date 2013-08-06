package org.echosoft.common.providers;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.echosoft.common.collections.issuers.IteratorIssuer;
import org.echosoft.common.collections.issuers.ReadAheadIssuer;

/**
 * @author Anton Sharapov
 */
public class ListDataProvider<T, Q> implements DataProvider<T, Q> {

    private final Iterable<T> collection;
    private final Iterator<T> iterator;

    public ListDataProvider() {
        collection = Collections.emptyList();
        iterator = null;
    }

    public ListDataProvider(final T[] beans) {
        this.collection = Arrays.asList(beans);
        this.iterator = null;
    }

    public ListDataProvider(final Iterable<T> beans) {
        this.collection = beans;
        this.iterator = null;
    }

    public ListDataProvider(final Iterator<T> iterator) {
        this.collection = null;
        this.iterator = iterator;
    }

    @Override
    public ReadAheadIssuer<T> execute(final Q query) {
        return collection != null
                ? new IteratorIssuer<T>(collection)
                : new IteratorIssuer<T>(iterator);
    }
}
