package org.taranix.cafe.beans.reflection;

import com.google.common.reflect.ClassPath;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.taranix.cafe.beans.exceptions.ReflectionUtilsException;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
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
     * Determines the mapping of TypeVariables to concrete Types for a given class.
     * This typically analyzes the generic superclass to resolve type arguments.
     *
     * @param cls The class to analyze.
     * @return A Map where keys are TypeVariables and values are their concrete Type replacements.
     */
    public static Map<TypeVariable<?>, Type> determineTypeArguments(Class<?> cls) {
        Type superClassGeneric = cls.getGenericSuperclass();

        if (isParametrizedType(superClassGeneric)) {
            // Use Apache TypeUtils for robust resolution based on the parameterized superclass
            return TypeUtils.determineTypeArguments(cls, (ParameterizedType) superClassGeneric);
        }

        // If the superclass is a raw class (e.g., extends MyClass), recursively check its hierarchy.
        if (isClass(superClassGeneric) && !superClassGeneric.equals(Object.class)) {
            return determineTypeArguments((Class<?>) superClassGeneric);
        }
        return Map.of();
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
                Type arg = actualArgs[i];
                // Recursively resolve type arguments within the parameterized type
                newActualTypes[i] = replaceTypeArguments(arg, mappings);
            }
            // Re-create the ParameterizedType with resolved arguments
            return TypeUtils.parameterize((Class<?>) parameterizedType.getRawType(), newActualTypes);
        }

        if (type instanceof TypeVariable<?> typeVariable) {
            if (!mappings.containsKey(typeVariable)) {
                // This exception handles cases where a generic variable is unresolved (e.g., T in a method)
                throw new ReflectionUtilsException("No concrete Type mapping found for TypeVariable: %s"
                        .formatted(typeVariable.getName()));
            }

            Type replacement = mappings.get(typeVariable);
            if (replacement instanceof TypeVariable<?>) {
                // If the replacement is still a TypeVariable, recursively check if it can be resolved further
                return replaceTypeArguments(replacement, mappings);
            }
            return replacement;
        }
        // Return raw type, Class, or other Type implementations unchanged
        return type;
    }

    /**
     * Determines the concrete Type of a field by resolving its generic arguments
     * against the class's type arguments.
     */
    public static Type determineFieldType(Field field, Class<?> clazz) {
        Type genericFieldType = field.getGenericType();
        return replaceTypeArguments(genericFieldType, determineTypeArguments(clazz));
    }

    /**
     * Determines the concrete return Type of a method by resolving its generic arguments
     * against the class's type arguments.
     */
    public static Type determineMethodReturnType(Method method, Class<?> clazz) {
        Type returnGenericType = method.getGenericReturnType();
        return replaceTypeArguments(returnGenericType, determineTypeArguments(clazz));
    }

    /**
     * Determines the concrete parameter Types of a method by resolving their generic arguments
     * against the class's type arguments.
     */
    public static Type[] determineMethodParameterTypes(Method method, Class<?> clazz) {
        Map<TypeVariable<?>, Type> typeArgs = determineTypeArguments(clazz);
        return Arrays.stream(method.getGenericParameterTypes())
                .map(type -> replaceTypeArguments(type, typeArgs))
                .toArray(Type[]::new);
    }


    /**
     * Determines the concrete parameter Types of a constructor by resolving their generic arguments
     * against the declaring class's type arguments.
     */
    public static List<Type> determineConstructorParameterTypes(Constructor<?> constructor) {
        Map<TypeVariable<?>, Type> typeArgs = determineTypeArguments(constructor.getDeclaringClass());
        return Arrays.stream(constructor.getGenericParameterTypes())
                .map(type -> replaceTypeArguments(type, typeArgs))
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
        if (theClass == null) {
            return Collections.emptySet();
        }

        // We use LinkedHashSet to maintain uniqueness and order.
        Set<Type> result = new LinkedHashSet<>();
        // Queue for processing types (BFS - Breadth First Search).
        Queue<Type> queue = new ArrayDeque<>();

        // The starting class must be added as a Type (which makes it a Type, not a Class)
        queue.add(theClass);

        while (!queue.isEmpty()) {
            Type currentType = queue.poll();

            // 1. Check for uniqueness at the full type level (e.g., List<String> != List<Integer>)
            if (currentType != null && !currentType.equals(Object.class) && result.add(currentType)) {

                // 2. Get the raw class (Class<?>) from the current Type.
                // This method must be able to extract Class<?> from both Class<?> and ParameterizedType.
                Class<?> rawClass = getRawClass(currentType);

                if (rawClass != null && !rawClass.equals(Object.class)) {

                    // 3. Add generic superclass (extends)
                    Type superclass = rawClass.getGenericSuperclass();
                    if (superclass != null) {
                        queue.add(superclass);
                    }

                    // 4. Add generic interfaces (implements)
                    Type[] interfaces = rawClass.getGenericInterfaces();
                    if (interfaces.length > 0) {
                        queue.addAll(Arrays.asList(interfaces));
                    }
                }
            }
        }

        // Removing the starting class and Object.class (which gets added as a Type after scanning superclasses)
        result.remove(theClass);
        // Remove Object.class if it appears as a raw type
        result.remove(Object.class);

        return Collections.unmodifiableSet(result);
    }

    /**
     * Utility method to get the raw class from a Type object.
     * You should place this implementation in CafeReflectionUtils.
     */
    private static Class<?> getRawClass(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        // If it is a generic type with parameters (e.g., List<String>)
        if (type instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?>) {
                return (Class<?>) rawType;
            }
        }
        // Other types like GenericArrayType, WildcardType, TypeVariable should be handled,
        // but they are marginal in this context.
        return null;
    }
}