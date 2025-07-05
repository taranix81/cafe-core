package org.taranix.cafe.beans.repositories.beans;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.CafeReflectionUtils;
import org.taranix.cafe.beans.exceptions.BeansRepositoryException;
import org.taranix.cafe.beans.repositories.HashMapRepository;
import org.taranix.cafe.beans.repositories.typekeys.TypeKey;

import java.util.Collection;
import java.util.Objects;

@Slf4j
public class BeansRepository extends HashMapRepository<TypeKey, BeanRepositoryEntry> {

    @Override
    public BeanRepositoryEntry getOne(TypeKey typeKey) {
        if (contains(typeKey)) {
            Collection<BeanRepositoryEntry> instances = getMany(typeKey);
            if (instances.size() == 1) {
                return instances.stream()
                        .findFirst()
                        .orElse(null);
            } else {
                return instances.stream()
                        .filter(BeanRepositoryEntry::isPrimary)
                        .findFirst()
                        .orElseThrow(() -> new BeansRepositoryException("Couldn't determine which entry to return : %s"
                                .formatted(instances.stream().map(BeanRepositoryEntry::getValue).toList())));
            }
        }
        throw new BeansRepositoryException("No instance of " + typeKey);
    }

    @Override
    public void set(TypeKey typeKey, BeanRepositoryEntry entry) {
        validate(typeKey);
        validate(typeKey, entry);
        if (havingNoEntryWithSameSource(typeKey, entry)) {
            super.set(typeKey, entry);
        } else {
            log.warn("Entry with source {} already exists", entry.getSource());
        }
    }

    private void validate(TypeKey typeKey, BeanRepositoryEntry entry) {
        //double primary validation
        if (entry.isPrimary() && getMany(typeKey).stream().anyMatch(BeanRepositoryEntry::isPrimary)) {
            throw new BeansRepositoryException("There are already a primary bean for  %s".formatted(typeKey));
        }
    }

    private boolean havingNoEntryWithSameSource(TypeKey typeKey, BeanRepositoryEntry value) {
        return Objects.isNull(value.getSource()) || getMany(typeKey).stream()
                .noneMatch(beanRepositoryEntry -> Objects.equals(beanRepositoryEntry.getSource(), value.getSource()));
    }


    private void validate(final TypeKey typeKey) {
        if (CafeReflectionUtils.isGenericType(typeKey.getType())) {
            throw new BeansRepositoryException("Generic type are not allowed : %s".formatted(typeKey));
        }
    }


}
