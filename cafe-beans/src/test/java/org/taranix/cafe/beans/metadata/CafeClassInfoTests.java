package org.taranix.cafe.beans.metadata;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.metadata.data.ManyProvidersAndInjectables;
import org.taranix.cafe.beans.metadata.data.generics.DateProviderWithInstantParameterAndLongInjectable;
import org.taranix.cafe.beans.metadata.data.generics.IntegerProviderAndStringInjectable;
import org.taranix.cafe.beans.metadata.members.CafeConstructorInfo;
import org.taranix.cafe.beans.metadata.members.CafeFieldInfo;
import org.taranix.cafe.beans.metadata.members.CafeMemberInfo;
import org.taranix.cafe.beans.metadata.members.CafeMethodInfo;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.services.CafeBeanDefinitionService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.Set;

class CafeClassInfoTests {


    @Test
    void shouldFindAllAnnotatedMethodsAndFields() {
        //given
        CafeClassInfo cafeClassInfo = CafeBeanDefinitionService
                .builder()
                .withClass(ManyProvidersAndInjectables.class)
                .build()
                .findClassInfo(ManyProvidersAndInjectables.class);


        //when
        Set<CafeMemberInfo> allMembers = cafeClassInfo.getMembers();
        Set<CafeMethodInfo> allMethods = cafeClassInfo.methods();
        Set<CafeFieldInfo> allFields = cafeClassInfo.fields();
        CafeConstructorInfo constructor = cafeClassInfo.constructor();

        //then
        Assertions.assertEquals(9, allMembers.size());
        Assertions.assertEquals(5, allMethods.size());
        Assertions.assertEquals(3, allFields.size());

        Assertions.assertTrue(constructor.hasDependencies());
        Assertions.assertTrue(constructor.dependencies().contains(BeanTypeKey.from(BigDecimal.class)));
        Assertions.assertTrue(constructor.provides().contains(BeanTypeKey.from(ManyProvidersAndInjectables.class)));


    }


    @Test
    void shouldDetermineFieldAndMethodType() {
        //given
        CafeClassInfo cafeClassInfo = CafeBeanDefinitionService
                .builder()
                .withClass(IntegerProviderAndStringInjectable.class)
                .build()
                .findClassInfo(IntegerProviderAndStringInjectable.class);

        //when
        CafeFieldInfo genericField = cafeClassInfo
                .fields().stream()
                .findFirst()
                .orElse(null);
        CafeMethodInfo genericMethod = cafeClassInfo
                .methods().stream()
                .findFirst()
                .orElse(null);
        CafeConstructorInfo cafeConstructorDescriptor = cafeClassInfo.constructor();

        //then
        Assertions.assertNotNull(genericField);
        Assertions.assertNotNull(genericMethod);
        Assertions.assertNotNull(cafeConstructorDescriptor);

        Assertions.assertEquals(3, cafeConstructorDescriptor.provides().size());
        Assertions.assertEquals(String.class, genericField.getFieldTypeKey().getType());
        Assertions.assertEquals(Integer.class, genericMethod.getMethodReturnTypeKey().getType());
    }

    @Test
    void shouldDetermineFieldAndMethodTypeAndParameterType() {
        //given
        CafeClassInfo cafeClassInfo = CafeBeanDefinitionService
                .builder()
                .withClass(DateProviderWithInstantParameterAndLongInjectable.class)
                .build()
                .findClassInfo(DateProviderWithInstantParameterAndLongInjectable.class);

        //when
        CafeFieldInfo genericField = cafeClassInfo
                .fields().stream()
                .findFirst()
                .orElse(null);
        CafeMethodInfo genericMethod = cafeClassInfo
                .methods().stream()
                .findFirst()
                .orElse(null);
        CafeConstructorInfo cafeConstructorDescriptor = cafeClassInfo.constructor();

        //then
        Assertions.assertNotNull(genericField);
        Assertions.assertNotNull(genericMethod);
        Assertions.assertNotNull(cafeConstructorDescriptor);

        Assertions.assertEquals(3, cafeConstructorDescriptor.provides().size());
        Assertions.assertEquals(Long.class, genericField.getFieldTypeKey().getType());
        Assertions.assertEquals(Date.class, genericMethod.getMethodReturnTypeKey().getType());
        Assertions.assertTrue(genericMethod.dependencies().contains(BeanTypeKey.from(Instant.class)));
    }
}
