FROM openjdk:17-jdk-slim
RUN apt-get update && apt-get install -y --no-install-recommends python3 python3-pip
RUN pip3 install selenium pymongo webdriver-manager
RUN apt-get install -y wget && wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb && dpkg -i google-chrome-stable_current_amd64.deb && apt-get install -f -y
ENV PATH="/usr/bin/google-chrome-stable:${PATH}"
WORKDIR /app
COPY build/libs/codin-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
