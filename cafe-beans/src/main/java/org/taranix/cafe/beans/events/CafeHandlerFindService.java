package org.taranix.cafe.beans.events;

import lombok.Getter;
import org.taranix.cafe.beans.repositories.Repository;
import org.taranix.cafe.beans.repositories.beans.BeanRepositoryEntry;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.HandlerTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.TypeKey;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CafeHandlerFindService {
    @Getter
    private final Repository<TypeKey, BeanRepositoryEntry> repository;

    public CafeHandlerFindService(Repository<TypeKey, BeanRepositoryEntry> repository) {
        this.repository = repository;
    }

    public Set find(Function<Annotation, Boolean> methodAnnotation,
                    Function<Annotation, Boolean> classAnnotation,
                    Function<BeanTypeKey[], Boolean> methodArguments) {
        Collection<HandlerTypeKey> matchedKeys = repository.getKeys(HandlerTypeKey.class)
                .filter(handlerTypeKey -> Arrays.stream(handlerTypeKey.getHandlerAnnotations()).anyMatch(methodAnnotation::apply)
                        && Arrays.stream(handlerTypeKey.getHandlerClassAnnotations()).anyMatch(classAnnotation::apply)
                        && methodArguments.apply(handlerTypeKey.getHandlerParameters())
                )
                .collect(Collectors.toSet());

        return Set.of();
    }


}
