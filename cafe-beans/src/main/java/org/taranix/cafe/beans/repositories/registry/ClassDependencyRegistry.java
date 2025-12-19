package org.taranix.cafe.beans.repositories.registry;

import org.taranix.cafe.beans.metadata.CafeClass;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.util.HashSet;
import java.util.Set;

public class ClassDependencyRegistry extends AbstractDependencyRegistry<CafeClass> {

    public static ClassDependencyRegistry from(Set<CafeClass> cafeClassMetadata) {
        ClassDependencyRegistry classDependencyRegistry = new ClassDependencyRegistry();

        for (CafeClass descriptor : cafeClassMetadata) {
            cafeClassMetadata.stream()
                    .filter(item -> !item.equals(descriptor))
                    .filter(provider -> containsAtLeastOneElement(descriptor.getRequiredTypes(), provider.getProvidedTypes()))
                    .forEach(provider -> classDependencyRegistry.set(descriptor, provider));
        }
        return classDependencyRegistry;
    }

    public static boolean containsAtLeastOneElement(Set<BeanTypeKey> dependencies, Set<BeanTypeKey> provided) {
        for (BeanTypeKey dependency : dependencies) {
            if (BeanTypeKey.isMatchByTypeOrGenericType(dependency, provided)) {
                return true;
            }
        }
        return false;
    }

    public Set<CafeClass> providers(CafeClass target) {
        return new HashSet<>(getMany(target));
    }
}
