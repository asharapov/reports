package org.echosoft.common.providers;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.echosoft.common.data.Query;

/**
 * @author Anton Sharapov
 */
public class ListDataProvider<T> implements DataProvider {

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
    public BeanIterator<T> execute(final Query query) {
        return collection != null
                ? new ProxyBeanIterator<T>(collection)
                : new ProxyBeanIterator<T>(iterator);
    }
}
