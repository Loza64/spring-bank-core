package com.server.app.domain.dto.account;

import com.server.app.domain.model.enums.AccountType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AccountCreateDto(
    @NotNull AccountType accountType,
    @Pattern(regexp = "^[A-Z]{3}$", message = "debe ser un código ISO de 3 letras, p. ej. USD") String currency,
    // Opcional: solo personal con ACCOUNT_READ_ALL puede abrir cuentas a nombre de otro usuario
    Long userId) {
}
