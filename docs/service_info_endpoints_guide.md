# Service Configuration Endpoints Testing Guide

This guide focuses specifically on testing the service configuration endpoints( `/config`, `/terms`, & `/privacy`) that provide dynamic information about the taler-wise service provider. These endpoints were added to support configurable service branding, terms of use, and privacy policy management.

---

## 📋 Table of Contents

1. [Overview](#1---overview)
2. [Prerequisites](#2---prerequisites)
3. [Service Configuration Endpoints](#3---service-configuration-endpoints)
4. [Configuration Management](#4---configuration-management)

---

## 1 - Overview

The service configuration endpoints allows the dynamic configuration of:
- Service provider information (name, logo)
- Terms of Use content and versioning
- Privacy Policy content and versioning

These endpoints support both static configuration via the main `application.yml` file and dynamic content loading from external files.

## 2 - Prerequisites

- Taler-Wise server running (typically on `http://localhost:8086`)
- REST client (cURL, Postman, or similar)
- Basic understanding of HTTP requests and JSON responses

---

## 3 - Service Configuration Endpoints

## 1. Get Service Configuration

**Endpoint:** `GET /v1/config`

**Purpose:** Retrieves the service provider name and logo URL for dynamic branding.

**Required Headers:**

X-Request-ID: UUID


**Example Request:**
```bash
curl -X GET \
  http://localhost:8086/v1/config \
  -H 'X-Request-ID: 123e4567-e89b-12d3-a456-426614174000'
```

Expected Response (200 OK):

```bash
{
  "serviceProviderName": "Taler-Wise Services",
  "logoUrl": "https://adorsys.com/wp-content/uploads/2023/02/adorsys-logo-white-rgb.svg"
}
```

## 2. Get Terms of Use

**Endpoint:** `GET /v1/terms`

**Purpose:** Retrieves the current terms of use with version information and last updated date.

**Required Headers:**

X-Request-ID: UUID


**Example Request:**
```bash
curl -X GET \
  http://localhost:8086/v1/terms \
  -H 'X-Request-ID: 123e4567-e89b-12d3-a456-426614174000'
```

Expected Response (200 OK):

```bash
{
  "content": "Terms of Use content...",
  "version": "1.0"
}
```

## 3. Get Privacy Policy

**Endpoint:** `GET /v1/privacy`

**Purpose:** Retrieves the current privacy policy content and version.

**Required Headers:**

X-Request-ID: UUID

**Example Request:**
```bash
curl -X GET \
  http://localhost:8086/v1/privacy \
  -H 'X-Request-ID: 123e4567-e89b-12d3-a456-426614174000'
```

Expected Response (200 OK):

```bash
{
  "content": "Privacy Policy content...",
  "version": "1.0"
}
```

---

## 4 - Configuration Management

### - Static Configuration via application.yml

Add or modify the following in your `application.yml`:

```bash
service-info:
  service-provider:
    name: "Your Custom Service Name"
    logo-url: "https://your-domain.com/logo.png"
    
  terms:
    content: |
      "Your terms of use content goes here.
      This can be multi-line content with proper formatting."

  privacy:
    content: |
      "Your privacy policy content goes here.
      Include all necessary legal information."
```
### - Dynamic File-based Configuration
For larger content, load from external files:

```bash
service-info:
  terms:
    file-path: "classpath:terms.txt"  # Load from resources

  privacy:
    file-path: "/opt/app/config/privacy.txt"  # Load from file system
```

**File Loading Priority:**
1. If `file-path` is configured and file exists → Load from file
2. If `file-path` is configured but file doesn't exist → Use `content` field
3. If `file-path` is not configured → Use `content` field

---

### Testing Configuration Changes

1. **Modify Configuration:**
   ```bash
   nano taler-wise-server/src/main/resources/application.yml
   ```

2. **Restart Application:**
   ```bash
   cd taler-wise-server
   mvn spring-boot:run
   ```

3. **Test Changes:**
   ```bash
   curl -X GET http://localhost:8086/v1/config \
     -H 'X-Request-ID: 123e4567-e89b-12d3-a456-426614174002'
   ```