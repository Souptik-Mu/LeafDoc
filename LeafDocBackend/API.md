# LeafDoc Backend API Reference

This document describes the REST endpoints implemented in the supplied backend source snapshot.

- **Local base URL:** `http://localhost:8080/api`
- **Context path:** `/api`
- **Default content type:** `application/json`
- **Multipart endpoints:** `multipart/form-data`
- **Authentication:** JWT bearer material is issued in the `accessToken` cookie; authenticated routes are protected by the JWT filter.
- **Ownership:** plant, plant-image, and scan records are scoped to the authenticated user.

> This is an implementation reference, not a promise of idealized behaviour. Notes marked **Implementation note** call out behaviour or limitations visible in the source. Example IDs, URLs, tokens, and data are illustrative.

## 1. Conventions

### 1.1 Authentication and cookies

Login, email verification, and logout responses manipulate the `accessToken` cookie.

| Attribute | Login / verification | Logout / logout-all |
|---|---|---|
| Name | `accessToken` | `accessToken` |
| HttpOnly | `true` | `true` |
| Secure | `false` | `false` |
| SameSite | `Lax` | `Lax` |
| Path | `/` | `/` |
| Max-Age | `app.jwt.expiration` (24 hours in the supplied properties) | `0` |

The JWT expiry is configured by `app.jwt.expiration` (`24h`). Verification and password-reset tokens use `app.token.expiration` (`30m` in the supplied properties).

**Implementation note:** the login response currently includes the JWT in both the HTTP-only cookie and the JSON response body (`accessToken`). The cookie itself is not readable by browser JavaScript, but the JSON field is. Do not assume the token is cookie-only.

For browser clients, send credentialed requests (for example, `credentials: "include"` in `fetch`). The configured CORS policy allows credentials and one configured origin (`app.cors.allowed-origin`, currently `http://localhost:5713`).

**Security note:** `Secure=false` and disabled CSRF protection are the current source configuration. Revisit both before deploying cookie authentication over HTTPS / cross-site production origins.

### 1.2 Public routes

The security configuration explicitly permits these paths:

| Method | Path | Public |
|---|---|---:|
| `POST` | `/auth/register` | Yes |
| `POST` | `/auth/login` | Yes |
| `POST` | `/auth/verify-email` | Yes |
| `POST` | `/auth/forgot-password` | Yes |
| `GET` | `/ml/test` | Yes |

All other routes require authentication. In particular, `/ml/predict`, all `/scan` routes, `/auth/me`, `/auth/logout`, and `/auth/logout-all` require an authenticated JWT.

### 1.3 Common error responses

The global exception handler explicitly maps the following exceptions:

| Status | Error body |
|---:|---|
| `400 Bad Request` | `{"error":"<validation message>"}` for request DTO validation |
| `400 Bad Request` | `{"error":"INVALID_ARGUMENT","message":"<message>"}` for `IllegalArgumentException` |
| `401 Unauthorized` | `{"error":"<exception message>"}` for `InvalidCredentialsException` |
| `404 Not Found` | `{"error":"RESOURCE_NOT_FOUND","message":"<message>"}` |
| `502 Bad Gateway` | `{"error":"ML_SERVICE_ERROR","message":"The ML service returned an unexpected response","status":"<upstream status/message>"}` |
| `502 Bad Gateway` | `{"error":"IMAGE_STORAGE_ERROR","message":"<storage error>"}` |
| `503 Service Unavailable` | `{"error":"ML_SERVICE_UNAVAILABLE","message":"<message>"}` |

Some upload validation failures are raised as Spring `ResponseStatusException` (including `400` and `413 Payload Too Large`) and are not formatted by the custom handler above. Authentication/authorization failures and other framework errors may also use Spring's default error body.

## 2. Authentication APIs

### 2.1 Register

| Property | Value |
|---|---|
| Method | `POST` |
| Path | `/auth/register` |
| Authentication | Public |
| Content-Type | `application/json` |
| Success | `202 Accepted`, empty body |

