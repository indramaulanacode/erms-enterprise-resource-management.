package com.erms.api;

import com.erms.tenant.TenantContext;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tenant")
public class TenantController {
    @GetMapping("/context")
    @PreAuthorize("hasAnyRole('ORG_ADMIN', 'HR_MANAGER', 'EMPLOYEE')")
    public Map<String, String> context() {
        return Map.of("organizationId", TenantContext.getRequiredOrganizationId().toString());
    }
}