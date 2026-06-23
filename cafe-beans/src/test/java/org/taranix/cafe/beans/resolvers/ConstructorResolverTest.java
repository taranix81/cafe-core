package org.taranix.cafe.beans.resolvers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.CafeApplicationContext;
import org.taranix.cafe.beans.annotations.classes.CafePrototype;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.beans.metadata.CafeConstructor;
import org.taranix.cafe.beans.metadata.CafeMetadataRegistry;
import org.taranix.cafe.beans.resolvers.metadata.constructor.PrototypeConstructorResolver;
import org.taranix.cafe.beans.resolvers.metadata.constructor.SingletonConstructorResolver;

class ConstructorResolverTest {

    // --- SingletonConstructorResolver.isApplicable ---

    @Test
    @DisplayName("SingletonConstructorResolver: isApplicable is true for @CafeSingleton constructor")
    void singletonResolverApplicableForSingleton() {
        CafeMetadataRegistry registry = CafeMetadataRegistry.builder()
                .withClass(SingletonClass.class)
                .build();
        CafeConstructor constructor = registry.getClassMetadata(SingletonClass.class).getConstructor();
        Assertions.assertTrue(new SingletonConstructorResolver().isApplicable(constructor));
    }

    @Test
    @DisplayName("SingletonConstructorResolver: isApplicable is false for @CafePrototype constructor")
    void singletonResolverNotApplicableForPrototype() {
        CafeMetadataRegistry registry = CafeMetadataRegistry.builder()
                .withClass(PrototypeClass.class)
                .build();
        CafeConstructor constructor = registry.getClassMetadata(PrototypeClass.class).getConstructor();
        Assertions.assertFalse(new SingletonConstructorResolver().isApplicable(constructor));
    }

    // --- PrototypeConstructorResolver.isApplicable ---

    @Test
    @DisplayName("PrototypeConstructorResolver: isApplicable is true for @CafePrototype constructor")
    void prototypeResolverApplicableForPrototype() {
        CafeMetadataRegistry registry = CafeMetadataRegistry.builder()
                .withClass(PrototypeClass.class)
                .build();
        CafeConstructor constructor = registry.getClassMetadata(PrototypeClass.class).getConstructor();
        Assertions.assertTrue(new PrototypeConstructorResolver().isApplicable(constructor));
    }

    @Test
    @DisplayName("PrototypeConstructorResolver: isApplicable is false for @CafeSingleton constructor")
    void prototypeResolverNotApplicableForSingleton() {
        CafeMetadataRegistry registry = CafeMetadataRegistry.builder()
                .withClass(SingletonClass.class)
                .build();
        CafeConstructor constructor = registry.getClassMetadata(SingletonClass.class).getConstructor();
        Assertions.assertFalse(new PrototypeConstructorResolver().isApplicable(constructor));
    }

    // --- Singleton: same instance returned on multiple resolutions ---

    @Test
    @DisplayName("SingletonConstructorResolver: returns the same instance on repeated resolution")
    void singletonReturnsTheSameInstance() {
        CafeApplicationContext ctx = CafeApplicationContext.builder()
                .withClass(SingletonClass.class)
                .build();
        ctx.initialize();
        SingletonClass a = ctx.getInstance(SingletonClass.class);
        SingletonClass b = ctx.getInstance(SingletonClass.class);
        Assertions.assertSame(a, b);
    }

    // --- Prototype: distinct instances on each resolution ---

    @Test
    @DisplayName("PrototypeConstructorResolver: returns distinct instances on each resolution")
    void prototypeReturnsDistinctInstances() {
        CafeApplicationContext ctx = CafeApplicationContext.builder()
                .withClass(PrototypeClass.class)
                .build();
        ctx.initialize();
        PrototypeClass a = ctx.getInstance(PrototypeClass.class);
        PrototypeClass b = ctx.getInstance(PrototypeClass.class);
        Assertions.assertNotSame(a, b);
    }

    // --- Fixture classes ---

    @CafeSingleton
    static class SingletonClass {
    }

    @CafePrototype
    static class PrototypeClass {
    }
}
