package org.taranix.cafe.beans.validation;

import org.taranix.cafe.beans.metadata.CafeMetadataRegistry;

import java.util.Optional;

public interface CafeValidator {

    Optional<ValidationResult> validate(CafeMetadataRegistry registry);
}
