package org.taranix.cafe.beans.descriptors.members;

import lombok.Getter;
import org.taranix.cafe.beans.CafeReflectionUtils;
import org.taranix.cafe.beans.descriptors.CafeClassInfo;
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

/**
 * Descriptor for a class constructor. This handles the dependency analysis
 * for the class instance itself (which is provided by the constructor)
 * and the dependencies needed for invocation (constructor parameters).
 */
@Getter
public class CafeConstructorInfo extends CafeMemberInfo {

    private final Constructor<?> constructor;

    /**
     * Constructs a descriptor for the given constructor.
     */
    CafeConstructorInfo(final Constructor<?> constructor, CafeClassInfo cafeClassInfo) {
        super(cafeClassInfo);
        this.constructor = constructor;
    }

    /**
     * Returns the annotations directly present on the constructor element.
     */
    public Set<Annotation> getConstructorAnnotations() {
        return Arrays.stream(constructor.getAnnotations()).collect(Collectors.toSet());
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
    public Set<BeanTypeKey> provides() {
        Set<BeanTypeKey> result = new HashSet<>();
        Class<?> declaringClass = getConstructor().getDeclaringClass();

        // 1. The class itself
        result.add(BeanTypeKey.from(declaringClass));

        // 2. All superclasses
        result.addAll(CafeReflectionUtils.getAllSuperClasses(declaringClass)
                .stream()
                .map(BeanTypeKey::from)
                .collect(Collectors.toSet()));

        // 3. All interfaces
        result.addAll(CafeReflectionUtils.getAllInterfaces(declaringClass)
                .stream()
                .map(BeanTypeKey::from)
                .collect(Collectors.toSet()));
        return result;
    }

    /**
     * Returns the dependencies required to execute this constructor.
     * These are derived from the constructor's parameter types.
     */
    @Override
    public List<BeanTypeKey> dependencies() {
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
    public List<PropertyTypeKey> propertyDependencies() {
        return List.of();
    }


}