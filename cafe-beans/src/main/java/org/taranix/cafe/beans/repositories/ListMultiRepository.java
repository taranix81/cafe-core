package org.taranix.cafe.beans.repositories;

import org.taranix.cafe.beans.exceptions.RepositoryException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ListMultiRepository<TKey, TValue> implements MultiRepository<TKey, TValue> {

    private final List<Repository<TKey, TValue>> repositories;


    private Repository<TKey, TValue> primaryRepository;

    public ListMultiRepository() {
        repositories = new ArrayList<>();
    }

    public ListMultiRepository(List<Repository<TKey, TValue>> repositories) {
        this.repositories = repositories;
    }


    public void addRepository(Repository<TKey, TValue> repository) {
        repositories.add(repository);
    }

    @Override
    public Repository<TKey, TValue> getPrimary() {
        return primaryRepository;
    }

    @Override
    public void setPrimary(Repository<TKey, TValue> repository) {
        primaryRepository = repository;
    }

    @Override
    public TValue getOne(TKey key) {
        //Check LeadRepository
        TValue found = Optional.ofNullable(getPrimary())
                .filter(lead -> lead.contains(key))
                .map(lead -> lead.getOne(key))
                .orElse(null);

        if (found != null) {
            return found;
        }

        // check other repositories
        for (Repository<TKey, TValue> repository : repositories) {
            if (repository.contains(key)) {
                return repository.getOne(key);
            }
        }

        throw new RepositoryException("No instance of " + key);
    }

    @Override
    public boolean contains(TKey key) {
        return repositories.stream().anyMatch(beansRepository -> beansRepository.contains(key));
    }


    /**
     * Return all instances identified by key from all repositories
     *
     * @param key, value identifier
     * @return Set of values matched
     */
    @Override
    public Collection<TValue> getMany(TKey key) {
        return repositories.stream()
                .map(r -> r.getMany(key))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    /**
     * If LeadRepository is set then, value will be set only in this repository.
     * Otherwise, value will be set in all repositories
     *
     * @param key,   value identified by key
     * @param value, value to be stored under key
     */
    @Override
    public void set(TKey key, TValue value) {
        Optional.ofNullable(getPrimary())
                .ifPresentOrElse(lead -> lead.set(key, value),
                        () -> repositories.forEach(r1 -> r1.set(key, value)));
    }

    /**
     * If LeadRepository is set then, only this repository will be wipe out.
     * Otherwise,  all repositories will be wipe-out
     */
    @Override
    public void clear() {
        Optional.ofNullable(getPrimary())
                .ifPresentOrElse(Repository::clear,
                        () -> repositories.forEach(Repository::clear));
    }

    /**
     * Collect all key across repositories.
     *
     * @return Set of TKey
     */
    @Override
    public Collection<TKey> getAllKeys() {
        return repositories.stream()
                .map(Repository::getAllKeys)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    /**
     * If LeadRepository is set then, only value will be unset only in this repository.
     * Otherwise, value will be unset in all repositories
     *
     * @param key, value identified by key to be removed from repository/-ies
     */
    @Override
    public void unSet(TKey key) {
        Optional.ofNullable(getPrimary())
                .ifPresentOrElse(lead -> lead.unSet(key),
                        () -> repositories.forEach(r1 -> r1.unSet(key)));

    }


}
