package org.taranix.cafe.shell.fixtures;

import java.util.concurrent.atomic.AtomicInteger;

public class InvocationOrder {

    private static final AtomicInteger counter = new AtomicInteger(0);

    public static int next() {
        return counter.getAndIncrement();
    }

    public static void reset() {
        counter.set(0);
    }
}
