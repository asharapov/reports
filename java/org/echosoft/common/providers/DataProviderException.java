package org.echosoft.common.providers;

/**
 * This exception will be throws in case of any errors in the {@link DataProvider} implementations.
 * @author Anton Sharapov
 */
public class DataProviderException extends RuntimeException {

    public DataProviderException() {
        super();
    }

    public DataProviderException(String message) {
        super(message);
    }

    public DataProviderException(Throwable cause) {
        super(cause);
    }

    public DataProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
