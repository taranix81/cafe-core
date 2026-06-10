package org.taranix.cafe.beans.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.metadata.CafeMetadataRegistry;
import org.taranix.cafe.beans.repositories.beans.BeansRepository;

import java.util.Optional;
import java.util.Set;

class CafeValidationServiceTest {

    private static final CafeMetadataRegistry EMPTY_REGISTRY = CafeMetadataRegistry.builder().build();
    private static final BeansRepository EMPTY_REPO = new BeansRepository();

    private static final CafeValidator PASSING = (registry, repository) -> Optional.empty();

    private static final CafeValidator FAILING = (registry, repository) ->
            Optional.of(ValidationResult.builder().message("failure").objects(Set.of()).build());

    @Test
    @DisplayName("validate: returns empty set when all validators pass")
    void returnsEmptySetWhenAllValidatorsPass() {
        CafeValidator anotherPassing = (registry, repository) -> Optional.empty();
        CafeValidationService service = new CafeValidationService(Set.of(PASSING, anotherPassing));
        Set<ValidationResult> results = service.validate(EMPTY_REGISTRY, EMPTY_REPO);
        Assertions.assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("validate: returns one result when one validator fails")
    void returnsOneResultWhenOneValidatorFails() {
        CafeValidationService service = new CafeValidationService(Set.of(PASSING, FAILING));
        Set<ValidationResult> results = service.validate(EMPTY_REGISTRY, EMPTY_REPO);
        Assertions.assertEquals(1, results.size());
    }

    @Test
    @DisplayName("validate: returns two results when two validators fail")
    void returnsTwoResultsWhenTwoValidatorsFail() {
        CafeValidator anotherFailing = (registry, repository) ->
                Optional.of(ValidationResult.builder().message("second failure").objects(Set.of()).build());
        CafeValidationService service = new CafeValidationService(Set.of(FAILING, anotherFailing));
        Set<ValidationResult> results = service.validate(EMPTY_REGISTRY, EMPTY_REPO);
        Assertions.assertEquals(2, results.size());
    }

    @Test
    @DisplayName("validate: Optional.empty() result is not included in output")
    void optionalEmptyResultIsExcluded() {
        CafeValidationService service = new CafeValidationService(Set.of(PASSING));
        Set<ValidationResult> results = service.validate(EMPTY_REGISTRY, EMPTY_REPO);
        Assertions.assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("validate: empty validator set returns empty results")
    void emptyValidatorSetReturnsEmpty() {
        CafeValidationService service = new CafeValidationService(Set.of());
        Set<ValidationResult> results = service.validate(EMPTY_REGISTRY, EMPTY_REPO);
        Assertions.assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("CafeValidationResultFormatter.format: returns non-empty string for failing results")
    void formatterReturnsNonEmptyStringForFailures() {
        Set<ValidationResult> results = Set.of(
                ValidationResult.builder().message("error one").objects(Set.of()).build()
        );
        String formatted = CafeValidationResultFormatter.format(results);
        Assertions.assertFalse(formatted.isBlank());
        Assertions.assertTrue(formatted.contains("error one"));
    }

    @Test
    @DisplayName("CafeValidationResultFormatter.format: returns empty string for empty results")
    void formatterReturnsEmptyStringForEmptyResults() {
        String formatted = CafeValidationResultFormatter.format(Set.of());
        Assertions.assertTrue(formatted.isEmpty());
    }

    @Test
    @DisplayName("CafeValidationResultFormatter.format: returns empty string for null input")
    void formatterReturnsEmptyStringForNull() {
        String formatted = CafeValidationResultFormatter.format(null);
        Assertions.assertTrue(formatted.isEmpty());
    }
}
