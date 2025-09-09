# 🚀 Taler Wise Application Setup Guide

This comprehensive guide walks you through setting up and launching the **Taler Wise** application, which integrates with an **OpenBanking Gateway Server** and uses **PostgreSQL** as its database backend.

---

## 📋 Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Prerequisites](#prerequisites)
3. [Environment Setup](#environment-setup)
4. [Service Configuration](#service-configuration)
5. [Testing & Validation](#testing--validation)

---

## 🏗️ Architecture Overview

The Taler Wise system consists of four main components:

| Component | Port | Purpose |
|-----------|------|---------|
| **PostgreSQL** | 5432 | Database for Gateway Server and Taler Wise |
| **OpenBanking Gateway** | 8085 | External API service for banking operations |
| **Taler Wise Server** | 8086 | Main application server |

---

## 🧱 Prerequisites

### Required Software

Ensure the following are installed on your system:

- [Docker](https://www.docker.com/) & [Docker Compose](https://docs.docker.com/compose/)
- [Git](https://git-scm.com/)
- [Java 21+](https://adoptium.net/)
- PostgreSQL client (optional, for manual DB inspection)

### Installation Verification
Username
After installing prerequisites, verify your setup:

```bash
# Verify installations
java --version        # Should show Java 21+
docker --version     # Should show Docker version
docker-compose --version

# Install Angular CLI if not already installed
npm install -g @angular/cli@19
```

## 🛠️ Environment Setup

### 1. Clone required Repositories

```bash
# Clone OpenBanking Gateway
git clone https://github.com/adorsys/open-banking-gateway.git

# Clone Taler Wise Application
git clone https://github.com/ADORSYS-GIS/taler-wise.git
```

### 2. Start PostgreSQL Database

Navigate to the OpenBanking Gateway directory and start the database:

```bash
cd open-banking-gateway
docker compose up -d postgres
```

Verify Database is Running:
```bash
docker compose ps postgres
# Should show postgres container as "Up"
```
## ⚙️ Service Configuration

### 1. Configure OpenBanking Gateway

In a test situation to achieve what we need to do the OBG project will work with our mock bank, in order to do that you need to modify the ``application.yml`` file that located ``open-banking-gateway/opba-embedded-starter/src/main/resources/application.yml``

### Modify configuration file
Navigate to the configuration file:
```bash
cd opba-embedded-starter/src/main/resources/
```
Open ``application.yml`` and update the sandbox URLs section:

```yaml
# Sandbox URLs - Replace existing values with:
adorsys-sandbox-url: https://xs2a-connector-modelbank.support.sol.adorsys.com/
adorsys-sandbox-oauth-server-url: https://xs2a-online-modelbank.support.sol.adorsys.com/
```

Also update this callback url (we will explain it further in the guide):
```yaml
protocol:
   xs2a:
    urls:
      pis:
        web-hooks:
          ok: http://localhost:8085/v1/callback/{authSessionId}/{aspspRedirectCode}/ok
          nok: http://localhost:8085/v1/callback/{authSessionId}/{aspspRedirectCode}/nok
```
### Start Openbanking Gateway Server

```bash
cd path-to-the-root-of-opba-project # here you are suppose to go to the root directory
mvn clean install -DskipTests
```

```bash
cd opba-embedded-starter
mvn spring-boot:run
```
The server will start on port 8085.

### Health check:

```bash
curl http://localhost:8085/actuator/health
# Should return {"status":"UP"}
```

### 2. Start Taler Wise Server

```bash
cd taler-wise 
mvn clean install -DskipTests
```
```bash
cd taler-wise-server
mvn spring-boot:run
```
The Taler Wise server will start on port 8086.

### Health check
```bash
curl http://localhost:8086/actuator/health
# Should return {"status":"UP"}
```

## 🧪 Testing & Validation

### Payment initiation flow test

### 1. Initiate Payment via API

```bash
curl -i -X POST http://localhost:8086/v1/pis/banks/2d8b3e75-9e3e-4fd2-b79c-063556ad9ecc/accounts/DE38760700240320465700/orchestrated/payments/single   -H "Content-Type: application/json"   -H "X-Request-ID: $(uuidgen)"   -H "Fintech-Redirect-URL-OK: http://localhost:4200/success"   -H "Fintech-Redirect-URL-NOK: http://localhost:4200/error"   -d '{
   "name": "string",
"debitorIban": "DE38760700240320465700",
"amount": 12,
"subject": "Taler withdrawal 4MZT6RS3RVB3B0E2RDMYW0YRA3Y0VPHYV0CYDE6XBB0YMPFXCEG0",
"psuId": "max.musterman"
  }'
```
**Note**: You have to keep the value of the response header **Authorization-Session-ID** it will be helpful to get the status of the payment we will explain it later.

### 2. Retrieve Redirection link

After initiating the payment, check the Taler Wise application logs for the redirection link. Look for a log entry similar to:

```text
 call was accepted, but redirect has to be done for location:https://xs2a-online-modelbank.support.sol.adorsys.com/payment-initiation/login?paymentId=jCnMRCUr8tCsGLpIE8dDa6R6PLXhPIzwTdYDq2QYAwDPE8NSFI0pm2ntVM8Qgx_0cgftJbETkzvNvu5mZQqWcA==_=_psGLvQpt9Q&redirectId=4a71ef14-045f-42a7-b509-c2715fd099eb
```
### 3.  Bank Authentication (Strong Customer Authentication)

#### - Login to mock bank
    - Login: max.musterman
    - Password: 12345

#### - Review Payment Details: Verify the payment information and click "Next"
#### - Select SCA method: Choose "Send Code" for SMS authentication
#### - Enter SCA Code: Use the test code ``123456``
#### - Complete Authentication: Follow the final confirmation steps

### 4. Redirect to the TPP page
#### - After a successful payment
In the previous version, we had something called the "consent-ui" that was actually managing everything related to authorisation. We removed it to stay with our taler-wise and the open banking gateway, keeping things simple for the customer. To do that, we created an intermediate endpoint as a webhook to actually retrieve the aspspRedirectCode and the authSessionId from the aspsp website to be able to continue with our operations at our app level. Don't worry, everything is already implemented. When you click on the button "Back to the TPP page" on the page of the online banking application of the user, it will trigger this endpoint because of this property inside the application.yml that we replaced earlier

```
protocol:
  xs2a:
    urls:
      pis:
        web-hooks:
          ok: http://localhost:8085/v1/callback/{authSessionId}/{aspspRedirectCode}/ok
   ```

It will actually lead you to a blank page, but just to give you an idea, you can create a screen responsible to handle this redirection from the online banking application and make it trigger our endpoint. The only thing that you will need to change is the value of the property above, and put the link of your screen. Example: ``` ok:localhost:4200/redirection-page-from-aspsp```
#### - After an unsuccessful payment
It is basically the same process as the one for the successful; the only thing that is changing is the property that you need to change this time, and our endpoint that you need to put there :
```
protocol:
  xs2a:
    urls:
      pis:
       web-hooks:
         nok: http://localhost:8085/v1/callback/{authSessionId}/{aspspRedirectCode}/nok
``` 

### 5. Get status of a payment

This section documents how to use the getPaymentStatus endpoint to check the current status of a payment. This endpoint is used to retrieve the status of a payment initiated through a separate workflow.

#### Endpoint Details

This endpoint uses a GET request and requires specific parameters to be passed in the URL path. It provides a response with details about the transaction status.

**** URL: GET /v1/{authSessionId}/{payment-product}/{bank-id}/status

**** Method: GET

**** Tags: FintechGetPaymentStatus

**** Operation ID: getPaymentStatus

#### Required Parameters

| Parameter Name | Location | Type |Description
|-----------|------|---------|---------------------|
| **X-Request-ID** | Header | string (UUID) | A unique ID that identifies this request through the common workflow. Example: 99391c7e-ad88-49ec-a2ad-99ddcb1f7721|
| **authSessionId** | Path | string | To simplify things just see it as a way to uniquely identify a payment in this case, a kind of paymentId (you can retrieve it in the response headers of the orchestrated payment initiation request)| 
| **payment-product** | Path | string (Enum) | The type of payment product. Possible values include: sepa-credit-transfers, instant-sepa-credit-transfers, target-2-payments, cross-border-credit-transfers, and their pain.001 equivalents.|
| **bank-id** | Path | string | The unique identifier for the bank. (to retrieve it use the ibanSearch endpoint )|

#### Interpreting the Response

A successful request will return a 200 OK response with a JSON object containing the payment status.

Example Response:
```json
{
    "transactionStatus": "ACSC",
    "psuMessage": "Your payment has been successfully completed."
}
```
The most important field in the response is ``transactionStatus``, which will provide the current status of the payment. This value is a string that indicates the state of the transaction within the bank's system. In our case we are working with ISO 20022 transaction status codes, so the possible value of ``transactionStatus `` can be:

- **ACCC**	(AcceptedSettlementCompleted, your payment has been fully settled and completed.)
- **ACCP**	(AcceptedCustomerProfile, the payment initiation has been accepted based on your profile.)
- **ACSC**	(AcceptedSettlementCompleted, your payment has been successfully completed.)
- **ACSP**	(AcceptedSettlementInProcess, your payment has been accepted and is being processed.)
- **ACTC**	(AcceptedTechnicalValidation, the payment passed technical checks and is being processed.)
- **ACWC**	(AcceptedWithChange, your payment was accepted but with some changes applied.)
- **ACWP**	(AcceptedWithoutPosting, your payment has been accepted but not yet posted to the account.)
- **PDNG**	(Pending, your payment is pending. Please wait for confirmation.)
- **RJCT**	(Rejected, your payment was rejected. Please contact your bank or check details.)
- **RCVD**	(Received, your payment request has been received and is awaiting processing.)
- **CANC**	(Cancelled, your payment request has been cancelled.)
- **PART**	(Partially Accepted, your payment has been partially processed. Some parts were not executed.)

The ``psuMessage`` may provide additional information to be displayed to the end-user.

#### Example cURL Command
You can use the following command structure to test the endpoint. Remember to replace <authSessionId> with the value of the response header **Authorization-Session-ID** from the orchestrated payment initiation request.

```bash
    curl -X GET 'http://localhost:8086/v1/<authSessionId>/instant-sepa-credit-transfers/2d8b3e75-9e3e-4fd2-b79c-063556ad9ecc/status' \
  -H "X-Request-ID: $(uuidgen)"
```

### 6. Retrieve All Single Payments Endpoint

This section documents how to use the retrieveAllSinglePayments endpoint to get a list of all single payments for a specific account. This method provides an overview of past payments initiated from the fintech side.

#### Endpoint Details
This endpoint uses a GET request and requires specific parameters to be passed in the URL path and header.

- **URL**: GET /v1/pis/banks/{bank-id}/accounts/{account-id}/payments/single

- **Method**: GET

- **Summary**: Ask for all payments of this account

- **Tags**: FintechRetrieveAllSinglePayments

- **Operation ID**: retrieveAllSinglePayments

#### Required Parameters

To successfully call this endpoint, you must provide the following parameters and headers.

| Parameter Name | Location | Type |Description
|-----------|------|---------|---------------------|
| **X-Request-ID** | Header | string (UUID) | A unique ID that identifies this request through the common workflow. Example: 99391c7e-ad88-49ec-a2ad-99ddcb1f7721|
| **account-id** | Path | string | The unique identifier for the account of the user.| 
| **bank-id** | Path | string | The unique identifier for the bank. (to retrieve it use the ibanSearch endpoint )|

#### Interpreting the response

A successful request will return a 200 OK response with an array of payments. Each payment object in the array contains detailed information about a single payment.

Example Response:
```json
    [
    {
        "endToEndIdentification": "string",
        "debtorAccount": {
            "iban": "DE38760700240320465700",
            "currency": "EUR"
        },
        "instructedAmount": {
            "currency": "EUR",
            "amount": "0.01"
        },
        "creditorAccount": {
            "iban": "DE89370400440532013000",
            "currency": "EUR"
        },
        "creditorName": "Demo Creditor",
        "remittanceInformationUnstructured": "123",
        "transactionStatus": "ACSC",
        "initiationDate": "2025-09-04"
    }
]
```
The response is a list (type: array) of objects, where each object represents a single payment. The ```transactionStatus``` field provides the status of each payment.

#### Example cURL Command

You can use the following command structure to test the endpoint.

```bash
curl -X GET 'http://localhost:8086/v1/pis/banks/2d8b3e75-9e3e-4fd2-b79c-063556ad9ecc/accounts/DE38760700240320465700/payments/single' \
  -H "X-Request-ID: $(uuidgen)"
```     
## Troubleshooting
Important note: Always check the directory where you are during the process

## 🆘 Support

If you encounter issues not covered in this guide:

Check the application logs for detailed error messages
Ensure all prerequisites are correctly installed and configured
Verify network connectivity and port availability
Contact a member of the Adorsys team
For additional support, please refer to the project repositories or create an issue with detailed error logs and system information.
