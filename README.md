# Spring Boot Base

공통 인증, 보안, 운영 기능을 기본 포함한 Spring Boot base 프로젝트입니다.

이 프로젝트는 단순한 샘플 API가 아니라, 실제 서비스 시작 시 바로 가져다 쓸 수 있는 공통 기반을 목표로 합니다. 인증, 토큰, 감사 로그, 파일 업로드, 외부 연동, 캐시, 표준 응답, 스케줄링, 운영 헬스체크, 문서화까지 한 번에 갖춘 형태입니다.

## 핵심 특징

- JWT access/refresh token 인증
- refresh token rotation, token family, 세션별 관리
- BCrypt 기반 비밀번호 저장/검증
- 로그인 실패 횟수 제한 및 계정 잠금
- 최근 로그인 시간/IP 기록
- 선택적 MFA(TOTP)
- 이메일 유틸 및 비밀번호 재설정
- Firebase 유틸
- 감사 로그
- 민감정보 로그 마스킹
- XSS 방어 및 HTML 허용 예외 정책
- 파일 업로드 보안 검증
- Redis/로컬 캐시 전략 표준화
- 외부 연동 timeout/retry/idempotency 기본기
- 운영/관리자 API
- Flyway 마이그레이션
- 표준 에러 응답
- Actuator 기반 운영 헬스체크
- SPA 라우팅 지원

## 패키지 구조

```text
src/main/java/com/threlease/base
├─ common
│  ├─ annotation
│  ├─ configs
│  ├─ controller
│  ├─ convert
│  ├─ entity
│  ├─ enums
│  ├─ exception
│  ├─ handler
│  ├─ interceptors
│  ├─ properties
│  ├─ provider
│  ├─ utils
│  └─ validation
├─ entities
├─ functions
│  └─ auth
└─ repositories
```

## 애플리케이션 시작점

- `BaseApplication`
  - 캐시 활성화
  - 스케줄링 활성화
  - JPA Auditing 활성화
  - JPA Repository 활성화
  - Redis/Redisson 자동 설정 제외

이 프로젝트는 Redis를 Spring Boot 기본 자동설정에 맡기지 않고, 내부 `CacheConfig`에서 직접 제어합니다.

## 설정 구조

### 공통 설정

`src/main/resources/application.yml`

공통적으로 관리하는 설정은 아래입니다.

- 로깅 관련
- 데이터베이스 관련
- CORS 관련
- Swagger 관련
- 기본 활성 프로필 선택

기본 활성 프로필:

```yml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}
```

### 프로필별 설정

- `application-local.yml`
- `application-dev.yml`
- `application-prod.yml`
- `application-env.yml`

역할은 아래와 같습니다.

- `local`, `dev`, `prod`
  - 베이스 프로젝트 커스텀 기능을 고정값으로 관리
- `env`
  - 베이스 프로젝트 커스텀 기능을 환경변수 기반으로 관리

주요 커스텀 설정 범위:

- `app.redis.*`
- `app.cache.*`
- `app.jwt.*`
- `app.token.*`
- `app.auth.*`
- `app.outbound.*`
- `app.privacy.*`
- `app.security.*`
- `app.qr.*`
- `storage.*`
- `crypto.*`
- `spring.cloud.aws.*`

## 인증/인가 기능

대표 클래스:

- `functions/auth/v1/AuthController`
- `functions/auth/AuthService`
- `common/provider/JwtProvider`
- `entities/AuthEntity`
- `entities/RefreshTokenEntity`

### 지원 기능

- 회원가입
- 로그인
- access token 발급
- refresh token 발급
- refresh token rotation
- 세션 목록 조회
- 특정 세션 종료
- 현재 세션 로그아웃
- 전체 로그아웃
- 비밀번호 변경
- 관리자용 사용자 조회/잠금/세션조회/전체로그아웃

### 인증 API

- `POST /api/v1/auth/signup`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `POST /api/v1/auth/logout-all`
- `GET /api/v1/auth/sessions`
- `DELETE /api/v1/auth/sessions/{tokenId}`
- `POST /api/v1/auth/password/change`
- `POST /api/v1/auth/password/reset/request`
- `POST /api/v1/auth/password/reset/confirm`
- `GET /api/v1/auth/@me`

