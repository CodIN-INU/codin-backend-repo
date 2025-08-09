package inu.codin.codinticketingapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CodinTicketingApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodinTicketingApiApplication.class, args);
    }

}
