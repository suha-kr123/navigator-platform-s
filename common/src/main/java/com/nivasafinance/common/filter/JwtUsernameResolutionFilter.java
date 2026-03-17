package com.nivasafinance.common.filter;

import com.nivasafinance.common.constants.AuthConstants;
import com.nivasafinance.common.resolver.UsernameResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@Order(1)
public class JwtUsernameResolutionFilter extends OncePerRequestFilter {

    @Autowired(required = false)
    private UsernameResolver usernameResolver;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String usernameHeader = request.getHeader(AuthConstants.X_USERNAME_HEADER);
        if (usernameHeader != null && !usernameHeader.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }
        if (usernameResolver == null) {
            filterChain.doFilter(request, response);
            return;
        }
        String email = trimToNull(request.getHeader(AuthConstants.X_EMAIL_HEADER));
        String phone = trimToNull(request.getHeader(AuthConstants.X_PHONE_HEADER));
        Optional<String> username = usernameResolver.resolveByEmail(email)
                .or(() -> usernameResolver.resolveByPhone(phone));
        if (username.isPresent()) {
            request.setAttribute(AuthConstants.X_USERNAME_HEADER, username.get());
        }
        filterChain.doFilter(request, response);
    }

    private static String trimToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
