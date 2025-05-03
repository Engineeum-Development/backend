package genum.genumUser.security;

import genum.shared.util.CachedBodyHttpServletRequest;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class RequestBodyCacheFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest){
            CachedBodyHttpServletRequest httpServletRequest = new CachedBodyHttpServletRequest((HttpServletRequest) servletRequest);
            filterChain.doFilter(httpServletRequest,servletResponse);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }
}
