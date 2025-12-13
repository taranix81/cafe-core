package org.taranix.cafe.beans.descriptors.members;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.CafeAnnotationUtils;
import org.taranix.cafe.beans.annotations.modifiers.CafeModifier;
import org.taranix.cafe.beans.annotations.types.CafeType;
import org.taranix.cafe.beans.descriptors.CafeClassInfo;
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
public abstract class CafeMemberInfo {

    @Getter
    private final CafeClassInfo cafeClassInfo;

    /**
     * Constructs a CafeMemberInfo with the given class descriptor.
     */
    protected CafeMemberInfo(final CafeClassInfo cafeClassInfo) {
        this.cafeClassInfo = cafeClassInfo;
    }

    /**
     * Factory method to create a CafeMemberInfo for the given reflective member.
     * Delegates to the specific subclass (CafeFieldInfo, CafeMethodInfo, CafeConstructorInfo).
     *
     * @param cafeClassInfo The descriptor of the owning class.
     * @param member        The underlying reflective member.
     * @return The appropriate CafeMemberInfo subclass instance.
     * @throws NullPointerException if member is null.
     */
    public static CafeMemberInfo from(CafeClassInfo cafeClassInfo, Member member) {
        Objects.requireNonNull(member, "member must not be null");
        if (member instanceof Field field) {
            return new CafeFieldInfo(field, cafeClassInfo);
        } else if (member instanceof Method method) {
            return new CafeMethodInfo(method, cafeClassInfo);
        } else if (member instanceof Constructor<?> constructor) {
            return new CafeConstructorInfo(constructor, cafeClassInfo);
        }
        // Throw an exception for unsupported member types for better failure visibility.
        throw new IllegalArgumentException("Unsupported member type: " + member.getClass().getName());
    }

    // --- Annotation Accessors (Refactored using getAnnotatedElement()) ---

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
     * Returns the type key for the owning class.
     */
    public BeanTypeKey getOwnerClassTypeKey() {
        return BeanTypeKey.from(cafeClassInfo.getTypeClass());
    }

    /**
     * Returns the declaring class of the member.
     */
    public Class<?> declaringClass() {
        return getMember().getDeclaringClass();
    }

    /**
     * Returns the underlying reflective member (Field, Method, or Constructor).
     */
    public abstract Member getMember();

    /**
     * Checks if this member and another have the same declaring class.
     */
    public boolean hasSameDeclaringClass(CafeMemberInfo other) {
        return other != null && declaringClass().equals(other.declaringClass());
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
    public abstract Set<BeanTypeKey> provides();

    /**
     * Returns true if this member has any bean or property dependencies.
     */
    public boolean hasDependencies() {
        return !dependencies().isEmpty() || !propertyDependencies().isEmpty();
    }

    /**
     * Returns the list of bean type keys this member depends on (e.g., constructor parameters,
     * method parameters, or injected fields).
     */
    public abstract List<BeanTypeKey> dependencies();

    /**
     * Returns the list of property type keys this member depends on (e.g., configuration properties).
     */
    public abstract List<PropertyTypeKey> propertyDependencies();

    /**
     * Returns true if this member depends on the given bean type key.
     */
    public boolean hasDependencies(BeanTypeKey typeKey) {
        return dependencies().contains(typeKey);
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
     */
    private Set<Class<? extends Annotation>> getAnnotationsMarkedBy(Class<? extends Annotation> markerType) {
        // Use getAnnotatedElement() for unified access
        return Arrays.stream(getAnnotatedElement().getAnnotations())
                .filter(annotation -> CafeAnnotationUtils.isAnnotationMarkedBy(annotation, markerType))
                .map(Annotation::annotationType)
                .collect(Collectors.toSet());
    }

    /**
     * Returns a set of annotation classes that are CafeModifier markers present on this member.
     * This uses the CafeAnnotationUtils to check for meta-annotations extending {@link CafeModifier}.
     */
    public final Set<Class<? extends Annotation>> getAnnotationModifiers() {
        return getAnnotationsMarkedBy(CafeModifier.class);
    }

    /**
     * Returns a set of annotation classes that are CafeType markers present on this member.
     * This is useful for identifying primary/qualifier types for bean resolution.
     */
    public final Set<Class<? extends Annotation>> getAnnotationTypes() {
        return getAnnotationsMarkedBy(CafeType.class);
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
        if (!(obj instanceof CafeMemberInfo other)) return false;
        // Equality based solely on the underlying Member object
        return Objects.equals(getMember(), other.getMember());
    }

    @Override
    public String toString() {
        String memberType = isConstructor() ? "Constructor"
                : isField() ? "Field"
                : "Method";
        return "(" + memberType + ") " + cafeClassInfo.getTypeClass().getCanonicalName() + ":" + getMember().getName();
    }
}