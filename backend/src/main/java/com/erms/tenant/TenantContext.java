package com.erms.tenant;

import java.util.UUID;

/** Request-scoped tenant identity. Always cleared by TenantContextFilter. */
public final class TenantContext {
    private static final ThreadLocal<UUID> ORGANIZATION_ID = new ThreadLocal<>();

    private TenantContext() {}

    public static void setOrganizationId(UUID organizationId) {
        ORGANIZATION_ID.set(organizationId);
    }

    public static UUID getRequiredOrganizationId() {
        UUID organizationId = ORGANIZATION_ID.get();
        if (organizationId == null) {
            throw new IllegalStateException("No authenticated organization is present in this request");
        }
        return organizationId;
    }

    public static void clear() {
        ORGANIZATION_ID.remove();
    }
}