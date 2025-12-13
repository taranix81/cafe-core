package org.taranix.cafe.beans.descriptors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.descriptors.data.ServiceClassInjectable;
import org.taranix.cafe.beans.descriptors.data.SubjectClassInjectable;
import org.taranix.cafe.beans.descriptors.data.SubjectClassProvider;
import org.taranix.cafe.beans.descriptors.data.SubjectClassProviderWCADate;
import org.taranix.cafe.beans.resolvers.CafeBeansResolvableService;

class CafeBeansResolvableServiceTests {


    @Test
    void shouldClassWithoutDependenciesBeResolvable() {
        //given
        CafeClassDescriptors cafeClassDescriptors = CafeClassDescriptors.builder()
                .withClass(SubjectClassProvider.class)
                .build();

        CafeBeansResolvableService resolvableDescriptor = CafeBeansResolvableService.from(cafeClassDescriptors);

        //then
        cafeClassDescriptors.allMembers().forEach(
                memberDescriptor -> Assertions.assertTrue(resolvableDescriptor.isResolvable(memberDescriptor)));

    }

    @Test
    void shouldClassWithDependenciesBeResolvable() {
        //given
        CafeClassDescriptors cafeClassDescriptors = CafeClassDescriptors.builder()
                .withClass(SubjectClassProvider.class)
                .withClass(SubjectClassInjectable.class)
                .build();


        CafeBeansResolvableService resolvableDescriptor = CafeBeansResolvableService.from(cafeClassDescriptors);

        //then
        cafeClassDescriptors.descriptor(SubjectClassProvider.class).getMembers().forEach(memberDescriptor ->
                Assertions.assertTrue(resolvableDescriptor.isResolvable(memberDescriptor), memberDescriptor.getMember().getName())
        );

        cafeClassDescriptors.descriptor(SubjectClassInjectable.class).getMembers().forEach(memberDescriptor ->
                Assertions.assertTrue(resolvableDescriptor.isResolvable(memberDescriptor), memberDescriptor.getMember().getName())
        );

    }

    @Test
    void shouldClassWithoutDependenciesNotBeResolvable() {

        //given
        CafeClassDescriptors cafeClassDescriptors = CafeClassDescriptors.builder()
                .withClass(ServiceClassInjectable.class)
                .build();
        CafeBeansResolvableService resolvableDescriptor = CafeBeansResolvableService.from(cafeClassDescriptors);

        //then
        cafeClassDescriptors.descriptor(ServiceClassInjectable.class)
                .fields()
                .forEach(memberDescriptor -> Assertions.assertFalse(resolvableDescriptor.isResolvable(memberDescriptor)));

    }

    @Test
    void shouldDependantSubjectClassBeResolvableAndProviderSubjectClassWithDependencyNot() {

        //given
        CafeClassDescriptors cafeClassDescriptors = CafeClassDescriptors.builder()
                .withClass(SubjectClassInjectable.class)
                .withClass(SubjectClassProviderWCADate.class)
                .build();

        CafeBeansResolvableService resolvableDescriptor = CafeBeansResolvableService.from(cafeClassDescriptors);

        //then
        cafeClassDescriptors.descriptor(SubjectClassInjectable.class)
                .getMembers() //getMembers()
                .forEach(memberDescriptor -> Assertions.assertTrue(resolvableDescriptor.isResolvable(memberDescriptor)));

        //then
        cafeClassDescriptors.descriptor(SubjectClassProviderWCADate.class)
                .getMembers()
                .forEach(memberDescriptor -> Assertions.assertFalse(resolvableDescriptor.isResolvable(memberDescriptor)));

    }
}
