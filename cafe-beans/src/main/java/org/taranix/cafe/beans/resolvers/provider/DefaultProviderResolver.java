package org.taranix.cafe.beans.resolvers.provider;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.descriptors.CafeMemberInfo;
import org.taranix.cafe.beans.descriptors.CafeMethodInfo;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

@Slf4j
public class DefaultProviderResolver implements CafeProviderResolver {
    @Override
    public Object resolve(CafeMemberInfo providerInfo, CafeBeansFactory cafeBeansFactory) {
        log.debug("Resolving {}", providerInfo);

        if (providerInfo.isConstructor()) {
            //For constructor as provider we need to trigger class resolver
            // NOTE: singleton class will be persisted, prototype not.
            return cafeBeansFactory.getResolvers()
                    .findClassResolver(providerInfo.getCafeClassInfo())
                    .resolve(providerInfo.getCafeClassInfo(), cafeBeansFactory);
        }

        if (providerInfo.isMethod()) {
            // Find existing owner of the method or instantiate it
            BeanTypeKey classBeanType = providerInfo.getOwnerClassTypeKey();
            CafeMethodInfo methodInfo = (CafeMethodInfo) providerInfo;

            Object instance = cafeBeansFactory.getBean(classBeanType);

            // In case when method's Class is singleton and this class was not resolved,
            // then we must check if method was not resolved during resolving the class
            // otherwise we direct resolve method
            if (cafeBeansFactory.isMethodResolved(methodInfo)) {
                log.debug("Method value's was resolved during resolving the class");
                if (methodInfo.getMethodReturnTypeKey().isCollection()) {
                    return cafeBeansFactory.getAllResolved(methodInfo.getMethodReturnTypeKey());
                }
                return cafeBeansFactory.getResolved(methodInfo);
            } else {
                log.debug("Method value's was not resolved during resolving the class.");
                return cafeBeansFactory.getResolvers()
                        .findMethodResolver(methodInfo)
                        .resolve(instance, methodInfo, cafeBeansFactory);
            }

        }
        return null;
    }

    @Override
    public boolean isApplicable(CafeMemberInfo memberInfo) {
        return !memberInfo.isField();
    }
}
