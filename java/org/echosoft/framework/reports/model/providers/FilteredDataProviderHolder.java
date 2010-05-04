package org.echosoft.framework.reports.model.providers;

import java.util.NoSuchElementException;

import org.echosoft.common.query.BeanIterator;
import org.echosoft.common.query.BeanMetaData;
import org.echosoft.common.query.Query;
import org.echosoft.common.query.QueryResult;
import org.echosoft.common.query.providers.DataProvider;
import org.echosoft.common.query.providers.DataProviderException;
import org.echosoft.framework.reports.model.el.ELContext;
import org.echosoft.framework.reports.model.el.Expression;
import org.echosoft.framework.reports.processor.ExecutionContext;
import org.echosoft.framework.reports.processor.SectionContext;

/**
 * Предназначен для динамического конструирования поставщиков данных на основе контекста выполнения отчета.
 *
 * @author Anton Sharapov
 */
public class FilteredDataProviderHolder implements DataProviderHolder {

    /**
     * Идентификатор поставщика данных.
     */
    private final String id;

    /**
     * Предикат используемый для фильтрации данных в исходном поставщике данных.
     * Результат данного выражения должен соответствовать классу {@link ComparablePredicate}.
     */
    private Expression predicate;

    public FilteredDataProviderHolder(String id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return id;
    }

    /**
     * Возвращает предикат используемый для фильтрации данных в исходном поставщике данных.
     * @return выражение, чье значение должно соответствовать классу {@link ComparablePredicate}.
     */
    public Expression getPredicate() {
        return predicate;
    }
    public void setPredicate(Expression predicate) {
        this.predicate = predicate;
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public DataProvider getProvider(final ELContext ctx) {
        return new DataProvider() {
            public BeanIterator execute(final Query query) throws DataProviderException {
                try {
                    final ExecutionContext ectx = (ExecutionContext)ctx.getVariables().get("context");
                    SectionContext sctx = ectx.sectionContext.parent;
                    while (sctx!=null && sctx.beanIterator==null) {
                        sctx = sctx.parent;
                    }
                    final ComparablePredicate cp = predicate!=null ? (ComparablePredicate)predicate.getValue(ctx) : null;
                    return (sctx!=null && cp!=null)
                                ? new FilteredBeanIterator(sctx.beanIterator, sctx.bean,  cp)
                                : null;
                } catch (Exception e) {
                    throw new DataProviderException(e.getMessage(), e);
                }
            }
            public QueryResult executePaged(final Query query) throws DataProviderException {
                throw new UnsupportedOperationException();
            }
        };

    }

    /**
     * {@inheritDoc}
     */
    public Query getQuery(final ELContext ctx) {
        return null;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}

final class FilteredBeanIterator<T> implements BeanIterator<T> {

    private final BeanIterator<T> iterator;
    private final T template;
    private final ComparablePredicate<T> predicate;
    private boolean hasNextBean;

    public FilteredBeanIterator(BeanIterator<T> iterator, T template, ComparablePredicate<T> predicate) throws Exception {
        this.iterator = iterator;
        this.template = template;
        this.predicate = predicate;
        checkNextObject();
    }

    public BeanMetaData getMetaData() {
        return iterator.getMetaData();
    }

    public boolean hasNext() {
        return hasNextBean;
    }

    public T next() throws Exception {
        if (!hasNextBean)
            throw new NoSuchElementException();
        T result = iterator.next();
        checkNextObject();
        return result;
    }

    public T readAhead() {
        if (!hasNextBean)
            throw new NoSuchElementException();
        return iterator.readAhead();
    }

    public void close() {
    }

    private void checkNextObject() throws Exception {
        hasNextBean = iterator.hasNext() && predicate.evaluate(template, iterator.readAhead());
    }

}
