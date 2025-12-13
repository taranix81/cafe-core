package org.taranix.cafe.beans.descriptors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.descriptors.data.*;
import org.taranix.cafe.beans.descriptors.data.generics.IntegerProviderAndStringInjectable;
import org.taranix.cafe.beans.descriptors.data.generics.SetServiceClassInjectable;
import org.taranix.cafe.beans.descriptors.members.CafeFieldInfo;
import org.taranix.cafe.beans.descriptors.members.CafeMemberInfo;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.util.Collection;
import java.util.Set;

class CafeBeansDependencyTests {


    @Test
    void shouldMatchConstructorProviderForField() {
        //given

        CafeClassDescriptors cafeClassDescriptors = CafeClassDescriptors.builder()
                .withClass(ServiceClassInjectable.class)
                .withClass(ServiceClass.class)
                .build();

        CafeBeansDependencyService cafeBeansDependencyService = CafeBeansDependencyService.from(cafeClassDescriptors);


        //when
        CafeMemberInfo field = cafeClassDescriptors.descriptor(ServiceClassInjectable.class)
                .fields()
                .stream()
                .findFirst()
                .orElse(null);

        CafeMemberInfo constructor = cafeClassDescriptors.descriptor(ServiceClass.class).constructor();


        //then
        Assertions.assertTrue(cafeBeansDependencyService.providers(field).contains(constructor));


    }

    @Test
    void shouldMatchConstructorProviderForArrayField() {
        //given

        CafeClassDescriptors cafeClassDescriptors = CafeClassDescriptors.builder()
                .withClass(ArrayServiceClassInjectable.class)
                .withClass(ServiceClass.class)
                .build();

        CafeBeansDependencyService cafeBeansDependencyService = CafeBeansDependencyService.from(cafeClassDescriptors);


        //when
        CafeMemberInfo field = cafeClassDescriptors.descriptor(ArrayServiceClassInjectable.class)
                .fields()
                .stream()
                .findFirst()
                .orElse(null);
        CafeMemberInfo constructor = cafeClassDescriptors.descriptor(ServiceClass.class).constructor();
        CafeClassInfo dependant = cafeClassDescriptors.descriptor(ArrayServiceClassInjectable.class);
        CafeClassInfo provider = cafeClassDescriptors.descriptor(ServiceClass.class);

        //then
        Assertions.assertNotNull(field);
        Assertions.assertNotNull(constructor);
        Assertions.assertTrue(cafeBeansDependencyService.providers(field).contains(constructor));
        Assertions.assertTrue(cafeBeansDependencyService.providersForClass(dependant).contains(provider));

    }

    @Test
    void shouldMatchConstructorProviderForListField() {
        //given

        CafeClassDescriptors cafeClassDescriptors = CafeClassDescriptors.builder()
                .withClass(ListServiceClassInjectable.class)
                .withClass(ServiceClass.class)
                .build();

        CafeBeansDependencyService cafeBeansDependencyService = CafeBeansDependencyService.from(cafeClassDescriptors);


        //when
        CafeMemberInfo field = cafeClassDescriptors.descriptor(ListServiceClassInjectable.class)
                .fields()
                .stream()
                .findFirst()
                .orElse(null);
        CafeMemberInfo constructor = cafeClassDescriptors.descriptor(ServiceClass.class).constructor();
        CafeClassInfo dependant = cafeClassDescriptors.descriptor(ListServiceClassInjectable.class);
        CafeClassInfo provider = cafeClassDescriptors.descriptor(ServiceClass.class);

        //then
        Assertions.assertNotNull(field);
        Assertions.assertNotNull(constructor);
        Assertions.assertNotNull(provider);
        Assertions.assertNotNull(dependant);
        Assertions.assertTrue(cafeBeansDependencyService.providers(field).contains(constructor));
        Assertions.assertTrue(cafeBeansDependencyService.providersForClass(dependant).contains(provider));

    }

    @Test
    void shouldMatchConstructorProviderForSetField() {
        //given

        CafeClassDescriptors cafeClassDescriptors = CafeClassDescriptors.builder()
                .withClass(SetServiceClassInjectable.class)
                .withClass(ServiceClass.class)
                .build();

        CafeBeansDependencyService cafeBeansDependencyService = CafeBeansDependencyService.from(cafeClassDescriptors);


        //when
        CafeMemberInfo field = cafeClassDescriptors.descriptor(SetServiceClassInjectable.class)
                .fields()
                .stream()
                .findFirst()
                .orElse(null);
        CafeMemberInfo constructor = cafeClassDescriptors.descriptor(ServiceClass.class).constructor();
        CafeClassInfo dependant = cafeClassDescriptors.descriptor(SetServiceClassInjectable.class);
        CafeClassInfo provider = cafeClassDescriptors.descriptor(ServiceClass.class);

        //then
        Assertions.assertNotNull(field);
        Assertions.assertNotNull(constructor);
        Assertions.assertNotNull(dependant);
        Assertions.assertNotNull(provider);

        Assertions.assertTrue(cafeBeansDependencyService.providers(field).contains(constructor));
        Assertions.assertTrue(cafeBeansDependencyService.providersForClass(dependant).contains(provider));

    }

