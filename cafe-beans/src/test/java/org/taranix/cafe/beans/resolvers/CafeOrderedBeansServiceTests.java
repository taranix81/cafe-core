package org.taranix.cafe.beans.resolvers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.metadata.data.*;
import org.taranix.cafe.beans.metadata.members.CafeMemberInfo;
import org.taranix.cafe.beans.services.CafeBeanDefinitionService;
import org.taranix.cafe.beans.services.CafeOrderedBeansService;

import java.util.List;

class CafeOrderedBeansServiceTests {


    @Test
    void shouldProperOrderedForSingleClassWithoutDependencies() {
        //given
        CafeBeanDefinitionService cafeBeanDefinitionService = CafeBeanDefinitionService.builder()
                .withClass(SubjectClassProvider.class)
                .build();
        CafeOrderedBeansService orderDescriptor = CafeOrderedBeansService.from(cafeBeanDefinitionService);

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
        CafeBeanDefinitionService cafeBeanDefinitionService = CafeBeanDefinitionService.builder()
                .withClass(SubjectClassProvider.class)
                .withClass(SubjectClass.class)
                .build();

        CafeOrderedBeansService orderDescriptor = CafeOrderedBeansService.from(cafeBeanDefinitionService);

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
        CafeBeanDefinitionService cafeBeanDefinitionService = CafeBeanDefinitionService.builder()
                .withClass(ServiceClassInjectable.class)
                .withClass(ServiceClass.class)
                .build();
        CafeOrderedBeansService orderDescriptor = CafeOrderedBeansService.from(cafeBeanDefinitionService);

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
        CafeBeanDefinitionService cafeBeanDefinitionService = CafeBeanDefinitionService.builder()
                .withClass(ServiceClassWCAInjectable.class)
                .withClass(ServiceClassWCA.class)
                .withClass(StringProvider.class)
                .build();
        CafeOrderedBeansService orderDescriptor = CafeOrderedBeansService.from(cafeBeanDefinitionService);

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
