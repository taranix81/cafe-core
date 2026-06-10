package org.taranix.cafe.beans.validation;

import org.taranix.cafe.beans.metadata.CafeMetadataRegistry;
import org.taranix.cafe.beans.repositories.Repository;
import org.taranix.cafe.beans.repositories.beans.BeanRepositoryEntry;
import org.taranix.cafe.beans.repositories.typekeys.TypeKey;

import java.util.Optional;

public interface CafeValidator {

    Optional<ValidationResult> validate(CafeMetadataRegistry registry, Repository<TypeKey, BeanRepositoryEntry> repository);
}
