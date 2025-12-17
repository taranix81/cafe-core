package org.taranix.cafe.beans.resolvers.types;

import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.exceptions.CafeBeanResolverException;
import org.taranix.cafe.beans.metadata.CafeMemberMetadata;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

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
        Set<CafeMemberMetadata> providers = beansFactory.getCafeBeansRegistry().findAnyTypeProviders(typeKey);
        return providers.stream()
                .findFirst()
                .map(memberInfo -> resolveProvider(memberInfo, beansFactory))
                .orElse(null);
    }


    @Override
    public boolean isApplicable(BeanTypeKey typeKey) {
        return !(typeKey.isArray() || typeKey.isCollection());
    }

    private Object resolveProvider(CafeMemberMetadata memberInfo, CafeBeansFactory beansFactory) {
        return beansFactory.getResolvers()
                .findProviderResolver(memberInfo)
                .resolve(memberInfo, beansFactory);
    }
}
