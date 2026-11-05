# LeafDoc Backend API Reference

Technical endpoint reference generated from the supplied backend source snapshot.

- **Base URL (local):** `http://localhost:8080/api`
- **Context path:** `/api`
- **Content types:** `application/json` unless otherwise stated; image endpoints use `multipart/form-data`.
- **Authentication:** JWT in the `accessToken` HTTP-only cookie.
- **Ownership:** plant and plant-image endpoints operate only on the authenticated user's resources.

> This reference documents the implementation as written. Known route/configuration issues are called out explicitly. Empty response bodies are shown as `—`. Examples use illustrative values.

## 1. Common conventions

### 1.1 Authentication cookie

| Attribute | Login         | Email verification | Logout |
|---|---------------|--------------------|---|
| Cookie | `accessToken` | `accessToken`      | `accessToken` |
| HttpOnly | `true`        | `true`             | `true` |
| Secure | `false`       | `false`            | `false` |
| SameSite | `Lax`         | `Lax`              | `Lax` |
| Path | `/`           | `/`                | `/` |
| Max-Age | 24 hours      | 24 hours           | 24 hours |

The JWT itself expires according to `app.jwt.expiration` (configured as `24h`). The cookie Max-Age derived from JWT expiration.

The frontend must send cookies on credentialed browser requests. JavaScript cannot read the HTTP-only JWT cookie.

### 1.2 Public routes

The current `SecurityConfig` explicitly permits:

| Route | Method | Public? |
|---|---|---|
| `/auth/register` | POST | Yes |
| `/auth/login` | POST | Yes |
| `/auth/verify-email` | GET | Yes |
| `/auth/forgot-password` | POST | Yes |
| `/auth/reset-password` | POST | Yes |
| `/ml/test` | GET | Yes |

All other routes require authentication. `/auth/resend-verification` is **not** in the public allow-list in the supplied source, even though it is a placeholder endpoint.

### 1.3 Error response formats

| HTTP status | Response body |
|---:|---|
| 400 validation | `{"error":"<validation message>"}` |
| 401 invalid credentials/token | `{"error":"Invalid email or password"}` |
| 404 resource not found | `{"error":"RESOURCE_NOT_FOUND","message":"Plant Does Not Exist for user"}` |
| 502 ML upstream error | `{"error":"ML_SERVICE_ERROR","message":"The ML service returned an unexpected response","status":"<upstream status>"}` |
| 502 storage error | `{"error":"IMAGE_STORAGE_ERROR","message":"<storage error message>"}` |
| 503 ML unavailable | `{"error":"ML_SERVICE_UNAVAILABLE","message":"<message>"}` |

Other framework/Spring Security errors may have a different body; they are not normalized by the supplied global exception handler.

## 2. Authentication APIs

### 2.1 Register

| Property | Value |
|---|---|
| Method | `POST` |
| Path | `/auth/register` |
| Full local URL | `http://localhost:8080/api/auth/register` |
| Authentication | Public |
| Content-Type | `application/json` |
| Success | `202 Accepted`, empty body |

**Request DTO:** `RegisterRequest`

| Field | Type | Required by annotations | Constraints / notes                     |
|---|---|---:|-----------------------------------------|
| `name` | string | Yes | `@NotBlank`                             |
| `email` | string | No | `@Email`, `@NotBlank`                   |
| `password` | string | No | No validation constraint in DTO         |
| `role` | enum string | No | `GUEST`, `USER`, `ADMIN`; `@NotBlank`   |

**Example request**

```json
{
  "name": "Alex Green",
  "email": "alex@example.com",
  "password": "SecurePass123",
  "role": "USER"
}
```

**Success response**

```http
HTTP/1.1 202 Accepted
```

No response body.

**Behaviour**

- Checks whether the email already exists.
- Generates a random verification token and stores only its SHA-256 hash.
- Encodes the password with BCrypt and stores pending registration data in the `tokens` table.
- Sends a verification email asynchronously.
- Verification token lifetime: 15 minutes.
- The user record is created only when the email is verified.

**Errors:** `InvalidCredentialsException` maps to 401 when the email already exists. Request validation errors map to 400. Other persistence/email failures are not given a dedicated documented response mapping.

### 2.2 Login

| Property | Value |
|---|---|
| Method | `POST` |
| Path | `/auth/login` |
| Authentication | Public |
| Content-Type | `application/json` |
| Success | `200 OK` |
| Response side effect | Sets `accessToken` cookie |

**Request DTO:** `LoginRequest`

