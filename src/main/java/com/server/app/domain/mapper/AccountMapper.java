package com.server.app.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.server.app.domain.dto.account.AccountResponseDto;
import com.server.app.domain.model.Account;

@Mapper(componentModel = "spring")
public interface AccountMapper {
  @Mapping(target = "userId", source = "user.id")
  AccountResponseDto toResponseDto(Account account);
}
