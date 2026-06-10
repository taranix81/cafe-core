package org.taranix.cafe.shell.services;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.shell.annotations.CafeCommand;
import org.taranix.cafe.shell.commands.CafeCommandRuntime;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CafeSingleton
@Slf4j
public class CafeCommandRuntimeOrderService {

    public List<CafeCommandRuntime> order(Collection<CafeCommandRuntime> commandRuntimes) {
        Map<Class<?>, CafeCommandRuntime> byClass = new HashMap<>();
        for (CafeCommandRuntime runtime : commandRuntimes) {
            byClass.put(runtime.getCommandInstance().getClass(), runtime);
        }

        Map<Class<?>, Set<Class<?>>> deps = new HashMap<>();
        for (Class<?> cls : byClass.keySet()) {
            CafeCommand ann = cls.getAnnotation(CafeCommand.class);
            Set<Class<?>> depSet = new HashSet<>();
            if (ann != null) {
                for (Class<?> dep : ann.dependsOn()) {
                    if (byClass.containsKey(dep)) {
                        depSet.add(dep);
                    }
                }
            }
            deps.put(cls, depSet);
        }

        List<Class<?>> sorted = topologicalSort(byClass.keySet(), deps);
        List<CafeCommandRuntime> result = new ArrayList<>();
        for (Class<?> cls : sorted) {
            result.add(byClass.get(cls));
        }
        return result;
    }

    private List<Class<?>> topologicalSort(Set<Class<?>> nodes, Map<Class<?>, Set<Class<?>>> deps) {
        // inDegree = number of dependsOn entries per node (nodes this one waits for)
        Map<Class<?>, Integer> inDegree = new HashMap<>();
        for (Class<?> node : nodes) {
            inDegree.put(node, deps.get(node).size());
        }

        Deque<Class<?>> queue = new ArrayDeque<>();
        for (Map.Entry<Class<?>, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<Class<?>> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            Class<?> current = queue.poll();
            result.add(current);
            for (Class<?> dependent : nodes) {
                if (deps.get(dependent).contains(current)) {
                    int newDeg = inDegree.get(dependent) - 1;
                    inDegree.put(dependent, newDeg);
                    if (newDeg == 0) {
                        queue.add(dependent);
                    }
                }
            }
        }
        return result;
    }
}
