package org.taranix.cafe.beans.exceptions;

public class ReflectionUtilsException extends CafeException {
    public ReflectionUtilsException(final String message) {
        super(message);
    }

    public ReflectionUtilsException(final String message, Throwable cause) {
        super(message, cause);
    }
}
