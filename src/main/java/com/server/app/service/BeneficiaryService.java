package com.server.app.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.server.app.common.exceptions.ConfictException;
import com.server.app.common.exceptions.NotFoundException;
import com.server.app.domain.dto.beneficiary.BeneficiaryCreateDto;
import com.server.app.domain.dto.beneficiary.BeneficiaryResponseDto;
import com.server.app.domain.mapper.BeneficiaryMapper;
import com.server.app.domain.model.Beneficiary;
import com.server.app.repository.BeneficiaryRepository;
import com.server.app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BeneficiaryService {

  private final BeneficiaryRepository repository;
  private final UserRepository userRepository;
  private final BeneficiaryMapper mapper;

  @Transactional
  public BeneficiaryResponseDto create(Long userId, BeneficiaryCreateDto dto) {
    if (repository.existsByUser_IdAndBankNameIgnoreCaseAndDestinationAccountNumber(
        userId, dto.bankName(), dto.destinationAccountNumber())) {
      throw new ConfictException("Ya tienes registrado un beneficiario con esa cuenta en ese banco");
    }

    Beneficiary beneficiary = mapper.toEntity(dto);
    beneficiary.setUser(userRepository.getReferenceById(userId));
    return mapper.toResponseDto(repository.save(beneficiary));
  }

  @Transactional(readOnly = true)
  public List<BeneficiaryResponseDto> findMine(Long userId) {
    return repository.findByUser_IdOrderByAliasAsc(userId).stream().map(mapper::toResponseDto).toList();
  }

  @Transactional
  public void delete(Long userId, Long id) {
    Beneficiary beneficiary = repository.findByIdAndUser_Id(id, userId)
        .orElseThrow(() -> new NotFoundException("Beneficiario no encontrado con ID: " + id));
    repository.delete(beneficiary);
  }
}
