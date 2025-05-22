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
| **Consent UI** | 4200 | Frontend for payment authorization flow |
| **Taler Wise Server** | 8086 | Main application server |

---

## 🧱 Prerequisites

### Required Software

Ensure the following are installed on your system:

- [Docker](https://www.docker.com/) & [Docker Compose](https://docs.docker.com/compose/)
- [Git](https://git-scm.com/)
- [Java 21+](https://adoptium.net/)
- [Node.js 18+](https://nodejs.org/)
- [Angular CLI v19](https://angular.io/cli)
- PostgreSQL client (optional, for manual DB inspection)

### Installation Verification
Username
After installing prerequisites, verify your setup:

```bash
# Verify installations
java --version        # Should show Java 21+
node --version        # Should show Node 18+
ng version           # Should show Angular CLI 19
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
docker-compose up -d postgres
```

Verify Database is Running:
```bash
docker-compose ps postgres
# Should show postgres container as "Up"
```
## ⚙️ Service Configuration

### 1. Configure OpenBanking Gateway

In a test situation to achieve what we need to do the OBG project will work with our mock bank, in order to do that you need to modify the ``application.yml`` file that located ``open-banking-gateway/opba-embedded-starter/src/main/resources/application.yml``

### Modify configuration file
Navigate to the configuration file:
```bash
cd open-banking-gateway/opba-embedded-starter/src/main/resources/
```
Open ``application.yml`` and update the sandbox URLs section:

```yaml
# Sandbox URLs - Replace existing values with:
adorsys-sandbox-url: https://xs2a-connector-modelbank.support.sol.adorsys.com/
adorsys-sandbox-oauth-server-url: https://xs2a-online-modelbank.support.sol.adorsys.com/
```

### Start Openbanking Gateway Server

```bash
cd open-banking-gateway
mvn clean install
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

### 2. Setup Consent UI
```bash
cd ../consent-ui
npm i
ng serve --proxy-config=proxy-conf-local-backend.json
```
The Consent UI will start on port 4200.
### Verification
Navigate to http://localhost:4200 in your browser (this UI is used to managed informations after a payment so right now if you want to check if it is up in the browser you will have a blue empty screen and it is normal.)

### 3. Start Taler Wise Server

```bash
cd taler-wise 
mvn clean install
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
curl -X POST http://localhost:8086/v1/pis/banks/2d8b3e75-9e3e-4fd2-b79c-063556ad9ecc/accounts/DE38760700240320465700/payments/single   -H "Content-Type: application/json"   -H "X-Request-ID: $(uuidgen)"   -H "Fintech-Redirect-URL-OK: http://localhost:4200/success"   -H "Fintech-Redirect-URL-NOK: http://localhost:4200/error"   -d '{
    "amount": "10.00",
    "name": "test",
    "debitorIban": "DE38760700240320465700",
    "subject": "Taler test payment"
  }'
```

### 2. Retrieve Authorization link

After initiating the payment, check the Taler Wise application logs for the authorization link. Look for a log entry similar to:

```text
INFO: Authorization URL: :http://localhost:4200/auth/pis/c866f1b0-8d12-4d6f-a455-9d4565cba061/login?redirectCode=bWizsvOiMiA=
```

### 3. Complete Authorization Flow

#### 1. Access Consent UI: Click the authorization link from the logs
#### 2. Create User (if you don't have yet)
#### 3. Enter PSU ID: Use max.musterman (our test user)
#### 4. Confirm Payment: Click "Confirm" to proceed to bank authentication

### 4.  Bank Authentication (Strong Customer Authentication)

#### 1. Login to mock bank
    - Login: max.musterman
    - Password: 12345

#### 2. Review Payment Details: Verify the payment information and click "Next"
#### 3. Select SCA method: Choose "Send Code" for SMS authentication
#### 4. Enter SCA Code: Use the test code ``123456``
#### 5. Complete Authentication: Follow the final confirmation steps
#### 6. Verify the payment: You can login in our online banking as max.musterman with the same credentials and check his last payment using this link ``https://xs2a-online-modelbank.support.sol.adorsys.com/login``

## 🆘 Support

If you encounter issues not covered in this guide:

Check the application logs for detailed error messages
Ensure all prerequisites are correctly installed and configured
Verify network connectivity and port availability
Contact a member of the Adorsys team
For additional support, please refer to the project repositories or create an issue with detailed error logs and system information.