package org.taranix.cafe.desktop.datasource;

public interface DataSerializer<T> {

    T deserialize(byte[] bytes);

    byte[] serialize(T data);
}
