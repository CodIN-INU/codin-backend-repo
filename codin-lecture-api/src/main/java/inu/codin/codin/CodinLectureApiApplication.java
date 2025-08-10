package inu.codin.codin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.util.TimeZone;

@SpringBootApplication
@EnableFeignClients
@EnableElasticsearchRepositories
public class CodinLectureApiApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        SpringApplication.run(CodinLectureApiApplication.class, args);
    }

}
