package dev.jacobandersen.cams.auth;

import com.redis.testcontainers.RedisContainer;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Testcontainers(disabledWithoutDocker = true)
public abstract class BaseIntegrationTest {
    private static final int MAILHOG_PORT_SMTP = 1025;
    private static final int MAILHOG_PORT_HTTP = 8025;

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:latest")
            .withDatabaseName("cams_auth")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("pg-init.sql")
            .waitingFor(Wait.forListeningPort());

    @Container
    static RedisContainer redis = new RedisContainer("redis:latest")
            .waitingFor(Wait.forListeningPort());

    @Container
    @SuppressWarnings("resource")
    static GenericContainer<?> mailhog = new GenericContainer<>("mailhog/mailhog")
            .withExposedPorts(MAILHOG_PORT_SMTP, MAILHOG_PORT_HTTP)
            .waitingFor(Wait.forListeningPort());

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) throws IOException, NoSuchAlgorithmException {
        final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);

        final KeyPair keyPair = keyPairGenerator.generateKeyPair();

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getRedisPort);

        registry.add("spring.mail.host", mailhog::getHost);
        registry.add("spring.mail.properties.mail.smtp.port", () -> mailhog.getMappedPort(MAILHOG_PORT_SMTP));

        registry.add("auth.key.id", () -> UUID.randomUUID().toString());

        final Path tempPublicKey = Files.createTempFile("test-public", ".pem");
        writePem(tempPublicKey, "PUBLIC KEY", keyPair.getPublic().getEncoded());
        registry.add("auth.key.path.public", () -> tempPublicKey.toAbsolutePath().toString());

        final Path tempPrivateKey = Files.createTempFile("test-private", ".pem");
        writePem(tempPrivateKey, "PRIVATE KEY", keyPair.getPrivate().getEncoded());
        registry.add("auth.key.path.private", () -> tempPrivateKey.toAbsolutePath().toString());
    }

    private static void writePem(Path path, String type, byte[] content) throws IOException {
        try (final PemWriter writer = new PemWriter(Files.newBufferedWriter(path))) {
            writer.writeObject(new PemObject(type, content));
        }
    }
}
