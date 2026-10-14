package com.server.app.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.server.app.domain.dto.audit.AuditLogResponseDto;
import com.server.app.domain.dto.transaction.TransactionResponseDto;
import com.server.app.domain.model.AuditLog;
import com.server.app.domain.model.Transaction;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
  @Mapping(target = "sourceAccountNumber", source = "sourceAccount.accountNumber")
  @Mapping(target = "destinationAccountNumber", source = "destinationAccount.accountNumber")
  TransactionResponseDto toResponseDto(Transaction transaction);

  @Mapping(target = "userId", source = "user.id")
  AuditLogResponseDto toAuditDto(AuditLog log);
}
