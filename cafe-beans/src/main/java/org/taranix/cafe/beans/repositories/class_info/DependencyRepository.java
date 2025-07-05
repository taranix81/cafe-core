package org.taranix.cafe.beans.repositories.class_info;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.repositories.HashMapRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public abstract class DependencyRepository<TValue> extends HashMapRepository<TValue, TValue> {


    public void generateDiagram(String name) {
        StringBuilder sb = new StringBuilder();
        sb.append("@startuml\n");
        for (TValue item : getAllKeys()) {
            Collection<TValue> successors = getMany(item);
            for (TValue successor : successors) {
                sb.append("[").append(item.toString()).append("]-->[").append(successor.toString()).append("]\n");
            }
        }
        sb.append("@enduml\n");
        try {
            Files.write(Paths.get(name + ".puml"), Collections.singleton(sb.toString()), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public Collection<TValue> cycleSet() {

        for (TValue startingPoint : getAllKeys()) {
            Set<TValue> path = traverse(startingPoint, startingPoint);
            if (!path.isEmpty()) {
                return path;
            }
        }
        return Set.of();
    }

    private Set<TValue> traverse(final TValue startingPoint, final TValue current) {
        Set<TValue> result = new HashSet<>();
        log.trace("Traverse start:{}, current={}", startingPoint, current);
        Collection<TValue> successors = getMany(current);
        log.trace("Successors {}", successors);
        //Found cycle
        if (successors.contains(startingPoint)) {
            return Set.of(current);
        }

        //No cycle and end of path
        if (successors.isEmpty()) {
            return Set.of();
        }

        for (TValue successor : successors) {
            result.addAll(traverse(startingPoint, successor));
        }

        if (!result.isEmpty()) {
            result.add(current);
        }

        return result;
    }
}
