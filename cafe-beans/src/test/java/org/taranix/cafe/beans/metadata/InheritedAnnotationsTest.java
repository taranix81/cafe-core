package org.taranix.cafe.beans.metadata;

import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.annotations.CafeProvider;
import org.taranix.cafe.beans.annotations.CafeService;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verify that annotated fields and methods declared on a superclass are discovered when scanning a subclass.
 * Uses java.lang.Deprecated to avoid relying on framework-specific annotations.
 */
class InheritedAnnotationsTest {

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
}