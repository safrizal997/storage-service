# Storage Service

A file upload and download service built with Spring Boot, PostgreSQL, and MinIO.

## Tech Stack

- **Spring Boot** - REST API framework
- **PostgreSQL** - File metadata storage
- **MinIO** - S3-compatible object storage for file storage

## Features

- Upload files to MinIO object storage
- Download files by ID
- Store and retrieve file metadata from PostgreSQL
- Streaming upload & download for large files (>500MB)

## Running Locally

```bash
# Start PostgreSQL and MinIO
docker compose up -d

# Run the application
mvn spring-boot:run
```

---

## API Reference

### 1. Upload File

**`POST /api/files/upload`**

Upload a file using `multipart/form-data`.

**Request**

```
Content-Type: multipart/form-data
```

| Field | Type | Description |
|---|---|---|
| `file` | File | File to upload |

**Example (curl)**

```bash
curl -X POST http://localhost:8080/api/files/upload \
  -F "file=@/path/to/document.pdf"
```

**Response `200 OK`**

```json
{
  "id": "a3f1c2d4-7e89-4b12-9c3a-1d2e3f4a5b6c",
  "fileName": "document.pdf",
  "fileSize": 204800,
  "downloadUrl": "/api/files/download/a3f1c2d4-7e89-4b12-9c3a-1d2e3f4a5b6c"
}
```

**Error Responses**

| Status | Description |
|---|---|
| `500 Internal Server Error` | Upload to MinIO failed |
| `413 Payload Too Large` | File exceeds max size (default 100MB) |

```json
{
  "timestamp": "2025-08-01T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Failed to upload file to storage"
}
```

---

### 2. Download File

**`GET /api/files/download/{id}`**

Download a file by its ID. Returns the raw file bytes with appropriate headers.

**Path Variable**

| Param | Type | Description |
|---|---|---|
| `id` | UUID | File ID returned from upload |

**Example (curl)**

```bash
curl -O -J http://localhost:8080/api/files/download/a3f1c2d4-7e89-4b12-9c3a-1d2e3f4a5b6c
```

**Response `200 OK`**

```
Content-Type: application/pdf
Content-Disposition: attachment; filename="document.pdf"

<binary file data>
```

**Error Responses**

| Status | Description |
|---|---|
| `404 Not Found` | File with given ID not found |
| `500 Internal Server Error` | Failed to retrieve file from MinIO |

```json
{
  "timestamp": "2025-08-01T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "File not found with id: a3f1c2d4-7e89-4b12-9c3a-1d2e3f4a5b6c"
}
```

---

### 3. Streaming Upload (Large File >500MB)

**`POST /api/files/stream/upload`**

Upload file besar menggunakan raw binary stream tanpa buffer di memory. Gunakan endpoint ini untuk file >500MB.

**Request Headers**

| Header | Required | Description |
|---|---|---|
| `Content-Type` | Yes | MIME type file, e.g. `application/octet-stream` |
| `Content-Length` | Yes | Ukuran file dalam bytes |
| `X-File-Name` | Yes | Nama file asli, e.g. `large-video.mp4` |

**Example (curl)**

```bash
curl -X POST http://localhost:8080/api/files/stream/upload \
  -H "Content-Type: video/mp4" \
  -H "X-File-Name: large-video.mp4" \
  -H "Content-Length: 2147483648" \
  --data-binary @/path/to/large-video.mp4
```

**Response `200 OK`**

```json
{
  "id": "b7e2d3f5-1a23-4c56-8d9e-2f3a4b5c6d7e",
  "fileName": "large-video.mp4",
  "fileSize": 2147483648,
  "downloadUrl": "/api/files/stream/download/b7e2d3f5-1a23-4c56-8d9e-2f3a4b5c6d7e"
}
```

**Error Responses**

| Status | Description |
|---|---|
| `400 Bad Request` | Header `X-File-Name` atau `Content-Length` tidak ada |
| `500 Internal Server Error` | Upload ke MinIO gagal / koneksi putus |

```json
{
  "timestamp": "2025-08-01T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Header X-File-Name is required"
}
```

---

### 4. Streaming Download (Large File >500MB)

**`GET /api/files/stream/download/{id}`**

Download file besar secara streaming chunk-by-chunk tanpa load seluruh file ke memory.

**Path Variable**

| Param | Type | Description |
|---|---|---|
| `id` | UUID | File ID returned from streaming upload |

**Example (curl)**

```bash
curl -O -J http://localhost:8080/api/files/stream/download/b7e2d3f5-1a23-4c56-8d9e-2f3a4b5c6d7e
```

**Response `200 OK`**

```
Content-Type: video/mp4
Content-Disposition: attachment; filename="large-video.mp4"

<binary stream data>
```

**Error Responses**

| Status | Description |
|---|---|
| `404 Not Found` | File dengan ID tidak ditemukan |
| `500 Internal Server Error` | Gagal stream dari MinIO |

```json
{
  "timestamp": "2025-08-01T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "File not found with id: b7e2d3f5-1a23-4c56-8d9e-2f3a4b5c6d7e"
}
```

---

## API Summary

| Method | Path | Description | Max Size |
|---|---|---|---|
| `POST` | `/api/files/upload` | Upload file (multipart) | 100MB |
| `GET` | `/api/files/download/{id}` | Download file | - |
| `POST` | `/api/files/stream/upload` | Streaming upload file besar | Unlimited |
| `GET` | `/api/files/stream/download/{id}` | Streaming download file besar | - |