### 관리자 API

- `GET /api/v1/auth/admin/users`
- `GET /api/v1/auth/admin/users/{uuid}/sessions`
- `POST /api/v1/auth/admin/users/{uuid}/logout-all`
- `POST /api/v1/auth/admin/users/{uuid}/lock`
- `GET /api/v1/auth/admin/audit-logs`

관리자 권한은 `ROLE_ADMIN` 이어야 합니다.

### 비밀번호 정책

- 저장: `BCrypt`
- 검증: `BCrypt`
- 레거시 `SHA-512 + salt` fallback 없음

즉 현재는 비밀번호 검증이 완전히 `BCrypt only` 입니다.

### refresh token 정책

- 평문 저장하지 않음
- 해시 기반 저장
- `tokenId`, `familyId` 기반 세션/rotation 관리
- 재사용 감지 시 family revoke 가능
- 세션 수 제한 설정 가능
- 세션별 `issuedAt`, `lastUsedAt`, `userAgent`, `deviceLabel`, `ipAddress` 관리

관련 설정:

- `app.token.storage`
- `app.token.max-sessions-per-user`
- `app.token.validate-schema`

## 로그인 보안 기능

대표 클래스:

- `common/properties/app/auth/AuthSecurityProperties`
- `functions/auth/AuthService`
- `functions/auth/MfaService`

### 로그인 실패 제한

- 실패 횟수 누적
- 제한 횟수 초과 시 계정 잠금
- 성공 로그인 시 실패 횟수 초기화
- 잠금 시간 설정 가능

관련 설정:

```yml
app:
  auth:
    login-failure:
      enabled: true
      max-attempts: 5
      lock-minutes: 15
```

### 최근 로그인 기록

`AuthEntity`에 아래 정보가 저장됩니다.

- `lastLoginAt`
- `lastLoginIp`

### 비밀번호 재설정

이 프로젝트는 이메일 기능 on/off에 따라 비밀번호 재설정 정책이 달라집니다.

- `app.email.enabled=true`
  - 재설정 요청 시 인증 코드를 발급하고 이메일로 전송합니다.
  - 재설정 완료 시 이메일 인증 코드를 검증한 뒤 새 비밀번호를 저장합니다.
- `app.email.enabled=false`
  - 재설정 완료 시 현재 비밀번호를 검증한 뒤 새 비밀번호를 저장합니다.

관련 API:

- `POST /api/v1/auth/password/reset/request`
- `POST /api/v1/auth/password/reset/confirm`

관련 설정:

```yml
app:
  auth:
    password-reset:
      code-expire-minutes: 10
```

### MFA

지원 기능:

- TOTP secret 발급
- OTP Auth URI 제공
- QR code 기반 MFA setup
- MFA enable
- MFA disable
- 로그인 시 OTP 검증

관련 설정:

```yml
app:
  auth:
    mfa:
      enabled: false
      issuer: spring-boot-base
      code-digits: 6
      time-step-seconds: 30
      allowed-windows: 1
      required-roles: []
```

## 감사 로그

대표 클래스:

- `entities/AuditLogEntity`
- `functions/auth/AuditLogService`
- `repositories/auth/AuditLogRepository`

기록 대상 예시:

- 로그인 성공
- 로그인 실패
- 계정 잠금
- 회원가입
- 비밀번호 변경
- MFA 활성화/비활성화
- 로그아웃
- 전체 로그아웃
- 세션 종료
- 관리자 작업

관련 설정:

```yml
app:
  auth:
    audit:
      enabled: true
      include-user-agent: true
```

개인정보 보호와 함께 연결되는 설정:

- `app.privacy.mask-audit-ip`
- `app.privacy.include-user-agent`
- `app.privacy.audit-retention-days`

## 이메일 유틸리티

대표 클래스:

- `common/properties/app/email/EmailProperties`
- `common/configs/EmailConfig`
- `common/utils/email/EmailService`

기능:

- SMTP 메일 발송
- 텍스트 메일 발송
- HTML 메일 발송
- 비밀번호 재설정 인증 코드 발송
- on/off 설정 지원
- on 인데 필수 설정이 비어 있으면 시작 시 강제 종료

