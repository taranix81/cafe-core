package org.taranix.cafe.beans.validation;

import org.taranix.cafe.beans.metadata.CafeClass;
import org.taranix.cafe.beans.metadata.CafeMember;
import org.taranix.cafe.beans.metadata.CafeMetadataRegistry;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CafeCycleDetectionValidator implements CafeValidator {

    private static final String CYCLE_ERROR_MESSAGE = "Circular dependencies detected in the Cafe context.";

    @Override
    public Optional<ValidationResult> validate(CafeMetadataRegistry registry) {
        // 1. Detect cycles among members (fields/methods)
        Collection<CafeMember> memberCycles = getMemberCycleSet(registry);

        // 2. Detect cycles among classes
        Collection<CafeClass> classCycles = getClassCycleSet(registry);

        // Check if any cycles were detected
        if (!memberCycles.isEmpty() || !classCycles.isEmpty()) {
            // Logic for formatting and gathering cycle details

            // Collecting all member/component info related to the cycle,
            // even if the cycle was originally detected at the class level

            Set<Object> allInvolvedObjects = Stream.concat(
                    memberCycles.stream(),
                    classCycles.stream()
            ).collect(Collectors.toSet());


            return Optional.of(ValidationResult.builder()
                    .message(CYCLE_ERROR_MESSAGE)
                    .objects(allInvolvedObjects)
                    .build());
        }

        return Optional.empty();
    }

    // --- Helper methods ---
    private Collection<CafeMember> getMemberCycleSet(CafeMetadataRegistry registry) {
        return registry.getMemberDependencyRegistry().cycleSet();
    }

    private Collection<CafeClass> getClassCycleSet(CafeMetadataRegistry registry) {
        return registry.getClassDependencyRegistry().cycleSet();
    }


}