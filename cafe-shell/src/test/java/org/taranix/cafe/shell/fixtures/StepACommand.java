package org.taranix.cafe.shell.fixtures;

import org.taranix.cafe.shell.annotations.CafeCommand;
import org.taranix.cafe.shell.annotations.CafeCommandRun;

// Depends on StepBCommand — must run after B
@CafeCommand(command = "a", longCommand = "step-a", description = "Step A", dependsOn = {StepBCommand.class})
public class StepACommand {

    public static int invokedAtStep = -1;

    public static void reset() {
        invokedAtStep = -1;
    }

    @CafeCommandRun
    public void execute() {
        invokedAtStep = InvocationOrder.next();
    }
}
