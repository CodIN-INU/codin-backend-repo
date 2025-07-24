package inu.codin.codin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class CodinLectureApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodinLectureApiApplication.class, args);
    }

}
