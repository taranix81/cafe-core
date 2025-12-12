package org.taranix.cafe.beans.resolvers;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.annotations.CafeFactory;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.beans.converters.CafeConverter;
import org.taranix.cafe.beans.descriptors.CafeBeansDependencyService;
import org.taranix.cafe.beans.descriptors.CafeClassDescriptors;
import org.taranix.cafe.beans.descriptors.CafeClassDescriptor;
import org.taranix.cafe.beans.descriptors.CafeMemberInfo;
import org.taranix.cafe.beans.resolvers.classInfo.CafeClassResolver;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Slf4j
public class CafeOrderedBeansService {

    private final CafeClassDescriptors cafeClassDescriptors;
    private final CafeBeansDependencyService dependencyDescriptor;

    private CafeOrderedBeansService(CafeClassDescriptors cafeClassDescriptors) {
        this.cafeClassDescriptors = cafeClassDescriptors;
        this.dependencyDescriptor = CafeBeansDependencyService.from(cafeClassDescriptors);
    }

    public static CafeOrderedBeansService from(CafeClassDescriptors cafeClassDescriptors) {
        return new CafeOrderedBeansService(cafeClassDescriptors);
    }

    public List<CafeClassDescriptor> orderedClasses() {
        if (!dependencyDescriptor.hasCycleBetweenClasses()) {
            return cafeClassDescriptors.descriptors().stream()
                    .map(this::calculateClassIndex)
                    .sorted(Comparator.comparing(IndexedClass::dependencyDepth))
                    .map(IndexedClass::classDescriptor)
                    .toList();
        }

        return List.of();
    }

    public List<CafeMemberInfo> orderedMembers() {

        if (!dependencyDescriptor.hasCycleBetweenMembers()) {
            return allMembers().stream()
                    .map(this::calculateIndex)
                    .sorted(Comparator.comparing(IndexedMember::dependencyDepth))
                    .map(IndexedMember::memberDescriptor)
                    .toList();
        }
        return List.of();
    }

    private IndexedMember calculateIndex(final CafeMemberInfo cafeMemberInfo) {
        return new IndexedMember(cafeMemberInfo, calculateDepth(cafeMemberInfo));
    }

    private IndexedClass calculateClassIndex(final CafeClassDescriptor cafeClassDescriptor) {
        return new IndexedClass(cafeClassDescriptor, calculateDepth(cafeClassDescriptor));
    }

    private int calculateDepth(final CafeClassDescriptor cafeClassDescriptor) {
        if (cafeClassDescriptor.hasDependencies()) {
            int dependenciesAmount = cafeClassDescriptor.dependencies().size();
            int dependenciesDepth = getDependenciesDepth(cafeClassDescriptor);
            int offsetDepth = offsetDepth(cafeClassDescriptor);
            log.trace("Depth for {} = {}", cafeClassDescriptor, dependenciesAmount + offsetDepth + dependenciesDepth);
            return dependenciesAmount + offsetDepth + dependenciesDepth;
        } else {
            return offsetDepth(cafeClassDescriptor);
        }
    }

    private Integer getDependenciesDepth(CafeClassDescriptor cafeClassDescriptor) {
        return dependencyDescriptor.providersForClass(cafeClassDescriptor)
                .stream()
                .map(this::calculateDepth)
                .reduce(0, Integer::sum);
    }

    private int calculateDepth(final CafeMemberInfo cafeMemberInfo) {
        if (cafeMemberInfo.hasDependencies()) {
            return cafeMemberInfo.dependencies().size() +
                    dependencyDescriptor.providers(cafeMemberInfo)
                            .stream()
                            .map(this::calculateDepth)
                            .reduce(0, Integer::sum);
        } else {
            return 0;
        }
    }

    private int offsetDepth(CafeClassDescriptor cafeClassDescriptor) {
        //Standard components without offset
        if (cafeClassDescriptor.getClassAnnotation(CafeFactory.class) != null || cafeClassDescriptor.getClassAnnotation(CafeService.class) != null) {
            return 0;
        }

        //Custom and standard converters without offset
        if (cafeClassDescriptor.isImplementing(CafeConverter.class)) {
            return 0;
        }

        //Custom resolvers without offset
        if (cafeClassDescriptor.isImplementing(CafeClassResolver.class)) {
            return 0;
        }

        //custom component with offset of custom resolvers
        return cafeClassDescriptors.descriptors().stream()
                .filter(ccd -> ccd.isImplementing(CafeClassResolver.class))
                .map(this::calculateDepth)
                .reduce(0, Integer::sum);
    }

    private Set<CafeMemberInfo> allMembers() {
        return cafeClassDescriptors.allMembers();
    }


    private record IndexedMember(CafeMemberInfo memberDescriptor, int dependencyDepth) {

    }

    private record IndexedClass(CafeClassDescriptor classDescriptor, int dependencyDepth) {

    }

}
