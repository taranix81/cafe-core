package org.taranix.cafe.beans.services;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.annotations.classes.CafeService;
import org.taranix.cafe.beans.converters.CafeConverter;
import org.taranix.cafe.beans.metadata.CafeClass;
import org.taranix.cafe.beans.metadata.CafeMember;
import org.taranix.cafe.beans.metadata.CafeMetadataRegistry;
import org.taranix.cafe.beans.resolvers.metadata.CafeClassResolver;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Slf4j
public class CafeOrderedBeansService {

    private final CafeMetadataRegistry cafeMetadataRegistry;

    private CafeOrderedBeansService(CafeMetadataRegistry cafeMetadataRegistry) {
        this.cafeMetadataRegistry = cafeMetadataRegistry;

    }

    public static CafeOrderedBeansService from(CafeMetadataRegistry cafeMetadataRegistry) {
        return new CafeOrderedBeansService(cafeMetadataRegistry);
    }

    public List<CafeClass> orderedClasses() {
        return cafeMetadataRegistry.getCafeClassMetadata().stream()
                .map(this::calculateClassIndex)
                .sorted(Comparator.comparing(IndexedClass::dependencyDepth))
                .map(IndexedClass::classDescriptor)
                .toList();
    }

    public List<CafeMember> orderedMembers() {
        return allMembers().stream()
                .map(this::calculateIndex)
                .sorted(Comparator.comparing(IndexedMember::dependencyDepth))
                .map(IndexedMember::memberDescriptor)
                .toList();

    }

    private IndexedMember calculateIndex(final CafeMember cafeMember) {
        return new IndexedMember(cafeMember, calculateDepth(cafeMember));
    }

    private IndexedClass calculateClassIndex(final CafeClass cafeClass) {
        return new IndexedClass(cafeClass, calculateDepth(cafeClass));
    }

    private int calculateDepth(final CafeClass cafeClass) {
        if (cafeClass.hasDependencies()) {
            int dependenciesAmount = cafeClass.getRequiredTypes().size();
            int dependenciesDepth = getDependenciesDepth(cafeClass);
            int offsetDepth = offsetDepth(cafeClass);
            log.trace("Depth for {} = {}", cafeClass, dependenciesAmount + offsetDepth + dependenciesDepth);
            return dependenciesAmount + offsetDepth + dependenciesDepth;
        } else {
            return offsetDepth(cafeClass);
        }
    }

    private Integer getDependenciesDepth(CafeClass cafeClass) {
        return cafeMetadataRegistry.getClassDependencyRegistry().providers(cafeClass)
                .stream()
                .map(this::calculateDepth)
                .reduce(0, Integer::sum);
    }

    private int calculateDepth(final CafeMember cafeMember) {
        if (cafeMember.hasDependencies()) {
            return cafeMember.getRequiredTypeKeys().size() +
                    cafeMetadataRegistry.getMemberDependencyRegistry().providers(cafeMember)
                            .stream()
                            .map(this::calculateDepth)
                            .reduce(0, Integer::sum);
        } else {
            return 0;
        }
    }

    private int offsetDepth(CafeClass cafeClass) {
        //Standard components without offset
        if (cafeClass.getRootClassAnnotation(CafeService.class) != null) {
            return 0;
        }

        //Custom and standard converters without offset
        if (cafeClass.isImplementing(CafeConverter.class)) {
            return 0;
        }

        //Custom resolvers without offset
        if (cafeClass.isImplementing(CafeClassResolver.class)) {
            return 0;
        }

        //custom component with offset of custom resolvers
        return cafeMetadataRegistry.getCafeClassMetadata().stream()
                .filter(ccd -> ccd.isImplementing(CafeClassResolver.class))
                .map(this::calculateDepth)
                .reduce(0, Integer::sum);
    }

    private Set<CafeMember> allMembers() {
        return cafeMetadataRegistry.allMembers();
    }


    private record IndexedMember(CafeMember memberDescriptor, int dependencyDepth) {

    }

    private record IndexedClass(CafeClass classDescriptor, int dependencyDepth) {

    }

}
