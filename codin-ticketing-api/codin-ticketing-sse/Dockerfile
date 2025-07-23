FROM openjdk:17-jdk-slim
WORKDIR /app
COPY build/libs/codin-ticketing-sse-0.0.1-SNAPSHOT.jar app.jar
COPY .env.prod .env
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]