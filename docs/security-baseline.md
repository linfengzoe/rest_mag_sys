# Security Baseline

## Role Matrix

- `admin`
  - Full management access for user, employee, customer, order, category, dish, table, review, statistics.
- `employee`
  - Operation access for order processing, review reply, statistics, category/dish/table management.
- `customer`
  - Self-service access for submit/pay/cancel order, review submit/update, profile endpoints, browsing menu/category/table availability.

## Enforcement Strategy

- API role requirements are declared by `@RequireRoles` on controller class/method.
- JWT claim `role` is parsed in `JwtTokenInterceptor` and normalized to lowercase.
- If route role requirement exists and user role is not in allow-list, interceptor returns:
  - HTTP `403`
  - JSON: `{"code":403,"msg":"无权限访问该资源"}`
- If token is missing/invalid/expired, interceptor returns:
  - HTTP `401`
  - JSON: `{"code":401,"msg":"..."}`

## Auth Header Rules

- Header must be `Authorization: Bearer <token>`.
- CORS preflight `OPTIONS` requests are passed through.

## Password Policy (Current)

- New/updated passwords use `PasswordEncoder` (`BCryptPasswordEncoder`).
- Legacy MD5 hashes are accepted only for compatibility during login and auto-upgraded to BCrypt on successful login.

## Configuration Baseline

- Database and JWT secrets should be provided by environment variables:
  - `DB_USERNAME`
  - `DB_PASSWORD`
  - `JWT_SECRET`
- CORS allow-list is controlled by:
  - `APP_CORS_ALLOWED_ORIGIN_PATTERNS`

## Residual Risks / Next Iterations

- Add endpoint-level ownership checks for customer-visible order/review detail APIs.
- Replace remaining controller `try-catch` blocks with global exception flow.
- Add integration tests for ownership constraints and role hierarchy policies.

