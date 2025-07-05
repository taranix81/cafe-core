package org.taranix.cafe.beans.descriptors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.annotations.CafeAnnotationUtils;
import org.taranix.cafe.beans.descriptors.data.*;
import org.taranix.cafe.beans.resolvers.CafeOrderedBeansService;

import java.util.List;

class CafeOrderedBeansServiceTests {


    @Test
    void shouldProperOrderedForSingleClassWithoutDependencies() {
        //given
        CafeClassDescriptors cafeClassDescriptors = CafeClassDescriptors.builder()
                .withAnnotations(CafeAnnotationUtils.BASE_ANNOTATIONS)
                .withClass(SubjectClassProvider.class)
                .build();
        CafeOrderedBeansService orderDescriptor = CafeOrderedBeansService.from(cafeClassDescriptors);

        //when
        List<CafeMemberInfo> ordered = orderDescriptor.orderedMembers();

        //then
        Assertions.assertEquals(2, ordered.size());
        Assertions.assertTrue(ordered.get(0).isConstructor());
        Assertions.assertTrue(ordered.get(1).isMethod());
    }

    @Test
    void shouldProperOrderedForSingleClassWithDependencyToProviderClass() {
        //given
        CafeClassDescriptors cafeClassDescriptors = CafeClassDescriptors.builder()
                .withAnnotations(CafeAnnotationUtils.BASE_ANNOTATIONS)
                .withClass(SubjectClassProvider.class)
                .withClass(SubjectClass.class)
                .build();

        CafeOrderedBeansService orderDescriptor = CafeOrderedBeansService.from(cafeClassDescriptors);

        //when
        List<CafeMemberInfo> ordered = orderDescriptor.orderedMembers();

        //then
        Assertions.assertEquals(3, ordered.size());
        Assertions.assertTrue(ordered.get(0).isConstructor());
        Assertions.assertTrue(ordered.get(1).isConstructor());
        Assertions.assertTrue(ordered.get(2).isMethod());

    }

    @Test
    void shouldProperOrderedForSingleClassWithDependencyToServiceClass() {
        //given
        CafeClassDescriptors cafeClassDescriptors = CafeClassDescriptors.builder()
                .withAnnotations(CafeAnnotationUtils.BASE_ANNOTATIONS)
                .withClass(ServiceClassInjectable.class)
                .withClass(ServiceClass.class)
                .build();
        CafeOrderedBeansService orderDescriptor = CafeOrderedBeansService.from(cafeClassDescriptors);

        //when
        List<CafeMemberInfo> ordered = orderDescriptor.orderedMembers();

        //then
        Assertions.assertEquals(3, ordered.size());
        Assertions.assertTrue(ordered.get(0).isConstructor());
        Assertions.assertTrue(ordered.get(1).isConstructor());
        Assertions.assertTrue(ordered.get(2).isField());

    }

    @Test
    void shouldProperOrderedForSingleClassWithDependencyToServiceClassWithConstructorParameter() {
        //given
        CafeClassDescriptors cafeClassDescriptors = CafeClassDescriptors.builder()
                .withAnnotations(CafeAnnotationUtils.BASE_ANNOTATIONS)
                .withClass(ServiceClassWCAInjectable.class)
                .withClass(ServiceClassWCA.class)
                .withClass(StringProvider.class)
                .build();
        CafeOrderedBeansService orderDescriptor = CafeOrderedBeansService.from(cafeClassDescriptors);

        //when
        List<CafeMemberInfo> ordered = orderDescriptor.orderedMembers();

        //then
        Assertions.assertEquals(5, ordered.size());
        Assertions.assertTrue(ordered.get(0).isConstructor());
        Assertions.assertTrue(ordered.get(1).isConstructor());
        Assertions.assertTrue(ordered.get(2).isMethod());
        Assertions.assertTrue(ordered.get(3).isConstructor());
        Assertions.assertTrue(ordered.get(4).isField());
    }
}
