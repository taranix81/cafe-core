package org.taranix.cafe.beans.descriptors;

import lombok.Getter;
import org.taranix.cafe.beans.CafeReflectionUtils;
import org.taranix.cafe.beans.annotations.CafeAnnotationUtils;
import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.annotations.CafeProperty;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.PropertyTypeKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class CafeFieldInfo extends CafeMemberInfo {

    private final Field field;


    CafeFieldInfo(final Field field, CafeClassDescriptor cafeClassDescriptor) {
        super(cafeClassDescriptor);
        this.field = field;
    }

    @Override
    public Member getMember() {
        return getField();
    }

    @Override
    public Set<BeanTypeKey> provides() {
        return Set.of();
    }

    @Override
    public List<BeanTypeKey> dependencies() {
        List<BeanTypeKey> result = new ArrayList<>();
        //If field is non-static then we need instance of declaring class;
        if (!isStatic()) {
            //Dependency to owner class
            result.add(getOwnerClassTypeKey());
        }

        if (hasAnnotation(CafeInject.class)) {
            result.add(getFieldTypeKey());
        }

        return result;
    }

    @Override
    public List<PropertyTypeKey> propertyDependencies() {
        if (hasAnnotation(CafeProperty.class)) {
            String propertyName = getFieldAnnotation(CafeProperty.class).name();
            return List.of(PropertyTypeKey.from(propertyName));
        }
        return List.of();
    }

    @Override
    public boolean isPrimary() {
        return false;
    }

    @Override
    public boolean isOptional() {
        return CafeAnnotationUtils.isOptional(getField());
    }

    @Override
    public boolean isTaskable() {
        return false;
    }

    @Override
    public boolean isInitable() {
        return CafeAnnotationUtils.hasInitableMarker(field);
    }

    public <T extends Annotation> T getFieldAnnotation(Class<T> annotationType) {
        return getField().getAnnotation(annotationType);
    }

    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        return getField().getAnnotation(annotationType) != null;
    }

    public BeanTypeKey getFieldTypeKey() {
        String memberIdentifier = CafeAnnotationUtils.getMemberName(getField());
        Type fieldType = CafeReflectionUtils.determineFieldType(getField(), getCafeClassDescriptor().getTypeClass());
        return BeanTypeKey.from(fieldType, memberIdentifier);
    }

    public Collection<Annotation> getFieldAnnotations() {
        return Arrays.stream(field.getAnnotations()).collect(Collectors.toSet());
    }
}
