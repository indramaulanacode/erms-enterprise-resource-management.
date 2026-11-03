package com.erms.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.erms.employee.EmployeeDtos.CreateEmployeeRequest;
import com.erms.tenant.TenantContext;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers(disabledWithoutDocker = true)
class EmployeeTenantIsolationIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("security.jwt.secret", () -> java.util.Base64.getEncoder().encodeToString(new byte[48]));
        registry.add("security.jwt.issuer", () -> "erms-tenant-test");
    }

    @Autowired EmployeeService employeeService;
    @Autowired JdbcTemplate jdbc;

    private UUID organizationA;
    private UUID organizationB;

    @BeforeEach
    void setUp() {
        organizationA = createOrganization("tenant-a-" + UUID.randomUUID().toString().substring(0, 8));
        organizationB = createOrganization("tenant-b-" + UUID.randomUUID().toString().substring(0, 8));
    }

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    @Test
    void employeeCreatedInOneOrganizationIsInvisibleToAnother() {
        TenantContext.setOrganizationId(organizationA);
        var employee = employeeService.create(new CreateEmployeeRequest(
                "EMP-001", "Tenant A Employee", "employee@tenant-a.example",
                null, null, null, LocalDate.of(2025, 1, 1)));

        TenantContext.setOrganizationId(organizationB);
        assertThrows(ResponseStatusException.class, () -> employeeService.findById(employee.id()));
        assertEquals(0, employeeService.findAll().size());
    }

    @Test
    void employeeNumberCanBeReusedAcrossDifferentOrganizations() {
        TenantContext.setOrganizationId(organizationA);
        employeeService.create(new CreateEmployeeRequest(
                "EMP-SHARED", "Employee A", "a@tenant.example",
                null, null, null, LocalDate.of(2025, 1, 1)));

        TenantContext.setOrganizationId(organizationB);
        var employeeB = employeeService.create(new CreateEmployeeRequest(
                "EMP-SHARED", "Employee B", "b@tenant.example",
                null, null, null, LocalDate.of(2025, 1, 1)));

        assertEquals("EMP-SHARED", employeeB.employeeNumber());
    }

    private UUID createOrganization(String slug) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO organizations(id,name,slug) VALUES(?,?,?)",
                id, "Tenant Test", slug);
        return id;
    }
}
