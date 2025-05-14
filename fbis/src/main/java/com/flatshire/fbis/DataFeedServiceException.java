package com.flatshire.fbis;

/**
 *
 * Thrown to signal a general issue with the data feed service.
 */
public class DataFeedServiceException extends RuntimeException {

    public DataFeedServiceException(Throwable cause) {
        super("Service error, cause was \"%s\"".formatted(cause.getMessage()),
                cause);
    }
}
