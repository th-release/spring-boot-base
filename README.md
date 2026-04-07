# 🚀 Spring Boot Base Project

본 프로젝트는 Spring Boot 기반의 고성능, 확장 가능한 백엔드 애플리케이션 개발을 위한 **프리미엄 스타터 킷(Base)**입니다. 보안, 데이터 관리, 장애 추적성, 다국어 대응 및 생산성 도구들이 최신 베스트 프랙티스에 맞춰 완벽하게 구성되어 있습니다.

---

## 🛠 Tech Stack

*   **Framework**: Spring Boot 3.1.5 (Java 17)
*   **Database**: PostgreSQL 16+
*   **ORM/Query**: Spring Data JPA, QueryDSL 5.0.0 (Jakarta)
*   **Security**: Spring Security 6 (Stateless JWT, RTR 적용)
*   **Caching & Lock**: Redis (Redisson 3.24.3) / ConcurrentMap (Local)
*   **Storage**: AWS S3 / Local File System (Hybrid)
*   **API Docs**: Springdoc-openapi 2.2.0 (Swagger UI)
*   **Monitoring**: Spring Boot Actuator, Micrometer (Prometheus)
*   **Logging**: SLF4J (Logback), P6Spy (SQL Logging)
*   **Build Tool**: Gradle 8.x
*   **Key Libs**: Apache POI (Excel), Spring Retry, MapStruct, Gson, ZXing (QR)

---

## 🚀 Key Features

### 🔐 Security & Identity
*   **JWT with Token Rotation**: RTR(Refresh Token Rotation) 기술이 적용된 무상태 인증 시스템.
*   **Data Encryption**: JPA Converter를 통해 민감 데이터를 DB 레벨에서 AES-256 자동 암복호화.
*   **Smart IP Extraction**: `IpUtils`를 통해 다양한 프록시 환경에서도 실제 클라이언트 IP를 정확히 추출.

### 💾 Data & Performance
*   **Distributed Lock**: `@DistributedLock`과 Redisson을 통한 동시성 제어 및 데이터 정합성 보장.
*   **QueryDSL Integration**: `QuerydslUtils`를 활용한 타입 세이프한 동적 쿼리 엔진 고도화.
*   **Hybrid Caching**: Redis 설정에 따라 분산 캐시와 로컬 캐시를 유연하게 자동 전환.
*   **Rate Limiting**: `@RateLimit`과 AOP를 활용한 API별 트래픽 제어.

### 📈 Observability (장애 추적성)
*   **Full-Trace Logging**: 인바운드-비동기-아웃바운드 전 구간 `X-Correlation-ID` 추적.
*   **Async MDC Propagation**: `@Async` 환경에서도 로그 추적 ID를 완벽하게 보존하는 `TaskDecorator`.
*   **Outbound Logging & Retry**: 외부 API 호출 시 모든 요청/응답 로깅 및 자동 재시도 전략 적용.
*   **Payload Masking**: 민감 정보를 재귀적으로 탐색하여 로그에서 자동 마스킹(`********`).

### 🛠 Productivity & Utilities
*   **Excel Automation**: 어노테이션 기반의 초간편 엑셀 Import/Export.
*   **Clean Versioning**: `@ApiVersion(n)` 기반의 자동화된 API 버전 경로 관리.
*   **i18n Error Handling**: 클라이언트 로케일에 맞춘 다국어 에러 메시지 자동 반환.
*   **Enum API Automation**: 서버의 Enum 정의를 프론트엔드용 JSON으로 자동 변환하여 제공.

---

## 📖 Detailed Usage Guide

### 1. 분산 락 (@DistributedLock)
동일한 자원에 대한 동시 접근을 제어합니다. 락 획득 후 **새로운 트랜잭션**이 시작되어 데이터 정합성을 보장합니다.
```java
@DistributedLock(key = "#productId", waitTime = 5, leaseTime = 3)
public void decreaseStock(Long productId, int quantity) {
    // 락 획득 상태에서 안전하게 로직 수행
}
```

### 2. 엑셀 처리 (ExcelUtils)
DTO 설정만으로 엑셀 기능을 구현합니다.
```java
public class UserExcel {
    @ExcelColumn(headerName = "이름", order = 1)
    private String name;
    @ExcelColumn(headerName = "연락처", order = 2)
    private String phone;
}

// Export: ExcelUtils.export(dataList, UserExcel.class);
// Import: ExcelUtils.importExcel(inputStream, UserExcel.class);
```

