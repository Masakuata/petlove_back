FROM eclipse-temurin:8u392-b08-jre-jammy

COPY build/libs/* app.jar

RUN apk update && apk add bash

ENTRYPOINT ["java", "-jar", "./app.jar"]
