package org.example.nexfit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires external services (MongoDB, Redis) to be running. Enable when using TestContainers or Docker.")
class NexFitApplicationTests {

    @Test
    void contextLoads() {
    }

}
