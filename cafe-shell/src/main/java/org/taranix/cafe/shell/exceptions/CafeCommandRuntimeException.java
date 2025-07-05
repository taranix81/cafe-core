package org.taranix.cafe.shell.exceptions;

public class CafeCommandRuntimeException extends RuntimeException {
    public CafeCommandRuntimeException(final Exception e) {
        super(e);
    }

    public CafeCommandRuntimeException(String message) {
        super(message);
    }
}
