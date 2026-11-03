# Authentication

## Current scope

The first login version uses Google through Auth0. SetupShop is an OAuth2
Resource Server that accepts JWT access tokens. It validates signature,
issuer, audience, and validity times, then exposes the authenticated issuer
and subject through `GET /auth/me`.

There is no frontend yet. Postman and the system browser act as the development
client. Account/Customer linkage, onboarding, local role checks, Customer activation
checks, and Order ownership authorization are not implemented in this slice.
All requests currently require authentication, including Swagger UI and
OpenAPI routes. A valid token alone does not establish ownership of a Customer
or an Order.

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

All four controller test classes define their own configuration:

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

The next business slice will link the validated identity to local Account and
Customer records. A future frontend will need its own client registration and
callback configuration. Role and Order ownership rules remain in
[technical decisions](decisions.md#order-access-policy-planned).
