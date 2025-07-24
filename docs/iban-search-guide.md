# 🏦 IBAN Search Endpoint Testing Guide

This guide provides step-by-step instructions for end users to test the `/v1/search/bankInfo` endpoint in the **Taler Wise** application. This endpoint allows you to retrieve bank metadata (such as bank name, code, BIC, and UUID) using an IBAN.

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Why IBAN Search?](#why-iban-search)
3. [Results Returned](#results-returned)
4. [Request Structure](#request-structure)
5. [Expected Responses](#expected-responses)
6. [How to Test](#how-to-test)
7. [Example Test Cases](#example-test-cases)
8. [Troubleshooting](#troubleshooting)
9. [Support](#support)

---

## 1. 📝 Overview

- **Endpoint URL:** `/v1/search/bankInfo`
- **Method:** `POST`
- **Content-Type:** `application/json`
- **Required Header:** `X-Request-ID` (a unique UUID for each request)
- **Purpose:** Retrieve bank details by providing an IBAN.

---

## 2. 🔄 Why IBAN Search?

Previously, the application used a `bank-search` endpoint that allowed users to search for banks by name or keyword. This approach returned a list of banks and their profiles, requiring users to filter and select the correct bank profile manually.

With the new IBAN search endpoint, users can directly retrieve the bank information associated with a specific IBAN. This streamlines the process, reduces ambiguity, and improves accuracy, as the IBAN uniquely identifies the bank and its details. The new approach is more user-friendly and eliminates the need for additional filtering or manual selection.

---

## 3. 📦 Results Returned

The `/v1/search/bankInfo` endpoint returns a single object containing the metadata for the bank associated with the provided IBAN. The response includes:

- `bankName`: The full legal name of the bank (e.g., "Commerzbank")
- `bankCode`: The national code used to identify the bank (e.g., "37040044")
- `bic`: The Bank Identifier Code (BIC), also known as the SWIFT code (e.g., "COBADEFFXXX")
- `uuid`: A unique identifier for this bank record in the system (e.g., "6140db51-b125-45b2-ac27-cbefa566574c")

**Example Response:**
```json
{
  "bankName": "Commerzbank",
  "bankCode": "37040044",
  "bic": "COBADEFFXXX",
  "uuid": "6140db51-b125-45b2-ac27-cbefa566574c"
}
```

This is a significant change from the previous `bank-search` endpoint, which returned a list of bank descriptors and profiles. Now, you receive a direct, unambiguous result for the IBAN you provide.

---

## 2. 📨 Request Structure

### Headers
- `X-Request-ID`: Unique identifier for the request (must be a valid UUID)
- `Content-Type`: `application/json`

### Body
- `iban`: The International Bank Account Number to look up

#### Example Request

```http
POST /v1/search/bankInfo HTTP/1.1
Host: <your-server-host>
Content-Type: application/json
X-Request-ID: 123e4567-e89b-12d3-a456-426614174000

{
  "iban": "DE89370400440532013000"
}
```

---

## 3. ✅ Expected Responses

- **200 OK**: Returns bank metadata for the given IBAN.
  ```json
  {
    "bankName": "Commerzbank",
    "bankCode": "37040044",
    "bic": "COBADEFFXXX",
    "uuid": "6140db51-b125-45b2-ac27-cbefa566574c"
  }
  ```
- **400 Bad Request**: Returned if the IBAN is missing, malformed, or invalid.
- **401 Unauthorized**: Returned if required authentication headers are missing (if your deployment requires authentication).
- **404 Not Found**: Returned if no bank information is found for the provided IBAN.

---

## 4. 🧪 How to Test

You can test the endpoint using `curl` or Postman.

### Using `curl`

Replace `<your-server-host>` with your actual server address.

#### Valid IBAN
```bash
curl -X POST http://<your-server-host>/v1/search/bankInfo \
  -H "Content-Type: application/json" \
  -H "X-Request-ID: 123e4567-e89b-12d3-a456-426614174000" \
  -d '{"iban": "DE89370400440532013000"}'
```

#### Invalid IBAN
```bash
curl -X POST http://<your-server-host>/v1/search/bankInfo \
  -H "Content-Type: application/json" \
  -H "X-Request-ID: 123e4567-e89b-12d3-a456-426614174000" \
  -d '{"iban": "foobar"}'
```

#### Missing IBAN
```bash
curl -X POST http://<your-server-host>/v1/search/bankInfo \
  -H "Content-Type: application/json" \
  -H "X-Request-ID: 123e4567-e89b-12d3-a456-426614174000" \
  -d '{}'
```

### Using Postman

1. Set the method to `POST` and the URL to `http://<your-server-host>/v1/search/bankInfo`.
2. In the **Headers** tab, add:
   - `Content-Type: application/json`
   - `X-Request-ID: <some-uuid>`
3. In the **Body** tab, select `raw` and `JSON`, then enter:
   ```json
   {
     "iban": "DE89370400440532013000"
   }
   ```
4. Click **Send** and observe the response.

---

## 5. 🧾 Example Test Cases

| Test Case                | Request Body                        | Expected Status | Notes                        |
|--------------------------|-------------------------------------|-----------------|------------------------------|
| Valid IBAN               | `{ "iban": "DE89370400440532013000" }` | 200             | Returns bank info            |
| Missing IBAN             | `{ }`                               | 400             | Error: No IBAN provided      |
| Malformed IBAN           | `{ "iban": "foobar" }`              | 400             | Error: Invalid IBAN format   |
| Invalid IBAN (checksum)  | `{ "iban": "DE00000000000000000000" }` | 400             | Error: Invalid IBAN digits   |
| Missing X-Request-ID     | `{ "iban": "DE89370400440532013000" }` (no header) | 401 | Error: Missing header        |

---

## 6. 🛠️ Troubleshooting

- Ensure the Taler Wise server is running and accessible at the specified host and port.
- Use a valid UUID for the `X-Request-ID` header (e.g., generated with `uuidgen`).
- The IBAN must be valid and correctly formatted.
- If you receive a `400 Bad Request`, check the IBAN value and request body format.
- If you receive a `401 Unauthorized`, ensure all required headers are present and authentication (if enabled) is configured.
- For `404 Not Found`, verify the IBAN exists in the system.

---

## 7. 🆘 Support

If you encounter issues not covered in this guide:

- Check the application logs for detailed error messages
- Ensure all prerequisites are correctly installed and configured
- Verify network connectivity and port availability
- Contact a member of the adorsys team
