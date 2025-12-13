package org.taranix.cafe.beans.descriptors;

import org.apache.commons.lang3.StringUtils;
import org.taranix.cafe.beans.descriptors.members.CafeConstructorInfo;
import org.taranix.cafe.beans.descriptors.members.CafeFieldInfo;
import org.taranix.cafe.beans.descriptors.members.CafeMemberInfo;
import org.taranix.cafe.beans.descriptors.members.CafeMethodInfo;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.TypeKey;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Aggregates multiple CafeClassDescriptor instances and provides
 * utility methods for querying bean providers and dependencies.
 */
public class CafeClassDescriptors {

    private final Set<CafeClassInfo> classDescriptors;
    private final CafeBeansDependencyService dependency;

    private CafeClassDescriptors(final Set<CafeClassInfo> classDescriptors) {
        this.classDescriptors = Collections.unmodifiableSet(new HashSet<>(classDescriptors));
        this.dependency = CafeBeansDependencyService.from(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns all provided TypeKeys from all members.
     */
    public Set<TypeKey> provides() {
        return allMembers().stream()
                .map(CafeMemberInfo::provides)
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns all member descriptors (constructors, methods, fields).
     */
    public Set<CafeMemberInfo> allMembers() {
        return classDescriptors.stream()
                .flatMap(cd -> cd.getMembers().stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns all constructor descriptors.
     */
    public Set<CafeConstructorInfo> constructors() {
        return classDescriptors.stream()
                .map(CafeClassInfo::constructor)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns all method descriptors.
     */
    public Set<CafeMethodInfo> methods() {
        return classDescriptors.stream()
                .map(CafeClassInfo::methods)
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns all field descriptors.
     */
    public Set<CafeFieldInfo> fields() {
        return classDescriptors.stream()
                .map(CafeClassInfo::fields)
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Finds all members that provide the given BeanTypeKey.
     */
    public Set<CafeMemberInfo> findProviders(BeanTypeKey typeKey) {
        return allMembers().stream()
                .filter(member -> member.provides().contains(typeKey))
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Finds all singleton members that provide the given BeanTypeKey.
     */
    public Set<CafeMemberInfo> findSingletonProviders(BeanTypeKey typeKey) {
        return findProviders(typeKey).stream()
                .filter(CafeMemberInfo::isSingleton)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Finds all prototype members that provide the given BeanTypeKey.
     */
    public Set<CafeMemberInfo> findPrototypeProviders(BeanTypeKey typeKey) {
        return findProviders(typeKey).stream()
                .filter(CafeMemberInfo::isPrototype)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns all class descriptors.
     */
    public Set<CafeClassInfo> descriptors() {
        return classDescriptors;
    }

    /**
     * Returns the descriptor for the given class, or null if not found.
     */
    public CafeClassInfo descriptor(Class<?> aClass) {
        return classDescriptors.stream()
                .filter(cd -> cd.getTypeClass().equals(aClass))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the dependency service for these descriptors.
     */
    public CafeBeansDependencyService getDependency() {
        return dependency;
    }

    @Override
    public String toString() {
        return StringUtils.join(classDescriptors, ",");
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

        public CafeClassDescriptors build() {
            Set<CafeClassInfo> descriptors = classSet.stream()
                    .map(CafeClassInfo::from)
                    .collect(Collectors.toSet());
            return new CafeClassDescriptors(descriptors);
        }
    }
}