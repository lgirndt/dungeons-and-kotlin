# Bruno API Testing Setup

This directory contains Bruno API test collections for the Dungeons & Dragons API.

## Development Token Authentication (Recommended)

The collection uses a **development token** for easy testing in the dev environment. This eliminates the need to authenticate before each request.

### How It Works

1. **Environment Configuration** (`environments/dev.json`)
   ```json
   {
     "variables": [
       {
         "name": "baseUrl",
         "value": "http://localhost:8080",
         "enabled": true
       },
       {
         "name": "devToken",
         "value": "this-is-our-dev-token",
         "enabled": true
       }
     ]
   }
   ```

2. **Requests** (e.g., `Hello.bru`)
   - Use `{{devToken}}` directly from environment
   - Example: `auth:bearer { token: {{devToken}} }`

### Benefits

✅ **Run individual requests** - No authentication setup needed
✅ **Works in Bruno CLI** - Each request can be run independently
✅ **Works in Bruno GUI** - Select the dev environment and run any request
✅ **No authentication overhead** - Instant testing without login flow

## Running Tests

### Bruno CLI

```bash
# Run a single request (recommended syntax)
bru run bruno/Hello.bru --env-file bruno/environments/dev.json

# Run the full collection
bru run bruno --env-file bruno/environments/dev.json
```

### Bruno GUI

1. Open Bruno desktop application
2. Load this collection
3. The environment should be auto-detected from `environments/dev.json`
4. Run any request - authentication happens automatically

## Request Files

- `environments/dev.json` - Development environment with dev token (checked in)
- `collection.bru` - Collection-level configuration
- `Hello.bru` - Example authenticated endpoint
- `Login.bru` - Login endpoint (for testing JWT authentication flow)

## Production vs Development

### Development Environment (dev profile)
- Uses hardcoded dev token: `this-is-our-dev-token`
- Dev token is accepted by `DevTokenAuthenticationFilter`
- Authenticates as the `user` account
- **Only available when app runs with dev profile**

### Production Environment (prod profile)
- Dev token authentication is **disabled** (filter not loaded)
- Must use real JWT tokens from `/api/auth/login`
- See `Login.bru` for authentication example

## Test Credentials

In-memory test users (for Login.bru):
- Username: `user`, Password: `password` (Role: USER)
- Username: `admin`, Password: `admin` (Roles: ADMIN, USER)

## Environment File Format

Bruno CLI requires environment files in JSON format with this structure:

```json
{
  "variables": [
    {
      "name": "variableName",
      "value": "variableValue",
      "enabled": true
    }
  ]
}
```

**Note**: The `.bru` format for environment files is not yet fully supported in Bruno CLI 2.14.2. Use JSON format for CLI compatibility.

## References

- [Bruno Documentation](https://docs.usebruno.com)
- [Bruno CLI](https://docs.usebruno.com/bru-cli/overview)
- [Environment Variables](https://docs.usebruno.com/variables/environment-variables)
