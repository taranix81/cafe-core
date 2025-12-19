package org.taranix.cafe.beans.metadata;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.base.CafeWirerType;
import org.taranix.cafe.beans.annotations.modifiers.CafeModifier;
import org.taranix.cafe.beans.reflection.CafeAnnotationUtils;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.PropertyTypeKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Abstract base class representing metadata and behavior for a class member
 * (constructor, method, or field) in the Cafe Beans framework.
 * This class abstracts the reflection details and provides common utilities
 * for dependency analysis (provides/dependencies) and annotation access.
 */
public abstract class CafeMember {


    @Getter
    private final CafeClass cafeClass;

    /**
     * Constructs a CafeMemberInfo with the given class metadata parent.
     */
    protected CafeMember(CafeClass cafeClass) {
        this.cafeClass = cafeClass;
    }

    /**
     * Returns the reflective element that carries the annotations for this member.
     * For Fields and Methods, it's the Member itself.
     * For Constructors, it returns the Constructor object (not the Declaring Class),
     * allowing for proper access to constructor-level annotations (e.g., @Inject).
     */
    protected AnnotatedElement getAnnotatedElement() {
        // Constructor annotations are read directly from the constructor element.
        return (AnnotatedElement) getMember();
    }

    /**
     * Retrieves all annotations present on the underlying reflective element.
     *
     * @return A Collection of annotations, or an empty Set if none are present.
     */
    public Collection<Annotation> getAnnotations() {
        return Arrays.stream(getAnnotatedElement().getAnnotations())
                .collect(Collectors.toSet());
    }

    /**
     * Returns a set of annotation classes that are CafeType markers present on this member.
     * This is useful for identifying primary/qualifier types for bean resolution.
     */
    public final Set<Class<? extends Annotation>> getAnnotationLifecycleMarkers() {
        Set<Class<? extends Annotation>> result = new HashSet<>(getAnnotationTypesMarkedBy(CafeWirerType.class));
        result.addAll(getAnnotationTypesMarkedBy(CafeWirerType.class));
        return result;
    }

    /**
     * Returns a set of annotation classes that are CafeModifier markers present on this member.
     * This uses the CafeAnnotationUtils to check for meta-annotations extending {@link CafeModifier}.
     */
    public final Set<Class<? extends Annotation>> getAnnotationModifiers() {
        return getAnnotationTypesMarkedBy(CafeModifier.class);
    }

    /**
     * Checks if a specific annotation type is present on the member.
     */
    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        return getAnnotation(annotationType) != null;
    }

    /**
     * Retrieves a specific annotation instance present on the member.
     *
     * @param annotationType The type of annotation to retrieve.
     * @return The annotation instance, or null if not found.
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return getAnnotatedElement().getAnnotation(annotationType);
    }

    // --- Utility Methods ---

    /**
     * Returns the root class from the parent metadata.
     */
    public CafeClass getParent() {
        return cafeClass;
    }

    /**
     * Returns the type key for the owning class.
     */
    public BeanTypeKey getParentTypeKey() {
        return getParent().getRootClassTypeKey();
    }

    public Class<?> getParentRootClass() {
        return getParent().getRootClass();
    }

    /**
     * Returns the declaring class of the member.
     */
    public Class<?> getMemberDeclaringClass() {
        return getMember().getDeclaringClass();
    }

    /**
     * Returns the underlying reflective member (Field, Method, or Constructor).
     */
    public abstract Member getMember();

    /**
     * Checks if this member and another have the same declaring class.
     */
    public boolean isBelongToTheSameClass(CafeMember other) {
        return other != null && getMemberDeclaringClass().equals(other.getMemberDeclaringClass());
    }

    /**
     * Returns true if the member is static.
     */
    public boolean isStatic() {
        return Modifier.isStatic(getMember().getModifiers());
    }

    /**
     * Returns true if the member is a constructor.
     */
    public boolean isConstructor() {
        return getMember() instanceof Constructor<?>;
    }

    /**
     * Returns true if the member is a field.
     */
    public boolean isField() {
        return getMember() instanceof Field;
    }

    /**
     * Returns true if the member is a method.
     */
    public boolean isMethod() {
        return getMember() instanceof Method;
    }

    // --- Dependency Analysis ---

    /**
     * Returns the set of bean type keys this member provides (e.g., the return type of a factory method,
     * or the class type for a constructor).
     */
    public abstract Set<BeanTypeKey> getProvidedTypeKeys();

    /**
     * Returns true if this member has any bean or property dependencies.
     */
    public boolean hasDependencies() {
        return !getRequiredTypeKeys().isEmpty() || !getRequiredPropertyTypeKeys().isEmpty();
    }

    /**
     * Returns the list of bean type keys this member depends on (e.g., constructor parameters,
     * method parameters, or injected fields).
     */
    public abstract List<BeanTypeKey> getRequiredTypeKeys();

    /**
     * Returns the list of property type keys this member depends on (e.g., configuration properties).
     */
    public abstract List<PropertyTypeKey> getRequiredPropertyTypeKeys();

    /**
     * Returns true if this member depends on the given bean type key.
     */
    public boolean hasDependencies(BeanTypeKey typeKey) {
        return getRequiredTypeKeys().contains(typeKey);
    }

    // --- Scoping & Modifiers ---

    /**
     * Returns true if this member is prototype scoped.
     */
    public boolean isPrototype() {
        return !isSingleton();
    }

    /**
     * Returns true if this member is singleton scoped.
     * Uses CafeAnnotationUtils to check for scope annotations on the member.
     */
    public boolean isSingleton() {
        return CafeAnnotationUtils.isSingleton(getMember());
    }

    /**
     * Generic private method to find annotations that are meta-annotated with a specific marker.
     *
     * @param markerType The type of the marker annotation (e.g., CafeModifier.class, CafeType.class).
     * @return A set of annotation classes that carry the marker.
     * <p>
     * Should be move to Annotation Utils
     */
    public Set<Class<? extends Annotation>> getAnnotationTypesMarkedBy(Class<? extends Annotation> markerType) {
        // Use getAnnotatedElement() for unified access
        return Arrays.stream(getAnnotatedElement().getAnnotations())
                .filter(annotation -> CafeAnnotationUtils.isAnnotationMarkedBy(annotation, markerType))
                .map(Annotation::annotationType)
                .collect(Collectors.toSet());
    }

    public Set<Annotation> getAnnotationsMarkedBy(Class<? extends Annotation> markerType) {
        // Use getAnnotatedElement() for unified access
        return Arrays.stream(getAnnotatedElement().getAnnotations())
                .filter(annotation -> CafeAnnotationUtils.isAnnotationMarkedBy(annotation, markerType))
                .collect(Collectors.toSet());
    }


    // --- Object Overrides ---

    @Override
    public int hashCode() {
        // Hash code based solely on the underlying Member object for quick comparison
        return Objects.hash(getMember());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CafeMember other)) return false;
        // Equality based solely on the underlying Member object
        return Objects.equals(getMember(), other.getMember());
    }

    @Override
    public String toString() {
        String memberType = isConstructor() ? "Constructor"
                : isField() ? "Field"
                : "Method";
        return "(" + memberType + ") " + getParent().getRootClass().getCanonicalName() + ":" + getMember().getName();
    }
}