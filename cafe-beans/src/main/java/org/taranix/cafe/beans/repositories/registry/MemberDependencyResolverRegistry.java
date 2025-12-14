package org.taranix.cafe.beans.repositories.registry;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.metadata.CafeClassInfo;
import org.taranix.cafe.beans.metadata.members.CafeMemberInfo;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.TypeKey;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class MemberDependencyResolverRegistry extends AbstractDependencyRegistry<CafeMemberInfo> {


    /**
     * Map each member in class with member of provider class
     *
     * @param cafeClassInfos
     * @return
     */
    public static MemberDependencyResolverRegistry from(Set<CafeClassInfo> cafeClassInfos) {
        MemberDependencyResolverRegistry result = new MemberDependencyResolverRegistry();
        Set<CafeMemberInfo> allMembers = allMembers(cafeClassInfos);

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

    private static Set<CafeMemberInfo> allMembers(Set<CafeClassInfo> cafeClassInfos) {
        return cafeClassInfos.stream()
                .flatMap(cd -> cd.getMembers().stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<CafeMemberInfo> providers(CafeMemberInfo target, TypeKey typeKey) {
        return providers(target).stream()
                .filter(memberDescriptor -> memberDescriptor.provides().contains(typeKey))
                .collect(Collectors.toSet());
    }

    public Set<CafeMemberInfo> providers(CafeMemberInfo target) {
        return new HashSet<>(getMany(target));
    }

    public Set<BeanTypeKey> providersTypeKeys(CafeMemberInfo targetMember) {
        return providers(targetMember).stream()
                .map(CafeMemberInfo::provides)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

    }
}
