package org.taranix.cafe.beans;

import com.google.common.reflect.ClassPath;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.taranix.cafe.beans.exceptions.ReflectionUtilsException;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CafeReflectionUtils {

    public static boolean isCollection(Type type) {
        return (isClass(type) && Collection.class.isAssignableFrom((Class<?>) type)) ||
                (isParametrizedType(type) && isCollection(((ParameterizedType) type).getRawType()));
    }

    public static boolean isGenericType(Type type) {
        return isTypeVariable(type) ||
                (isParametrizedType(type) && Arrays.stream(((ParameterizedType) type).getActualTypeArguments())
                        .allMatch(CafeReflectionUtils::isGenericType)
                );
    }

    public static boolean isTypeVariable(Type type) {
        return type instanceof TypeVariable<?>;
    }

    public static boolean isParametrizedType(final Type type) {
        return type instanceof ParameterizedType;
    }

    public static boolean isArray(Type type) {
        return isClass(type) && ((Class<?>) type).isArray();
    }

    public static boolean isClass(Type type) {
        return type instanceof Class<?>;
    }

    public static void setFieldValue(Field field, Object fieldOwner, Object value) {
        try {
            field.setAccessible(true);
            field.set(fieldOwner, value);
        } catch (IllegalAccessException e) {
            throw new ReflectionUtilsException("Couldn't set field %s value %s on object %s:  %n  %s"
                    .formatted(field.getName(), value, fieldOwner.getClass(), e.getCause().getMessage()));
        }
    }

    public static Object getMethodValue(Method method, Object owner, Object... args) {
        try {
            method.setAccessible(true);
            return method.invoke(owner, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ReflectionUtilsException("Couldn't invoke method %s on object %s : %n  %s"
                    .formatted(method.getName()
                            , owner.getClass()
                            , e.getCause().getMessage()));
        }
    }

    public static Set<Type> getAllInterfaces(Class<?> clazz) {
        Set<Type> result = new HashSet<>();
        for (Type typeInterface : clazz.getGenericInterfaces()) {
            result.add(typeInterface);
            if (typeInterface instanceof Class<?> classInterface) {
                result.addAll(getAllInterfaces(classInterface));
            }
        }
        return result;
    }

    public static Set<Type> getAllSuperClasses(Class<?> clazz) {
        Set<Type> result = new HashSet<>();
        if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
            result.add(clazz.getGenericSuperclass());
            result.add(clazz.getSuperclass());
            result.addAll(getAllSuperClasses(clazz.getSuperclass()));
        }
        return result;
    }

    public static Map<TypeVariable<?>, Type> determineTypeArguments(Class<?> cls) {
        Type superClassGeneric = cls.getGenericSuperclass();

        if (isParametrizedType(superClassGeneric)) {
            return TypeUtils.determineTypeArguments(cls, (ParameterizedType) superClassGeneric);
        }
        if (isClass(superClassGeneric) && !superClassGeneric.equals(Object.class)) {
            return determineTypeArguments((Class<?>) superClassGeneric);
        }
        return Map.of();
    }


    public static Type replaceTypeArguments(Type type, Map<TypeVariable<?>, Type> mappings) {
        if (type instanceof ParameterizedType) {
            List<Type> newActualTypes = new ArrayList<>(((ParameterizedType) type).getActualTypeArguments().length);
            for (int i = 0; i < ((ParameterizedType) type).getActualTypeArguments().length; i++) {
                Type arg = ((ParameterizedType) type).getActualTypeArguments()[i];
                if (CafeReflectionUtils.isTypeVariable(arg)) {
                    if (!mappings.containsKey((TypeVariable<?>) arg)) {
                        throw new ReflectionUtilsException("No mappings for %s".formatted(((TypeVariable<?>) arg).getGenericDeclaration()));
                    }

                    Type newType = mappings.get((TypeVariable<?>) arg);
                    if (newType instanceof TypeVariable<?>) {
                        newType = replaceTypeArguments(newType, mappings);
                    }
                    newActualTypes.add(newType);
                } else {
                    newActualTypes.add(arg);
                }
            }
            return TypeUtils.parameterize((Class<?>) ((ParameterizedType) type).getRawType(), newActualTypes.toArray(Type[]::new));
        }

        if (type instanceof TypeVariable<?>) {
            if (!mappings.containsKey((TypeVariable<?>) type)) {
                throw new ReflectionUtilsException("No mappings for %s".formatted(((TypeVariable<?>) type).getGenericDeclaration()));
            }
            Type replacement = mappings.get((TypeVariable<?>) type);
            if (replacement instanceof TypeVariable<?>) {
                return replaceTypeArguments(replacement, mappings);
            }
            return replacement;
        }
        return type;
    }

    public static Type determineFieldType(Field field, Class<?> clazz) {
        Type genericFieldType = field.getGenericType();
        return replaceTypeArguments(genericFieldType, determineTypeArguments(clazz));
    }

    public static Type determineMethodReturnType(Method method, Class<?> clazz) {
        Type returnGenericType = method.getGenericReturnType();
        return replaceTypeArguments(returnGenericType, determineTypeArguments(clazz));
    }

    public static List<Type> determineMethodParameterTypes(Method method, Class<?> clazz) {
        Map<TypeVariable<?>, Type> typeArgs = determineTypeArguments(clazz);
        return Arrays.stream(method.getGenericParameterTypes())
                .map(type -> replaceTypeArguments(type, typeArgs))
                .toList();
    }


    public static List<Type> determineConstructorParameterTypes(Constructor<?> constructor) {
        Map<TypeVariable<?>, Type> typeArgs = CafeReflectionUtils.determineTypeArguments(constructor.getDeclaringClass());
        return Arrays.stream(constructor.getGenericParameterTypes())
                .map(type -> replaceTypeArguments(type, typeArgs))
                .toList();
    }


    public static Object instantiate(Constructor<?> constructor, Object... args) {
        try {
            constructor.setAccessible(true);
            return (constructor.newInstance(args));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ClassCastException(e.getMessage());
        }
    }

    public static ClassLoader getDefault() {
        return Thread.currentThread().getContextClassLoader();
    }

    public static Stream<Class<?>> getAllClassesFromPackage(ClassLoader classLoader, String lookupPackage) {
        try {
            ClassPath classPath = ClassPath.from(classLoader);
            return classPath
                    .getTopLevelClassesRecursive(lookupPackage)
                    .stream()
                    .map(classInfo -> (Class<?>) classInfo.load())
                    .filter(clazz -> !clazz.isInterface() && !clazz.isRecord())
                    .map(aClass -> (Class<?>) aClass);


        } catch (IOException e) {
            throw new ReflectionUtilsException(e.getMessage());
        }
    }

    public static boolean isSelfInstantiable(Type type) {
        return type instanceof Class<?>
                && !((Class<?>) type).isInterface()
                && !((Class<?>) type).isArray()
                && !((Class<?>) type).isRecord()
                && !((Class<?>) type).isEnum();
    }


}
