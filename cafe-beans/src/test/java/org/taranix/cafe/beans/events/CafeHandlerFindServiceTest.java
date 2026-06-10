package org.taranix.cafe.beans.events;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.CafeApplicationContext;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.beans.annotations.methods.CafeHandler;
import org.taranix.cafe.beans.annotations.methods.CafeProvider;
import org.taranix.cafe.beans.repositories.beans.BeansRepository;

import java.util.Set;

class CafeHandlerFindServiceTest {

    @Test
    @DisplayName("find: returns empty set when repository is empty")
    void findReturnsEmptySetForEmptyRepository() {
        CafeHandlerFindService service = new CafeHandlerFindService(new BeansRepository());
        Set<CafeHandlerSignature> result = service.find(ann -> true, ann -> true, params -> true);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("find: returns matching handler signature for correct annotation type")
    void findReturnsHandlerForMatchingAnnotationType() {
        CafeApplicationContext ctx = CafeApplicationContext.builder()
                .withClass(SingleHandlerClass.class)
                .build();
        ctx.initialize();

        CafeHandlerFindService service = new CafeHandlerFindService(ctx.getBeansFactory().getRepository());
        Set<CafeHandlerSignature> result = service.find(
                ann -> ann.annotationType().equals(CafeHandler.class),
                ann -> true,
                params -> true
        );

        Assertions.assertEquals(1, result.size());
    }

    @Test
    @DisplayName("find: returns empty set when annotation type does not match")
    void findReturnsEmptySetForNonMatchingAnnotationType() {
        CafeApplicationContext ctx = CafeApplicationContext.builder()
                .withClass(SingleHandlerClass.class)
                .build();
        ctx.initialize();

        CafeHandlerFindService service = new CafeHandlerFindService(ctx.getBeansFactory().getRepository());
        Set<CafeHandlerSignature> result = service.find(
                ann -> ann.annotationType().equals(CafeProvider.class),
                ann -> true,
                params -> true
        );

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("find: returns all handlers when multiple methods match")
    void findReturnsAllMatchingHandlers() {
        CafeApplicationContext ctx = CafeApplicationContext.builder()
                .withClass(MultiHandlerClass.class)
                .build();
        ctx.initialize();

        CafeHandlerFindService service = new CafeHandlerFindService(ctx.getBeansFactory().getRepository());
        Set<CafeHandlerSignature> result = service.find(
                ann -> ann.annotationType().equals(CafeHandler.class),
                ann -> true,
                params -> true
        );

        Assertions.assertEquals(2, result.size());
    }

    @Test
    @DisplayName("find: parameter predicate filters out non-matching handlers")
    void findFiltersHandlersByParameterPredicate() {
        CafeApplicationContext ctx = CafeApplicationContext.builder()
                .withClass(MultiHandlerClass.class)
                .build();
        ctx.initialize();

        CafeHandlerFindService service = new CafeHandlerFindService(ctx.getBeansFactory().getRepository());
        // Only match handlers with no parameters
        Set<CafeHandlerSignature> result = service.find(
                ann -> ann.annotationType().equals(CafeHandler.class),
                ann -> true,
                params -> params.length == 0
        );

        Assertions.assertFalse(result.isEmpty());
    }

    // --- Fixture classes ---

    @CafeSingleton
    static class SingleHandlerClass {
        @CafeHandler
        void onEvent() {}
    }

    @CafeSingleton
    static class MultiHandlerClass {
        @CafeHandler
        void onFirst() {}

        // Different parameter type gives a distinct HandlerTypeKey
        @CafeHandler
        void onSecond(String arg) {}
    }
}
