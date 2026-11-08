package com.example.critmod;

import java.util.HashMap;
import java.util.Map;

public class MarkedClient {
    private static final Map<Integer, Long> MARKED = new HashMap<>();

    public static void set(int entityId, int duration, long now) {
        if (duration == 0) {
            MARKED.remove(entityId);
        } else {
            MARKED.put(entityId, duration < 0 ? Long.MAX_VALUE : now + duration);
        }
    }

    public static boolean isMarked(int entityId, long now) {
        Long end = MARKED.get(entityId);
        if (end == null) return false;
        if (now >= end) {
            MARKED.remove(entityId);
            return false;
        }
        return true;
    }

    public static void clear() {
        MARKED.clear();
    }
}