필수 설정:

- `app.email.host`
- `app.email.port`
- `app.email.username`
- `app.email.password`
- `app.email.from-address`

사용 예시:

```java
emailService.sendText("user@example.com", "Subject", "Body");
emailService.sendHtml("user@example.com", "Subject", "<b>Body</b>");
```

## Firebase 유틸리티

대표 클래스:

- `common/properties/app/firebase/FirebaseProperties`
- `common/configs/FirebaseConfig`
- `common/utils/firebase/FirebaseUtils`

기능:

- Firebase Admin SDK 초기화
- Firebase ID 토큰 검증
- FCM push 전송
- on/off 설정 지원
- on 인데 필수 설정이 비어 있으면 시작 시 강제 종료

필수 설정:

- `app.firebase.project-id`
- `app.firebase.credentials-path` 또는 `app.firebase.credentials-json`

사용 예시:

```java
firebaseUtils.verifyIdToken(idToken);
firebaseUtils.sendNotification(targetToken, "title", "body", Map.of("type", "notice"));
```

## 웹/MVC 기능

대표 클래스:

- `common/configs/WebMvcConfig`
- `common/interceptors/TokenInterceptor`
- `common/interceptors/ApiVersionInterceptor`
- `common/controller/ViewController`
- `common/controller/CommonController`

### API 버저닝

`@ApiVersion(1)` 이 붙은 컨트롤러는 자동으로 `/api/v1` prefix가 붙습니다.

예:

```java
@RestController
@ApiVersion(1)
@RequestMapping("/auth")
public class AuthController {
}
```

실제 경로:

```text
/api/v1/auth
```

### 토큰 인터셉터

`TokenInterceptor`는 다음을 처리합니다.

- `Authorization` 헤더 존재 여부 확인
- `Bearer ` prefix 확인
- JWT에서 사용자 조회
- 요청 attribute에 사용자 주입

### SPA 라우팅 지원

`ViewController`는 `/api/**` 가 아닌 확장자 없는 요청을 모두 `index.html`로 forward합니다.

즉 React/Vue SPA 라우팅을 서버 단에서도 지원합니다.

### 공통 enum API

`CommonController`는 시스템 enum 목록을 JSON으로 노출합니다.

예:

- `GET /api/common/enums`

## 보안 헤더/XSS/로그 마스킹

대표 클래스:

- `common/configs/WebSecurityConfig`
- `common/handler/XssFilter`
- `common/handler/XssRequestWrapper`
- `common/handler/XssRequestBodyAdvice`
- `common/handler/LoggingFilter`
- `common/utils/XssUtils`

### 보안 헤더

기본 제공:

- CSP
- Frame deny
- HSTS
- Referrer-Policy
- Content-Type-Options

설정:

```yml
app:
  security:
    headers:
      hsts-enabled: true
      content-security-policy: "default-src 'self'; script-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'none'"
      referrer-policy: strict-origin-when-cross-origin
```

### XSS 방어

기능:

- request parameter sanitize
- request body string sanitize
- JSON/Map/List 재귀 sanitize
- `@AllowHtml` 예외 지원

`@AllowHtml` 예시:

```java
public class NoticeCreateDto {
    @AllowHtml(AllowHtml.Policy.BASIC)
    private String content;
}
```

기본은 sanitize, HTML을 허용해야 하는 필드만 명시적으로 예외 처리합니다.

### 로그 마스킹

`LoggingFilter`는 다음을 지원합니다.

- request/response payload 로깅
- correlation id 생성 및 응답 헤더 추가
- JSON/form/query string 마스킹
- 민감 키 이름 기반 마스킹
- JWT/Bearer/API key 패턴 기반 마스킹

기본 민감 키 예시:

- `password`
- `newPassword`
- `accessToken`
- `refreshToken`
- `authorization`
- `secret`
- `apiKey`
- `otp`
- `cookie`

## 표준 응답/예외 처리

대표 클래스:

- `common/utils/responses/BasicResponse`
- `common/utils/responses/PageResponse`
- `common/handler/GlobalExceptionHandler`
- `common/exception/ErrorCode`
- `common/exception/BusinessException`

기능:

