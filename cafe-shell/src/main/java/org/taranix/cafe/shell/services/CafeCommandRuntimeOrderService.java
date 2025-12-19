package org.taranix.cafe.shell.services;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.annotations.classes.CafeService;
import org.taranix.cafe.beans.metadata.CafeClass;
import org.taranix.cafe.beans.metadata.CafeClassFactory;
import org.taranix.cafe.beans.metadata.CafeMetadataRegistry;
import org.taranix.cafe.beans.services.CafeOrderedBeansService;
import org.taranix.cafe.shell.commands.CafeCommandRuntime;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@CafeService
@Slf4j

public class CafeCommandRuntimeOrderService {


    public List<CafeCommandRuntime> order(Collection<CafeCommandRuntime> commandRuntimes) {

        CafeMetadataRegistry classDescriptors = createClassDescriptors(commandRuntimes);
        CafeOrderedBeansService beansOrder = CafeOrderedBeansService.from(classDescriptors);
        List<CafeClass> orderedClassInfo = beansOrder.orderedClasses();
        return orderedClassInfo.stream()
                .map(classInfo -> match(classInfo, commandRuntimes))
                .flatMap(Collection::stream)
                .toList();
    }

    private List<CafeCommandRuntime> match(CafeClass classInfo, Collection<CafeCommandRuntime> unOrdered) {
        return unOrdered.stream()
                .filter(cafeCommandRuntime -> cafeCommandRuntime.commandTypeKey().equals(classInfo.getRootClassTypeKey()))
                .toList();
    }

    private CafeMetadataRegistry createClassDescriptors(Collection<CafeCommandRuntime> commandRuntimes) {
        return CafeMetadataRegistry.builder()
                .withClasses(commandRuntimes.stream()
                        .map(CafeCommandRuntime::getExecutor)
                        .map(cafeMethodMetadata -> CafeClassFactory.create(cafeMethodMetadata.getParent().getRootClass()))
                        .map(CafeClass::getRootClass)
                        .collect(Collectors.toSet())
                )
                .build();
    }


}
