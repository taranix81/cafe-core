package org.taranix.cafe.beans.repositories;

import java.util.Collection;

public interface Repository<TKey, TValue> {
    TValue getOne(TKey key);

    boolean contains(TKey key);

    Collection<TValue> getMany(TKey key);

    void set(TKey typeKey, TValue bean);

    void clear();

    Collection<TKey> getAllKeys();

    void unSet(TKey typeKey);
}
