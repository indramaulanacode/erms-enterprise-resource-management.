# ERMS Implementation Roadmap

## Milestone 1 — Secure backend foundation
- [x] Java 21 / Spring Boot API bootstrap
- [x] JWT signature, issuer, and expiry validation
- [x] Stateless Spring Security and method-level RBAC
- [x] Organization identity context from signed claims

## Milestone 2 — Identity and access management
- [x] PostgreSQL schema for organizations, users, roles, and refresh tokens
- [x] Organization registration and first administrator
- [x] Login with BCrypt password verification
- [x] Opaque refresh tokens with rotation and replay-family revocation
- [x] Logout endpoint
- [ ] Integration tests for registration, login, refresh, logout, and replay
- [ ] Rate limiting and email verification

## Milestone 3 — Tenant-safe domain foundation
- [ ] Tenant-scoped repositories and service policies
- [ ] Composite foreign keys for all tenant-owned relationships
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
