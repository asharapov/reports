package org.echosoft.framework.reports.model.events;

import org.echosoft.framework.reports.processor.ExecutionContext;

/**
 * @author Anton Sharapov
 */
public class BaseSectionEventListener implements SectionEventListener {

    @Override
    public void beforeSection(final ExecutionContext ectx) throws Exception {
    }

    @Override
    public void beforeRecord(final ExecutionContext ectx) throws Exception {
    }

    @Override
    public void afterRecord(final ExecutionContext ectx) throws Exception {
    }

    @Override
    public void afterSection(final ExecutionContext ectx) throws Exception {
    }
}
