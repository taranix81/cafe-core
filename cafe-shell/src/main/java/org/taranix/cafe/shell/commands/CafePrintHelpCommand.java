package org.taranix.cafe.shell.commands;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.annotations.fields.CafeProperty;
import org.taranix.cafe.shell.annotations.CafeCommand;
import org.taranix.cafe.shell.annotations.CafeCommandRun;

@CafeCommand(command = "h", longCommand = "help", description = "Printing help")
public final class CafePrintHelpCommand {

    @CafeProperty(name = "cafe.shell.name")
    private String applicationName;
    @CafeInject
    private HelpFormatter formatter;
    @CafeInject
    private Options options;

    @CafeCommandRun
    public void execute(CafeCommandArguments cafeCommandArguments) {
        String name = applicationName != null ? applicationName : "Application";
        formatter.printHelp(name, options, true);
    }
}
