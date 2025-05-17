package genum.shared.config.cache;

import genum.shared.util.CacheService;
import genum.shared.util.InMemoryCacheService;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class InMemoryCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @Bean
    CacheService<String, Object> cacheService() {
        return new InMemoryCacheService();
    }
}
