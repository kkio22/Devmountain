FROM openjdk:17-jdk-slim

WORKDIR /app

# Node.js 설치 (Node 18 LTS)
RUN apt-get update && \
    apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs

# MCP dist 복사
COPY mcp-dist ./youtube-mcp-server

# 리소스 복사 (yml 포함)
COPY src/main/resources/application-prod.yml /app/application-prod.yml

# JAR 복사
COPY build/libs/app.jar app.jar

# 명시적으로 prod profile 활성화
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
