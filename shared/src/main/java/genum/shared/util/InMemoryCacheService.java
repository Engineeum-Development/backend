package genum.shared.util;



import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class InMemoryCacheService implements CacheService<String,Object>{

    private final Map<String, Object> cacheStore = new ConcurrentHashMap<>();
    private static final int TTL = 20;
    public static final ChronoUnit temporalUnit = ChronoUnit.MINUTES;
    @Override
    public void put(String key, Object value) {
        var cache = new Cache(key, value, Instant.now().plus(TTL, temporalUnit));
        cacheStore.put(key, cache);
    }

    @Override
    public Object get(String key) {
        return ((Cache) cacheStore.get(key)).value();
    }

    @Override
    public boolean hasKey(String key) {
        return cacheStore.containsKey(key);
    }

    @Override
    public Long getRemainingLockTime(String key, TimeUnit timeUnit) {
        Cache cache = (Cache) get(key);
        var duration = Duration.of(
                cache.expiry().getEpochSecond() - Instant.now()
                        .getEpochSecond(),
                timeUnit.toChronoUnit()
                );

        return switch (timeUnit) {
            case DAYS -> duration.toDays();
            case HOURS -> duration.toHours();
            case MINUTES -> duration.toMinutes();
            case SECONDS -> duration.toSeconds();
            case NANOSECONDS -> duration.toNanos();
            case MILLISECONDS -> duration.toMillis();
            default -> duration.getSeconds();
        };
    }

    @Override
    public void evict(String key) {
        cacheStore.remove(key);
    }
}

record Cache (String key, Object value, Instant expiry){
}