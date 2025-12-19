package org.taranix.cafe.beans.validation;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.annotations.base.CafeHandlerType;
import org.taranix.cafe.beans.annotations.modifiers.CafeOptional;
import org.taranix.cafe.beans.metadata.CafeMember;
import org.taranix.cafe.beans.metadata.CafeMetadataRegistry;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.util.*;

@Slf4j
public class CafeResolvableBeansValidator implements CafeValidator {

    private static final String NOT_RESOLVABLE_ERROR_MESSAGE =
            "The following members have unresolvable dependencies in the Cafe context.";

    /**
     * Validates whether all dependencies (except those marked as CafeOptional or CafeTaskable)
     * have providers in the registry.
     * * @param registry The registry of Cafe bean definitions.
     *
     * @return An Optional containing the ValidationResult if unresolvable members are found,
     * or Optional.empty() otherwise.
     */
    @Override
    public Optional<ValidationResult> validate(CafeMetadataRegistry registry) {
        Map<CafeMember, BeanTypeKey> unresolvableMembers = findNonResolvableMembers(registry);
        if (unresolvableMembers.isEmpty()) {
            // Validation successful – no unresolvable members
            return Optional.empty();
        }

        StringBuilder fullMessage = new StringBuilder(NOT_RESOLVABLE_ERROR_MESSAGE);
        // Building a detailed message containing the names of the members
        // that could not be resolved.
        for (Map.Entry<CafeMember, BeanTypeKey> unresolvableMember : unresolvableMembers.entrySet()) {
            fullMessage.append(unresolvableMember.getKey());
            fullMessage.append(" --> ");
            fullMessage.append(unresolvableMember.getValue());
            fullMessage.append("\n");
        }

        // Creating the ValidationResult
        return Optional.of(new ValidationResult(fullMessage.toString(), new HashSet<>(unresolvableMembers.keySet())));
    }


    /**
     * Returns a set of CafeMemberInfo that have unresolvable dependencies.
     * It iterates through all registered members and checks for unresolvable types.
     * * @param registry The registry containing all bean definitions.
     *
     * @return A Set of members with unresolvable dependencies.
     */
    private Map<CafeMember, BeanTypeKey> findNonResolvableMembers(CafeMetadataRegistry registry) {
        Map<CafeMember, BeanTypeKey> result = new HashMap<>();

        for (CafeMember member : registry.allMembers()) {
            BeanTypeKey nonResolvableDependencyType = findNonResolvableTypeForMember(registry, member);
            if (nonResolvableDependencyType != null) {
                result.put(member, nonResolvableDependencyType);
            }
        }
        return result;
    }


    /**
     * Checks if a CafeMemberInfo has any unresolvable dependency.
     * Returns the BeanTypeKey of the first unresolvable dependency, or null if all are resolvable.
     * * @param registry The registry used to check for providers.
     *
     * @param cafeMember The member (field/constructor parameter) to check.
     * @return The BeanTypeKey of the unresolvable dependency, or null if resolvable.
     */
    private BeanTypeKey findNonResolvableTypeForMember(CafeMetadataRegistry registry, CafeMember cafeMember) {
        // Skipping optional members or members annotated with @CafeTaskable
        if (cafeMember.getAnnotationModifiers().contains(CafeOptional.class) ||
                cafeMember.getAnnotationLifecycleMarkers().contains(CafeHandlerType.class)) {
            return null;
        }

        if (cafeMember.hasDependencies()) {
            // Get all type keys that can be provided by existing beans
            Set<BeanTypeKey> providedTypeKeys = registry.getMemberDependencyRegistry().providersTypeKeys(cafeMember);

            for (BeanTypeKey dependency : cafeMember.getRequiredTypeKeys()) {
                // Check if a match exists among available providers for the current dependency
                if (!BeanTypeKey.isMatchByTypeOrGenericType(dependency, providedTypeKeys)) {
                    log.debug("Not resolvable : {}.{} (dependency={})", cafeMember.getMemberDeclaringClass().getSimpleName(), cafeMember.getMember().getName(), dependency);
                    return dependency; // Found an unresolvable dependency
                }
            }
        }
        return null; // All dependencies are resolvable
    }
}