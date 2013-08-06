package org.echosoft.common.providers;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.echosoft.common.collections.issuers.Issuer;
import org.echosoft.common.collections.issuers.IteratorIssuer;
import org.echosoft.common.collections.issuers.ReadAheadIssuer;
import org.echosoft.common.collections.issuers.SimpleReadAheadIssuer;
import org.echosoft.common.collections.iterators.ObjectArrayIterator;
import org.echosoft.common.data.db.Query;

/**
 * Implementation of the {@link DataProvider} instance which uses dynamic invocations of the
 * appropriated class methods for querying requested data.
 * The next kinds of methods supported in this implementation:
 * <ul>
 * <li>Methods with one parameter - {@link Query}.
 * <li>Methods without any parameters.
 * </ul>
 *
 * @author Anton Sharapov
 */
public class ClassDataProvider<T, Q> implements DataProvider<T, Q> {

    private final Object object;
    private final Method method;
    private final Class[] paramTypes;

    public ClassDataProvider(final Object object, Method method) {
        if (object == null || method == null)
            throw new NullPointerException("Object and method must be specified");
        this.object = object;
        this.method = method;
        this.paramTypes = method.getParameterTypes();
    }


    @SuppressWarnings("unchecked")
    @Override
    public ReadAheadIssuer<T> execute(final Q query) throws Exception {
        final Object result;
        // вызываем метод ...
        if (paramTypes.length == 0) {  // это метод без параметров.
            result = method.invoke(object);
        } else
        if (paramTypes.length == 1) {  // это метод с одним параметром (должен быть наследник от Query)
            result = method.invoke(object, query);
        } else
            throw new IllegalArgumentException("Unsupported method [" + method + "] arguments. ");

        // анализируем полученный результат ...
        if (result instanceof ReadAheadIssuer) {
            return (ReadAheadIssuer) result;
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
