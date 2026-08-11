package com.threlease.base.common.configs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiAccessRulesTest {

    @Test
    void allowsPublicApiAndNonApiGetRequests() {
        assertTrue(ApiAccessRules.isPublicRequest("GET", "/dashboard"));
        assertTrue(ApiAccessRules.isPublicRequest("HEAD", "/dashboard"));
        assertTrue(ApiAccessRules.isPublicRequest("GET", "/api/v1/auth/login"));
        assertTrue(ApiAccessRules.isPublicRequest("GET", "/api/v1/files/content/sample"));
        assertTrue(ApiAccessRules.isPublicRequest("GET", "/api/swagger"));
        assertTrue(ApiAccessRules.isPublicRequest("GET", "/api/v3/api-docs"));
        assertTrue(ApiAccessRules.isPublicRequest("GET", "/api/actuator/prometheus"));
    }

    @Test
    void blocksPrivateApiRequests() {
        assertFalse(ApiAccessRules.isPublicRequest("POST", "/api/v1/auth/me"));
        assertFalse(ApiAccessRules.isPublicRequest("GET", "/api/v1/auth/me"));
    }
}
