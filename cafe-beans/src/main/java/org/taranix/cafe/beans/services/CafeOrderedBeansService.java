package org.taranix.cafe.beans.services;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.annotations.CafeFactory;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.beans.converters.CafeConverter;
import org.taranix.cafe.beans.metadata.CafeBeansDefinitionRegistry;
import org.taranix.cafe.beans.metadata.CafeClassInfo;
import org.taranix.cafe.beans.metadata.members.CafeMemberInfo;
import org.taranix.cafe.beans.resolvers.metadata.CafeClassResolver;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Slf4j
public class CafeOrderedBeansService {

    private final CafeBeansDefinitionRegistry cafeBeansDefinitionRegistry;

    private CafeOrderedBeansService(CafeBeansDefinitionRegistry cafeBeansDefinitionRegistry) {
        this.cafeBeansDefinitionRegistry = cafeBeansDefinitionRegistry;

    }

    public static CafeOrderedBeansService from(CafeBeansDefinitionRegistry cafeBeansDefinitionRegistry) {
        return new CafeOrderedBeansService(cafeBeansDefinitionRegistry);
    }

    public List<CafeClassInfo> orderedClasses() {
        return cafeBeansDefinitionRegistry.getCafeClassInfos().stream()
                .map(this::calculateClassIndex)
                .sorted(Comparator.comparing(IndexedClass::dependencyDepth))
                .map(IndexedClass::classDescriptor)
                .toList();
    }

    public List<CafeMemberInfo> orderedMembers() {
        return allMembers().stream()
                .map(this::calculateIndex)
                .sorted(Comparator.comparing(IndexedMember::dependencyDepth))
                .map(IndexedMember::memberDescriptor)
                .toList();

    }

    private IndexedMember calculateIndex(final CafeMemberInfo cafeMemberInfo) {
        return new IndexedMember(cafeMemberInfo, calculateDepth(cafeMemberInfo));
    }

    private IndexedClass calculateClassIndex(final CafeClassInfo cafeClassInfo) {
        return new IndexedClass(cafeClassInfo, calculateDepth(cafeClassInfo));
    }

    private int calculateDepth(final CafeClassInfo cafeClassInfo) {
        if (cafeClassInfo.hasDependencies()) {
            int dependenciesAmount = cafeClassInfo.dependencies().size();
            int dependenciesDepth = getDependenciesDepth(cafeClassInfo);
            int offsetDepth = offsetDepth(cafeClassInfo);
            log.trace("Depth for {} = {}", cafeClassInfo, dependenciesAmount + offsetDepth + dependenciesDepth);
            return dependenciesAmount + offsetDepth + dependenciesDepth;
        } else {
            return offsetDepth(cafeClassInfo);
        }
    }

    private Integer getDependenciesDepth(CafeClassInfo cafeClassInfo) {
        return cafeBeansDefinitionRegistry.getClassDependencyResolverRegistry().providers(cafeClassInfo)
                .stream()
                .map(this::calculateDepth)
                .reduce(0, Integer::sum);
    }

    private int calculateDepth(final CafeMemberInfo cafeMemberInfo) {
        if (cafeMemberInfo.hasDependencies()) {
            return cafeMemberInfo.dependencies().size() +
                    cafeBeansDefinitionRegistry.getMemberDependencyResolverRegistry().providers(cafeMemberInfo)
                            .stream()
                            .map(this::calculateDepth)
                            .reduce(0, Integer::sum);
        } else {
            return 0;
        }
    }

    private int offsetDepth(CafeClassInfo cafeClassInfo) {
        //Standard components without offset
        if (cafeClassInfo.getClassAnnotation(CafeFactory.class) != null || cafeClassInfo.getClassAnnotation(CafeService.class) != null) {
            return 0;
        }

        //Custom and standard converters without offset
        if (cafeClassInfo.isImplementing(CafeConverter.class)) {
            return 0;
        }

        //Custom resolvers without offset
        if (cafeClassInfo.isImplementing(CafeClassResolver.class)) {
            return 0;
        }

        //custom component with offset of custom resolvers
        return cafeBeansDefinitionRegistry.getCafeClassInfos().stream()
                .filter(ccd -> ccd.isImplementing(CafeClassResolver.class))
                .map(this::calculateDepth)
                .reduce(0, Integer::sum);
    }

    private Set<CafeMemberInfo> allMembers() {
        return cafeBeansDefinitionRegistry.allMembers();
    }


    private record IndexedMember(CafeMemberInfo memberDescriptor, int dependencyDepth) {

    }

    private record IndexedClass(CafeClassInfo classDescriptor, int dependencyDepth) {

    }

}
