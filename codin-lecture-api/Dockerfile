FROM openjdk:17-jdk-slim
RUN apt-get update && apt-get install -y --no-install-recommends python3 python3-pip
RUN pip3 install pymysql pandas openpyxl
WORKDIR /app
COPY build/libs/codin-lecture-api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]