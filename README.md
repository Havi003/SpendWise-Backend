# SpendWise

SpendWise is a reactive personal-finance backend that helps a user understand and manage everyday spending. It combines manually entered cash transactions with M-Pesa SMS events, keeps account balances up to date, categorizes spending, and provides budgets, savings goals, and spending forecasts for a companion client application.

The API is designed for a frontend running at `http://localhost:3000` and exposes its resources under `/api`.

## What it does

- Creates accounts with email/password credentials or Google sign-in, then issues JWTs for authenticated API calls.
- Creates default **Cash**, **M-Pesa**, and **Bank** wallets for locally registered users and reports individual and total balances.
- Records manual income and expense transactions. Expenses are stored as negative amounts; income is stored as positive amounts.
- Accepts M-Pesa SMS webhooks, extracts the transaction amount, direction, merchant, transaction code, and reported M-Pesa balance, then records the transaction in the user's M-Pesa wallet.
- Applies configurable merchant rules to automatically categorize M-Pesa transactions; users can later override a transaction category.
- Lets users set category budgets and see budget usage and a monthly health state: `good`, `warning`, or `critical`.
- Lets users create savings goals, add funds to them, and marks a goal complete once its target is reached.
- Sends recent transaction history to an optional forecasting service to generate a forecast for the last 3 months, 6 months, or year.

## Architecture

| Area | Implementation |
| --- | --- |
| Application | Java 17 and Spring Boot 3.2 |
| Web/API | Spring WebFlux and Reactor |
| Persistence | Spring Data R2DBC with PostgreSQL |
| Database changes | Flyway migrations and the `spendwise` PostgreSQL schema |
| Authentication | BCrypt passwords, signed JWTs, and Google OAuth 2.0 login |
| Forecasting | HTTP call to a local FastAPI-compatible `/predict` service |

## API overview

Except for registration, login, and the SMS webhook, endpoints require a valid bearer JWT.

| Resource | Endpoint | Purpose |
| --- | --- | --- |
| Authentication | `POST /api/auth/register` | Register a user and create default wallets. |
|  | `POST /api/auth/login` | Authenticate with email/password and receive a JWT. |
|  | `GET /api/auth/me` | Retrieve the authenticated user's profile. |
| Transactions | `GET /api/transactions` | List the current user's transactions. |
|  | `POST /api/transactions/manual` | Create a manual cash income or expense. |
|  | `PUT /api/transactions/{id}/category` | Override a transaction category. |
|  | `GET /api/transactions/balances` | Return Cash, M-Pesa, Bank, and total balances. |
|  | `POST /api/transactions/accounts/{accountId}/starting-balance` | Set the starting balance of a Cash wallet. |
|  | `GET /api/transactions/forecast?range=6months` | Request a forecast (`3months`, `6months`, or `1year`). |
| Budgets | `POST /api/budgets` | Create a category budget. |
|  | `GET /api/budgets` | List active budgets with calculated spending. |
|  | `PUT /api/budgets/{budgetId}` | Update a budget. |
|  | `GET /api/budgets/health` | Get monthly budget health. |
|  | `DELETE /api/budgets/{budgetId}` | Delete a budget. |
| Goals | `POST /api/goals` | Create a savings goal. |
|  | `GET /api/goals` | List the user's goals. |
|  | `PUT /api/goals/{goalId}/add-funds` | Add progress to a goal. |
|  | `DELETE /api/goals/{goalId}` | Delete a goal. |
| Webhook | `POST /api/webhook/sms/{webhookId}` | Ingest an M-Pesa SMS payload. |

Responses use a `WsResponse` envelope containing a status/message header and response data.

## M-Pesa SMS flow

Each user has an `smsWebhookId`, returned during registration and profile retrieval. An SMS relay can submit a payload such as:

```json
{
  "from": "MPESA",
  "message": "QWE123ABCD Confirmed. Ksh1,250.00 paid to Example Store on 1/1/2026. M-PESA balance is Ksh4,500.00."
}
```

to `POST /api/webhook/sms/{smsWebhookId}`. The service determines whether the message represents income or an expense, categorizes the merchant from `application-merchants.yml` when possible, and synchronizes the M-Pesa wallet balance from the SMS when that balance is present.

## Run locally

### Prerequisites

- JDK 17
- PostgreSQL with a database named `smart_spending_coach` (or equivalent connection configuration)
- Maven, or the included Maven Wrapper
- Google OAuth credentials if Google sign-in is needed
- A forecasting service at `http://localhost:8000/predict` only when using the forecast endpoint

### Configuration

Use external configuration for local secrets and connection details. The main settings are:

```properties
spring.r2dbc.url=r2dbc:postgresql://localhost:5432/smart_spending_coach?schema=spendwise
spring.r2dbc.username=postgres
spring.r2dbc.password=change-me
jwt.secret=replace-with-a-random-secret-of-at-least-32-bytes
jwt.expiration=86400000
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
```

Set `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` in your environment before starting the app if using Google OAuth. The configured local CORS origin is `http://localhost:3000`.

Create and evolve the `spendwise` schema before running the application. Database scripts are located in `src/main/resources/schema.sql` and `src/main/resources/db/migration/`.

### Start the API

```bash
./mvnw spring-boot:run
```

The service listens on port `8080` by default. Build a runnable JAR with:

```bash
./mvnw clean package
java -jar target/spendwise-0.0.1-SNAPSHOT.jar
```

## Docker

A multi-stage Dockerfile is included. Build and run it with a PostgreSQL instance and the required configuration supplied at runtime:

```bash
docker build -t spendwise-api -f dockerfile .
docker run --rm -p 8080:8080 \
  -e SPRING_R2DBC_URL='r2dbc:postgresql://host.docker.internal:5432/smart_spending_coach?schema=spendwise' \
  -e SPRING_R2DBC_USERNAME=postgres \
  -e SPRING_R2DBC_PASSWORD='change-me' \
  -e JWT_SECRET='replace-with-a-random-secret-of-at-least-32-bytes' \
  spendwise-api
```

## Customizing categories

Merchant-to-category mappings live in `src/main/resources/application-merchants.yml`. Add or adjust merchant keywords there to tune automatic categorization for incoming M-Pesa messages. A manually updated transaction category takes precedence and is marked as manual.

## Development notes

- The API uses an HS256 JWT secret. Keep it out of source control and rotate it for deployed environments.
- The OAuth success handler currently redirects to `/onboarding` or `/dashboard` on the local frontend, depending on the user's onboarding state.
- Forecasting is an integration point: this repository contains the Java client, not the FastAPI prediction service itself.
