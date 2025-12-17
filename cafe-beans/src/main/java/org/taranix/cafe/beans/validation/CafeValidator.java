package org.taranix.cafe.beans.validation;

import org.taranix.cafe.beans.metadata.CafeBeansRegistry;

import java.util.Optional;

public interface CafeValidator {

    Optional<ValidationResult> validate(CafeBeansRegistry registry);
}
