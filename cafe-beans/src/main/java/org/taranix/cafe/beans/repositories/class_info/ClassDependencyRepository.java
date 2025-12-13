package org.taranix.cafe.beans.repositories.class_info;

import org.taranix.cafe.beans.descriptors.CafeClassDescriptors;
import org.taranix.cafe.beans.descriptors.CafeClassInfo;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.util.Set;

public class ClassDependencyRepository extends DependencyRepository<CafeClassInfo> {

    public static ClassDependencyRepository from(CafeClassDescriptors cafeClassDescriptors) {
        ClassDependencyRepository classDependencyRepository = new ClassDependencyRepository();

        for (CafeClassInfo descriptor : cafeClassDescriptors.descriptors()) {
            cafeClassDescriptors.descriptors().stream()
                    .filter(item -> !item.equals(descriptor))
                    .filter(provider -> containsAtLeastOneElement(descriptor.dependencies(), provider.provides()))
                    .forEach(provider -> classDependencyRepository.set(descriptor, provider));
        }
        return classDependencyRepository;
    }

    public static boolean containsAtLeastOneElement(Set<BeanTypeKey> dependencies, Set<BeanTypeKey> provided) {
        for (BeanTypeKey dependency : dependencies) {
            if (BeanTypeKey.isMatchByTypeOrGenericType(dependency, provided)) {
                return true;
            }
        }
        return false;
    }
}
