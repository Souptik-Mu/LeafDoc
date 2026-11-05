# LeafDoc Backend

Spring Boot REST backend for LeafDoc. The current implementation provides account registration and authentication, personal plant management, plant-image storage, and a proxy endpoint for the external machine-learning service.

> **Implementation scope:** This document describes the code present in this repository snapshot. It does not imply that planned scan persistence, disease/treatment data, community features, or profile features are implemented.

## Contents

- [Technology stack](#technology-stack)
- [Implemented functionality](#implemented-functionality)
- [Architecture and package layout](#architecture-and-package-layout)
- [External services](#external-services)
- [Database model](#database-model)
- [Authentication and security](#authentication-and-security)
- [Image handling](#image-handling)
- [Configuration](#configuration)
- [Run locally](#run-locally)
- [Errors and HTTP behaviour](#errors-and-http-behaviour)
- [Known implementation notes](#known-implementation-notes)
- [API reference](#api-reference)

## Technology stack

| Area | Technology / implementation |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.1 |
| Build | Maven / Maven Wrapper |
| HTTP | Spring MVC, Spring `RestClient` |
| Persistence | Spring Data JPA, Hibernate |
| Database driver | MySQL Connector/J |
| Authentication | Spring Security, stateless JWT |
| JWT library | JJWT 0.12.6 |
| Password hashing | BCrypt |
| Validation | Jakarta Bean Validation |
| Email | Spring Mail, Thymeleaf HTML templates |
| External image storage | Supabase Storage |
| ML integration | External HTTP service (configured as a local service by default) |

## Implemented functionality

- Register a user through a pending email-verification flow.
- Verify email and automatically issue an authentication cookie.
- Login and logout using an HTTP-only JWT cookie.
- Request a password-reset email and reset the password using a one-time token.
- Retrieve the current authenticated principal.
- Create, list, retrieve, rename, and delete the authenticated user's plants.
- Upload, list (paginated), and delete plant images.
- Include the latest image response in plant listing and detail responses.
- Forward an image to an external ML service and expose a health-check proxy endpoint.
- Periodically delete expired verification/reset token records.

Not implemented in the supplied source snapshot: scan persistence/history, disease and care database endpoints, community posts/comments/likes, and profile management.

## Architecture and package layout

The application follows a conventional controller/service/repository structure.

```text
src/main/java/com/example/leafdoc/
├── client/          # Supabase Storage and ML HTTP clients
├── config/          # Security, CORS, RestClient, exception handling, async config
├── controllers/     # AuthController, PlantController, MlController
├── DTO/             # Request and response records
├── entity/          # User, Plant, PlantImage, VerificationToken
├── enums/           # Role, CropType
├── exceptions/      # Domain and external-service exceptions
├── repository/      # Spring Data repositories
├── schedulers/      # Expired token cleanup
├── security/        # JWT filter/service and authenticated principal
├── service/         # Authentication, email, and plant use cases
└── util/             # Image validation, storage paths, pending registration payload
```

### Request flow

1. The controller receives and validates the HTTP request.
2. The controller delegates business operations to a service.
3. Services use repositories for database access.
4. `ImageStorageClient` communicates with Supabase Storage.
5. `MlServiceClient` communicates with the ML service.
6. `GlobalExceptionHandler` maps selected application exceptions to JSON error responses.

## External services

### MySQL

Spring Data JPA persists users, plants, plant images, and verification tokens. The configured Hibernate DDL mode is `update`; see [Known implementation notes](#known-implementation-notes) before using this setting in production.

### Supabase Storage

`ImageStorageClient` uploads and deletes objects through the Supabase Storage REST API.

- Base URL: `SUPABASE_URL`
- API key: `SUPABASE_SECRET_KEY`
- Bucket: `Leafdoc_Image` (currently hard-coded as `supabase.bucket` in `application.properties`)
- Upload route used by the client: `POST /storage/v1/object/{bucket}/{path}`
- Delete route used by the client: `DELETE /storage/v1/object/{bucket}/{path}`
- Uploads use `x-upsert: false`.
- The backend constructs public object URLs using `/storage/v1/object/public/{bucket}/{path}`.

The current implementation therefore expects the bucket/object to be publicly readable for the returned URL to work. Do not expose the Supabase secret key to the frontend.

### Machine-learning service

`MlServiceClient` calls the configured ML service using the `x-ml-service-key` request header.

| Setting | Current value / behaviour |
|---|---|
| Base URL | `http://localhost:8000` (hard-coded in `application.properties`) |
| Health route | `GET /ml/health` |
| Prediction route | `POST /ml/predict` |
| Authentication header | `x-ml-service-key` |
| Connect timeout | 5 seconds |
| Read timeout | 60 seconds |

The backend's `/ml/test` endpoint proxies the ML health response. `/ml/predict` validates the uploaded image and forwards it to the ML service. The response is currently an untyped `Map<String,Object>`; the precise response schema depends on the ML service and is not defined in this repository.

### Email service

Spring Mail sends registration-verification and password-reset emails using Thymeleaf templates:

- `templates/emails/verification-email.html`
- `templates/emails/password-reset-email.html`

The sender address is configured through `MAIL_FROM`. Email methods are asynchronous through the configured email executor. A dummy email template/method is also present for testing.

## Database model

| Entity / table | Main fields and relationships |
|---|---|
| `User` / `users` | User identity, name, email, password hash, role, created timestamp |
| `Plant` / `plants` | Nickname, `CropType`, owner (`user_id`); unique nickname per user |
| `PlantImage` / `plant_image` | Plant FK, Supabase `object_path`, content type, creation timestamp |
| `VerificationToken` | Hashed token, creation/expiry timestamps, serialized pending registration or reset payload |

`PlantImage` has an index over `(plant_id, created_at, id)` for recent-image retrieval. Plant image history is ordered newest first, with ID as a tie-breaker.

Current `CropType` enum values: `TOMATO`, `POTATO`.

Current `Role` enum values: `GUEST`, `USER`, `ADMIN`.

## Authentication and security

### JWT cookie

- Cookie name: `accessToken`
- HTTP-only: `true`
- SameSite: `Lax`
- Path: `/`
- `Secure`: currently `false` in the controller cookie builders
- cookie Max-Age: 24 hour (from JWT expiration)
- JWT expiration: `app.jwt.expiration` (currently `24h`)

The JWT subject contains the user ID and a `role` claim contains the user's role. The JWT filter reads the cookie and creates an `AuthenticatedUserPrincipal`.

The frontend should use browser credentialed requests; it cannot read the HTTP-only cookie from JavaScript. For cross-origin requests, the frontend must be configured to send credentials and the origin must match the configured CORS origin.

### Authorization

Spring Security is stateless (`SessionCreationPolicy.STATELESS`). The configured public routes are:

- `/auth/register`
- `/auth/login`
- `/auth/verify-email`
- `/auth/forgot-password`
- `/auth/reset-password`
- `/ml/test`

All other routes require authentication. In particular, `/auth/resend-verification` is not in the public allow-list in the current security configuration, despite being a placeholder endpoint.

Plant operations are scoped by the authenticated user's ID in the service/repository layer.

### Security configuration requiring review before production

The current `SecurityConfig` disables CSRF protection while authentication is cookie-based. Cookie-authenticated state-changing requests need an explicit CSRF strategy; review this before deployment. The cookie builders currently set `Secure=false`; production HTTPS deployments should use secure cookies. CORS currently allows one configured origin and credentials.

The JWT secret must be a strong, private Base64-encoded key and must never be committed.

## Image handling

`ImageFile` validates uploads before they are sent to storage or the ML service:

- Empty files are rejected.
- Default maximum size: 2 MB (`app.image.max-size`).
- Allowed MIME types: JPEG, PNG, WebP (`app.image.allowed-types`).
- File extension is checked against the allowed MIME type.
- Actual image format is checked using file-signature/magic-byte detection.
- The request must provide a MIME type matching the detected format.

Plant image object paths are generated using a user ID, plant ID, UUID, and extension. The database stores the object path and metadata; the API returns an image URL and creation timestamp.

When a plant is deleted, the service deletes its Supabase objects and image metadata before deleting the plant record. When an upload succeeds in storage but database persistence fails, the service attempts compensating deletion of the uploaded object.

## Configuration

The application imports an optional `.env` file from the working directory using:

```properties
spring.config.import=optional:file:.env[.properties]
```

Create a local `.env` file based on the variable names below. Do not commit `.env` or real credentials.

| Environment variable | Used for | Example / notes |
|---|---|---|
| `PORT` | HTTP server port | Defaults to `8080` |
| `DB_URL` | JDBC URL | `jdbc:mysql://localhost:3306/leafdoc` |
| `DB_USER` | MySQL username | Local DB user |
| `DB_PASS` | MySQL password | Secret |
| `JWT_SECRET` | JWT signing key | Base64-encoded strong secret |
| `MAIL_HOST` | SMTP host | e.g. `smtp.gmail.com` |
| `MAIL_PORT` | SMTP port | e.g. `587` |
| `MAIL_USERNAME` | SMTP username | Mail account |
| `MAIL_PASSWORD` | SMTP password/app password | Secret |
| `MAIL_FROM` | Sender address | Required by `app.mail.from` |
| `ML_SERVICE_KEY` | Shared key for ML service | Must match ML service |
| `SUPABASE_URL` | Supabase project URL | `https://<project-ref>.supabase.co` |
| `SUPABASE_SECRET_KEY` | Supabase secret/service key | Secret; backend only |

The following are configured directly in `application.properties`, not through environment variables in this snapshot:

| Property | Current value |
|---|---|
| `server.port` | `${PORT:8080}` |
| `server.address` | `0.0.0.0` |
| `server.servlet.context-path` | `/api` |
| `spring.jpa.hibernate.ddl-auto` | `update` |
| `app.jwt.expiration` | `24h` |
| `app.cors.allowed-origin` | `http://localhost:5713` |
| `app.ml.service.url` | `http://localhost:8000` |
| `app.image.max-size` | `2MB` |
| `app.image.allowed-types` | `image/png,image/jpeg,image/webp` |
| `supabase.bucket` | `Leafdoc_Image` |

### Example `.env` template

```dotenv
PORT=8080

DB_URL=jdbc:mysql://localhost:3306/leafdoc
DB_USER=your_mysql_user
DB_PASS=replace_me

# Generate a private Base64-encoded signing key.
JWT_SECRET=replace_with_base64_secret

ML_SERVICE_KEY=replace_with_shared_ml_key

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_mail_account
MAIL_PASSWORD=replace_with_smtp_app_password
MAIL_FROM=your_sender_address

SUPABASE_URL=https://your-project-ref.supabase.co
SUPABASE_SECRET_KEY=replace_with_supabase_secret_key
```

Do not use the example values as real credentials. The checked-in `.env.example` in the supplied archive contained credential-like values; replace it with a sanitized template and rotate any credentials that were exposed.

## Run locally

### Prerequisites

- JDK 21
- MySQL
- Maven (or use the included Maven Wrapper)
- SMTP credentials if testing email flows
- Supabase project and configured storage bucket if testing plant images
- ML service running at the configured URL if testing ML endpoints

### Steps

1. Create a MySQL database.
2. Create a local `.env` file in the project root using the template above.
3. Confirm the Supabase bucket exists and its access policy matches the public-URL behaviour.
4. Start the ML service at `http://localhost:8000` if using ML endpoints.
5. Start the backend:

```bash
# macOS / Linux
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

The configured base URL is:

```text
http://localhost:8080/api
```

### Build and test

```bash
./mvnw clean test
./mvnw clean package
```

The supplied test source contains only the Spring Boot application context smoke test; endpoint-level behaviour is not comprehensively tested in this snapshot.

## Errors and HTTP behaviour

### Explicit global exception mappings

| Exception | HTTP status | Response body |
|---|---:|---|
| `MethodArgumentNotValidException` | 400 | `{"error":"<first field validation message>"}` |
| `InvalidCredentialsException` | 401 | `{"error":"Invalid email or password"}` |
| `ResourceNotFoundException` | 404 | `{"error":"RESOURCE_NOT_FOUND","message":"Plant Does Not Exist for user"}` |
| `MlServiceResponseException` | 502 | `{"error":"ML_SERVICE_ERROR","message":"The ML service returned an unexpected response","status":"<upstream status text>"}` |
| `ImageStorageException` | 502 | `{"error":"IMAGE_STORAGE_ERROR","message":"<message>"}` |
| `MlServiceUnavailableException` | 503 | `{"error":"ML_SERVICE_UNAVAILABLE","message":"<message>"}` |

Image validation also throws `ResponseStatusException` directly:

| Condition | HTTP status | Message |
|---|---:|---|
| Missing or empty image | 400 | `Please upload an image` |
| Invalid filename/extension | 400 | `Invalid image filename` |
| Unsupported/mismatched image format | 400 | `Please upload an correct image` |
| File exceeds configured size | 413 | `Image exceeds maximum size of <configured size>` |
| Unable to read/inspect file | 400 | Corresponding image-read/inspection message |

Spring Security and framework-generated errors may have a different response body because they are not normalized by `GlobalExceptionHandler` in the supplied code. The handler does not currently define an explicit `AccessDeniedException` mapping.

## Known implementation notes

These are observations from the supplied source, not changes made by this documentation:

1. **Image delete route contains a trailing parenthesis.** The mapping is currently `/{plantId}/image/{imageId})`. The route in `API.md` records this literal mapping. Remove the extra `)` in the controller if it is unintended.
2. **CORS omits PATCH.** `CorsConfig` allows `GET`, `POST`, `PUT`, `DELETE`, and `OPTIONS`, but the plant rename endpoint uses `PATCH`. Cross-origin browser requests to that endpoint may fail preflight until PATCH is allowed.
3. **CSRF is disabled with cookie authentication.** Review and implement an appropriate CSRF defence before production.
4. **Cookie security flags are development-oriented.** The auth cookie builders use `Secure=false`.
5. **Cookie Max-Age and JWT expiry differ.** Login cookie Max-Age is one hour, verification cookie Max-Age is 20 minutes, while the JWT itself is configured for 24 hours.
6. **ML service URL and Supabase bucket are hard-coded properties.** Move them to environment-backed properties for environment-specific deployment.
7. **`/auth/resend-verification` is a placeholder.** It returns 202 but does not resend an email, and it is not permitted anonymously by the current security allow-list.
8. **ML response is not strongly typed.** `/ml/predict` returns the upstream response as `Map<String,Object>`.
9. **Password-reset email enumeration.** The current forgot-password service throws `InvalidCredentialsException` when an email does not exist. Consider returning a generic accepted response to avoid revealing account existence.
10. **Registration validation is partial.** `RegisterRequest` validates `name` as nonblank and `email` as an email, but does not validate password strength or explicitly require email nonblank. `LoginRequest` has no field constraints.
11. **Plant list sort is fixed in the controller.** The endpoint accepts Spring `Pageable`, but the controller replaces requested sorting with `id DESC`; page size defaults to 20 and is capped at 100.
12. **Hibernate schema mode is `update`.** Use managed migrations and a deliberate schema strategy for production.
13. **Debug logging includes verification/reset links.** `AuthService` logs raw token-bearing links at DEBUG level. Remove or protect these logs in production.
14. **Supabase URL generation assumes a public bucket.** Private-bucket access would require signed URLs or a backend image proxy.
15. **`/ml/test` is public, while `/ml/predict` is authenticated** because of the security allow-list and the default authenticated rule.

## API reference

See [`API.md`](API.md) for the endpoint-by-endpoint request and response reference.
