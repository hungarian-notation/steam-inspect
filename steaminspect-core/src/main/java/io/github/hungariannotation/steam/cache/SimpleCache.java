package io.github.hungariannotation.steam.cache;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class SimpleCache<R, V> {

    static final Logger LOGGER = Logger.getLogger(SimpleCache.class.getName());

    public record Collated<R, V>(Map<R, V> cached, List<R> missed) {

    }

    private static final Optional<Path> CACHE_ROOT;

    static {
        CACHE_ROOT = Optional
                .ofNullable(System.getProperty("user.home"))
                .map(Path::of)
                .map(x -> x.resolve(".cache", SimpleCache.class.getPackageName()))
                .flatMap(dir -> {
                    try {
                        Files.createDirectories(dir);
                        return Optional.of(dir);
                    } catch (IOException e) {
                        System.err.print("CRITICAL: could not create cache directory");
                        e.printStackTrace();
                        return Optional.empty();
                    }
                });
    }

    static final Path getCacheRoot() {
        return CACHE_ROOT.orElseThrow(() -> new RuntimeException("cache unavailable"));
    }

    private final CachedValueTraits<R, V> traits;

    private final Object LOCK = new Object();

    private final Object LOCK_MEMCACHE = new Object();

    private volatile ArrayList<V> memcache;

    public SimpleCache(CachedValueTraits<R, V> traits) {
        this.traits = traits;
        if (!traits.getType().isRecord())
            throw new IllegalArgumentException("implementation only supports record types");
    }

    private boolean doEviction(V item) {
        if (traits.isExpired(item)) {
            LOGGER.info(() -> String.format("evicting %s", traits.valueId(item)));
            return false;
        } else {
            return true;
        }
    }

    /**
     * Compare a list of request objects against the cache, returning a
     * {@link Collated} instance.
     * 
     * @param requestList
     * @return
     */
    public Collated<R, V> queryCache(List<R> requestList) {
        HashMap<String, R> index = new HashMap<>();

        for (R request : requestList) {
            index.put(traits.requestId(request), request);
        }

        HashMap<R, V> collated = new HashMap<>();

        for (V cached : readIt()) {
            String key = traits.valueId(cached);

            if (index.containsKey(key)) {
                collated.put(index.get(key), cached);
                index.remove(key);
            }
        }

        List<R> uncollated = index.values().stream().toList();

        for (var v : uncollated) {
            LOGGER.info(() -> String.format("cache miss: %s was not present%n", traits.requestId(v)));
        }

        return new Collated<>(collated, uncollated);
    }

    private int compareLife(V a, V b) {
        if (traits.expiration(a).isBefore(traits.expiration(b))) {
            return -1;
        } else if (traits.expiration(a).equals(traits.expiration(b))) {
            return 0;
        } else {
            return 1;
        }
    }

    public void updateCache(Collection<V> values) {
        Map<String, V> newValues = new HashMap<>();

        for (var value : values) {
            if (traits.isLive(value))
                newValues.put(traits.valueId(value), value);
        }

        synchronized (LOCK) {
            for (var oldValue : readIt()) {
                String key = traits.valueId(oldValue);
                if (newValues.containsKey(key) && compareLife(oldValue, newValues.get(key)) < 0)
                    continue;
                newValues.put(key, oldValue);
            }

            write(newValues.values().stream());
        }
    }

    private Stream<V> read() {
        if (memcache == null) {
            synchronized (LOCK_MEMCACHE) {
                if (memcache == null)
                    memcache = actuallyRead();
            }
        }

        return memcache.stream().filter(this::doEviction);
    }

    private Iterable<V> readIt() {
        var stream = read();
        return stream::iterator;
    }

    private ArrayList<V> actuallyRead() {
        ArrayList<V> accumulatorList = null;
        synchronized (LOCK) {
            try (var stream = new ObjectInputStream(Files.newInputStream(traits.getCachePath()))) {
                int count = stream.readInt();
                accumulatorList = new ArrayList<V>(count);

                for (int i = 0; i < count; ++i) {
                    try {
                        var deserialized = traits.getType().cast(stream.readObject());
                        if (traits.isLive(deserialized))
                            accumulatorList.add(deserialized);
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return accumulatorList != null ? accumulatorList : new ArrayList<>();
        }
    }

    private void write(Stream<V> values) {
        synchronized (LOCK) {
            List<V> loaded = values
                    .filter(this::doEviction)
                    .toList();
            try (var stream = new ObjectOutputStream(Files.newOutputStream(traits.getCachePath(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING))) {
                stream.writeInt(loaded.size());
                loaded.forEach(object -> {
                    assert traits.getType().isInstance(object)
                            : "bad instance type: " + object.getClass().getSimpleName();
                    try {
                        stream.writeObject(object);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
