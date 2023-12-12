FROM eclipse-temurin:17-jdk-alpine

COPY build/libs/* app.jar

RUN apk update && apk add bash

ENTRYPOINT ["java", "-jar", "./app.jar"]
