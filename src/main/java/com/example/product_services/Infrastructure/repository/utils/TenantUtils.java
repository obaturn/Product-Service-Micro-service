package com.example.product_services.Infrastructure.repository.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class TenantUtils {

    public String extractTenantId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }
        return jwt.getClaimAsString("tenant_id");
    }
}
