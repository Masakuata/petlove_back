FROM eclipse-temurin:8u392-b08-jre-jammy

COPY build/libs/* app.jar

ENTRYPOINT ["java", "-jar", "./app.jar"]
