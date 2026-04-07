# ══════════════════════════════════════════════
# Stage 1: 빌드 + Layered JAR 추출
# ══════════════════════════════════════════════
FROM amazoncorretto:17-alpine-jdk AS builder
WORKDIR /workspace

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# BuildKit 캐시 마운트로 빌드 속도 개선
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
FROM amazoncorretto:17-alpine-jdk AS jlink-stage
WORKDIR /workspace

# jlink --strip-debug에 필요한 objcopy(binutils) 설치
RUN apk add --no-cache binutils

COPY --from=builder /workspace/build/libs/*.jar app.jar

# jdeps로 실제 사용 모듈 분석
RUN jdeps --ignore-missing-deps --print-module-deps \
    --multi-release 17 --recursive \
    --class-path app.jar app.jar > /tmp/modules.txt 2>/dev/null || true

# jlink로 커스텀 JRE 생성
RUN MODULES=$(cat /tmp/modules.txt 2>/dev/null) && \
    FALLBACK="java.base,java.logging,java.naming,java.desktop,java.management,java.security.jgss,java.instrument,jdk.unsupported,jdk.crypto.ec" && \
    jlink \
      --module-path /usr/lib/jvm/default-jvm/jmods \
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

# 보안을 위한 non-root 유저 설정
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Layered JAR 레이어 복사
COPY --from=builder /workspace/layers/dependencies/ ./
COPY --from=builder /workspace/layers/spring-boot-loader/ ./
COPY --from=builder /workspace/layers/snapshot-dependencies/ ./
COPY --from=builder /workspace/layers/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
