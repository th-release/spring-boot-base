# 🚀 Spring Boot Base Project

본 프로젝트는 Spring Boot 기반의 고성능, 확장 가능한 백엔드 애플리케이션 개발을 위한 **프리미엄 스타터 킷(Base)**입니다. 실제 서비스 운영 시 마주하는 장애 추적성, 동시성 제어, 다국어 대응 및 생산성 도구들이 최신 베스트 프랙티스에 맞춰 완벽하게 구성되어 있습니다.

---

## 🛠 Tech Stack

*   **Framework**: Spring Boot 3.1.5 (Java 17)
*   **Database**: PostgreSQL 16+
*   **ORM/Query**: Spring Data JPA, QueryDSL 5.0.0 (Jakarta)
*   **Security**: Spring Security 6 (Stateless JWT)
*   **Caching & Lock**: Redis (Redisson 3.24.3) / ConcurrentMap (Local)
*   **Storage**: AWS S3 / Local File System
*   **API Docs**: Springdoc-openapi 2.2.0 (Swagger UI)
*   **Monitoring**: Spring Boot Actuator, Micrometer (Prometheus)
*   **Logging**: SLF4J (Logback), P6Spy (SQL Logging)
*   **Build Tool**: Gradle 8.x
*   **Utility**: Apache POI 5.2.3 (Excel), ZXing (QR), MapStruct, Gson

---

## 🚀 Key Features

### 🔐 Security & Identity
*   **JWT with Token Rotation**: `jjwt`를 사용한 인증 시스템으로, Access/Refresh Token 및 Refresh Token Rotation(RTR) 기술이 적용되어 있습니다.
*   **Data Encryption**: `EncryptedStringConverter`를 통해 민감 데이터(개인정보 등)를 DB 레벨에서 AES-256으로 자동 암호화 처리합니다.
*   **Smart IP Extraction**: `IpUtils`를 통해 Proxy, L4/L7, Cloudflare 환경에서도 실제 클라이언트 IP를 정확하게 추출합니다.

### 💾 Data & Performance
*   **Distributed Lock**: `@DistributedLock` 어노테이션과 Redisson을 활용하여 분산 환경에서도 동시성 이슈를 안전하게 제어합니다. (Lock-then-Transaction 전략 적용)
*   **QueryDSL Integration**: 타입 세이프한 동적 쿼리 작성을 지원하며, `QuerydslUtils`를 통해 null-safe한 필터링을 한 줄로 구현합니다.
*   **Smart Caching**: Redis 활성화 여부에 따라 자동으로 **Distributed Cache**와 **Local Cache** 사이를 전환합니다.
*   **Rate Limiting**: `@RateLimit` 어노테이션을 통해 API별 호출 속도를 제한하여 서비스 안정성을 확보합니다.

### 📈 Observability (장애 추적성)
*   **Full-Trace Logging**: 인바운드 요청부터 비동기 처리, 아웃바운드 API 호출까지 하나의 `X-Correlation-ID`로 묶어 완벽한 로그 추적 기능을 제공합니다.
*   **Async MDC Propagation**: `@Async` 비동기 스레드 실행 시에도 로그 추적 ID가 유실되지 않도록 `TaskDecorator`를 통해 컨텍스트를 전파합니다.
*   **Payload Masking**: 로그 기록 시 비밀번호 등 민감한 JSON 필드를 재귀적으로 탐색하여 자동으로 마스킹(`********`) 처리합니다.
*   **Outbound Logging**: `RestService`(Axios-like)를 통한 외부 API 호출 시 모든 요청/응답을 기록하며 추적 ID를 전파합니다.

### 🛠 Productivity & Utilities
*   **Excel Automation**: `@ExcelColumn` 어노테이션 설정만으로 리스트 데이터를 엑셀로 Export하거나 업로드된 엑셀을 Import하는 기능을 제공합니다.
*   **Clean Versioning**: `@ApiVersion(n)` 어노테이션과 패키지 구조를 활용하여 `/api/v1/...` 경로를 자동으로 생성하고 관리합니다.
*   **i18n Support**: `MessageUtils`와 연동된 `GlobalExceptionHandler`를 통해 에러 메시지를 클라이언트 언어에 맞춰 자동 번역하여 반환합니다.
*   **Standardized Responses**: `BasicResponse`, `PageResponse`를 통해 전사적으로 통일된 API 규격을 유지합니다.

---

## 📂 Project Structure

```text
src/main/java/com/threlease/base/
├── common/             # 공통 인프라 스트럭처
│   ├── annotation/     # 커스텀 어노테이션 (DistributedLock, ApiVersion, RateLimit 등)
│   ├── configs/        # 상세 설정 (Async, Cache, QueryDsl, RestTemplate 등)
│   ├── convert/        # JPA Attribute Converters (AES 암호화 등)
│   ├── exception/      # 전역 예외 정의 및 에러 코드
│   ├── handler/        # AOP Aspect, Global Exception Handler, Logging Filter
│   ├── interceptors/   # 비즈니스 인터셉터 (Auth Token)
│   └── utils/          # 유틸리티 (Excel, RestService, Ip, Message, QR, PageHelper)
├── entities/           # JPA 도메인 엔티티
├── functions/          # 도메인별 비즈니스 기능 (Controller, Service, DTO)
│   └── auth/           # 예: 인증 도메인
│       └── v1/         # API 버전별 컨트롤러 위치
└── repositories/       # Data Access Layer (JPA, QueryDSL)
```

---

## ⚙️ Configuration (application.yml)

| 속성 | 설명 | 비고 |
| :--- | :--- | :--- |
| `app.redis.enabled` | Redis & 분산 락 사용 여부 | `false` 시 로컬 캐시 사용 및 락 비활성화 |
| `app.logging.request` | 요청 페이로드 로깅 여부 | 민감 정보 마스킹 포함 |
| `app.logging.sensitive-fields` | 마스킹 처리할 필드 목록 | `password`, `token` 등 |
| `crypto.aes.secret-key` | DB 암호화용 비밀키 | **필수 설정 (Base64)** |
| `storage.local.path` | 로컬 파일 저장 경로 | 기본값 `./uploads` |

---

## 🏃 Getting Started

1.  **Environment**: PostgreSQL 및 Redis(선택) 설치
2.  **Build**: `./gradlew clean build`
3.  **Run**: `./gradlew bootRun`
4.  **API Docs**: `http://localhost:8080/api/swagger`

---

## ⚛️ React Deployment

React 빌드물(`static/`) 서비스 지원:
1.  React 빌드 결과를 `src/main/resources/static/`으로 복사합니다.
2.  **SPA 라우팅**: `/api`로 시작하지 않는 모든 요청은 자동으로 `index.html`로 포워딩되어 React Router가 처리합니다.
3.  **API 호출**: 모든 백엔드 API는 자동으로 `/api/v{n}/` 접두사가 붙어 프론트엔드와 명확히 분리됩니다.
