# Spring Boot Base Project

이 프로젝트는 Spring Boot 기반의 고성능, 확장 가능한 백엔드 애플리케이션 개발을 위한 **프리미엄 스타터 킷(Base)**입니다. 보안, 데이터 관리, 파일 스토리지, 장애 추적성 및 생산성 도구들이 최신 베스트 프랙티스에 맞춰 완벽하게 구성되어 있습니다.

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
*   **Key Libs**: Apache POI, Spring Retry, MapStruct, Gson, ZXing, Commons Text

---

## 🚀 Key Features

### 🔐 Security & Identity
*   **JWT with Token Rotation**: RTR(Refresh Token Rotation) 기술이 적용된 인증 시스템으로 보안성을 극대화했습니다.
*   **Global XSS Prevention**: Jackson 및 Servlet Filter를 통해 JSON과 파라미터 전역에 대한 XSS 방지 처리를 자동 수행합니다.
*   **Data Encryption**: `EncryptedStringConverter`를 통해 민감 데이터를 DB 레벨에서 AES-256으로 자동 암복호화합니다.
*   **Smart IP Extraction**: `IpUtils`를 통해 Proxy, L4/L7, Cloudflare 환경에서도 실제 클라이언트 IP를 정확히 추출합니다.

### 💾 Data & Performance
*   **Distributed Lock**: `@DistributedLock`과 Redisson을 활용하여 분산 환경에서도 동시성 이슈를 안전하게 제어합니다.
*   **QueryDSL Integration**: `QuerydslUtils`를 활용하여 타입 세이프하고 직관적인 동적 쿼리 엔진을 구축했습니다.
*   **Hybrid Caching**: Redis 설정 여부에 따라 자동으로 Distributed Cache와 Local Cache 사이를 전환합니다.
*   **Rate Limiting**: `@RateLimit` 어노테이션과 AOP를 통해 API별 호출 속도를 손쉽게 제어합니다.

### 📂 Storage Abstraction
*   **Hybrid Storage Service**: 설정에 따라 AWS S3와 로컬 파일 시스템을 투명하게 전환하여 사용합니다.
*   **Orphan File Cleanup**: DB에 기록되지 않은 유령 파일을 주기적으로 삭제하는 배치 프로세스가 내장되어 있습니다.

### 📈 Production Readiness (Observability)
*   **Full-Trace Logging**: 인바운드-비동기-아웃바운드 전 구간을 관통하는 `X-Correlation-ID` 추적 시스템을 제공합니다.
*   **Async MDC Propagation**: `@Async` 실행 시에도 로그 추적 ID를 완벽하게 보존하는 `TaskDecorator`를 적용했습니다.
*   **Outbound Logging & Retry**: 외부 API 호출 시 모든 과정을 로깅하며, 일시적 장애 시 Spring Retry로 자동 복구합니다.
*   **Payload Masking**: 로그 기록 시 비밀번호 등 민감한 JSON 필드를 재귀적으로 탐색하여 자동 마스킹 처리합니다.

---

## 📖 Detailed Usage Guide

### 1. 분산 락 (@DistributedLock)
서버가 여러 대인 환경에서도 특정 키를 기준으로 동시성을 제어합니다.
```java
@DistributedLock(key = "#productId", waitTime = 5, leaseTime = 3)
public void decreaseStock(Long productId, int quantity) {
    // 락 획득 후 새로운 트랜잭션이 시작되어 데이터 정합성 보장
}
```

### 2. 엑셀 자동화 (ExcelUtils)
DTO에 `@ExcelColumn` 설정만으로 엑셀 기능을 구현합니다.
```java
public class UserExcel {
    @ExcelColumn(headerName = "이름", order = 1) private String name;
    @ExcelColumn(headerName = "연락처", order = 2) private String phone;
}
// Export: Workbook wb = ExcelUtils.export(dataList, UserExcel.class);
// Import: List<UserExcel> list = ExcelUtils.importExcel(inputStream, UserExcel.class);
```

### 3. HTTP 요청 (RestService)
자동 로깅, Trace ID 전파, 자동 Retry 기능이 포함된 간결한 HTTP 클라이언트입니다.
```java
// GET
var res = restService.get("https://api.com/user", Map.of("id", 1), UserRes.class);
// POST (자동 Retry 3회 적용)
var res = restService.post("https://api.com/save", body, Result.class);
```

### 4. 동적 쿼리 (QuerydslUtils)
반복되는 null 체크 로직을 제거하여 가독성 높은 쿼리를 작성합니다.
```java
.where(
    QuerydslUtils.eqString(user.name, cond.getName()),
    QuerydslUtils.contains(user.email, cond.getEmail()),
    QuerydslUtils.in(user.role, cond.getRoles()),
    QuerydslUtils.andAll(QuerydslUtils.eqNumber(user.age, 20), ...)
)
```

