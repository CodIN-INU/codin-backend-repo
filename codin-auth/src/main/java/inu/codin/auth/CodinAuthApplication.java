package inu.codin.auth;

import inu.codin.auth.feign.AppleAuthClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackageClasses = AppleAuthClient.class)
public class CodinAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(CodinAuthApplication.class, args);
    }
}