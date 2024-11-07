package inu.codin.codin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class CodinApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodinApplication.class, args);
	}

}
