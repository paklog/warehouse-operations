package com.paklog.warehouse.domain.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

public final class List {
    private List() {} // Prevent instantiation

    public static <T> java.util.List<T> of(T... elements) {
        java.util.List<T> list = new ArrayList<>();
        for (T element : elements) {
            list.add(element);
        }
        return list;
    }

    public static <T> java.util.List<T> copyOf(java.util.List<T> original) {
        return original == null ? 
            new ArrayList<>() : 
            new ArrayList<>(original);
    }

    public static <T> java.util.List<T> unmodifiableList(java.util.List<T> original) {
        return original == null ? 
            Collections.emptyList() : 
            Collections.unmodifiableList(new ArrayList<>(original));
    }
}