- 성공/실패 응답 형식 통일
- 에러 코드 표준화
- validation field error 응답
- path, timestamp, correlationId 포함
- 메시지 국제화 연동

### 인증 응답 노출 정책

인증 관련 응답은 JPA 엔티티를 그대로 반환하지 않고 안전한 DTO로 제한합니다.

- 회원가입 응답: `AuthProfileDto`
- 내 정보 조회 응답: `AuthProfileDto`

즉 아래 내부 인증 필드는 외부 응답에서 직접 노출하지 않습니다.

- `password`
- `salt`
- `passwordResetCodeHash`
- `passwordResetCodeExpiry`
- `failedLoginCount`
- `lockedUntil`
- `lastLoginIp`

## 캐시

대표 클래스:

- `common/configs/CacheConfig`
- `common/properties/app/cache/CachePolicyProperties`
- `common/properties/app/redis/RedisProperties`

기능:

- Redis 기반 캐시
- Redis 비활성 시 로컬 캐시 fallback
- cache name 정책
- TTL 설정
- cache prefix 설정

대표 캐시 사용처:

- 사용자 조회 캐시

## 데이터베이스/Flyway/JPA

대표 클래스:

- `common/properties/app/database/DatabaseProperties`
- `common/configs/CustomPhysicalNamingStrategy`
- `common/handler/RefreshTokenSchemaValidator`

기능:

- JPA auditing
- 테이블/컬럼 naming 전략
- Flyway migration
- JPA schema와 Flyway schema 분리 설정
- refresh token RDB 스키마 검증

설정 예시:

```yml
app:
  database:
    jpa-schema: public
    flyway-schema: public
```

## 외부 연동

대표 클래스:

- `common/configs/RestTemplateConfig`
- `common/configs/RetryConfig`
- `common/handler/OutboundRequestInterceptor`
- `common/handler/RestTemplateLoggingInterceptor`
- `common/utils/RestService`
- `common/properties/app/outbound/OutboundProperties`

기능:

- connect/read timeout
- retry
- correlation-id 전달
- idempotency key 추가
- 외부 요청 로깅

`RestService` 사용 예시:

```java
String response = restService.get(
    "https://example.com/api/users",
    Map.of("page", 0, "size", 20),
    String.class
);
```

헤더 포함 호출 예시:

```java
ResponseEntity<String> response = restService.exchange(
    "https://example.com/api/orders",
    HttpMethod.POST,
    Map.of("name", "sample"),
    Map.of("Authorization", "Bearer xxx"),
    String.class
);
```

## 파일 업로드/스토리지

대표 클래스:

- `common/configs/StorageConfig`
- `common/utils/storage/StorageService`
- `common/utils/storage/LocalStorageService`
- `common/utils/storage/S3StorageService`
- `common/utils/storage/FileUploadSecurityService`
- `common/utils/storage/entity/FileEntity`
- `common/utils/storage/repository/FileRepository`
- `common/utils/storage/batch/OrphanFileCleanupJob`

기능:

- 로컬 저장
- S3 저장
- 스토리지 구현 자동 선택
- 업로드 파일명 sanitize
- 허용 확장자 검사
- 허용 content-type 검사
- 이중 확장자 차단
- 파일 메타데이터 저장
- 고아 파일 정리 스케줄

업로드 보안 설정 예시:

```yml
storage:
  upload:
    enabled: true
    max-file-size-bytes: 10485760
    block-double-extension: true
    allowed-extensions: [jpg, jpeg, png, gif, pdf, txt, csv]
    allowed-content-types: [image/jpeg, image/png, image/gif, application/pdf, text/plain, text/csv]
```

## 스케줄링/비동기/락

대표 클래스:

- `common/configs/AsyncConfig`
- `common/configs/SchedulingConfig`
- `common/handler/MdcTaskDecorator`
- `common/annotation/DistributedLock`
- `common/handler/DistributedLockAspect`

기능:

- 비동기 task executor
- MDC 전파
- 스케줄 작업
- 분산 락 기반 임계영역 보호

## Swagger / Actuator / 운영성

대표 클래스:

- `common/configs/SwaggerConfig`
- `common/handler/BasePlatformHealthIndicator`

기능:

- Swagger UI
- OpenAPI 문서
- health / metrics / prometheus
- base 플랫폼 health indicator

