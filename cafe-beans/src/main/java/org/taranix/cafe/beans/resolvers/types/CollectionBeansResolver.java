package org.taranix.cafe.beans.resolvers.types;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.exceptions.CollectionTypeResolverException;
import org.taranix.cafe.beans.metadata.members.CafeMemberInfo;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class CollectionBeansResolver implements CafeBeanResolver {
    @Override
    public Object resolve(BeanTypeKey typeKey, CafeBeansFactory beansFactory) {
        log.debug("Resolving collection :{}", typeKey);
        Type[] collectionActualParameters = typeKey.getActualParameters();

        if (collectionActualParameters.length == 0) {
            throw new CollectionTypeResolverException("No parameters for %s".formatted(typeKey.getType()));
        }

        if (collectionActualParameters.length > 1) {
            throw new CollectionTypeResolverException("To many parameters for %s".formatted(typeKey.getType()));
        }

        BeanTypeKey collectionActualParameter = BeanTypeKey.from(collectionActualParameters[0], typeKey.getTypeIdentifier());
        Collection<Object> items = resolveBeansByProvider(beansFactory, collectionActualParameter);

        if (typeKey.isList()) {
            return new ArrayList<>(items);
        } else if (typeKey.isSet()) {
            return new HashSet<>(items);
        } else if (typeKey.isCollection()) {
            return new ArrayList<>(items);
        } else {
            throw new CollectionTypeResolverException("Couldn't determine collection type %s".formatted(typeKey.getType()));
        }
    }

    @Override
    public Object resolveOrNull(BeanTypeKey typeKey, CafeBeansFactory beansFactory) {
        return resolve(typeKey, beansFactory);
    }

    @Override
    public boolean isApplicable(BeanTypeKey typeKey) {
        return typeKey.isCollection();
    }

    public Collection<Object> resolveBeansByProvider(CafeBeansFactory beansFactory, BeanTypeKey typeKey) {
        //Resolve Singletons not yet resolved
        beansFactory.getClassDescriptors().findSingletonProviders(typeKey)
                .forEach(memberInfo -> resolveProvider(beansFactory, memberInfo));
        //Gather all singletons
        Collection<Object> singletons = beansFactory.getAllResolved(typeKey);

        //Resolve prototypes
        Collection<Object> prototypes = beansFactory.getClassDescriptors().findPrototypeProviders(typeKey).stream()
                .map(memberInfo -> resolveProvider(beansFactory, memberInfo))
                .collect(Collectors.toSet());

        return Stream.concat(prototypes.stream(), singletons.stream())
                .collect(Collectors.toSet());
    }

    private Object resolveProvider(CafeBeansFactory beansFactory, CafeMemberInfo memberInfo) {
        return beansFactory.getResolvers()
                .findProviderResolver(memberInfo)
                .resolve(memberInfo, beansFactory);
    }

}
