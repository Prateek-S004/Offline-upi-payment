package com.demo.upimesh.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findTop20ByOrderByIdDesc();
    boolean existsByPacketHash(String packetHash);
    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM Transaction t
    WHERE t.senderVpa = :senderVpa
    AND t.status = 'SETTLED'
    AND t.settledAt >= :start
    AND t.settledAt < :end
""")
    BigDecimal getDailySpent(
            @Param("senderVpa") String senderVpa,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    List<Transaction> findTop10BySenderVpaOrReceiverVpaOrderByIdDesc(
            String senderVpa,
            String receiverVpa
    );

    List<Transaction> findTop5BySenderVpaOrderByIdDesc(
            String senderVpa
    );

    List<Transaction> findTop5ByReceiverVpaOrderByIdDesc(
            String receiverVpa
    );
}