Swagger 경로:

- `/api/swagger`

## 공통 유틸리티

이 프로젝트는 실무에서 자주 반복되는 작업을 줄이기 위한 유틸리티를 많이 포함합니다.

### 날짜/시간

클래스:

- `common/utils/DateTimeUtils`

기능:

- 문자열 `yyyy-MM-dd` → `LocalDateTime`
- `LocalDateTime` → 날짜 문자열
- `LocalDateTime` → 날짜시간 문자열
- 현재 시각 조회

사용 예시:

```java
Failable<LocalDateTime, String> result = dateTimeUtils.parseDateStringToLocalDateTime("2026-04-08");
if (result.isSuccess()) {
    LocalDateTime value = result.getSuccess();
}

String date = dateTimeUtils.formatLocalDateTimeToDateString(LocalDateTime.now());
String dateTime = dateTimeUtils.formatLocalDateTimeToDateTimeString(LocalDateTime.now());
```

### JSON

클래스:

- `common/utils/JsonUtils`

기능:

- 객체 → pretty JSON
- 객체 → compact JSON
- JSON 문자열 → 객체
- JSON pretty print

사용 예시:

```java
String json = JsonUtils.toJson(Map.of("name", "base"));
String compact = JsonUtils.toJsonCompact(Map.of("id", 1));
MyDto dto = JsonUtils.fromJson("{\"name\":\"sample\"}", MyDto.class);
```

### 국제화 메시지

클래스:

- `common/utils/MessageUtils`

기능:

- 현재 locale 기준 메시지 조회
- 파라미터 치환 메시지 조회
- locale 지정 조회

사용 예시:

```java
String message = MessageUtils.getMessage("common.error");
String formatted = MessageUtils.getMessage("USER_NOT_FOUND", new Object[]{"tester"});
```

### 암호화

클래스:

- `common/utils/crypto/AesComponent`
- `common/convert/EncryptedStringConverter`
- `common/utils/crypto/Base64Component`
- `common/utils/crypto/HashComponent`

기능:

- AES-256-GCM 양방향 암복호화
- JPA entity 필드 자동 암복호화
- Base64 encode/decode
- SHA-512 해시 유틸

`AesComponent` 사용 예시:

```java
String encrypted = aesComponent.encrypt("01012345678");
String plain = aesComponent.decrypt(encrypted);
boolean same = aesComponent.matches("01012345678", encrypted);
```

`EncryptedStringConverter` 사용 예시:

```java
@Convert(converter = EncryptedStringConverter.class)
private String phoneNumber;
```

### 난수/코드 생성

클래스:

- `common/utils/random/RandomComponent`

기능:

- OTP 생성
- 임시 비밀번호 생성
- 초대코드 생성
- 알파뉴메릭 문자열 생성

사용 예시:

```java
String otp = randomComponent.generateOtp();
String tempPassword = randomComponent.generateTempPassword();
String inviteCode = randomComponent.generateInviteCode();
String random = randomComponent.generateAlphanumeric(32);
```

### 파일 처리

클래스:

- `common/utils/FileUtils`

기능:

- 파일/디렉토리 존재 확인
- 파일/디렉토리 생성
- 파일 삭제
- 디렉토리 재귀 삭제
- 문자열 읽기/쓰기
- 파일 복사
- 확장자 추출

사용 예시:

```java
if (!FileUtils.exists("./uploads/test.txt")) {
    FileUtils.createDirectory("./uploads");
    FileUtils.writeStringToFile("./uploads/test.txt", "hello");
}

String ext = FileUtils.getFileExtension("sample.pdf");
```

### 페이지네이션

클래스:

- `common/utils/PageRequestHelper`

기능:

- 기본 pageable 생성
- 정렬 포함 pageable 생성
- 최신순 pageable 생성

사용 예시:

```java
Pageable pageable = PageRequestHelper.of(page, size);
Pageable latest = PageRequestHelper.ofLatest(page, size);
```

### 개인정보 마스킹

클래스:

- `common/utils/masking/MaskingComponent`

기능:

- 이름 마스킹
- 전화번호 마스킹
- 이메일 마스킹

사용 예시:

