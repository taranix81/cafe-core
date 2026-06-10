package org.taranix.cafe.shell.fixtures;

import org.taranix.cafe.shell.annotations.CafeCommand;
import org.taranix.cafe.shell.annotations.CafeCommandRun;
import org.taranix.cafe.shell.commands.CafeCommandArguments;

// No command name → treated as default command (receives leftover positional args)
@CafeCommand(description = "Handles positional arguments")
public class DefaultCommand {

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
