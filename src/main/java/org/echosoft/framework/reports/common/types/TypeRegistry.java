package org.echosoft.framework.reports.common.types;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Anton Sharapov
 */
public class TypeRegistry {

    private final ConcurrentHashMap<Class<?>, Type<?>> types;

    public TypeRegistry() {
        types = new ConcurrentHashMap<>();
        registerType(Types.CHARACTER, char.class);
        registerType(Types.BOOLEAN, boolean.class);
        registerType(Types.SHORT, short.class);
        registerType(Types.INTEGER, int.class);
        registerType(Types.LONG, long.class);
        registerType(Types.FLOAT, float.class);
        registerType(Types.DOUBLE, double.class);
        registerType(Types.STRING);
        registerType(Types.CHARACTER);
        registerType(Types.BOOLEAN);
        registerType(Types.SHORT);
        registerType(Types.INTEGER);
        registerType(Types.LONG);
        registerType(Types.FLOAT);
        registerType(Types.DOUBLE);
        registerType(Types.BIGINTEGER);
        registerType(Types.BIGDECIMAL);
        registerType(Types.DATE);
        registerType(Types.STRING_ARRAY);
        registerType(Types.INTEGER_ARRAY);
        registerType(Types.LONG_ARRAY);
    }

    public void registerType(final Type<?> type) {
        types.put(type.getTarget(), type);
    }

    public <T> void registerType(final Type<T> type, final Class<? super T> cl) {
        types.put(cl, type);
    }

    @SuppressWarnings("unchecked")
    public <T> Type<T> findType(final Class<T> cl) {
        Type<?> type = types.get(cl);
        if (type == null) {
            for (Map.Entry<Class<?>, Type<?>> entry : types.entrySet()) {
                if (cl.isAssignableFrom(entry.getKey())) {
                    type = entry.getValue();
                    break;
                }
            }
        }
        return (Type<T>) type;
    }
}
