# Bruno API Testing Setup

This directory contains Bruno API test collections for the Dungeons & Dragons API.

## Authentication Flow

The collection uses JWT Bearer token authentication:

1. **Login.bru** (seq: 0) - Runs first
   - Authenticates with `/api/auth/login`
   - Post-response script saves JWT token to `authToken` variable
   - This variable persists for the duration of the collection run

2. **All other requests** (e.g., Hello.bru)
   - Use Bearer token authentication via `auth:bearer { token: {{authToken}} }`
   - Automatically use the token set by Login.bru

## Running Tests

### Bruno CLI

```bash
# Run the full collection (REQUIRED for authentication)
bru run
```

⚠️ **Individual Request Limitation**: You **cannot** run individual request files with authentication in Bruno CLI due to two issues:
1. Collection-level pre-request scripts [don't execute for single files](https://github.com/usebruno/bruno/issues/3603)
2. Variables don't persist between separate `bru run` invocations

**Solution**: Always use `bru run` to run the full collection.

### Bruno GUI

The GUI doesn't have these limitations:
1. Open Bruno desktop application
2. Load this collection
3. Run individual requests - collection-level pre-request script will execute automatically

## Request Files

- `Login.bru` - Authentication request (seq: 0, runs first)
- `Hello.bru` - Example authenticated endpoint (seq: 1)
- `collection.bru` - Collection-level pre-request script with `bru.sendRequest()` for automatic authentication

## How It Works

The `collection.bru` includes a pre-request script that:
- Skips execution for the `/api/auth/login` endpoint itself
- Checks if `authToken` variable already exists
- If not, makes an HTTP request to `/api/auth/login` using `bru.sendRequest()`
- Stores the resulting JWT token in `authToken` variable

**Note**: This works in Bruno GUI but not in CLI for single files (see limitations above).

## Test Credentials

In-memory test users:
- Username: `user`, Password: `password` (Role: USER)
- Username: `admin`, Password: `admin` (Roles: ADMIN, USER)

Update credentials in `Login.bru` or `collection.bru` to test with different users.

## References

- [Bruno JavaScript API Reference](https://docs.usebruno.com/testing/script/javascript-reference)
- [Script Flow in Bruno](https://docs.usebruno.com/testing/script/script-flow)
- [Known CLI Issue](https://github.com/usebruno/bruno/issues/3603)
