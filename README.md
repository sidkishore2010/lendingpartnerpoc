# Lending Platform Commission Quote POC

This repository contains a complete local proof of concept for a lending platform that collects loan details, calls a backend API, and requests a commission quote from a separate mock vendor service.

## Architecture overview

```text
+------------------+      HTTP JSON       +----------------------+
| React Frontend   | -------------------> | Lending Backend      |
| Vite + React TS  |                      | Spring Boot 3 / Java |
| Port 5173        |                      | Port 8080            |
+------------------+                      +-----------+----------+
                                                       |
                                                       | vendor HTTP + api-key
                                                       v
                                              +----------------------+
                                              | Mock Vendor Service  |
                                              | Spring Boot 3 / Java |
                                              | Port 9090            |
                                              +----------------------+
```

The frontend talks only to the lending backend. The lending backend owns the vendor abstraction and sends the required `api-key` header to the mock vendor. The mock vendor exposes a separate API and simulates failures and latency. See [Architecture decision: Java/Spring Boot on ECS Fargate](docs/architecture-decision.md) for the backend technology choice and deployment rationale.

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 18+
- npm 10+

## Check out the GitHub repository

Clone the repository, then change into the cloned project folder:

```powershell
git clone https://github.com/YOUR_GITHUB_USERNAME/lendingpartnerpoc.git
Set-Location lendingpartnerpoc
```

Run each module from its own folder. Open a separate terminal for each service, and start each terminal at the cloned `lendingpartnerpoc` project root before changing into that module's folder. For example:

```powershell
# Terminal 1: mock vendor
Set-Location .\mock-vendor
```

```powershell
# Terminal 2: lending backend
Set-Location .\lending-backend
```

```powershell
# Terminal 3: frontend
Set-Location .\frontend
```

Then run that module's commands in the corresponding terminal. The relative `cd mock-vendor`, `cd lending-backend`, and `cd frontend` commands in this README assume the current directory is the cloned project root. If a terminal is already in the repository root, it does not need to clone or enter the repository again.

## Project structure

```text
lendingplatformspec.md
README.md
docs/
  architecture-decision.md
.env.example
insomnia/
  commission-quote-poc.insomnia.json
frontend/
  package.json
  src/
  vite.config.ts
  index.html
lending-backend/
  pom.xml
  src/main/java/...
  src/test/java/...
mock-vendor/
  pom.xml
  src/main/java/...
  src/test/java/...
```

## Local environment configuration

The following values are optional overrides; the services have local POC defaults. Environment variables are useful when changing keys, failure rates, URLs, or timeouts.

```bash
# .env example
VENDOR_API_KEY=dev-api-key-123
VENDOR_BASE_URL=http://localhost:9090
VENDOR_CONNECT_TIMEOUT_MS=3000
VENDOR_READ_TIMEOUT_MS=5000
LENDING_API_KEY=dev-lending-api-key-123
COMMISSION_FAILURE_RATE=0.4
VITE_BACKEND_URL=http://localhost:8080
VITE_LENDING_API_KEY=dev-lending-api-key-123
MOCK_FAILURE_RATE=0.2
MOCK_MIN_DELAY_MS=200
MOCK_MAX_DELAY_MS=1000
MOCK_RANDOM_SEED=
```

For Windows PowerShell, use:

```powershell
$env:VENDOR_API_KEY='dev-api-key-123'
$env:VENDOR_BASE_URL='http://localhost:9090'
$env:VENDOR_CONNECT_TIMEOUT_MS='3000'
$env:VENDOR_READ_TIMEOUT_MS='5000'
$env:LENDING_API_KEY='dev-lending-api-key-123'
$env:COMMISSION_FAILURE_RATE='0.4'
$env:MOCK_FAILURE_RATE='0.0'
$env:MOCK_MIN_DELAY_MS='200'
$env:MOCK_MAX_DELAY_MS='1000'
```

The API keys and other values above are local development defaults defined in the services' `application.properties`; environment variables are optional overrides. You can start both Java services without setting either API key. The default lending key is `dev-lending-api-key-123` and the default mock-vendor key is `dev-api-key-123`; the frontend also uses the default lending key if `VITE_LENDING_API_KEY` is not set. The Insomnia Base Environment uses these same defaults.

