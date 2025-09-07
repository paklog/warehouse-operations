package com.paklog.warehouse.domain.shared;

import java.util.Arrays;
import java.util.List;

public final class Lists {
    private Lists() {} // Prevent instantiation

    @SafeVarargs
    public static <T> List<T> of(T... elements) {
        return Arrays.asList(elements);
    }
}