package org.taranix.cafe.shell.resolvers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.taranix.cafe.beans.metadata.CafeClass;
import org.taranix.cafe.beans.metadata.CafeMethod;
import org.taranix.cafe.beans.resolvers.CafeBeansFactory;
import org.taranix.cafe.beans.resolvers.metadata.AbstractClassResolver;
import org.taranix.cafe.shell.annotations.CafeCommand;
import org.taranix.cafe.shell.annotations.CafeCommandRun;
import org.taranix.cafe.shell.commands.CafeCommandOptionBinding;
import org.taranix.cafe.shell.exceptions.CafeCommandClassResolverException;

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

    public static boolean isOptedCommand(CafeCommand cafeCommandAnnotation) {
        return StringUtils.isNotBlank(cafeCommandAnnotation.command())
                && StringUtils.isNotBlank(cafeCommandAnnotation.description());
    }

    @Override
    public Object resolve(CafeClass classInfo, CafeBeansFactory beansFactory) {
        CafeCommand cafeCommandAnnotation = classInfo.getRootClassAnnotation(CafeCommand.class);
        Object commandInstance = super.resolve(classInfo, beansFactory);

        Option option = buildOption(cafeCommandAnnotation);
        if (option != null) {
            beansFactory.addToRepository(option);
        }

        CafeMethod executorMethod = classInfo.getMethods().stream()
                .filter(m -> m.hasAnnotation(CafeCommandRun.class))
                .findFirst()
                .orElseThrow(() -> new CafeCommandClassResolverException(
                        "No @CafeCommandRun method found in: " + classInfo.getRootClass().getName()));

        beansFactory.addToRepository(CafeCommandOptionBinding.builder()
                .optionBinding(option)
                .commandInstance(commandInstance)
                .executor(executorMethod)
                .build());

        return commandInstance;
    }

    @Override
    protected void resolveMethod(CafeBeansFactory cafeBeansFactory, Object instance, CafeMethod descriptor) {
        // Methods are resolved on demand by CafeCommandRuntimeService.run(), not during context init.
    }

    @Override
    public boolean isApplicable(CafeClass cafeClass) {
        return true;
    }

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return CafeCommand.class.equals(annotation);
    }
}
