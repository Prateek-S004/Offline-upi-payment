package com.demo.upimesh.crypto;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;

/**
 * Holds the server's RSA keypair.
 *
 * In production, the private key would live in an HSM (Hardware Security Module)
 * or at least a KMS like AWS KMS / HashiCorp Vault. NEVER in the JAR or source.
 *
 * For this demo we generate a fresh keypair on every startup. The public key is
 * exposed via /api/server-key so the (simulated) sender devices can use it to
 * encrypt payloads.
 */
@Component
public class ServerKeyHolder {

    private static final Logger log = LoggerFactory.getLogger(ServerKeyHolder.class);

    private KeyPair keyPair;
    @Value("${upi.keystore.path}")
    private String keystorePath;

    @Value("${upi.keystore.password}")
    private String keystorePassword;

    @PostConstruct
    public void init() throws Exception {

        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            keyStore.load(fis, keystorePassword.toCharArray());
        }

        PrivateKey privateKey = (PrivateKey) keyStore.getKey("upi-server", keystorePassword.toCharArray());
        Certificate certificate = keyStore.getCertificate("upi-server");
        PublicKey publicKey = certificate.getPublicKey();

        this.keyPair = new KeyPair(publicKey, privateKey);

        log.info("Persistent RSA keypair loaded successfully.");
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }
}
