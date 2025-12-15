package org.taranix.cafe.beans.validation;

import java.util.Set;
import java.util.stream.Collectors;


public class CafeValidationResultFormatter {

    public static String format(Set<ValidationResult> validationResults) {
        if (validationResults == null || validationResults.isEmpty()) {
            return "";
        }

        return validationResults.stream()
                .map(result -> {
                    String objectsDetails = result.objects().stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(" | "));

                    return String.format("%s (Objects: %s)",
                            result.message(),
                            objectsDetails.isEmpty() ? "No related objects" : objectsDetails);
                })
                .collect(Collectors.joining("\n"));
    }
}