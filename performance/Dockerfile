FROM openjdk:11-slim

ARG WAR_FILE=./*war
COPY ${WAR_FILE} app.war

ENTRYPOINT ["java", "-jar", "/app.war"]
