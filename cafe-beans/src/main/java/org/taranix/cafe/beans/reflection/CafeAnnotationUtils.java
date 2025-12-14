package org.taranix.cafe.beans.reflection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.beans.annotations.Scope;
import org.taranix.cafe.beans.annotations.modifiers.CafeName;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for common reflection and annotation operations within the Cafe framework.
 * Handles scope resolution, member naming, and meta-annotation (marker) detection.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CafeAnnotationUtils {

    // --- Annotation Presence & Retrieval ---

    /**
     * Checks if a specific annotation is present on a given Type (Class).
     *
     * @param type       The reflection type (must be a Class).
     * @param annotation The annotation class to check for.
     * @return true if the annotation is present.
     */
    public static boolean isAnnotationPresent(Type type, Class<? extends Annotation> annotation) {
        if (annotation != null && type instanceof Class<?> clazz) {
            return clazz.isAnnotationPresent(annotation);
        }
        return false;
    }

    /**
     * Retrieves all annotations present on a given Class.
     */
    public static Set<Annotation> getAnnotations(Class<?> clz) {
        return Arrays.stream(clz.getAnnotations()).collect(Collectors.toSet());
    }

    /**
     * Retrieves a specific annotation instance from a Class.
     */
    public static <T extends Annotation> T getAnnotationByType(Class<?> clz, Class<T> annotationType) {
        return clz.getAnnotation(annotationType);
    }

    // --- Scope Resolution ---

    /**
     * Determines the scope of a class, defaulting to Singleton if no @CafeService is present.
     */
    public static Scope getScope(Class<?> clazz) {
        return Optional.ofNullable(clazz.getAnnotation(CafeService.class))
                .map(CafeService::scope)
                .orElse(Scope.Singleton);
    }

    /**
     * Checks if a class is configured as a Singleton scope.
     */
    public static boolean isSingleton(Class<?> clazz) {
        return getScope(clazz).equals(Scope.Singleton);
    }

    /**
     * Determines the scope based on the Member type.
     * Logic is simplified to handle only methods (@CafeProvider) and constructors/classes (@CafeService).
     *
     * @param member The reflective member (Method, Constructor, Field).
     * @return The determined Scope, defaults to Singleton.
     */
    public static Scope getScope(Member member) {
//        if (member instanceof Method method) {
//            // Check method annotation for factory methods
//            return Optional.ofNullable(method.getAnnotation(CafeProvider.class))
//                    .map(CafeProvider::scope)
//                    .orElse(Scope.Singleton);
//        }

        if (member instanceof Constructor<?>) {
            // Check declaring class annotation for constructors
            return Optional.ofNullable(member.getDeclaringClass().getAnnotation(CafeService.class))
                    .map(CafeService::scope)
                    .orElse(Scope.Singleton);
        }

        // Fields and non-factory methods default to Singleton (if injected, they follow the class scope,
        // but here we only check annotations directly on the member or its class).
        return Scope.Singleton;
    }

    /**
     * Checks if a member (Method, Constructor, Field) is configured as a Singleton scope.
     */
    public static boolean isSingleton(Member member) {
        return getScope(member).equals(Scope.Singleton);
    }

    // --- Naming Resolution ---

    /**
     * Retrieves the explicit bean name from a member using the @CafeName annotation.
     *
     * @param member The reflective member (Field or Method).
     * @return The explicitly configured name, or an empty string if not found.
     */
    public static String getMemberName(Member member) {
        // Unified way to get @CafeName for Field and Method
        AnnotatedElement element = (AnnotatedElement) member;
        return Optional.ofNullable(element.getAnnotation(CafeName.class))
                .map(CafeName::value)
                .orElse(StringUtils.EMPTY);
    }

    // --- Hierarchy Scanning ---

    /**
     * Finds all methods in a class hierarchy (including superclasses) that are annotated
     * with the specified annotation type, ensuring that only the most specific (overriding)
     * method is returned for a given signature, thereby preventing duplicates.
     *
     * @param clz            The starting class for scanning.
     * @param annotationType The annotation to look for.
     * @return A List of all unique, matching Methods (most specific one is chosen).
     */
    public static List<Method> getClassMethodsAnnotatedBy(Class<?> clz, Class<? extends Annotation> annotationType) {
        // Map to store methods, keyed by a unique signature, ensuring that only the most derived method is kept.
        // We use Map.putIfAbsent to enforce that the first method encountered (from the most derived class) is retained.
        Map<String, Method> uniqueMethods = new HashMap<>();
        Class<?> currentClass = clz;

        while (currentClass != null && currentClass != Object.class) {
            for (Method method : currentClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(annotationType)) {
                    // Create a unique key based on method signature
                    String key = getMethodSignature(method);

                    // Add the method only if the key is not already present.
                    // Since we iterate from subclass up to superclass, this ensures the most specific method wins.
                    uniqueMethods.putIfAbsent(key, method);
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        // Return all unique methods found.
        return new ArrayList<>(uniqueMethods.values());
    }

    /**
     * Helper method to generate a unique key for a method based on its name and parameter types.
     * This key is used to detect overridden methods across the class hierarchy.
     */
    private static String getMethodSignature(Method method) {
        return method.getName() + Arrays.stream(method.getParameterTypes())
                .map(Class::getName)
                .collect(Collectors.joining(",", "(", ")"));
    }

    // --- Meta-Annotation (Marker) Logic ---

    /**
     * Checks if an annotation is meta-annotated (extended) by a specific marker interface/annotation.
     * This handles two cases:
     * 1. The annotation itself is directly marked by the otherAnnotationClass.
     * 2. The annotation is marked by an intermediate annotation, which is marked by the otherAnnotationClass.
     * (Implicit in the original code, but often requires recursion for full depth).
     * <p>
     *
     * @param annotation           The annotation instance to check.
     * @param otherAnnotationClass The marker annotation (e.g., CafeModifier.class, CafeType.class).
     * @return true if the annotation is extended by the marker.
     */
    public static boolean isAnnotationMarkedBy(Annotation annotation, Class<? extends Annotation> otherAnnotationClass) {
        // 1. Check if the annotation type is directly marked by the otherAnnotationClass
        if (annotation.annotationType().isAnnotationPresent(otherAnnotationClass)) {
            return true;
        }

        // 2. Check all annotations *on* this annotation for the marker.
        return Arrays.stream(annotation.annotationType().getAnnotations())
                .filter(a -> !a.annotationType().getPackageName().contains("java.lang"))
                .anyMatch(a -> isAnnotationMarkedBy(a, otherAnnotationClass));
    }

    /**
     * Unified method to check if an AnnotatedElement (Class, Field, Method, Constructor)
     * has any annotation that is marked by the given markerClass.
     *
     * @param element     The reflective element to check.
     * @param markerClass The marker annotation (e.g., Initable.class, Taskable.class).
     * @return true if any annotation on the element is extended by the markerClass.
     */
    public static boolean hasMarker(AnnotatedElement element, Class<? extends Annotation> markerClass) {
        return Arrays.stream(element.getAnnotations())
                .anyMatch(annotation -> isAnnotationMarkedBy(annotation, markerClass));
    }
}