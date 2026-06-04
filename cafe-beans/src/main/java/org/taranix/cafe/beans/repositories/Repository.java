package org.taranix.cafe.beans.repositories;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Repository<TKey, TValue> {
    TValue getOne(TKey key);

    boolean contains(TKey key);

    Collection<TValue> getMany(TKey key);

    void set(TKey typeKey, TValue bean);

    void clear();

    Collection<TKey> getKeys();

    void unSet(TKey typeKey);

    Stream<TKey> getKeys(Function<TKey, Boolean> filter);

    <TKey2> Stream<TKey2> getKeys(Class<TKey2> key);
}
