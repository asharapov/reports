package org.echosoft.framework.reports.processor;

import org.apache.poi.ss.usermodel.Workbook;
import org.echosoft.framework.reports.model.Report;
import org.echosoft.framework.reports.model.el.ELContext;

/**
 * Интерфейс который должен реализовывать построитель отчетов оформленных в виде электронных таблиц.
 *
 * @author Anton Sharapov
 */
public interface ReportProcessor {

    /**
     * Формирует отчет на основании его модели и указанных пользователем в контексте параметров.
     *
     * @param report модель формируемого отчета.
     * @param ctx    данные необходимые для формирования данного отчета.
     * @return сформированный отчет.
     * @throws ReportProcessingException в случае каких-либо проблем.
     */
    public Workbook process(final Report report, final ELContext ctx) throws ReportProcessingException;

}
