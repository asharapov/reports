package org.echosoft.framework.reports.model.events;

import java.io.Serializable;

import org.echosoft.framework.reports.common.utils.ObjectUtil;
import org.echosoft.framework.reports.model.el.ELContext;
import org.echosoft.framework.reports.model.el.Expression;

/**
 * Предназначен для динамического конструирования обработчиков событий на основе контекста выполнения отчета.
 *
 * @author Anton Sharapov
 */
public class CellEventListenerHolder implements Serializable, Cloneable {

    private final Expression classExpr;
    private final Expression instanceExpr;

    public CellEventListenerHolder(Expression classExpr, Expression instanceExpr) {
        this.classExpr = classExpr;
        this.instanceExpr = instanceExpr;
    }

    public CellEventListener getListener(ELContext context) {
        try {
            CellEventListener result = null;
            if (instanceExpr!=null) {
                final Object value = instanceExpr.getValue(context);
                result = (CellEventListener)value;
            }
            if (result==null && classExpr!=null) {
                final Object value = classExpr.getValue(context);
                if (value instanceof String) {
                    result = ObjectUtil.makeInstance((String)value, CellEventListener.class);
                } else
                if (value instanceof Class) {
                    result = ObjectUtil.makeInstance(((Class)value).getCanonicalName(), CellEventListener.class);
                } else
                if (value!=null)
                    throw new RuntimeException("Unsupported type of listener's class: "+classExpr+" = "+value.getClass());
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public int hashCode() {
        return classExpr!=null ? classExpr.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj==null || !getClass().equals(obj.getClass()))
            return false;
        final CellEventListenerHolder other = (CellEventListenerHolder)obj;
        return (classExpr!=null ? classExpr.equals(other.classExpr) : other.classExpr==null) &&
               (instanceExpr!=null ? instanceExpr.equals(other.instanceExpr) : other.instanceExpr==null);
    }

    @Override
    public String toString() {
        return "[CellEventListenerHolder{class:"+classExpr+", instance:"+instanceExpr+"}]";
    }

}