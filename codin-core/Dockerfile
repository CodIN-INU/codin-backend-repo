FROM openjdk:17-jdk-slim
RUN apt-get update && apt-get install -y python3 python3-pip && apt-get clean
WORKDIR /app
COPY build/libs/codin-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
