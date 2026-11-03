# Tenant Isolation

ERMS treats each organization as a hard data boundary.

## Request identity
1. JWT filter verifies signature, issuer, and expiry.
2. The authenticated principal contains the organization claim.
3. TenantContextFilter copies that verified claim into TenantContext.
4. TenantContextFilter clears the ThreadLocal in a finally block.

Never accept organization_id from a URL, query parameter, request body, or arbitrary header as the authorization boundary.

## Employee module
Every employee SELECT, UPDATE, and DELETE is scoped by both employee ID (where applicable) and the authenticated organization ID. Department and position references use composite foreign keys (id, organization_id), preventing cross-organization assignments at database level.

## Rules for future modules
- Every tenant-owned table must have a non-null organization_id.
- Parent tables referenced by tenant-scoped children need UNIQUE (id, organization_id).
- Use composite foreign keys (parent_id, organization_id).
- Scope every read, update, and delete by organization_id.
- Derive organization_id only from TenantContext.
- Test that tenant A cannot read, update, delete, or reference tenant B data.
- Include organization_id in cache keys for tenant-owned data.
- Background jobs must receive an explicit trusted organization ID, never request ThreadLocal state.

## Employee API
- POST /api/v1/employees
- GET /api/v1/employees
- GET /api/v1/employees/{id}
- PUT /api/v1/employees/{id}
- DELETE /api/v1/employees/{id}

Access is restricted to ORG_ADMIN and HR_MANAGER. Requests cannot assign an organization ID; the server derives it from the verified access token.
