package inu.codin.auth;

import inu.codin.auth.feign.AppleAuthClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = {
        "inu.codin.auth",
        "inu.codin.codin.domain.user",
        "inu.codin.codin.infra",
        "inu.codin.security",
        "inu.codin.common"
})
@EnableMongoRepositories(basePackages = "inu.codin.codin")
@EnableFeignClients(basePackageClasses = AppleAuthClient.class)
public class CodinAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(CodinAuthApplication.class, args);
    }
}