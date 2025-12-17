package org.taranix.cafe.beans.metadata;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.taranix.cafe.beans.repositories.registry.ClassDependencyRegistry;
import org.taranix.cafe.beans.repositories.registry.MemberDependencyResolverRegistry;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Aggregates multiple CafeClassMetadata instances.
 * Acts as a read-only repository for static metadata.
 */
public class CafeBeansRegistry {

    @Getter
    private final MemberDependencyResolverRegistry memberDependencyRegistry;

    @Getter
    private final ClassDependencyRegistry classDependencyRegistry;

    @Getter
    private final Set<CafeClassMetadata> cafeClassMetadata;

    private final Map<Class<?>, CafeClassMetadata> classMetadataMap;

    private CafeBeansRegistry(
            Set<CafeClassMetadata> cafeClassMetadata,
            MemberDependencyResolverRegistry memberDependencyRegistry,
            ClassDependencyRegistry classDependencyRegistry) {

        this.cafeClassMetadata = Set.copyOf(cafeClassMetadata);
        this.memberDependencyRegistry = memberDependencyRegistry;
        this.classDependencyRegistry = classDependencyRegistry;

        this.classMetadataMap = this.cafeClassMetadata.stream()
                .collect(Collectors.toUnmodifiableMap(CafeClassMetadata::getRootClass, Function.identity()));
    }

    public static Builder builder() {
        return new Builder();
    }

    public Set<CafeMemberMetadata> allMembers() {
        return cafeClassMetadata.stream()
                .flatMap(cd -> cd.getMembers().stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<CafeMemberMetadata> findAnyTypeProviders(BeanTypeKey typeKey) {
        return allMembers().stream()
                .filter(member -> member.getProvidedTypes().contains(typeKey))
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<CafeMemberMetadata> findSingletonProviders(BeanTypeKey typeKey) {
        return findAnyTypeProviders(typeKey).stream()
                .filter(CafeMemberMetadata::isSingleton)
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<CafeMemberMetadata> findPrototypeProviders(BeanTypeKey typeKey) {
        return findAnyTypeProviders(typeKey).stream()
                .filter(CafeMemberMetadata::isPrototype)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns the metadata for the given class, or null if not found.
     * Changed name from findClassInfo to findClassMetadata for consistency.
     */
    public CafeClassMetadata findClassMetadata(Class<?> aClass) {
        return classMetadataMap.get(aClass);
    }

    @Override
    public String toString() {
        return StringUtils.join(cafeClassMetadata, ",");
    }

    // --- Builder ---

    public static class Builder {
        private final Set<Class<?>> classSet = new HashSet<>();

        public Builder withClass(Class<?> aClass) {
            classSet.add(aClass);
            return this;
        }

        public Builder withClasses(Set<Class<?>> classes) {
            classSet.addAll(classes);
            return this;
        }

        public CafeBeansRegistry build() {
            // 1. Create Metadata objects
            Set<CafeClassMetadata> descriptors = classSet.stream()
                    .map(CafeClassMetadataFactory::create)
                    .collect(Collectors.toSet());

            // 2. Build Dependency Graphs (Logic moved here from Registry constructor)
            MemberDependencyResolverRegistry memberRegistry = MemberDependencyResolverRegistry.from(descriptors);
            ClassDependencyRegistry classRegistry = ClassDependencyRegistry.from(descriptors);

            return new CafeBeansRegistry(descriptors, memberRegistry, classRegistry);
        }
    }
}