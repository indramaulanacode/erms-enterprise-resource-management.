# ERMS — Enterprise Resource Management System

A modular enterprise resource management platform built with Java 21, Spring Boot, React, TypeScript, and PostgreSQL.

## Project status

The repository is being initialized. The first implementation milestone focuses on a secure backend foundation:

- JWT-based stateless authentication
- Role-based access control (RBAC)
- Organization-scoped multi-tenancy
- PostgreSQL schema migrations
- API documentation and automated tests

## Planned modules

- Organization and employee management
- Attendance and leave management
- Inventory and warehouse management
- Procurement and suppliers
- Finance and transaction records
- Reporting and audit logs

## Technology

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot |
| Security | Spring Security, JWT |
| Frontend | React, TypeScript, Vite |
| Database | PostgreSQL, Flyway |
| API | REST, OpenAPI |
| Delivery | Docker, GitHub Actions |

## Development

See the backend and frontend README files for setup instructions. Never commit real credentials or production secrets.

## Security

All tenant-owned data must be queried within the authenticated user's organization context. Never trust an organization ID supplied by a client as the authorization boundary.
