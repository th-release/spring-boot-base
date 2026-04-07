# Spring Boot Base Project

이 프로젝트는 Spring Boot 기반의 고성능, 확장 가능한 백엔드 애플리케이션 개발을 위한 프리미엄 스타터 킷(Base)입니다. 보안, 데이터 관리, 파일 스토리지, 모니터링 및 생산성 도구들이 최신 베스트 프랙티스에 맞춰 구성되어 있습니다.

## 🛠 Tech Stack

*   **Framework**: Spring Boot 3.1.5 (Java 17)
*   **Database**: PostgreSQL 16+
*   **ORM/Query**: Spring Data JPA, QueryDSL 5.0.0 (Jakarta)
*   **Security**: Spring Security 6 (Stateless JWT)
*   **Caching**: Redis (Optional) / ConcurrentMap (Local)
*   **Storage**: AWS S3 / Local File System
*   **API Docs**: Springdoc-openapi 2.2.0 (Swagger UI)
*   **Monitoring**: Spring Boot Actuator, Micrometer (Prometheus)
*   **Logging**: SLF4J (Logback), P6Spy (SQL Logging)
*   **Build Tool**: Gradle 8.x

## 🚀 Key Features

### 🔐 Security & Identity
*   **JWT with Token Rotation**: `jjwt`를 사용한 인증 시스템으로, Access Token과 Refresh Token을 모두 지원하며 보안 강화를 위해 Refresh Token Rotation(RTR) 기술이 적용되어 있습니다.
*   **Stateless Architecture**: 세션을 사용하지 않는 완전한 무상태성 아키텍처로 설계되어 수평적 확장에 용이합니다.
*   **Custom Interceptor**: `TokenInterceptor`를 통해 유연한 인증 제어가 가능하며, 특정 경로에 대한 예외 처리가 간편합니다.
*   **Data Encryption**: `EncryptedStringConverter`를 통해 민감한 데이터(전화번호, 이메일 등)를 DB 레벨에서 AES-256으로 자동 암호화/복호화합니다.

### 💾 Data & Performance
*   **QueryDSL Integration**: 타입 세이프한 동적 쿼리 작성을 지원하여 복잡한 검색 및 필터링 로직을 안전하게 구현할 수 있습니다.
*   **Smart Caching**: Redis 연결 설정 여부에 따라 시스템이 자동으로 **Distributed Cache(Redis)**와 **Local Cache(ConcurrentMap)** 사이를 전환합니다.
*   **Rate Limiting**: `@RateLimit` 어노테이션과 AOP를 통해 API별 호출 속도 제한을 손쉽게 설정하여 서비스 안정성을 확보합니다.
*   **SQL Monitoring**: P6Spy를 통해 런타임에 생성되는 SQL문을 실시간 모니터링하며, 바인딩 파라미터가 포함된 완전한 쿼리를 로그로 남깁니다.

### 📂 Storage Abstraction
*   **Hybrid Storage Service**: 설정에 따라 **AWS S3**와 **로컬 파일 시스템**을 투명하게 전환하여 사용합니다.
*   **Orphan File Cleanup**: DB에 기록되지 않은 유령 파일을 주기적으로 찾아 삭제하는 `OrphanFileCleanupJob` 배치 프로세스가 내장되어 있습니다.

### 🛠 Productivity & Utilities
*   **QR Code Generator**: ZXing 라이브러리를 기반으로 한 고성능 QR 코드 생성 유틸리티를 제공합니다.
*   **MapStruct**: 엔티티와 DTO 간의 변환 코드를 자동 생성하여 개발자의 실수 방지 및 성능을 극대화합니다.
*   **Standardized Responses**: `BasicResponse`, `PageResponse`를 통해 전사적으로 통일된 API 응답 규격을 유지합니다.
*   **Global Exception Handling**: 비즈니스 예외(`BusinessException`) 중심의 체계적인 에러 핸들링 시스템을 구축하였습니다.

### 📈 Production Readiness
*   **Observability**: Actuator를 통해 애플리케이션의 Health, Metrics 정보를 Prometheus 포맷으로 노출합니다.
*   **Structured Logging**: `LoggingFilter`를 통해 요청/응답 페이로드, 처리 시간, MDC 기반의 `requestId`를 로그에 기록하여 추적성을 보장합니다.

## 📂 Project Structure

```text
src/main/java/com/threlease/base/
├── common/             # 공통 인프라 스트럭처
│   ├── annotation/     # 커스텀 어노테이션 (RateLimit 등)
│   ├── configs/        # 프레임워크 상세 설정 (Security, Async, Cache, Storage 등)
│   ├── convert/        # JPA Attribute Converters (암호화 등)
│   ├── exception/      # 전역 예외 정의 및 에러 코드
│   ├── handler/        # AOP Aspect 및 Global Exception Handler
│   ├── interceptors/   # 비즈니스 인터셉터 (Auth)
│   ├── provider/       # JWT 및 보안 관련 Provider
│   └── utils/          # 유틸리티 컴포넌트 (QR, Storage, Crypto, Response)
├── entities/           # JPA 도메인 엔티티
├── functions/          # 도메인별 비즈니스 기능 (Controller, Service, DTO)
└── repositories/       # Data Access Layer (JPA, QueryDSL)
```

## ⚙️ Configuration (application.yml)

| 속성 | 설명 | 비고 |
| :--- | :--- | :--- |
| `app.redis.enabled` | Redis 사용 여부 제어 | `false` 시 로컬 캐시 사용 |
| `app.token.storage` | 토큰 저장 위치 설정 | `cache` (Redis/Local), `rdb` 선택 가능 |
| `storage.local.path` | 로컬 파일 저장 경로 | 기본값 `./uploads` |
| `storage.cleanup.cron` | 유령 파일 정리 주기 | 기본값 매일 새벽 3시 |
| `crypto.aes.secret` | DB 암호화용 비밀키 | **필수 설정** |

## 🏃 Getting Started

1.  **Environment Setup**:
    *   PostgreSQL 설치 및 데이터베이스 생성
    *   `src/main/resources/application.yml`의 DB 접속 정보 수정
2.  **Build**:
    ```bash
    ./gradlew clean build
    ```
3.  **Run**:
    ```bash
    ./gradlew bootRun
    ```
4.  **API Documentation**:
    *   Swagger UI: `http://localhost:8080/api/swagger`

## ⚛️ React Deployment

React의 빌드 결과물(HTML, CSS, JS)을 Spring Boot에서 서비스하려면 다음 단계를 따르세요:

1.  React 프로젝트에서 빌드 수행: `npm run build`
2.  빌드된 `build/` 폴더 내의 모든 파일을 이 프로젝트의 `src/main/resources/static/` 디렉토리로 복사합니다.
3.  Spring Boot 애플리케이션을 실행하면 `http://localhost:8080/` 에서 React 앱이 로드됩니다.
4.  **라우팅 구조**:
    *   **Backend API**: 모든 `@RestController`는 자동으로 `/api` 접두사가 붙습니다 (예: `GET /api/auth/@me`).
    *   **Frontend View**: `/api`로 시작하지 않는 모든 경로는 `index.html`로 포워딩되어 React Router가 처리합니다.
    *   **Swagger**: `http://localhost:8080/api/swagger`를 통해 접근 가능합니다.
