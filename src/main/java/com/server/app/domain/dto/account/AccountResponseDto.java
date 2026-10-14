package com.server.app.domain.dto.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.server.app.domain.model.enums.AccountStatus;
import com.server.app.domain.model.enums.AccountType;

public record AccountResponseDto(
    Long id,
    String accountNumber,
    AccountType accountType,
    BigDecimal balance,
    String currency,
    AccountStatus status,
    Long userId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
}
