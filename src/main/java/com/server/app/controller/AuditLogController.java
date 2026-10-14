package com.server.app.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.server.app.common.constants.PermissionNames;
import com.server.app.common.pagination.PaginationResponse;
import com.server.app.domain.dto.audit.AuditLogResponseDto;
import com.server.app.service.AuditService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

  private final AuditService auditService;

  @GetMapping
  @PreAuthorize("hasAuthority('" + PermissionNames.AUDIT_READ + "')")
  public ResponseEntity<PaginationResponse<AuditLogResponseDto>> findAll(
      @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    return ResponseEntity.ok(auditService.findAll(pageable));
  }
}
