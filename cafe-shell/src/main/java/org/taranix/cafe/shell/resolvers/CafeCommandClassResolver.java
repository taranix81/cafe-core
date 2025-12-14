package org.taranix.cafe.shell.resolvers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.metadata.CafeClassInfo;
import org.taranix.cafe.beans.metadata.members.CafeMethodInfo;
import org.taranix.cafe.beans.resolvers.classInfo.AbstractClassResolver;
import org.taranix.cafe.shell.annotations.CafeCommand;
import org.taranix.cafe.shell.commands.CafeCommandOptionBinding;

import java.lang.annotation.Annotation;


@Slf4j
public class CafeCommandClassResolver extends AbstractClassResolver {

    public static Option buildOption(CafeCommand cafeCommandAnnotation) {
        if (isOptedCommand(cafeCommandAnnotation)) {
            Option option = new Option(cafeCommandAnnotation.command(), cafeCommandAnnotation.description());
            option.setArgs(cafeCommandAnnotation.noOfArgs());
            option.setLongOpt(cafeCommandAnnotation.longCommand());
            option.setOptionalArg(cafeCommandAnnotation.hasOptionalArgument());
            option.setArgName(cafeCommandAnnotation.argumentName());
            option.setRequired(cafeCommandAnnotation.required());
            option.setValueSeparator(cafeCommandAnnotation.valueSeparator());
            return option;
        }
        return null;

    }

    private static boolean isOptedCommand(CafeCommand cafeCommandAnnotation) {
        return StringUtils.isNotBlank(cafeCommandAnnotation.command())
                && StringUtils.isNotBlank(cafeCommandAnnotation.description());
    }

    @Override
    public Object resolve(CafeClassInfo classInfo, CafeBeansFactory beansFactory) {
        CafeCommand cafeCommandAnnotation = classInfo.getClassAnnotation(CafeCommand.class);
        Object commandInstance = super.resolve(classInfo, beansFactory);
        Option option = buildOption(cafeCommandAnnotation);
        if (option != null) {
            beansFactory.addToRepository(option);
        }
        //CafeMethodInfo methodExecutor = getExecutorMethodInfo(classInfo);
        //CafeCommandOptionBinding commandOptionBinding = buildCommandBinding(commandInstance, methodExecutor, option);
        //beansFactory.addToRepository(commandOptionBinding);
        return commandInstance;
    }

    @Override
    protected void resolveMethod(CafeBeansFactory cafeBeansFactory, Object instance, CafeMethodInfo descriptor) {
        //We skip resolving method at this stage. Method will be resolved on demand.
    }

    private CafeCommandOptionBinding buildCommandBinding(Object commandExecutable, CafeMethodInfo executor, Option option) {
        return CafeCommandOptionBinding.builder()
                .optionBinding(option)
                .commandInstance(commandExecutable)
                .executor(executor)
                .build();
    }

//    private CafeMethodInfo getExecutorMethodInfo(CafeClassDescriptor cafeClassDescriptor) {
//        return cafeClassDescriptor.findAllMethodsAnnotatedBy(CafeCommandRun.class).stream()
//                .findFirst()
//                .orElseThrow(() -> new CafeCommandClassResolverException("No method annotated by @CafeCommandRun for command %s".formatted(cafeClassDescriptor.getTypeClass().getName())));
//    }

    @Override
    public boolean isApplicable(CafeClassInfo cafeClassInfo) {
        return true;
    }

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return CafeCommand.class.equals(annotation);
    }
}
