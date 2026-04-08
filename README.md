# Spring Boot Base

공통 인증, 보안, 운영 기능을 기본 포함한 Spring Boot base 프로젝트입니다.

## 포함 기능

- JWT access/refresh token 인증
- refresh token rotation 및 세션 관리
- 감사 로그
- 로그인 실패 횟수 제한 및 계정 잠금
- 최근 로그인 시간/IP 기록
- 선택적 MFA(TOTP)
- 프로필 기반 CORS/보안 헤더 분리
- 표준 에러 응답 및 validation 오류 포맷
- Flyway 마이그레이션
- 운영/관리자 API
- 파일 업로드 보안 검증
- Redis/Local 캐시 전략 표준화
- 외부 연동 timeout/retry/idempotency 기본기
- Actuator 기반 운영 헬스체크 확장

## 프로필

- `local`
- `dev`
- `prod`

프로필별 CORS, Swagger, 보안 헤더, Actuator 노출 범위는 `application-*.yml` 에서 관리합니다.

## 주요 설정 키

- `app.auth.login-failure.*`
- `app.auth.mfa.*`
- `app.cache.*`
- `app.outbound.*`
- `app.security.headers.*`
- `app.privacy.*`
- `storage.upload.*`

## 관리자 API

- `GET /api/v1/auth/admin/users`
- `GET /api/v1/auth/admin/users/{uuid}/sessions`
- `POST /api/v1/auth/admin/users/{uuid}/logout-all`
- `POST /api/v1/auth/admin/users/{uuid}/lock`
- `GET /api/v1/auth/admin/audit-logs`

관리자 권한은 `ROLE_ADMIN` 이어야 합니다.
