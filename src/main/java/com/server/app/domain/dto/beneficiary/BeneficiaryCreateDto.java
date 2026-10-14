package com.server.app.domain.dto.beneficiary;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BeneficiaryCreateDto(
    @NotBlank @Size(max = 100) String alias,
    @NotBlank @Size(max = 100) String bankName,
    @NotBlank @Size(max = 34) String destinationAccountNumber,
    @Size(max = 50) String destinationDocumentNumber,
    @Email String email) {
}
