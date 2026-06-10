package org.taranix.cafe.beans.resolvers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.CafeApplicationContext;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.annotations.methods.CafeProvider;
import org.taranix.cafe.beans.annotations.modifiers.CafePrimary;
import org.taranix.cafe.beans.exceptions.BeansRepositoryException;
import org.taranix.cafe.beans.exceptions.CafeBeansContextException;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.resolvers.types.ArrayBeanTypeResolver;
import org.taranix.cafe.beans.resolvers.types.ClassBeanTypeResolver;
import org.taranix.cafe.beans.resolvers.types.CollectionBeanTypeResolver;
import org.taranix.cafe.beans.resolvers.types.OptionalBeanTypeResolver;

import java.util.List;
import java.util.Optional;

class BeanTypeResolverTest {

    // --- ClassBeanTypeResolver.isApplicable ---

    @Test
    @DisplayName("ClassBeanTypeResolver: isApplicable is true for plain class")
    void classResolverApplicableForClass() {
        ClassBeanTypeResolver resolver = new ClassBeanTypeResolver();
        Assertions.assertTrue(resolver.isApplicable(BeanTypeKey.from(String.class)));
    }

    @Test
    @DisplayName("ClassBeanTypeResolver: isApplicable is false for collection")
    void classResolverNotApplicableForCollection() {
        ClassBeanTypeResolver resolver = new ClassBeanTypeResolver();
        Assertions.assertFalse(resolver.isApplicable(BeanTypeKey.from(List.class, String.class)));
    }

    @Test
    @DisplayName("ClassBeanTypeResolver: isApplicable is false for Optional")
    void classResolverNotApplicableForOptional() {
        ClassBeanTypeResolver resolver = new ClassBeanTypeResolver();
        Assertions.assertFalse(resolver.isApplicable(BeanTypeKey.from(Optional.class, String.class)));
    }

    @Test
    @DisplayName("ClassBeanTypeResolver: isApplicable is false for array")
    void classResolverNotApplicableForArray() {
        ClassBeanTypeResolver resolver = new ClassBeanTypeResolver();
        Assertions.assertFalse(resolver.isApplicable(BeanTypeKey.from(String[].class)));
    }

    // --- ClassBeanTypeResolver: resolution via context ---

    @Test
    @DisplayName("ClassBeanTypeResolver: resolves single provider")
    void classResolverResolvesOneProvider() {
        CafeApplicationContext ctx = CafeApplicationContext.builder()
                .withClass(SingleStringProvider.class)
                .build();
        ctx.initialize();
        Assertions.assertEquals("hello", ctx.getInstance(String.class));
    }

    @Test
    @DisplayName("ClassBeanTypeResolver: resolves @CafePrimary when multiple providers exist")
    void classResolverResolvesPrimaryAmongMultiple() {
        CafeApplicationContext ctx = CafeApplicationContext.builder()
                .withClass(PrimaryStringProvider.class)
                .build();
        ctx.initialize();
        Assertions.assertEquals("primary", ctx.getInstance(String.class));
    }

    @Test
    @DisplayName("ClassBeanTypeResolver: throws when multiple providers exist without @CafePrimary")
    void classResolverThrowsForAmbiguousProviders() {
        CafeApplicationContext ctx = CafeApplicationContext.builder()
                .withClass(AmbiguousStringConsumer.class)
                .withClass(AmbiguousStringProviderA.class)
                .withClass(AmbiguousStringProviderB.class)
                .build();
        Assertions.assertThrows(BeansRepositoryException.class, ctx::initialize);
    }

    @Test
    @DisplayName("ClassBeanTypeResolver: resolveOrNull returns null when no provider exists")
    void classResolverReturnsNullWhenNoProvider() {
        CafeApplicationContext ctx = CafeApplicationContext.builder()
                .withClass(OptionalStringConsumer.class)
                .build();
        ctx.initialize();
        // Optional field gets Optional.empty() rather than null
        OptionalStringConsumer instance = ctx.getInstance(OptionalStringConsumer.class);
        Assertions.assertTrue(instance.value.isEmpty());
    }

    // --- CollectionBeanTypeResolver ---

    @Test
    @DisplayName("CollectionBeanTypeResolver: isApplicable is true for List")
    void collectionResolverApplicableForList() {
        CollectionBeanTypeResolver resolver = new CollectionBeanTypeResolver();
        Assertions.assertTrue(resolver.isApplicable(BeanTypeKey.from(List.class, String.class)));
    }

    @Test
    @DisplayName("CollectionBeanTypeResolver: isApplicable is false for plain class")
    void collectionResolverNotApplicableForClass() {
        CollectionBeanTypeResolver resolver = new CollectionBeanTypeResolver();
        Assertions.assertFalse(resolver.isApplicable(BeanTypeKey.from(String.class)));
    }

    @Test
    @DisplayName("CollectionBeanTypeResolver: resolves all providers into a list")
    void collectionResolverGathersAllProviders() {
        CafeApplicationContext ctx = CafeApplicationContext.builder()
                .withClass(MultiStringProvider.class)
                .withClass(CollectionStringConsumer.class)
                .build();
        ctx.initialize();
        CollectionStringConsumer instance = ctx.getInstance(CollectionStringConsumer.class);
        Assertions.assertEquals(2, instance.values.size());
    }

