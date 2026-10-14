package com.server.app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.server.app.common.constants.PermissionNames;
import com.server.app.domain.dto.auth.ProfileResponseDto;
import com.server.app.domain.dto.beneficiary.BeneficiaryCreateDto;
import com.server.app.domain.dto.beneficiary.BeneficiaryResponseDto;
import com.server.app.service.BeneficiaryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/beneficiaries")
@RequiredArgsConstructor
public class BeneficiaryController {

  private final BeneficiaryService beneficiaryService;

  @GetMapping
  @PreAuthorize("hasAuthority('" + PermissionNames.BENEFICIARY_READ + "')")
  public ResponseEntity<List<BeneficiaryResponseDto>> findMine(@AuthenticationPrincipal ProfileResponseDto me) {
    return ResponseEntity.ok(beneficiaryService.findMine(me.id()));
  }

  @PostMapping
  @PreAuthorize("hasAuthority('" + PermissionNames.BENEFICIARY_CREATE + "')")
  public ResponseEntity<BeneficiaryResponseDto> create(
      @Valid @RequestBody BeneficiaryCreateDto dto,
      @AuthenticationPrincipal ProfileResponseDto me) {
    return ResponseEntity.status(HttpStatus.CREATED).body(beneficiaryService.create(me.id(), dto));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('" + PermissionNames.BENEFICIARY_DELETE + "')")
  public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal ProfileResponseDto me) {
    beneficiaryService.delete(me.id(), id);
    return ResponseEntity.noContent().build();
  }
}
