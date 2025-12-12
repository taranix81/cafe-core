package org.taranix.cafe.beans.descriptors;

import lombok.Getter;
import org.taranix.cafe.beans.CafeReflectionUtils;
import org.taranix.cafe.beans.annotations.CafeAnnotationUtils;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.PropertyTypeKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class CafeMethodInfo extends CafeMemberInfo {

    private final Method method;

    CafeMethodInfo(final Method method, CafeClassDescriptor cafeClassDescriptor) {
        super(cafeClassDescriptor);
        this.method = method;
    }

    @Override
    public Member getMember() {
        return getMethod();
    }

    @Override
    public Set<BeanTypeKey> provides() {
        return Set.of(getMethodReturnTypeKey());
    }

    @Override
    public List<BeanTypeKey> dependencies() {
        List<BeanTypeKey> result = new ArrayList<>();

        //If method is non-static then we need instance of declaring class;
        if (!isStatic()) {
            result.add(getOwnerClassTypeKey());
        }

        // Add all parameters
        result.addAll(CafeReflectionUtils.determineMethodParameterTypes(getMethod(), getCafeClassDescriptor().getTypeClass())
                .stream()
                .map(BeanTypeKey::from)
                .toList());
        return result;
    }

    @Override
    public List<PropertyTypeKey> propertyDependencies() {
        return List.of();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof CafeMethodInfo cafeMethodInfo) {
            return getMember().equals(cafeMethodInfo.getMember());
        }
        return false;
    }

    public boolean isPrimary() {
        return CafeAnnotationUtils.isPrimary(method);
    }

    @Override
    public boolean isOptional() {
        return false;
    }

    @Override
    public boolean isTaskable() {
        return CafeAnnotationUtils.hasTaskableMarker(method);
    }

    @Override
    public boolean isInitable() {
        return CafeAnnotationUtils.hasInitableMarker(method);
    }

    public BeanTypeKey getMethodReturnTypeKey() {
        String memberIdentifier = CafeAnnotationUtils.getMemberName(getMethod());
        Type methodReturnType = CafeReflectionUtils.determineMethodReturnType(getMethod(), getCafeClassDescriptor().getTypeClass());
        return BeanTypeKey.from(methodReturnType, memberIdentifier);
    }

    public Collection<Annotation> getMethodAnnotations() {
        return Arrays.stream(method.getAnnotations()).collect(Collectors.toSet());
    }
}
