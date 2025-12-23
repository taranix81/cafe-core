package org.taranix.cafe.beans.repositories;

import org.taranix.cafe.beans.exceptions.RepositoryException;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class HashMapRepository<TKey, TValue> implements Repository<TKey, TValue> {

    private final HashMap<TKey, Set<TValue>> mappings = new HashMap<>();

    public TValue getOne(TKey key) {
        if (contains(key)) {
            Collection<TValue> instances = getMany(key);
            if (instances.size() == 1) {
                return instances.stream().findFirst().orElse(null);
            } else {
                throw new RepositoryException("More than one instance of " + key);
            }
        }
        throw new RepositoryException("No instance of " + key);
    }

    public boolean contains(TKey key) {
        return !getMany(key).isEmpty();

    }

    public Collection<TValue> getMany(TKey key) {
        return new HashSet<>(mappings.getOrDefault(key, new HashSet<>()));
    }


    public void set(TKey key, TValue value) {
        mappings.computeIfAbsent(key, k -> new HashSet<>()).add(value);
    }

    public void clear() {
        mappings.clear();
    }

    @Override
    public Collection<TKey> getAllKeys() {
        return mappings.keySet();
    }


    @Override
    public void unSet(TKey typeKey) {
        mappings.remove(typeKey);
    }

    @Override
    public Collection<TKey> getKeys(Function<TKey, Boolean> filter) {
        return mappings.keySet().stream()
                .filter(filter::apply)
                .collect(Collectors.toSet());
    }


}