```java
String maskedName = maskingComponent.maskName("홍길동");
String maskedPhone = maskingComponent.maskPhone("+821012345678");
String maskedEmail = maskingComponent.maskEmail("hello@example.com");
```

### QR 코드

클래스:

- `common/utils/QR/QRCode`
- `common/utils/QR/QRCodeOption`

기능:

- QR 생성 byte[]
- QR 생성 Base64
- QR 파일 저장
- 로고 포함 QR 생성
- QR decode

사용 예시:

```java
byte[] qr = qrCode.generateQrCode("otpauth://totp/sample");
String base64 = qrCode.generateQrCodeBase64("hello");
qrCode.generateQrCodeToFile("hello", Path.of("./tmp/hello.png"));
```

### 엑셀

클래스:

- `common/utils/ExcelUtils`
- `common/annotation/ExcelColumn`

기능:

- 리스트 → 엑셀 export
- 엑셀 → 객체 리스트 import

사용 예시:

```java
Workbook workbook = ExcelUtils.export(users, UserExcelDto.class);
List<UserExcelDto> rows = ExcelUtils.importExcel(inputStream, UserExcelDto.class);
```

DTO 예시:

```java
public class UserExcelDto {
    @ExcelColumn(headerName = "아이디", order = 1)
    private String username;

    @ExcelColumn(headerName = "닉네임", order = 2)
    private String nickname;
}
```

### 기타 유틸리티

- `CollectionUtils`
  - 컬렉션 관련 헬퍼
- `ObjectUtils`
  - null-safe 객체 처리
- `ReflectionUtils`
  - reflection 헬퍼
- `IpUtils`
  - 클라이언트 IP 추출
- `QuerydslUtils`
  - Querydsl 관련 보조 로직
- `EnumMapperValue`, `EnumValueValidator`
  - enum 코드 노출/검증

## 공통 검증 어노테이션

지원 어노테이션:

- `@ValidEmail`
- `@ValidPhoneNumber`
- `@ValidEnum`

예시:

```java
public class SignUpDto {
    @ValidEmail
    private String email;
}
```

## 정적 리소스

`src/main/resources/static`

- `index.html`
- `script.js`
- `style.css`

기본 SPA 진입 화면/샘플 리소스로 사용할 수 있습니다.

## 메시지/로깅 리소스

- `messages.properties`
  - 다국어/메시지 코드 리소스
- `logback-spring.xml`
  - 로깅 설정

## DB 마이그레이션

- `src/main/resources/db/migration/V1__base_security_extensions.sql`

보안/인증 관련 확장 스키마를 Flyway로 관리합니다.

## 테스트

현재 포함된 테스트:

- `BaseApplicationTests`
- `RefreshTokenSchemaValidatorTest`
- `AuthSecurityPolicyTest`
- `AuthServiceRdbTest`
- `MfaServiceTest`

검증 명령:

```bash
./gradlew compileJava
./gradlew test
```

## 빠른 시작

### 1. 로컬 실행

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

### 2. 환경변수 기반 실행

```bash
SPRING_PROFILES_ACTIVE=env \
JWT_SECRET_KEY=... \
AES_SECRET_KEY=... \
./gradlew bootRun
```

### 3. Swagger 확인

```text
http://localhost:8080/api/swagger
```

## 권장 사용 방식

- 사용자 인증이 필요한 API는 `@ApiVersion` + `TokenInterceptor` 흐름을 따릅니다.
- 민감정보는 로그에 남기지 말고 `LoggingFilter` 마스킹 규칙을 따릅니다.
- 저장형 민감정보는 `EncryptedStringConverter` 같은 암호화 경로를 우선 검토합니다.
- HTML 허용 필드는 기본 sanitize를 유지한 채 `@AllowHtml`로 예외 처리합니다.
- 외부 API 호출은 직접 `RestTemplate`보다 `RestService`를 우선 사용합니다.
- 파일 업로드는 반드시 `StorageService`를 통해 처리합니다.

## 문서

- `docs/BASE_TEMPLATE_CHECKLIST.md`
- `docs/API_DEPRECATION_POLICY.md`

이 문서들과 함께 보면 베이스 프로젝트를 팀 표준으로 가져가기가 더 쉽습니다.
