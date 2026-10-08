package ru.cherrrnikov.wealthhandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

/**
 * Generates a throwaway RSA key pair for tests and writes it as PEM files
 * into a temp directory, so tests do not depend on certs/private.pem.
 */
final class TestJwtKeys {
    private static final Base64.Encoder PEM_ENCODER =
            Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII));

    private final Path privateKeyFile;
    private final Path publicKeyFile;

    TestJwtKeys() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            Path dir = Files.createTempDirectory("wealth-handler-test-keys");
            dir.toFile().deleteOnExit(); // registered first, so it is deleted last

            privateKeyFile = writePem(dir.resolve("private.pem"), "PRIVATE KEY", keyPair.getPrivate().getEncoded());
            publicKeyFile = writePem(dir.resolve("public.pem"), "PUBLIC KEY", keyPair.getPublic().getEncoded());
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalStateException("Cannot generate JWT keys for tests", e);
        }
    }

    String privateKeyLocation() {
        return privateKeyFile.toUri().toString();
    }

    String publicKeyLocation() {
        return publicKeyFile.toUri().toString();
    }

    private static Path writePem(Path file, String type, byte[] der) throws IOException {
        String pem = "-----BEGIN " + type + "-----\n"
                + PEM_ENCODER.encodeToString(der)
                + "\n-----END " + type + "-----\n";
        Files.writeString(file, pem);
        file.toFile().deleteOnExit();
        return file;
    }
}