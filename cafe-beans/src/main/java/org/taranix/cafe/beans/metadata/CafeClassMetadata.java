package org.taranix.cafe.beans.metadata;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.taranix.cafe.beans.annotations.types.CafeType;
import org.taranix.cafe.beans.exceptions.CafeClassMetadataException;
import org.taranix.cafe.beans.reflection.CafeAnnotationUtils;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the immutable metadata for a specific class within the Cafe IoC container.
 * This class captures and caches all necessary reflection information, including member
 * metadata (fields, constructors, methods), dependency requirements, and provided types.
 *
 * <p>Metadata is pre-calculated during construction to ensure fast, thread-safe access
 * to member subsets and dependency graphs.</p>
 */
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CafeClassMetadata {

    /**
     * The underlying Class object for which this metadata was created.
     */
    @Getter
    @ToString.Include
    @EqualsAndHashCode.Include
    private final Class<?> rootClass;

    /**
     * An unmodifiable set of all members (fields, constructor, methods) relevant to the container.
     */
    @Getter
    private final Set<CafeMemberMetadata> members;

    /**
     * The metadata for the constructor used for bean instantiation, or null if not applicable.
     */
    @Getter
    private final CafeConstructorMetadata constructor;

    /**
     * An unmodifiable set of field metadata, pre-filtered from all members.
     */
    @Getter
    private final Set<CafeFieldMetadata> fields;

    /**
     * An unmodifiable set of method metadata, pre-filtered from all members.
     */
    @Getter
    private final Set<CafeMethodMetadata> methods;

    // Caches for pre-calculated dependency graph information
    private final Set<BeanTypeKey> cachedDependencies;
    private final Set<BeanTypeKey> cachedProvides;

    /**
     * Constructs the metadata object by scanning and processing the root class's members.
     * All subsets and dependency information are cached immediately.
     *
     * @param rootClass The class being analyzed.
     * @throws IllegalStateException if the class has multiple constructors annotated for dependency injection.
     */
    protected CafeClassMetadata(final Class<?> rootClass) {
        this.rootClass = Objects.requireNonNull(rootClass, "Root class cannot be null");

        // 1. Inicjalizacja kolekcji
        Set<CafeMemberMetadata> tempMembers = new HashSet<>();
        CafeConstructorMetadata foundConstructor = null;
        Set<CafeFieldMetadata> foundFields = new HashSet<>();
        Set<CafeMethodMetadata> foundMethods = new HashSet<>();

        // 2. Logika skanowania (przeniesiona z CafeClassMetadataFactory) [cite: 1513]
        if (!isNotScannable(rootClass)) {

            // A. Konstruktory
            if (!rootClass.isInterface()) {
                Constructor<?> rawConstructor = extractConstructor(rootClass);
                // Tworzymy metadane przekazując 'this'
                foundConstructor = new CafeConstructorMetadata(rawConstructor, this);
                tempMembers.add(foundConstructor);
            }

            // B. Pola
            Set<Field> rawFields = extractAnnotatedFields(rootClass);
            for (Field field : rawFields) {
                CafeFieldMetadata fieldMetadata = new CafeFieldMetadata(field, this);
                foundFields.add(fieldMetadata);
                tempMembers.add(fieldMetadata);
            }

            // C. Metody
            Set<Method> rawMethods = extractAnnotatedMethods(rootClass);
            for (Method method : rawMethods) {
                CafeMethodMetadata methodMetadata = new CafeMethodMetadata(method, this);
                foundMethods.add(methodMetadata);
                tempMembers.add(methodMetadata);
            }
        }

        // 3. Finalizacja pól
        this.members = Collections.unmodifiableSet(tempMembers);
        this.constructor = foundConstructor;
        this.fields = Collections.unmodifiableSet(foundFields);
        this.methods = Collections.unmodifiableSet(foundMethods);

        // 4. Pre-kalkulacja zależności (bez zmian) [cite: 1468]
        this.cachedDependencies = calculateDependencies();
        this.cachedProvides = calculateProvides();
    }

    // --- Helper Scan Methods (Przeniesione i dostosowane z CafeClassMetadataFactory) ---

    private boolean isNotScannable(Class<?> clazz) {
        return clazz.isAnonymousClass() || clazz.isPrimitive() || clazz.isArray();
    }

    private Constructor<?> extractConstructor(Class<?> aClass) {
        Constructor<?>[] constructors = aClass.getDeclaredConstructors();
        if (constructors.length != 1) {
            throw new CafeClassMetadataException(
                    "Cafe currently supports only single constructor injection. Class: %s found: %d"
                            .formatted(aClass.getName(), constructors.length));
        }
        return constructors[0];
    }

    private Set<Field> extractAnnotatedFields(Class<?> introspectedClass) {
        if (introspectedClass == null || introspectedClass.equals(Object.class)) {
            return Collections.emptySet();
        }
        Set<Field> allFields = Arrays.stream(introspectedClass.getDeclaredFields())
                .filter(field -> CafeAnnotationUtils.hasMarker(field, CafeType.class))
                .collect(Collectors.toSet());
        allFields.addAll(extractAnnotatedFields(introspectedClass.getSuperclass()));
        return allFields;
    }

    private String generateMethodSignature(Method method) {
        String paramTypes = Arrays.stream(method.getParameterTypes())
                .map(Class::getName)
                .collect(Collectors.joining(","));
        return method.getName() + "(" + paramTypes + ")";
    }

    private Set<Method> extractAnnotatedMethods(Class<?> introspectedClass) {
        if (introspectedClass == null || introspectedClass.equals(Object.class)) {
            return Collections.emptySet();
        }

        Map<String, Method> uniqueMethods = new HashMap<>();
        Class<?> currentClass = introspectedClass;

        while (currentClass != null && !currentClass.equals(Object.class)) {
            for (Method method : currentClass.getDeclaredMethods()) {
                if (method.isBridge() || method.isSynthetic()) {
                    continue;
                }
                if (!CafeAnnotationUtils.hasMarker(method, CafeType.class)) {
                    continue;
                }
                String methodSignature = generateMethodSignature(method);
                uniqueMethods.putIfAbsent(methodSignature, method);
            }

            currentClass = currentClass.getSuperclass();
        }

        return new HashSet<>(uniqueMethods.values());
    }

    /**
     * Returns an unmodifiable set of all annotations directly present on the root class.
     *
     * @return A set of annotations.
     */
    public Set<Annotation> getRootClassAnnotations() {
        return CafeAnnotationUtils.getAnnotations(rootClass);
    }

    /**
     * Retrieves a specific annotation instance from the root class.
     *
     * @param annotationType The type of the annotation to retrieve.
     * @param <T>            The annotation type.
     * @return The annotation instance, or {@code null} if not present.
     */
    public <T extends Annotation> T getRootClassAnnotation(Class<T> annotationType) {
        return CafeAnnotationUtils.getAnnotationByType(rootClass, annotationType);
    }

    /**
     * Retrieves the metadata for a specific field by its name.
     *
     * @param fieldName The name of the field.
     * @return The {@link CafeFieldMetadata} or {@code null} if the field is not found or not managed.
     */
    public CafeFieldMetadata getField(String fieldName) {
        return fields.stream()
                .filter(f -> f.getField().getName().equals(fieldName))
                .findFirst()
                .orElse(null);
    }

    public CafeMethodMetadata getMethodMetadata(Method method) {
        return methods.stream()
                .filter(cafeMethodMetadata -> cafeMethodMetadata.getMethod().equals(method))
                .findFirst()
                .orElse(null);
    }

    public CafeMethodMetadata getMethodMetadata(String methodName, BeanTypeKey... requiredTypes) {
        return methods.stream()
                .filter(cafeMethodMetadata -> cafeMethodMetadata.getMethod().getName().equals(methodName))
                .filter(cafeMethodMetadata -> Arrays.equals(cafeMethodMetadata.getMethodParameterTypeKeys(), requiredTypes))
                .findFirst()
                .orElse(null);
    }


    /**
     * Returns the {@link BeanTypeKey} representing the primary type of this class.
     *
     * @return The type key of the root class.
     */
    public BeanTypeKey getRootClassTypeKey() {
        return BeanTypeKey.from(rootClass);
    }

    /**
     * Returns an unmodifiable set of all {@link BeanTypeKey}s that this class and its
     * members (e.g., factory methods) can provide to the container.
     *
     * @return A set of provided bean types (keys).
     */
    public Set<BeanTypeKey> getProvidedTypes() {
        return cachedProvides;
    }

    /**
     * Returns an unmodifiable set of all {@link BeanTypeKey}s that this class requires
     * from the container via injection (constructor, field, method parameters).
     *
     * @return A set of required bean types (keys).
     */
    public Set<BeanTypeKey> getRequiredTypes() {
        return cachedDependencies;
    }

    /**
     * Checks if this class has any dependencies that need to be injected by the container.
     *
     * @return {@code true} if {@link #getRequiredTypes()} is not empty.
     */
    public boolean hasDependencies() {
        return !cachedDependencies.isEmpty();
    }

    /**
     * Checks if the bean defined by this metadata is a prototype scope (not singleton).
     *
     * @return {@code true} if the bean scope is prototype.
     */
    public boolean isPrototype() {
        return !isSingleton();
    }

    /**
     * Checks if the bean defined by this metadata is a singleton scope.
     *
     * @return {@code true} if the bean scope is singleton.
     */
    public boolean isSingleton() {
        return CafeAnnotationUtils.isSingleton(rootClass);
    }

    /**
     * Checks if the root class implements or is assignable from the given class/interface.
     *
     * @param clazz The class or interface to check against.
     * @return {@code true} if the root class implements/extends the specified class.
     */
    public boolean isImplementing(Class<?> clazz) {
        return clazz.isAssignableFrom(rootClass);
    }

    /**
     * Calculates all types provided by the class's members (e.g., factory methods).
     *
     * @return A set of provided {@link BeanTypeKey}s.
     */
    private Set<BeanTypeKey> calculateProvides() {
        return members.stream()
                .map(CafeMemberMetadata::getProvidedTypes)
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Calculates all dependency types required by the class's constructor, fields, and setter methods.
     *
     * @return A set of required {@link BeanTypeKey}s.
     */
    private Set<BeanTypeKey> calculateDependencies() {
        Set<BeanTypeKey> result = new HashSet<>();
        if (constructor != null) {
            result.addAll(constructor.getRequiredTypes());
        }
        methods.forEach(m -> result.addAll(m.getRequiredTypes()));
        fields.forEach(f -> result.addAll(f.getRequiredTypes()));
        return Collections.unmodifiableSet(result);
    }
}