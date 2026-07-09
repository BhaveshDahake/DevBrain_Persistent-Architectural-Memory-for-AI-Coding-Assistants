package com.example.DevBrain.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

import org.springframework.util.StringUtils;

@Component
public class SupabaseAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SupabaseAuthFilter.class);

    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.anon.key:}")
    private String supabaseAnonKey;

    @Value("${supabase.auth.enabled:false}")
    private boolean authEnabled;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Target protected endpoints when auth is explicitly enabled
        if (authEnabled && (path.startsWith("/api/datasets") || path.startsWith("/api/chat")
                || path.startsWith("/api/graph") || path.startsWith("/api/memory"))) {
            log.info("Intercepted protected API call: {}", path);
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Unauthorized access attempt on {} due to missing or invalid bearer token", path);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter()
                        .write("{\"success\":false,\"message\":\"Missing or invalid Authorization header\"}");
                return;
            }

            String token = authHeader.substring(7);

            try {
                String cleanUrl = StringUtils.trimWhitespace(supabaseUrl).replace("\r", "").replace("\n", "")
                        .replaceAll("^\"|\"$", "");
                String cleanKey = StringUtils.trimWhitespace(supabaseAnonKey).replace("\r", "").replace("\n", "")
                        .replaceAll("^\"|\"$", "");
                if (!StringUtils.hasText(cleanUrl) || !StringUtils.hasText(cleanKey)) {
                    log.warn("Supabase auth enabled but configuration is incomplete");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\":false,\"message\":\"Authentication validation failed\"}");
                    return;
                }

                String normalizedUrl = cleanUrl.endsWith("/") ? cleanUrl : cleanUrl + "/";
                String validationUrl = normalizedUrl + "auth/v1/user";

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + token);
                headers.set("apikey", cleanKey);

                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ResponseEntity<Map> userResponse = restTemplate.exchange(
                        validationUrl,
                        HttpMethod.GET,
                        entity,
                        Map.class);

                if (userResponse.getStatusCode() == HttpStatus.OK && userResponse.getBody() != null) {
                    String userId = (String) userResponse.getBody().get("id");
                    if (userId != null) {
                        request.setAttribute("userId", userId);
                        log.debug("Successfully validated token for user ID: {}", userId);
                    } else {
                        log.warn("Supabase auth succeeded but user ID was null");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter()
                                .write("{\"success\":false,\"message\":\"Authentication validation failed\"}");
                        return;
                    }
                } else {
                    log.warn("Supabase auth returned non-OK status: {}", userResponse.getStatusCode());
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\":false,\"message\":\"Authentication validation failed\"}");
                    return;
                }
            } catch (Exception e) {
                log.warn("Supabase authentication validation failed for request {}", path);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"Authentication validation failed\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
