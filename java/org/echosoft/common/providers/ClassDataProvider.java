package org.echosoft.common.providers;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;

import org.echosoft.common.collections.ObjectArrayIterator;
import org.echosoft.common.data.db.Query;
import org.echosoft.common.utils.StringUtil;

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
public class ClassDataProvider<T> implements DataProvider {

    private final Object object;
    private final Method method;
    private final Class[] paramTypes;

    public ClassDataProvider(final Object object, String methodName) {
        if (object == null)
            throw new NullPointerException("Object must be specified");
        if ((methodName = StringUtil.trim(methodName)) == null)
            throw new NullPointerException("Method must be specified");
        this.object = object;

        Method method;
        try {
            method = object.getClass().getMethod(methodName, Query.class);
        } catch (NoSuchMethodException e) {
            try {
                method = object.getClass().getMethod(methodName, (Class[]) null); // upcasting to Class[] required for compatibility to JSDK 5
            } catch (NoSuchMethodException ee) {
                throw new IllegalArgumentException("Object [" + object + "] has not appropriated method with name [" + methodName + "]");
            }
        }
        this.method = method;
        this.paramTypes = method.getParameterTypes();
    }


    @SuppressWarnings("unchecked")
    @Override
    public BeanIterator<T> execute(final Query query) throws Exception {
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
        if (result instanceof BeanIterator) {
            return (BeanIterator) result;
        } else
        if (result instanceof Iterator) {
            return new ProxyBeanIterator<T>((Iterator)result);
        } else
        if (result instanceof Iterable) {
            return new ProxyBeanIterator<T>((Iterable) result);
        } else
        if (result instanceof Object[]) {
            return new ProxyBeanIterator<T>(new ObjectArrayIterator((T[])result));
        } else
        if (result == null) {
            return new ProxyBeanIterator<T>(Collections.<T>emptyList().iterator());
        } else
            throw new IllegalArgumentException("Invalid method [" + method + "] return type: " + result.getClass());
    }
}
