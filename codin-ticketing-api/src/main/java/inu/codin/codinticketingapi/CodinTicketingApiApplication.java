package inu.codin.codinticketingapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
@EnableMongoAuditing
public class CodinTicketingApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodinTicketingApiApplication.class, args);
    }

}
