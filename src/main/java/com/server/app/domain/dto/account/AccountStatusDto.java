package com.server.app.domain.dto.account;

import com.server.app.domain.model.enums.AccountStatus;

import jakarta.validation.constraints.NotNull;

public record AccountStatusDto(@NotNull AccountStatus status) {
}
