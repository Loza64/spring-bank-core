package com.server.app.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.server.app.common.constants.PermissionNames;
import com.server.app.common.pagination.PaginationResponse;
import com.server.app.common.security.AuthUtils;
import com.server.app.domain.dto.auth.ProfileResponseDto;
import com.server.app.domain.dto.transaction.CashOperationDto;
import com.server.app.domain.dto.transaction.TransactionResponseDto;
import com.server.app.domain.dto.transaction.TransferRequestDto;
import com.server.app.domain.model.enums.Channel;
import com.server.app.service.TransactionService;
import com.server.app.service.TransactionService.TransferCommand;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

  private final TransactionService transactionService;

  /** Transferencia entre cuentas propias o a cuentas internas de terceros. */
  @PostMapping("/transfer")
  @PreAuthorize("hasAuthority('" + PermissionNames.TRANSFER_CREATE + "')")
  public ResponseEntity<TransactionResponseDto> transfer(
      @Valid @RequestBody TransferRequestDto dto,
      @AuthenticationPrincipal ProfileResponseDto me) {
    TransferCommand cmd = new TransferCommand(
        dto.sourceAccountNumber(), dto.destinationAccountNumber(), dto.amount(),
        dto.description(), dto.reference(), Channel.REST, me.id(), true);
    return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.transfer(cmd));
  }

  /** Operaciones de ventanilla (personal del banco). */
  @PostMapping("/deposit")
  @PreAuthorize("hasAuthority('" + PermissionNames.TRANSACTION_CREATE + "')")
  public ResponseEntity<TransactionResponseDto> deposit(
      @Valid @RequestBody CashOperationDto dto,
      @AuthenticationPrincipal ProfileResponseDto me) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(transactionService.deposit(dto.accountNumber(), dto.amount(), dto.description(), Channel.REST, me.id()));
  }

  @PostMapping("/withdraw")
  @PreAuthorize("hasAuthority('" + PermissionNames.TRANSACTION_CREATE + "')")
  public ResponseEntity<TransactionResponseDto> withdraw(
      @Valid @RequestBody CashOperationDto dto,
      @AuthenticationPrincipal ProfileResponseDto me) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(transactionService.withdraw(dto.accountNumber(), dto.amount(), dto.description(), Channel.REST, me.id()));
  }

  /** Movimientos en los que participa alguna cuenta del usuario autenticado. */
  @GetMapping("/me")
  @PreAuthorize("hasAuthority('" + PermissionNames.TRANSACTION_READ + "')")
  public ResponseEntity<PaginationResponse<TransactionResponseDto>> findMine(
      @AuthenticationPrincipal ProfileResponseDto me,
      @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    return ResponseEntity.ok(transactionService.findMine(me.id(), pageable));
  }

  @GetMapping
  @PreAuthorize("hasAuthority('" + PermissionNames.TRANSACTION_READ_ALL + "')")
  public ResponseEntity<PaginationResponse<TransactionResponseDto>> findAll(
      @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    return ResponseEntity.ok(transactionService.findAll(pageable));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('" + PermissionNames.TRANSACTION_READ + "')")
  public ResponseEntity<TransactionResponseDto> findById(
      @PathVariable Long id,
      @AuthenticationPrincipal ProfileResponseDto me,
      Authentication auth) {
    boolean staff = AuthUtils.hasAuthority(auth, PermissionNames.TRANSACTION_READ_ALL);
    return ResponseEntity.ok(transactionService.findById(id, me.id(), staff));
  }
}
