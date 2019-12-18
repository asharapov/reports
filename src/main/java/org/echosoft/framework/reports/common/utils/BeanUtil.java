package org.echosoft.framework.reports.common.utils;

import java.beans.Introspector;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.echosoft.framework.reports.common.types.Type;
import org.echosoft.framework.reports.common.types.TypeRegistry;

/**
 * Используется для динамического доступа к свойствам объектов.
 *
 * @author Anton Sharapov
 * @version 2.0
 */
public class BeanUtil {

    private static final ConcurrentHashMap<Class<?>, BeanMetadata> cache = new ConcurrentHashMap<>();
    private static final TypeRegistry registry = new TypeRegistry();

    /**
     * <p>Возвращает значение свойства объекта.</p>
     * <p><strong>Важно: </strong> Если в процессе вычисления сложного выражения вида <code>a.b.c</code> значение подвыражения <code>a.b</code> получилось равным <code>null</code> то
     * данный метод вернет <code>null</code> а не поднимет исключение!</p>
     *
     * @param bean объект относительно которого вычисляется выражение.
     * @param expr вычисляемое выражение.
     * @return Значение полученное в результате вычисления выражения.
     * @throws IllegalAccessException    поднимается если вызывающий поток не имеет прав на обращение к вызываемому методу или полю класса.
     * @throws InvocationTargetException поднимается в случае если вызываемый метод возвращает исключительную ситуацию.
     */
    public static Object getProperty(final Object bean, final String expr) throws IllegalAccessException, InvocationTargetException {
        if (expr == null)
            throw new IllegalArgumentException("Expression not specified");
        if (bean == null)
            return null;

        int start = 0;
        Object result = bean;
        final int length = expr.length();
        for (int i = 0; i < length; i++) {
            final char c = expr.charAt(i);
            switch (c) {
                case '.': {
                    if (i > start) {
                        final BeanMetadata meta = getMetadata(result.getClass());
                        result = meta.getValue(result, expr.substring(start, i));
                        if (result == null)
                            return null;
                    }
                    start = i + 1;
                    break;
                }
                case '[': {
                    final int r = expr.indexOf(']', i);
                    if (r < i)
                        throw new IllegalArgumentException("Missed ']' symbol: " + expr);
                    final List<String> lst = StringUtil.split(expr.substring(i + 1, r), ',');
                    final String[] args = lst != null ? lst.toArray(new String[lst.size()]) : null;
                    if (i > start) {
                        final BeanMetadata meta = getMetadata(result.getClass());
                        result = meta.getValue(result, expr.substring(start, i));
                        if (result == null)
                            return null;
                    }
                    result = getIndexedProperty(result, args);
                    if (result == null)
                        return null;
                    i = r;
                    start = i + 1;
                    break;
                }
                case '(': {
                    final int r = expr.indexOf(')', i);
                    if (r < i)
                        throw new IllegalArgumentException("Missed ')' symbol: " + expr);
                    final List<String> lst = StringUtil.split(expr.substring(i + 1, r), ',');
                    final String[] args = lst != null ? lst.toArray(new String[lst.size()]) : null;
                    final BeanMetadata meta = getMetadata(result.getClass());
                    result = meta.getValue(result, expr.substring(start, i), args);
                    if (result == null)
                        return null;
                    i = r;
                    start = i + 1;
                    break;
                }
            }
        }
        if (start < length) {
            final BeanMetadata meta = getMetadata(result.getClass());
            result = meta.getValue(result, expr.substring(start));
        }
        return result;
    }

