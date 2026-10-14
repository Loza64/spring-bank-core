package com.server.app.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.server.app.common.exceptions.BadRequestException;
import com.server.app.common.exceptions.ConfictException;
import com.server.app.common.exceptions.NotFoundException;
import com.server.app.common.pagination.PaginationMapper;
import com.server.app.common.pagination.PaginationResponse;
import com.server.app.domain.dto.transaction.TransactionResponseDto;
import com.server.app.domain.mapper.TransactionMapper;
import com.server.app.domain.model.Account;
import com.server.app.domain.model.Transaction;
import com.server.app.domain.model.enums.AccountStatus;
import com.server.app.domain.model.enums.Channel;
import com.server.app.domain.model.enums.TransactionStatus;
import com.server.app.domain.model.enums.TransactionType;
import com.server.app.repository.AccountRepository;
import com.server.app.repository.TransactionRepository;
import com.server.app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Capa transaccional ÚNICA para mover dinero. Tanto los controladores REST como
 * los endpoints SOAP pasan por aquí, así que las reglas de negocio y la
 * atomicidad son idénticas sin importar el canal.
 *
 * Estrategia transaccional:
 * - Por defecto TODO el servicio es de solo lectura (readOnly = true): Spring
 *   optimiza la sesión y la base de datos rechaza cualquier escritura accidental.
 * - Los métodos que mueven dinero la sobrescriben con una transacción de
 *   escritura. Si lanzan CUALQUIER excepción (BadRequest, Conflict, NotFound,
 *   errores de BD, etc.) Spring hace rollback automático de todo: saldos y
 *   registro de la transacción. No hay try/catch ni rollback manual.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;
  private final UserRepository userRepository;
  private final TransactionMapper mapper;
  private final PaginationMapper paginationMapper;

  /**
   * @param initiatorUserId  usuario que origina la operación; null en SOAP máquina-a-máquina
   * @param enforceOwnership si true, la cuenta origen debe pertenecer a initiatorUserId
   * @param reference        clave de idempotencia opcional
   */
  public record TransferCommand(
      String sourceAccountNumber,
      String destinationAccountNumber,
      BigDecimal amount,
      String description,
      String reference,
      Channel channel,
      Long initiatorUserId,
      boolean enforceOwnership) {
  }

  /**
   * REQUIRED: se une a la transacción existente o crea una nueva.
   * READ_COMMITTED + bloqueo pesimista de filas: evita lecturas sucias y lost update.
   * rollbackFor = Exception.class: rollback también ante excepciones checked.
   * timeout: si no obtiene los bloqueos a tiempo, aborta y hace rollback.
   */
  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class, timeout = 15)
  public TransactionResponseDto transfer(TransferCommand cmd) {
    validateAmount(cmd.amount());

    if (cmd.sourceAccountNumber().equals(cmd.destinationAccountNumber())) {
      throw new BadRequestException("La cuenta origen y destino no pueden ser la misma");
    }

    // Idempotencia: la misma referencia devuelve el movimiento original
    if (cmd.reference() != null) {
      Optional<Transaction> existing = transactionRepository.findByTransactionReference(cmd.reference());
      if (existing.isPresent()) {
        Transaction tx = existing.get();
        boolean sameOperation = tx.getType() == TransactionType.TRANSFER
            && tx.getSourceAccount() != null
            && tx.getSourceAccount().getAccountNumber().equals(cmd.sourceAccountNumber())
            && tx.getAmount().compareTo(cmd.amount()) == 0;
        if (!sameOperation) {
          throw new ConfictException("La referencia ya fue utilizada en otra operación");
        }
        return mapper.toResponseDto(tx);
      }
    }

    // Solo ids: las entidades se cargan DESPUÉS de tomar el bloqueo para ver el saldo vigente
    Long srcId = accountRepository.findIdByAccountNumber(cmd.sourceAccountNumber())
        .orElseThrow(() -> new NotFoundException("Cuenta origen no encontrada"));
    Long dstId = accountRepository.findIdByAccountNumber(cmd.destinationAccountNumber())
        .orElseThrow(() -> new NotFoundException("Cuenta destino no encontrada"));

    Map<Long, Account> locked = lock(srcId, dstId);
    Account src = locked.get(srcId);
    Account dst = locked.get(dstId);

    // 404 en vez de 403: no revelamos si la cuenta existe a quien no es su dueño
    if (cmd.enforceOwnership() && !src.getUser().getId().equals(cmd.initiatorUserId())) {
      throw new NotFoundException("Cuenta origen no encontrada");
    }

    assertActive(src, "origen");
    assertActive(dst, "destino");

    if (!src.getCurrency().equals(dst.getCurrency())) {
      throw new BadRequestException("Las cuentas tienen monedas distintas; no hay conversión automática");
    }

    BigDecimal fee = BigDecimal.ZERO; // punto de extensión para tarifas
    BigDecimal totalDebit = cmd.amount().add(fee);

    if (src.getBalance().compareTo(totalDebit) < 0) {
      throw new ConfictException("Fondos insuficientes");
    }

    // Entidades gestionadas: Hibernate hace el UPDATE al confirmar (dirty checking).
    // Si algo falla después de estas líneas, el rollback deshace ambos saldos.
    src.setBalance(src.getBalance().subtract(totalDebit));
    dst.setBalance(dst.getBalance().add(cmd.amount()));

    Transaction tx = Transaction.builder()
        .transactionReference(cmd.reference() != null ? cmd.reference() : newReference())
        .type(TransactionType.TRANSFER)
        .amount(cmd.amount())
        .fee(fee)
        .sourceAccount(src)
        .destinationAccount(dst)
        .status(TransactionStatus.COMPLETED)
        .channel(cmd.channel())
        .description(cmd.description())
        .user(cmd.initiatorUserId() != null ? userRepository.getReferenceById(cmd.initiatorUserId()) : null)
        .build();

    return mapper.toResponseDto(transactionRepository.save(tx));
  }

  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class, timeout = 15)
  public TransactionResponseDto deposit(String accountNumber, BigDecimal amount, String description,
      Channel channel, Long operatorId) {
    validateAmount(amount);

    Account account = lockByNumber(accountNumber);
    assertActive(account, "destino");

    account.setBalance(account.getBalance().add(amount));

    Transaction tx = Transaction.builder()
        .transactionReference(newReference())
        .type(TransactionType.DEPOSIT)
        .amount(amount)
        .destinationAccount(account)
        .status(TransactionStatus.COMPLETED)
        .channel(channel)
        .description(description)
        .user(operatorId != null ? userRepository.getReferenceById(operatorId) : null)
        .build();

    return mapper.toResponseDto(transactionRepository.save(tx));
  }

  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class, timeout = 15)
  public TransactionResponseDto withdraw(String accountNumber, BigDecimal amount, String description,
      Channel channel, Long operatorId) {
    validateAmount(amount);

    Account account = lockByNumber(accountNumber);
    assertActive(account, "origen");

    if (account.getBalance().compareTo(amount) < 0) {
      throw new ConfictException("Fondos insuficientes");
    }
    account.setBalance(account.getBalance().subtract(amount));

    Transaction tx = Transaction.builder()
        .transactionReference(newReference())
        .type(TransactionType.WITHDRAWAL)
        .amount(amount)
        .sourceAccount(account)
        .status(TransactionStatus.COMPLETED)
        .channel(channel)
        .description(description)
        .user(operatorId != null ? userRepository.getReferenceById(operatorId) : null)
        .build();

    return mapper.toResponseDto(transactionRepository.save(tx));
  }

  // ── Lecturas: heredan readOnly = true del nivel de clase ────────────────

  public PaginationResponse<TransactionResponseDto> findMine(Long userId, Pageable pageable) {
    Page<Transaction> page = transactionRepository.findAllByParticipantUser(userId, pageable);
    return paginationMapper.toPaginationResponse(page.map(mapper::toResponseDto));
  }

  public PaginationResponse<TransactionResponseDto> findAll(Pageable pageable) {
    return paginationMapper.toPaginationResponse(transactionRepository.findAll(pageable).map(mapper::toResponseDto));
  }

  public TransactionResponseDto findById(Long id, Long requesterId, boolean staff) {
    Transaction tx = transactionRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Transacción no encontrada con ID: " + id));

    if (!staff) {
      boolean participant = isOwner(tx.getSourceAccount(), requesterId) || isOwner(tx.getDestinationAccount(), requesterId);
      if (!participant) {
        throw new NotFoundException("Transacción no encontrada con ID: " + id);
      }
    }
    return mapper.toResponseDto(tx);
  }

  // ── helpers ──────────────────────────────────────────────────────────────
  private Map<Long, Account> lock(Long... ids) {
    List<Account> accounts = accountRepository.lockAllByIdIn(List.of(ids));
    return accounts.stream().collect(Collectors.toMap(Account::getId, Function.identity()));
  }

  private Account lockByNumber(String accountNumber) {
    Long id = accountRepository.findIdByAccountNumber(accountNumber)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada: " + accountNumber));
    return lock(id).get(id);
  }

  private boolean isOwner(Account account, Long userId) {
    return account != null && account.getUser().getId().equals(userId);
  }

  private void validateAmount(BigDecimal amount) {
    if (amount == null || amount.signum() <= 0) {
      throw new BadRequestException("El monto debe ser mayor que cero");
    }
    if (amount.scale() > 2) {
      throw new BadRequestException("El monto admite como máximo 2 decimales");
    }
  }

  private void assertActive(Account account, String role) {
    if (account.getStatus() != AccountStatus.ACTIVE) {
      throw new ConfictException("La cuenta " + role + " no está activa (estado: " + account.getStatus() + ")");
    }
  }

  private String newReference() {
    return "TX-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
  }
}