package org.taranix.cafe.beans.annotations;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.taranix.cafe.beans.annotations.modifiers.CafeOptional;
import org.taranix.cafe.beans.annotations.modifiers.CafePrimary;
import org.taranix.cafe.beans.annotations.modifiers.CafeName;
import org.taranix.cafe.beans.annotations.types.CafeInitable;
import org.taranix.cafe.beans.annotations.types.CafeTaskable;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CafeAnnotationUtils {

    public static final Set<Class<? extends Annotation>> BASE_ANNOTATIONS = Set.of(
            CafeService.class,
            CafeInject.class,
            CafeFactory.class,
            CafeProvider.class,
            CafePostInit.class,
            CafeProperty.class);

    public static boolean containsAnnotation(final Class<?> aClass, final Set<Class<? extends Annotation>> annotations) {
        return annotations
                .stream()
                .anyMatch(annotation -> isAnnotationPresent(aClass, annotation));
    }

    public static boolean isAnnotationPresent(Type type, Class<? extends Annotation> annotation) {
        if (annotation != null && type instanceof Class<?>) {
            return ((Class<?>) type).isAnnotationPresent(annotation);
        }
        return false;
    }

    public static boolean isSingleton(Class<?> clazz) {
        return getScope(clazz).equals(Scope.Singleton);
    }

    public static Scope getScope(Class<?> clazz) {
        return Optional.ofNullable(clazz.getAnnotation(CafeService.class))
                .map(CafeService::scope)
                .orElse(Scope.Singleton);
    }

    public static boolean isPrimary(Method method) {
        return Optional.ofNullable((method).getAnnotation(CafePrimary.class))
                .isPresent();
    }

    public static boolean isPrimary(Class<?> clazz) {
        return Optional.ofNullable((clazz).getAnnotation(CafePrimary.class))
                .isPresent();
    }

    public static boolean isSingleton(Member member) {
        return getScope(member).equals(Scope.Singleton);
    }

    private static Scope getScope(Member member) {
        if (member instanceof Method method) {
            return Optional.ofNullable(method.getAnnotation(CafeProvider.class))
                    .map(CafeProvider::scope)
                    .orElse(Scope.Singleton);
        }

        if (member instanceof Constructor<?>) {
            return Optional.ofNullable(member.getDeclaringClass().getAnnotation(CafeService.class))
                    .map(CafeService::scope)
                    .orElse(Scope.Singleton);
        }

        return Scope.Singleton;
    }

    public static String getMemberName(Member member) {
        if (member instanceof Field field) {
            return Optional.ofNullable(field.getAnnotation(CafeName.class))
                    .map(CafeName::value)
                    .orElse(StringUtils.EMPTY);
        }

        if (member instanceof Method method) {
            return Optional.ofNullable(method.getAnnotation(CafeName.class))
                    .map(CafeName::value)
                    .orElse(StringUtils.EMPTY);
        }

        return StringUtils.EMPTY;
    }

    public static Set<Annotation> getAnnotations(Class<?> clz) {
        return Arrays.stream(clz.getAnnotations()).collect(Collectors.toSet());
    }

    public static <T extends Annotation> T getAnnotationByType(Class<?> clz, Class<T> annotationType) {
        return clz.getAnnotation(annotationType);
    }

    public static List<Method> getMethodsAnnotatedBy(Class<?> clz, Class<? extends Annotation> annotationType) {
        List<Method> result = new ArrayList<>(Arrays.stream(clz.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(annotationType))
                .toList());

        if (clz.getSuperclass() != null) {
            result.addAll(getMethodsAnnotatedBy(clz.getSuperclass(), annotationType));
        }
        return result;
    }

    public static boolean isOptional(Field field) {
        return Optional.ofNullable((field).getAnnotation(CafeOptional.class))
                .isPresent();
    }

    public static boolean hasTaskableMarker(Method method) {
        return Arrays.stream(method.getAnnotations()).anyMatch(annotation -> isAnnotationExtend(annotation, CafeTaskable.class));
    }

    public static boolean hasInitableMarker(Executable executable){
        return Arrays.stream(executable.getAnnotations()).anyMatch(annotation -> isAnnotationExtend(annotation, CafeInitable.class));
    }

    public static boolean hasInitableMarker(Field field){
        return Arrays.stream(field.getAnnotations()).anyMatch(annotation -> isAnnotationExtend(annotation, CafeInitable.class));
    }


    public static boolean isAnnotationExtend(Annotation annotation, Class<? extends Annotation> annotationClass){
        if( annotation.annotationType().isAnnotationPresent(annotationClass)){
            return true;
        }
        return Arrays.stream(annotation.getClass().getAnnotations()).anyMatch(a -> a.annotationType().isAnnotationPresent(annotationClass));

    }
}
