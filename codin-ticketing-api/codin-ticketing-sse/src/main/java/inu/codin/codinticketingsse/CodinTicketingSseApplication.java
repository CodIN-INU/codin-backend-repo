package inu.codin.codinticketingsse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CodinTicketingSseApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodinTicketingSseApplication.class, args);
    }

}
