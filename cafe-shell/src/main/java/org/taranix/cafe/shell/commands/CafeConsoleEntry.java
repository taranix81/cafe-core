package org.taranix.cafe.shell.commands;

import java.util.List;

public record CafeConsoleEntry(Class<?> command, List<String> lines) {}
