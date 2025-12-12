package org.taranix.cafe.beans.descriptors;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.taranix.cafe.beans.exceptions.CafeClassDescriptorException;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.TypeKey;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

public class CafeClassDescriptors {

    @Getter
    private final CafeBeansDependencyService dependency;

    private final Set<CafeClassDescriptor> classDescriptors;
    @Getter
    private final Set<Class<? extends Annotation>> annotations;


    private CafeClassDescriptors(final Set<CafeClassDescriptor> classDescriptors, final Set<Class<? extends Annotation>> annotations) {
        this.classDescriptors = classDescriptors;
        this.annotations = annotations;
        dependency = CafeBeansDependencyService.from(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Set<TypeKey> provides() {
        return allMembers().stream()
                .map(CafeMemberInfo::provides)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    public Set<CafeMemberInfo> allMembers() {
        Set<CafeMemberInfo> result = new HashSet<>();
        result.addAll(constructors());
        result.addAll(methods());
        result.addAll(fields());
        return result;
    }

    public Set<CafeConstructorInfo> constructors() {
        return classDescriptors.stream()
                .map(CafeClassDescriptor::constructor)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Set<CafeMethodInfo> methods() {
        return classDescriptors.stream()
                .map(CafeClassDescriptor::methods)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    public Set<CafeFieldInfo> fields() {
        return classDescriptors.stream()
                .map(CafeClassDescriptor::fields)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    public Set<CafeMemberInfo> findProviders(BeanTypeKey typeKey) {
        return allMembers()
                .stream()
                .filter(memberDescriptor -> memberDescriptor.provides().contains(typeKey))
                .collect(Collectors.toSet());
    }

    public Set<CafeMemberInfo> findSingletonProviders(BeanTypeKey typeKey) {
        return findProviders(typeKey).stream()
                .filter(CafeMemberInfo::isSingleton)
                .collect(Collectors.toSet());
    }

    public Set<CafeMemberInfo> findPrototypeProviders(BeanTypeKey typeKey) {
        return findProviders(typeKey).stream()
                .filter(CafeMemberInfo::isPrototype)
                .collect(Collectors.toSet());
    }

    public Set<CafeClassDescriptor> descriptors() {
        return Collections.unmodifiableSet(classDescriptors);
    }

    public CafeClassDescriptor descriptor(Class<?> aClass) {
        return classDescriptors.stream()
                .filter(classDescriptor -> classDescriptor.getTypeClass().equals(aClass))
                .findAny()
                .orElse(null);
    }


    @Override
    public String toString() {
        return StringUtils.join(classDescriptors.toArray(), ",");
    }

    public static class Builder {
        private final Set<Class<?>> classSet = new HashSet<>();
        private final Set<Class<? extends Annotation>> annotations = new HashSet<>();

        public Builder withClass(Class<?> aClass) {
            classSet.add(aClass);
            return this;
        }

        public CafeClassDescriptors build() {
            if (annotations.isEmpty()) {
                throw new CafeClassDescriptorException("No annotation(s) specified");
            }

            Set<CafeClassDescriptor> cafeClassDescriptors = classSet.stream()
                    .map(CafeClassDescriptor::from)
                    .collect(Collectors.toSet());
            return new CafeClassDescriptors(cafeClassDescriptors, annotations);
        }

        public Builder withAnnotations(final Set<Class<? extends Annotation>> annotations) {
            this.annotations.addAll(annotations);
            return this;
        }

        public Builder withClasses(final Set<Class<?>> classes) {
            classSet.addAll(classes);
            return this;
        }
    }
}
