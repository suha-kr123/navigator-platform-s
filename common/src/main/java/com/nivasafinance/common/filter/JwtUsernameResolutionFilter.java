package com.nivasafinance.common.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.nivasafinance.common.constants.AuthConstants;
import com.nivasafinance.common.resolver.UsernameResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component
@Order(1)
public class JwtUsernameResolutionFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtUsernameResolutionFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired(required = false)
    private UsernameResolver usernameResolver;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String usernameHeader = request.getHeader("X-Username");
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
        if (email == null && phone == null) {
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);
            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                String token = authHeader.substring(BEARER_PREFIX.length()).trim();
                if (!token.isEmpty()) {
                    try {
                        DecodedJWT decoded = JWT.decode(token);
                        if (email == null) {
                            email = getClaimAsString(decoded, "email");
                        }
                        if (phone == null) {
                            phone = getClaimAsString(decoded, "phone");
                        }
                        if ((email == null || email.isBlank()) || (phone == null || phone.isBlank())) {
                            Map<String, Object> userMetadata = getClaimAsMap(decoded, "user_metadata");
                            if (userMetadata != null) {
                                if ((email == null || email.isBlank()) && userMetadata.get("email") != null) {
                                    email = userMetadata.get("email").toString();
                                }
                                if ((phone == null || phone.isBlank()) && userMetadata.get("phone") != null) {
                                    phone = userMetadata.get("phone").toString();
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.debug("Could not resolve username from JWT: {}", e.getMessage());
                    }
                }
            }
        }
        Optional<String> username = usernameResolver.resolveByEmailOrPhone(trimToNull(email), trimToNull(phone));
        if (username.isPresent()) {
            request.setAttribute(AuthConstants.RESOLVED_USERNAME_ATTRIBUTE, username.get());
        }
        filterChain.doFilter(request, response);
    }

    private static String getClaimAsString(DecodedJWT decoded, String claimName) {
        if (decoded.getClaim(claimName).isNull()) {
            return null;
        }
        String value = decoded.getClaim(claimName).asString();
        return (value != null && !value.isBlank()) ? value : null;
    }

    private static Map<String, Object> getClaimAsMap(DecodedJWT decoded, String claimName) {
        if (decoded.getClaim(claimName).isNull()) {
            return null;
        }
        try {
            return decoded.getClaim(claimName).asMap();
        } catch (Exception e) {
            return null;
        }
    }

    private static String trimToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
