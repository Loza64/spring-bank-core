package com.server.app.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.server.app.domain.dto.beneficiary.BeneficiaryCreateDto;
import com.server.app.domain.dto.beneficiary.BeneficiaryResponseDto;
import com.server.app.domain.model.Beneficiary;

@Mapper(componentModel = "spring")
public interface BeneficiaryMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "user", ignore = true)
  Beneficiary toEntity(BeneficiaryCreateDto dto);

  BeneficiaryResponseDto toResponseDto(Beneficiary beneficiary);
}
