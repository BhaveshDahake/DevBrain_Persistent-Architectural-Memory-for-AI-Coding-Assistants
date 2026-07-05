package com.example.DevBrain.config;

import com.example.DevBrain.exception.RateLimitExceededException;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitConfig rateLimitConfig;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Autowired
    public RateLimitFilter(RateLimitConfig rateLimitConfig, @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
        this.rateLimitConfig = rateLimitConfig;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String endpointType = null;

        if (path.startsWith("/api/chat/ask")) {
            endpointType = "chat";
        } else if (path.startsWith("/api/datasets/upload")) {
            endpointType = "upload";
        } else if (path.startsWith("/api/memory")) {
            endpointType = "memory";
        }

        if (endpointType != null) {
            // Using remote address as a fallback proxy for per-user isolation since no JWT/Auth is implemented yet
            String clientIp = request.getRemoteAddr();
            Bucket bucket = rateLimitConfig.resolveBucket(clientIp, endpointType);

            if (!bucket.tryConsume(1)) {
                // Delegate to GlobalExceptionHandler to preserve unified ApiErrorResponse payload
                handlerExceptionResolver.resolveException(request, response, null, 
                    new RateLimitExceededException("Rate limit exceeded for endpoint: " + endpointType));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
