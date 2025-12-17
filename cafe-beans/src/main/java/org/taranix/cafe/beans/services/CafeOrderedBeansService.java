package org.taranix.cafe.beans.services;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.annotations.CafeFactory;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.beans.converters.CafeConverter;
import org.taranix.cafe.beans.metadata.CafeBeansRegistry;
import org.taranix.cafe.beans.metadata.CafeClassMetadata;
import org.taranix.cafe.beans.metadata.CafeMemberMetadata;
import org.taranix.cafe.beans.resolvers.metadata.CafeClassResolver;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Slf4j
public class CafeOrderedBeansService {

    private final CafeBeansRegistry cafeBeansRegistry;

    private CafeOrderedBeansService(CafeBeansRegistry cafeBeansRegistry) {
        this.cafeBeansRegistry = cafeBeansRegistry;

    }

    public static CafeOrderedBeansService from(CafeBeansRegistry cafeBeansRegistry) {
        return new CafeOrderedBeansService(cafeBeansRegistry);
    }

    public List<CafeClassMetadata> orderedClasses() {
        return cafeBeansRegistry.getCafeClassMetadata().stream()
                .map(this::calculateClassIndex)
                .sorted(Comparator.comparing(IndexedClass::dependencyDepth))
                .map(IndexedClass::classDescriptor)
                .toList();
    }

    public List<CafeMemberMetadata> orderedMembers() {
        return allMembers().stream()
                .map(this::calculateIndex)
                .sorted(Comparator.comparing(IndexedMember::dependencyDepth))
                .map(IndexedMember::memberDescriptor)
                .toList();

    }

    private IndexedMember calculateIndex(final CafeMemberMetadata cafeMemberMetadata) {
        return new IndexedMember(cafeMemberMetadata, calculateDepth(cafeMemberMetadata));
    }

    private IndexedClass calculateClassIndex(final CafeClassMetadata cafeClassMetadata) {
        return new IndexedClass(cafeClassMetadata, calculateDepth(cafeClassMetadata));
    }

    private int calculateDepth(final CafeClassMetadata cafeClassMetadata) {
        if (cafeClassMetadata.hasDependencies()) {
            int dependenciesAmount = cafeClassMetadata.getRequiredTypes().size();
            int dependenciesDepth = getDependenciesDepth(cafeClassMetadata);
            int offsetDepth = offsetDepth(cafeClassMetadata);
            log.trace("Depth for {} = {}", cafeClassMetadata, dependenciesAmount + offsetDepth + dependenciesDepth);
            return dependenciesAmount + offsetDepth + dependenciesDepth;
        } else {
            return offsetDepth(cafeClassMetadata);
        }
    }

    private Integer getDependenciesDepth(CafeClassMetadata cafeClassMetadata) {
        return cafeBeansRegistry.getClassDependencyRegistry().providers(cafeClassMetadata)
                .stream()
                .map(this::calculateDepth)
                .reduce(0, Integer::sum);
    }

    private int calculateDepth(final CafeMemberMetadata cafeMemberMetadata) {
        if (cafeMemberMetadata.hasDependencies()) {
            return cafeMemberMetadata.getRequiredTypes().size() +
                    cafeBeansRegistry.getMemberDependencyRegistry().providers(cafeMemberMetadata)
                            .stream()
                            .map(this::calculateDepth)
                            .reduce(0, Integer::sum);
        } else {
            return 0;
        }
    }

    private int offsetDepth(CafeClassMetadata cafeClassMetadata) {
        //Standard components without offset
        if (cafeClassMetadata.getRootClassAnnotation(CafeFactory.class) != null || cafeClassMetadata.getRootClassAnnotation(CafeService.class) != null) {
            return 0;
        }

        //Custom and standard converters without offset
        if (cafeClassMetadata.isImplementing(CafeConverter.class)) {
            return 0;
        }

        //Custom resolvers without offset
        if (cafeClassMetadata.isImplementing(CafeClassResolver.class)) {
            return 0;
        }

        //custom component with offset of custom resolvers
        return cafeBeansRegistry.getCafeClassMetadata().stream()
                .filter(ccd -> ccd.isImplementing(CafeClassResolver.class))
                .map(this::calculateDepth)
                .reduce(0, Integer::sum);
    }

    private Set<CafeMemberMetadata> allMembers() {
        return cafeBeansRegistry.allMembers();
    }


    private record IndexedMember(CafeMemberMetadata memberDescriptor, int dependencyDepth) {

    }

    private record IndexedClass(CafeClassMetadata classDescriptor, int dependencyDepth) {

    }

}