### 3. HTTP 요청 (RestService)
Axios와 유사한 인터페이스를 제공하며, 자동 로깅 및 Trace ID 전파, Retry 기능이 포함되어 있습니다.
```java
// GET
Map<String, Object> params = Map.of("id", 123);
var res = restService.get("https://api.com/user", params, UserRes.class);

// POST
var res = restService.post("https://api.com/save", body, Result.class);
```

### 4. 동적 쿼리 (QuerydslUtils)
반복되는 null 체크 로직을 제거하여 쿼리 가독성을 높입니다.
```java
.where(
    QuerydslUtils.eqString(user.name, cond.getName()),
    QuerydslUtils.contains(user.email, cond.getEmail()),
    QuerydslUtils.andAll(QuerydslUtils.eqNumber(user.age, 20), ...)
)
```

### 5. API 버전 관리 (@ApiVersion)
URL 상수를 이어 붙일 필요 없이 어노테이션만으로 경로를 자동 생성합니다.
```java
@ApiVersion(1) // -> /api/v1/auth/...
@RequestMapping("/auth")
public class AuthController { ... }
```

### 6. 다국어 예외 처리 (BusinessException)
로케일에 따른 에러 메시지 자동 반환 및 파라미터 치환을 지원합니다.
```java
// messages.properties: USER_NOT_FOUND=User {0} not found.
throw new BusinessException(ErrorCode.USER_NOT_FOUND, "cth");
```

### 7. 유용한 기타 유틸리티
*   **IpUtils**: `IpUtils.getClientIp(request)` - 실제 클라이언트 IP 추출.
*   **ObjectUtils**: 
    *   `copyNonNullProperties(src, target)` - Patch API 구현 시 유용.
    *   `isChanged(old, new)` - 데이터 변경 여부 감지.
*   **PageRequestHelper**: `PageRequestHelper.of(page, size, sort)` - 프론트 파라미터를 Pageable로 안전하게 변환.
*   **EnumValue**: Enum에 구현 시 `/api/common/enums`를 통해 프론트엔드에 코드-명칭 쌍 자동 노출.

---

## 📂 Project Structure

```text
src/main/java/com/threlease/base/
├── common/             # 공통 인프라 스트럭처
│   ├── annotation/     # DistributedLock, ApiVersion, RateLimit 등
│   ├── configs/        # Async, Cache, QueryDsl, RestTemplate, Retry, Storage 등
│   ├── controller/     # Common Enum API, View Controller (SPA 지원)
│   ├── exception/      # BusinessException, ErrorCode
│   ├── handler/        # AOP Aspect, GlobalExceptionHandler, LoggingFilter
│   ├── interceptors/   # Auth Token Interceptor
│   └── utils/          # Excel, RestService, Ip, Message, QR, PageHelper 등
├── entities/           # JPA 도메인 엔티티
├── functions/          # 비즈니스 도메인 (v1, v2 패키지 구조)
└── repositories/       # Data Access Layer
```

---

## ⚙️ Configuration (application.yml)

| 속성 | 설명 | 비고 |
| :--- | :--- | :--- |
| `app.redis.enabled` | Redis & 분산 락 사용 여부 | `false` 시 로컬 캐시 사용 |
| `app.logging.request` | 요청 페이로드 로깅 여부 | 민감 정보 마스킹 포함 |
| `crypto.aes.secret-key` | DB 데이터 암호화용 비밀키 | **필수 설정** |
| `storage.local.path` | 로컬 파일 저장 경로 | 기본값 `./uploads` |

---

## 🏃 Getting Started

### 1. 로컬 인프라 및 앱 동시 실행 (Docker)
제공된 `Dockerfile`은 Multi-stage 빌드와 `jlink`를 통해 이미지 크기를 최소화(약 100MB 내외)하고 보안을 강화한 최적화된 설정을 제공합니다.

```bash
# 전체 시스템(App + DB + Redis + Prometheus) 빌드 및 실행
docker-compose up -d --build
```

### 2. 매뉴얼 빌드 & 실행
인프라만 Docker로 띄우고 앱은 로컬에서 실행할 경우:
```bash
# 인프라만 실행
docker-compose up -d db redis prometheus

# 앱 빌드 및 실행
./gradlew clean build
./gradlew bootRun
```
3.  **API Docs**: `http://localhost:8080/api/swagger`

---

## ⚛️ React Deployment

1.  React 빌드 결과물을 `src/main/resources/static/`으로 복사합니다.
2.  **SPA 라우팅**: `/api`로 시작하지 않는 모든 요청은 자동으로 `index.html`로 포워딩됩니다.
3.  **API 호출**: 모든 API는 `/api/v{n}/` 접두사를 사용하므로 프론트엔드 경로와 충돌하지 않습니다.
