package org.taranix.cafe.beans.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.metadata.CafeClassInfo;
import org.taranix.cafe.beans.metadata.data.*;
import org.taranix.cafe.beans.metadata.data.generics.IntegerProviderAndStringInjectable;
import org.taranix.cafe.beans.metadata.data.generics.SetServiceClassInjectable;
import org.taranix.cafe.beans.metadata.members.CafeFieldInfo;
import org.taranix.cafe.beans.metadata.members.CafeMemberInfo;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

class CafeBeansDependencyTests {


    @Test
    void shouldMatchConstructorProviderForField() {
        //given

        CafeBeanDefinitionService cafeBeanDefinitionService = CafeBeanDefinitionService.builder()
                .withClass(ServiceClassInjectable.class)
                .withClass(ServiceClass.class)
                .build();


        //when
        CafeMemberInfo field = cafeBeanDefinitionService.findClassInfo(ServiceClassInjectable.class)
                .fields()
                .stream()
                .findFirst()
                .orElse(null);

        CafeMemberInfo constructor = cafeBeanDefinitionService.findClassInfo(ServiceClass.class).constructor();


        //then
        Assertions.assertTrue(cafeBeanDefinitionService.getMemberDependencyResolverRegistry().providers(field).contains(constructor));


    }

    @Test
    void shouldMatchConstructorProviderForArrayField() {
        //given

        CafeBeanDefinitionService cafeBeanDefinitionService = CafeBeanDefinitionService.builder()
                .withClass(ArrayServiceClassInjectable.class)
                .withClass(ServiceClass.class)
                .build();


        //when
        CafeMemberInfo field = cafeBeanDefinitionService.findClassInfo(ArrayServiceClassInjectable.class)
                .fields()
                .stream()
                .findFirst()
                .orElse(null);
        CafeMemberInfo constructor = cafeBeanDefinitionService.findClassInfo(ServiceClass.class).constructor();
        CafeClassInfo dependant = cafeBeanDefinitionService.findClassInfo(ArrayServiceClassInjectable.class);
        CafeClassInfo provider = cafeBeanDefinitionService.findClassInfo(ServiceClass.class);

        //then
        Assertions.assertNotNull(field);
        Assertions.assertNotNull(constructor);
        Assertions.assertTrue(cafeBeanDefinitionService.getMemberDependencyResolverRegistry().providers(field).contains(constructor));
        Assertions.assertTrue(cafeBeanDefinitionService.getClassDependencyResolverRegistry().providers(dependant).contains(provider));

    }

    @Test
    void shouldMatchConstructorProviderForListField() {
        //given

        CafeBeanDefinitionService cafeBeanDefinitionService = CafeBeanDefinitionService.builder()
                .withClass(ListServiceClassInjectable.class)
                .withClass(ServiceClass.class)
                .build();


        //when
        CafeMemberInfo field = cafeBeanDefinitionService.findClassInfo(ListServiceClassInjectable.class)
                .fields()
                .stream()
                .findFirst()
                .orElse(null);
        CafeMemberInfo constructor = cafeBeanDefinitionService.findClassInfo(ServiceClass.class).constructor();
        CafeClassInfo dependant = cafeBeanDefinitionService.findClassInfo(ListServiceClassInjectable.class);
        CafeClassInfo provider = cafeBeanDefinitionService.findClassInfo(ServiceClass.class);

        //then
        Assertions.assertNotNull(field);
        Assertions.assertNotNull(constructor);
        Assertions.assertNotNull(provider);
        Assertions.assertNotNull(dependant);
        Assertions.assertTrue(cafeBeanDefinitionService.getMemberDependencyResolverRegistry().providers(field).contains(constructor));
        Assertions.assertTrue(cafeBeanDefinitionService.getClassDependencyResolverRegistry().providers(dependant).contains(provider));

    }

    @Test
    void shouldMatchConstructorProviderForSetField() {
        //given

        CafeBeanDefinitionService cafeBeanDefinitionService = CafeBeanDefinitionService.builder()
                .withClass(SetServiceClassInjectable.class)
                .withClass(ServiceClass.class)
                .build();


        //when
        CafeMemberInfo field = cafeBeanDefinitionService.findClassInfo(SetServiceClassInjectable.class)
                .fields()
                .stream()
                .findFirst()
                .orElse(null);
        CafeMemberInfo constructor = cafeBeanDefinitionService.findClassInfo(ServiceClass.class).constructor();
        CafeClassInfo dependant = cafeBeanDefinitionService.findClassInfo(SetServiceClassInjectable.class);
        CafeClassInfo provider = cafeBeanDefinitionService.findClassInfo(ServiceClass.class);

        //then
        Assertions.assertNotNull(field);
        Assertions.assertNotNull(constructor);
        Assertions.assertNotNull(dependant);
        Assertions.assertNotNull(provider);

        Assertions.assertTrue(cafeBeanDefinitionService.getMemberDependencyResolverRegistry().providers(field).contains(constructor));
        Assertions.assertTrue(cafeBeanDefinitionService.getClassDependencyResolverRegistry().providers(dependant).contains(provider));

    }

