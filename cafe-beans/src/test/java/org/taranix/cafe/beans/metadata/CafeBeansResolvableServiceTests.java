package org.taranix.cafe.beans.metadata;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.metadata.data.ServiceClassInjectable;
import org.taranix.cafe.beans.metadata.data.SubjectClassInjectable;
import org.taranix.cafe.beans.metadata.data.SubjectClassProvider;
import org.taranix.cafe.beans.metadata.data.SubjectClassProviderWCADate;
import org.taranix.cafe.beans.services.CafeBeanDefinitionService;
import org.taranix.cafe.beans.services.CafeBeansResolvableService;

class CafeBeansResolvableServiceTests {


    @Test
    void shouldClassWithoutDependenciesBeResolvable() {
        //given
        CafeBeanDefinitionService cafeBeanDefinitionService = CafeBeanDefinitionService.builder()
                .withClass(SubjectClassProvider.class)
                .build();

        CafeBeansResolvableService resolvableDescriptor = CafeBeansResolvableService.from(cafeBeanDefinitionService);

        //then
        cafeBeanDefinitionService.allMembers().forEach(
                memberDescriptor -> Assertions.assertTrue(resolvableDescriptor.isResolvable(memberDescriptor)));

    }

    @Test
    void shouldClassWithDependenciesBeResolvable() {
        //given
        CafeBeanDefinitionService cafeBeanDefinitionService = CafeBeanDefinitionService.builder()
                .withClass(SubjectClassProvider.class)
                .withClass(SubjectClassInjectable.class)
                .build();


        CafeBeansResolvableService resolvableDescriptor = CafeBeansResolvableService.from(cafeBeanDefinitionService);

        //then
        cafeBeanDefinitionService.findClassInfo(SubjectClassProvider.class).getMembers().forEach(memberDescriptor ->
                Assertions.assertTrue(resolvableDescriptor.isResolvable(memberDescriptor), memberDescriptor.getMember().getName())
        );

        cafeBeanDefinitionService.findClassInfo(SubjectClassInjectable.class).getMembers().forEach(memberDescriptor ->
                Assertions.assertTrue(resolvableDescriptor.isResolvable(memberDescriptor), memberDescriptor.getMember().getName())
        );

    }

    @Test
    void shouldClassWithoutDependenciesNotBeResolvable() {

        //given
        CafeBeanDefinitionService cafeBeanDefinitionService = CafeBeanDefinitionService.builder()
                .withClass(ServiceClassInjectable.class)
                .build();
        CafeBeansResolvableService resolvableDescriptor = CafeBeansResolvableService.from(cafeBeanDefinitionService);

        //then
        cafeBeanDefinitionService.findClassInfo(ServiceClassInjectable.class)
                .fields()
                .forEach(memberDescriptor -> Assertions.assertFalse(resolvableDescriptor.isResolvable(memberDescriptor)));

    }

    @Test
    void shouldDependantSubjectClassBeResolvableAndProviderSubjectClassWithDependencyNot() {

        //given
        CafeBeanDefinitionService cafeBeanDefinitionService = CafeBeanDefinitionService.builder()
                .withClass(SubjectClassInjectable.class)
                .withClass(SubjectClassProviderWCADate.class)
                .build();

        CafeBeansResolvableService resolvableDescriptor = CafeBeansResolvableService.from(cafeBeanDefinitionService);

        //then
        cafeBeanDefinitionService.findClassInfo(SubjectClassInjectable.class)
                .getMembers() //getMembers()
                .forEach(memberDescriptor -> Assertions.assertTrue(resolvableDescriptor.isResolvable(memberDescriptor)));

        //then
        cafeBeanDefinitionService.findClassInfo(SubjectClassProviderWCADate.class)
                .getMembers()
                .forEach(memberDescriptor -> Assertions.assertFalse(resolvableDescriptor.isResolvable(memberDescriptor)));

    }
}