    @Test
    void shouldMatchMethodProviderForField() {
        //given
        CafeClassDescriptors cafeClassDescriptors = CafeClassDescriptors.builder()
                .withClass(ServiceClassInjectable.class)
                .withClass(ServiceClassProvider.class)
                .build();
        CafeBeansDependencyService cafeBeansDependencyService = CafeBeansDependencyService.from(cafeClassDescriptors);

        //when
        CafeMemberInfo field = cafeClassDescriptors.descriptor(ServiceClassInjectable.class)
                .fields()
                .stream()
                .findFirst()
                .orElse(null);

        CafeMemberInfo method = cafeClassDescriptors.descriptor(ServiceClassProvider.class)
                .methods()
                .stream()
                .findFirst()
                .orElse(null);

        boolean cycleBetweenClasses = cafeBeansDependencyService.hasCycleBetweenClasses();

        //then
        Assertions.assertFalse(cycleBetweenClasses);
        Assertions.assertNotNull(field);
        Assertions.assertNotNull(method);
        Assertions.assertTrue(cafeBeansDependencyService.providers(field).contains(method));
    }

    @Test
    void shouldFindEmptyProvidersForDependantField() {
        //given
        CafeClassDescriptors cafeClassDescriptors = CafeClassDescriptors.builder()
                .withClass(ServiceClassInjectable.class)
                .build();

        CafeBeansDependencyService cafeBeansDependencyService = CafeBeansDependencyService.from(cafeClassDescriptors);

        //when
        CafeMemberInfo field = cafeClassDescriptors.descriptor(ServiceClassInjectable.class)
                .fields()
                .stream()
                .findFirst()
                .orElse(null);

        CafeMemberInfo constructor = cafeClassDescriptors.descriptor(ServiceClassInjectable.class)
                .constructor();

        //then
        Assertions.assertNotNull(field);
        Assertions.assertTrue(field.hasDependencies(BeanTypeKey.from(ServiceClassInjectable.class))); //field's constructor
        Assertions.assertTrue(field.hasDependencies(BeanTypeKey.from(ServiceClass.class))); //field's value
        Assertions.assertTrue(cafeBeansDependencyService.providers(field).contains(constructor));
    }

    @Test
    void shouldFindCycleWithinOneClass() {
        //given
        CafeClassDescriptors cafeClassDescriptors = CafeClassDescriptors.builder()
                .withClass(ProvidersWithCycle.class)
                .build();
        CafeBeansDependencyService cafeBeansDependencyService = CafeBeansDependencyService
                .from(cafeClassDescriptors);

        //when
        boolean hasCycle = cafeBeansDependencyService.hasCycleBetweenMembers();
        Collection<CafeMemberInfo> cycleSet = cafeBeansDependencyService.membersCycleSet();
        //then
        Assertions.assertTrue(hasCycle);
        Assertions.assertTrue(cycleSet.containsAll(cafeClassDescriptors.methods()));
    }

    @Test
    void shouldFindCycleAmongManyClasses() {
        //given
        CafeClassDescriptors cafeClassDescriptors = CafeClassDescriptors.builder()
                .withClass(StringProviderWithParameter.class)
                .withClass(IntegerProviderWithStringParameter.class)
                .withClass(DateProviderWithIntegerParameter.class)
                .build();
        CafeBeansDependencyService cafeBeansDependencyService = CafeBeansDependencyService.from(cafeClassDescriptors);

        //when
        boolean hasCycle = cafeBeansDependencyService.hasCycleBetweenMembers();
        boolean hacClassCycle = cafeBeansDependencyService.hasCycleBetweenClasses();
        Collection<CafeMemberInfo> cycleSet = cafeBeansDependencyService.membersCycleSet();
        Collection<CafeClassInfo> classCycleSet = cafeBeansDependencyService.classCycleSet();

        //then
        Assertions.assertTrue(hasCycle);
        Assertions.assertTrue(hacClassCycle);
        Assertions.assertEquals(3, cycleSet.size());
        Assertions.assertEquals(3, classCycleSet.size());
    }

    @Test
    void shouldFindStringProviderForGenericSuperClass() {
        //given
        CafeClassDescriptors cafeClassDescriptors = CafeClassDescriptors.builder()
                .withClass(IntegerProviderAndStringInjectable.class)
                .withClass(StringProvider.class)
                .build();

        CafeBeansDependencyService cafeBeansDependencyService = CafeBeansDependencyService.from(cafeClassDescriptors);

        //when
        CafeFieldInfo stringField = cafeClassDescriptors.descriptor(IntegerProviderAndStringInjectable.class)
                .fields().stream()
                .findFirst()
                .orElse(null);

        Set<CafeMemberInfo> stringProviders = cafeBeansDependencyService.providers(stringField, BeanTypeKey.from(String.class));

        //then
        Assertions.assertNotNull(stringField);
        Assertions.assertFalse(stringProviders.isEmpty());
        Assertions.assertEquals(1, stringProviders.size());

    }


}
