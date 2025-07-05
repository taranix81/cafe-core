package org.taranix.cafe.beans.repositories.typekeys;


import java.lang.reflect.Type;
import java.util.Objects;

public class PropertyTypeKey extends TypeKey {


    private PropertyTypeKey(Type type, String typeIdentifier) {
        super(type, typeIdentifier);
    }

    public static PropertyTypeKey from(String propertyName) {
        return new PropertyTypeKey(Object.class, propertyName);
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(getTypeIdentifier());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof PropertyTypeKey propertyTypeKey) {
            return getTypeIdentifier().equals(propertyTypeKey.getTypeIdentifier());
        }

        return false;
    }
}
