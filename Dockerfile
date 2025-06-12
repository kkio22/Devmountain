FROM openjdk:17-jdk-slim

WORKDIR /app

# 리소스 복사 (yml 포함)
COPY src/main/resources/application-prod.yml /app/application-prod.yml

# JAR 복사
COPY build/libs/app.jar app.jar

# 명시적으로 prod profile 활성화
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]