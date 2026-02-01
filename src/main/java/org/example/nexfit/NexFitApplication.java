package org.example.nexfit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class NexFitApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexFitApplication.class, args);
    }

}