    /**
     * Устанавливает новое значение свойства объекта. Допускается использование вложенных свойств.
     * <p><strong>Важно: </strong> Если в процессе установки значения для выражения вида <code>a.b.c</code> значение подвыражения <code>a</code> или <code>a.b</code> было вычислено как <code>null</code> то
     * данный метод поднимет исключение.</p>
     *
     * @param bean  объект чье свойство подлежит изменению
     * @param expr  выражение, ссылающееся на свойство чье значение требуется изменить.
     * @param value новое значение свойства.
     * @throws IllegalAccessException    поднимается если вызывающий поток не имеет прав на обращение к вызываемому методу или полю класса.
     * @throws InvocationTargetException поднимается в случае если вызываемый метод возвращает исключительную ситуацию.
     * @throws NullPointerException      если в процессе вычисления сложного выражения было получено <code>null</code>.
     */
    public static void setProperty(final Object bean, final String expr, final Object value) throws IllegalAccessException, InvocationTargetException {
        int start = 0;
        Object scope = bean;
        final int length = expr.length();
        final int lastPos = length - 1;
        for (int i = 0; i < length; i++) {
            final char c = expr.charAt(i);
            switch (c) {
                case '.': {
                    if (i > start) {
                        final BeanMetadata meta = getMetadata(scope.getClass());
                        scope = meta.getValue(scope, expr.substring(start, i));
                    }
                    start = i + 1;
                    break;
                }
                case '[': {
                    final int r = expr.indexOf(']', i);
                    if (r < i)
                        throw new IllegalArgumentException("Missed ']' symbol: " + expr);
                    final List<String> lst = StringUtil.split(expr.substring(i + 1, r), ',');
                    final String[] args = lst != null ? lst.toArray(new String[lst.size()]) : null;
                    if (i > start) {
                        final BeanMetadata meta = getMetadata(scope.getClass());
                        scope = meta.getValue(scope, expr.substring(start, i));
                    }
                    if (r < lastPos) {
                        scope = getIndexedProperty(scope, args);
                    } else {
                        setIndexedProperty(scope, args, value);
                    }
                    i = r;
                    start = i + 1;
                    break;
                }
                case '(': {
                    final int r = expr.indexOf(')', i);
                    if (r < i)
                        throw new IllegalArgumentException("Missed ')' symbol: " + expr);
                    final List<String> lst = StringUtil.split(expr.substring(i + 1, r), ',');
                    final String[] args = lst != null ? lst.toArray(new String[lst.size()]) : null;
                    final BeanMetadata meta = getMetadata(scope.getClass());
                    if (r < lastPos) {
                        scope = meta.getValue(scope, expr.substring(start, i), args);
                    } else {
                        meta.setValue(scope, expr.substring(start, i), args, value);
                    }
                    i = r;
                    start = i + 1;
                    break;
                }
            }
        }
        if (start < length) {
            final BeanMetadata meta = getMetadata(scope.getClass());
            meta.setValue(scope, expr.substring(start), value);
        }
    }

    public static void reset() {
        cache.clear();
    }

    public static void reset(final ClassLoader clsLoader) {
        for (Class<?> cls : cache.keySet()) {
            if (cls.getClassLoader().equals(clsLoader)) {
                cache.remove(cls);
            }
        }
    }

    public static void reset(final Class<?> cls) {
        cache.remove(cls);
    }


    private static BeanMetadata getMetadata(final Class<?> cl) {
        BeanMetadata meta = cache.get(cl);
        if (meta == null) {
            meta = new BeanMetadata(cl);
            cache.put(cl, meta);
        }
        return meta;
    }

    private static Object getIndexedProperty(final Object bean, final String[] args) {
        Object result = bean;
        for (int i = 0; i < args.length; i++) {
            if (result == null)
                break;
            final int idx = Integer.parseInt(args[i], 10);
            if (result.getClass().isArray()) {
                result = Array.get(result, idx);
            } else
            if (result instanceof List) {
                result = ((List) result).get(idx);
            } else
            if (result instanceof Iterable) {
                final Iterator it = ((Iterable) result).iterator();
                for (int j = idx; j >= 0; j--) {
                    result = it.next();
                }
            } else
            if (result instanceof Iterator) {
                final Iterator it = (Iterator) result;
                for (int j = idx; j >= 0; j--) {
                    result = it.next();
                }
            }
        }
        return result;
    }

    private static void setIndexedProperty(Object bean, final String[] args, final Object value) {
        final int lastPos = args.length - 1;
        if (lastPos > 0) {
            final String[] args2 = new String[lastPos];
            System.arraycopy(args, 0, args2, 0, lastPos);
            bean = getIndexedProperty(bean, args2);
        }

        final int idx = Integer.parseInt(args[lastPos], 10);
        if (bean.getClass().isArray()) {
            Array.set(bean, idx, value);
        } else
        if (bean instanceof List) {
            ((List) bean).set(idx, value);
        }
    }


    private static final class BeanMetadata {
        private final String clsName;
        private final Map<String, Getter> getters;
        private final Map<String, Setter> setters;
        private final boolean inheritsMapInterface;

        private BeanMetadata(final Class<?> cls) {
            this.getters = new HashMap<>();
            this.setters = new HashMap<>();
            this.clsName = cls.getName();
            this.inheritsMapInterface = Map.class.isAssignableFrom(cls);
            init(cls);
        }

