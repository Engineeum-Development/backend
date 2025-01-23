package genum.genumUser.security.jwt;

import genum.genumUser.security.constant.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class LogoutHandlingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().contains("/api/auth/logout")) {
            response.setHeader("Authorization", null);
            filterChain.doFilter(request,response);
        }
        else {
            filterChain.doFilter(request,response);
        }
    }
}
