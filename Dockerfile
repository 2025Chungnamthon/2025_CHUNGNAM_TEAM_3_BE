# 빌드 스테이지
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# 전체 프로젝트 복사 (간단한 방법)
COPY . .

# 실행 권한 부여 (gradlew가 있다면)
RUN chmod +x gradlew || true

# 애플리케이션 빌드
RUN ./gradlew clean bootJar --no-daemon || gradle clean bootJar --no-daemon

# 실행 스테이지
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 헬스체크 추가
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

# 실행 스테이지
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]