package com.server.app.domain.model;

import java.math.BigDecimal;

import org.hibernate.annotations.Check;

import com.server.app.domain.model.base.BaseEntity;
import com.server.app.domain.model.enums.AccountStatus;
import com.server.app.domain.model.enums.AccountType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "accounts", indexes = @Index(name = "idx_accounts_user", columnList = "user_id"))
@Check(constraints = "balance >= 0")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false, unique = true, length = 20, updatable = false)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
