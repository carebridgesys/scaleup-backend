package com.scaleup.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class InternalApiKeyFilter
        extends OncePerRequestFilter {

    private static final String HEADER_NAME =
            "X-ScaleUp-Internal-Key";

    private final String internalApiKey;

    public InternalApiKeyFilter(
            @Value("${security.internal-api-key}")
            String internalApiKey
    ) {

        if (
                internalApiKey == null
                        || internalApiKey.isBlank()
        ) {

            throw new IllegalStateException(
                    "Internal API key is not configured."
            );
        }

        this.internalApiKey =
                internalApiKey.trim();
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {

        return !request
                .getRequestURI()
                .startsWith(
                        "/api/internal/"
                );
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String providedKey =
                request.getHeader(
                        HEADER_NAME
                );

        if (
                !isValidKey(
                        providedKey
                )
        ) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.setContentType(
                    MediaType.APPLICATION_JSON_VALUE
            );

            response.getWriter().write(
                    """
                    {
                      "success": false,
                      "error": "UNAUTHORIZED"
                    }
                    """
            );

            return;
        }

        filterChain.doFilter(
                request,
                response
        );
    }

    private boolean isValidKey(
            String providedKey
    ) {

        if (
                providedKey == null
                        || providedKey.isBlank()
        ) {
            return false;
        }

        byte[] expected =
                internalApiKey.getBytes(
                        StandardCharsets.UTF_8
                );

        byte[] actual =
                providedKey
                        .trim()
                        .getBytes(
                                StandardCharsets.UTF_8
                        );

        return MessageDigest.isEqual(
                expected,
                actual
        );
    }
}