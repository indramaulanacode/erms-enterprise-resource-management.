# ERMS Backend

## Requirements
- Java 21
- Maven 3.9+

## Configure JWT

Generate a 256-bit (or longer) secret and Base64-encode it. Example:

```bash
openssl rand -base64 48
```

Export the result as `JWT_SECRET` before starting the application:

```bash
export JWT_SECRET='replace-with-generated-base64-secret'
mvn spring-boot:run
```

The service intentionally has no insecure default signing key. Startup must fail when `JWT_SECRET` is absent or too short.

## Security foundation
- Bearer JWT validation with signature, issuer, and expiration verification.
- Stateless Spring Security configuration.
- Role authorities are derived from signed token claims and can be used with `@PreAuthorize`.
- Organization identity is derived from the signed `org` claim, never from a request parameter or header.
- `TenantContext` is cleared in a `finally` block after every request.

## Important implementation boundary
This milestone establishes token validation and tenant context. It does **not** yet include a production login flow, refresh-token rotation, user/role persistence, revocation, or complete tenant-scoped repositories. Do not deploy as a production identity system until those pieces and security tests are completed.

Every tenant-owned query must include the authenticated organization ID, for example:
`findByOrganizationIdAndId(TenantContext.getRequiredOrganizationId(), id)`.
Never load a tenant-owned record by its ID alone.
