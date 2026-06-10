package org.taranix.cafe.shell.fixtures;

import org.taranix.cafe.shell.annotations.CafeCommand;
import org.taranix.cafe.shell.annotations.CafeCommandRun;
import org.taranix.cafe.shell.commands.CafeCommandArguments;

@CafeCommand(command = "greet", longCommand = "greet", description = "Prints a greeting")
public class GreetCommand {

    public static boolean invoked = false;
    public static String[] receivedArgs = null;

    public static void reset() {
        invoked = false;
        receivedArgs = null;
    }

    @CafeCommandRun
    public void execute(CafeCommandArguments args) {
        invoked = true;
        receivedArgs = args.getValues().orElse(new String[0]);
    }
}
