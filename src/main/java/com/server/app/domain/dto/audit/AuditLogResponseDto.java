package com.server.app.domain.dto.audit;

import java.time.LocalDateTime;

import com.server.app.domain.model.enums.Channel;

public record AuditLogResponseDto(
    Long id,
    Channel channel,
    String endpoint,
    String requestPayload,
    String responsePayload,
    String ipAddress,
    Integer statusCode,
    Long userId,
    LocalDateTime createdAt) {
}
