package org.taranix.cafe.beans.exceptions;

public abstract class CafeException extends RuntimeException {
    protected CafeException(String message) {
        super(message);
    }

    protected CafeException(String message, Throwable cause) {
        super(message, cause);
    }
}
