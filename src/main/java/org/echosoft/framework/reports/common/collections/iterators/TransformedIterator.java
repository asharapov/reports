package org.echosoft.framework.reports.common.collections.iterators;

import java.util.Iterator;

import org.echosoft.framework.reports.common.collections.Transformer;
import org.echosoft.framework.reports.common.collections.Transformers;

/**
 * Итератор по некоторому множеству данных с их одновременной трансформацией.
 *
 * @author Anton Sharapov
 */
public class TransformedIterator<S, D> implements Iterator<D> {

    private final Iterator<S> source;
    private final Transformer<S, D> transformer;

    /**
     * @param source итератор по исходеному множеству данных.
     * @param transformer задает механизм трансформации. Если <code>null</code> то трансформации по сути не происходит.
     */
    public TransformedIterator(final Iterator<S> source, final Transformer<S, D> transformer) {
        this.source = source;
        this.transformer = transformer != null ? transformer : Transformers.<S, D>nothing();
    }

    @Override
    public boolean hasNext() {
        return source.hasNext();
    }

    @Override
    public D next() {
        return transformer.transform(source.next());
    }

    @Override
    public void remove() {
        source.remove();
    }
}
