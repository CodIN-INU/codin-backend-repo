FROM openjdk:17-jdk-slim
WORKDIR /app
COPY build/libs/codin-ticketing-api-0.0.1-plain.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]