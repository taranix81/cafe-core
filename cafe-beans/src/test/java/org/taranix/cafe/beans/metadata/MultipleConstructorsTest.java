package org.taranix.cafe.beans.metadata;

import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.exceptions.CafeBeansFactoryException;
import org.taranix.cafe.beans.metadata.data.TwoConstructorsClass;

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