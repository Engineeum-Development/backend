package genum.shared.config;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Component("customPageableKeyGenerator")
public class PageableKeyGenerator implements KeyGenerator {
    @Override
    public Object generate(Object target, Method method, Object... params) {
        return method.getName() + "_" + Arrays.stream(params)
                .map(Object::toString)
                .reduce((a,b) -> a + "_" + b)
                .orElse("");
    }
}