The backend sends the mock-vendor key on every vendor request. The lending API separately validates the incoming `api-key` header against the lending key. To change either key, set `VENDOR_API_KEY` or `LENDING_API_KEY` before starting the corresponding Java service. When changing the lending key, also set `VITE_LENDING_API_KEY` before starting Vite and update Insomnia's `lending_api_key` environment value. Keep these keys different. The lending key is included in frontend configuration and can be inspected by browser users; these defaults are for local POC use only, not production authentication or confidential secrets.

`COMMISSION_FAILURE_RATE` controls simulated failures at the lending API. Its default is `0.4`: on average, about 4 out of every 10 valid, authenticated requests will fail with HTTP 503. This is a probability per request, not a guaranteed quota; the actual number in any group of 10 can be lower or higher. The mock vendor independently defaults to `MOCK_FAILURE_RATE=0.2` (about 2 out of 10 vendor requests on average). Both values accept `0.0` through `1.0`; `1.0` forces every eligible request to fail and `0.0` disables that service's simulated failures. Since the two defaults are independent, when both are enabled the combined end-to-end failure probability is about 52% (40% lending API failure, otherwise 20% mock vendor failure). Set both to `0.0` for reliable normal development. These values are read at service startup; restart the relevant service after changing them.

### Default local keys and production warning

The development defaults make the services convenient to start locally without setting key variables:

| Service/request path | Default key | Configuration override |
|---|---|---|
| Browser/frontend to lending backend | `dev-lending-api-key-123` | Backend: `LENDING_API_KEY`; frontend: `VITE_LENDING_API_KEY` |
| Lending backend to mock vendor | `dev-api-key-123` | `VENDOR_API_KEY` (must match on backend and mock vendor) |

These public, predictable keys are intentionally for the POC only. The lending key must be delivered to the browser for this demo, so any browser user can inspect it. It does not authenticate a staff member and must not be treated as a production security boundary. Production must remove these defaults and implement real user authentication/authorization plus secure server-side vendor secret injection.

## Run the mock vendor

From the project root:

```bash
cd mock-vendor
mvn package
java -jar target/mock-vendor-0.0.1-SNAPSHOT.jar
```

With environment variables from PowerShell:

```powershell
cd mock-vendor
$env:VENDOR_API_KEY='dev-api-key-123'
$env:MOCK_FAILURE_RATE='0.0'
$env:MOCK_MIN_DELAY_MS='50'
$env:MOCK_MAX_DELAY_MS='150'
mvn package
java -jar target/mock-vendor-0.0.1-SNAPSHOT.jar
```

The mock vendor listens on port 9090. Its default API key is `dev-api-key-123`, so the `VENDOR_API_KEY` assignment can be omitted for the default local setup.

## Run the lending backend

```powershell
cd lending-backend
$env:VENDOR_API_KEY='dev-api-key-123'
$env:LENDING_API_KEY='dev-lending-api-key-123'
$env:VENDOR_BASE_URL='http://localhost:9090'
$env:COMMISSION_FAILURE_RATE='0.0'
mvn package
java -jar target/lending-backend-0.0.1-SNAPSHOT.jar
```

The backend listens on port 8080. Its default lending API key is `dev-lending-api-key-123`; the default failure rate is `0.4`. Omit `LENDING_API_KEY` to use the POC default. In the command above, `COMMISSION_FAILURE_RATE='0.0'` is an explicit override for reliably exercising successful quote generation.

## Run the frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend runs on `http://localhost:5173` by default and uses the POC lending API key when `VITE_LENDING_API_KEY` is unset. In PowerShell, configure the backend URL and key only if you want to override defaults:

```powershell
$env:VITE_BACKEND_URL='http://localhost:8080'
$env:VITE_LENDING_API_KEY='dev-lending-api-key-123'
npm run dev
```

The backend enables CORS for `http://localhost:5173` and `http://127.0.0.1:5173` for local development. Change the CORS policy before hosting the frontend at another origin.

## Example quote flow

1. Open `http://localhost:5173`
2. Enter a valid loan request like `100000` amount, `60` months, risk band `B`
3. Click `Generate Quote`
4. The frontend calls `POST /api/commission-quotes` on the lending backend
5. The backend calls `POST /vendor-api/commission-quotes` on the mock vendor
6. The mock vendor validates the `api-key` header and returns a quote

The lending backend validates the incoming `api-key` header before processing the request. Use the separate lending API key for browser-to-backend calls; never send the vendor key from the frontend.

## API contract and error behavior

### Lending API

`POST http://localhost:8080/api/commission-quotes`

Required headers: `Content-Type: application/json` and `api-key: <LENDING_API_KEY>`.

