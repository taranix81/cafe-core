package org.taranix.cafe.beans.reflection;

import com.google.common.reflect.ClassPath;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.exceptions.ReflectionUtilsException;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Utility class providing common reflection operations for the Cafe IoC container.
 * This includes type checking, member access, class hierarchy scanning, and generic type resolution.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CafeReflectionUtils {

    // --- Type Checking Utilities ---

    /**
     * Checks if the given Type represents a {@link Collection} (either raw or parameterized).
     *
     * @param type The Type to check.
     * @return true if the type is or extends Collection.
     */
    public static boolean isCollection(Type type) {
        return (isClass(type) && Collection.class.isAssignableFrom((Class<?>) type)) ||
                (isParametrizedType(type) && isCollection(((ParameterizedType) type).getRawType()));
    }

    /**
     * Checks if the given Type is a generic type variable or contains generic type variables.
     *
     * @param type The Type to check.
     * @return true if the type is generic (i.e., a TypeVariable or a ParameterizedType containing TypeVariables).
     */
    public static boolean isGenericType(Type type) {
        if (isTypeVariable(type)) {
            return true;
        }
        return isParametrizedType(type) && Arrays.stream(((ParameterizedType) type).getActualTypeArguments())
                .allMatch(CafeReflectionUtils::isGenericType);
    }

    /**
     * Checks if the Type is a {@link TypeVariable}.
     */
    public static boolean isTypeVariable(Type type) {
        return type instanceof TypeVariable<?>;
    }

    /**
     * Checks if the Type is a {@link ParameterizedType}.
     */
    public static boolean isParametrizedType(final Type type) {
        return type instanceof ParameterizedType;
    }

    /**
     * Checks if the given Type represents an array.
     */
    public static boolean isArray(Type type) {
        return isClass(type) && ((Class<?>) type).isArray();
    }

    /**
     * Checks if the Type is a raw {@link Class} type.
     */
    public static boolean isClass(Type type) {
        return type instanceof Class<?>;
    }

    // --- Member Access and Invocation ---

    /**
     * Sets the value of a field on a target object, handling private access.
     *
     * @param field      The Field to set.
     * @param fieldOwner The object owning the field.
     * @param value      The value to set.
     * @throws ReflectionUtilsException if access fails.
     */
    public static void setFieldValue(Field field, Object fieldOwner, Object value) {
        try {
            field.setAccessible(true);
            field.set(fieldOwner, value);
        } catch (IllegalAccessException e) {
            throw new ReflectionUtilsException("Couldn't set field %s value %s on object %s: %s"
                    .formatted(field.getName(), value, fieldOwner.getClass().getName(), e.getMessage()));
        }
    }

    /**
     * Invokes a method on a target object, handling private access and arguments.
     *
     * @param method The Method to invoke.
     * @param owner  The object owning the method.
     * @param args   The arguments for the method.
     * @return The result of the method invocation.
     * @throws ReflectionUtilsException if invocation fails (IllegalAccess or InvocationTarget exception).
     */
    public static Object getMethodValue(Method method, Object owner, Object... args) {
        try {
            method.setAccessible(true);
            return method.invoke(owner, args);
        } catch (IllegalAccessException e) {
            throw new ReflectionUtilsException("Couldn't invoke method %s on object %s: Illegal access."
                    .formatted(method.getName(), owner.getClass().getName()));
        } catch (InvocationTargetException e) {
            // Wrap the underlying exception if available
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new ReflectionUtilsException("Method %s on object %s threw an exception: %s"
                    .formatted(method.getName(), owner.getClass().getName(), cause.getMessage()));
        }
    }

    /**
     * Instantiates a new object using the given constructor and arguments.
     *
     * @param constructor The Constructor to use.
     * @param args        The arguments for the constructor.
     * @return The newly instantiated object.
     * @throws ReflectionUtilsException if instantiation fails (Instantiation, IllegalAccess, or InvocationTarget exception).
     */
    public static Object instantiate(Constructor<?> constructor, Object... args) {
        try {
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ReflectionUtilsException("Couldn't instantiate class %s: %s"
                    .formatted(constructor.getDeclaringClass().getName(), e.getMessage()));
        } catch (InvocationTargetException e) {
            // Wrap the underlying exception if available
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new ReflectionUtilsException("Constructor for class %s threw an exception: %s"
                    .formatted(constructor.getDeclaringClass().getName(), cause.getMessage()));
        }
    }

    // --- Hierarchy Scanning ---

    // --- Generic Type Resolution ---


    /**
     * Determines the concrete Type of a field by resolving its generic arguments
     * against the class's type arguments.
     */
    public static Type determineFieldType(Field field, Class<?> clazz) {
        return CafeTypesUtils.resolve(clazz, field.getGenericType());
    }

    /**
     * Determines the concrete return Type of a method by resolving its generic arguments
     * against the class's type arguments.
     */
    public static Type determineMethodReturnType(Method method, Class<?> clazz) {
        return CafeTypesUtils.resolve(clazz, method.getGenericReturnType());
    }

    /**
     * Determines the concrete parameter Types of a method by resolving their generic arguments
     * against the class's type arguments.
     */
    public static Type[] determineMethodParameterTypes(Method method, Class<?> clazz) {
        return Arrays.stream(method.getGenericParameterTypes())
                .map(type -> CafeTypesUtils.resolve(clazz, type))
                .toArray(Type[]::new);
    }


    /**
     * Determines the concrete parameter Types of a constructor by resolving their generic arguments
     * against the declaring class's type arguments.
     */
    public static List<Type> determineConstructorParameterTypes(Constructor<?> constructor) {
        return Arrays.stream(constructor.getGenericParameterTypes())
                .map(type -> CafeTypesUtils.resolve(constructor.getDeclaringClass(), type))
                .toList();
    }


    // --- Class Loading and Scanning ---

    /**
     * Returns the default ClassLoader for the current thread.
     */
    public static ClassLoader getDefault() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * Scans a package recursively using a ClassLoader and returns a stream of
     * loadable concrete classes (excluding interfaces, records, etc.).
     *
     * @param classLoader   The ClassLoader to use for loading.
     * @param lookupPackage The base package name (e.g., "com.mycompany.beans").
     * @return A Stream of Class objects.
     * @throws ReflectionUtilsException if class path scanning fails (e.g., IOException).
     */
    public static Stream<Class<?>> getAllClassesFromPackage(ClassLoader classLoader, String lookupPackage) {
        try {
            ClassPath classPath = ClassPath.from(classLoader);
            return classPath
                    .getTopLevelClassesRecursive(lookupPackage)
                    .stream()
                    .map(classInfo -> (Class<?>) classInfo.load())
                    .filter(clazz -> !clazz.isInterface() && !clazz.isRecord())
                    .map(aClass -> (Class<?>) aClass); // Casting needed because ClassPath uses ClassInfo
        } catch (IOException e) {
            throw new ReflectionUtilsException("Failed to scan package '%s' for classes: %s"
                    .formatted(lookupPackage, e.getMessage()));
        }
    }


    /**
     * Returns a set of all supertypes (classes and interfaces) for a given class,
     * considering their full generic signatures.
     *
     * @param theClass The starting class to be analyzed.
     * @return A unique set of supertypes (interfaces and classes) as Type objects.
     */
    public static Set<Type> getAllSuperTypes(Class<?> theClass) {
        return CafeTypesUtils.getAllSuperTypes(theClass);
    }


}