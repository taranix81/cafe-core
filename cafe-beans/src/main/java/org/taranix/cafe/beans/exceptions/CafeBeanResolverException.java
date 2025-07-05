package org.taranix.cafe.beans.exceptions;

import lombok.Getter;
import org.taranix.cafe.beans.repositories.typekeys.TypeKey;

public class CafeBeanResolverException extends RuntimeException {

    @Getter
    private final TypeKey typeKey;

    public CafeBeanResolverException(String message, TypeKey typeKey) {
        super(message);
        this.typeKey = typeKey;
    }
}
