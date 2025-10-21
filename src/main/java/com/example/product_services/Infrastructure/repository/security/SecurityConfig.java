package com.example.product_services.Infrastructure.repository.security;

import org.jboss.logging.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import java.util.*;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger log = Logger.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((req, res, ex1) -> {
                            res.setStatus(HttpStatus.FORBIDDEN.value());
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"Access Denied - You don’t have permission to access this resource\"}");
                        })
                )

                .authorizeHttpRequests(auth -> auth
                        // Vendor endpoints
                        .requestMatchers("/products/vendor/**").hasRole("VENDOR")

                        // Customer endpoints
                        .requestMatchers("/products/customer/**").hasRole("CUSTOMERS")

                        // Admin endpoints
                        .requestMatchers("/products/admin/**").hasRole("ADMIN")

                        // Debug or general endpoints
                        .requestMatchers("/auth/me").authenticated()

                        // All others are public
                        .anyRequest().permitAll()
                )

                // Enable JWT-based authentication via Keycloak
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint((req, res, ex) -> {
                            log.error("Authentication failed: " + ex.getMessage(), ex);
                            res.setStatus(HttpStatus.UNAUTHORIZED.value());
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"Unauthorized - Invalid or expired token\"}");
                        })
                );

        return http.build();
    }

    /**
     * Custom converter to extract roles dynamically from Keycloak JWT
     */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Set<GrantedAuthority> authorities = new HashSet<>();

            // 🔍 Log all claims for debugging
            log.info("JWT Claims received: " + jwt.getClaims());

            // 1️⃣ Realm roles
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                ((List<String>) realmAccess.get("roles")).forEach(role -> {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase(Locale.ROOT)));
                });
            }


            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null) {
                // Log the keys to verify
                log.info("Keys found inside resource_access: " + resourceAccess.keySet());


                String clientId = jwt.getClaimAsString("azp");
                log.info("Using clientId from token (azp): " + clientId);

                if (clientId != null && resourceAccess.containsKey(clientId)) {
                    Map<String, Object> client = (Map<String, Object>) resourceAccess.get(clientId);
                    List<String> clientRoles = (List<String>) client.get("roles");
                    if (clientRoles != null) {
                        clientRoles.forEach(role -> {
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase(Locale.ROOT)));
                        });
                    }
                } else {
                    log.warn("⚠️ No client roles found for clientId: " + clientId);
                }
            }


            log.info("Extracted Authorities from JWT: " + authorities);
            return authorities;
        });

        return converter;
    }
}
