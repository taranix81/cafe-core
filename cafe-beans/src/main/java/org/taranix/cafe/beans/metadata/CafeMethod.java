package org.taranix.cafe.beans.metadata;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.base.CafeWirerType;
import org.taranix.cafe.beans.reflection.CafeAnnotationUtils;
import org.taranix.cafe.beans.reflection.CafeReflectionUtils;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.PropertyTypeKey;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Descriptor for a class method. This primarily handles factory methods
 * (which provide a bean) and task/event handler methods (which have dependencies).
 */
@Getter
public class CafeMethod extends CafeMember {

    private final Method method;

    /**
     * Constructs a descriptor for the given method.
     */
    CafeMethod(final Method method, CafeClass cafeClass) {
        super(cafeClass);
        this.method = method;
    }

    @Override
    public Member getMember() {
        return getMethod();
    }

    /**
     * Returns the set of BeanTypeKeys provided by this method.
     * For factory methods, this is the method's return type.
     */
    @Override
    public Set<BeanTypeKey> getProvidedTypeKeys() {
        // A method can only provide one bean (its return value)
        if (!CafeAnnotationUtils.hasAnnotationMarker(getMethod(), CafeWirerType.class)) {
            return Set.of();
        }
        return Set.of(getMethodReturnTypeKey());
    }

    /**
     * Returns the dependencies required to invoke this method. These include:
     * <ul>
     * <li>Dependency on the owner class instance (if the method is non-static).</li>
     * <li>Dependencies on beans matching all method parameters.</li>
     * </ul>
     */
    @Override
    public List<BeanTypeKey> getRequiredTypeKeys() {
        if (!CafeAnnotationUtils.hasAnnotationMarker(getMethod(), CafeWirerType.class)) {
            return List.of();
        }

        List<BeanTypeKey> result = new ArrayList<>();

        // Dependency on owner class instance for non-static methods
        if (!isStatic()) {
            result.add(getParent().getRootClassTypeKey());
        }

        // Add all parameter types as bean dependencies

        result.addAll(Arrays.stream(getMethodParameterTypeKeys()).toList());
        return result;
    }

    /**
     * Methods typically do not have direct property dependencies defined
     * on the method itself.
     */
    @Override
    public List<PropertyTypeKey> getRequiredPropertyTypeKeys() {
        return List.of();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        // Uses pattern matching for instanceof (Java 16+)
        if (obj instanceof CafeMethod cafeMethodInfo) {
            return getMember().equals(cafeMethodInfo.getMember());
        }
        return false;
    }

    /**
     * Retrieves the BeanTypeKey representing the return type and optional identifier of the method.
     */
    public BeanTypeKey getMethodReturnTypeKey() {
        String memberIdentifier = CafeAnnotationUtils.getMemberName(getMethod());
        // Resolves the actual generic return type if necessary
        Type methodReturnType = CafeReflectionUtils.determineMethodReturnType(getMethod(), getParent().getRootClass());
        return BeanTypeKey.from(methodReturnType, memberIdentifier);
    }

    public BeanTypeKey[] getMethodParameterTypeKeys() {
        return Arrays.stream(CafeReflectionUtils.determineMethodParameterTypes(getMethod(), getParentRootClass()))
                .map(BeanTypeKey::from)
                .toArray(BeanTypeKey[]::new);
    }

}