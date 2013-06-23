package org.echosoft.common.providers;

import java.io.Closeable;

/**
 * @author Anton Sharapov
 */
public interface BeanIterator<T> extends Closeable {

    /**
     * Returns <code>true</code> if the dataset has more beans of the same type.
     * (In other words, returns <code>true</code> if <code>next</code> would return an element
     * rather than throwing an exception.)
     *
     * @return <code>true</code> if the dataset has more rows.
     */
    public boolean hasNext() throws Exception;

    /**
     * Retrieves the next bean from the dataset.
     *
     * @return a corresponding bean instance.
     * @throws java.util.NoSuchElementException
     *          if dataset has no more elements.
     */
    public T next() throws Exception;


    /**
     * Returns the next element in the iteration without changing iteration state.
     *
     * @return the next element in the iteration.
     * @throws java.util.NoSuchElementException
     *          if iteration has no more elements.
     */
    public T readAhead() throws Exception;

    /**
     * This method called, when the client has finished iterating
     * through the dataset to allow resources to be deallocated.
     */
    public void close();
}