**Request body**

| Field | Type | Required | Constraints |
|---|---|---:|---|
| `name` | string | Yes | Not blank; max 100 characters |
| `email` | string | Yes | Not blank; valid email format |
| `password` | string | Yes | Not blank; minimum 8 characters |

```json
{
  "name": "Alex Green",
  "email": "alex@example.com",
  "password": "SecurePass123"
}
```

**Behaviour**
- Rejects an email that already belongs to a user.
- Generates a random verification token; stores its SHA-256 hash, not the raw token.
- Stores pending registration data with the password BCrypt-encoded and role set to `USER`.
- Sends an email containing a frontend verification link at `/verify?token=...`.
- The pending token expires according to `app.token.expiration` (30 minutes in the supplied properties).
- The user account is created only after email verification.

**Success response:** no body. The endpoint does not issue a login cookie; verification does that.

### 2.2 Login

| Property | Value |
|---|---|
| Method | `POST` |
| Path | `/auth/login` |
| Authentication | Public |
| Content-Type | `application/json` |
| Success | `200 OK` |
| Response side effect | Sets `accessToken` cookie |

**Request body**

| Field | Type | Required | Constraints |
|---|---|---:|---|
| `email` | string | Yes | Not blank; valid email format |
| `password` | string | Yes | Not blank |

```json
{
  "email": "alex@example.com",
  "password": "SecurePass123"
}
```

**Success response**

```json
{
  "message": "Login successful",
  "accessToken": "<jwt>"
}
```

