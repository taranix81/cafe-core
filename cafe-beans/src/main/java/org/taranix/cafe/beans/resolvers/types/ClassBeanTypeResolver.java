package org.taranix.cafe.beans.resolvers.types;

import org.taranix.cafe.beans.annotations.modifiers.CafePrimary;
import org.taranix.cafe.beans.exceptions.CafeBeanResolverException;
import org.taranix.cafe.beans.metadata.CafeMember;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.resolvers.CafeBeansFactory;

import java.util.Optional;
import java.util.Set;

public class ClassBeanTypeResolver implements CafeBeanTypeResolver {
    @Override
    public Object resolve(BeanTypeKey typeKey, CafeBeansFactory beansFactory) {
        return Optional.ofNullable(resolveOrNull(typeKey, beansFactory))
                .orElseThrow(() -> new CafeBeanResolverException("No provider found for %s".formatted(typeKey), typeKey));
    }

    @Override
    public Object resolveOrNull(BeanTypeKey typeKey, CafeBeansFactory beansFactory) {
        Set<CafeMember> providers = beansFactory.getCafeMetadataRegistry().findAnyTypeProviders(typeKey);
        if (providers.isEmpty()) {
            return null;
        }
        if (providers.size() == 1) {
            return resolveProvider(providers.iterator().next(), beansFactory);
        }
        return providers.stream()
                .filter(m -> m.getAnnotationModifiers().contains(CafePrimary.class))
                .findFirst()
                .map(m -> resolveProvider(m, beansFactory))
                .orElseThrow(() -> new CafeBeanResolverException(
                        "Ambiguous providers for %s — annotate one with @CafePrimary".formatted(typeKey), typeKey));
    }


    @Override
    public boolean isApplicable(BeanTypeKey typeKey) {
        return !(typeKey.isArray() || typeKey.isCollection() || typeKey.isOptional());
    }

    private Object resolveProvider(CafeMember memberInfo, CafeBeansFactory beansFactory) {
        return beansFactory.getResolvers()
                .findProviderResolver(memberInfo)
                .resolve(memberInfo, beansFactory);
    }
}
