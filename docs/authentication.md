# Authentication

## Current scope

The first login version uses Google through Auth0. SetupShop is an OAuth2
Resource Server that accepts JWT access tokens. It validates signature,
issuer, audience, and validity times, then exposes the authenticated issuer
and subject through `GET /auth/me`.

There is no frontend yet. Postman and the system browser act as the development
client. Account/Customer linkage, onboarding, local role checks, Customer activation
checks, and Order ownership authorization are not implemented in this slice.
`GET /products` and `GET /products/{id}` are public for anonymous and
authenticated users. Other requests require authentication, including Swagger
UI and OpenAPI routes. A valid token alone does not establish ownership of a Customer
or an Order.

`GET /accounts/me` now resolves the local Account using issuer and subject from
the validated JWT. It returns id, email, role, and customerId for an existing
Account. This is a lookup, not an onboarding or account-creation operation.

## Adopted access and onboarding flow (pending implementation)

Visitors and authenticated users can browse products through the public listing
and detail routes. Comparison requires no login but has no dedicated endpoint.
Purchasing requires Google authentication and completed local onboarding.
Google login creates or recognizes an Auth0 identity, not a local Account.
Onboarding will collect the required business data, save Customer to obtain
its ID, then save a CUSTOMER Account with that ID and the validated issuer and
subject, committing both records in the same database transaction.

Keep Account and Customer separate. A dedicated ADMIN Account represents an
individual staff member's management access, has no Customer link, and cannot
purchase. Personal buying uses a separate CUSTOMER Account; with one role per
Account and Google-only login, this requires a different Google identity.
Public registration must assign CUSTOMER on the server and cannot grant ADMIN.
The controlled administrative provisioning procedure remains to be defined.
Keep a single ADMIN role until concrete needs justify granular permissions.

The ADMIN Customer-link restriction is implemented: the domain rejects a
non-null Customer ID for ADMIN with `IllegalArgumentException`, and V8 enforces
the same rule through `ck_accounts_admin_without_customer`. The user confirmed
an empty local accounts table and successfully applied V8; preserve applied
V6, V7, and V8.

