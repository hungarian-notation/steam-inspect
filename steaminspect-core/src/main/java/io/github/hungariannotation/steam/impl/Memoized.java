package io.github.hungariannotation.steam.impl;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * <!-- we have stable values at home -->
 * 
 * <a href="https://en.wikipedia.org/wiki/Memoization">Memoizing</a> Supplier
 * <p>
 * Allows for thread-safe deferred initialization of expensive values.
 * <p>
 * 
 * <strong>Note:</strong> this class is pre-deprecated pending the finalization
 * of the StableValue feature. See: <a href="https://openjdk.org/jeps/502">JEP
 * 502</a> and <a href="https://openjdk.org/jeps/526">JEP 526</a>.
 * 
 * @param <T>
 */
public class Memoized<T> implements Supplier<T> {
    private final Object lock = new Object();

    private volatile Supplier<T> delegate;
    private T value;

    /**
     * Creates a memoizing supplier that will invoke it's wrapped supplier exactly
     * once.
     * 
     * @param source
     */
    public Memoized(Supplier<T> source) {
        this.delegate = Objects.requireNonNull(source);
    }

    /**
     * Get the memoized value, creating it if necessary.
     */
    public T get() {
        if (delegate != null) {
            synchronized (lock) {
                if (delegate != null) {
                    T memoizedValue = Objects.requireNonNull(delegate.get());
                    this.value = memoizedValue;
                    delegate = null;
                    return memoizedValue;
                }
            }
        }

        T result = value;
        return result;
    }

    /**
     * Wraps the provided supplier. Equivalent to: {@code new Memoized(supplier)}.
     * 
     * @param <T>
     * @param supplier
     * @return
     */
    public static <T> Memoized<T> of(Supplier<T> supplier) {
        return new Memoized<>(supplier);
    }
}
