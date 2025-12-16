FROM eclipse-temurin:17-jdk-jammy

# 1. 기본 패키지 + Python + Chrome 의존성 설치
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      python3 python3-pip \
      libglib2.0-0 libnss3 libgconf-2-4 libfontconfig1 \
      libxdamage1 libxkbcommon0 libxrandr2 xdg-utils \
      chromium-driver wget && \
    rm -rf /var/lib/apt/lists/*

# 2. 파이썬 라이브러리
RUN pip3 install selenium pymongo webdriver-manager pandas openpyxl

# 3. Google Chrome 설치 (.deb를 apt가 설치하도록)
RUN wget -O /tmp/google-chrome.deb \
      https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb && \
    apt-get update && \
    apt-get install -y /tmp/google-chrome.deb && \
    rm -rf /var/lib/apt/lists/* /tmp/google-chrome.deb \

ENV PATH="/usr/bin/google-chrome-stable:${PATH}"

WORKDIR /app
COPY build/libs/codin-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]