package org.taranix.cafe.beans.repositories.typekeys;

public interface TypeKey {
    @Override
    String toString();

    java.lang.reflect.Type getType();

    String getTypeIdentifier();
}
