package com.server.app.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.server.app.domain.model.enums.Channel;
import com.server.app.domain.model.enums.TransactionStatus;
import com.server.app.domain.model.enums.TransactionType;

import jakarta.persistence.*;
import lombok.*;

/**
 * Registro inmutable de movimientos de dinero. No se actualiza ni se borra:
 * las correcciones se hacen con una transacción nueva (p. ej. REVERSED).
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_tx_source", columnList = "source_account_id"),
    @Index(name = "idx_tx_destination", columnList = "destination_account_id"),
    @Index(name = "idx_tx_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "transaction_reference", nullable = false, unique = true, updatable = false, length = 64)
  private String transactionReference;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, updatable = false, length = 20)
  private TransactionType type;

  @Column(nullable = false, updatable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, updatable = false, precision = 19, scale = 2)
  @Builder.Default
  private BigDecimal fee = BigDecimal.ZERO;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_account_id", updatable = false)
  private Account sourceAccount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "destination_account_id", updatable = false)
  private Account destinationAccount;

  @Column(name = "external_destination_info")
  private String externalDestinationInfo;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TransactionStatus status;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, updatable = false, length = 10)
  private Channel channel;

  private String description;

  // Nullable: las operaciones SOAP de sistemas externos no tienen usuario humano
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", updatable = false)
  private User user;

  @Column(name = "created_at", nullable = false, updatable = false)
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();
}
