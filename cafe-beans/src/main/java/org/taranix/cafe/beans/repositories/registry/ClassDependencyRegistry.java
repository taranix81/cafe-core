package org.taranix.cafe.beans.repositories.registry;

import org.taranix.cafe.beans.metadata.CafeClassMetadata;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.util.HashSet;
import java.util.Set;

public class ClassDependencyRegistry extends AbstractDependencyRegistry<CafeClassMetadata> {

    public static ClassDependencyRegistry from(Set<CafeClassMetadata> cafeClassMetadata) {
        ClassDependencyRegistry classDependencyRegistry = new ClassDependencyRegistry();

        for (CafeClassMetadata descriptor : cafeClassMetadata) {
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

    public Set<CafeClassMetadata> providers(CafeClassMetadata target) {
        return new HashSet<>(getMany(target));
    }
}
