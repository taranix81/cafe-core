package org.taranix.cafe.beans.resolvers;

import org.taranix.cafe.beans.exceptions.CafeBeansFactoryException;
import org.taranix.cafe.beans.metadata.*;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.resolvers.metadata.CafeClassResolver;
import org.taranix.cafe.beans.resolvers.metadata.CafeProviderResolver;
import org.taranix.cafe.beans.resolvers.metadata.DefaultClassResolver;
import org.taranix.cafe.beans.resolvers.metadata.DefaultProviderResolver;
import org.taranix.cafe.beans.resolvers.metadata.constructor.CafeConstructorResolver;
import org.taranix.cafe.beans.resolvers.metadata.constructor.DefaultConstructorResolver;
import org.taranix.cafe.beans.resolvers.metadata.field.CafeFieldResolver;
import org.taranix.cafe.beans.resolvers.metadata.field.DefaultFieldResolver;
import org.taranix.cafe.beans.resolvers.metadata.field.PropertyResolver;
import org.taranix.cafe.beans.resolvers.metadata.method.CafeMethodResolver;
import org.taranix.cafe.beans.resolvers.metadata.method.DefaultMethodResolver;
import org.taranix.cafe.beans.resolvers.types.ArrayBeanTypeResolver;
import org.taranix.cafe.beans.resolvers.types.CafeBeanTypeResolver;
import org.taranix.cafe.beans.resolvers.types.ClassBeanTypeResolver;
import org.taranix.cafe.beans.resolvers.types.CollectionBeanTypeResolver;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class CafeResolvers {

    public static final String NO_RESOLVER_FOUND = "No resolver found for %s";
    public static final String TOO_MANY_RESOLVERS = "Too many resolvers for  %s";
    private final Set<CafeProviderResolver> providerResolvers = new HashSet<>(List.of(
            new DefaultProviderResolver()));

    private final Set<CafeClassResolver> classResolvers = new HashSet<>(List.of(
            new DefaultClassResolver()));

    private final Set<CafeConstructorResolver> constructorResolvers = new HashSet<>(List.of(
            new DefaultConstructorResolver()));
    private final Set<CafeFieldResolver> fieldResolvers = new HashSet<>(List.of(
            new DefaultFieldResolver(),
            new PropertyResolver()));

    private final Set<CafeMethodResolver> methodResolvers = new HashSet<>(List.of(
            new DefaultMethodResolver()));
    private final Set<CafeBeanTypeResolver> beanTypekeyResolvers = new HashSet<>(List.of(
            new ClassBeanTypeResolver(),
            new ArrayBeanTypeResolver(),
            new CollectionBeanTypeResolver()));

    public CafeMethodResolver findMethodResolver(CafeMethodMetadata methodDescriptor) {
        Set<CafeMethodResolver> matched = findMethodResolvers(methodDescriptor);
        if (matched.size() > 1) {
            throw new CafeBeansFactoryException(TOO_MANY_RESOLVERS.formatted(methodDescriptor.getMember()));
        }

        return matched.stream()
                .findFirst()
                .orElseThrow(() -> new CafeBeansFactoryException(NO_RESOLVER_FOUND.formatted(methodDescriptor.getMember())));
    }

    private Set<CafeMethodResolver> findMethodResolvers(CafeMethodMetadata methodDescriptor) {
        return methodResolvers.stream()
                .map(CafeMethodResolver.class::cast)
                .filter(resolver -> resolver.isApplicable(methodDescriptor))
                .filter(resolver -> methodDescriptor.getAnnotations()
                        .stream()
                        .anyMatch(annotation -> resolver.supports(annotation.annotationType())))
                .collect(Collectors.toSet());
    }

    public CafeClassResolver findClassResolver(CafeClassMetadata descriptor) {
        Set<CafeClassResolver> matched = findClassResolvers(descriptor);
        if (matched.size() > 1) {
            throw new CafeBeansFactoryException(TOO_MANY_RESOLVERS.formatted(descriptor));
        }

        return matched.stream()
                .findFirst()
                .orElseThrow(() -> new CafeBeansFactoryException(NO_RESOLVER_FOUND.formatted(descriptor)));
    }


    private Set<CafeClassResolver> findClassResolvers(CafeClassMetadata classDescriptor) {
        return classResolvers.stream()
                .filter(resolver -> resolver.isApplicable(classDescriptor))
                .filter(resolver -> classDescriptor.getRootClassAnnotations()
                        .stream()
                        .anyMatch(annotation -> resolver.supports(annotation.annotationType())))
                .collect(Collectors.toSet());
    }

    public CafeConstructorResolver findConstructorResolver(CafeConstructorMetadata constructorDescriptor) {
        Set<CafeConstructorResolver> matched = findConstructorResolvers(constructorDescriptor);
        if (matched.size() > 1) {
            throw new CafeBeansFactoryException(TOO_MANY_RESOLVERS.formatted(constructorDescriptor.getMember()));
        }

        return matched.stream()
                .findFirst()
                .orElseThrow(() -> new CafeBeansFactoryException(NO_RESOLVER_FOUND.formatted(constructorDescriptor.getMember())));
    }

    private Set<CafeConstructorResolver> findConstructorResolvers(CafeConstructorMetadata cafeAnnotationConstructorInfo) {
        return constructorResolvers.stream()
                .map(CafeConstructorResolver.class::cast)
                .filter(resolver -> resolver.isApplicable(cafeAnnotationConstructorInfo))
                .filter(resolver -> cafeAnnotationConstructorInfo.getAnnotations().stream().anyMatch(annotation -> resolver.supports(annotation.annotationType()))
                        || resolver.supports(null))
                .collect(Collectors.toSet());
    }

    public CafeFieldResolver findFieldResolver(CafeFieldMetadata fieldDescriptor) {
        Set<CafeFieldResolver> matched = findFieldResolvers(fieldDescriptor);
        if (matched.size() > 1) {
            throw new CafeBeansFactoryException(TOO_MANY_RESOLVERS.formatted(fieldDescriptor.getMember()));
        }

        return matched.stream()
                .findFirst()
                .orElseThrow(() -> new CafeBeansFactoryException(NO_RESOLVER_FOUND.formatted(fieldDescriptor.getMember())));
    }

    private Set<CafeFieldResolver> findFieldResolvers(CafeFieldMetadata fieldDescriptor) {
        return fieldResolvers.stream()
                .map(CafeFieldResolver.class::cast)
                .filter(resolver -> resolver.isApplicable(fieldDescriptor))
                .filter(resolver -> fieldDescriptor.getAnnotations()
                        .stream()
                        .anyMatch(annotation -> resolver.supports(annotation.annotationType())))
                .collect(Collectors.toSet());
    }

    public CafeBeanTypeResolver findBeanTypekeyResolver(BeanTypeKey typeKey) {
        Set<CafeBeanTypeResolver> matched = beanTypekeyResolvers.stream()
                .filter(cafeBeanTypekeyResolver -> cafeBeanTypekeyResolver.isApplicable(typeKey))
                .collect(Collectors.toSet());

        if (matched.size() > 1) {
            throw new CafeBeansFactoryException(TOO_MANY_RESOLVERS.formatted(typeKey));
        }
        return matched.stream()
                .findFirst()
                .orElseThrow(() -> new CafeBeansFactoryException(NO_RESOLVER_FOUND.formatted(typeKey)));
    }

    public CafeProviderResolver findProviderResolver(CafeMemberMetadata memberInfo) {
        return providerResolvers.stream()
                .filter(cafeProviderResolver -> cafeProviderResolver.isApplicable(memberInfo))
                .findFirst()
                .orElseThrow(() -> new CafeBeansFactoryException(NO_RESOLVER_FOUND.formatted(memberInfo)));
    }

    public void add(CafeClassResolver... classResolvers) {
        this.classResolvers.addAll(Set.of(classResolvers));
    }

    public void add(CafeConstructorResolver... constructorResolvers) {
        this.constructorResolvers.addAll(Set.of(constructorResolvers));
    }

    public void add(CafeFieldResolver... fieldResolvers) {
        this.fieldResolvers.addAll(Set.of(fieldResolvers));
    }

    public void add(CafeMethodResolver... methodResolvers) {
        this.methodResolvers.addAll(Set.of(methodResolvers));
    }

    public void add(CafeBeanTypeResolver... cafeBeanTypeResolvers) {
        this.beanTypekeyResolvers.addAll(Set.of(cafeBeanTypeResolvers));
    }
}

