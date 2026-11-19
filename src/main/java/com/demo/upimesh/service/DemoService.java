package com.demo.upimesh.service;

import com.demo.upimesh.crypto.HybridCryptoService;
import com.demo.upimesh.crypto.ServerKeyHolder;
import com.demo.upimesh.model.Account;
import com.demo.upimesh.model.AccountRepository;
import com.demo.upimesh.model.MeshPacket;
import com.demo.upimesh.model.PaymentInstruction;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.UUID;

/**
 * Helper service that:
 *   - seeds demo accounts on startup
 *   - simulates "sender phone creates an encrypted packet" flow
 */
@Service
public class DemoService {

    private static final Logger log = LoggerFactory.getLogger(DemoService.class);

    @Autowired private AccountRepository accounts;
    @Autowired private HybridCryptoService crypto;
    @Autowired private ServerKeyHolder serverKey;
    @Autowired private PinService pinService;

    @PostConstruct
    public void seedAccounts() {
        if (accounts.count() == 0) {
            accounts.save(new Account("alice@demo", "Alice", pinService.hashPin("1111"),  new BigDecimal("5000.00")));
            accounts.save(new Account("bob@demo",   "Bob",  pinService.hashPin("2222"),  new BigDecimal("1000.00")));
            accounts.save(new Account("carol@demo", "Carol", pinService.hashPin("3333"),  new BigDecimal("2500.00")));
            accounts.save(new Account("dave@demo",  "Dave", pinService.hashPin("4444"),   new BigDecimal("500.00")));
            log.info("Seeded 4 demo accounts");
        }
    }

    /**
     * Simulates the sender's phone:
     *   1. Build a PaymentInstruction with a fresh nonce + signedAt timestamp.
     *   2. Encrypt with the server's public key (hybrid RSA+AES).
     *   3. Wrap in a MeshPacket with TTL.
     *
     * In a real Android app, this exact code (minus the server-side reference)
     * would run on the phone. The phone would have already cached the server's
     * public key during a previous online session.
     */
    public MeshPacket createPacket(String senderVpa, String receiverVpa,
                                   BigDecimal amount, String pin, int ttl) throws Exception {
        Account sender = accounts.findById(senderVpa).orElseThrow(() ->
                        new RuntimeException("Sender account not found"));
        boolean validPin = pinService.verifyPin(pin, sender.getPinHash());

        if(!validPin){
            throw new Exception("Entered wrong pin");
        }
        PaymentInstruction instruction = new PaymentInstruction(
                senderVpa,
                receiverVpa,
                amount,
                UUID.randomUUID().toString(),       // nonce — guarantees uniqueness
                Instant.now().toEpochMilli()        // signedAt — for freshness check
        );

        String ciphertext = crypto.encrypt(instruction, serverKey.getPublicKey());

        MeshPacket packet = new MeshPacket();
        packet.setPacketId(UUID.randomUUID().toString());
        packet.setTtl(ttl);
        packet.setCreatedAt(Instant.now().toEpochMilli());
        packet.setCiphertext(ciphertext);
        return packet;
    }

    private String sha256Hex(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(input.getBytes());
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) hex.append(String.format("%02x", b));
        return hex.toString();
    }
}
