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
public class CafeMetadataRegistry {

    @Getter
    private final MemberDependencyResolverRegistry memberDependencyRegistry;

    @Getter
    private final ClassDependencyRegistry classDependencyRegistry;

    @Getter
    private final Set<CafeClass> cafeClassMetadata;

    private final Map<Class<?>, CafeClass> classMetadataMap;

    private CafeMetadataRegistry(
            Set<CafeClass> cafeClassMetadata,
            MemberDependencyResolverRegistry memberDependencyRegistry,
            ClassDependencyRegistry classDependencyRegistry) {

        this.cafeClassMetadata = Set.copyOf(cafeClassMetadata);
        this.memberDependencyRegistry = memberDependencyRegistry;
        this.classDependencyRegistry = classDependencyRegistry;

        this.classMetadataMap = this.cafeClassMetadata.stream()
                .collect(Collectors.toUnmodifiableMap(CafeClass::getRootClass, Function.identity()));
    }

    public static Builder builder() {
        return new Builder();
    }

    public Set<CafeMember> allMembers() {
        return cafeClassMetadata.stream()
                .flatMap(cd -> cd.getMembers().stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<CafeMember> findAnyTypeProviders(BeanTypeKey typeKey) {
        return allMembers().stream()
                .filter(member -> member.getProvidedTypeKeys().contains(typeKey))
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<CafeMember> findSingletonProviders(BeanTypeKey typeKey) {
        return findAnyTypeProviders(typeKey).stream()
                .filter(CafeMember::isSingleton)
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<CafeMember> findPrototypeProviders(BeanTypeKey typeKey) {
        return findAnyTypeProviders(typeKey).stream()
                .filter(CafeMember::isPrototype)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns the metadata for the given class, or null if not found.
     * Changed name from findClassInfo to findClassMetadata for consistency.
     */
    public CafeClass getClassMetadata(Class<?> aClass) {
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

        public CafeMetadataRegistry build() {
            // 1. Create Metadata objects
            Set<CafeClass> cafeClassSet = classSet.stream()
                    .map(CafeClassFactory::create)
                    .collect(Collectors.toSet());

            // 2. Build Dependency Graphs (Logic moved here from Registry constructor)
            MemberDependencyResolverRegistry memberRegistry = MemberDependencyResolverRegistry.from(cafeClassSet);
            ClassDependencyRegistry classRegistry = ClassDependencyRegistry.from(cafeClassSet);

            return new CafeMetadataRegistry(cafeClassSet, memberRegistry, classRegistry);
        }
    }
}