package com.server.app.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.server.app.common.constants.PermissionNames;
import com.server.app.common.pagination.PaginationResponse;
import com.server.app.common.security.AuthUtils;
import com.server.app.domain.dto.account.AccountCreateDto;
import com.server.app.domain.dto.account.AccountResponseDto;
import com.server.app.domain.dto.account.AccountStatusDto;
import com.server.app.domain.dto.auth.ProfileResponseDto;
import com.server.app.service.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

  private final AccountService accountService;

  @PostMapping
  @PreAuthorize("hasAuthority('" + PermissionNames.ACCOUNT_CREATE + "')")
  public ResponseEntity<AccountResponseDto> create(
      @Valid @RequestBody AccountCreateDto dto,
      @AuthenticationPrincipal ProfileResponseDto me,
      Authentication auth) {
    boolean staff = AuthUtils.hasAuthority(auth, PermissionNames.ACCOUNT_READ_ALL);
    return ResponseEntity.status(HttpStatus.CREATED).body(accountService.create(dto, me.id(), staff));
  }

  /** Cuentas del usuario autenticado. */
  @GetMapping("/me")
  @PreAuthorize("hasAuthority('" + PermissionNames.ACCOUNT_READ + "')")
  public ResponseEntity<PaginationResponse<AccountResponseDto>> findMine(
      @AuthenticationPrincipal ProfileResponseDto me,
      @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    return ResponseEntity.ok(accountService.findMine(me.id(), pageable));
  }

  /** Listado global (personal del banco). */
  @GetMapping
  @PreAuthorize("hasAuthority('" + PermissionNames.ACCOUNT_READ_ALL + "')")
  public ResponseEntity<PaginationResponse<AccountResponseDto>> findAll(
      @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    return ResponseEntity.ok(accountService.findAll(pageable));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('" + PermissionNames.ACCOUNT_READ + "')")
  public ResponseEntity<AccountResponseDto> findById(
      @PathVariable Long id,
      @AuthenticationPrincipal ProfileResponseDto me,
      Authentication auth) {
    boolean staff = AuthUtils.hasAuthority(auth, PermissionNames.ACCOUNT_READ_ALL);
    return ResponseEntity.ok(accountService.findById(id, me.id(), staff));
  }

  @PatchMapping("/{id}/status")
  @PreAuthorize("hasAuthority('" + PermissionNames.ACCOUNT_UPDATE + "')")
  public ResponseEntity<AccountResponseDto> changeStatus(
      @PathVariable Long id,
      @Valid @RequestBody AccountStatusDto dto) {
    return ResponseEntity.ok(accountService.changeStatus(id, dto.status()));
  }
}
