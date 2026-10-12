package com.bookcorner.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

/**
 * Full application + a real MySQL started by Testcontainers (Docker must be running).
 * Every test class using this annotation has the SAME configuration, so Spring reuses one application context
 * and one database container for the whole test run (fast).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(properties = {
        // "tc" = Testcontainers JDBC URL; TC_DAEMON keeps the container alive until the test JVM ends
        "spring.datasource.url=jdbc:tc:mysql:8.4:///auth_test?TC_DAEMON=true",
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "spring.datasource.username=test",
        "spring.datasource.password=test",
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false"
})
@AutoConfigureMockMvc
public @interface IntegrationTest {
}
