package com.erms.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TenantContextTest {
    @AfterEach
    void cleanUp() {
        TenantContext.clear();
    }

    @Test
    void returnsOrganizationFromCurrentRequestContext() {
        UUID organizationId = UUID.randomUUID();
        TenantContext.setOrganizationId(organizationId);
        assertEquals(organizationId, TenantContext.getRequiredOrganizationId());
    }

    @Test
    void failsClosedWhenNoTenantIsEstablished() {
        assertThrows(IllegalStateException.class, TenantContext::getRequiredOrganizationId);
    }
}