| Field | Validation |
|---|---|
| `loanAmount` | Greater than 0 and no more than 10,000,000; up to two decimal places |
| `loanTermInMonths` | Integer from 1 to 360 |
| `riskBand` | One of `A`, `B`, `C`, `D` |

These are illustrative POC limits, not lending policy. On success, HTTP 200 returns `quoteId`, `commissionRate` (decimal fraction; `0.015` means 1.5%), and `totalCommission`. The frontend displays the backend response; it does not calculate commission.

Example request:

```json
{
  "loanAmount": 100000.00,
  "loanTermInMonths": 60,
  "riskBand": "B"
}
```

Example successful response:

```json
{
  "quoteId": "QUOTE-<uuid>",
  "commissionRate": 0.015,
  "totalCommission": 1500.00
}
```

The quote ID is generated uniquely by the mock vendor. The example rate and amount use its illustrative B-band calculation only; a real vendor determines real commission terms.

Error responses use `{ "code": "...", "message": "...", "correlationId": "...", "timestamp": "..." }` where applicable. Common mappings:

| Condition | HTTP status | Code |
|---|---:|---|
| Missing/incorrect lending API key | 401 | `API_KEY_INVALID` |
| Invalid input or malformed JSON | 400 | `VALIDATION_ERROR` or `MALFORMED_JSON` |
| Lending API simulated failure | 503 | `COMMISSION_QUOTE_TEMPORARY_FAILURE` |
| Vendor HTTP 400 | 502 | `VENDOR_BAD_REQUEST` |
| Vendor authentication failure or rate limit | 503 | `VENDOR_AUTH_FAILED` or `VENDOR_RATE_LIMITED` |
| Vendor HTTP 500/503 | 502 | `VENDOR_SERVER_ERROR` |
| Malformed/incomplete vendor success response | 502 | `VENDOR_MALFORMED_RESPONSE` or `VENDOR_INVALID_RESPONSE` |
| Vendor API key missing/backend configuration invalid | 500 | `VENDOR_API_KEY_MISSING` or `VENDOR_CONFIGURATION_ERROR` |
| Vendor connection unavailable | 503 | `VENDOR_UNAVAILABLE` |
| Vendor response timeout | 504 | `VENDOR_TIMEOUT` |
| Unexpected backend exception | 500 | `INTERNAL_ERROR` |

The mock vendor directly requires `api-key: <VENDOR_API_KEY>` on `POST /vendor-api/commission-quotes`; it returns 401 for missing or incorrect keys, 400 for invalid request data, and may return a simulated HTTP 500.

Each application request gets a correlation ID. Supply `X-Correlation-Id` to propagate a known ID; otherwise the backend generates one and returns it in the response header. Error JSON also contains the ID.

### Timeouts, retries, and idempotency

The backend defaults to a 3-second vendor connection timeout and a 5-second response/read timeout. Override them with `VENDOR_CONNECT_TIMEOUT_MS` and `VENDOR_READ_TIMEOUT_MS`, then restart the backend. The POC performs **no automatic retries**: retrying quote-creation POSTs could create duplicate quotes if the vendor completed processing but the response was lost. The POC assumes no vendor idempotency support. Add retries only after the real vendor contract defines a supported idempotency mechanism and duplicate-creation behavior is tested. Users can manually retry a failed request.

## Example curl commands

### Backend API

```bash
curl -X POST http://localhost:8080/api/commission-quotes \
  -H "Content-Type: application/json" \
  -H "api-key: dev-lending-api-key-123" \
  -d '{"loanAmount":100000.00,"loanTermInMonths":60,"riskBand":"B"}'
```

### Health checks

```text
GET http://localhost:8080/actuator/health
GET http://localhost:9090/actuator/health
```

Actuator health is exposed on both Java services for local checks and future orchestration health checks.

### Mock vendor API

```bash
curl -X POST http://localhost:9090/vendor-api/commission-quotes \
  -H "Content-Type: application/json" \
  -H "api-key: dev-api-key-123" \
  -d '{"loanAmount":100000.00,"loanTermInMonths":60,"riskBand":"B"}'
```

### API-key rejection

```bash
curl -X POST http://localhost:9090/vendor-api/commission-quotes \
  -H "Content-Type: application/json" \
  -H "api-key: wrong-key" \
  -d '{"loanAmount":100000.00,"loanTermInMonths":60,"riskBand":"B"}'
```

## Insomnia request collection

