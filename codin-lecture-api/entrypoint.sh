#!/bin/sh

# 마운트된 인증서 파일 경로
CERT_FILE="/app/certs/ca.crt"
CACERTS_PATH="$JAVA_HOME/lib/security/cacerts"

# 인증서 파일이 존재하면 import 수행
if [ -f "$CERT_FILE" ]; then
    echo "Found certificate at $CERT_FILE. Importing to keystore..."

    # 이미 등록된 alias가 있다면 삭제 (재기동 시 오류 방지)
    keytool -delete -alias elasticsearch -keystore $CACERTS_PATH -storepass changeit -noprompt 2>/dev/null || true

    # 인증서 등록
    keytool -importcert -trustcacerts -alias elasticsearch \
        -file $CERT_FILE \
        -keystore $CACERTS_PATH \
        -storepass changeit -noprompt

    echo "Certificate imported successfully."
else
    echo "No certificate found at $CERT_FILE. Skipping import."
fi

# Java 애플리케이션 실행
exec java -jar app.jar
