package org.obsidian.client.utils.other;

import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class Singleton<T> {
    private final Supplier<T> supplier;
    private final AtomicReference<T> instance = new AtomicReference<>();

    public static <T> Singleton<T> create(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "Supplier must not be null");
        return new Singleton<>(supplier);
    }

    public T get() {
        return instance.updateAndGet(current -> current != null ? current : supplier.get());
    }

    public void reset() {
        instance.set(null);
    }

    public boolean isInitialized() {
        return instance.get() != null;
    }

    public void ifInitialized(Consumer<T> consumer) {
        T result = instance.get();
        if (result != null) {
            consumer.accept(result);
        }
    }

    public T getOrThrow() {
        T result = instance.get();
        if (result == null) {
            throw new IllegalStateException("Instance is not initialized yet");
        }
        return result;
    }

    public T getOrElse(T defaultValue) {
        return instance.updateAndGet(current -> current != null ? current : defaultValue);
    }

    public T getOrSupply(Supplier<T> fallbackSupplier) {
        Objects.requireNonNull(fallbackSupplier, "Fallback supplier must not be null");
        return instance.updateAndGet(current -> current != null ? current : fallbackSupplier.get());
    }
}
