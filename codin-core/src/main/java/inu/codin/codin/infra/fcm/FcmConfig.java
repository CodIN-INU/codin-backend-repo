package inu.codin.codin.infra.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;

@Component
@Slf4j
public class FcmConfig {

    @Value("${google.firebase.key-path}")
    private String fcmKeyPath;

    @PostConstruct
    public void init(){
        try {
            FileInputStream serviceAccount = new FileInputStream(fcmKeyPath);
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("[init] FirebaseApp initialized");
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
