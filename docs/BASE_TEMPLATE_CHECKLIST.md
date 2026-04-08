# Base Template Checklist

## 보안

- JWT 비밀키 필수 검증
- refresh token 해시 저장
- 로그인 실패 잠금
- MFA 확장
- 감사 로그
- 민감정보 로그 마스킹
- 보안 헤더/CORS 프로필 분리

## 운영

- Actuator health/metrics/prometheus
- 관리자 세션 제어 API
- 표준 에러 코드/응답
- Flyway 마이그레이션
- Redis 캐시 prefix 전략

## 외부 연동

- timeout
- retry
- correlation id 전달
- idempotency key 부여

## 파일

- 확장자 allowlist
- content-type allowlist
- 다중 확장자 차단
- 파일명 sanitize

## 개인정보

- 감사 로그 IP 마스킹
- MFA secret 보호
- 보존 주기 설정
