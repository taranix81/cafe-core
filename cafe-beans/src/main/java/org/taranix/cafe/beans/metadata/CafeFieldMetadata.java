package org.taranix.cafe.beans.metadata;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.annotations.CafeProperty;
import org.taranix.cafe.beans.reflection.CafeAnnotationUtils;
import org.taranix.cafe.beans.reflection.CafeReflectionUtils;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.PropertyTypeKey;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Descriptor for a class field. This handles the dependency analysis
 * for field injection (e.g., @CafeInject) and property injection (e.g., @CafeProperty).
 */
@Getter
public class CafeFieldMetadata extends CafeMemberMetadata {

    private final Field field;

    /**
     * Constructs a descriptor for the given field.
     */
    CafeFieldMetadata(final Field field, CafeClassMetadata cafeClassMetadata) {
        super(cafeClassMetadata);
        this.field = field;
    }

    @Override
    public Member getMember() {
        return getField();
    }

    /**
     * Fields, typically, do not provide new beans to the container.
     */
    @Override
    public Set<BeanTypeKey> getProvidedTypes() {
        return Set.of();
    }

    /**
     * Returns the dependencies required for this field. These include:
     * <ul>
     * <li>Dependency on the owner class instance (if the field is non-static).</li>
     * <li>Dependency on a bean matching the field's type/identifier (if annotated with @CafeInject).</li>
     * </ul>
     */
    @Override
    public List<BeanTypeKey> getRequiredTypes() {
        List<BeanTypeKey> result = new ArrayList<>();

        // Dependency on owner class instance for non-static fields
        if (!isStatic()) {
            result.add(getParentTypeKey());
        }

        // Dependency on a bean for @CafeInject
        if (hasAnnotation(CafeInject.class)) {
            result.add(getFieldTypeKey());
        }

        return result;
    }

    /**
     * Returns property dependencies if the field is annotated with @CafeProperty.
     */
    @Override
    public List<PropertyTypeKey> getRequiredProperties() {
        if (hasAnnotation(CafeProperty.class)) {
            String propertyName = getAnnotation(CafeProperty.class).name();
            return List.of(PropertyTypeKey.from(propertyName));
        }
        return List.of();
    }

    /**
     * Retrieves the BeanTypeKey representing the type and optional identifier of the field.
     */
    public BeanTypeKey getFieldTypeKey() {
        String memberIdentifier = CafeAnnotationUtils.getMemberName(getField());
        // Resolves the actual generic type if necessary
        Type fieldType = CafeReflectionUtils.determineFieldType(getField(), getParent().getRootClass());
        return BeanTypeKey.from(fieldType, memberIdentifier);
    }
}