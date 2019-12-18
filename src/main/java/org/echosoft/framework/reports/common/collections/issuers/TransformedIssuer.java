package org.echosoft.framework.reports.common.collections.issuers;

import org.echosoft.framework.reports.common.collections.Transformer;
import org.echosoft.framework.reports.common.collections.Transformers;

/**
 * Итератор по некоторому множеству данных с их одновременной трансформацией.
 *
 * @author Anton Sharapov
 */
public class TransformedIssuer<S,D> implements Issuer<D> {

    private final Issuer<S> source;
    private final Transformer<S, D> transformer;

    /**
     * @param source итератор по исходеному множеству данных.
     * @param transformer задает механизм трансформации. Если <code>null</code> то трансформации по сути не происходит.
     */
    public TransformedIssuer(final Issuer<S> source, final Transformer<S, D> transformer) {
        this.source = source;
        this.transformer = transformer != null ? transformer : Transformers.<S, D>nothing();
    }

    @Override
    public boolean hasNext() throws Exception {
        return source.hasNext();
    }

    @Override
    public D next() throws Exception {
        return transformer.transform(source.next());
    }

    @Override
    public void close() throws Exception {
        source.close();
    }
}
