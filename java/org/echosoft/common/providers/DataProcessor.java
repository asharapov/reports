package org.echosoft.common.providers;

/**
 * @author Anton Sharapov
 */
public interface DataProcessor<T> {

    public void process(T bean) throws Exception;

}
