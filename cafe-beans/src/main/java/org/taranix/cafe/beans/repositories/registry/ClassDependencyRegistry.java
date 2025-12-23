package org.taranix.cafe.beans.repositories.registry;

import org.taranix.cafe.beans.metadata.CafeClass;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.util.HashSet;
import java.util.Set;

public class ClassDependencyRegistry extends AbstractDependencyRegistry<CafeClass> {

    public static ClassDependencyRegistry from(Set<CafeClass> cafeClassMetadata) {
        ClassDependencyRegistry classDependencyRegistry = new ClassDependencyRegistry();

        for (CafeClass cafeClass : cafeClassMetadata) {
            cafeClassMetadata.stream()
                    .filter(item -> !item.equals(cafeClass))
                    .filter(provider -> containsAtLeastOneElement(cafeClass.getRequiredTypes(), provider.getProvidedTypes()))
                    .forEach(provider -> classDependencyRegistry.set(cafeClass, provider));
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
