package org.taranix.cafe.beans.repositories;

public interface MultiRepository<TKey, TValue> extends Repository<TKey, TValue> {

    void addRepository(Repository<TKey, TValue> repository);

    Repository<TKey, TValue> getPrimary();

    void setPrimary(Repository<TKey, TValue> repository);
}
