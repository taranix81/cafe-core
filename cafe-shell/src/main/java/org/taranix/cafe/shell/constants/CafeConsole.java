package org.taranix.cafe.shell.constants;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CafeConsole {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK_TEXT = "\u001B[30m";
    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_TEXT = "\u001B[31m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_TEXT = "\u001B[32m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_TEXT = "\u001B[33m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_TEXT = "\u001B[34m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_TEXT = "\u001B[35m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_TEXT = "\u001B[36m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_TEXT = "\u001B[37m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";

    public static String ansiColouredText(String text, CafeConsoleTextColour textColour, CafeConsoleBackgroundColour backgroundColour) {
        return textColour.getAnsiColour() + backgroundColour.getAnsiColour() + text + ANSI_RESET;
    }

    public enum CafeConsoleBackgroundColour {
        NONE(""),
        BLACK(ANSI_BLACK_BACKGROUND),
        RED(ANSI_RED_BACKGROUND),
        GREEN(ANSI_GREEN_BACKGROUND),
        YELLOW(ANSI_YELLOW_BACKGROUND),
        BLUE(ANSI_BLUE_BACKGROUND),
        PURPLE(ANSI_PURPLE_BACKGROUND),
        CYAN(ANSI_CYAN_BACKGROUND),
        WHITE(ANSI_WHITE_BACKGROUND);

        @Getter
        private final String ansiColour;

        CafeConsoleBackgroundColour(String ansiColour) {
            this.ansiColour = ansiColour;
        }
    }

    public enum CafeConsoleTextColour {
        NONE(""),
        BLACK(ANSI_BLACK_TEXT),
        RED(ANSI_RED_TEXT),
        GREEN(ANSI_GREEN_TEXT),
        YELLOW(ANSI_YELLOW_TEXT),
        BLUE(ANSI_BLUE_TEXT),
        PURPLE(ANSI_PURPLE_TEXT),
        CYAN(ANSI_CYAN_TEXT),
        WHITE(ANSI_WHITE_TEXT);

        @Getter
        private final String ansiColour;

        CafeConsoleTextColour(String ansiColour) {
            this.ansiColour = ansiColour;
        }
    }
}
