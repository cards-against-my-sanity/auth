package dev.jacobandersen.cams.auth;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class BaseIntegrationTest {
    private static final int MAILHOG_PORT_SMTP = 1025;
    private static final int MAILHOG_PORT_HTTP = 8025;

    @Container
    @SuppressWarnings("resource")
    static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:latest")
            .withDatabaseName("cams_auth")
            .withUsername("test")
            .withPassword("test")
            .waitingFor(Wait.forListeningPort());

    @Container
    @SuppressWarnings("resource")
    static GenericContainer<?> mailhog = new GenericContainer<>("mailhog/mailhog")
            .withExposedPorts(MAILHOG_PORT_SMTP, MAILHOG_PORT_HTTP)
            .waitingFor(Wait.forListeningPort());

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mariadb::getJdbcUrl);
        registry.add("spring.datasource.username", mariadb::getUsername);
        registry.add("spring.datasource.password", mariadb::getPassword);

        registry.add("spring.mail.host", mailhog::getHost);
        registry.add("spring.mail.properties.mail.smtp.port", () -> mailhog.getMappedPort(MAILHOG_PORT_SMTP));

        registry.add("application.security.secret-key", () -> RandomStringUtils.secureStrong().nextAlphanumeric(64));
    }
}
