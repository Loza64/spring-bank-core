package com.server.app.service;

import java.util.regex.Pattern;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.server.app.common.pagination.PaginationMapper;
import com.server.app.common.pagination.PaginationResponse;
import com.server.app.domain.dto.audit.AuditLogResponseDto;
import com.server.app.domain.mapper.TransactionMapper;
import com.server.app.domain.model.AuditLog;
import com.server.app.domain.model.enums.Channel;
import com.server.app.repository.AuditLogRepository;
import com.server.app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditService {

  private static final int MAX_PAYLOAD = 10_000;

  // Nunca persistir credenciales ni tokens en la auditoría
  private static final Pattern JSON_SECRETS = Pattern.compile(
      "(\"(?:password|currentPassword|newPassword|confirmNewPassword|token|refreshToken|accessToken)\"\\s*:\\s*)\"[^\"]*\"",
      Pattern.CASE_INSENSITIVE);

  private final AuditLogRepository repository;
  private final UserRepository userRepository;
  private final TransactionMapper mapper;
  private final PaginationMapper paginationMapper;

  /**
   * REQUIRES_NEW: el registro de auditoría se guarda aunque la operación de
   * negocio haga rollback (justo los intentos fallidos son los que interesan).
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void record(Channel channel, String endpoint, String requestPayload, String responsePayload,
      String ipAddress, int statusCode, Long userId) {
    AuditLog log = AuditLog.builder()
        .channel(channel)
        .endpoint(truncate(endpoint, 500))
        .requestPayload(truncate(mask(requestPayload), MAX_PAYLOAD))
        .responsePayload(truncate(mask(responsePayload), MAX_PAYLOAD))
        .ipAddress(truncate(ipAddress, 64))
        .statusCode(statusCode)
        .user(userId != null ? userRepository.getReferenceById(userId) : null)
        .build();
    repository.save(log);
  }

  @Transactional(readOnly = true)
  public PaginationResponse<AuditLogResponseDto> findAll(Pageable pageable) {
    return paginationMapper.toPaginationResponse(repository.findAll(pageable).map(mapper::toAuditDto));
  }

  private String mask(String payload) {
    return payload == null ? null : JSON_SECRETS.matcher(payload).replaceAll("$1\"***\"");
  }

  private String truncate(String value, int max) {
    return value == null || value.length() <= max ? value : value.substring(0, max);
  }
}
