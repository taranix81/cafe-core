package org.taranix.cafe.shell.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.Option;
import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.beans.reflection.CafeAnnotationUtils;
import org.taranix.cafe.shell.CafeShell;
import org.taranix.cafe.shell.annotations.CafeCommand;
import org.taranix.cafe.shell.commands.CafeCommandArguments;
import org.taranix.cafe.shell.commands.CafeCommandRuntime;
import org.taranix.cafe.shell.exceptions.CafeCommandRuntimeServiceException;
import org.taranix.cafe.shell.resolvers.CafeCommandClassResolver;

import java.util.*;

@CafeService
@Slf4j
public class CafeCommandRuntimeService {

    @CafeInject
    private CafeShell cafeShell;

    @CafeInject
    private CafeCommandBindingService cafeCommandBindingService;

    @CafeInject
    private CafeCommandRuntimeOrderService orderService;

    public CafeCommandRuntime map(String[] args) {
        return Optional.ofNullable(cafeCommandBindingService.noOptedCommandBinding())
                .map(commandBinding -> commandBinding.asCommandRuntime(args))
                .orElse(null);
    }

    public List<CafeCommandRuntime> map(List<Option> options) {
        return options.stream()
                .map(option -> cafeCommandBindingService
                        .noOptedCommandBinding(option)
                        .map(commandBinding -> commandBinding.asCommandRuntime(option.getValues()))

                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public void run(Class<?> optedCommandClass, String... args) throws CafeCommandRuntimeServiceException {
        Option option = getOption(optedCommandClass);
        if (option == null) {
            throw new CafeCommandRuntimeServiceException("Class %s doesn't contain Option".formatted(optedCommandClass.getSimpleName()));
        }
        cafeCommandBindingService.noOptedCommandBinding(option)
                .map(commandBinding -> commandBinding.asCommandRuntime(args))
                .ifPresent(this::run);
    }

    private Option getOption(Class<?> commandClass) {
        CafeCommand cafeCommandAnnotation = CafeAnnotationUtils.getAnnotationByType(commandClass, CafeCommand.class);
        return CafeCommandClassResolver.buildOption(cafeCommandAnnotation);
    }


    public void run(List<CafeCommandRuntime> commandRuntimes) {
        commandRuntimes.forEach(this::run);
    }

    public void run(CafeCommandRuntime commandRuntime) {
        log.debug("Executing command : {} -> {}({})", commandRuntime.getExecutor().getCafeClassInfo().getTypeClass()
                , commandRuntime.getExecutor().getMethod().getName()
                , commandRuntime.getExecutor().getMethod().getParameterTypes());
        log.debug("CLI arguments -> {} ", commandRuntime.getArguments() != null ? Arrays.stream(commandRuntime.getArguments()).toList() : "null");
        CafeBeansFactory beansFactory = cafeShell.getBeansFactory();
        CafeCommandArguments commandArguments = cafeShell.getInstance(CafeCommandArguments.class);
        commandArguments.setValues(commandRuntime.getArguments());

        Object produced = beansFactory.getResolvers()
                .findMethodResolver(commandRuntime.getExecutor())
                .resolve(commandRuntime.getCommandInstance(), commandRuntime.getExecutor(), beansFactory);

        if (Objects.nonNull(produced)) {
            log.debug("Output -> {} ", produced);
        }

    }

    public List<CafeCommandRuntime> order(Collection<CafeCommandRuntime> commandRuntimes) {
        return orderService.order(commandRuntimes);
    }
}
