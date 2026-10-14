package com.server.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.server.app.domain.model.Beneficiary;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

  List<Beneficiary> findByUser_IdOrderByAliasAsc(Long userId);

  Optional<Beneficiary> findByIdAndUser_Id(Long id, Long userId);

  boolean existsByUser_IdAndBankNameIgnoreCaseAndDestinationAccountNumber(Long userId, String bankName,
      String destinationAccountNumber);
}
