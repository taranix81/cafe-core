package org.taranix.cafe.beans.reflection;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.taranix.cafe.beans.exceptions.ReflectionUtilsException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.*;

@Slf4j
public class CafeTypesUtils {

    /**
     * Returns a set of all supertypes (classes and interfaces) for a given class,
     * considering their full generic signatures.
     *
     * @param theClass The starting class to be analyzed.
     * @return A unique set of supertypes (interfaces and classes) as Type objects.
     */
    public static Set<Type> getAllSuperTypes(Class<?> theClass) {
        if (theClass == null) {
            return Collections.emptySet();
        }

        Set<Type> result = new LinkedHashSet<>();
        Queue<Type> queue = new ArrayDeque<>();

        if (theClass.getGenericSuperclass() != null) {
            queue.add(replaceTypeArguments(theClass.getGenericSuperclass(), Collections.emptyMap()));
        }
        for (Type iface : theClass.getGenericInterfaces()) {
            queue.add(replaceTypeArguments(iface, Collections.emptyMap()));
        }

        while (!queue.isEmpty()) {
            Type currentType = queue.poll();
            if (currentType == null || currentType.equals(Object.class) || !result.add(currentType)) {
                continue;
            }

            Class<?> rawClass = TypeUtils.getRawType(currentType, null);
            if (rawClass != null && !rawClass.equals(Object.class)) {
                Map<TypeVariable<?>, Type> currentMappings = TypeUtils.getTypeArguments(currentType, rawClass);

                Type superclass = rawClass.getGenericSuperclass();
                if (superclass != null && !superclass.equals(Object.class)) {
                    queue.add(replaceTypeArguments(superclass, currentMappings));
                }

                for (Type iface : rawClass.getGenericInterfaces()) {
                    queue.add(replaceTypeArguments(iface, currentMappings));
                }
            }
        }

        return Collections.unmodifiableSet(result);
    }

    /**
     * Universally resolves a generic type to its most concrete form within a given context.
     *
     * @param contextClass  The class providing the context for type variables (e.g., UserServiceImpl).
     * @param typeToResolve The type to be resolved (can be Class, ParameterizedType, or TypeVariable).
     * @return The resolved Type (either a Class or a ParameterizedType with concrete arguments).
     */
    public static Type resolve(Class<?> contextClass, Type typeToResolve) {
        if (contextClass == null || typeToResolve == null) {
            return typeToResolve;
        }

        if (typeToResolve instanceof Class<?>) {
            return typeToResolve;
        }

        // Identify the class that owns the type variables within the typeToResolve
        Class<?> declaringClass = findDeclaringClass(typeToResolve);

        // Get mappings from the context class to the declaring class
        Map<TypeVariable<?>, Type> mappings = TypeUtils.getTypeArguments(contextClass, declaringClass);

        // Apply mappings recursively
        return replaceTypeArguments(typeToResolve, mappings);
    }

    public static boolean isTypeCompatible(Type declaredType, Type providedType) {
        log.debug("Checking compatibility: {} vs. {}", declaredType, providedType);
        if (providedType instanceof Class<?> providedClass) {
            if (declaredType instanceof Class<?> declaredClass) {
                return declaredClass.isAssignableFrom(providedClass);
            }

            if (declaredType instanceof ParameterizedType declaredParameterizedType) {
                Set<Type> allSuperTypes = getAllSuperTypes(providedClass); //
                return allSuperTypes.stream()
                        .anyMatch(superType -> TypeUtils.equals(superType, declaredParameterizedType));
            }
        }

        if (providedType instanceof ParameterizedType providedParameterized) {
            if (declaredType instanceof ParameterizedType declaredParameterized) {
                Class<?> declaredRaw = (Class<?>) declaredParameterized.getRawType();
                Class<?> providedRaw = (Class<?>) providedParameterized.getRawType();

                if (!declaredRaw.isAssignableFrom(providedRaw)) {
                    return false;
                }

                Type[] declaredTypeArguments = declaredParameterized.getActualTypeArguments();
                Type[] providedTypeArguments = providedParameterized.getActualTypeArguments();

                if (declaredTypeArguments.length != providedTypeArguments.length) {
                    return false;
                }

                for (int i = 0; i < declaredTypeArguments.length; i++) {
                    if (!isTypeCompatible(declaredTypeArguments[i], providedTypeArguments[i])) {
                        return false;
                    }
                }
                return true;
            }
        }

        if (declaredType instanceof WildcardType declaredWildcard) {
            return Arrays.stream(declaredWildcard.getUpperBounds())
                    .allMatch(bound -> isTypeCompatible(bound, providedType)) &&
                    Arrays.stream(declaredWildcard.getLowerBounds())
                            .allMatch(bound -> isTypeCompatible(providedType, bound));
        }

        return TypeUtils.equals(declaredType, providedType);
    }


    private static Class<?> findDeclaringClass(Type type) {
        if (type instanceof TypeVariable<?> tv) {
            if (tv.getGenericDeclaration() instanceof Class<?> clazz) {
                return clazz;
            }
        } else if (type instanceof ParameterizedType pt) {
            Class<?> bestCandidate = null;
            for (Type arg : pt.getActualTypeArguments()) {
                Class<?> currentCandidate = findDeclaringClass(arg);

                if (currentCandidate != null) {
                    if (bestCandidate == null || bestCandidate.isAssignableFrom(currentCandidate)) {
                        bestCandidate = currentCandidate;
                    }
                }
            }
            return bestCandidate != null ? bestCandidate : (Class<?>) pt.getRawType();
        }
        return null;
    }


    /**
     * Replaces TypeVariables in a given Type (which can be a ParameterizedType or a TypeVariable)
     * using the provided mappings. This is crucial for resolving generic types in fields,
     * methods, and parameters based on the declaring class's concrete type arguments.
     *
     * @param type     The Type potentially containing TypeVariables.
     * @param mappings The map of TypeVariables to concrete Types determined from the class hierarchy.
     * @return The resolved Type with concrete arguments.
     * @throws ReflectionUtilsException if a TypeVariable is encountered without a corresponding mapping.
     */
    private static Type replaceTypeArguments(Type type, Map<TypeVariable<?>, Type> mappings) {
        if (type instanceof ParameterizedType parameterizedType) {
            Type[] actualArgs = parameterizedType.getActualTypeArguments();
            Type[] newActualTypes = new Type[actualArgs.length];

            for (int i = 0; i < actualArgs.length; i++) {
                newActualTypes[i] = replaceTypeArguments(actualArgs[i], mappings);
            }
            return TypeUtils.parameterize((Class<?>) parameterizedType.getRawType(), newActualTypes);
        }

        if (type instanceof TypeVariable<?> typeVariable) {
            if (mappings == null || !mappings.containsKey(typeVariable)) {
                return type;
            }

            Type replacement = mappings.get(typeVariable);
            if (replacement instanceof TypeVariable<?>) {
                return replaceTypeArguments(replacement, mappings);
            }
            return replacement;
        }
        return type;
    }


}
