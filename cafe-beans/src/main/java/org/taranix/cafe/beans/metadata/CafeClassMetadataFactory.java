package org.taranix.cafe.beans.metadata;

import java.util.Objects;

public class CafeClassMetadataFactory {

    public static CafeClassMetadata create(Class<?> aClass) {
        Objects.requireNonNull(aClass, "Class cannot be null");
        return new CafeClassMetadata(aClass);
    }


}
