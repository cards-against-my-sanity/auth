package dev.jacobandersen.cams.auth.security;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Component
public class RSAKeyService {
    private static final JcaPEMKeyConverter JCE_CONVERTER = new JcaPEMKeyConverter().setProvider(new BouncyCastleProvider());

    @Value("${auth.key.id}")
    private String keyId;

    @Value("${auth.key.path.public}")
    private String publicKeyPath;

    @Value("${auth.key.path.private}")
    private String privateKeyPath;

    @Bean("authKeyId")
    public String keyId() {
        return keyId;
    }

    @Bean
    public RSAPublicKey rsaPublicKey() throws IOException {
        return use(publicKeyPath, (obj, conv) -> {
            if (!(obj instanceof SubjectPublicKeyInfo)) {
                throw new IllegalArgumentException("Public key path does not provide expected SubjectPublicKeyInfo");
            }

            final PublicKey publicKey = conv.getPublicKey((SubjectPublicKeyInfo) obj);
            if (publicKey instanceof RSAPublicKey rsaPublicKey) {
                return rsaPublicKey;
            }

            throw new IllegalStateException("Public key path does not provide expected RSAPublicKey");
        });
    }

    @Bean
    public RSAPrivateKey rsaPrivateKey() throws IOException {
        return use(privateKeyPath, (obj, conv) -> {
            if (!(obj instanceof PrivateKeyInfo)) {
                throw new IllegalArgumentException("Private key path does not provide expected PrivateKeyInfo");
            }

            final PrivateKey privateKey = conv.getPrivateKey((PrivateKeyInfo) obj);
            if (privateKey instanceof RSAPrivateKey rsaPrivateKey) {
                return rsaPrivateKey;
            }

            throw new IllegalStateException("Private key path does not provide expected RSAPrivateKey");
        });
    }

    private <T> T use(final String path, final KeyExtractor<T> func) throws IOException {
        final Path keyPath = Path.of(path);
        if (!Files.exists(keyPath) || !Files.isRegularFile(keyPath) || !Files.isReadable(keyPath)) {
            throw new IllegalArgumentException("Key file does not exist or is not readable");
        }

        try (final PEMParser parser = new PEMParser(Files.newBufferedReader(keyPath))) {
            return func.extract(parser.readObject(), JCE_CONVERTER);
        }
    }

    @FunctionalInterface
    private interface KeyExtractor<R> {
        R extract(final Object opaqueKey, final JcaPEMKeyConverter converter) throws IOException;
    }
}