Other access rules still require implementation: Order placement accepts a request-supplied Customer
ID. Role-based endpoint checks, including preventing ADMIN purchases, remain
pending. A database constraint on Account alone does not enforce Order access.
See [the decisions](decisions.md#dedicated-administrative-accounts-planned).

## Local Account lookup

`AccountRepository.findByIssuerAndSubject(String issuer, String subject)`
returns `Optional<Account>`. The persistence adapter delegates to Spring Data
to match both values exactly, preserving their stored representation.

`GetAccountByExternalIdentityUseCase.execute(String issuer, String subject)`
uses that repository contract to return the Account or throw
`AccountNotFoundException` with the message
`Account not found for external identity`. It is a Spring service with a
read-only transaction and accepts plain strings rather than JWT or HTTP types.
AccountController calls it for `GET /accounts/me`; AccountExceptionHandler
maps the exception to 404 using the standard ApiErrorResponse JSON.

When integrated into an application flow, a found Account can supply the
local role and Customer ID. An empty result means there is no local Account
for that identity and onboarding still needs to be handled. The lookup itself
does not create a Customer or Account, link existing records, or grant access.
`GET /auth/me` continues to return issuer and subject directly from the JWT.

## Current Account endpoint

Send `GET http://localhost:8080/accounts/me` with the access token selected in
Postman. An existing Account returns 200 with a body shaped like:

```json
{
  "id": 1,
  "email": "customer@example.com",
  "role": "CUSTOMER",
  "customerId": 10
}
```

ADMIN Accounts have a null customerId. The endpoint takes no request body or
identity parameters; issuer and subject come exclusively from the validated
JWT. It returns no external identity fields in AccountResponse.

Missing authentication returns 401 before the use case is called. A valid
identity without a local Account returns 404 with timestamp, status, error,
message (`Account not found for external identity`), and path (`/accounts/me`).
This is not an authentication failure or a role-based 403. Onboarding handling
remains to be implemented. The endpoint does not create or link records and
does not establish authorization for other business endpoints. These behaviors
are covered by mocked-decoder web tests; no manual real-token check of this
new endpoint has been reported yet.

## Development configuration

| Setting | Value |
| --- | --- |
| Tenant domain | `setupshop-dev.us.auth0.com` |
| Issuer | `https://setupshop-dev.us.auth0.com/` |
| API name | `SetupShop API` |
| API Identifier / audience | `https://api.setupshop.example` |
| Token signing algorithm | `RS256` |
| Test application | `SetupShop Postman Dev` |
| Test application type | Native |
| Login connection | `google-oauth2` |

The audience is a logical API identifier, not a deployed website. Preserve the
issuer's trailing slash. These values and client IDs are public configuration;
client secrets and access tokens are credentials and must not be committed.

## Auth0 and Google setup

1. Register the API in Auth0 with the Identifier and algorithm above.
2. Create the Native test application. Enable the Authorization Code grant and
   use a public client with token endpoint authentication method `None`.
3. Register the exact callback URL displayed by Postman in the application's
   Allowed Callback URLs. With browser authorization enabled, Postman controls
   this field. The verified development setup used
   `https://oauth.pstmn.io/v1/callback`; if Postman displays
   `https://oauth.pstmn.io/v1/browser-callback`, register that exact value too.
4. On the API's Application Access settings, authorize user-delegated access
   for `SetupShop Postman Dev`. This is a user login flow; machine-to-machine
   Client Access is a separate permission and is not needed for this test.
5. In Google Cloud, create a development project and configure Google Auth
   Platform branding, an External audience, and the Google accounts used for
   testing while the app is in testing mode.
6. Create a Google OAuth client of type Web application with:
   - Authorized JavaScript origin: `https://setupshop-dev.us.auth0.com`.
   - Authorized redirect URI:
     `https://setupshop-dev.us.auth0.com/login/callback`.
7. Configure the Google client ID and secret on Auth0's `google-oauth2`
   connection. Enable the connection for the Native test application and
   disable its database/password connection. Use only basic identity/profile
   permissions. Test it with Auth0's Try Connection action.

The Google client secret belongs in the Auth0 Google connection. Postman uses
the Auth0 application's client ID and no client secret. The SetupShop API needs
neither of these client secrets to verify RS256 access tokens.

## Obtain an access token in Postman

Create a GET request for `http://localhost:8080/auth/me`. In Authorization,
select OAuth 2.0, add authorization data to Request Headers, use the `Bearer`
header prefix, and keep Share Token disabled.

Under Configure New Token:

| Field | Value |
| --- | --- |
| Token Name | `SetupShop Google Dev` |
| Grant Type | Authorization Code (With PKCE) |
| Authorize using browser | Enabled |
| Callback URL | The automatically displayed URL, registered in Auth0 |
| Auth URL | `https://setupshop-dev.us.auth0.com/authorize` |
| Access Token URL | `https://setupshop-dev.us.auth0.com/oauth/token` |
| Client ID | The Auth0 client ID of `SetupShop Postman Dev`, not the Google client ID |
| Client Secret | Empty |
| Code Challenge Method | SHA-256 / S256 |
| Code Verifier | Empty for Postman's automatic generation |
| Scope | `openid profile email` |
| State | `{{$guid}}`, resolved to a fresh value for the attempt |
| Client Authentication | Send client credentials in body |

Under Advanced Options > Auth Request, send these parameters in Request URL:

| Key | Value |
| --- | --- |
| `audience` | `https://api.setupshop.example` |
| `connection` | `google-oauth2` |

No extra parameters are needed in Token Request or Refresh Request. Refresh
Token URL may use the same `/oauth/token` endpoint, but this setup does not
request `offline_access` or rely on a refresh token.

Select Get New Access Token, authenticate with Google in the browser, return
to Postman, and select Use Token. Use the access token to call the API, not the
ID token. Generate a fresh token when the previous one expires.

## Run and verify the API

Add these values to the local `.env`, preserving the database configuration:

```dotenv
AUTH0_ISSUER_URI=https://setupshop-dev.us.auth0.com/
AUTH0_AUDIENCE=https://api.setupshop.example
```

From the project root:

```bash
docker compose up -d
set -a
source .env
set +a
./mvnw spring-boot:run
```

Spring Boot does not automatically read this `.env` file. Export the variables
in the same terminal as the Maven command. The application uses issuer discovery
and the issuer's public keys for token verification; real verification requires
connectivity to Auth0 when those keys or metadata are needed.

When running from IntelliJ IDEA, open Run > Edit Configurations and select
SetupShopApplication. Add the same Auth0 variables and the required database
variables under Environment variables (enable the field through Modify options
if hidden). Variables exported in a separate terminal are not automatically
available to an IDE run configuration. Use local values from `.env` for the
database configuration.

In Postman, send GET `http://localhost:8080/auth/me` with the selected access
token. The manually verified result is 200 with a body shaped like:

```json
{
  "issuer": "https://setupshop-dev.us.auth0.com/",
  "subject": "google-oauth2|example-user"
}
```

Switch the request to No Auth and remove any manually added Authorization
header. The manually verified result is 401. Authentication failures currently
use Spring Security's response rather than the controller JSON error DTO.

## Troubleshooting

| Symptom | Check |
| --- | --- |
| Missing `AUTH0_ISSUER_URI` placeholder when running the application | Export `.env` before starting Maven, or configure environment variables in the IntelliJ application run configuration |
| Missing Auth0 placeholders when running controller tests | Keep the class's `@TestPropertySource` issuer and audience values; tests should not depend on the local `.env` |
| `Unknown client` mentioning `.apps.googleusercontent.com` | Replace the Postman Client ID with the Auth0 application Client ID |
| Client not authorized to access the resource server | Authorize user-delegated access for the Native client on SetupShop API |
| Callback mismatch | Register exactly the callback generated by Postman |
| 401 from `/auth/me` | Use a fresh access token, select Use Token, and check issuer, audience, and the response's WWW-Authenticate header |

Do not paste tokens or client secrets into logs, issues, or documentation.
For failures, share the status and error description instead.

## Tests and remaining work

AuthenticationControllerTest uses MockMvc with the real security filter chain
and a mocked JwtDecoder. Its three tests cover missing tokens, decoder
rejection, and the authenticated identity response. They do not independently
validate real signatures or incorrect issuer, audience, or time claims.

The controller and security test classes define their own configuration:

```java
@TestPropertySource(properties = {
    "AUTH0_ISSUER_URI=https://issuer.example/",
    "AUTH0_AUDIENCE=https://api.example"
})
```

These fictitious values allow Spring configuration to initialize without
loading `.env`. AuthenticationControllerTest still controls decoder results
through its mock. Application runtime settings remain environment-specific.

With Docker running, execute the suite independently of local Auth0 variables:

```bash
env -u AUTH0_ISSUER_URI -u AUTH0_AUDIENCE ./mvnw test
```

The user reported 191 tests passing with zero failures, errors, or skipped
tests using this setup. Removing the variables applies only to that command
and does not change `.env` or the shell's environment.
Product, Customer, and Order controller tests disable MockMvc filters for
their HTTP contract scope. Separate authorization coverage is still needed
when their access policies are implemented. The current CI workflow runs
`./mvnw test` without defining Auth0 environment variables; the suite supplies
the configuration it needs. A GitHub Actions run has not been verified in
this session.

The Account lookup slice adds three PostgreSQL integration tests: a matching
pair returns the saved Account, the same issuer with a different subject
returns empty, and the same subject with a different issuer returns empty.
The user reported a successful full suite with 194 tests after this addition.
The 191-test result above records the earlier run without Auth0 variables.

The use case adds two unit tests with a mocked AccountRepository: returning the
found Account and throwing AccountNotFoundException when the repository returns
empty. They also verify the issuer and subject passed to the repository; the
absence test checks the exception message. The user reported 196 passing tests
with zero failures, errors, or skipped tests after this addition.

The ADMIN Customer-link restriction adds two domain tests and two persistence
tests. They verify rejection with a Customer ID and valid creation/storage
without one. The database rejection test inserts through JdbcTemplate so that
it tests the constraint independently of the constructor. The full-suite
result reported at that stage was 200 tests passing with zero failures, errors,
or skipped tests. V8 was also successfully applied to the local database.

The public catalog slice adds ProductSecurityTest with the real SecurityConfig
and active filters, a mocked JwtDecoder, and mocked Product use cases. Three
tests verify unauthenticated listing and detail return 200 and unauthenticated
creation returns 401 without calling its use case. The creation test supplies
a valid JSON body and a CSRF token to isolate authentication; CSRF behavior in
production is unchanged. The full-suite result reported at that stage was
203 passing tests with zero failures, errors, or skipped tests. No ADMIN role
check for product writes is established by this slice.

AccountControllerTest adds three tests for the current Account response (200),
missing authentication (401 without a use case call), and authenticated identity
without a local Account (404 with the standard error body). They use active
filters, the real SecurityConfig, a mocked decoder and use case, and test-owned
issuer/audience configuration. The latest full-suite result reported by the
user is 206 passing tests with zero failures, errors, or skipped tests.

The next business work will define onboarding, creation/linking of local
records, and backend role/ownership enforcement. A future frontend will need its own client registration and
callback configuration. Role and Order ownership rules remain in
[technical decisions](decisions.md#order-access-policy-planned).
