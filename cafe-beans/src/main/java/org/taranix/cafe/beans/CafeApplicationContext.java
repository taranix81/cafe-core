package org.taranix.cafe.beans;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.taranix.cafe.beans.exceptions.CafeApplicationContextException;
import org.taranix.cafe.beans.metadata.CafeClass;
import org.taranix.cafe.beans.metadata.CafeClassFactory;
import org.taranix.cafe.beans.metadata.CafeMetadataRegistry;
import org.taranix.cafe.beans.reflection.ClassScanner;
import org.taranix.cafe.beans.repositories.ListMultiRepository;
import org.taranix.cafe.beans.repositories.beans.BeanRepositoryEntry;
import org.taranix.cafe.beans.repositories.beans.BeansRepository;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.TypeKey;
import org.taranix.cafe.beans.resolvers.CafeResolvers;
import org.taranix.cafe.beans.resolvers.metadata.CafeClassResolver;
import org.taranix.cafe.beans.resolvers.metadata.constructor.CafeConstructorResolver;
import org.taranix.cafe.beans.resolvers.metadata.field.CafeFieldResolver;
import org.taranix.cafe.beans.resolvers.metadata.method.CafeMethodResolver;
import org.taranix.cafe.beans.resolvers.types.CafeBeanTypeResolver;
import org.taranix.cafe.beans.validation.CafeCycleDetectionValidator;
import org.taranix.cafe.beans.validation.CafeResolvableBeansValidator;
import org.taranix.cafe.beans.validation.CafeValidationService;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j

public class CafeApplicationContext {


    @Getter
    private final CafeBeansFactory beansFactory;

    @Getter
    private final CafeHandlersRegistry handlersRegistry;

    private CafeApplicationContext(
            CafeBeansFactory cafeBeansFactory,
            CafeHandlersRegistry handlersRegistry,
            final ClassLoader classLoader
    ) {
        this.beansFactory = cafeBeansFactory;
        this.handlersRegistry = handlersRegistry;
        CafePropertiesContext.load(beansFactory.getRepository(), classLoader);
    }

    public static BeansContextBuilder builder() {
        return new BeansContextBuilder();
    }


    public void initialize() {
        log.debug("Resolving all beans");
        beansFactory.resolveAllBeans();
    }


    public <T> Collection<T> getInstances(Class<T> clz) {
        return getInstances(clz, StringUtils.EMPTY);
    }

    public <T> Collection<T> getInstances(Class<T> clz, String identifier) {
        Object instance = beansFactory.getBean((BeanTypeKey.from(Set.class, identifier, clz)));
        if (instance instanceof Set) {
            return ((Set<?>) instance).stream()
                    .filter(clz::isInstance)
                    .map(clz::cast)
                    .collect(Collectors.toSet());
        }
        throw new CafeApplicationContextException("No instance of %s found".formatted(clz));
    }

    /**
     * Method return instance of resolved class if class is marked as Singleton, or
     * instantiate Prototype class.
     *
     * @param clz - class type
     * @return instance of the class
     */
    public <T> T getInstance(Class<T> clz) {
        return getInstance(clz, StringUtils.EMPTY);
    }

    /**
     * Method return instance of resolved class if class is marked as Singleton, or
     * instantiate Prototype class.
     *
     * @param clz        - class type
     * @param identifier - custom name for the class's instance
     * @return instance of the class
     */
    public <T> T getInstance(Class<T> clz, String identifier) {
        Object instance = beansFactory.getBean(BeanTypeKey.from(clz, identifier));
        if (clz.isInstance(instance)) {
            return clz.cast(instance);
        }
        throw new CafeApplicationContextException("Now instance of %s found".formatted(clz));
    }

//    public CafeClassMetadata getClassDescriptor(Class<?> clz) {
//        return beansFactory.getCafeBeansRegistry().findClassMetadata(clz);
//    }

    public Object getProperty(String propertyName) {
        return beansFactory.getProperty(propertyName);
    }

    public void refresh(Object object) {
        Class<?> clx = object.getClass();
        CafeClass cci = CafeClassFactory.create(clx);

        if (cci.isSingleton()) {
            cci.getFields().forEach(
                    cafeFieldInfo -> beansFactory.getResolvers().findFieldResolver(cafeFieldInfo).resolve(object, cafeFieldInfo, beansFactory)
            );
        }

    }

    public static final class BeansContextBuilder {

        private final Set<CafeClassResolver> classResolvers = new HashSet<>();
        private final Set<CafeConstructorResolver> constructorResolvers = new HashSet<>();
        private final Set<CafeMethodResolver> methodResolvers = new HashSet<>();
        private final Set<CafeFieldResolver> fieldResolvers = new HashSet<>();
        private final Set<Class<?>> classesToBeResolved = new HashSet<>();

        private final Set<CafeBeanTypeResolver> typeResolvers = new HashSet<>();
        private String[] packages;

        private ListMultiRepository<TypeKey, BeanRepositoryEntry> repository;

        private CafeValidationService cafeValidationService;

        private ClassLoader classLoader;


        public BeansContextBuilder withPackageScan(String... packages) {
            this.packages = packages;
            return this;
        }


        public BeansContextBuilder withClassResolver(Set<CafeClassResolver> cafeTypeResolvers) {
            classResolvers.addAll(cafeTypeResolvers);
            return this;
        }

        public BeansContextBuilder repository(ListMultiRepository<TypeKey, BeanRepositoryEntry> repository) {
            this.repository = repository;
            return this;
        }

        public BeansContextBuilder withMethodResolver(Set<CafeMethodResolver> cafeMethodResolvers) {
            methodResolvers.addAll(cafeMethodResolvers);
            return this;
        }

        public BeansContextBuilder withClass(Class<?> cls) {
            classesToBeResolved.add(cls);
            return this;
        }

        public BeansContextBuilder withClasses(Set<Class<?>> classes) {
            this.classesToBeResolved.addAll(classes);
            return this;
        }

        public BeansContextBuilder withClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }


        public CafeApplicationContext build() {

            Set<Class<?>> allClasses = Stream.concat(classesToBeResolved.stream()
                            , ClassScanner.getInstance().scan(packages).stream())
                    .collect(Collectors.toSet());
            CafeMetadataRegistry metadataRegistry = CafeMetadataRegistry.builder()
                    .withClasses(allClasses)
                    .build();

            if (cafeValidationService == null) {
                cafeValidationService = new CafeValidationService(
                        Set.of(
                                new CafeCycleDetectionValidator(),
                                new CafeResolvableBeansValidator())
                );
            }


            if (classLoader == null) {
                classLoader = Thread.currentThread().getContextClassLoader();
            }

            if (repository == null) {
                repository = new ListMultiRepository<>(List.of(new BeansRepository()));
            }

            CafeResolvers cafeResolvers = new CafeResolvers();
            cafeResolvers.add(classResolvers.toArray(CafeClassResolver[]::new));
            cafeResolvers.add(constructorResolvers.toArray(CafeConstructorResolver[]::new));
            cafeResolvers.add(fieldResolvers.toArray(CafeFieldResolver[]::new));
            cafeResolvers.add(methodResolvers.toArray(CafeMethodResolver[]::new));
            cafeResolvers.add(typeResolvers.toArray(CafeBeanTypeResolver[]::new));

            CafeBeansFactory beansFactory1 = new CafeBeansFactory(repository, cafeValidationService, metadataRegistry, cafeResolvers);
            CafeHandlersRegistry handlersRegistry1 = new CafeHandlersRegistry();

            return new CafeApplicationContext(beansFactory1, handlersRegistry1, classLoader);
        }


    }
}