Import [insomnia/commission-quote-poc.insomnia.json](./insomnia/commission-quote-poc.insomnia.json) into Insomnia using **Import > From File**. Select the `Base Environment` and adjust `backend_url`, `vendor_url`, `vendor_api_key`, or `lending_api_key` if your local configuration differs. The example API keys are for local development only; do not export or share real secrets.

The collection contains health checks and requests for:

- Successful quotes through the lending backend and directly through the mock vendor.
- Lending API missing/incorrect key rejection and simulated failures.
- Backend validation failures for missing fields, zero and excessive loan amounts, invalid terms, and unsupported risk bands.
- Malformed JSON against both APIs.
- Mock vendor missing/incorrect API-key authentication and request validation.
- Deterministic mock HTTP 500, backend vendor timeout, and vendor-unavailable scenarios.

Failure and timeout requests need service configuration changes before you run them. Select the `Base Environment`; its `lending_api_key` and `vendor_api_key` must match the respective server configuration. Normal requests require both failure rates to be `0.0`. The collection's descriptions explain scenario preconditions:

- Lending API forced failure: backend `COMMISSION_FAILURE_RATE=1.0`, expect HTTP 503 with `COMMISSION_QUOTE_TEMPORARY_FAILURE`.
- Vendor error mapping: backend `COMMISSION_FAILURE_RATE=0.0`, mock `MOCK_FAILURE_RATE=1.0`, expect backend HTTP 502 with `VENDOR_SERVER_ERROR`.
- Vendor timeout: backend `COMMISSION_FAILURE_RATE=0.0`, mock `MOCK_FAILURE_RATE=0.0` with a fixed 7000 ms delay, and backend read timeout at its default 5000 ms; expect HTTP 504 with `VENDOR_TIMEOUT`.
- Vendor unavailable: backend `COMMISSION_FAILURE_RATE=0.0` and stop the mock vendor while leaving the backend running; expect HTTP 503 with `VENDOR_UNAVAILABLE`.

Restart a service after changing its environment variables. If `COMMISSION_FAILURE_RATE` is nonzero during vendor testing, the lending API may fail before it makes a vendor call, so set it to `0.0` when testing vendor error mapping, timeout, or availability.

## Failure and latency simulation

The mock vendor supports the following environment variables:

- `MOCK_FAILURE_RATE`: probability between 0.0 and 1.0, default `0.2`
- `MOCK_MIN_DELAY_MS`: minimum artificial delay, default `200`
- `MOCK_MAX_DELAY_MS`: maximum artificial delay, default `1000`
- `MOCK_RANDOM_SEED`: optional seed for deterministic tests

Set `MOCK_FAILURE_RATE=0.0` to disable random failures during normal development work. This describes a probability over many requests, not a guarantee in a small sample.

The lending backend independently supports `COMMISSION_FAILURE_RATE`, a probability from 0.0 to 1.0 (default `0.4`) for simulated HTTP 503 failures on `POST /api/commission-quotes`. That means about 4 requests out of 10 fail on average, not that exactly 4 of every set of 10 will fail. The API key and request validation are checked before this simulation. `1.0` means every valid request with a valid key returns HTTP 503 with code `COMMISSION_QUOTE_TEMPORARY_FAILURE`; the mock vendor is not called for those requests. `0.0` disables lending API failures. The mock vendor independently defaults to `MOCK_FAILURE_RATE=0.2`; when both default rates are active, some vendor calls can also fail and are mapped to HTTP 502. Set both failure rates to `0.0` for normal quote generation.

### Deterministic lending API failure test

Stop the backend, then start it in PowerShell with a failure rate of `1.0`:

```powershell
cd lending-backend
$env:LENDING_API_KEY='dev-lending-api-key-123'
$env:VENDOR_API_KEY='dev-api-key-123'
$env:VENDOR_BASE_URL='http://localhost:9090'
$env:COMMISSION_FAILURE_RATE='1.0'
java -jar target/lending-backend-0.0.1-SNAPSHOT.jar
```

Send a valid request with the lending key:

```powershell
$headers = @{
  'Content-Type' = 'application/json'
  'api-key' = 'dev-lending-api-key-123'
}
$body = '{"loanAmount":100000.00,"loanTermInMonths":60,"riskBand":"B"}'
Invoke-RestMethod -Uri 'http://localhost:8080/api/commission-quotes' -Method Post -Headers $headers -Body $body
```

The call returns HTTP 503. To restore success testing, stop and restart the backend with `$env:COMMISSION_FAILURE_RATE='0.0'`. The configured value is loaded when the backend starts; changing the environment in another PowerShell window or while the process is running does not change its behavior.

