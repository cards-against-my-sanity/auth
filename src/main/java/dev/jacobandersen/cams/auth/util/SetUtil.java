package dev.jacobandersen.cams.auth.util;

import java.util.Collection;
import java.util.Set;

public class SetUtil {
    public static <T> void setAll(Set<T> destination, Collection<T> collection) {
        destination.clear();
        destination.addAll(collection);
    }
}
