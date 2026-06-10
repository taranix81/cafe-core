package org.taranix.cafe.shell.resolvers;

import org.apache.commons.cli.Option;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.shell.annotations.CafeCommand;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

class CafeCommandClassResolverTest {

    private final CafeCommandClassResolver resolver = new CafeCommandClassResolver();

    // ---------------------------------------------------------------------------
    // supports()
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("supports()")
    class Supports {

        @Test
        @DisplayName("returns true for @CafeCommand")
        void supportsCommand() {
            assertTrue(resolver.supports(CafeCommand.class));
        }

        @Test
        @DisplayName("returns false for unrelated annotation")
        void doesNotSupportOtherAnnotation() {
            assertFalse(resolver.supports(Deprecated.class));
        }

        @Test
        @DisplayName("returns false for null")
        void doesNotSupportNull() {
            assertFalse(resolver.supports((Class<? extends Annotation>) null));
        }
    }

    // ---------------------------------------------------------------------------
    // buildOption()
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("buildOption()")
    class BuildOption {

        @Test
        @DisplayName("returns Option with correct short and long name")
        void buildsOptionWithNames() {
            Option option = CafeCommandClassResolver.buildOption(command("x", "execute", "Run it"));
            assertNotNull(option);
            assertEquals("x", option.getOpt());
            assertEquals("execute", option.getLongOpt());
        }

        @Test
        @DisplayName("sets description correctly")
        void buildsOptionWithDescription() {
            Option option = CafeCommandClassResolver.buildOption(command("p", "process", "Process files"));
            assertNotNull(option);
            assertEquals("Process files", option.getDescription());
        }

        @Test
        @DisplayName("sets noOfArgs correctly")
        void buildsOptionWithArgs() {
            Option option = CafeCommandClassResolver.buildOption(commandWithArgs("f", "file", "Load file", 2, "path"));
            assertNotNull(option);
            assertEquals(2, option.getArgs());
            assertEquals("path", option.getArgName());
        }

        @Test
        @DisplayName("sets required flag correctly")
        void buildsRequiredOption() {
            Option option = CafeCommandClassResolver.buildOption(commandRequired("r", "run", "Run required"));
            assertNotNull(option);
            assertTrue(option.isRequired());
        }

        @Test
        @DisplayName("sets hasOptionalArgument correctly")
        void buildsOptionWithOptionalArg() {
            Option option = CafeCommandClassResolver.buildOption(commandOptionalArg("o", "out", "Output"));
            assertNotNull(option);
            assertTrue(option.hasOptionalArg());
        }

        @Test
        @DisplayName("returns null when command name is blank")
        void returnsNullForBlankCommand() {
            Option option = CafeCommandClassResolver.buildOption(command("", "long", "Description"));
            assertNull(option);
        }

        @Test
        @DisplayName("returns null when description is blank")
        void returnsNullForBlankDescription() {
            Option option = CafeCommandClassResolver.buildOption(command("c", "cmd", ""));
            assertNull(option);
        }

        @Test
        @DisplayName("returns null for default (no-option) command")
        void returnsNullForDefaultCommand() {
            Option option = CafeCommandClassResolver.buildOption(command("", "", ""));
            assertNull(option);
        }
    }

    // ---------------------------------------------------------------------------
    // isOptedCommand()
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("isOptedCommand()")
    class IsOptedCommand {

        @Test
        @DisplayName("returns true when command and description are both non-blank")
        void trueWhenBothPresent() {
            assertTrue(CafeCommandClassResolver.isOptedCommand(command("x", "exec", "Executes")));
        }

        @Test
        @DisplayName("returns false when command is blank")
        void falseWhenCommandBlank() {
            assertFalse(CafeCommandClassResolver.isOptedCommand(command("", "exec", "Executes")));
        }

        @Test
        @DisplayName("returns false when description is blank")
        void falseWhenDescriptionBlank() {
            assertFalse(CafeCommandClassResolver.isOptedCommand(command("x", "exec", "")));
        }
    }

    // ---------------------------------------------------------------------------
    // Helpers — create CafeCommand annotation instances
    // ---------------------------------------------------------------------------

    private static CafeCommand command(String cmd, String longCmd, String desc) {
        return new CafeCommand() {
            public Class<? extends Annotation> annotationType() { return CafeCommand.class; }
            public String command()             { return cmd; }
            public String longCommand()         { return longCmd; }
            public String description()         { return desc; }
            public int noOfArgs()               { return 0; }
            public String argumentName()        { return ""; }
            public boolean required()           { return false; }
            public char valueSeparator()        { return ','; }
            public boolean hasOptionalArgument(){ return false; }
            public Class<?>[] dependsOn()       { return new Class[0]; }
        };
    }

    private static CafeCommand commandWithArgs(String cmd, String longCmd, String desc, int args, String argName) {
        return new CafeCommand() {
            public Class<? extends Annotation> annotationType() { return CafeCommand.class; }
            public String command()             { return cmd; }
            public String longCommand()         { return longCmd; }
            public String description()         { return desc; }
            public int noOfArgs()               { return args; }
            public String argumentName()        { return argName; }
            public boolean required()           { return false; }
            public char valueSeparator()        { return ','; }
            public boolean hasOptionalArgument(){ return false; }
            public Class<?>[] dependsOn()       { return new Class[0]; }
        };
    }

    private static CafeCommand commandRequired(String cmd, String longCmd, String desc) {
        return new CafeCommand() {
            public Class<? extends Annotation> annotationType() { return CafeCommand.class; }
            public String command()             { return cmd; }
            public String longCommand()         { return longCmd; }
            public String description()         { return desc; }
            public int noOfArgs()               { return 0; }
            public String argumentName()        { return ""; }
            public boolean required()           { return true; }
            public char valueSeparator()        { return ','; }
            public boolean hasOptionalArgument(){ return false; }
            public Class<?>[] dependsOn()       { return new Class[0]; }
        };
    }

    private static CafeCommand commandOptionalArg(String cmd, String longCmd, String desc) {
        return new CafeCommand() {
            public Class<? extends Annotation> annotationType() { return CafeCommand.class; }
            public String command()             { return cmd; }
            public String longCommand()         { return longCmd; }
            public String description()         { return desc; }
            public int noOfArgs()               { return 1; }
            public String argumentName()        { return ""; }
            public boolean required()           { return false; }
            public char valueSeparator()        { return ','; }
            public boolean hasOptionalArgument(){ return true; }
            public Class<?>[] dependsOn()       { return new Class[0]; }
        };
    }
}
