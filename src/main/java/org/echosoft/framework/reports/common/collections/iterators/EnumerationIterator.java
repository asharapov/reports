package org.echosoft.framework.reports.common.collections.iterators;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Представляет объект некоторого класса, реализующего интерфейс {@link Enumeration}
 * в виде другого объекта реализующего интерфейс {@link Iterator}.
 *
 * @author Anton Sharapov
 */
public class EnumerationIterator<T> implements Iterator<T> {
    
    private final Enumeration<T> enumeration;
    
    public EnumerationIterator(final Enumeration<T> enumeration) {
        this.enumeration = enumeration;
    }

    @Override
    public boolean hasNext() {
        return enumeration.hasMoreElements();
    }

    @Override
    public T next() {
        return enumeration.nextElement();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("this operation does not supported");
    }

}
