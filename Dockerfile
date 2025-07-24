FROM gradle:7.6-jdk17 AS builder

WORKDIR /app
COPY . .
RUN gradle clean build --no-daemon

FROM openjdk:17-jre-slim

WORKDIR /app

# 타임존 설정
RUN apt-get update && apt-get install -y tzdata curl && \
    ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/build/libs/*.jar app.jar

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/api/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}", "app.jar"]