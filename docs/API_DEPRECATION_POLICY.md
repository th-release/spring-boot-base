# API Deprecation Policy

## 기본 원칙

- 모든 API는 `/api/v{n}` 형태의 버전을 사용합니다.
- 폐기 예정 API는 `@DeprecatedApi` 어노테이션으로 표시합니다.
- 응답 헤더에 `Deprecation`, `X-API-Deprecated-Since`, `Sunset`, `Link` 를 함께 내려줍니다.

## 권장 절차

1. 신규 버전 추가
2. 기존 버전에 `@DeprecatedApi` 부여
3. 마이그레이션 기간 제공
4. sunset 이후 제거