| Field | Type | Required by annotations | Constraints |
|---|---|---:|---|
| `email` | string | No | No DTO constraints |
| `password` | string | No | No DTO constraints |

**Example request**

```json
{
  "email": "alex@example.com",
  "password": "SecurePass123"
}
```

**Success response body**

```json
{
  "message": "Login successful"
}
```

**Response cookie:** `accessToken`; HttpOnly, SameSite=Lax, Path=/, Secure=false, Max-Age=3600.

**Errors**

| Condition | Status | Body |
|---|---:|---|
| Email not found | 401 | `{"error":"Invalid email or password"}` |
| Password mismatch | 401 | `{"error":"Invalid email or password"}` |

### 2.3 Verify email

| Property | Value |
|---|---|
| Method | `GET` |
| Path | `/auth/verify-email` |
| Authentication | Public |
| Query parameter | `token` (string, required) |
| Success | `201 Created`, empty body |
| Response side effect | Sets `accessToken` cookie |

**Example request**

```http
GET /api/auth/verify-email?token=<raw-verification-token>
```

**Success response:** empty body; sets `accessToken` cookie with HttpOnly=true, SameSite=Lax, Path=/, Secure=false, Max-Age=1200 seconds.

**Behaviour:** hashes the supplied token, checks existence and expiry, creates the user from the serialized pending registration payload, deletes the verification token, and issues a JWT.

**Errors:** missing/unknown/expired token is represented by `InvalidCredentialsException` (401). The source does not define a separate invalid-token response.

### 2.5 Forgot password

| Property | Value |
|---|---|
| Method | `POST` |
| Path | `/auth/forgot-password` |
| Authentication | Public |
| Content-Type | Query parameter; no request body |
| Success | `202 Accepted`, empty body |

**Query parameters**

| Name | Type | Required | Constraints |
|---|---|---:|---|
| `email` | string | Yes | `@NotBlank`, `@Email` |

**Example request**

```http
POST /api/auth/forgot-password?email=alex%40example.com
```

**Success response:** empty body.

**Behaviour:** finds the user by email, creates a random token whose SHA-256 hash is stored, sets expiry to 15 minutes, and sends a password-reset email asynchronously. The reset link targets the frontend `/reset-password` route.

**Errors:** an unknown email throws `InvalidCredentialsException` and maps to 401. This reveals whether an account exists; consider changing to a generic response if account enumeration is not desired.

### 2.6 Reset password

| Property | Value |
|---|---|
| Method | `POST` |
| Path | `/auth/reset-password` |
| Authentication | Public |
| Content-Type | `application/json` |
| Success | `200 OK`, empty body |

**Request DTO:** `ResetPassRequest`

| Field | Type | Required | Constraints |
|---|---|---:|---|
| `token` | string | Yes | `@NotBlank` |
| `newPassword` | string | Yes | `@NotBlank`, minimum length 8 |

**Example request**

```json
{
  "token": "<raw-password-reset-token>",
  "newPassword": "NewSecurePass123"
}
```

**Success response:** empty body.

**Behaviour:** hashes and validates the token, checks its expiry, updates the account password using BCrypt, and deletes the token.

**Errors**

| Condition | Status | Body |
|---|---:|---|
| Missing required field / password shorter than 8 | 400 | `{"error":"<validation message>"}` |
| Invalid/expired token or failed user update | 401 | `{"error":"Invalid email or password"}` |

### 2.7 Current authenticated principal

| Property | Value |
|---|---|
| Method | `GET` |
| Path | `/auth/me` |
| Authentication | Required |
| Success | `200 OK` |

**Request body:** none.

**Success response:** serialized `AuthenticatedUserPrincipal` record.

```json
{
  "userId": 42,
  "role": "USER"
}
```

Fields are `userId` (number) and `role` (enum string: `GUEST`, `USER`, `ADMIN`).

### 2.8 Logout

| Property | Value                       |
|---|-----------------------------|
| Method | `GET`                       |
| Path | `/auth/logout`              |
| Authentication | Required                    |
| Request body | None                        |
| Success | `204 No Content`            |
| Response side effect | Clears `accessToken` cookie |

**Success response:** empty body. The response sets an empty `accessToken` cookie with Max-Age=0, HttpOnly=true, SameSite=Lax, Path=/, Secure=false.

**Note:** logout is client-cookie deletion only. And Revokes the session.

## 3. Plant APIs

All plant routes require authentication. The authenticated user's ID is taken from the JWT principal; clients must not submit a user ID to select the owner.

### 3.1 Create plant

