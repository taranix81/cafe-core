package org.taranix.cafe.beans.repositories.registry;

import org.taranix.cafe.beans.metadata.CafeClassInfo;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.util.HashSet;
import java.util.Set;

public class ClassDependencyResolverRegistry extends AbstractDependencyRegistry<CafeClassInfo> {

    public static ClassDependencyResolverRegistry from(Set<CafeClassInfo> cafeClassInfos) {
        ClassDependencyResolverRegistry classDependencyResolverRegistry = new ClassDependencyResolverRegistry();

        for (CafeClassInfo descriptor : cafeClassInfos) {
            cafeClassInfos.stream()
                    .filter(item -> !item.equals(descriptor))
                    .filter(provider -> containsAtLeastOneElement(descriptor.dependencies(), provider.provides()))
                    .forEach(provider -> classDependencyResolverRegistry.set(descriptor, provider));
        }
        return classDependencyResolverRegistry;
    }

    public static boolean containsAtLeastOneElement(Set<BeanTypeKey> dependencies, Set<BeanTypeKey> provided) {
        for (BeanTypeKey dependency : dependencies) {
            if (BeanTypeKey.isMatchByTypeOrGenericType(dependency, provided)) {
                return true;
            }
        }
        return false;
    }

    public Set<CafeClassInfo> providers(CafeClassInfo target) {
        return new HashSet<>(getMany(target));
    }
}
