package org.taranix.cafe.beans.metadata;

import lombok.Getter;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.annotations.CafeProvider;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.beans.metadata.members.CafeFieldInfo;
import org.taranix.cafe.beans.metadata.members.CafeMemberInfo;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.util.List;
import java.util.Set;

class CafeBeansDefinitionRegistryTest {
    @Test
    void shouldMatchConstructorProviderForListField() {
        //given

        CafeBeansDefinitionRegistry cafeBeansDefinitionRegistry = CafeBeansDefinitionRegistry.builder()
                .withClass(ListServiceClassInjectable.class)
                .withClass(ServiceClass.class)
                .build();


        //when
        CafeMemberInfo field = cafeBeansDefinitionRegistry.findClassInfo(ListServiceClassInjectable.class)
                .fields()
                .stream()
                .findFirst()
                .orElse(null);
        CafeMemberInfo constructor = cafeBeansDefinitionRegistry.findClassInfo(ServiceClass.class).constructor();
        CafeClassInfo dependant = cafeBeansDefinitionRegistry.findClassInfo(ListServiceClassInjectable.class);
        CafeClassInfo provider = cafeBeansDefinitionRegistry.findClassInfo(ServiceClass.class);

        //then
        Assertions.assertNotNull(field);
        Assertions.assertNotNull(constructor);
        Assertions.assertNotNull(provider);
        Assertions.assertNotNull(dependant);
        Assertions.assertTrue(cafeBeansDefinitionRegistry.getMemberDependencyResolverRegistry().providers(field).contains(constructor));
        Assertions.assertTrue(cafeBeansDefinitionRegistry.getClassDependencyResolverRegistry().providers(dependant).contains(provider));

    }

    @Test
    void shouldMatchConstructorProviderForSetField() {
        //given

        CafeBeansDefinitionRegistry cafeBeansDefinitionRegistry = CafeBeansDefinitionRegistry.builder()
                .withClass(SetServiceClassInjectable.class)
                .withClass(ServiceClass.class)
                .build();


        //when
        CafeMemberInfo field = cafeBeansDefinitionRegistry.findClassInfo(SetServiceClassInjectable.class)
                .fields()
                .stream()
                .findFirst()
                .orElse(null);
        CafeMemberInfo constructor = cafeBeansDefinitionRegistry.findClassInfo(ServiceClass.class).constructor();
        CafeClassInfo dependant = cafeBeansDefinitionRegistry.findClassInfo(SetServiceClassInjectable.class);
        CafeClassInfo provider = cafeBeansDefinitionRegistry.findClassInfo(ServiceClass.class);

        //then
        Assertions.assertNotNull(field);
        Assertions.assertNotNull(constructor);
        Assertions.assertNotNull(dependant);
        Assertions.assertNotNull(provider);

        Assertions.assertTrue(cafeBeansDefinitionRegistry.getMemberDependencyResolverRegistry().providers(field).contains(constructor));
        Assertions.assertTrue(cafeBeansDefinitionRegistry.getClassDependencyResolverRegistry().providers(dependant).contains(provider));

    }

    @Test
    void shouldFindStringProviderForGenericSuperClass() {
        //given
        CafeBeansDefinitionRegistry cafeBeansDefinitionRegistry = CafeBeansDefinitionRegistry.builder()
                .withClass(IntegerProviderAndStringInjectable.class)
                .withClass(StringProvider.class)
                .build();


        //when
        CafeFieldInfo stringField = cafeBeansDefinitionRegistry.findClassInfo(IntegerProviderAndStringInjectable.class)
                .fields().stream()
                .findFirst()
                .orElse(null);

        Set<CafeMemberInfo> stringProviders = cafeBeansDefinitionRegistry.getMemberDependencyResolverRegistry().providers(stringField, BeanTypeKey.from(String.class));

        //then
        Assertions.assertNotNull(stringField);
        Assertions.assertFalse(stringProviders.isEmpty());
        Assertions.assertEquals(1, stringProviders.size());

    }

    @Disabled
    @Test
    void shouldMatchMethodProviderForField() {
        //given
        CafeBeansDefinitionRegistry cafeBeansDefinitionRegistry = CafeBeansDefinitionRegistry.builder()
                .withClass(ServiceClassInjectable.class)
                .withClass(ServiceClassProvider.class)
                .build();

        //when
        CafeMemberInfo field = cafeBeansDefinitionRegistry.findClassInfo(ServiceClassInjectable.class)
                .fields()
                .stream()
                .findFirst()
                .orElse(null);

        CafeMemberInfo method = cafeBeansDefinitionRegistry.findClassInfo(ServiceClassProvider.class)
                .methods()
                .stream()
                .findFirst()
                .orElse(null);

        // boolean cycleBetweenClasses = cafeBeansDefinitionRegistry.hasCycleBetweenClasses();

        //then
        // Assertions.assertFalse(cycleBetweenClasses);
        Assertions.assertNotNull(field);
        Assertions.assertNotNull(method);
        Assertions.assertTrue(cafeBeansDefinitionRegistry.getMemberDependencyResolverRegistry().providers(field).contains(method));
    }

