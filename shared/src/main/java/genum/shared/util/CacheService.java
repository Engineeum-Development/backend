package genum.shared.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

public interface CacheService<K,V> {

    void put(K key, V value);
    V get(K key);

    Long getRemainingLockTime(K key, TimeUnit timeUnit);
    void evict(K key);

    boolean hasKey(K key);
}
