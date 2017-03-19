package org.echosoft.framework.reports.processor;

/**
 * Данное исключение поднимается в случае возникновения ошибок в процессе построения Excel отчетов.
 * Данный класс при помощи метода {@link #getContext} способен предоставить самую подробную информацию о месте возникновения ошибки и данных, приведших к этому.
 *
 * @author Anton Sharapov
 */
public class ReportProcessingException extends Exception {

    private final ExecutionContext ctx;

    public ReportProcessingException(final String message) {
        super(message);
        ctx = null;
    }

    public ReportProcessingException(final String message, final Throwable cause) {
        super(message, cause);
        ctx = null;
    }

    public ReportProcessingException(final String message, final ExecutionContext ctx) {
        super(message);
        this.ctx = ctx;
    }

    public ReportProcessingException(final String message, final Throwable cause, final ExecutionContext ctx) {
        super(message, cause);
        this.ctx = ctx;
    }

    /**
     * Возвращает контекст построения отчета, содержащий максимум информации о состоянии задачи на момент возникновения ошибки.
     *
     * @return контекст построения отчета или <code>null</code> если ошибка возникла на самых ранних этапах построения отчета.
     */
    public ExecutionContext getContext() {
        return ctx;
    }
}
