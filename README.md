# Spring Boot Base Project

이 프로젝트는 Spring Boot 기반의 백엔드 애플리케이션 개발을 위한 스타터 킷(Base)입니다. 보안, DB 연동, API 문서화, 공통 유틸리티 등 서비스 개발에 필수적인 요소들이 미리 설정되어 있습니다.

## 🛠 Tech Stack

*   **Framework**: Spring Boot 3.1.5 (Java 17)
*   **Database**: PostgreSQL
*   **ORM/Query**: Spring Data JPA, QueryDSL 5.0.0
*   **Security**: Spring Security 6 (Stateless JWT 기반)
*   **API Documentation**: Springdoc-openapi (Swagger UI)
*   **Build Tool**: Gradle
*   **Utilities**: Lombok, Gson, JJWT (v0.12.5)

## 🚀 Key Features

*   **Security & JWT Auth**:
    *   `jjwt`를 사용한 토큰 기반 인증 및 권한 관리.
    *   Stateless 세션 정책 적용 및 CORS 설정 완료.
    *   `TokenInterceptor`를 통한 유연한 토큰 검증 및 경로 예외 처리.
*   **Database & Query**:
    *   PostgreSQL 연동 및 HikariCP 최적화 설정.
    *   `QueryDSL` 설정으로 복잡한 동적 쿼리 및 타입 세이프한 쿼리 작성 가능.
    *   `CustomPhysicalNamingStrategy`를 통한 네이밍 컨벤션 자동화.
*   **API Documentation**:
    *   Swagger(Springdoc) 연동 (`/api/swagger`를 통해 접속 가능).
*   **Validation**:
    *   Spring Validation 적용 및 Enum 값 검증을 위한 커스텀 어노테이션 (`EnumValueValidator`) 제공.
*   **Global Exception Handling**:
    *   `BusinessException` 및 `GlobalExceptionHandler`를 통한 일관된 에러 응답 구조.
*   **Production Readiness (Monitoring & Logging)**:
    *   **Spring Boot Actuator**: 애플리케이션 상태 및 메트릭 노출 (`/api/actuator/health`, `/api/actuator/metrics` 등).
    *   **Request/Response Logging**: `LoggingFilter`를 통해 모든 API 요청/응답의 Payload 및 처리 시간 기록.
    *   **MDC Request Tracing**: 각 요청마다 고유한 `requestId`를 부여하여 로그 추적성 확보.
    *   **P6Spy SQL Logging**: JPA가 생성하는 SQL문의 `?` 파라미터를 실제 값으로 치환하여 로깅 (`decorator.datasource.p6spy` 설정).
    *   **Hybrid Caching**: Redis 설정(`spring.data.redis.host`) 여부에 따라 **Redis** 또는 **Local Cache(ConcurrentMap)**를 자동으로 선택하여 사용.
    *   **Storage Abstraction**: AWS S3 설정(`spring.cloud.aws.s3.bucket`) 여부에 따라 **S3** 또는 **Local File System**을 스토리지로 사용하도록 자동 전환.
    *   **Async & Scheduling**: 별도의 쓰레드 풀 설정을 통해 비동기 작업(`@Async`) 및 주기적 작업(`@Scheduled`)의 성능과 안정성 확보.
    *   **Structured Logging**: `logback-spring.xml` 설정을 통한 가독성 높은 로그 포맷 적용.
*   **Common Utilities**:
    *   `BaseEntity`: 생성/수정 시간 자동 추적.
    *   `BasicResponse`, `PageResponse`: 표준화된 API 응답 규격.
    *   `Hash`: SHA-256 기반 암호화 유틸리티.
    *   `GetRandom`: 랜덤 문자열 및 숫자 생성기.

## 📂 Project Structure

```text
src/main/java/com/threlease/base/
├── common/             # 공통 기능 (Config, Exception, Utils, Response)
│   ├── configs/        # QueryDSL, Security, Swagger, WebMvc 등 설정
│   ├── entity/         # BaseEntity 등 공통 엔티티
│   ├── exception/      # 비즈니스 예외 및 에러 코드 정의
│   ├── handler/        # 전역 예외 처리기 (ControllerAdvice)
│   ├── interceptors/   # JWT 인증 인터셉터
│   ├── provider/       # JWT 토큰 생성 및 검증 Provider
│   └── utils/          # 각종 유틸리티 (Encryption, Validation, Response)
├── entites/            # JPA 엔티티 정의 (AuthEntity 등)
├── functions/          # 비즈니스 로직 (Controller, Service, DTO)
│   └── auth/           # 인증 관련 (로그인, 회원가입 등)
└── repositories/       # JPA 및 QueryDSL 리포지토리
```

## ⚙️ Configuration

애플리케이션 설정은 `src/main/resources/application.yml`에서 관리합니다.

*   **Database**: PostgreSQL 환경변수(`DATABASE_HOST`, `DATABASE_PORT` 등)를 통해 동적 설정이 가능합니다.
*   **Context Path**: 모든 API는 `/api` 프리픽스를 가집니다.
*   **Swagger**: `/api/swagger`를 통해 API 문서를 확인할 수 있습니다.

## 🏃 Getting Started

1.  **PostgreSQL 설정**: `application.yml`의 DB 연결 정보를 환경에 맞게 수정합니다.
2.  **의존성 설치**: `./gradlew build`
3.  **애플리케이션 실행**: `./gradlew bootRun`
