FROM eclipse-temurin:17-jdk-jammy
RUN apt-get update && apt-get install -y --no-install-recommends python3 python3-pip
RUN pip3 install pymysql pandas openpyxl python-dotenv
WORKDIR /app
COPY build/libs/codin-lecture-api-0.0.1-SNAPSHOT.jar app.jar
COPY entrypoint.sh entrypoint.sh
RUN chmod +x entrypoint.sh
EXPOSE 8085
ENTRYPOINT ["./entrypoint.sh"]
