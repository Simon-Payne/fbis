package com.flatshire.fbis;

/**
 *
 * Thrown to signal that a service is temporarily unavailable such that a retry later
 * may be appropriate.
 */
public class DataFeedServiceUnavailableException extends RuntimeException {

    public DataFeedServiceUnavailableException(Throwable cause) {
        super("Service unavailable, advice was \"%s\"".formatted(cause.getMessage()),
                cause);
    }
}
