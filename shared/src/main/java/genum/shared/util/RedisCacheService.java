package genum.shared.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Profile({"deployment", "dev"})
public class RedisCacheService implements CacheService<String, Object> {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void put(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
        redisTemplate.expire(key,20, TimeUnit.MINUTES);
    }

    @Override
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public Long getRemainingLockTime(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(key, timeUnit);
    }

    @Override
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void evict(String key) {
        redisTemplate.opsForValue().getAndDelete(key);
    }
}
