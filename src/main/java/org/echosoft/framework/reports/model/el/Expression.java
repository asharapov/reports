package org.echosoft.framework.reports.model.el;

import java.io.Serializable;

/**
 * Описывает вычисляемые выражения, на основе которых формируется содержимое одной ячейки отчета.
 *
 * @author Anton Sharapov
 */
public interface Expression extends Serializable {

    /**
     * @return true  если выражение является полностью статичным, т.е. не содержит ссылок на
     * параметры в том или ином пространстве имен контекста.
     */
    boolean isStatic();

    /**
     * Вычисляет выражение на основе приведенного контекста.
     *
     * @param context текущий контекст выполнения.
     * @return содержимое ячейки отчета.
     * @throws Exception в случае возникновения каких-либо проблем.
     */
    Object getValue(ELContext context) throws Exception;

}