        private void init(final Class<?> cls) {
            getters.clear();
            setters.clear();
            for (final Field field : cls.getFields()) {
                if (Modifier.isStatic(field.getModifiers()))
                    continue;
                final FieldAccessor accessor = new FieldAccessor(field);
                getters.put(field.getName(), accessor);
                if (!Modifier.isFinal(field.getModifiers()))
                    setters.put(field.getName(), accessor);
            }
            final HashMap<String, ArrayList<Method>> overloadedGetters = new HashMap<String, ArrayList<Method>>();
            for (final Method method : cls.getMethods()) {
                if (Modifier.isStatic(method.getModifiers()))
                    continue;
                final String name = method.getName();
                final int nameLength = name.length();
                final Class<?> returnType = method.getReturnType();
                final Class<?>[] paramTypes = method.getParameterTypes();
                if (returnType != void.class && returnType != Void.class) {
                    // init getters ...
                    if (paramTypes.length == 0) {
                        final PropertyGetter getter = new PropertyGetter(method);
                        if (nameLength > 3 && name.startsWith("get", 0)) {
                            getters.put(Introspector.decapitalize(name.substring(3)), getter);
                        } else
                        if (nameLength > 2 && name.startsWith("is", 0) && (returnType == boolean.class || returnType == Boolean.class)) {
                            getters.put(Introspector.decapitalize(name.substring(2)), getter);
                        }
                        if (!getters.containsKey(name))
                            getters.put(name, getter);
                    } else {
                        ArrayList<Method> list = overloadedGetters.get(name);
                        if (list == null) {
                            list = new ArrayList<>();
                            overloadedGetters.put(name, list);
                        }
                        list.add(method);
                    }
                } else
                if (paramTypes.length == 1 && name.startsWith("set", 0)) {
                    // init setters ...
                    setters.put(Introspector.decapitalize(name.substring(3)), new PropertySetter(method));
                }
            }
            for (Map.Entry<String, ArrayList<Method>> entry : overloadedGetters.entrySet()) {
                final Method[] methods = entry.getValue().toArray(new Method[entry.getValue().size()]);
                final OverloadedMethodsGetter getter = new OverloadedMethodsGetter(methods);
                final String name = entry.getKey();
                if (!getters.containsKey(name))
                    getters.put(name, getter);
                if (name.startsWith("get", 0)) {
                    final String altName = Introspector.decapitalize(name.substring(3));
                    if (!getters.containsKey(altName))
                        getters.put(altName, getter);
                }
            }
        }

        public Object getValue(final Object bean, final String name) throws InvocationTargetException, IllegalAccessException {
            final Getter getter = getters.get(name);
            if (getter != null) {
                return getter.getValue(bean);
            } else
            if (inheritsMapInterface) {
                return ((Map) bean).get(name);
            } else
                throw new RuntimeException("Can't get '" + name + "' property for " + (bean != null ? bean.getClass().getName() : "null") + " bean.");
        }

        public Object getValue(final Object bean, final String name, final String[] args) throws InvocationTargetException, IllegalAccessException {
            final Getter getter = getters.get(name);
            if (getter != null) {
                return getter.getValue(bean, args);
            } else
                throw new RuntimeException("Can't get '" + name + "' property for " + (bean != null ? bean.getClass().getName() : "null") + " bean.");
        }

        public void setValue(final Object bean, final String name, final Object value) throws InvocationTargetException, IllegalAccessException {
            final Setter setter = setters.get(name);
            if (setter != null) {
                setter.setValue(bean, value);
            } else
            if (inheritsMapInterface) {
                ((Map) bean).put(name, value);
            } else
                throw new RuntimeException("Can't set '" + name + "' property for " + (bean != null ? bean.getClass().getName() : "null") + " bean.");
        }

        public void setValue(final Object bean, final String name, final String[] args, final Object value) {
            // TODO: fix bugs ...
            final Setter setter = setters.get(name);
//            if (setter==null && inheritsMapInterface) {
//            } else
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public String toString() {
            return "[BeanMetadata{" + clsName + "}]";
        }
    }


    private static interface Getter {
        public Object getValue(Object bean) throws IllegalAccessException, InvocationTargetException;

        public Object getValue(Object bean, final String[] args) throws IllegalAccessException, InvocationTargetException;
    }

    private static interface Setter {
        public void setValue(Object bean, Object value) throws IllegalAccessException, InvocationTargetException;
    }


    private static class FieldAccessor implements Getter, Setter {
        private final Field field;

