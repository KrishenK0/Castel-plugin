package fr.krishenk.castel.utils.internal;

import java.util.concurrent.Callable;
import java.util.function.*;

public class Fn {
    private static final Predicate<?> IDENTITY_PREDICATE = (x) -> true;
    public static <T> Supplier<T> supply(Supplier<T> supplier) {
        return supplier;
    }

    public static <T> Callable<T> call(Callable<T> callable) {
        return callable;
    }

    public static <T> Predicate<T> predicate(Predicate<T> predicate) {
        return predicate;
    }

    public static <T> Consumer<T> consume(Consumer<T> consumer) {
        return consumer;
    }

    public static Runnable run(Runnable runnable) {
        return runnable;
    }

    public static <T, R> Function<T, R> function(Function<T, R> function) {
        return function;
    }

    public static <T, U, R> BiFunction<T, U, R> function(BiFunction<T, U, R> function) {
        return function;
    }

    public static <T> T cast(T obj) {
        return obj;
    }

    public static <T> Predicate<T> alwaysTrue() {
        return (Predicate<T>) IDENTITY_PREDICATE;
    }
}