    @Test
    void shouldFindEmptyProvidersForDependantField() {
        //given
        CafeBeansDefinitionRegistry cafeBeansDefinitionRegistry = CafeBeansDefinitionRegistry.builder()
                .withClass(ServiceClassInjectable.class)
                .build();


        //when
        CafeMemberInfo field = cafeBeansDefinitionRegistry.findClassInfo(ServiceClassInjectable.class)
                .fields()
                .stream()
                .findFirst()
                .orElse(null);

        CafeMemberInfo constructor = cafeBeansDefinitionRegistry.findClassInfo(ServiceClassInjectable.class)
                .constructor();

        //then
        Assertions.assertNotNull(field);
        Assertions.assertTrue(field.hasDependencies(BeanTypeKey.from(ServiceClassInjectable.class))); //field's constructor
        Assertions.assertTrue(field.hasDependencies(BeanTypeKey.from(ServiceClass.class))); //field's value
        Assertions.assertTrue(cafeBeansDefinitionRegistry.getMemberDependencyResolverRegistry().providers(field).contains(constructor));
    }

    @Test
    void shouldMatchConstructorProviderForField() {
        //given

        CafeBeansDefinitionRegistry cafeBeansDefinitionRegistry = CafeBeansDefinitionRegistry.builder()
                .withClass(ServiceClassInjectable.class)
                .withClass(ServiceClass.class)
                .build();


        //when
        CafeMemberInfo field = cafeBeansDefinitionRegistry.findClassInfo(ServiceClassInjectable.class)
                .fields()
                .stream()
                .findFirst()
                .orElse(null);

        CafeMemberInfo constructor = cafeBeansDefinitionRegistry.findClassInfo(ServiceClass.class).constructor();


        //then
        Assertions.assertTrue(cafeBeansDefinitionRegistry.getMemberDependencyResolverRegistry().providers(field).contains(constructor));


    }

    @Test
    void shouldMatchConstructorProviderForArrayField() {
        //given

        CafeBeansDefinitionRegistry cafeBeansDefinitionRegistry = CafeBeansDefinitionRegistry.builder()
                .withClass(ArrayServiceClassInjectable.class)
                .withClass(ServiceClass.class)
                .build();


        //when
        CafeMemberInfo field = cafeBeansDefinitionRegistry.findClassInfo(ArrayServiceClassInjectable.class)
                .fields()
                .stream()
                .findFirst()
                .orElse(null);
        CafeMemberInfo constructor = cafeBeansDefinitionRegistry.findClassInfo(ServiceClass.class).constructor();
        CafeClassInfo dependant = cafeBeansDefinitionRegistry.findClassInfo(ArrayServiceClassInjectable.class);
        CafeClassInfo provider = cafeBeansDefinitionRegistry.findClassInfo(ServiceClass.class);

        //then
        Assertions.assertNotNull(field);
        Assertions.assertNotNull(constructor);
        Assertions.assertTrue(cafeBeansDefinitionRegistry.getMemberDependencyResolverRegistry().providers(field).contains(constructor));
        Assertions.assertTrue(cafeBeansDefinitionRegistry.getClassDependencyResolverRegistry().providers(dependant).contains(provider));

    }

    static class GenericUProviderAndTInjectable<T, U> {

        @CafeInject
        private T unknown;

        @CafeProvider
        public U getUnknown() {
            throw new NotImplementedException();
        }
    }

    @CafeService
    static class IntegerProviderAndStringInjectable extends GenericUProviderAndTInjectable<String, Integer> {
    }

    static class StringProvider {

        @CafeProvider
        String getString() {
            throw new NotImplementedException();
        }
    }


    static class ServiceClassProvider {

        @CafeProvider
        public ServiceClass getServiceClass() {
            return new ServiceClass();
        }
    }

    static class SetServiceClassInjectable {


        @CafeInject
        Set<ServiceClass> serviceClass;
    }

    static class ListServiceClassInjectable {
        @CafeInject
        List<ServiceClass> serviceClass;
    }

    @Getter
    static class ArrayServiceClassInjectable {

        @CafeInject
        ServiceClass[] serviceClass;
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
