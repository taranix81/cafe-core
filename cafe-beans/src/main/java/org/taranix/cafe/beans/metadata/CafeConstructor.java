package org.taranix.cafe.beans.metadata;

import lombok.Getter;
import org.taranix.cafe.beans.reflection.CafeReflectionUtils;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.PropertyTypeKey;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Descriptor for a class constructor. This handles the dependency analysis
 * for the class instance itself (which is provided by the constructor)
 * and the dependencies needed for invocation (constructor parameters).
 */
@Getter
public class CafeConstructor extends CafeMember {

    private final Constructor<?> constructor;

    /**
     * Constructs a descriptor for the given constructor.
     */
    CafeConstructor(final Constructor<?> constructor, CafeClass cafeClass) {
        super(cafeClass);
        this.constructor = constructor;
    }

    @Override
    public Member getMember() {
        return getConstructor();
    }

    /**
     * Returns the set of BeanTypeKeys provided by this constructor, which includes:
     * <ul>
     * <li>The declaring class itself.</li>
     * <li>All superclasses it extends.</li>
     * <li>All interfaces it implements.</li>
     * </ul>
     */
    @Override
    public Set<BeanTypeKey> getProvidedTypeKeys() {
        Set<BeanTypeKey> result = new HashSet<>();
        Class<?> declaringClass = getConstructor().getDeclaringClass();

        // 1. The class itself
        result.add(BeanTypeKey.from(getParentRootClass()));
        result.addAll(CafeReflectionUtils.getAllSuperTypes(declaringClass).stream()
                .map(BeanTypeKey::from)
                .collect(Collectors.toSet()));
        return result;
    }

    /**
     * Returns the dependencies required to execute this constructor.
     * These are derived from the constructor's parameter types.
     */
    @Override
    public List<BeanTypeKey> getRequiredTypeKeys() {
        List<Type> parameterTypes = CafeReflectionUtils.determineConstructorParameterTypes(getConstructor());
        return parameterTypes.stream()
                .map(BeanTypeKey::from)
                .toList();
    }

    /**
     * Constructors typically do not have property dependencies directly defined
     * on them in this framework design.
     */
    @Override
    public List<PropertyTypeKey> getRequiredPropertyTypeKeys() {
        return List.of();
    }


}