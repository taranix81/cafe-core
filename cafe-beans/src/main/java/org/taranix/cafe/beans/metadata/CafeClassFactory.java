package org.taranix.cafe.beans.metadata;

import java.util.Objects;

public class CafeClassFactory {

    public static CafeClass create(Class<?> aClass) {
        Objects.requireNonNull(aClass, "Class cannot be null");
        return new CafeClass(aClass);
    }


}
