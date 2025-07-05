package org.taranix.cafe.beans.repositories.class_info;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.descriptors.CafeClassDescriptors;
import org.taranix.cafe.beans.descriptors.CafeMemberInfo;
import org.taranix.cafe.beans.diagnostics.ClassMemberDependencyGraphDiagramBuilder;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class MemberDependencyRepository extends DependencyRepository<CafeMemberInfo> {

    @Getter
    private final CafeClassDescriptors cafeClassDescriptors;

    private MemberDependencyRepository(CafeClassDescriptors cafeClassDescriptors) {
        this.cafeClassDescriptors = cafeClassDescriptors;
    }

    public static MemberDependencyRepository from(CafeClassDescriptors cafeClassDescriptors) {
        MemberDependencyRepository result = new MemberDependencyRepository(cafeClassDescriptors);
        Set<CafeMemberInfo> allMembers = cafeClassDescriptors.allMembers();

        for (CafeMemberInfo analyzingMember : allMembers) {
            for (BeanTypeKey requiredTypeKey : analyzingMember.dependencies()) {
                Set<CafeMemberInfo> matchedProviders = getProviders(allMembers, analyzingMember, requiredTypeKey);
                matchedProviders.forEach(provider ->
                        result.set(analyzingMember, provider)
                );
            }
        }
        return result;
    }

    private static Set<CafeMemberInfo> getProviders(final Set<CafeMemberInfo> allMembers,
                                                    final CafeMemberInfo targetMember,
                                                    final BeanTypeKey requiredTypeKey) {
        return allMembers.stream()
                //exclude member which doesn't provide anything
                .filter(providerMember -> !providerMember.provides().isEmpty())
                //exclude itself
                .filter(providerMember -> !providerMember.equals(targetMember))
                //exclude constructor as provider for same declaring class
                .filter(providerMember -> !(targetMember.isConstructor() && targetMember.hasSameDeclaringClass(providerMember)))
                // only providers for required TypeKey
                .filter(providerMember -> BeanTypeKey.isMatchByTypeOrGenericType(requiredTypeKey, providerMember.provides()))
                .collect(Collectors.toSet());
    }

    public void generateDiagram(String name) {
        String diagram = ClassMemberDependencyGraphDiagramBuilder
                .builder()
                .withComponents(getCafeClassDescriptors())
                .withRelations(this)
                .build();
        try {
            Files.write(Paths.get(name + ".puml"), Collections.singleton(diagram), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
