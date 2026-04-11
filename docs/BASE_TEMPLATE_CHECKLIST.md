# Base Template Checklist

## 보안

- JWT 비밀키 필수 검증
- refresh token 해시 저장
- `auth_login_history` 기반 로그인 실패 잠금
- MFA 확장
- 콘솔 기반 감사/운영 이벤트 로그
- 민감정보 로그 마스킹
- 보안 헤더/CORS 프로필 분리
- 초기 관리자 자동 생성 및 `SYSTEM_ADMIN` 권한 보장

## 운영

- Actuator health/metrics/prometheus
- 관리자 세션 제어 API
- 관리자 권한/권한부여 API
- 표준 에러 코드/응답
- Flyway 마이그레이션
- Redis 캐시 prefix 전략
- `application-env.yml` 환경변수 기반 운영 설정

## 외부 연동

- timeout
- retry
- correlation id 전달
- idempotency key 부여

## 파일

- 서비스별 확장자/content-type 검증 확장 포인트
- 다중 확장자 차단
- 파일명 sanitize
- DB 메타데이터 기반 파일 업로드/삭제/다운로드 URL
- 대용량 파일 스트리밍 응답

## 개인정보

- 감사 로그 IP 마스킹
- MFA secret 보호
- 보존 주기 설정

## Repository

- `@Query` 기반 명시적 JPQL
- default wrapper 조회 메서드 지양
- 관계 컬럼은 FK scalar 대신 entity reference로 조회
- 복잡한 검색은 QueryDSL custom repository 사용
