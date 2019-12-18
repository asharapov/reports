package org.echosoft.framework.reports.common.collections;

/**
 * Содержит ряд общеупотребительных предикатов.
 *
 * @author Anton Sharapov
 */
public class Predicates {

    /**
     * Предикат, возвращающий <code>true</code> для абсолютно всех входных данных.
     */
    public static final Predicate ALL =
            new Predicate() {
                public boolean accept(final Object input) {
                    return true;
                }
            };

    /**
     * Предикат, возвращающий <code>false</code> для абсолютно всех входных данных.
     */
    public static final Predicate NOTHING =
            new Predicate() {
                public boolean accept(final Object input) {
                    return false;
                }
            };

    /**
     * Предикат, возвращающиц <code>true</code> для всех объектов не равных <code>null</code>.
     */
    public static final Predicate NOT_NULL =
            new Predicate() {
                @Override
                public boolean accept(final Object input) {
                    return input != null;
                }
            };

    /**
     * Метод возвращает параметризированный предикат, возвращающий <code>true</code> для абсолютно всех входных данных.
     *
     * @return параметризированный предикат, возвращающий <code>true</code> для абсолютно всех входных данных.
     */
    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> all() {
        return (Predicate<T>) ALL;
    }

    /**
     * Метод возвращает параметризированный предикат, возвращающий <code>true</code> для абсолютно всех входных данных.
     *
     * @return параметризированный предикат, возвращающий <code>false</code> для абсолютно всех входных данных.
     */
    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> nothing() {
        return (Predicate<T>) NOTHING;
    }

    /**
     * Метод возвращает параметризированный предикат, возвращающий <code>true</code> для абсолютно всех непустых данных.
     *
     * @return параметризированный предикат, возвращающий <code>false</code> для всех непустых данных.
     */
    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> notNull() {
        return (Predicate<T>) NOT_NULL;
    }

}
