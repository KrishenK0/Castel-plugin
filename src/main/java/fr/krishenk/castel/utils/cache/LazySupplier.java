package fr.krishenk.castel.utils.cache;

import java.util.Objects;
import java.util.function.Supplier;

public class LazySupplier<T> implements Supplier<T> {
    private final Supplier<T> supplier;
    private T value;

    public LazySupplier(Supplier<T> supplier) {
        this.supplier = Objects.requireNonNull(supplier);
    }

    public static <T> LazySupplier<T> of(Supplier<T> supplier) {
        return new LazySupplier<>(supplier);
    }

    @Override
    public T get() {
        return this.value == null ? (this.value = this.supplier.get()) : this.value;
    }
}
