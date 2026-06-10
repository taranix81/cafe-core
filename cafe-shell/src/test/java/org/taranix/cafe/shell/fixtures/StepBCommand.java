package org.taranix.cafe.shell.fixtures;

import org.taranix.cafe.shell.annotations.CafeCommand;
import org.taranix.cafe.shell.annotations.CafeCommandRun;

@CafeCommand(command = "b", longCommand = "step-b", description = "Step B")
public class StepBCommand {

    public static int invokedAtStep = -1;

    public static void reset() {
        invokedAtStep = -1;
    }

    @CafeCommandRun
    public void execute() {
        invokedAtStep = InvocationOrder.next();
    }
}
