package org.taranix.cafe.shell;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.taranix.cafe.beans.CafeApplication;
import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.resolvers.classInfo.CafeClassResolver;
import org.taranix.cafe.beans.resolvers.classInfo.method.CafeMethodResolver;
import org.taranix.cafe.shell.annotations.CafeCommand;
import org.taranix.cafe.shell.commands.CafeCommandRuntime;
import org.taranix.cafe.shell.commands.PrintHelpCommand;
import org.taranix.cafe.shell.exceptions.CafeCommandRuntimeServiceException;
import org.taranix.cafe.shell.resolvers.CafeCommandClassResolver;
import org.taranix.cafe.shell.resolvers.CafeCommandMethodResolver;
import org.taranix.cafe.shell.services.CafeCommandRuntimeService;

import java.lang.annotation.Annotation;
import java.util.*;


@Slf4j
public class CafeShell extends CafeApplication {

    public static final int SUCCESS = 0;
    public static final int ERROR = 1;
    private static final int WRONG_ARGUMENT_ERROR = 2;
    @CafeInject
    private CafeCommandRuntimeService runtimeService;

    @CafeInject
    private CommandLineParser commandLineParser;
    @CafeInject
    private Options options;

    public CafeShell(Class<?> applicationConfigClass) {
        super(applicationConfigClass);
    }

    @Override
    protected void postContextInit() {
        super.postContextInit();
        createApacheOptions();
    }

    @Override
    protected void beforeContextInit() {
        addBeanToContext(this);
    }

    @Override
    protected Set<Class<? extends Annotation>> getCustomAnnotations() {
        return Set.of(CafeCommand.class);
    }

    @Override
    protected Set<CafeClassResolver> getCustomClassResolvers() {
        return Set.of(new CafeCommandClassResolver());
    }

    @Override
    protected Set<CafeMethodResolver> getCustomMethodResolvers() {
        return Set.of(new CafeCommandMethodResolver());
    }

    protected int execute(String... args) {
        List<CafeCommandRuntime> executionCommands = new ArrayList<>();
        String[] leftArgs = null;
        if (args != null) {
            try {
                log.debug("Arguments : {}", Arrays.stream(args).toList());
                CommandLine commandLine = commandLineParser.parse(options, args);
                leftArgs = commandLine.getArgs();
                Option[] matchedOptions = commandLine.getOptions();
                if (matchedOptions.length > 0) {
                    Collection<CafeCommandRuntime> commandRuntimes = runtimeService.map(List.of(matchedOptions));
                    List<CafeCommandRuntime> orderedCommandRuntimes = runtimeService.order(commandRuntimes);
                    executionCommands.addAll(orderedCommandRuntimes);
                }

            } catch (ParseException e) {
                System.out.println(e.getMessage());
                try {
                    runtimeService.run(PrintHelpCommand.class);
                } catch (CafeCommandRuntimeServiceException ex) {
                    System.out.println(e.getMessage());
                    log.debug(e.getMessage(), ex);
                    return ERROR;
                }
                return WRONG_ARGUMENT_ERROR;
            }
        }

        CafeCommandRuntime defaultCommand = runtimeService.map(leftArgs);
        if (defaultCommand != null) {
            executionCommands.add(runtimeService.map(leftArgs));
        }

        try {
            log.debug("Matched commands : {}", executionCommands);
            if (executionCommands.isEmpty()) {
                runtimeService.run(PrintHelpCommand.class);
            } else {
                runtimeService.run(executionCommands);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ERROR;
        }

        return SUCCESS;
    }

    private void createApacheOptions() {
        // Option can not be resolved during Context initialization
        // and need to be manually created
        getInstances(Option.class).forEach(options::addOption);
    }

}

