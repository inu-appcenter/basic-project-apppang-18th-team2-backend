# =========================
# Build Stage
# =========================

# Gradle과 JDK21이 설치된 공식 이미지를 사용한다.
FROM gradle:8.10.2-jdk21 AS builder
# 컨테이너 내부에서 앞으로 작업할 기본 기렉토리를 /app으로 설정한다.
WORKDIR /app
# 현재 프로젝트의 모든 파일을 컨테이너 내부(/app)로 복사한다.
COPY . .
# Spring Boot 프로젝트를 빌드한다.
RUN ./gradlew clean bootJar --no-daemon

# =========================
# Runtime Stage
# =========================
# 실제 서비스를 실행하기 위한 가벼운 Java Runtime 이미지를 사용한다.
FROM eclipse-temurin:21-jre
# 실행 컨테이너의 기본 작업 디렉토리를 /app으로 지정한다.
WORKDIR /app
# Build Stage에서 생성된 Jar 파일만 가져온다.
COPY --from=builder /app/build/libs/*.jar app.jar
# 컨테이너가 사용할 포트는 8080임을 알려준다.
EXPOSE 8080
# 컨테이너가 시작되면 자동으로 실행되는 명령이다.
ENTRYPOINT ["java", "-jar", "app.jar"]