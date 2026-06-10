package org.taranix.cafe.shell.fixtures;

import org.taranix.cafe.shell.annotations.CafeCommand;
import org.taranix.cafe.shell.annotations.CafeCommandRun;

@CafeCommand(command = "fail", longCommand = "fail", description = "Always throws")
public class FailCommand {

    @CafeCommandRun
    public void execute() {
        throw new RuntimeException("intended command failure");
    }
}
