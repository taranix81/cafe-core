package org.taranix.cafe.beans.repositories.registry;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.repositories.HashMapRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Slf4j
abstract class AbstractDependencyRegistry<TValue> extends HashMapRepository<TValue, TValue> {


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
