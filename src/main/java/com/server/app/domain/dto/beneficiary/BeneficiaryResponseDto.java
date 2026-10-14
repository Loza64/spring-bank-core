package com.server.app.domain.dto.beneficiary;

public record BeneficiaryResponseDto(
    Long id,
    String alias,
    String bankName,
    String destinationAccountNumber,
    String destinationDocumentNumber,
    String email) {
}
