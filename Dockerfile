# ══════════════════════════════════════════════
# Stage 1: 빌드 + Layered JAR 추출
# ══════════════════════════════════════════════
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /workspace

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# BuildKit 캐시 마운트: Gradle 캐시가 이미지 레이어에 포함되지 않음
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon

COPY src src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar --no-daemon && \
    java -Djarmode=layertools \
         -jar build/libs/*.jar \
         extract --destination /workspace/layers

# ══════════════════════════════════════════════
# Stage 2: jlink 커스텀 JRE 생성
# ══════════════════════════════════════════════
FROM eclipse-temurin:17-jdk-alpine AS jlink-stage
WORKDIR /workspace
COPY --from=builder /workspace/build/libs/*.jar app.jar

# jdeps로 실제 사용 모듈 분석
RUN jdeps --ignore-missing-deps --print-module-deps \
    --multi-release 17 --recursive \
    --class-path app.jar app.jar > /tmp/modules.txt 2>/dev/null || true

# jlink로 커스텀 JRE 생성 (jdeps 실패 시 fallback 모듈 병합)
RUN MODULES=$(cat /tmp/modules.txt 2>/dev/null) && \
    FALLBACK="java.base,java.logging,java.naming,java.desktop,java.management,java.security.jgss,java.instrument,jdk.unsupported,jdk.crypto.ec" && \
    jlink \
      --add-modules "${MODULES:+$MODULES,}$FALLBACK" \
      --strip-debug \
      --no-man-pages \
      --no-header-files \
      --compress=2 \
      --output /custom-jre

# ══════════════════════════════════════════════
# Stage 3: 최종 런타임 이미지
# ══════════════════════════════════════════════
FROM alpine:3.19
WORKDIR /app

# 커스텀 JRE 복사
COPY --from=jlink-stage /custom-jre /opt/jre
ENV PATH="/opt/jre/bin:$PATH" \
    JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

# non-root 유저 설정 (보안)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 변경 빈도 낮은 레이어 먼저 복사 → Docker 캐시 효율 극대화
COPY --from=builder /workspace/layers/dependencies/ ./
COPY --from=builder /workspace/layers/spring-boot-loader/ ./
COPY --from=builder /workspace/layers/snapshot-dependencies/ ./
COPY --from=builder /workspace/layers/application/ ./

# Spring Boot 2.x ~ 3.1
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
