package inu.codin.codinticketingapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
@EnableJpaAuditing
@EnableFeignClients
public class CodinTicketingApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodinTicketingApiApplication.class, args);
    }

}
