package io.github.hungariannotation.steam.cache;

import java.nio.file.Path;
import java.time.Instant;
import java.util.function.Function;

public interface CachedValueTraits<R, T> {

    Class<T> getType();

    Path getCachePath();

    String requestId(R request);

    String valueId(T value);

    Instant expiration(T value);

    boolean isExpired(T value);

    public default boolean isLive(T value) {
        return !isExpired(value);
    }

    /**
     * Get a staged builder for a CacheTraits holdings instances of type {@link T}
     * 
     * @param <T>
     * @param type
     * @return
     */
    public static <T> Builder$Initial<T> builderFor(Class<T> type) {
        return new Builder$Initial<T>(type);
    }

    public static final class Builder$Initial<T> {
        private final Class<T> type;

        Builder$Initial(Class<T> type) {
            this.type = type;
        }

        /**
         * Configure the required {@code value → expiration} mapping function.
         * 
         * @param expirationFunction
         * @return
         */
        public Builder$Final<T> expiration(Function<T, Instant> expirationFunction) {
            return new Builder$Final<>(type, expirationFunction);
        }
    }

    public static final class Builder$Final<T> {
        private final Class<T> type;
        private final Function<T, Instant> expirationFunction;

        Builder$Final(Class<T> type, Function<T, Instant> expirationFunction) {
            this.type = type;
            this.expirationFunction = expirationFunction;
        }

        /**
         * For a cache with string request values that can be used directly as cache
         * keys, define a mapping from values back to those keys.
         * 
         * @param expirationFunction
         * @return
         */
        public CachedValueTraits<String, T> keys(Function<T, String> valueToId) {
            return new DuallyMappedCacheTraits<>(type, k -> k, valueToId, expirationFunction);
        }

        /**
         * Configure a non-identity {@code request → key} mapping, as well as the
         * required {@code value → key} mapping.
         * 
         * @param expirationFunction
         * @return
         */
        public <K> CachedValueTraits<K, T> keys(Function<K, String> keyToId, Function<T, String> valueToId) {
            return new DuallyMappedCacheTraits<>(type, keyToId, valueToId, expirationFunction);
        }
    }
}