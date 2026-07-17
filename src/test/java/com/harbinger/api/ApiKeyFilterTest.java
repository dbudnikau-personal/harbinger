package com.harbinger.api;

import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiKeyFilterTest {

    private static final String VALID_KEY = "test-api-key";
    private static final String PROTECTED_PATH = "/api/v1/chat";

    private final ApiKeyFilter filter = new ApiKeyFilter(VALID_KEY);

    @Test
    void shouldPassChainWithValidKey() throws ServletException, IOException {
        MockHttpServletRequest request = protectedRequest();
        request.addHeader(ApiKeyFilter.API_KEY_HEADER, VALID_KEY);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNotNull(chain.getRequest());
        assertEquals(200, response.getStatus());
    }

    @Test
    void shouldRejectWrongKey() throws ServletException, IOException {
        MockHttpServletRequest request = protectedRequest();
        request.addHeader(ApiKeyFilter.API_KEY_HEADER, "wrong-key");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertUnauthorized(response, chain);
    }

    @Test
    void shouldRejectMissingKey() throws ServletException, IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(protectedRequest(), response, chain);

        assertUnauthorized(response, chain);
    }

    @Test
    void shouldBypassOpenPaths() throws ServletException, IOException {
        for (String path : new String[] {"/swagger-ui.html", "/swagger-ui/index.html",
                "/api-docs", "/v3/api-docs", "/actuator/health"}) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
            request.setRequestURI(path);
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();

            filter.doFilter(request, response, chain);

            assertNotNull(chain.getRequest(), "open path should bypass the filter: " + path);
            assertEquals(200, response.getStatus());
        }
    }

    @Test
    void shouldRejectWhenKeyUnconfigured() throws ServletException, IOException {
        ApiKeyFilter unconfigured = new ApiKeyFilter("");
        MockHttpServletRequest request = protectedRequest();
        request.addHeader(ApiKeyFilter.API_KEY_HEADER, "");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        unconfigured.doFilter(request, response, chain);

        assertUnauthorized(response, chain);
    }

    private static MockHttpServletRequest protectedRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", PROTECTED_PATH);
        request.setRequestURI(PROTECTED_PATH);
        return request;
    }

    private static void assertUnauthorized(MockHttpServletResponse response, MockFilterChain chain)
            throws IOException {
        assertNull(chain.getRequest(), "request must not reach the chain");
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("message"),
                "401 body follows the error shape");
    }
}