        public FieldAccessor(final Field field) {
            this.field = field;
        }

        @Override
        public Object getValue(final Object bean) throws IllegalAccessException {
            return field.get(bean);
        }

        @Override
        public Object getValue(final Object bean, final String[] args) throws IllegalAccessException {
            if (args.length == 1) {
                final Object result = field.get(bean);
                if (result == null) {
                    return null;
                } else
                if (result instanceof Map) {
                    return ((Map) result).get(args[0]);
                }
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public void setValue(final Object bean, final Object value) throws IllegalAccessException {
            field.set(bean, value);
        }
    }

    private static final class PropertyGetter implements Getter {
        private final Method method;

        public PropertyGetter(final Method method) {
            this.method = method;
        }

        @Override
        public Object getValue(final Object bean) throws IllegalAccessException, InvocationTargetException {
            return method.invoke(bean);
        }

        @Override
        public Object getValue(final Object bean, final String[] args) throws InvocationTargetException, IllegalAccessException {
            if (args.length == 1) {
                final Object result = method.invoke(bean);
                if (result == null) {
                    return null;
                } else
                if (result instanceof Map) {
                    return ((Map) result).get(args[0]);
                }
            }
            throw new UnsupportedOperationException();
        }
    }

    private static final class PropertySetter implements Setter {
        private final Method method;

        public PropertySetter(final Method method) {
            this.method = method;
        }

        @Override
        public void setValue(final Object bean, final Object value) throws InvocationTargetException, IllegalAccessException {
            method.invoke(bean, value);
        }
    }

    private static final class OverloadedMethodsGetter implements Getter {
        private final Method[] methods;
        private final Class<?>[][] paramTypes;

        public OverloadedMethodsGetter(final Method[] methods) {
            this.methods = methods;
            this.paramTypes = new Class<?>[methods.length][];
            for (int i = 0; i < methods.length; i++) {
                this.paramTypes[i] = methods[i].getParameterTypes();
            }
        }

        @Override
        public Object getValue(final Object bean) throws InvocationTargetException, IllegalAccessException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getValue(final Object bean, final String[] args) throws InvocationTargetException, IllegalAccessException {
            for (int i = methods.length - 1; i >= 0; i--) {
                final Class<?>[] types = paramTypes[i];
                if (types.length != args.length)
                    continue;
                final Object[] objects = new Object[types.length];
                for (int j = types.length - 1; j >= 0; j--) {
                    final Type<?> type = registry.findType(types[j]);
                    if (type == null)
                        continue;
                    try {
                        objects[j] = type.decode(args[j]);
                    } catch (Exception e) {
                        continue;
                    }
                }
                return methods[i].invoke(bean, objects);
            }
            throw new IllegalArgumentException();
        }
    }

    private static final class OverloadedMethodsSetter implements Setter {
        private final Method[] methods;
        private final Class<?>[][] paramTypes;

        public OverloadedMethodsSetter(final Method[] methods) {
            this.methods = methods;
            this.paramTypes = new Class<?>[methods.length][];
            for (int i = 0; i < methods.length; i++) {
                this.paramTypes[i] = methods[i].getParameterTypes();
            }
        }

        @Override
        public void setValue(final Object bean, final Object value) throws InvocationTargetException, IllegalAccessException {
            if (value == null) {
                for (int i = methods.length - 1; i >= 0; i--) {
                    final Class<?>[] types = paramTypes[i];
                    if (types.length == 1 && !types[0].isPrimitive()) {
                        methods[i].invoke(bean, value);
                        return;
                    }
                }
            } else {
                for (int i = methods.length - 1; i >= 0; i--) {
                    final Class<?>[] types = paramTypes[i];
                    if (types.length == 1 && types[0].equals(value.getClass())) {
                        methods[i].invoke(bean, value);
                        return;
                    }
                }
                for (int i = methods.length - 1; i >= 0; i--) {
                    final Class<?>[] types = paramTypes[i];
                    if (types.length == 1 && types[0].isAssignableFrom(value.getClass())) {
                        methods[i].invoke(bean, value);
                        return;
                    }
                }
                for (int i = methods.length - 1; i >= 0; i--) {
                    final Class<?>[] types = paramTypes[i];
                    if (types.length == 1 && types[0].isAssignableFrom(value.getClass())) {
                        methods[i].invoke(bean, value);
                        return;
                    }
                }
            }
            throw new IllegalArgumentException();
        }
    }
}
