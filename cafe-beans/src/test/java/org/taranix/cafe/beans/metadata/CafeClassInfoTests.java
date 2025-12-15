package org.taranix.cafe.beans.metadata;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.annotations.CafeProvider;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.beans.annotations.modifiers.CafeName;
import org.taranix.cafe.beans.exceptions.CafeBeansFactoryException;
import org.taranix.cafe.beans.metadata.members.CafeConstructorInfo;
import org.taranix.cafe.beans.metadata.members.CafeFieldInfo;
import org.taranix.cafe.beans.metadata.members.CafeMemberInfo;
import org.taranix.cafe.beans.metadata.members.CafeMethodInfo;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CafeClassInfoTests {
    @Test
    void shouldThrowWhenMoreThanOneConstructor() {
        // building CafeClassInfo triggers scanMembers -> findConstructor -> should throw
        assertThrows(CafeBeansFactoryException.class, () -> CafeClassInfo.from(TwoConstructorsClass.class));
    }

    @Test
    void shouldFindAllAnnotatedMethodsAndFields() {
        //given
        CafeClassInfo cafeClassInfo = CafeBeansDefinitionRegistry
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
        CafeClassInfo cafeClassInfo = CafeBeansDefinitionRegistry
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
        CafeClassInfo cafeClassInfo = CafeBeansDefinitionRegistry
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

    @Test
    void shouldFindInheritedAnnotatedMembers() {
        CafeClassInfo info = CafeClassInfo.from(Sub.class);

        // inherited annotated field
        assertTrue(info.fields().stream()
                        .anyMatch(f -> f.getField().getName().equals("deprecatedField")),
                "Inherited field should be found");

        // inherited annotated method
        assertTrue(info.methods().stream()
                        .anyMatch(m -> m.getMethod().getName().equals("deprecatedMethod")),
                "Inherited method should be found");
    }

    @Test
    void staticMethodShouldNotDependOnOwner() throws NoSuchMethodException {
        CafeClassInfo info = CafeClassInfo.from(StaticProvider.class);

        Method staticM = StaticProvider.class.getDeclaredMethod("provideStatic");
        Method instM = StaticProvider.class.getDeclaredMethod("provideInstance");

        CafeMethodInfo staticDesc = (CafeMethodInfo) CafeMemberInfo.from(info, staticM);
        CafeMethodInfo instDesc = (CafeMethodInfo) CafeMemberInfo.from(info, instM);

        // static provider must not require owner class
        assertFalse(staticDesc.dependencies().contains(BeanTypeKey.from(StaticProvider.class)),
                "Static method should not depend on owner class");

        // instance provider must require owner class
        assertTrue(instDesc.dependencies().contains(BeanTypeKey.from(StaticProvider.class)),
                "Instance method should depend on owner class");
    }

    static class GenericUProviderWithXParameterAndTInjectable<T, U, X> {

        @CafeInject
        private T unknown;

        @CafeProvider
        public U getUnknown(X input) {
            throw new NotImplementedException();
        }
    }

    static class GenericUProviderAndTInjectable<T, U> {

        @CafeInject
        private T unknown;

        @CafeProvider
        public U getUnknown() {
            throw new NotImplementedException();
        }
    }

    static class DateProviderWithInstantParameterAndLongInjectable extends GenericUProviderWithXParameterAndTInjectable<Long, Date, Instant> {
    }

    @CafeService
    static class IntegerProviderAndStringInjectable extends GenericUProviderAndTInjectable<String, Integer> {
    }

    // Inner test fixture
    static class StaticProvider {
        public static String provideStatic() {
            return "static";
        }

        public String provideInstance() {
            return "instance";
        }
    }

    @CafeService
    static class SuperWithDeprecated {
        @CafeInject
        public String deprecatedField;

        @CafeProvider
        public String deprecatedMethod() {
            return "";
        }
    }

    static class Sub extends SuperWithDeprecated {
    }

    static class ManyProvidersAndInjectables {

        @CafeInject
        Double aDouble;

        @CafeInject
        @CafeName("Sample1")
        Double namedDouble;

        @CafeInject
        @CafeName("Sample2")
        Double otherNamedDouble;

        Serializable serializable;

        ManyProvidersAndInjectables(BigDecimal bigDecimal) {

        }

        @CafeProvider
        String getString() {
            throw new NotImplementedException();
        }

        @CafeProvider
        String getString(int a) {
            throw new NotImplementedException();
        }


        @CafeProvider
        @CafeName("Sample1")
        String getNamedString() {
            throw new NotImplementedException();
        }

        @CafeProvider
        @CafeName("Sample1")
        String getOtherNamedString() {
            throw new NotImplementedException();
        }

        @CafeProvider
        @CafeName("Sample2")
        String getNamedString(int a) {
            throw new NotImplementedException();
        }

        Runnable getRunnable() {
            throw new NotImplementedException();
        }

        @CafeName("Runnable")
        Runnable getNamedRunnable() {
            throw new NotImplementedException();
        }
    }

    static class TwoConstructorsClass {

        public TwoConstructorsClass() {
        }

        public TwoConstructorsClass(int x) {
        }
    }
}
