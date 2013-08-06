package org.echosoft.framework.reports.model.providers;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.echosoft.common.collections.issuers.Issuer;
import org.echosoft.common.collections.issuers.IteratorIssuer;
import org.echosoft.common.collections.issuers.ReadAheadIssuer;
import org.echosoft.common.collections.issuers.SimpleReadAheadIssuer;
import org.echosoft.common.collections.iterators.ObjectArrayIterator;

/**
 * @author Anton Sharapov
 */
class Issuers {

    @SuppressWarnings("unchecked")
    public static <T> ReadAheadIssuer<T> asIssuer(final Object result) {
        if (result instanceof ReadAheadIssuer) {
            return (ReadAheadIssuer<T>) result;
        } else
        if (result instanceof Issuer) {
            return new SimpleReadAheadIssuer<T>((Issuer)result);
        } else
        if (result instanceof Iterator) {
            return new IteratorIssuer<T>((Iterator)result);
        } else
        if (result instanceof Iterable) {
            return new IteratorIssuer<T>((Iterable) result);
        } else
        if (result instanceof Object[]) {
            return new IteratorIssuer<T>(new ObjectArrayIterator((T[])result));
        } else
        if (result == null) {
            return new IteratorIssuer<T>(Collections.<T>emptyList().iterator());
        } else {
            final Collection single = Collections.singleton(result);
            return new IteratorIssuer<T>(single);
        }
    }

}
