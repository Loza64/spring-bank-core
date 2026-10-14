package com.server.app.domain.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.server.app.domain.model.enums.Channel;
import com.server.app.domain.model.enums.TransactionStatus;
import com.server.app.domain.model.enums.TransactionType;

public record TransactionResponseDto(
    Long id,
    String transactionReference,
    TransactionType type,
    BigDecimal amount,
    BigDecimal fee,
    String sourceAccountNumber,
    String destinationAccountNumber,
    String externalDestinationInfo,
    TransactionStatus status,
    Channel channel,
    String description,
    LocalDateTime createdAt) {
}
