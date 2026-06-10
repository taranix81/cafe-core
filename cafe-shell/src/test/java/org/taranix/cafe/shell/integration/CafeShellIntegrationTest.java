package org.taranix.cafe.shell.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.shell.CafeShell;
import org.taranix.cafe.shell.fixtures.*;

import static org.junit.jupiter.api.Assertions.*;

class CafeShellIntegrationTest {

    // Each test gets a fresh shell scoped to the relevant command set.
    // CafeShell scans the package of the config class, so fixture commands
    // in the same package are picked up automatically.

    @BeforeEach
    void resetStaticState() {
        GreetCommand.reset();
        DefaultCommand.reset();
        StepACommand.reset();
        StepBCommand.reset();
        InvocationOrder.reset();
    }

    // ---------------------------------------------------------------------------
    // Single command execution
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("Single command")
    class SingleCommand {

        @Test
        @DisplayName("returns SUCCESS and invokes command method")
        void executesMatchedCommand() {
            CafeShell shell = new CafeShell(TestShellConfig.class);
            int code = shell.run(new String[]{"--greet"});
            assertEquals(CafeShell.SUCCESS, code);
            assertTrue(GreetCommand.invoked);
        }

        @Test
        @DisplayName("passes CLI arguments to CafeCommandArguments")
        void passesArgumentsToCommand() {
            CafeShell shell = new CafeShell(TestShellConfig.class);
            shell.run(new String[]{"-greet", "hello", "world"});
            assertNotNull(GreetCommand.receivedArgs);
        }
    }

    // ---------------------------------------------------------------------------
    // Error handling
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("Error handling")
    class ErrorHandling {

        @Test
        @DisplayName("returns WRONG_ARGUMENT_ERROR for unknown option")
        void unknownOptionReturnsWrongArgError() {
            CafeShell shell = new CafeShell(TestShellConfig.class);
            int code = shell.run(new String[]{"--unknown-xyz"});
            assertEquals(2, code); // CafeShell.WRONG_ARGUMENT_ERROR
        }

        @Test
        @DisplayName("returns ERROR when command method throws")
        void throwingCommandReturnsError() {
            CafeShell shell = new CafeShell(TestShellConfig.class);
            int code = shell.run(new String[]{"--fail"});
            assertEquals(CafeShell.ERROR, code);
        }
    }

    // ---------------------------------------------------------------------------
    // No-arg / empty input
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("No args")
    class NoArgs {

        @Test
        @DisplayName("returns SUCCESS and prints help when no args given and no default command")
        void noArgsRunsHelp() {
            CafeShell shell = new CafeShell(TestShellConfig.class);
            int code = shell.run(new String[]{});
            assertEquals(CafeShell.SUCCESS, code);
        }

        @Test
        @DisplayName("null args returns SUCCESS")
        void nullArgsReturnsSuccess() {
            CafeShell shell = new CafeShell(TestShellConfig.class);
            int code = shell.run(null);
            assertEquals(CafeShell.SUCCESS, code);
        }
    }

    // ---------------------------------------------------------------------------
    // Help command
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("Help command")
    class HelpCommand {

        @Test
        @DisplayName("-h returns SUCCESS")
        void shortHelpFlagReturnsSuccess() {
            CafeShell shell = new CafeShell(TestShellConfig.class);
            int code = shell.run(new String[]{"-h"});
            assertEquals(CafeShell.SUCCESS, code);
        }

        @Test
        @DisplayName("--help returns SUCCESS")
        void longHelpFlagReturnsSuccess() {
            CafeShell shell = new CafeShell(TestShellConfig.class);
            int code = shell.run(new String[]{"--help"});
            assertEquals(CafeShell.SUCCESS, code);
        }
    }

    // ---------------------------------------------------------------------------
    // Dependency ordering
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("Dependency ordering")
    class DependencyOrdering {

        @Test
        @DisplayName("StepB runs before StepA even when --step-a appears first")
        void dependentCommandRunsAfterDependency() {
            CafeShell shell = new CafeShell(TestShellConfig.class);
            // Pass --step-a before --step-b; dependency ordering must invert this
            int code = shell.run(new String[]{"--step-a", "--step-b"});
            assertEquals(CafeShell.SUCCESS, code);
            assertTrue(StepBCommand.invokedAtStep < StepACommand.invokedAtStep,
                    "StepB (" + StepBCommand.invokedAtStep + ") must run before StepA (" + StepACommand.invokedAtStep + ")");
        }

        @Test
        @DisplayName("both steps are executed")
        void bothStepsExecuted() {
            CafeShell shell = new CafeShell(TestShellConfig.class);
            shell.run(new String[]{"--step-a", "--step-b"});
            assertNotEquals(-1, StepACommand.invokedAtStep, "StepA was not invoked");
            assertNotEquals(-1, StepBCommand.invokedAtStep, "StepB was not invoked");
        }
    }

    // ---------------------------------------------------------------------------
    // Multiple sequential runs on same shell instance
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("Multiple runs")
    class MultipleRuns {

        @Test
        @DisplayName("second run works independently of the first")
        void secondRunIsIndependent() {
            CafeShell shell = new CafeShell(TestShellConfig.class);
            int first = shell.run(new String[]{"--greet"});
            GreetCommand.reset();
            int second = shell.run(new String[]{"--greet"});
            assertEquals(CafeShell.SUCCESS, first);
            assertEquals(CafeShell.SUCCESS, second);
            assertTrue(GreetCommand.invoked);
        }
    }
}
