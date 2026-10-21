package de.fhms.mu.pse.utils;

import java.util.AbstractMap;
import java.util.Map;

public class TypeUtils {
    public static Map<Class<?>, Class<?>> PRIMITIVE_TO_TYPE = Map.ofEntries(
            new AbstractMap.SimpleEntry<>(Boolean.TYPE, Boolean.class),
            new AbstractMap.SimpleEntry<>(Byte.TYPE, Byte.class),
            new AbstractMap.SimpleEntry<>(Short.TYPE, Short.class),
            new AbstractMap.SimpleEntry<>(Integer.TYPE, Integer.class),
            new AbstractMap.SimpleEntry<>(Long.TYPE, Long.class),
            new AbstractMap.SimpleEntry<>(Float.TYPE, Float.class),
            new AbstractMap.SimpleEntry<>(Double.TYPE, Double.class)
    );

    public static <T> Class<T> normalizeType(Class<?> type) {
        if (!type.isPrimitive() || !PRIMITIVE_TO_TYPE.containsKey(type)) {
            return (Class<T>) type;
        }
        return (Class<T>) PRIMITIVE_TO_TYPE.get(type);
    }

    public static boolean isBooleanType(Class<?> type) {
        return normalizeType(type).equals(Boolean.class);
    }

    public static boolean isIntegerType(Class<?> type) {
        return normalizeType(type).equals(Byte.class)
                || normalizeType(type).equals(Short.class)
                || normalizeType(type).equals(Integer.class)
                || normalizeType(type).equals(Long.class);
    }

    public static boolean isDecimalType(Class<?> type) {
        return normalizeType(type).equals(Float.class)
                || normalizeType(type).equals(Double.class);
    }

    public static boolean isStringType(Class<?> type) {
        return type.equals(String.class);
    }

    public static boolean isPrimitiveType(Class<?> type) {
        return isBooleanType(type) || isIntegerType(type) || isDecimalType(type) || isStringType(type);
    }
}
