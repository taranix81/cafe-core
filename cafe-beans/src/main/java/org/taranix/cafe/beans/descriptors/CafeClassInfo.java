package org.taranix.cafe.beans.descriptors;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.taranix.cafe.beans.annotations.CafeAnnotationUtils;
import org.taranix.cafe.beans.exceptions.CafeBeansFactoryException;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ClassInfo performs analyzing for a class:
 */
@Slf4j
public class CafeClassInfo implements CafeTypeDescriptor {
    @Getter
    private final Class<?> typeClass;
    @Getter
    private final Set<CafeMemberInfo> members = new HashSet<>();
    @Getter
    private final Set<Class<? extends Annotation>> annotations = new HashSet<>();


    private CafeClassInfo(final Class<?> typeClass, final Set<Class<? extends Annotation>> annotations) {
        this.typeClass = typeClass;
        this.annotations.addAll(annotations);
        scanMembers();
    }

    public static CafeClassInfo from(final Class<?> aClass, final Set<Class<? extends Annotation>> annotations) {
        return new CafeClassInfo(aClass, annotations);
    }

    public Set<Annotation> getClassAnnotations() {
        return CafeAnnotationUtils.getAnnotations(typeClass);
    }

    public <T extends Annotation> T getClassAnnotation(Class<T> annotationType) {
        return CafeAnnotationUtils.getAnnotationByType(typeClass, annotationType);
    }

    /*
    Scanning member class annotated by one of annotation listed in annotations
     */
    private void scanMembers() {
        if (!typeClass.isAnonymousClass() && !typeClass.isInterface() && !Modifier.isAbstract(typeClass.getModifiers())) {
            members.add(CafeConstructorInfo.from(this, findConstructor()));
        }

        for (Class<? extends Annotation> annotation : annotations) {
            members.addAll(methodDescriptors(annotation));
            members.addAll(fieldDescriptors(annotation));
        }
    }

    /**
     * Function return all methods containing Annotation(s) listed under getAnnotations()
     *
     * @return Set of {@link CafeMethodInfo}
     */
    public Set<CafeMethodInfo> methods(Class<? extends Annotation> annotationType) {
        return members.stream()
                .filter(CafeMethodInfo.class::isInstance)
                .map(CafeMethodInfo.class::cast)
                .filter(cafeMethodDescriptor -> cafeMethodDescriptor.getMethodAnnotations().stream()
                        .anyMatch(annotation -> annotation.annotationType().equals(annotationType)))
                .collect(Collectors.toSet());
    }

    private Constructor<?> findConstructor() {
        Constructor<?>[] constructors = typeClass.getDeclaredConstructors();
        if (constructors.length > 1) {
            throw new CafeBeansFactoryException("Couldn't determine which constructor of %s to use. Candidates : %n%s"
                    .formatted(typeClass.getName(), StringUtils.join(constructors, ",%n")));
        }
        return constructors[0];
    }

    private Set<Field> findFieldsAnnotatedWith(Class<?> introspectedClass, Class<? extends Annotation> annotation) {
        if (introspectedClass.equals(Object.class)) {
            return Set.of();
        }

        Set<Field> allFields = Arrays.stream(introspectedClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(annotation))
                .collect(Collectors.toSet());
        allFields.addAll(findFieldsAnnotatedWith(introspectedClass.getSuperclass(), annotation));

        return allFields;
    }

    private Set<Method> findMethodsAnnotatedWith(Class<?> introspectedClass, Class<? extends Annotation> annotation) {
        if (introspectedClass.equals(Object.class)) {
            return Set.of();
        }

        Set<Method> allMethods = Arrays.stream(introspectedClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(annotation))
                .collect(Collectors.toSet());
        allMethods.addAll(findMethodsAnnotatedWith(introspectedClass.getSuperclass(), annotation));
        return allMethods;
    }

    private Set<CafeMemberInfo> methodDescriptors(final Class<? extends Annotation> memberAnnotation) {
        return findMethodsAnnotatedWith(typeClass, memberAnnotation)
                .stream()
                .map(method -> new CafeMethodInfo(method, this))
                .collect(Collectors.toSet());
    }

