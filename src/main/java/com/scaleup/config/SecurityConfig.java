package com.scaleup.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final InternalApiKeyFilter
            internalApiKeyFilter;

    public SecurityConfig(
            InternalApiKeyFilter internalApiKeyFilter
    ) {

        this.internalApiKeyFilter =
                internalApiKeyFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf ->
                        csrf.disable()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth ->
                        auth

                                .requestMatchers(
                                        "/api/public/**"
                                )
                                .permitAll()

                                .requestMatchers(
                                        "/api/integrations/highlevel/**"
                                )
                                .permitAll()

                                .requestMatchers(
                                        "/actuator/health"
                                )
                                .permitAll()

                                /*
                                 * InternalApiKeyFilter performs
                                 * authentication for these.
                                 */
                                .requestMatchers(
                                        "/api/internal/**"
                                )
                                .permitAll()

                                /*
                                 * Do not accidentally expose a
                                 * newly-created endpoint.
                                 */
                                .anyRequest()
                                .denyAll()
                )

                .addFilterBefore(
                        internalApiKeyFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}