package dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionReceipt {
    private Long transactionId;
    private String senderVpa;
    private String receiverVpa;
    private BigDecimal amount;
    private String status;
    private Instant timestamp;
    private String reason;
}
