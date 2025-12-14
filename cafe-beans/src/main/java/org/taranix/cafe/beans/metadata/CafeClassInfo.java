package org.taranix.cafe.beans.metadata;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.taranix.cafe.beans.annotations.types.CafeType;
import org.taranix.cafe.beans.exceptions.CafeBeansFactoryException;
import org.taranix.cafe.beans.metadata.members.CafeConstructorInfo;
import org.taranix.cafe.beans.metadata.members.CafeFieldInfo;
import org.taranix.cafe.beans.metadata.members.CafeMemberInfo;
import org.taranix.cafe.beans.metadata.members.CafeMethodInfo;
import org.taranix.cafe.beans.reflection.CafeAnnotationUtils;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class CafeClassInfo {
    @Getter
    private final Class<?> typeClass;
    @Getter
    private final Set<CafeMemberInfo> members;

    private CafeClassInfo(final Class<?> typeClass) {
        this.typeClass = typeClass;
        this.members = Collections.unmodifiableSet(scanMembers());
    }

    public static CafeClassInfo from(final Class<?> aClass) {
        return new CafeClassInfo(aClass);
    }

    public Set<Annotation> getClassAnnotations() {
        return CafeAnnotationUtils.getAnnotations(typeClass);
    }

    public <T extends Annotation> T getClassAnnotation(Class<T> annotationType) {
        return CafeAnnotationUtils.getAnnotationByType(typeClass, annotationType);
    }

    private Set<CafeMemberInfo> scanMembers() {
        Set<CafeMemberInfo> result = new HashSet<>();
        if (!typeClass.isAnonymousClass() && !typeClass.isInterface() && !Modifier.isAbstract(typeClass.getModifiers())) {
            result.add(CafeMemberInfo.from(this, extractConstructor()));
        }
        extractAnnotatedMethods(typeClass).forEach(method -> result.add(CafeMemberInfo.from(this, method)));
        extractAnnotatedFields(typeClass).forEach(field -> result.add(CafeMemberInfo.from(this, field)));
        result.removeIf(Objects::isNull);
        return result;
    }

//    public Set<CafeMethodInfo> methods(Class<? extends Annotation> annotationType) {
//        return members.stream()
//                .filter(CafeMethodInfo.class::isInstance)
//                .map(CafeMethodInfo.class::cast)
//                .filter(m -> m.getAnnotations().stream()
//                        .anyMatch(a -> a.annotationType().equals(annotationType)))
//                .collect(Collectors.toUnmodifiableSet());
//    }

    public CafeFieldInfo getFieldInfo(String fieldName) {
        return members.stream()
                .filter(CafeMemberInfo::isField)
                .map(cafeMemberInfo -> (CafeFieldInfo) cafeMemberInfo)
                .filter(cafeFieldInfo -> cafeFieldInfo.getField().getName().equals(fieldName))
                .findFirst()
                .orElse(null);
    }


    public Set<CafeMethodInfo> getMethodInfos(String methodName) {
        return Set.of();
    }

    private Constructor<?> extractConstructor() {
        Constructor<?>[] constructors = typeClass.getDeclaredConstructors();
        if (constructors.length != 1) {
            throw new CafeBeansFactoryException("Couldn't determine which constructor of %s to use. Candidates : %n%s"
                    .formatted(typeClass.getName(), StringUtils.join(constructors, ",%n")));
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

    private Set<Method> extractAnnotatedMethods(Class<?> introspectedClass) {
        if (introspectedClass == null || introspectedClass.equals(Object.class)) {
            return Collections.emptySet();
        }
        Set<Method> allMethods = Arrays.stream(introspectedClass.getDeclaredMethods())
                .filter(m -> CafeAnnotationUtils.hasMarker(m, CafeType.class))
                .collect(Collectors.toSet());
        allMethods.addAll(extractAnnotatedMethods(introspectedClass.getSuperclass()));
        return allMethods;
    }

    public BeanTypeKey typeKey() {
        return BeanTypeKey.from(typeClass);
    }

    public Set<BeanTypeKey> provides() {
        return members.stream()
                .map(CafeMemberInfo::provides)
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<BeanTypeKey> dependencies() {
        Set<BeanTypeKey> result = new HashSet<>();
        Optional.ofNullable(constructor()).ifPresent(c -> result.addAll(c.dependencies()));
        methods().forEach(m -> result.addAll(m.dependencies()));
        fields().forEach(f -> result.addAll(f.dependencies()));
        return Collections.unmodifiableSet(result);
    }

    public CafeConstructorInfo constructor() {
        return members.stream()
                .filter(CafeConstructorInfo.class::isInstance)
                .map(CafeConstructorInfo.class::cast)
                .findFirst()
                .orElse(null);
    }

    public Set<CafeMethodInfo> methods() {
        return members.stream()
                .filter(CafeMethodInfo.class::isInstance)
                .map(CafeMethodInfo.class::cast)
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<CafeFieldInfo> fields() {
        return members.stream()
                .filter(CafeFieldInfo.class::isInstance)
                .map(CafeFieldInfo.class::cast)
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean hasDependencies() {
        return !dependencies().isEmpty();
    }

    public boolean isPrototype() {
        return !isSingleton();
    }

    public boolean isSingleton() {
        return CafeAnnotationUtils.isSingleton(typeClass);
    }

    public boolean isImplementing(Class<?> clazz) {
        return clazz.isAssignableFrom(typeClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeClass, members);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CafeClassInfo that)) return false;
        return Objects.equals(typeClass, that.typeClass) && Objects.equals(members, that.members);
    }

    @Override
    public String toString() {
        return typeClass.toString();
    }
}