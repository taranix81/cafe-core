package org.taranix.cafe.beans.resolvers;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.descriptors.CafeClassDescriptors;
import org.taranix.cafe.beans.descriptors.CafeMemberInfo;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.util.Set;

@Slf4j
public class CafeBeansResolvableService {

    private final CafeClassDescriptors classDescriptors;

    private CafeBeansResolvableService(CafeClassDescriptors classDescriptors) {
        this.classDescriptors = classDescriptors;
    }

    public static CafeBeansResolvableService from(final CafeClassDescriptors classDescriptors) {
        return new CafeBeansResolvableService(classDescriptors);
    }

    public boolean isResolvable(CafeMemberInfo cafeMemberInfo) {
        return notResolvableType(cafeMemberInfo) == null;
    }


    public BeanTypeKey notResolvableType(CafeMemberInfo cafeMemberInfo) {
        if (cafeMemberInfo.isOptional() || !cafeMemberInfo.isInitable()) {
            return null;
        }

        if (cafeMemberInfo.hasDependencies()) {
            Set<BeanTypeKey> providedTypeKeys = classDescriptors.getDependency()
                    .providedTypeKeys(cafeMemberInfo);

            for (BeanTypeKey dependency : cafeMemberInfo.dependencies()) {
                if (!BeanTypeKey.isMatchByTypeOrGenericType(dependency, providedTypeKeys)) {
                    log.debug("Not resolvable : {}.{} (dependency={})", cafeMemberInfo.declaringClass(), cafeMemberInfo.getMember().getName(), dependency);
                    return dependency;
                }
            }
        }
        return null;
    }

}