    @Test
    @DisplayName("CollectionBeanTypeResolver: validation fails when no providers exist for element type")
    void collectionResolverEmptyWhenNoProviders() {
        CafeApplicationContext ctx = CafeApplicationContext.builder()
                .withClass(CollectionStringConsumer.class)
                .build();
        Assertions.assertThrows(CafeBeansContextException.class, ctx::initialize);
    }

    // --- ArrayBeanTypeResolver ---

    @Test
    @DisplayName("ArrayBeanTypeResolver: isApplicable is true for array")
    void arrayResolverApplicableForArray() {
        ArrayBeanTypeResolver resolver = new ArrayBeanTypeResolver();
        Assertions.assertTrue(resolver.isApplicable(BeanTypeKey.from(String[].class)));
    }

    @Test
    @DisplayName("ArrayBeanTypeResolver: isApplicable is false for non-array")
    void arrayResolverNotApplicableForClass() {
        ArrayBeanTypeResolver resolver = new ArrayBeanTypeResolver();
        Assertions.assertFalse(resolver.isApplicable(BeanTypeKey.from(String.class)));
    }

    @Test
    @DisplayName("ArrayBeanTypeResolver: resolves array when providers exist")
    void arrayResolverBuildsArrayFromProviders() {
        CafeApplicationContext ctx = CafeApplicationContext.builder()
                .withClass(MultiStringProvider.class)
                .withClass(ArrayStringConsumer.class)
                .build();
        ctx.initialize();
        ArrayStringConsumer instance = ctx.getInstance(ArrayStringConsumer.class);
        Assertions.assertEquals(2, instance.values.length);
    }

    @Test
    @DisplayName("ArrayBeanTypeResolver: validation fails when no providers exist for element type")
    void arrayResolverThrowsWhenNoProviders() {
        CafeApplicationContext ctx = CafeApplicationContext.builder()
                .withClass(ArrayStringConsumer.class)
                .build();
        Assertions.assertThrows(CafeBeansContextException.class, ctx::initialize);
    }

    // --- OptionalBeanTypeResolver ---

    @Test
    @DisplayName("OptionalBeanTypeResolver: isApplicable is true for Optional")
    void optionalResolverApplicable() {
        OptionalBeanTypeResolver resolver = new OptionalBeanTypeResolver();
        Assertions.assertTrue(resolver.isApplicable(BeanTypeKey.from(Optional.class, String.class)));
    }

    @Test
    @DisplayName("OptionalBeanTypeResolver: isApplicable is false for non-Optional")
    void optionalResolverNotApplicable() {
        OptionalBeanTypeResolver resolver = new OptionalBeanTypeResolver();
        Assertions.assertFalse(resolver.isApplicable(BeanTypeKey.from(String.class)));
    }

    @Test
    @DisplayName("OptionalBeanTypeResolver: returns Optional.of(bean) when bean present")
    void optionalResolverPresent() {
        CafeApplicationContext ctx = CafeApplicationContext.builder()
                .withClass(SingleStringProvider.class)
                .withClass(OptionalStringConsumer.class)
                .build();
        ctx.initialize();
        OptionalStringConsumer instance = ctx.getInstance(OptionalStringConsumer.class);
        Assertions.assertTrue(instance.value.isPresent());
        Assertions.assertEquals("hello", instance.value.get());
    }

    @Test
    @DisplayName("OptionalBeanTypeResolver: returns Optional.empty() when bean absent")
    void optionalResolverAbsent() {
        CafeApplicationContext ctx = CafeApplicationContext.builder()
                .withClass(OptionalStringConsumer.class)
                .build();
        ctx.initialize();
        OptionalStringConsumer instance = ctx.getInstance(OptionalStringConsumer.class);
        Assertions.assertTrue(instance.value.isEmpty());
    }

    // --- Fixture classes ---

    @CafeSingleton
    static class SingleStringProvider {
        @CafeProvider
        String get() { return "hello"; }
    }

    @CafeSingleton
    static class PrimaryStringProvider {
        @CafePrimary
        @CafeProvider
        String primary() { return "primary"; }

        @CafeProvider
        String other() { return "other"; }
    }

    @CafeSingleton
    static class AmbiguousStringProviderA {
        @CafeProvider
        String get() { return "a"; }
    }

    @CafeSingleton
    static class AmbiguousStringProviderB {
        @CafeProvider
        String get() { return "b"; }
    }

    @CafeSingleton
    static class AmbiguousStringConsumer {
        @CafeInject
        String value;
    }

    @CafeSingleton
    static class OptionalStringConsumer {
        @CafeInject
        Optional<String> value;
    }

    @CafeSingleton
    static class MultiStringProvider {
        @CafeProvider
        String first() { return "first"; }

        @CafeProvider
        String second() { return "second"; }
    }

    @CafeSingleton
    static class CollectionStringConsumer {
        @CafeInject
        List<String> values;
    }

    @CafeSingleton
    static class ArrayStringConsumer {
        @CafeInject
        String[] values;
    }
}
