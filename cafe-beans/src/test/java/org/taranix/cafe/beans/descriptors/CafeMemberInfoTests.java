package org.taranix.cafe.beans.descriptors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.descriptors.data.MemberDescriptorClass;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

class CafeMemberInfoTests {


    @Test
    @Disabled
    void shouldCreateFieldInfo() throws NoSuchFieldException {
        Field field = MemberDescriptorClass.class.getDeclaredField("integer");
        CafeFieldInfo cafeFieldDescriptor = new CafeFieldInfo(field, null);

        Assertions.assertNotNull(cafeFieldDescriptor);
        Assertions.assertEquals(Integer.class, cafeFieldDescriptor.getFieldTypeKey().getType());
        Assertions.assertTrue(cafeFieldDescriptor.dependencies().contains(BeanTypeKey.from(MemberDescriptorClass.class)));
        Assertions.assertTrue(cafeFieldDescriptor.dependencies().contains(BeanTypeKey.from(Integer.class)));
        Assertions.assertEquals(field, cafeFieldDescriptor.getMember());
        Assertions.assertTrue(cafeFieldDescriptor.provides().isEmpty());


    }

    @Test
    @Disabled
    void shouldCreateMethodInfoWithEmptyParameters() throws NoSuchMethodException {
        Method method = MemberDescriptorClass.class.getDeclaredMethod("getString");
        CafeMethodInfo methodInfo = new CafeMethodInfo(method, null);

        Assertions.assertNotNull(methodInfo);
        Assertions.assertEquals(String.class, methodInfo.getMethodReturnTypeKey().getType());
        Assertions.assertEquals(String.class, Objects.requireNonNull(methodInfo.provides().stream().findFirst().orElse(null)).getType());
        Assertions.assertEquals(method, methodInfo.getMember());
        Assertions.assertEquals(MemberDescriptorClass.SOME_EXAMPLE_ID, methodInfo.getMethodReturnTypeKey().getTypeIdentifier());

        Assertions.assertTrue(methodInfo.dependencies().contains(BeanTypeKey.from(MemberDescriptorClass.class)));
    }

    @Test
    @Disabled
    void shouldCreateMethodInfoWithParameters() throws NoSuchMethodException {
        Method method = MemberDescriptorClass.class.getDeclaredMethod("getString", String.class);
        CafeMethodInfo methodInfo = new CafeMethodInfo(method, null);

        Assertions.assertNotNull(methodInfo);
        Assertions.assertEquals(method, methodInfo.getMember());
        Assertions.assertEquals(String.class, methodInfo.getMethodReturnTypeKey().getType());
        Assertions.assertEquals(String.class, Objects.requireNonNull(methodInfo.provides().stream().findFirst().orElse(null)).getType());
        Assertions.assertTrue(methodInfo.dependencies().contains(BeanTypeKey.from(String.class)));
        Assertions.assertTrue(methodInfo.dependencies().contains(BeanTypeKey.from(MemberDescriptorClass.class)));

    }


}
