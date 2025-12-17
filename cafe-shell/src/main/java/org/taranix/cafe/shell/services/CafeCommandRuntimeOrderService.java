package org.taranix.cafe.shell.services;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.beans.metadata.CafeBeansRegistry;
import org.taranix.cafe.beans.metadata.CafeClassMetadata;
import org.taranix.cafe.beans.metadata.CafeClassMetadataFactory;
import org.taranix.cafe.beans.services.CafeOrderedBeansService;
import org.taranix.cafe.shell.commands.CafeCommandRuntime;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@CafeService
@Slf4j

public class CafeCommandRuntimeOrderService {


    public List<CafeCommandRuntime> order(Collection<CafeCommandRuntime> commandRuntimes) {

        CafeBeansRegistry classDescriptors = createClassDescriptors(commandRuntimes);
        CafeOrderedBeansService beansOrder = CafeOrderedBeansService.from(classDescriptors);
        List<CafeClassMetadata> orderedClassInfo = beansOrder.orderedClasses();
        return orderedClassInfo.stream()
                .map(classInfo -> match(classInfo, commandRuntimes))
                .flatMap(Collection::stream)
                .toList();
    }

    private List<CafeCommandRuntime> match(CafeClassMetadata classInfo, Collection<CafeCommandRuntime> unOrdered) {
        return unOrdered.stream()
                .filter(cafeCommandRuntime -> cafeCommandRuntime.commandTypeKey().equals(classInfo.getRootClassTypeKey()))
                .toList();
    }

    private CafeBeansRegistry createClassDescriptors(Collection<CafeCommandRuntime> commandRuntimes) {
        return CafeBeansRegistry.builder()
                .withClasses(commandRuntimes.stream()
                        .map(CafeCommandRuntime::getExecutor)
                        .map(cafeMethodMetadata -> CafeClassMetadataFactory.create(cafeMethodMetadata.getParent().getRootClass()))
                        .map(CafeClassMetadata::getRootClass)
                        .collect(Collectors.toSet())
                )
                .build();
    }


}
