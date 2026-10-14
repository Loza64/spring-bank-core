package com.server.app.domain.dto.transaction;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TransferRequestDto(
    @NotBlank String sourceAccountNumber,
    @NotBlank String destinationAccountNumber,
    @NotNull @DecimalMin(value = "0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount,
    @Size(max = 255) String description,
    // Opcional: clave de idempotencia. Reenviar la misma referencia no duplica el movimiento.
    @Size(max = 64) String reference) {
}
