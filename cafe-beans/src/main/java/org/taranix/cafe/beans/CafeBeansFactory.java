package org.taranix.cafe.beans;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.taranix.cafe.beans.annotations.modifiers.CafePrimary;
import org.taranix.cafe.beans.converters.CafeConverter;
import org.taranix.cafe.beans.exceptions.CafeBeansContextException;
import org.taranix.cafe.beans.metadata.CafeBeansDefinitionRegistry;
import org.taranix.cafe.beans.metadata.CafeClassInfo;
import org.taranix.cafe.beans.metadata.members.CafeMemberInfo;
import org.taranix.cafe.beans.metadata.members.CafeMethodInfo;
import org.taranix.cafe.beans.repositories.Repository;
import org.taranix.cafe.beans.repositories.beans.BeanRepositoryEntry;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.PropertyTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.TypeKey;
import org.taranix.cafe.beans.resolvers.CafeResolvers;
import org.taranix.cafe.beans.services.CafeOrderedBeansService;
import org.taranix.cafe.beans.validation.CafeValidationResultFormatter;
import org.taranix.cafe.beans.validation.CafeValidationService;
import org.taranix.cafe.beans.validation.ValidationResult;

import java.lang.reflect.Executable;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j

public final class CafeBeansFactory {


    private final Repository<TypeKey, BeanRepositoryEntry> repository;

    private final CafeValidationService cafeValidationService;

    @Getter
    private final CafeBeansDefinitionRegistry cafeBeansDefinitionRegistry;


    @Getter
    private final CafeResolvers resolvers;
    private final CafeOrderedBeansService orderedBeansService;

    public CafeBeansFactory(Repository<TypeKey, BeanRepositoryEntry> repository, CafeValidationService cafeValidationService, CafeBeansDefinitionRegistry cafeBeansDefinitionRegistry, CafeResolvers resolvers) {
        this.repository = repository;
        this.cafeValidationService = cafeValidationService;
        this.cafeBeansDefinitionRegistry = cafeBeansDefinitionRegistry;
        this.resolvers = resolvers;
        this.orderedBeansService = CafeOrderedBeansService.from(cafeBeansDefinitionRegistry);

    }

    public <S, T> CafeConverter<S, T> getConverter(Class<S> source, Class<T> target) {
        return repository.getMany(BeanTypeKey.from(CafeConverter.class, StringUtils.EMPTY, source, target))
                .stream()
                .map(BeanRepositoryEntry::getValue)
                .map(o -> (CafeConverter<S, T>) o)
                .findFirst()
                .orElse(null);
    }

    public void resolveAllBeans() {
        validate();
        orderedBeansService.orderedClasses()
                .forEach(this::resolveClass);
    }

    private void validate() {
        Set<ValidationResult> results = cafeValidationService.validate(cafeBeansDefinitionRegistry);
        String fullMessage = CafeValidationResultFormatter.format(results);
        if (StringUtils.isNoneBlank(fullMessage)) {
            throw new CafeBeansContextException(fullMessage);
        }
    }

    private void resolveClass(CafeClassInfo classDescriptor) {
        resolvers.findClassResolver(classDescriptor)
                .resolve(classDescriptor, this);
    }

    public void addToRepository(Object instance) {
        BeanTypeKey tk = BeanTypeKey.from(instance.getClass());
        repository.set(tk, BeanRepositoryEntry.builder()
                .primary(false)
                .value(instance)
                .source(null)
                .build());
    }

    public boolean hasBeenExecuted(Executable executable) {
        return repository.getAllKeys().stream()
                .anyMatch(typeKey -> repository.getMany(typeKey).stream()
                        .anyMatch(beanRepositoryEntry -> executable.equals(beanRepositoryEntry.getSource())));
    }


    public void persistSingleton(final CafeMemberInfo member, Object resolved) {
        if (member.isSingleton()) {
            persistAny(member, resolved, (Executable) member.getMember());
        }
    }

    public void persistAny(CafeMemberInfo member, Object resolved, Executable source) {
        if (Objects.isNull(resolved)) {
            return;
        }

        member.provides().forEach(typeKey ->
                repository.set(typeKey, BeanRepositoryEntry.builder()
                        .source(source)
                        .value(resolved)
                        .primary(member.getAnnotationModifiers().contains(CafePrimary.class))
                        .build()));
    }


    public Object getProperty(String key) {
        return repository.getOne(PropertyTypeKey.from(key)).getValue();
    }

    public boolean isResolved(BeanTypeKey typeKey) {
        return repository.contains(typeKey);
    }

    public boolean isMethodResolved(CafeMethodInfo methodInfo) {
        return repository.getMany(methodInfo.getMethodReturnTypeKey())
                .stream()
                .anyMatch(beanRepositoryEntry -> beanRepositoryEntry.getSource().equals(methodInfo.getMethod()));
    }

    /**
     * This method return value of the bean already resolved if is marked as Singleton, or
     * instantiated Bean if it's marked as Prototype.
     *
     * @param typeKey type of the bean
     * @return instance of the Bean (it can be Class or ParametrizedType instance)
     */
    public Object getBean(BeanTypeKey typeKey) {
        if (isResolved(typeKey)) {
            return getResolved(typeKey);
        }
        return getResolvers().findBeanTypekeyResolver(typeKey).resolve(typeKey, this);
    }

    public Collection<Object> getAllResolved(BeanTypeKey typeKey) {
        return repository.getMany(typeKey).stream()
                .map(BeanRepositoryEntry::getValue)
                .collect(Collectors.toSet());
    }

    public Object getResolved(BeanTypeKey typeKey) {
        return repository.getOne(typeKey).getValue();
    }

    public Object getResolved(CafeMethodInfo methodInfo) {
        return repository.getMany(methodInfo.getMethodReturnTypeKey()).stream()
                .filter(beanRepositoryEntry -> beanRepositoryEntry.getSource().equals(methodInfo.getMethod()))
                .map(BeanRepositoryEntry::getValue)
                .findFirst()
                .orElse(null);
    }

    public Object getBeanOrNull(BeanTypeKey typeKey) {
        if (isResolved(typeKey)) {
            return getResolved(typeKey);
        }
        return getResolvers().findBeanTypekeyResolver(typeKey).resolveOrNull(typeKey, this);
    }
}
