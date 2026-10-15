package com.server.app.service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.server.app.common.exceptions.ConfictException;
import com.server.app.common.exceptions.ForbiddenException;
import com.server.app.common.exceptions.NotFoundException;
import com.server.app.common.pagination.PaginationMapper;
import com.server.app.common.pagination.PaginationResponse;
import com.server.app.domain.dto.account.AccountCreateDto;
import com.server.app.domain.dto.account.AccountResponseDto;
import com.server.app.domain.mapper.AccountMapper;
import com.server.app.domain.model.Account;
import com.server.app.domain.model.User;
import com.server.app.domain.model.enums.AccountStatus;
import com.server.app.repository.AccountRepository;
import com.server.app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

  private static final String DEFAULT_CURRENCY = "USD";
  private static final SecureRandom RANDOM = new SecureRandom();

  private final AccountRepository repository;
  private final UserRepository userRepository;
  private final AccountMapper mapper;
  private final PaginationMapper paginationMapper;

  @Transactional
  public AccountResponseDto create(AccountCreateDto dto, Long requesterId, boolean staff) {
    Long ownerId = dto.userId() != null ? dto.userId() : requesterId;

    if (!ownerId.equals(requesterId) && !staff) {
      throw new ForbiddenException("No puedes abrir cuentas a nombre de otro usuario");
    }

    User owner = userRepository.findById(ownerId)
        .orElseThrow(() -> new NotFoundException("Usuario no encontrado con ID: " + ownerId));

    if (owner.isBlocked() || owner.getDeletedAt() != null) {
      throw new ConfictException("El usuario está bloqueado o eliminado, no puede abrir cuentas");
    }

    Account account = Account.builder()
        .accountNumber(generateAccountNumber())
        .accountType(dto.accountType())
        .currency(dto.currency() != null ? dto.currency() : DEFAULT_CURRENCY)
        .balance(BigDecimal.ZERO)
        .status(AccountStatus.ACTIVE)
        .user(owner)
        .build();

    return mapper.toResponseDto(repository.save(account));
  }

  /** Cuenta por id; un cliente solo puede ver las suyas (404 para no revelar existencia). */
  @Transactional(readOnly = true)
  public AccountResponseDto findById(Long id, Long requesterId, boolean staff) {
    Account account = repository.findById(id)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con ID: " + id));

    if (!staff && !account.getUser().getId().equals(requesterId)) {
      throw new NotFoundException("Cuenta no encontrada con ID: " + id);
    }
    return mapper.toResponseDto(account);
  }

  /** Uso interno del canal SOAP (cliente máquina autenticado, sin usuario humano). */
  @Transactional(readOnly = true)
  public AccountResponseDto findByAccountNumber(String accountNumber) {
    return findOptionalByAccountNumber(accountNumber)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada: " + accountNumber));
  }

  /** Igual que la anterior pero sin lanzar 404; para validar si una cuenta existe. */
  @Transactional(readOnly = true)
  public Optional<AccountResponseDto> findOptionalByAccountNumber(String accountNumber) {
    return repository.findByAccountNumber(accountNumber).map(mapper::toResponseDto);
  }

  @Transactional(readOnly = true)
  public PaginationResponse<AccountResponseDto> findMine(Long userId, Pageable pageable) {
    Page<Account> page = repository.findByUser_Id(userId, pageable);
    return paginationMapper.toPaginationResponse(page.map(mapper::toResponseDto));
  }

  @Transactional(readOnly = true)
  public PaginationResponse<AccountResponseDto> findAll(Pageable pageable) {
    return paginationMapper.toPaginationResponse(repository.findAll(pageable).map(mapper::toResponseDto));
  }

  @Transactional
  public AccountResponseDto changeStatus(Long id, AccountStatus newStatus) {
    // Se bloquea la fila: evita cerrar una cuenta mientras una transferencia la mueve
    Account account = repository.lockAllByIdIn(java.util.List.of(id)).stream().findFirst()
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con ID: " + id));

    if (account.getStatus() == AccountStatus.CLOSED) {
      throw new ConfictException("La cuenta ya está cerrada y no puede cambiar de estado");
    }
    if (newStatus == AccountStatus.CLOSED && account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
      throw new ConfictException("No se puede cerrar una cuenta con saldo distinto de cero");
    }

    account.setStatus(newStatus);
    return mapper.toResponseDto(repository.save(account));
  }

  private String generateAccountNumber() {
    String number;
    do {
      number = String.format("%012d", RANDOM.nextLong(1_000_000_000_000L));
    } while (repository.existsByAccountNumber(number));
    return number;
  }
}
