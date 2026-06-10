package org.taranix.cafe.shell.services;

import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.shell.commands.CafeConsoleEntry;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@CafeSingleton
public class CafeConsoleService {

    private final LinkedList<CafeConsoleEntry> queue = new LinkedList<>();

    public void add(Class<?> command, String line) {
        queue.addLast(new CafeConsoleEntry(command, List.of(line)));
    }

    public void add(Class<?> command, String... lines) {
        queue.addLast(new CafeConsoleEntry(command, List.of(lines)));
    }

    public void add(Class<?> command, List<String> lines) {
        queue.addLast(new CafeConsoleEntry(command, List.copyOf(lines)));
    }

    public List<CafeConsoleEntry> getEntries() {
        return Collections.unmodifiableList(queue);
    }

    public List<String> get(Class<?> command) {
        return queue.stream()
                .filter(e -> e.command().equals(command))
                .flatMap(e -> e.lines().stream())
                .toList();
    }

    public void remove(Class<?> command) {
        queue.removeIf(e -> e.command().equals(command));
    }

    public void removeAll() {
        queue.clear();
    }

    public void clear() {
        queue.clear();
    }

    public void flush() {
        queue.stream()
                .flatMap(e -> e.lines().stream())
                .forEach(System.out::println);
        clear();
    }
}
