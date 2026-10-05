package com.bookcorner.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false"
})
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
        // Fails if the gateway cannot start (bad route config, missing dependency, ...).
    }
}
