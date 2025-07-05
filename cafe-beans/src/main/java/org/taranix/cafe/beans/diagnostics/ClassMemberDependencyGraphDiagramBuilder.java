package org.taranix.cafe.beans.diagnostics;

import org.taranix.cafe.beans.descriptors.CafeClassDescriptors;
import org.taranix.cafe.beans.descriptors.CafeClassInfo;
import org.taranix.cafe.beans.descriptors.CafeMemberInfo;
import org.taranix.cafe.beans.repositories.class_info.DependencyRepository;

import java.lang.reflect.Executable;

public class ClassMemberDependencyGraphDiagramBuilder {

    public static final String STARTUML = "@startuml";
    public static final String ENDUML = "@enduml";
    public static final String NEW_LINE = "\n";
    public static final String RELATION_TYPE = "-->";
    public static final String AS = " as ";

    private final StringBuilder components;
    private final StringBuilder graphDiagram;
    private final StringBuilder relations;

    private ClassMemberDependencyGraphDiagramBuilder() {
        this.components = new StringBuilder();
        this.graphDiagram = new StringBuilder();
        this.relations = new StringBuilder();
    }

    public static ClassMemberDependencyGraphDiagramBuilder builder() {
        return new ClassMemberDependencyGraphDiagramBuilder();
    }

    private String generateElementId(CafeMemberInfo memberInfo) {
        return "%s.%s.%s".formatted(memberInfo.getOwnerClassTypeKey().getType().getTypeName()
                , memberInfo.getMember().getName()
                , getMemberType(memberInfo));
    }

    private String generateElementLabel(CafeMemberInfo memberInfo) {
        String memberType = getMemberType(memberInfo);
        return "[\"(%s) %s%s\"]".formatted(memberType
                , memberInfo.getMember().getName()
                , memberInfo.getMember() instanceof Executable ? "()" : "");
    }

    private String getMemberType(CafeMemberInfo memberInfo) {
        if (memberInfo.isConstructor()) {
            return "Constructor";
        }

        if (memberInfo.isMethod()) {
            return "Method";
        }

        if (memberInfo.isField()) {
            return "Field";
        }
        return "";
    }

    public ClassMemberDependencyGraphDiagramBuilder withComponents(CafeClassDescriptors components) {
        for (CafeClassInfo cafeClassInfo : components.descriptors()) {
            StringBuilder component = new StringBuilder("package " + cafeClassInfo.getTypeClass().getName() + " {");

            component.append(NEW_LINE);
            if (cafeClassInfo.constructor() != null) {
                component.append(generateElementLabel(cafeClassInfo.constructor()))
                        .append(AS)
                        .append(generateElementId(cafeClassInfo.constructor()))
                        .append(NEW_LINE);
            }

            cafeClassInfo.fields().forEach(field -> {
                component.append(generateElementLabel(field))
                        .append(AS)
                        .append(generateElementId(field))
                        .append(NEW_LINE);
            });

            cafeClassInfo.methods().forEach(method -> {
                component.append(generateElementLabel(method))
                        .append(AS)
                        .append(generateElementId(method))
                        .append(NEW_LINE);
            });

            component.append("}").append(NEW_LINE);
            this.components.append(component).append(NEW_LINE);
        }

        return this;
    }

    public ClassMemberDependencyGraphDiagramBuilder withRelations(DependencyRepository<CafeMemberInfo> repository) {
        for (CafeMemberInfo member : repository.getAllKeys()) {
            String memberFrom = generateElementId(member);
            repository.getMany(member).forEach(depMember -> {
                        String memberTo = generateElementId(depMember);
                        relations.append(memberFrom)
                                .append(RELATION_TYPE)
                                .append(memberTo)
                                .append(NEW_LINE);
                    }
            );
        }

        return this;
    }


    public String build() {
        this.graphDiagram.append(STARTUML).append(NEW_LINE)
                .append(components).append(NEW_LINE)
                .append(relations).append(NEW_LINE)
                .append(ENDUML).append(NEW_LINE);
        return graphDiagram.toString();
    }
}