### 5. API 버전 관리 (@ApiVersion)
URL 상수를 이어 붙일 필요 없이 어노테이션만으로 경로를 자동 생성합니다.
```java
@ApiVersion(1) // -> /api/v1/auth/... 자동으로 접두사 부여
@RequestMapping("/auth")
public class AuthController { ... }
```

### 6. 다국어 예외 처리 (BusinessException)
로케일에 따른 에러 메시지 자동 반환 및 `{0}` 형태의 파라미터 치환을 지원합니다.
```java
// messages.properties: USER_NOT_FOUND=User {0} not found.
throw new BusinessException(ErrorCode.USER_NOT_FOUND, "admin");
```

### 7. 클라이언트 IP 추출 (IpUtils)
L4, Nginx, Cloudflare 등 복잡한 네트워크 환경에서 실제 IP를 추출합니다.
```java
String clientIp = IpUtils.getClientIp(request);
if (IpUtils.isInternalIp(clientIp)) { ... } // 사설 IP 여부 판별
```

### 8. 객체 병합 및 변경 감지 (ObjectUtils)
Patch API 구현 및 데이터 변경 이력 기록 시 사용합니다.
```java
ObjectUtils.copyNonNullProperties(dto, entity); // Null이 아닌 필드만 덮어쓰기
boolean isChanged = ObjectUtils.isChanged(oldObj, newObj); // 변경 여부 확인
```

### 9. 페이지네이션 헬퍼 (PageRequestHelper)
프론트엔드 파라미터를 Pageable로 안전하게 변환(기본값 및 최대치 제한 포함)합니다.
```java
Pageable pageable = PageRequestHelper.of(page, size, Sort.by("id").descending());
```

### 10. 공통 코드 API (EnumValue)
Enum에 구현 시 `/api/common/enums`를 통해 프론트엔드에 코드-명칭 쌍을 자동 노출합니다.
```java
public enum Roles implements EnumValue {
    ROLE_USER("일반 사용자");
    // getCode(), getName() 오버라이드
}
```

### 11. 트래픽 제어 (@RateLimit)
API별 호출 속도를 제어하여 무분별한 요청을 방어합니다.
```java
@RateLimit(limit = 10, window = 60) // 60초당 10회 제한
public ResponseEntity<?> limitedApi() { ... }
```

### 12. QR 코드 생성 (QRCode)
ZXing 기반의 고성능 QR 코드 생성 기능을 제공합니다.
```java
byte[] qrImage = QRCode.generate("https://google.com", 300, 300);
```

---

## 📂 Project Structure

```text
src/main/java/com/threlease/base/
├── common/             # 공통 인프라 스트럭처
│   ├── annotation/     # 커스텀 어노테이션 (Lock, ApiVersion, RateLimit 등)
│   ├── configs/        # 상세 설정 (Async, Cache, QueryDsl, Jackson, Retry, Docker 등)
│   ├── controller/     # 공통 컨트롤러 (Common Enum API, View Controller)
│   ├── exception/      # BusinessException, ErrorCode
│   ├── handler/        # AOP Aspect, GlobalExceptionHandler, LoggingFilter, XssFilter
│   ├── interceptors/   # 비즈니스 인터셉터 (Auth Token)
│   └── utils/          # 유틸리티 (Excel, RestService, Ip, Message, QR, PageHelper, Object, Xss)
├── entities/           # JPA 도메인 엔티티
├── functions/          # 도메인별 비즈니스 기능 (v1, v2 패키지 구조)
└── repositories/       # Data Access Layer (JPA, QueryDSL)
```

## ⚙️ Configuration (application.yml)

| 속성 | 설명 | 비고 |
| :--- | :--- | :--- |
| `app.redis.enabled` | Redis & 분산 락 사용 여부 | `false` 시 로컬 캐시 사용 |
| `app.logging.request` | 요청 페이로드 로깅 여부 | 민감 정보 마스킹 포함 |
| `crypto.aes.secret-key` | DB 데이터 암호화용 비밀키 | **필수 설정 (Base64)** |
| `storage.local.path` | 로컬 파일 저장 경로 | 기본값 `./uploads` |

## 🏃 Getting Started

### 1. 로컬 인프라 및 앱 동시 실행 (Docker)
```bash
# PostgreSQL, Redis, Prometheus 동시 실행
docker-compose up -d --build
```

### 2. 매뉴얼 실행
```bash
# 앱 빌드 및 실행
./gradlew clean build
./gradlew bootRun
```

*   **API Documentation**: `http://localhost:8080/api/swagger`

## ⚛️ React Deployment

1.  React 빌드 결과물(HTML, CSS, JS)을 `src/main/resources/static/`으로 복사합니다.
2.  **SPA 라우팅**: `/api`로 시작하지 않는 모든 요청은 자동으로 `index.html`로 포워딩되어 React Router가 처리합니다.
3.  **API 호출**: 모든 API는 자동으로 `/api/v{n}/` 접두사가 붙어 프론트엔드 경로와 명확히 분리됩니다.
