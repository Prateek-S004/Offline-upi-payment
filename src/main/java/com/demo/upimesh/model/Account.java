package com.demo.upimesh.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Simulated bank account. In a real system this would live in the bank's core,
 * not in our service. For the demo, we own the ledger.
 */
@Entity
@Table(name = "accounts")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Account {

    @Id
    private String vpa; // Virtual Payment Address, e.g. "alice@demo"

    @Column(nullable = false)
    private String holderName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false)
    private String pinHash;

    @Version  // Optimistic locking — prevents lost updates on concurrent transfers
    private Long version;

    public Account(String vpa, String holderName, String pinHash, BigDecimal balance) {
        this.vpa = vpa;
        this.holderName = holderName;
        this.balance = balance;
        this.pinHash = pinHash;
    }
}
