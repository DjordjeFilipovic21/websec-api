## Security fixes (vuln/idor)
Implemented fixes for two vulnerabilities:

1. IDOR / Broken Access Control
- Review endpoints now enforce ownership checks on backend by loading reviews with `(reviewId, userId)`.
- Authenticated users can access/update only their own reviews.
- Requests with mismatched `userId` return `403 Forbidden`.

2. Brute-force / Dictionary attack on login
- Added temporary login lock after multiple failed attempts.
- Default configuration:
  - `websec.auth.max-failed-attempts=3`
  - `websec.auth.lock-duration-seconds=60`
- While locked, login returns `429 Too Many Requests`.
- Successful login resets failed-attempt counters and removes lock.

## Build
- Changed the lombok version to 1.18.24 (latest) to resolve build issues with newer JDK versions.