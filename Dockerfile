FROM openjdk:22-ea-17-jdk-slim

COPY build/libs/* app.jar

COPY src/main/resources/pet-icon.png src/main/resources/pet-icon.png

CMD ["java", "-jar", "./app.jar"]
