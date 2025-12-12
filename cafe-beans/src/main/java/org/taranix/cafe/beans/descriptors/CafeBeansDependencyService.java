package org.taranix.cafe.beans.descriptors;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.repositories.class_info.ClassDependencyRepository;
import org.taranix.cafe.beans.repositories.class_info.MemberDependencyRepository;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.TypeKey;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
public class CafeBeansDependencyService {

    private final MemberDependencyRepository memberDependencies;
    private final ClassDependencyRepository classDependencies;

    private CafeBeansDependencyService(CafeClassDescriptors cafeClassDescriptors) {
        this.memberDependencies = MemberDependencyRepository.from(cafeClassDescriptors);
        this.classDependencies = ClassDependencyRepository.from(cafeClassDescriptors);
    }

    public static CafeBeansDependencyService from(CafeClassDescriptors cafeClassDescriptors) {
        return new CafeBeansDependencyService(cafeClassDescriptors);
    }

    /*
     * Function check if there is a cycle among MemberDependency set
     *
     * @return true, if cycle exist, otherwise false
     */
    public boolean hasCycleBetweenMembers() {
        return !membersCycleSet().isEmpty();
    }

    Collection<CafeMemberInfo> membersCycleSet() {
        if (log.isTraceEnabled()) {
            memberDependencies.generateDiagram("members-dependency");
        }
        return memberDependencies.cycleSet();
    }

    public boolean hasCycleBetweenClasses() {
        return !classCycleSet().isEmpty();
    }

    Collection<CafeClassDescriptor> classCycleSet() {
        if (log.isTraceEnabled()) {
            classDependencies.generateDiagram("class-dependency");
        }
        return classDependencies.cycleSet();
    }

    public Set<CafeClassDescriptor> providersForClass(CafeClassDescriptor target) {
        return new HashSet<>(classDependencies.getMany(target));
    }

    public Set<CafeMemberInfo> providers(CafeMemberInfo target, TypeKey typeKey) {
        return providers(target).stream()
                .filter(memberDescriptor -> memberDescriptor.provides().contains(typeKey))
                .collect(Collectors.toSet());
    }

    public Set<CafeMemberInfo> providers(CafeMemberInfo target) {
        return new HashSet<>(memberDependencies.getMany(target));
    }


    public Set<BeanTypeKey> providedTypeKeys(CafeMemberInfo targetMember) {
        return memberDependencies.getMany(targetMember).stream()
                .map(CafeMemberInfo::provides)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

    }

}
