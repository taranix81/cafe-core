package org.taranix.cafe.beans.resolvers;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.annotations.classes.CafeService;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.annotations.methods.CafeProvider;
import org.taranix.cafe.beans.metadata.CafeMember;
import org.taranix.cafe.beans.metadata.CafeMetadataRegistry;
import org.taranix.cafe.beans.services.CafeOrderedBeansService;

import java.util.List;

class CafeOrderedBeansServiceTests {

    @Test
    void shouldProperOrderedForSingleClassWithoutDependencies() {
        //given
        CafeMetadataRegistry cafeMetadataRegistry = CafeMetadataRegistry.builder()
                .withClass(SubjectClassProvider.class)
                .build();
        CafeOrderedBeansService orderDescriptor = CafeOrderedBeansService.from(cafeMetadataRegistry);

        //when
        List<CafeMember> ordered = orderDescriptor.orderedMembers();

        //then
        Assertions.assertEquals(2, ordered.size());
        Assertions.assertTrue(ordered.get(0).isConstructor());
        Assertions.assertTrue(ordered.get(1).isMethod());
    }

    @Test
    void shouldProperOrderedForSingleClassWithDependencyToProviderClass() {
        //given
        CafeMetadataRegistry cafeMetadataRegistry = CafeMetadataRegistry.builder()
                .withClass(SubjectClassProvider.class)
                .withClass(SubjectClass.class)
                .build();

        CafeOrderedBeansService orderDescriptor = CafeOrderedBeansService.from(cafeMetadataRegistry);

        //when
        List<CafeMember> ordered = orderDescriptor.orderedMembers();

        //then
        Assertions.assertEquals(3, ordered.size());
        Assertions.assertTrue(ordered.get(0).isConstructor());
        Assertions.assertTrue(ordered.get(1).isConstructor());
        Assertions.assertTrue(ordered.get(2).isMethod());

    }

    @Test
    void shouldProperOrderedForSingleClassWithDependencyToServiceClass() {
        //given
        CafeMetadataRegistry cafeMetadataRegistry = CafeMetadataRegistry.builder()
                .withClass(ServiceClassInjectable.class)
                .withClass(ServiceClass.class)
                .build();
        CafeOrderedBeansService orderDescriptor = CafeOrderedBeansService.from(cafeMetadataRegistry);

        //when
        List<CafeMember> ordered = orderDescriptor.orderedMembers();

        //then
        Assertions.assertEquals(3, ordered.size());
        Assertions.assertTrue(ordered.get(0).isConstructor());
        Assertions.assertTrue(ordered.get(1).isConstructor());
        Assertions.assertTrue(ordered.get(2).isField());

    }

    @Test
    void shouldProperOrderedForSingleClassWithDependencyToServiceClassWithConstructorParameter() {
        //given
        CafeMetadataRegistry cafeMetadataRegistry = CafeMetadataRegistry.builder()
                .withClass(ServiceClassWCAInjectable.class)
                .withClass(ServiceClassWCA.class)
                .withClass(StringProvider.class)
                .build();
        CafeOrderedBeansService orderDescriptor = CafeOrderedBeansService.from(cafeMetadataRegistry);

        //when
        List<CafeMember> ordered = orderDescriptor.orderedMembers();

        //then
        Assertions.assertEquals(5, ordered.size());
        Assertions.assertTrue(ordered.get(0).isConstructor());
        Assertions.assertTrue(ordered.get(1).isConstructor());
        Assertions.assertTrue(ordered.get(2).isMethod());
        Assertions.assertTrue(ordered.get(3).isConstructor());
        Assertions.assertTrue(ordered.get(4).isField());
    }

    static class SubjectClass {

    }

    static class SubjectClassProvider {
        @CafeProvider
        private SubjectClass producer() {
            throw new NotImplementedException();
        }

    }

    @CafeService
    static class ServiceClassWCA {

        ServiceClassWCA(String someString) {

        }
    }

    static class StringProvider {

        @CafeProvider
        String getString() {
            throw new NotImplementedException();
        }
    }

    static class ServiceClassWCAInjectable {

        @CafeInject
        ServiceClassWCA service;
    }

    @CafeService
    static class ServiceClass {
    }

    @CafeService
    static class ServiceClassInjectable {

        @CafeInject
        ServiceClass serviceClass;
    }
}