### Should the UI expose a failure-rate control?

No. `COMMISSION_FAILURE_RATE` is server-side operational configuration, not a quote input. Letting a browser user control it would allow callers to deliberately make the service unavailable for everyone, and a browser cannot safely hold a privileged server control. The backend therefore does not read a failure-rate value from the quote request or from a frontend variable. Use the environment variable and restart the backend for manual POC testing. If a future team needs a clickable simulator, implement it only as a separate, explicitly development-only tool protected from non-development environments—not as a field on the staff quote form.

For a deterministic HTTP 500 test, stop and restart the mock vendor with:

```powershell
$env:VENDOR_API_KEY='dev-api-key-123'
$env:MOCK_FAILURE_RATE='1.0'
$env:MOCK_MIN_DELAY_MS='0'
$env:MOCK_MAX_DELAY_MS='0'
java -jar target/mock-vendor-0.0.1-SNAPSHOT.jar
```

Every valid authenticated quote request to that instance returns HTTP 500. The lending backend maps this to HTTP 502 with error code `VENDOR_SERVER_ERROR`. Restart the vendor with `MOCK_FAILURE_RATE='0.0'` afterward to restore successful quotes.

For a deterministic timeout test, restart the vendor with zero failure rate and a fixed delay longer than the backend's default 5000 ms read timeout:

```powershell
$env:VENDOR_API_KEY='dev-api-key-123'
$env:MOCK_FAILURE_RATE='0.0'
$env:MOCK_MIN_DELAY_MS='7000'
$env:MOCK_MAX_DELAY_MS='7000'
java -jar target/mock-vendor-0.0.1-SNAPSHOT.jar
```

With the backend still running and `VENDOR_READ_TIMEOUT_MS` at its default, the backend maps the timeout to HTTP 504 with error code `VENDOR_TIMEOUT`. The vendor itself may finish the delayed request after the backend has timed out. Restart the vendor with a shorter delay afterward.

For the unavailable scenario, keep the backend running and stop the mock vendor process. A quote request then returns HTTP 503 with error code `VENDOR_UNAVAILABLE`. Restart the mock vendor when finished.

## Running tests

### Backend tests

```bash
cd lending-backend
mvn test
```

### Mock vendor tests

```bash
cd mock-vendor
mvn test
```

### Frontend tests

```bash
cd frontend
npm install
npm test -- --run
```

### Production build

```bash
cd frontend
npm run build
```

The Maven `package` goal also runs the associated Maven test phase before creating the executable JAR.

## Limitations and assumptions

- This is a POC and not production-ready authentication or authorization.
- The default API keys and simulated failures are local-demo conveniences; do not use the defaults in a deployed environment.
- The mock vendor uses illustrative rates: A=1.0%, B=1.5%, C=2.0%, D=2.5%.
- The backend and mock vendor are intentionally separate services, matching the architecture requirement.
- No database is included because the POC does not require persistence.
- The frontend never calls the mock vendor directly and does not contain the vendor API key. It does contain the POC lending API key to call the backend; that key is inspectable and is not staff authentication.
- The mock vendor is intentionally isolated from production integration logic so switching to a real vendor later only requires configuration and a new client implementation.

## Deployment outline for ECS Fargate

This POC is designed to run locally without containers. For a future ECS Fargate deployment, the recommended pattern is:

1. Package the lending backend as an immutable artifact and run it in an ECS Fargate service.
2. Integrate the real vendor behind `CommissionQuoteClient`; do not deploy the mock vendor as a production dependency.
3. Store real vendor credentials in AWS Secrets Manager or SSM Parameter Store and inject them into the backend task.
4. Expose the lending backend through the approved application ingress; keep vendor connectivity private/outbound where possible.
5. Configure task resources, health checks, environment, logs, metrics, alerts, and an appropriate service scaling policy.
6. Serve the frontend separately as static assets. Replace the POC lending API key with staff authentication and authorization before production.

See [docs/architecture-decision.md](docs/architecture-decision.md) for the Java/Spring Boot and ECS Fargate rationale, alternatives, and operational caveats.

## Notes on security and supportability

- Never commit real secrets.
- Use environment variables or injected secrets for local development.
- Log correlation IDs and keep the error response stable and minimal.
- Do not expose vendor secrets in browser code, frontend environment variables, or logs.
- Authentication and authorization for staff users must be added before production use.
