package org.taranix.cafe.beans.descriptors;

import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.descriptors.data.TwoConstructorsClass;
import org.taranix.cafe.beans.exceptions.CafeBeansFactoryException;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verify that classes with more than one constructor cause the findConstructor() error path.
 */
class MultipleConstructorsTest {

    @Test
    void shouldThrowWhenMoreThanOneConstructor() {
        // building CafeClassInfo triggers scanMembers -> findConstructor -> should throw
        assertThrows(CafeBeansFactoryException.class, () -> CafeClassInfo.from(TwoConstructorsClass.class));
    }
}