    @Test
    void shouldMatchMethodProviderForField() {
        //given
        CafeBeanDefinitionService cafeBeanDefinitionService = CafeBeanDefinitionService.builder()
                .withClass(ServiceClassInjectable.class)
                .withClass(ServiceClassProvider.class)
                .build();

        //when
        CafeMemberInfo field = cafeBeanDefinitionService.findClassInfo(ServiceClassInjectable.class)
                .fields()
                .stream()
                .findFirst()
                .orElse(null);

        CafeMemberInfo method = cafeBeanDefinitionService.findClassInfo(ServiceClassProvider.class)
                .methods()
                .stream()
                .findFirst()
                .orElse(null);

        boolean cycleBetweenClasses = cafeBeanDefinitionService.hasCycleBetweenClasses();

        //then
        Assertions.assertFalse(cycleBetweenClasses);
        Assertions.assertNotNull(field);
        Assertions.assertNotNull(method);
        Assertions.assertTrue(cafeBeanDefinitionService.getMemberDependencyResolverRegistry().providers(field).contains(method));
    }

    @Test
    void shouldFindEmptyProvidersForDependantField() {
        //given
        CafeBeanDefinitionService cafeBeanDefinitionService = CafeBeanDefinitionService.builder()
                .withClass(ServiceClassInjectable.class)
                .build();


        //when
        CafeMemberInfo field = cafeBeanDefinitionService.findClassInfo(ServiceClassInjectable.class)
                .fields()
                .stream()
                .findFirst()
                .orElse(null);

        CafeMemberInfo constructor = cafeBeanDefinitionService.findClassInfo(ServiceClassInjectable.class)
                .constructor();

        //then
        Assertions.assertNotNull(field);
        Assertions.assertTrue(field.hasDependencies(BeanTypeKey.from(ServiceClassInjectable.class))); //field's constructor
        Assertions.assertTrue(field.hasDependencies(BeanTypeKey.from(ServiceClass.class))); //field's value
        Assertions.assertTrue(cafeBeanDefinitionService.getMemberDependencyResolverRegistry().providers(field).contains(constructor));
    }

    @Test
    void shouldFindCycleWithinOneClass() {
        //given
        CafeBeanDefinitionService cafeBeanDefinitionService = CafeBeanDefinitionService.builder()
                .withClass(ProvidersWithCycle.class)
                .build();

        //when
        boolean hasCycle = cafeBeanDefinitionService.hasCycleBetweenClassMembers();
        Collection<CafeMemberInfo> cycleSet = cafeBeanDefinitionService.membersCycleSet();
        //then
        Assertions.assertTrue(hasCycle);
        Assertions.assertTrue(cycleSet.containsAll(cafeBeanDefinitionService.allMembers().stream().filter(CafeMemberInfo::isMethod).collect(Collectors.toSet())));
    }

    @Test
    void shouldFindCycleAmongManyClasses() {
        //given
        CafeBeanDefinitionService cafeBeanDefinitionService = CafeBeanDefinitionService.builder()
                .withClass(StringProviderWithParameter.class)
                .withClass(IntegerProviderWithStringParameter.class)
                .withClass(DateProviderWithIntegerParameter.class)
                .build();

        //when
        boolean hasCycle = cafeBeanDefinitionService.hasCycleBetweenClassMembers();
        boolean hacClassCycle = cafeBeanDefinitionService.hasCycleBetweenClasses();
        Collection<CafeMemberInfo> cycleSet = cafeBeanDefinitionService.membersCycleSet();
        Collection<CafeClassInfo> classCycleSet = cafeBeanDefinitionService.classCycleSet();

        //then
        Assertions.assertTrue(hasCycle);
        Assertions.assertTrue(hacClassCycle);
        Assertions.assertEquals(3, cycleSet.size());
        Assertions.assertEquals(3, classCycleSet.size());
    }

    @Test
    void shouldFindStringProviderForGenericSuperClass() {
        //given
        CafeBeanDefinitionService cafeBeanDefinitionService = CafeBeanDefinitionService.builder()
                .withClass(IntegerProviderAndStringInjectable.class)
                .withClass(StringProvider.class)
                .build();


        //when
        CafeFieldInfo stringField = cafeBeanDefinitionService.findClassInfo(IntegerProviderAndStringInjectable.class)
                .fields().stream()
                .findFirst()
                .orElse(null);

        Set<CafeMemberInfo> stringProviders = cafeBeanDefinitionService.getMemberDependencyResolverRegistry().providers(stringField, BeanTypeKey.from(String.class));

        //then
        Assertions.assertNotNull(stringField);
        Assertions.assertFalse(stringProviders.isEmpty());
        Assertions.assertEquals(1, stringProviders.size());

    }


}