| Property | Value |
|---|---|
| Method | `POST` |
| Path | `/plants` |
| Authentication | Required |
| Content-Type | `application/json` |
| Success | `201 Created`, empty body |

**Request DTO:** `CreatePlantRequest`

| Field | Type | Required | Constraints / allowed values |
|---|---|---:|---|
| `nickname` | string | Yes | `@NotBlank`, max length 20 |
| `cropType` | string enum | Yes | `TOMATO`, `POTATO` |

**Example request**

```json
{
  "nickname": "Balcony Tomato",
  "cropType": "TOMATO"
}
```

**Success response:** empty body.

**Notes:** nickname uniqueness is scoped to the user through the `(user_id, nickname)` unique constraint.

### 3.2 List authenticated user's plants

| Property | Value |
|---|---|
| Method | `GET` |
| Path | `/plants` |
| Authentication | Required |
| Success | `200 OK` |
| Response | Spring Data `Page<PlantResponse>` |

**Query parameters**

| Name | Type | Required | Default | Effective behaviour |
|---|---|---:|---:|---|
| `page` | integer | No | `0` | Zero-based page index |
| `size` | integer | No | `20` | Capped at 100 |
| `sort` | string | No | Spring default | Requested sort is overridden by controller to `id,DESC` |

**Example request**

```http
GET /api/plants?page=0&size=20
```

**PlantResponse fields**

| Field | Type | Nullable | Description |
|---|---|---:|---|
| `id` | number | No | Plant ID |
| `nickname` | string | No | User-defined plant name |
| `cropType` | string | No | `TOMATO` or `POTATO` |
| `latestImage` | object | Yes | Latest image metadata/URL; null when no image exists |

**Example response**

```json
{
  "content": [
    {
      "id": 17,
      "nickname": "Balcony Tomato",
      "cropType": "TOMATO",
      "latestImage": {
        "id": 31,
        "imageUrl": "https://<project>.supabase.co/storage/v1/object/public/Leafdoc_Image/...",
        "createdAt": "2026-10-30T08:15:30Z"
      }
    },
    {
      "id": 18,
      "nickname": "Potato Pot",
      "cropType": "POTATO",
      "latestImage": null
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 2,
  "totalPages": 1,
  "last": true,
  "first": true,
  "size": 20,
  "number": 0,
  "numberOfElements": 2,
  "empty": false
}
```

The `pageable` object above is abbreviated for readability; Spring Data's serialized `Page` metadata may include additional fields depending on framework version/configuration.

### 3.3 Get plant details

| Property | Value |
|---|---|
| Method | `GET` |
| Path | `/plants/{plantId}` |
| Authentication | Required |
| Path parameter | `plantId` (Long, required) |
| Success | `200 OK` |

**Request body:** none.

**Example request**

```http
GET /api/plants/17
```

**Success response**

```json
{
  "id": 17,
  "nickname": "Balcony Tomato",
  "cropType": "TOMATO",
  "latestImage": {
    "id": 31,
    "imageUrl": "https://<project>.supabase.co/storage/v1/object/public/Leafdoc_Image/...",
    "createdAt": "2026-10-30T08:15:30Z"
  }
}
```

`latestImage` is `null` if no image is associated with the plant.

**Errors:** 404 if the plant does not exist or does not belong to the authenticated user.

### 3.4 Rename plant

| Property | Value |
|---|---|
| Method | `PATCH` |
| Path | `/plants/{plantId}` |
| Authentication | Required |
| Path parameter | `plantId` (Long, required) |
| Content-Type | `application/json` |
| Success | `200 OK` |

**Request body:** a JSON string, not a JSON object. Constraints on the controller parameter are `@NotBlank` and `@Size(max=20)`.

```json
"Balcony Cherry Tomato"
```

**Success response**

```json
{
  "id": 17,
  "nickname": "Balcony Cherry Tomato",
  "cropType": "TOMATO",
  "latestImage": null
}
```

The update response is built from the plant without loading its latest image, so `latestImage` is returned as `null` by the current implementation even if the plant has images.

**Errors:** 404 for missing/non-owned plant. Validation failures are intended to be 400 where method validation is applied.

**CORS note:** the current CORS allowed-method list does not include PATCH. Cross-origin browser clients may fail preflight until PATCH is added to `CorsConfig`.

### 3.5 Delete plant

| Property | Value |
|---|---|
| Method | `DELETE` |
| Path | `/plants/{plantId}` |
| Authentication | Required |
| Path parameter | `plantId` (Long, required) |
| Success | `204 No Content` |

