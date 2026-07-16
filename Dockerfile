# =========================
# Build Stage
# =========================

FROM gradle:8.10.2-jdk21 AS builder

WORKDIR /app

# 프로젝트 전체 복사
COPY . .

# gradlew 실행 권한
RUN chmod +x gradlew

# BootJar 생성
RUN ./gradlew clean bootJar --no-daemon

# =========================
# Runtime Stage
# =========================

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]