# ERMS Backend

## Requirements
- Java 21
- Maven 3.9+
- PostgreSQL 16+

## Configure environment

Create a PostgreSQL database and application user, then export:

```bash
export DB_URL='jdbc:postgresql://localhost:5432/erms'
export DB_USERNAME='erms'
export DB_PASSWORD='change-this-password'
export JWT_SECRET="$(openssl rand -base64 48)"
export JWT_ISSUER='erms-api'
mvn spring-boot:run
```

Flyway applies the database migrations automatically. Never commit real credentials.

## Authentication API

All endpoints accept and return JSON under /api/v1/auth.

### Register an organization and first administrator
POST /register

```json
{
  "organizationName": "Acme Corporation",
  "organizationSlug": "acme",
  "email": "admin@acme.example",
  "password": "a-long-unique-password",
  "fullName": "Organization Administrator"
}
```

Creates the organization, first user, and ORG_ADMIN membership in one transaction. Role selection is not accepted from the client.

### Login
POST /login

```json
{
  "organizationSlug": "acme",
  "email": "admin@acme.example",
  "password": "a-long-unique-password"
}
```

### Refresh
POST /refresh with a JSON body containing refreshToken. A successful request rotates the refresh token. Reuse of an already rotated token revokes its active token family.

### Logout
POST /logout with a JSON body containing refreshToken. Revokes the supplied refresh token.

## Security notes
- Passwords are hashed with BCrypt (cost 12).
- Refresh tokens are 512-bit random opaque values; only SHA-256 hashes are stored.
- Access tokens are short-lived signed JWTs.
- Organization identity comes from the signed JWT claim, not a request-supplied organization ID.
- Every tenant-owned query must include the authenticated organization ID.
- Rate limiting, email verification, password reset, audit trails, and security integration tests remain required before production use.
