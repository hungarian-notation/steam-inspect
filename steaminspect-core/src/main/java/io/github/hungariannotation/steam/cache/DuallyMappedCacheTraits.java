package io.github.hungariannotation.steam.cache;

import java.nio.file.Path;
import java.time.Instant;
import java.util.function.Function;

public class DuallyMappedCacheTraits<R, V> implements CachedValueTraits<R, V> {

    final Class<V> type;
    private final Function<R, String> requestToId;
    private final Function<V, String> valueToId;
    private final Function<V, Instant> valueToExpiration;
    private final String uid;
    private final Path cachefile;

    public DuallyMappedCacheTraits(Class<V> type,
            Function<R, String> requestToId,
            Function<V, String> valueToId,
            Function<V, Instant> valueToExpiration) {
        this.uid = type.getSimpleName();
        this.cachefile = SimpleCache.getCacheRoot().resolve(uid + ".cache");
        this.type = type;
        this.requestToId = requestToId;
        this.valueToId = valueToId;
        this.valueToExpiration = valueToExpiration;
    }

    @Override
    public String requestId(R request) {
        return requestToId.apply(request);
    }

    @Override
    public String valueId(V value) {
        return valueToId.apply(value);
    }

    @Override
    public Instant expiration(V value) {
        return valueToExpiration.apply(value);
    }

    @Override
    public boolean isExpired(V value) {
        return expiration(value).isBefore(Instant.now());
    }

    @Override
    public Path getCachePath() {
        return cachefile;
    }

    public Class<V> getType() {
        return type;
    }

    public Function<R, String> getRequestToId() {
        return requestToId;
    }

    public Function<V, String> getValueToId() {
        return valueToId;
    }

    public Function<V, Instant> getValueToExpiration() {
        return valueToExpiration;
    }

    public String getUid() {
        return uid;
    }

    public Path getCachefile() {
        return cachefile;
    }

}