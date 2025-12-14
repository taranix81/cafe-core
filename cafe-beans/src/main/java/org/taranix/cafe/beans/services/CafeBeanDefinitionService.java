package org.taranix.cafe.beans.services;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.taranix.cafe.beans.metadata.CafeClassInfo;
import org.taranix.cafe.beans.metadata.members.CafeMemberInfo;
import org.taranix.cafe.beans.repositories.registry.ClassDependencyResolverRegistry;
import org.taranix.cafe.beans.repositories.registry.MemberDependencyResolverRegistry;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Aggregates multiple CafeClassDescriptor instances and provides
 * utility methods for querying bean providers and dependencies.
 */
public class CafeBeanDefinitionService {

    @Getter
    private final MemberDependencyResolverRegistry memberDependencyResolverRegistry;
    @Getter
    private final ClassDependencyResolverRegistry classDependencyResolverRegistry;
    @Getter
    private final Set<CafeClassInfo> cafeClassInfos;

    /**
     * -- GETTER --
     * Returns the dependency service for these descriptors.
     */

    private CafeBeanDefinitionService(final Set<CafeClassInfo> cafeClassInfos) {
        this.cafeClassInfos = Set.copyOf(cafeClassInfos);
        memberDependencyResolverRegistry = MemberDependencyResolverRegistry.from(cafeClassInfos);
        classDependencyResolverRegistry = ClassDependencyResolverRegistry.from(cafeClassInfos);
    }

    public static Builder builder() {
        return new Builder();
    }

//    /**
//     * Returns all provided TypeKeys from all members.
//     */
//    public Set<TypeKey> provides() {
//        return allMembers().stream()
//                .map(CafeMemberInfo::provides)
//                .flatMap(Collection::stream)
//                .collect(Collectors.toUnmodifiableSet());
//    }

    /**
     * Returns all member descriptors (constructors, methods, fields).
     */
    public Set<CafeMemberInfo> allMembers() {
        return cafeClassInfos.stream()
                .flatMap(cd -> cd.getMembers().stream())
                .collect(Collectors.toUnmodifiableSet());
    }

//    /**
//     * Returns all method descriptors.
//     */
//    public Set<CafeMethodInfo> methods() {
//        return cafeClassInfos.stream()
//                .map(CafeClassInfo::methods)
//                .flatMap(Collection::stream)
//                .collect(Collectors.toUnmodifiableSet());
//    }

//    /**
//     * Returns all field descriptors.
//     */
//    public Set<CafeFieldInfo> fields() {
//        return cafeClassInfos.stream()
//                .map(CafeClassInfo::fields)
//                .flatMap(Collection::stream)
//                .collect(Collectors.toUnmodifiableSet());
//    }

    /**
     * Finds all members that provide the given BeanTypeKey.
     */
    public Set<CafeMemberInfo> findAnyTypeProviders(BeanTypeKey typeKey) {
        return allMembers().stream()
                .filter(member -> member.provides().contains(typeKey))
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Finds all singleton members that provide the given BeanTypeKey.
     */
    public Set<CafeMemberInfo> findSingletonProviders(BeanTypeKey typeKey) {
        return findAnyTypeProviders(typeKey).stream()
                .filter(CafeMemberInfo::isSingleton)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Finds all prototype members that provide the given BeanTypeKey.
     */
    public Set<CafeMemberInfo> findPrototypeProviders(BeanTypeKey typeKey) {
        return findAnyTypeProviders(typeKey).stream()
                .filter(CafeMemberInfo::isPrototype)
                .collect(Collectors.toUnmodifiableSet());
    }

//    /**
//     * Returns all class descriptors.
//     */
//    public Set<CafeClassInfo> allCafeClassInfos() {
//        return cafeClassInfos;
//    }

    /**
     * Returns the descriptor for the given class, or null if not found.
     */
    public CafeClassInfo findClassInfo(Class<?> aClass) {
        return cafeClassInfos.stream()
                .filter(cd -> cd.getTypeClass().equals(aClass))
                .findFirst()
                .orElse(null);
    }


    /*
     * Function check if there is a cycle among MemberDependency set
     *
     * @return true, if cycle exist, otherwise false
     */
    public boolean hasCycleBetweenClassMembers() {
        return !membersCycleSet().isEmpty();
    }

    Collection<CafeMemberInfo> membersCycleSet() {
        return memberDependencyResolverRegistry.cycleSet();
    }

    public boolean hasCycleBetweenClasses() {
        return !classCycleSet().isEmpty();
    }

    Collection<CafeClassInfo> classCycleSet() {
        return classDependencyResolverRegistry.cycleSet();
    }

    @Override
    public String toString() {
        return StringUtils.join(cafeClassInfos, ",");
    }

    /**
     * Builder for CafeClassDescriptors.
     */
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

        public CafeBeanDefinitionService build() {
            Set<CafeClassInfo> descriptors = classSet.stream()
                    .map(CafeClassInfo::from)
                    .collect(Collectors.toSet());
            return new CafeBeanDefinitionService(descriptors);
        }
    }
}