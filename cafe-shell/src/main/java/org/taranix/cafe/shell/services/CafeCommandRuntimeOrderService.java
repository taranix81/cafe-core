package org.taranix.cafe.shell.services;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.beans.metadata.CafeBeansDefinitionRegistry;
import org.taranix.cafe.beans.metadata.CafeClassInfo;
import org.taranix.cafe.beans.metadata.members.CafeMemberInfo;
import org.taranix.cafe.beans.services.CafeOrderedBeansService;
import org.taranix.cafe.shell.commands.CafeCommandRuntime;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@CafeService
@Slf4j

public class CafeCommandRuntimeOrderService {


    public List<CafeCommandRuntime> order(Collection<CafeCommandRuntime> commandRuntimes) {

        CafeBeansDefinitionRegistry classDescriptors = createClassDescriptors(commandRuntimes);
        CafeOrderedBeansService beansOrder = CafeOrderedBeansService.from(classDescriptors);
        List<CafeClassInfo> orderedClassInfo = beansOrder.orderedClasses();
        return orderedClassInfo.stream()
                .map(classInfo -> match(classInfo, commandRuntimes))
                .flatMap(Collection::stream)
                .toList();
    }

    private List<CafeCommandRuntime> match(CafeClassInfo classInfo, Collection<CafeCommandRuntime> unOrdered) {
        return unOrdered.stream()
                .filter(cafeCommandRuntime -> cafeCommandRuntime.commandTypeKey().equals(classInfo.typeKey()))
                .toList();
    }

    private CafeBeansDefinitionRegistry createClassDescriptors(Collection<CafeCommandRuntime> commandRuntimes) {
        return CafeBeansDefinitionRegistry.builder()
                .withClasses(commandRuntimes.stream()
                        .map(CafeCommandRuntime::getExecutor)
                        .map(CafeMemberInfo::getCafeClassInfo)
                        .map(CafeClassInfo::getTypeClass)
                        .collect(Collectors.toSet())
                )
                .build();
    }


}
