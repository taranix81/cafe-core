package org.taranix.cafe.beans.repositories.registry;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.metadata.CafeClassMetadata;
import org.taranix.cafe.beans.metadata.CafeMemberMetadata;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.TypeKey;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class MemberDependencyResolverRegistry extends AbstractDependencyRegistry<CafeMemberMetadata> {


    /**
     * Factory method to build the dependency registry from a set of class definitions.
     * It iterates over all members and finds the providers for each dependency required by a member.
     *
     * @param cafeClassMetadata A set of all defined classes and their metadata.
     * @return A fully populated MemberDependencyResolverRegistry instance.
     */
    public static MemberDependencyResolverRegistry from(Set<CafeClassMetadata> cafeClassMetadata) {
        MemberDependencyResolverRegistry result = new MemberDependencyResolverRegistry();
        Set<CafeMemberMetadata> allMembers = allMembers(cafeClassMetadata);

        // Iterate through every member that might require dependencies
        for (CafeMemberMetadata analyzingMember : allMembers) {
            // Iterate through every type key required by the analyzing member
            for (BeanTypeKey requiredTypeKey : analyzingMember.getRequiredTypes()) {

                // Find all members that can provide the currently required type key
                Set<CafeMemberMetadata> matchedProviders = getProviders(allMembers, analyzingMember, requiredTypeKey);

                // Register the dependency relationship (analyzingMember requires provider)
                matchedProviders.forEach(provider ->
                        result.set(analyzingMember, provider) // 'set' likely means: analyzingMember -> provider
                );
            }
        }
        return result;
    }

    /**
     * Filters the set of all members to find those that can satisfy the given dependency requirement.
     *
     * @param allMembers      A set of all available members in the system.
     * @param targetMember    The member currently requiring the dependency.
     * @param requiredTypeKey The specific type key required by the target member.
     * @return A Set of CafeMemberInfo that are valid providers for the required type.
     */
    private static Set<CafeMemberMetadata> getProviders(final Set<CafeMemberMetadata> allMembers,
                                                        final CafeMemberMetadata targetMember,
                                                        final BeanTypeKey requiredTypeKey) {
        return allMembers.stream()
                // 1. Exclude members that don't provide any type (e.g., simple fields without @Cafe annotations)
                .filter(providerMember -> !providerMember.getProvidedTypes().isEmpty())
                // 2. Exclude the member itself to prevent self-referencing dependencies
                .filter(providerMember -> !providerMember.equals(targetMember))
                // 3. Exclude constructors as providers if they belong to the same class as the target member.
                // This prevents circular self-injection within a single class's construction.
                .filter(providerMember -> !(targetMember.isConstructor() && targetMember.isBelongToTheSameClass(providerMember)))
                // 4. Filter for providers that actually match the required type key (including generics)
                .filter(providerMember -> BeanTypeKey.isMatchByTypeOrGenericType(requiredTypeKey, providerMember.getProvidedTypes()))
                .collect(Collectors.toSet());
    }

    /**
     * Flattens the set of CafeClassInfo objects to produce a single set containing all CafeMemberInfo objects.
     *
     * @param cafeClassMetadata A set of class metadata objects.
     * @return A set of all unique members (fields, methods, constructors) defined in the classes.
     */
    private static Set<CafeMemberMetadata> allMembers(Set<CafeClassMetadata> cafeClassMetadata) {
        return cafeClassMetadata.stream()
                .flatMap(cd -> cd.getMembers().stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Retrieves providers for the target member that specifically provide the given TypeKey.
     *
     * @param target  The member requiring the dependency.
     * @param typeKey The specific type key to filter providers by.
     * @return A filtered set of members providing the exact type.
     */
    public Set<CafeMemberMetadata> providers(CafeMemberMetadata target, TypeKey typeKey) {
        // First get all registered providers for the target, then filter them by the specific TypeKey
        return providers(target).stream()
                .filter(memberDescriptor -> memberDescriptor.getProvidedTypes().contains(typeKey))
                .collect(Collectors.toSet());
    }

    /**
     * Retrieves all members that satisfy any dependency requirement of the target member.
     * This set is built during the 'from' factory method execution.
     *
     * @param target The member requiring the dependencies.
     * @return A Set of all known providers for the target member.
     */
    public Set<CafeMemberMetadata> providers(CafeMemberMetadata target) {
        // 'getMany' is assumed to be inherited from AbstractDependencyRegistry,
        // retrieving the set of dependencies (providers) associated with the target key.
        return new HashSet<>(getMany(target));
    }

    /**
     * Retrieves a unique set of all BeanTypeKeys provided by all registered providers of the target member.
     *
     * @param targetMember The member whose providers' types are being queried.
     * @return A Set of all BeanTypeKeys provided by the target member's providers.
     */
    public Set<BeanTypeKey> providersTypeKeys(CafeMemberMetadata targetMember) {
        return providers(targetMember).stream()
                // Map each provider member to its set of provided types
                .map(CafeMemberMetadata::getProvidedTypes)
                // Flatten the stream of sets into a single stream of types
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
}