**Request body:** none.

**Success response:** empty body.

**Behaviour:** verifies plant ownership, deletes each associated image object from Supabase, deletes image metadata rows, then deletes the plant row.

**Errors:** 404 if the plant does not exist or is not owned by the authenticated user. Supabase failures can prevent the operation from completing.

## 4. Plant image APIs

All image routes require authentication and verify that the parent plant belongs to the authenticated user.

### 4.1 Upload plant image

| Property | Value |
|---|---|
| Method | `POST` |
| Path | `/plants/{plantId}/image` |
| Authentication | Required |
| Path parameter | `plantId` (Long, required) |
| Content-Type | `multipart/form-data` |
| Form field | `file` (required file) |
| Success | `201 Created` |

**Form data**

| Field | Type | Required | Constraints |
|---|---|---:|---|
| `file` | file | Yes | JPEG, PNG, WebP; max 2 MB by default |

**Example request**

```bash
curl -X POST \
  'http://localhost:8080/api/plants/17/image' \
  -H 'Cookie: accessToken=<jwt-cookie-value>' \
  -F 'file=@./tomato.jpg'
```

**Success response DTO:** `ImageResponse`

```json
{
  "id": 31,
  "imageUrl": "https://<project>.supabase.co/storage/v1/object/public/Leafdoc_Image/...",
  "createdAt": "2026-10-30T08:15:30Z"
}
```

**Validation responses**

| Condition | Status | Example error message |
|---|---:|---|
| No file / empty file | 400 | `Please upload an image` |
| Invalid filename or extension | 400 | `Invalid image filename` |
| Detected content type does not match supplied MIME type / unsupported type | 400 | `Please upload an correct image` |
| File larger than configured max | 413 | `Image exceeds maximum size of 2MB` |
| Cannot read/inspect upload | 400 | Image read/inspection error message |

**Other errors:** 404 for missing/non-owned plant; 502 for Supabase Storage error.

### 4.2 Get paginated plant image history

| Property | Value |
|---|---|
| Method | `GET` |
| Path | `/plants/{plantId}/image` |
| Authentication | Required |
| Path parameter | `plantId` (Long, required) |
| Success | `200 OK` |
| Response | Spring Data `Page<ImageResponse>` |

**Query parameters**

| Name | Type | Required | Default | Description |
|---|---|---:|---:|---|
| `page` | integer | No | `0` | Zero-based page index |
| `size` | integer | No | `20` | Number of records per page |
| `sort` | string | No | Repository order | Query orders by `createdAt DESC, id DESC` |

**Example request**

```http
GET /api/plants/17/image?page=0&size=20
```

**ImageResponse fields**

| Field | Type | Nullable | Description |
|---|---|---:|---|
| `id` | number | No | Plant image record ID |
| `imageUrl` | string | No | Public Supabase object URL |
| `createdAt` | ISO-8601 instant | No | Image creation timestamp |

**Example response**

```json
{
  "content": [
    {
      "id": 31,
      "imageUrl": "https://<project>.supabase.co/storage/v1/object/public/Leafdoc_Image/...",
      "createdAt": "2026-10-30T08:15:30Z"
    },
    {
      "id": 28,
      "imageUrl": "https://<project>.supabase.co/storage/v1/object/public/Leafdoc_Image/...",
      "createdAt": "2026-10-28T10:05:00Z"
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "numberOfElements": 2,
  "first": true,
  "last": true,
  "empty": false
}
```

Spring Data may serialize additional `Page`/`Pageable` metadata fields.

**Errors:** 404 if the plant does not exist or is not owned by the authenticated user.

### 4.3 Delete plant image

| Property | Value |
|---|---|
| Method | `DELETE` |
| Literal mapping in source | `/{plantId}/image/{imageId})` |
| Full literal local URL | `/api/plants/{plantId}/image/{imageId})` |
| Authentication | Required |
| Path parameters | `plantId` (Long), `imageId` (Long) |
| Success | `204 No Content` |

> **Route defect:** the controller mapping contains a trailing `)` after `{imageId}`. This documentation records the literal source mapping. If unintended, change the annotation to `@DeleteMapping("/{plantId}/image/{imageId}")`; after that correction, the URL becomes `/api/plants/{plantId}/image/{imageId}`.

**Request body:** none.

**Example request using the literal current mapping**

```http
DELETE /api/plants/17/image/31)
```

**Success response:** empty body.

**Behaviour:** verifies ownership of the plant, verifies that the image belongs to that plant, deletes the Supabase object, then deletes the database row.

