package org.taranix.cafe.beans.descriptors;

import lombok.Getter;
import org.taranix.cafe.beans.CafeReflectionUtils;
import org.taranix.cafe.beans.annotations.CafeAnnotationUtils;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.PropertyTypeKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class CafeConstructorInfo extends CafeMemberInfo {

    private final Constructor<?> constructor;

    CafeConstructorInfo(final Constructor<?> constructor, CafeClassDescriptor cafeClassDescriptor) {
        super(cafeClassDescriptor);
        this.constructor = constructor;
    }

    public Set<Annotation> getConstructorAnnotations() {
        return Arrays.stream(constructor.getAnnotations()).collect(Collectors.toSet());
    }


    @Override
    public Member getMember() {
        return getConstructor();
    }

    @Override
    public Set<BeanTypeKey> provides() {
        Set<BeanTypeKey> result = new HashSet<>();
        result.add(BeanTypeKey.from(getConstructor().getDeclaringClass()));
        //Add superclass
        result.addAll(CafeReflectionUtils.getAllSuperClasses(getConstructor().getDeclaringClass())
                .stream()
                .map(BeanTypeKey::from)
                .collect(Collectors.toSet()));

        //Add interfaces
        result.addAll(CafeReflectionUtils.getAllInterfaces(getConstructor().getDeclaringClass())
                .stream()
                .map(BeanTypeKey::from)
                .collect(Collectors.toSet()));
        return result;
    }

    @Override
    public List<BeanTypeKey> dependencies() {
        List<Type> parameterTypes = CafeReflectionUtils.determineConstructorParameterTypes(getConstructor());
        return parameterTypes.stream()
                .map(BeanTypeKey::from)
                .toList();
    }

    @Override
    public List<PropertyTypeKey> propertyDependencies() {
        return List.of();
    }

    public boolean isPrimary() {
        return CafeAnnotationUtils.isPrimary(getCafeClassDescriptor().getTypeClass());
    }

    @Override
    public boolean isOptional() {
        return false;
    }

    @Override
    public boolean isTaskable() {
        return false;
    }

    @Override
    public boolean isInitable() {
        return true;
    }
}
