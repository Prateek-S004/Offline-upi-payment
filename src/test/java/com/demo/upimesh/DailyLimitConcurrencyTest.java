package com.demo.upimesh;

import com.demo.upimesh.model.Account;
import com.demo.upimesh.model.AccountRepository;
import com.demo.upimesh.model.PaymentInstruction;
import com.demo.upimesh.model.Transaction;
import com.demo.upimesh.model.TransactionRepository;
import com.demo.upimesh.service.SettlementService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DailyLimitConcurrencyTest {

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private AccountRepository accounts;

    @Autowired
    private TransactionRepository transactions;

    private final String sender = "test-sender@upi";
    private final String receiver = "test-receiver@upi";

    @AfterEach
    void cleanup() {
        transactions.findAll().stream()
                .filter(tx -> sender.equals(tx.getSenderVpa()))
                .forEach(tx -> transactions.deleteById(tx.getId()));

        accounts.deleteById(sender);
        accounts.deleteById(receiver);
    }

    @Test
    void concurrentPaymentsShouldNotExceedDailyLimit() throws Exception {

        // Create isolated test accounts
        accounts.save(new Account(
                sender, "Test Sender", "dummy", new BigDecimal("50000.00")));

        accounts.save(new Account(
                receiver, "Test Receiver", "dummy", BigDecimal.ZERO));

        // Record ₹20,000 already spent today
        Transaction previous = new Transaction();
        previous.setPacketHash(generateHash());
        previous.setSenderVpa(sender);
        previous.setReceiverVpa(receiver);
        previous.setAmount(new BigDecimal("20000.00"));
        previous.setSignedAt(Instant.now());
        previous.setSettledAt(Instant.now());
        previous.setBridgeNodeId("test-bridge");
        previous.setHopCount(1);
        previous.setStatus(Transaction.Status.SETTLED);

        transactions.save(previous);

        // Two separate payments of ₹3,000
        PaymentInstruction payment1 = createPayment();
        PaymentInstruction payment2 = createPayment();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Future<Transaction> future1 = executor.submit(() -> {
                ready.countDown();
                start.await();
                return settlementService.settle(
                        payment1, generateHash(), "bridge-1", 1);
            });

            Future<Transaction> future2 = executor.submit(() -> {
                ready.countDown();
                start.await();
                return settlementService.settle(
                        payment2, generateHash(), "bridge-2", 1);
            });

            // Make both threads start together
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();

            Transaction result1 = future1.get(10, TimeUnit.SECONDS);
            Transaction result2 = future2.get(10, TimeUnit.SECONDS);

            // Exactly one payment should succeed
            long settled = java.util.stream.Stream.of(result1, result2)
                    .filter(tx -> tx.getStatus() == Transaction.Status.SETTLED)
                    .count();

            long rejected = java.util.stream.Stream.of(result1, result2)
                    .filter(tx -> tx.getStatus() == Transaction.Status.REJECTED)
                    .count();

            assertEquals(1, settled);
            assertEquals(1, rejected);

            // Total spending should be ₹23,000
            BigDecimal spent = transactions.getDailySpent(
                    sender,
                    java.time.LocalDate.now(
                                    java.time.ZoneId.of("Asia/Kolkata"))
                            .atStartOfDay(java.time.ZoneId.of("Asia/Kolkata"))
                            .toInstant(),
                    java.time.LocalDate.now(
                                    java.time.ZoneId.of("Asia/Kolkata"))
                            .plusDays(1)
                            .atStartOfDay(java.time.ZoneId.of("Asia/Kolkata"))
                            .toInstant()
            );

            assertEquals(0, new BigDecimal("23000.00").compareTo(spent));

            // Sender should have lost only ₹3,000
            Account updatedSender = accounts.findById(sender).orElseThrow();

            assertEquals(
                    0,
                    new BigDecimal("47000.00")
                            .compareTo(updatedSender.getBalance())
            );

            // The rejected transaction must have the correct reason
            Transaction rejectedTx =
                    result1.getStatus() == Transaction.Status.REJECTED
                            ? result1 : result2;

            assertEquals("DAILY_LIMIT_EXCEEDED", rejectedTx.getReason());

        } finally {
            executor.shutdownNow();
        }
    }

    private PaymentInstruction createPayment() {
        PaymentInstruction instruction = new PaymentInstruction();

        instruction.setSenderVpa(sender);
        instruction.setReceiverVpa(receiver);
        instruction.setAmount(new BigDecimal("3000.00"));
        instruction.setNonce(UUID.randomUUID().toString());
        instruction.setSignedAt(System.currentTimeMillis());

        return instruction;
    }

    private String generateHash() {
        return UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
    }
}