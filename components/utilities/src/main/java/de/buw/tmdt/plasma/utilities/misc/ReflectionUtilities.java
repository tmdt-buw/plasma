package de.buw.tmdt.plasma.utilities.misc;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public final class ReflectionUtilities {
    private static final Logger logger = LoggerFactory.getLogger(ReflectionUtilities.class);

    private static final Map<Class, Class> arrayTypeCache = new HashMap<>();

    private ReflectionUtilities() {
    }

    public static <T> Class<T[]> getArrayTypeOf(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        Class<T[]> arrayClass = (Class<T[]>) arrayTypeCache.computeIfAbsent(
                clazz,
                basic -> Array.newInstance(clazz, 0).getClass()
        );
        return arrayClass;
    }

    public static Deque<@NotNull Class<?>> getClassHierarchy(Class<?> clazz) {
        Deque<Class<?>> hierarchy = new ArrayDeque<>();
        while (clazz != null) {
            hierarchy.push(clazz);
            clazz = clazz.getSuperclass();
        }
        return hierarchy;
    }

    public static <T> Set<T> getStaticValuesOfType(@NotNull Class<?> classToInspect, @NotNull Class<T> typeOfFields) {
        Set<T> values = new HashSet<>();

        for (Field field : classToInspect.getDeclaredFields()) {
            if (!field.getType().equals(typeOfFields)) {
                continue;
            }
            getValueOfStaticField(field, typeOfFields).ifPresent(values::add);
        }
        return values;
    }

    public static <T> Set<T> getAnnotatedStaticValuesOfType(
            @NotNull Class<?> classToInspect,
            @NotNull Class<T> typeOfFields,
            @NotNull Class<? extends Annotation> annotationType
    ) {
        Set<T> values = new HashSet<>();

        for (Field field : classToInspect.getDeclaredFields()) {
            if (!field.getType().equals(typeOfFields)) {
                continue;
            }
            if (field.getAnnotation(annotationType) == null) {
                continue;
            }
            getValueOfStaticField(field, typeOfFields).ifPresent(values::add);
        }
        return values;
    }

    private static <T> Optional<T> getValueOfStaticField(Field field, Class<T> typeOfFields) {
        final T value;

        if (!Modifier.isStatic(field.getModifiers())) {
            logger.warn("Omitting Action which was not static: {}.{}", field.getDeclaringClass(), field);
            return Optional.empty();
        }

        try {
            field.setAccessible(true);
        } catch (Exception e) {
            logger.warn("Failed to get access to {}: {}.{}", typeOfFields.getSimpleName(), field.getDeclaringClass(), field, e);
            return Optional.empty();
        }

        try {
            value = typeOfFields.cast(field.get(null));
        } catch (IllegalAccessException e) {
            logger.error("Failed to access field although `Field.trySetAccessible()` was successful: {}.{}", field.getDeclaringClass(), field, e);
            return Optional.empty();
        }

        return Optional.ofNullable(value);
    }
}
