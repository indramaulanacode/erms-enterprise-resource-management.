# Security Design

## Authentication
Access tokens are signed JWTs. The API validates the signature, configured issuer, and token expiration before creating an authenticated principal.

Required claims:
- `sub`: immutable user UUID
- `org`: immutable organization UUID
- `username`: display/login identifier
- `roles`: role names assigned by the trusted identity service

The signing key is supplied only through `JWT_SECRET`. Use a secret manager in production and rotate keys through a planned key-id strategy.

## Authorization (RBAC)
Use method-level checks for business operations:

```java
@PreAuthorize("hasAnyRole('ORG_ADMIN', 'HR_MANAGER')")
public void approveLeaveRequest(UUID requestId) { ... }
```

Never accept role names from client request bodies. Roles must be assigned by trusted server-side identity and signed into the token.

## Tenant isolation
The tenant ID is sourced exclusively from a verified JWT claim. Services and repositories must scope every tenant-owned read, update, and delete by that organization ID. A client-supplied organization ID is not an authorization decision.

Use composite database constraints where appropriate, such as a foreign key containing both `organization_id` and the referenced row ID. Add integration tests that prove organization A cannot access organization B data.

## Remaining production controls
- Login with password hashing (Argon2id or BCrypt) and rate limiting
- Short-lived access tokens plus refresh-token rotation and reuse detection
- Token revocation / account disablement strategy
- Audit events for privileged actions
- CORS allowlist, TLS, security headers, and secret management
- Tenant-isolation integration tests for every domain module
