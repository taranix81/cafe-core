package org.taranix.cafe.beans.descriptors;

import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.descriptors.members.CafeMemberInfo;
import org.taranix.cafe.beans.descriptors.members.CafeMethodInfo;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verify that static provider methods do not declare a dependency on the owner class,
 * while instance provider methods do.
 */
class StaticMethodDependencyTest {

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

    // Inner test fixture
    static class StaticProvider {
        public static String provideStatic() {
            return "static";
        }

        public String provideInstance() {
            return "instance";
        }
    }
}