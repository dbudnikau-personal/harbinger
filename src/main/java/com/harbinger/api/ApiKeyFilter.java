package com.harbinger.api;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Static API-key authentication for {@code /api/**}.
 *
 * <p>Secure by default: when {@code HARBINGER_API_KEY} is unset, every protected
 * request is rejected with 401 (never an open fallback). Swagger UI, the OpenAPI
 * spec and the health endpoint stay open.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class ApiKeyFilter extends OncePerRequestFilter {

    static final String API_KEY_HEADER = "X-API-Key";

    private static final Logger LOG = LoggerFactory.getLogger(ApiKeyFilter.class);
    private static final String PROTECTED_PREFIX = "/api/";

    private final byte[] apiKey;
    private final boolean configured;

    ApiKeyFilter(@Value("${harbinger.security.api-key:}") String apiKey) {
        this.apiKey = apiKey.getBytes(StandardCharsets.UTF_8);
        this.configured = !apiKey.isBlank();
        if (!configured) {
            LOG.warn("HARBINGER_API_KEY is not set — all /api/** requests will be rejected with 401");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(PROTECTED_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String provided = request.getHeader(API_KEY_HEADER);
        if (configured && provided != null
                && MessageDigest.isEqual(apiKey, provided.getBytes(StandardCharsets.UTF_8))) {
            filterChain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"Invalid or missing API key\"}");
    }
}
