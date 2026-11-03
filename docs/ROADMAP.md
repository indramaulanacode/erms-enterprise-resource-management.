# ERMS Implementation Roadmap

## Milestone 1 — Secure backend foundation
- [x] Java 21 / Spring Boot API bootstrap
- [x] JWT signature, issuer, and expiry validation
- [x] Stateless Spring Security filter chain
- [x] RBAC role authorities and method-level authorization
- [x] Organization identity context from signed token claims
- [x] Fail-closed tenant context accessor
- [x] Unit tests for token validation and tenant context
- [ ] Compile and execute CI against GitHub Actions

## Milestone 2 — Identity and access management
- [ ] Organization, user, role, and permission persistence
- [ ] Login endpoint with BCrypt/Argon2id password verification
- [ ] Refresh-token rotation, revocation, and reuse detection
- [ ] Account disablement and role-change invalidation
- [ ] Login rate limiting and security audit events

## Milestone 3 — Tenant-safe domain foundation
- [ ] PostgreSQL + Flyway migrations
- [ ] Tenant-scoped repositories and service policies
- [ ] Composite foreign keys for tenant-owned relationships
- [ ] Cross-tenant access integration tests
- [ ] Audit log and request correlation

## Milestone 4 — Business modules
- [ ] Organization and employee management
- [ ] Attendance and leave management
- [ ] Inventory and warehouse management
- [ ] Procurement and supplier management
- [ ] Finance and reporting

## Milestone 5 — Frontend and delivery
- [ ] React + TypeScript application
- [ ] Role-aware navigation and protected routes
- [ ] Docker Compose development environment
- [ ] Deployment, observability, backup, and recovery
