package org.echosoft.framework.reports.model.providers;

import java.util.NoSuchElementException;

import org.echosoft.framework.reports.common.collections.issuers.ReadAheadIssuer;
import org.echosoft.framework.reports.common.utils.ObjectUtil;
import org.echosoft.framework.reports.model.el.ELContext;
import org.echosoft.framework.reports.model.el.Expression;
import org.echosoft.framework.reports.processor.ExcelReportProcessor;
import org.echosoft.framework.reports.processor.ExecutionContext;
import org.echosoft.framework.reports.processor.SectionContext;

/**
 * Предназначен для динамического конструирования поставщиков данных на основе контекста выполнения отчета.
 *
 * @author Anton Sharapov
 */
public class FilteredDataProvider implements DataProvider {

    /**
     * Идентификатор поставщика данных.
     */
    private final String id;
    private final String predicateId;

    /**
     * Предикат используемый для фильтрации данных в исходном поставщике данных.
     * Результат данного выражения должен соответствовать классу {@link ComparablePredicate}.
     */
    private Expression predicate;

    public FilteredDataProvider(final String id) {
        this.id = id;
        this.predicateId = id + "-p-int";
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Возвращает предикат используемый для фильтрации данных в исходном поставщике данных.
     *
     * @return выражение, чье значение должно соответствовать классу {@link ComparablePredicate}.
     */
    public Expression getPredicate() {
        return predicate;
    }
    public void setPredicate(final Expression predicate) {
        this.predicate = predicate;
    }

    public ComparablePredicate getPredicateValue(final ELContext ctx) throws Exception {
        ComparablePredicate result = (ComparablePredicate)ctx.getVariables().get(predicateId);
        if (result == null && predicate != null) {
            final Object obj = predicate.getValue(ctx);
            if (obj == null)
                return null;
            if (obj instanceof ComparablePredicate)
                return (ComparablePredicate) obj;
            if (!(obj instanceof CharSequence))
                throw new IllegalArgumentException("Invalid ComparablePredicate instance: " + obj);
            final String predicateCls = obj.toString();
            result = ObjectUtil.makeInstance(predicateCls, ComparablePredicate.class);
            ctx.getVariables().put(predicateId, result);
        }
        return result;
    }

    @Override
    public ReadAheadIssuer getIssuer(final ELContext ctx) throws Exception {
        final ExecutionContext ectx = (ExecutionContext) ctx.getVariables().get(ExcelReportProcessor.VAR_CONTEXT);
        SectionContext sctx = ectx.sectionContext.parent;
        while (sctx != null && sctx.issuer == null) {
            sctx = sctx.parent;
        }
        final ComparablePredicate cp = getPredicateValue(ctx);
        return (sctx != null && cp != null)
                ? new FilteredBeanIterator<Object>(sctx.issuer, sctx.bean, cp)
                : null;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }


    private static final class FilteredBeanIterator<T> implements ReadAheadIssuer<T> {
        private final ReadAheadIssuer<T> issuer;
        private final T template;
        private final ComparablePredicate<T> predicate;
        private boolean hasNextBean;

        private FilteredBeanIterator(final ReadAheadIssuer<T> issuer, final T template, final ComparablePredicate<T> predicate) throws Exception {
            this.issuer = issuer;
            this.template = template;
            this.predicate = predicate;
            checkNextObject();
        }

        @Override
        public boolean hasNext() {
            return hasNextBean;
        }

        @Override
        public T next() throws Exception {
            if (!hasNextBean)
                throw new NoSuchElementException();
            T result = issuer.next();
            checkNextObject();
            return result;
        }

        @Override
        public T readAhead() throws Exception {
            if (!hasNextBean)
                throw new NoSuchElementException();
            return issuer.readAhead();
        }

        @Override
        public void close() {
        }

        private void checkNextObject() throws Exception {
            hasNextBean = issuer.hasNext() && predicate.evaluate(template, issuer.readAhead());
        }
    }
}

