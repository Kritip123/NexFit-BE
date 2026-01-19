package org.example.trainerhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class TrainerHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainerHubApplication.class, args);
    }

}
