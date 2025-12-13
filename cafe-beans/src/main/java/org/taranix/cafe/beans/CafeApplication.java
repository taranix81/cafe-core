package org.taranix.cafe.beans;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.annotations.CafeAnnotationUtils;
import org.taranix.cafe.beans.converters.CafeConverter;
import org.taranix.cafe.beans.exceptions.CafeApplicationException;
import org.taranix.cafe.beans.repositories.beans.BeanRepositoryEntry;
import org.taranix.cafe.beans.repositories.beans.BeansRepository;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.resolvers.classInfo.CafeClassResolver;
import org.taranix.cafe.beans.resolvers.classInfo.method.CafeMethodResolver;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
public abstract class CafeApplication {

    private final Class<?> applicationConfigClass;

    private final CafeApplicationContext cafeApplicationContext;

    protected CafeApplication(Class<?> applicationConfigClass) {
        this.applicationConfigClass = validateApplicationClass(applicationConfigClass);
        this.cafeApplicationContext = createApplicationContext(applicationConfigClass);
        beforeContextInit();
        cafeApplicationContext.initialize();
        postContextInit();
    }

    protected void postContextInit() {
        cafeApplicationContext.refresh(this);
    }

    protected void beforeContextInit() {
        //do nothing
    }

    public void addBeanToContext(Object object) {
        addBeanToContext(object, false);
    }

    public void addBeanToContext(Object object, boolean asPrimary) {
        cafeApplicationContext.getRepository().set(BeanTypeKey.from(object.getClass()), BeanRepositoryEntry.builder()
                .value(object)
                .primary(asPrimary)
                .source(null)
                .build());
    }

    public void addRepositoryToContext(BeansRepository repository) {
        cafeApplicationContext.getRepository().addRepository(repository);
    }

    protected Class<?> validateApplicationClass(Class<?> applicationConfigClass) {
        if (!CafeAnnotationUtils.isAnnotationPresent(applicationConfigClass, org.taranix.cafe.beans.annotations.CafeApplication.class)) {
            throw new CafeApplicationException("Missing annotation :" + org.taranix.cafe.beans.annotations.CafeApplication.class.getName());
        }
        return applicationConfigClass;
    }

    private CafeApplicationContext createApplicationContext(Class<?> applicationConfigClass) {
        return CafeApplicationContext.builder()
                .withClassResolver(getCustomClassResolvers())
                .withMethodResolver(getCustomMethodResolvers())
                .withPackageScan(getPackages())
                .withClassLoader(this.getClass().getClassLoader())
                .withClass(applicationConfigClass)
                .build();
    }

    protected Set<CafeClassResolver> getCustomClassResolvers() {
        return Set.of();
    }

    protected Set<CafeMethodResolver> getCustomMethodResolvers() {
        return Set.of();
    }

    protected String[] getPackages() {
        Set<String> packages = new HashSet<>();

        //Built in service
        packages.add(CafeConverter.class.getPackageName());

        //Add package for class extending CafeApplication
        packages.add(this.getClass().getPackageName());

        //Configuration class packages
        String[] customPackages = getCafeApplicationAnnotation().packages();
        if (customPackages.length == 0) {
            packages.add(applicationConfigClass.getPackageName());
        } else {
            packages.addAll(Arrays.stream(customPackages).collect(Collectors.toSet()));
        }
        log.debug("Packages to scan: {}", packages);
        return packages.toArray(new String[]{});
    }

    protected org.taranix.cafe.beans.annotations.CafeApplication getCafeApplicationAnnotation() {
        return applicationConfigClass.getAnnotation(org.taranix.cafe.beans.annotations.CafeApplication.class);
    }

    /**
     * Execute application
     *
     * @param args, arguments from console
     * @return 0 is Successfully execute the application, otherwise value other than 0
     */
    public int run(String[] args) {
        return execute(args);
    }

    protected abstract int execute(String[] args);


    public <T> Collection<T> getInstances(Class<T> clz) {
        return cafeApplicationContext.getInstances(clz);
    }

    public <T> Collection<T> getInstances(Class<T> clz, String identifier) {
        return cafeApplicationContext.getInstances(clz, identifier);
    }

    public <T> T getInstance(Class<T> clz) {
        return cafeApplicationContext.getInstance(clz);
    }

    public <T> T getInstance(Class<T> clz, String identifier) {
        return cafeApplicationContext.getInstance(clz, identifier);
    }

    public CafeBeansFactory getBeansFactory() {
        return cafeApplicationContext.getBeansFactory();
    }
}
