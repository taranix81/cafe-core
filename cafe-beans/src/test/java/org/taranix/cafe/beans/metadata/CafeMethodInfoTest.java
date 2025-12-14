package org.taranix.cafe.beans.metadata;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.annotations.CafeProvider;
import org.taranix.cafe.beans.annotations.modifiers.CafeName;
import org.taranix.cafe.beans.metadata.members.CafeMemberInfo;
import org.taranix.cafe.beans.metadata.members.CafeMethodInfo;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.lang.reflect.Method;
import java.util.Objects;

class CafeMethodInfoTest {

    @Test
    void shouldCreateMethodInfoWithEmptyParameters() throws NoSuchMethodException {
        Method method = ClassA.class.getDeclaredMethod("getString");
        CafeMethodInfo methodInfo = (CafeMethodInfo) CafeMemberInfo.from(CafeClassInfo.from(ClassA.class), method);

        Assertions.assertNotNull(methodInfo);
        Assertions.assertEquals(String.class, methodInfo.getMethodReturnTypeKey().getType());
        Assertions.assertEquals(String.class, Objects.requireNonNull(methodInfo.provides().stream().findFirst().orElse(null)).getType());
        Assertions.assertEquals(method, methodInfo.getMember());
        Assertions.assertEquals(ClassA.SOME_EXAMPLE_ID, methodInfo.getMethodReturnTypeKey().getTypeIdentifier());

        Assertions.assertTrue(methodInfo.dependencies().contains(BeanTypeKey.from(ClassA.class)));
    }

    @Test
    void shouldCreateMethodInfoWithParameters() throws NoSuchMethodException {
        Method method = ClassA.class.getDeclaredMethod("getString", String.class);
        CafeMethodInfo methodInfo = (CafeMethodInfo) CafeMemberInfo.from(CafeClassInfo.from(ClassA.class), method);

        Assertions.assertNotNull(methodInfo);
        Assertions.assertEquals(method, methodInfo.getMember());
        Assertions.assertEquals(String.class, methodInfo.getMethodReturnTypeKey().getType());
        Assertions.assertEquals(String.class, Objects.requireNonNull(methodInfo.provides().stream().findFirst().orElse(null)).getType());
        Assertions.assertTrue(methodInfo.dependencies().contains(BeanTypeKey.from(String.class)));
        Assertions.assertTrue(methodInfo.dependencies().contains(BeanTypeKey.from(ClassA.class)));

    }

    static class ClassA {
        public static final String SOME_EXAMPLE_ID = "some-example-id";

        @CafeInject
        private Integer integer;

        @CafeName(value = SOME_EXAMPLE_ID)
        public String getString() {
            throw new NotImplementedException();
        }

        public String getString(String value) {
            throw new NotImplementedException();
        }
    }

    static class GenericClass<T> {
        public static final String SOME_EXAMPLE_ID = "some-example-id";

        @CafeInject
        private T unknownField;

        @CafeInject
        private Integer integer;

        @CafeName(value = SOME_EXAMPLE_ID)
        public String getString() {
            throw new NotImplementedException();
        }

        @CafeProvider
        public String getString(String value) {
            throw new NotImplementedException();
        }
    }

    static class IntegerClass extends GenericClass<Integer> {

    }

}
