package org.echosoft.common.providers;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.echosoft.common.data.Query;
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
    public BeanIterator<T> execute(final Query query) throws DataProviderException {
        try {
            final Object result;
            // invoke method ...
            if (paramTypes.length == 0) {  // method hasn't any arguments ...
                result = method.invoke(object);
            } else
            if (paramTypes.length == 1) {  // method has one argument (should be Query) ...
                result = method.invoke(object, query);
            } else
                throw new IllegalArgumentException("Unsupported method [" + method + "] arguments. ");

            // resolve result values...
            if (result instanceof BeanIterator) {
                return (BeanIterator) result;
            } else
            if (result instanceof List) {
                return new ListBeanIterator<T>((List) result);
            } else
            if (result instanceof Object[]) {
                return new ListBeanIterator<T>((T[]) result);
            } else
            if (result instanceof Iterator) {
                return new ListDataProvider((Iterator) result).execute(query);
            } else
            if (result == null) {
                return new ListBeanIterator(Collections.emptyList());
            } else
                throw new IllegalArgumentException("Invalid method [" + method + "] return type: " + result.getClass());
        } catch (Exception e) {
            throw new DataProviderException(e.getMessage(), e);
        }
    }
}
