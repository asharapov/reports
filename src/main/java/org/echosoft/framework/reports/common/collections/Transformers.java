package org.echosoft.framework.reports.common.collections;

/**
 * @author Anton Sharapov
 */
public class Transformers {

    /**
     * Ничего не делающий трансформер. Результатом трансформации является исходный объект переданный в аргументе метода.
     */
    public static final Transformer NOTHING =
            new Transformer() {
                @Override
                public Object transform(final Object value) {
                    return value;
                }
            };

    @SuppressWarnings("unchecked")
    public static <S, D> Transformer<S, D> nothing() {
        return (Transformer<S,D>)NOTHING;
    }
}
