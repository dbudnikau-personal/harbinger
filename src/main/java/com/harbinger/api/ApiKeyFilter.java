package com.harbinger.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UncheckedIOException;
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
    private final String unauthorizedBody;

    ApiKeyFilter(@Value("${harbinger.security.api-key:}") String apiKey, ObjectMapper objectMapper) {
        this.apiKey = apiKey.getBytes(StandardCharsets.UTF_8);
        this.configured = !apiKey.isBlank();
        this.unauthorizedBody = serialize(objectMapper,
                new ApiExceptionHandler.ErrorResponse("Invalid or missing API key"));
        if (!configured) {
            LOG.warn("HARBINGER_API_KEY is not set — all /api/** requests will be rejected with 401");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // servletPath + pathInfo is container-decoded, normalized and context-path-relative —
        // matching on the raw getRequestURI() would let an encoded path (/%61pi/...) or a
        // non-root context-path bypass auth. Assumes the default DispatcherServlet "/" mapping.
        String pathInfo = request.getPathInfo();
        String path = request.getServletPath() + (pathInfo != null ? pathInfo : "");
        return !path.startsWith(PROTECTED_PREFIX);
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
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(unauthorizedBody);
    }

    private static String serialize(ObjectMapper objectMapper, ApiExceptionHandler.ErrorResponse body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