    private Set<CafeFieldInfo> fieldDescriptors(final Class<? extends Annotation> memberAnnotation) {
        return findFieldsAnnotatedWith(typeClass, memberAnnotation)
                .stream()
                .map(field -> new CafeFieldInfo(field, this))
                .collect(Collectors.toSet());
    }

    /**
     * @return
     */
    @Override
    public BeanTypeKey typeKey() {
        return BeanTypeKey.from(typeClass);
    }

    /**
     * Function find all Beans provided by this class through it's members : <br>
     * - methods and <br>
     * - constructor
     *
     * @return list of {@link BeanTypeKey}
     */
    @Override
    public Set<BeanTypeKey> provides() {
        return allMembers().stream()
                .map(CafeMemberInfo::provides)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    /**
     * Function find all Beans required by this class to be able to be resolved
     *
     * @return list of {@link BeanTypeKey}
     */
    @Override
    public Set<BeanTypeKey> dependencies() {
        Set<BeanTypeKey> result = new HashSet<>();

        if (constructor() != null) {
            result.addAll(constructor().dependencies());
        }
        result.addAll(methods().stream()
                .map(CafeMethodInfo::dependencies)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet()));

        result.addAll(fields().stream()
                .map(CafeFieldInfo::dependencies)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet()));

        return result;
    }

    /**
     * Function return class's member annotated by one of the list from getAnnotations()
     *
     * @return Colletion fo {@link CafeMethodInfo}
     */
    public Set<CafeMemberInfo> allMembers() {
        Set<CafeMemberInfo> result = new HashSet<>();
        if (constructor() != null) {
            result.add(constructor());
        }
        result.addAll(methods());
        result.addAll(fields());
        return result;
    }

    /**
     * Function return ConstructorInfo class
     *
     * @return {@link CafeConstructorInfo}
     */
    public CafeConstructorInfo constructor() {
        return members.stream()
                .filter(CafeConstructorInfo.class::isInstance)
                .map(CafeConstructorInfo.class::cast)
                .findFirst()
                .orElse(null);
    }

    /**
     * Function return all methods annotated  by one of the list from getAnnotations()
     *
     * @return Set of {@link CafeMethodInfo}
     */
    public Set<CafeMethodInfo> methods() {
        return members.stream()
                .filter(CafeMethodInfo.class::isInstance)
                .map(CafeMethodInfo.class::cast)
                .collect(Collectors.toSet());
    }

    /**
     * Function return all fields annotated  by one of the list from getAnnotations()
     *
     * @return Set of {@link CafeFieldInfo}
     */
    public Set<CafeFieldInfo> fields() {
        return members.stream()
                .filter(CafeFieldInfo.class::isInstance)
                .map(CafeFieldInfo.class::cast)
                .collect(Collectors.toSet());
    }

    /**
     * @return
     */
    public boolean hasDependencies() {
        return !dependencies().isEmpty();
    }

    /**
     * @return
     */
    public boolean isPrototype() {
        return !isSingleton();
    }

    /**
     * @return
     */
    public boolean isSingleton() {
        return CafeAnnotationUtils.isSingleton(getTypeClass());
    }

    /**
     * @param clazz
     * @return
     */
    public boolean isImplementing(Class<?> clazz) {
        return clazz.isAssignableFrom(typeClass);
    }

    /**
     * Function find method annotated by {@link Class} <? extends {@link Annotation} >. <br>
     * NOTE: Annotation can be out of scope in declare annotations
     *
     * @param annotationType
     * @return
     */
    public Set<CafeMethodInfo> findAllMethodsAnnotatedBy(Class<? extends Annotation> annotationType) {
        return findMethodsAnnotatedWith(typeClass, annotationType)
                .stream()
                .map(method -> new CafeMethodInfo(method, this))
                .collect(Collectors.toSet());
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeClass, allMembers());
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CafeClassInfo cafeClassInfo = (CafeClassInfo) o;
        return hashCode() == cafeClassInfo.hashCode();
    }

    @Override
    public String toString() {
        return typeClass.toString();
    }


}
