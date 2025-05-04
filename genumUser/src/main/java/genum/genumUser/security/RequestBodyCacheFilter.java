package genum.genumUser.security;

import genum.shared.util.CachedBodyHttpServletRequest;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.regex.Pattern;

@Component
public class RequestBodyCacheFilter implements Filter {

    public static final Pattern PAYMENT_PATH = Pattern.compile("/api/payment/(paystack|flutterwave)/webhook");
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest httpServletRequest && servletResponse instanceof HttpServletResponse httpServletResponse){
            if (PAYMENT_PATH.matcher(httpServletRequest.getRequestURI()).matches()){
                CachedBodyHttpServletRequest cachedBodyHttpServletRequest = new CachedBodyHttpServletRequest(httpServletRequest);
                filterChain.doFilter(cachedBodyHttpServletRequest,servletResponse);
            } else {
                filterChain.doFilter(httpServletRequest, httpServletResponse);
            }
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }
}
