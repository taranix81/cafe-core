package org.taranix.cafe.beans.validation;

import org.taranix.cafe.beans.metadata.CafeBeansDefinitionRegistry;

import java.util.Optional;

public interface CafeValidator {

    Optional<ValidationResult> validate(CafeBeansDefinitionRegistry registry);
}