The same JWT is set in the `accessToken` cookie. Cookie attributes are documented in [Authentication and cookies](#11-authentication-and-cookies).

**Errors:** unknown email or incorrect password returns `401 Unauthorized` with:

```json
{
  "error": "Invalid email or password"
}
```

### 2.3 Verify email

| Property | Value |
|---|---|
| Method | `POST` |
| Path | `/auth/verify-email` |
| Authentication | Public |
| Parameter | Query parameter `token` (required) |
| Success | `201 Created`, empty body |
| Response side effect | Creates a session and sets `accessToken` cookie |

```http
POST /api/auth/verify-email?token=<raw-verification-token>
```

**Behaviour:** hashes the submitted token, checks that it exists and has not expired, creates the user, deletes the pending token, creates a web-browser session, and issues a JWT. This provides automatic sign-in after successful verification.

**Errors:** missing, unknown, expired, or invalid token returns `401 Unauthorized` with the invalid-credentials error body.

### 2.4 Forgot password

| Property | Value |
|---|---|
| Method | `POST` |
| Path | `/auth/forgot-password` |
| Authentication | Public |
| Parameter | Query parameter `email` (required) |
| Success | `202 Accepted`, empty body |

```http
POST /api/auth/forgot-password?email=alex%40example.com
```

The email is validated as nonblank and as an email address. A random token hash is stored with the configured token expiry, and an email is sent with a frontend `/reset-password?token=...` link.

**Errors:** an unknown email currently returns `401 Unauthorized`. This reveals whether an account exists; consider a generic accepted response if account enumeration is not desired.

### 2.5 Reset password

| Property | Value |
|---|---|
| Method | `POST` |
| Path | `/auth/reset-password` |
| Authentication | Public |
| Content-Type | `application/json` |
| Success | `200 OK`, empty body |

**Request body**

| Field | Type | Required | Constraints |
|---|---|---:|---|
| `token` | string | Yes | Not blank |
| `newPassword` | string | Yes | Not blank; minimum 8 characters |

```json
{
  "token": "<raw-password-reset-token>",
  "newPassword": "NewSecurePass123"
}
```

**Behaviour:** validates the hashed token and expiry, updates the password using BCrypt, deletes the reset token, and revokes all sessions belonging to the user.

**Errors:** validation failure returns `400`; invalid/expired token or failed account update returns `401`.

### 2.6 Current user

| Property | Value |
|---|---|
| Method | `GET` |
| Path | `/auth/me` |
| Authentication | Required |
| Success | `200 OK` |

```http
GET /api/auth/me
```

**Success response**

```json
{
  "userId": 42,
  "role": "USER",
  "sessionId": "<session-id>"
}
```

The response is the `AuthenticatedUserPrincipal` record. `role` is a role enum value (for example, `USER`); `sessionId` identifies the current session.

### 2.7 Logout current session

| Property | Value |
|---|---|
| Method | `POST` |
| Path | `/auth/logout` |
| Authentication | Required |
| Success | `204 No Content` |
| Response side effect | Revokes the current session and clears `accessToken` |

```http
POST /api/auth/logout
```

No request body. The service revokes the session identified by the authenticated principal, then the response expires the cookie (`Max-Age=0`).

### 2.8 Logout all sessions

| Property | Value |
|---|---|
| Method | `POST` |
| Path | `/auth/logout-all` |
| Authentication | Required |
| Success | `204 No Content` |
| Response side effect | Revokes all user sessions and clears `accessToken` |

```http
POST /api/auth/logout-all
```

No request body.

**Not implemented:** resend-verification and session-list / selected-session-revocation routes are present only as comments or TODOs in the source; they are not API endpoints.

## 3. Plant APIs

All plant endpoints require authentication. The owner is derived from the authenticated principal; clients do not provide a user ID.

### 3.1 Create plant

| Property | Value |
|---|---|
| Method | `POST` |
| Path | `/plants` |
| Authentication | Required |
| Content-Type | `application/json` |
| Success | `201 Created`, empty body |

**Request body**

| Field | Type | Required | Constraints |
|---|---|---:|---|
| `nickname` | string | Yes | Not blank; max 20 characters |
| `cropName` | string | Yes | Not null/blank; max 20 characters; must match an existing crop name |

```json
{
  "nickname": "Balcony Tomato",
  "cropName": "Tomato"
}
```

The `cropName` value is looked up in the crop catalog. An unknown crop name raises `IllegalArgumentException` and maps to `400 Bad Request`.

### 3.2 List my plants

| Property | Value |
|---|---|
| Method | `GET` |
| Path | `/plants` |
| Authentication | Required |
| Success | `200 OK` |
| Response | Spring Data `Page<PlantResponse>` |

**Query parameters**

| Name | Type | Required | Default / behaviour |
|---|---|---:|---|
| `page` | integer | No | `0`, zero-based |
| `size` | integer | No | `20`, capped at `100` |
| `sort` | string | No | Controller enforces `id,DESC`; caller sort is ignored |

```http
GET /api/plants?page=0&size=20
```

**PlantResponse**

| Field | Type | Nullable | Description |
|---|---|---:|---|
| `id` | number | No | Plant ID |
| `nickname` | string | No | User-defined nickname |
| `cropType` | string | No | Crop name from the crop record |
| `latestImage` | object | Yes | Latest image metadata, or `null` |

```json
{
  "content": [
    {
      "id": 17,
      "nickname": "Balcony Tomato",
      "cropType": "Tomato",
      "latestImage": {
        "id": 31,
        "imageUrl": "https://<storage-host>/storage/v1/object/public/Leafdoc_Image/...",
        "createdAt": "2026-11-20T08:15:30Z"
      }
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "numberOfElements": 1,
  "first": true,
  "last": true,
  "empty": false
}
```

Spring Data may include additional page metadata fields in its serialized response.

### 3.3 Get plant details

| Property | Value |
|---|---|
| Method | `GET` |
| Path | `/plants/{plantId}` |
| Authentication | Required |
| Path parameter | `plantId` (Long) |
| Success | `200 OK` |

```http
GET /api/plants/17
```

Returns one `PlantResponse`, including its latest image or `null`.

**Errors:** `404` if the plant does not exist or is not owned by the authenticated user.

### 3.4 Rename plant

| Property | Value |
|---|---|
| Method | `PATCH` |
| Path | `/plants/{plantId}` |
| Authentication | Required |
| Path parameter | `plantId` (Long) |
| Content-Type | `application/json` |
| Success | `200 OK` |

The request body is a JSON string, not an object:

```json
"Balcony Cherry Tomato"
```

Returns a `PlantResponse`.

**Implementation note:** the update path constructs the response without fetching the latest image, so `latestImage` is `null` in this response even if the plant has images. A subsequent `GET /plants/{plantId}` returns the latest image.

### 3.5 Delete plant

| Property | Value |
|---|---|
| Method | `DELETE` |
| Path | `/plants/{plantId}` |
| Authentication | Required |
| Success | `204 No Content` |

Deletes all associated image objects from storage, removes image metadata, and then deletes the plant.

**Errors:** `404` if the plant is missing or not owned by the user. A storage failure can interrupt deletion.

## 4. Plant image APIs

All routes require authentication and verify ownership of the parent plant.

### 4.1 Upload plant image

| Property | Value |
|---|---|
| Method | `POST` |
| Path | `/plants/{plantId}/image` |
| Authentication | Required |
| Content-Type | `multipart/form-data` |
| Form field | `file` (required) |
| Success | `201 Created` |

Image validation uses `app.image.max-size` (2 MB in the supplied properties) and `app.image.allowed-types` (JPEG, PNG, WebP). The validator checks the filename extension, declared MIME type, and file signature.

```bash
curl -X POST \
  'http://localhost:8080/api/plants/17/image' \
  -b 'accessToken=<jwt>' \
  -F 'file=@./tomato.jpg'
```

**Success response — `ImageResponse`**

```json
{
  "id": 31,
  "imageUrl": "https://<storage-host>/storage/v1/object/public/Leafdoc_Image/...",
  "createdAt": "2026-11-20T08:15:30Z"
}
```

**Common validation failures:** `400` for missing/empty file, invalid filename, unsupported or mismatched file type, or unreadable image; `413` if the file exceeds the configured maximum. Storage errors map to `502`.

### 4.2 List plant image history

| Property | Value |
|---|---|
| Method | `GET` |
| Path | `/plants/{plantId}/image` |
| Authentication | Required |
| Success | `200 OK` |
| Response | Spring Data `Page<ImageResponse>` |

**Query parameters:** `page` (zero-based, default `0`), `size` (default `20`), and `sort` (repository query orders by `createdAt DESC, id DESC`).

```http
GET /api/plants/17/image?page=0&size=20
```

Each `ImageResponse` contains `id`, `imageUrl`, and `createdAt`.

**Errors:** `404` if the plant is missing or not owned by the authenticated user.

### 4.3 Delete plant image

| Property | Value |
|---|---|
| Method | `DELETE` |
| Path | `/plants/{plantId}/image/{imageId}` |
| Authentication | Required |
| Path parameters | `plantId` and `imageId` (Long) |
| Success | `204 No Content` |

```http
DELETE /api/plants/17/image/31
```

The service checks plant ownership and that the image belongs to that plant, deletes the storage object, then deletes the database row.

**Errors:** `404` if the plant/image is missing or does not belong to the specified plant; `502` for storage errors.

## 5. Machine-learning proxy APIs

### 5.1 Predict from an image

| Property | Value |
|---|---|
| Method | `POST` |
| Path | `/ml/predict` |
| Authentication | Required |
| Content-Type | `multipart/form-data` |
| Form field | `file` (required image) |
| Success | `200 OK` |

```bash
curl -X POST \
  'http://localhost:8080/api/ml/predict' \
  -b 'accessToken=<jwt>' \
  -F 'file=@./tomato.jpg'
```

The backend validates the image and forwards it to the configured ML service at `/ml/predict`.

**Success response — `MlPredictResponse`**

```json
{
  "predictionStatus": "success",
  "prediction": [
    {
      "mlClassLabel": "Tomato___Early_blight",
      "confidenceScore": 0.94
    },
    {
      "mlClassLabel": "Tomato___healthy",
      "confidenceScore": 0.04
    }
  ],
  "modelInfo": {
    "modelName": "plant-disease-classifier",
    "modelVersion": "1.0",
    "inputSize": 224
  }
}
```

The field values above are examples; the actual values are supplied by the ML service. `prediction` is a list of `{ mlClassLabel, confidenceScore }` entries.

**Errors:** image validation errors as described above; `503` if the ML service cannot be reached; `502` for an unexpected ML response. The route is authenticated because only `/ml/test` is permitted by the security configuration.

### 5.2 Check ML service health

| Property | Value |
|---|---|
| Method | `GET` |
| Path | `/ml/test` |
| Authentication | Public |
| Success | `200 OK` |
| Response | JSON object returned by the ML service |

```http
GET /api/ml/test
```

The backend forwards this request to the ML service's `/ml/health` endpoint and returns its JSON object.

**Errors:** `503 Service Unavailable` if the ML service cannot be reached.

## 6. Scan APIs

Scan endpoints combine image upload, ML inference, disease-catalog lookup, image storage, and persistence. All scan endpoints require authentication. Scan records are restricted to the authenticated user.

### 6.1 Create a scan

| Property | Value |
|---|---|
| Method | `POST` |
| Path | `/scan` |
| Authentication | Required |
| Content-Type | `multipart/form-data` |
| Part name | `image` (required image) |
| Success | `201 Created` |

```bash
curl -X POST \
  'http://localhost:8080/api/scan' \
  -b 'accessToken=<jwt>' \
  -F 'image=@./tomato-leaf.jpg'
```

The service:
1. Validates the image and uploads it to image storage.
2. Sends the image to the ML service.
3. Selects the prediction with the highest confidence score.
4. Looks up that prediction's `mlClassLabel` in the disease catalog.
5. Stores the scan and returns its details. If processing fails after upload but before persistence, it attempts to delete the uploaded object.

**Success response — `ScanResponse`**

```json
{
  "cropName": "Tomato",
  "disease": {
    "name": "Early blight",
    "description": "Example disease description.",
    "symptoms": ["Dark spots on leaves"],
    "causes": ["Fungal infection"],
    "treatment": ["Remove affected leaves"],
    "prevention": ["Avoid overhead watering"],
    "careTips": ["Monitor new growth"]
  },
  "scanStatus": "DISEASE_DETECTED",
  "healthy": false,
  "imageUrl": "https://<storage-host>/storage/v1/object/public/Leafdoc_Image/...",
  "createdAt": "2026-11-20T08:15:30Z"
}
```

`disease` and `cropName` are `null` when the predicted class does not match a catalog entry. The `disease` object contains `name`, `description`, `symptoms`, `causes`, `treatment`, `prevention`, and `careTips`.

**Scan status rules in the current implementation**

| Condition | `scanStatus` |
|---|---|
| Predicted class not found in disease catalog | `UNSUPPORTED` |
| Catalog match, confidence score below `0.75` | `UNCERTAIN` |
| Catalog match, confidence at least `0.75`, disease marked healthy | `HEALTHY` |
| Catalog match, confidence at least `0.75`, disease not marked healthy | `DISEASE_DETECTED` |

The `healthy` boolean is derived from the catalog disease record; it is `false` when no disease record is found.

**Errors:** invalid image (`400`), oversized image (`413`), ML service unavailable (`503`), unexpected/missing ML prediction (`502`), storage failure (`502`). A scan is not persisted when inference fails.

### 6.2 List scan history

| Property | Value |
|---|---|
| Method | `GET` |
| Path | `/scan` |
| Authentication | Required |
| Success | `200 OK` |
| Response | Spring Data `Page<ScanHistoryResponse>` |

**Query parameters:** standard Spring `Pageable` parameters. Defaults are `page=0`, `size=10`, sorted by `scannedAt DESC`.

```http
GET /api/scan?page=0&size=10
```

**Each history item**

```json
{
  "id": 51,
  "title": "Early blight",
  "status": "DISEASE_DETECTED",
  "createdAt": "2026-11-20T08:15:30Z"
}
```

`status` is one of `DISEASE_DETECTED`, `HEALTHY`, `UNCERTAIN`, or `UNSUPPORTED`.

### 6.3 Get scan details

| Property | Value |
|---|---|
| Method | `GET` |
| Path | `/scan/{id}` |
| Authentication | Required |
| Path parameter | `id` (Long) |
| Success | `200 OK` |

```http
GET /api/scan/51
```

Returns the same `ScanResponse` shape as scan creation, including the disease information (if catalog-matched), status, health flag, stored image URL, and creation timestamp.

**Errors:** `404` if the scan does not exist or is not owned by the authenticated user.

### 6.4 Delete a scan

| Property | Value |
|---|---|
| Method | `DELETE` |
| Path | `/scan/{id}` |
| Authentication | Required |
| Path parameter | `id` (Long) |
| Success | `204 No Content` |

```http
DELETE /api/scan/51
```

Deletes the stored image object and then the scan record.

**Errors:** `404` if the scan does not exist or is not owned by the authenticated user; storage errors map to `502`.

## 7. Quick endpoint index

| Method | Endpoint | Auth | Purpose |
|---|---|---:|---|
| `POST` | `/auth/register` | No | Begin registration / send verification email |
| `POST` | `/auth/login` | No | Sign in |
| `POST` | `/auth/verify-email` | No | Verify email and sign in |
| `POST` | `/auth/forgot-password` | No | Send password reset email |
| `POST` | `/auth/reset-password` | No | Set a new password |
| `GET` | `/auth/me` | Yes | Get current principal |
| `POST` | `/auth/logout` | Yes | Revoke current session |
| `POST` | `/auth/logout-all` | Yes | Revoke all sessions |
| `POST` | `/plants` | Yes | Create plant |
| `GET` | `/plants` | Yes | List owned plants |
| `GET` | `/plants/{plantId}` | Yes | Get plant details |
| `PATCH` | `/plants/{plantId}` | Yes | Rename plant |
| `DELETE` | `/plants/{plantId}` | Yes | Delete plant |
| `POST` | `/plants/{plantId}/image` | Yes | Upload plant image |
| `GET` | `/plants/{plantId}/image` | Yes | List plant images |
| `DELETE` | `/plants/{plantId}/image/{imageId}` | Yes | Delete plant image |
| `POST` | `/ml/predict` | Yes | Proxy image prediction |
| `GET` | `/ml/test` | No | Check ML service health |
| `POST` | `/scan` | Yes | Create and persist a disease scan |
| `GET` | `/scan` | Yes | List scan history |
| `GET` | `/scan/{id}` | Yes | Get scan details |
| `DELETE` | `/scan/{id}` | Yes | Delete a scan |

## 8. Configuration relevant to the API

Values below are from the supplied `application.properties`; environment variables provide secrets and deployment-specific URLs.

| Property | Supplied value / source |
|---|---|
| `server.port` | `${PORT:8080}` |
| `server.servlet.context-path` | `/api` |
| `app.jwt.expiration` | `24h` |
| `app.token.expiration` | `30m` |
| `app.cors.allowed-origin` | `http://localhost:5713` |
| `app.ml.service.url` | `http://localhost:8000` |
| `app.image.max-size` | `2MB` |
| `app.image.allowed-types` | `image/png,image/jpeg,image/webp` |
| `supabase.bucket` | `Leafdoc_Image` |

Database, mail, JWT secret, ML service key, and Supabase credentials are supplied through environment configuration and are intentionally not reproduced here.
