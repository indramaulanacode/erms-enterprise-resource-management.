# Security Design

## Authentication
- Passwords are stored as BCrypt hashes (work factor 12).
- Access tokens are signed JWTs with issuer and expiration validation.
- Refresh tokens are high-entropy opaque values. Only SHA-256 hashes are stored in PostgreSQL.
- Refresh token rotation is transactional. Reuse of a rotated token revokes all still-active tokens in its family.
- Disabled users and suspended organizations cannot log in or refresh sessions.

## Authorization (RBAC)
Roles are loaded from server-owned organization membership records. Registration always assigns the first account the ORG_ADMIN role; client input cannot select roles. Use method-level PreAuthorize checks for protected business operations.

## Tenant isolation
The tenant ID is sourced exclusively from a verified JWT claim. Services and repositories must scope every tenant-owned read, update, and delete by that organization ID. Composite database constraints bind user-role assignments to the same organization.

## Remaining production controls
- Login and registration rate limiting
- Email verification and secure password reset
- Access-token revocation strategy for account disablement and role changes
- Refresh-token cleanup job and security event audit
- CORS allowlist, TLS, secure headers, and secret manager
- Tenant-isolation integration tests for every business module
