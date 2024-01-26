FROM openjdk:22-ea-17-jdk-slim

COPY build/libs/* app.jar

CMD ["java", "-jar", "./app.jar"]
