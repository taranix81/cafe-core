package org.taranix.cafe.beans.validation;

import org.taranix.cafe.beans.metadata.CafeMetadataRegistry;
import org.taranix.cafe.beans.repositories.Repository;
import org.taranix.cafe.beans.repositories.beans.BeanRepositoryEntry;
import org.taranix.cafe.beans.repositories.typekeys.TypeKey;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CafeValidationService {

    private final Set<CafeValidator> validatorSet;

    public CafeValidationService(Set<CafeValidator> validatorSet) {
        this.validatorSet = validatorSet;
    }

    public Set<ValidationResult> validate(CafeMetadataRegistry registry, Repository<TypeKey, BeanRepositoryEntry> repository) {
        return validatorSet.stream()
                .map(cafeValidator -> cafeValidator.validate(registry, repository))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }


}