**Errors:** 404 for missing/non-owned plant or image; 502 for storage error.

## 5. Machine-learning proxy APIs

The ML controller does not directly require an authenticated principal. `/ml/test` is explicitly public; `/ml/predict` is not public in the security configuration and therefore requires authentication.

### 5.1 Predict

| Property | Value |
|---|---|
| Method | `POST` |
| Path | `/ml/predict` |
| Authentication | Required by current SecurityConfig |
| Content-Type | `multipart/form-data` |
| Form field | `file` (required file) |
| Success | `200 OK` |
| Response type | `Map<String,Object>` from ML service |

**Request**

| Field | Type | Required | Validation |
|---|---|---:|---|
| `file` | file | Yes | Same `ImageFile` validation as plant image upload |

**Example request**

```bash
curl -X POST \
  'http://localhost:8080/api/ml/predict' \
  -H 'Cookie: accessToken=<jwt-cookie-value>' \
  -F 'file=@./leaf.jpg'
```

**Success response:** the backend returns the ML service's JSON response as-is. The precise keys and value types are not defined in this backend source. Obtain the authoritative response schema from the ML service implementation.

**Errors**

| Condition | Status | Response |
|---|---:|---|
| Invalid image | 400 / 413 | `ResponseStatusException` response |
| ML service returns 4xx | 502 | `ML_SERVICE_ERROR` |
| ML service returns 5xx | 503 | `ML_SERVICE_UNAVAILABLE` |
| ML service cannot be reached / timeout | 503 | `ML_SERVICE_UNAVAILABLE` |

### 5.2 ML health check

| Property | Value |
|---|---|
| Method | `GET` |
| Path | `/ml/test` |
| Authentication | Public |
| Success | `200 OK` |
| Response type | `Map<String,Object>` from ML service |

**Request body:** none.

**Behaviour:** backend calls `GET /ml/health` on the configured ML service and relays the returned JSON map.

**Success response:** shape is defined by the ML service and is not specified in this repository.

**Errors:** 503 if the ML service cannot be reached; upstream 5xx is also mapped to 503 by the configured RestClient status handler.

## 6. DTO and enum quick reference

### `RegisterRequest`

| Field | Type |
|---|---|
| `name` | string |
| `email` | string |
| `password` | string |
| `role` | `GUEST` \| `USER` \| `ADMIN` |

### `LoginRequest`

| Field | Type |
|---|---|
| `email` | string |
| `password` | string |

### `ResetPassRequest`

| Field | Type |
|---|---|
| `token` | string |
| `newPassword` | string (minimum 8 characters) |

### `CreatePlantRequest`

| Field | Type |
|---|---|
| `nickname` | string (nonblank, max 20 characters) |
| `cropType` | `TOMATO` \| `POTATO` |

### `PlantResponse`

| Field | Type |
|---|---|
| `id` | number |
| `nickname` | string |
| `cropType` | `TOMATO` \| `POTATO` |
| `latestImage` | `ImageResponse` or null |

### `ImageResponse`

| Field | Type |
|---|---|
| `id` | number |
| `imageUrl` | string |
| `createdAt` | ISO-8601 instant |

### `AuthenticatedUserPrincipal`

| Field | Type |
|---|---|
| `userId` | number |
| `role` | `GUEST` \| `USER` \| `ADMIN` |

## 7. Endpoint inventory

| Method | Endpoint | Authentication | Success |
|---|---|---|---:|
| POST | `/auth/register` | Public | 202 |
| POST | `/auth/login` | Public | 200 |
| GET | `/auth/verify-email?token=...` | Public | 201 |
| POST | `/auth/resend-verification` | Required | 202 (placeholder) |
| POST | `/auth/forgot-password?email=...` | Public | 202 |
| POST | `/auth/reset-password` | Public | 200 |
| GET | `/auth/me` | Required | 200 |
| POST | `/auth/logout` | Required | 204 |
| POST | `/plants` | Required | 201 |
| GET | `/plants` | Required | 200 |
| GET | `/plants/{plantId}` | Required | 200 |
| PATCH | `/plants/{plantId}` | Required | 200 |
| DELETE | `/plants/{plantId}` | Required | 204 |
| POST | `/plants/{plantId}/image` | Required | 201 |
| GET | `/plants/{plantId}/image` | Required | 200 |
| DELETE | `/plants/{plantId}/image/{imageId})` (literal source mapping) | Required | 204 |
| POST | `/ml/predict` | Required | 200 |
| GET | `/ml/test` | Public | 200 |

