package com.server.app.soap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import com.server.app.common.exceptions.NotFoundException;
import com.server.app.common.pagination.PaginationResponse;
import com.server.app.common.pagination.PaginationResponse.PaginationMetadata;
import com.server.app.domain.dto.account.AccountResponseDto;
import com.server.app.domain.dto.transaction.TransactionResponseDto;
import com.server.app.domain.model.enums.AccountStatus;
import com.server.app.domain.model.enums.AccountType;
import com.server.app.domain.model.enums.Channel;
import com.server.app.domain.model.enums.TransactionStatus;
import com.server.app.domain.model.enums.TransactionType;
import com.server.app.service.AccountService;
import com.server.app.service.TransactionService;
import com.server.app.service.TransactionService.TransferCommand;
import com.server.app.soap.endpoint.BankingEndpoint;
import com.server.app.soap.fault.SoapError;
import com.server.app.soap.mapper.BankingSoapMapper;
import com.server.app.soap.model.GetAccountStatementRequest;
import com.server.app.soap.model.GetAccountStatementResponse;
import com.server.app.soap.model.GetBalanceRequest;
import com.server.app.soap.model.GetBalanceResponse;
import com.server.app.soap.model.GetTransactionStatusRequest;
import com.server.app.soap.model.GetTransactionStatusResponse;
import com.server.app.soap.model.TransferRequest;
import com.server.app.soap.model.TransferResponse;
import com.server.app.soap.model.ValidateAccountRequest;
import com.server.app.soap.model.ValidateAccountResponse;

@ExtendWith(MockitoExtension.class)
class BankingEndpointTest {

  private static final String ACCOUNT = "024907063042";
  private static final String OTHER = "111111111111";
  private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 14, 12, 0);

  @Mock
  private AccountService accountService;
  @Mock
  private TransactionService transactionService;

  private BankingEndpoint endpoint;

  @BeforeEach
  void setUp() {
    endpoint = new BankingEndpoint(accountService, transactionService, new BankingSoapMapper());
  }

  // ── GetBalance ──────────────────────────────────────────────────────────

  @Test
  void getBalance_devuelveLosDatosDeLaCuenta() {
    when(accountService.findByAccountNumber(ACCOUNT)).thenReturn(account(AccountStatus.ACTIVE));

    GetBalanceRequest request = new GetBalanceRequest();
    request.setAccountNumber(ACCOUNT);

    GetBalanceResponse response = endpoint.getBalance(request);

    assertThat(response.getAccountNumber()).isEqualTo(ACCOUNT);
    assertThat(response.getBalance()).isEqualByComparingTo("150.00");
    assertThat(response.getStatus()).isEqualTo("ACTIVE");
  }

  // ── ValidateAccount ─────────────────────────────────────────────────────

  @Test
  void validateAccount_cuentaExistente() {
    when(accountService.findOptionalByAccountNumber(ACCOUNT))
        .thenReturn(Optional.of(account(AccountStatus.FROZEN)));

    ValidateAccountResponse response = endpoint.validateAccount(validateRequest(ACCOUNT));

    assertThat(response.isExists()).isTrue();
    assertThat(response.getStatus()).isEqualTo("FROZEN");
    assertThat(response.getCurrency()).isEqualTo("USD");
  }

  @Test
  void validateAccount_cuentaInexistenteNoEsError() {
    when(accountService.findOptionalByAccountNumber(OTHER)).thenReturn(Optional.empty());

    ValidateAccountResponse response = endpoint.validateAccount(validateRequest(OTHER));

    assertThat(response.isExists()).isFalse();
    assertThat(response.getStatus()).isNull();
    assertThat(response.getCurrency()).isNull();
  }

  // ── Transfer ────────────────────────────────────────────────────────────

  @Test
  void transfer_usaCanalSoapYNoExigeDuenio() {
    when(transactionService.transfer(any())).thenReturn(transaction("TX-1", TransactionType.TRANSFER));

    TransferRequest request = new TransferRequest();
    request.setSourceAccountNumber(ACCOUNT);
    request.setDestinationAccountNumber(OTHER);
    request.setAmount(new BigDecimal("10.00"));
    request.setReference("TX-1");

    TransferResponse response = endpoint.transfer(request);

    ArgumentCaptor<TransferCommand> captor = ArgumentCaptor.forClass(TransferCommand.class);
    verify(transactionService).transfer(captor.capture());
    assertThat(captor.getValue().channel()).isEqualTo(Channel.SOAP);
    assertThat(captor.getValue().enforceOwnership()).isFalse();
    assertThat(response.getTransactionReference()).isEqualTo("TX-1");
    assertThat(response.getCreatedAt()).isNotNull();
  }

  // ── GetTransactionStatus ────────────────────────────────────────────────

  @Test
  void getTransactionStatus_devuelveElMovimiento() {
    when(transactionService.findByReference("TX-1")).thenReturn(transaction("TX-1", TransactionType.TRANSFER));

    GetTransactionStatusRequest request = new GetTransactionStatusRequest();
    request.setReference("TX-1");

    GetTransactionStatusResponse response = endpoint.getTransactionStatus(request);

    assertThat(response.getTransaction().getTransactionReference()).isEqualTo("TX-1");
    assertThat(response.getTransaction().getType()).isEqualTo("TRANSFER");
    assertThat(response.getTransaction().getStatus()).isEqualTo("COMPLETED");
    assertThat(response.getTransaction().getChannel()).isEqualTo("SOAP");
  }

  // ── GetAccountStatement ─────────────────────────────────────────────────

  @Test
  void getAccountStatement_sinPaginarUsaPagina1Tamano20() {
    when(transactionService.findByAccountNumber(any(), any())).thenReturn(page(List.of(), 0));

    GetAccountStatementRequest request = new GetAccountStatementRequest();
    request.setAccountNumber(ACCOUNT);

    endpoint.getAccountStatement(request);

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(transactionService).findByAccountNumber(any(), captor.capture());
    // SOAP cuenta desde 1; Spring desde 0
    assertThat(captor.getValue().getPageNumber()).isZero();
    assertThat(captor.getValue().getPageSize()).isEqualTo(20);
  }

  @Test
  void getAccountStatement_traduceLaPagina() {
    List<TransactionResponseDto> txs = List.of(
        transaction("TX-1", TransactionType.TRANSFER),
        transaction("TX-2", TransactionType.DEPOSIT));
    when(transactionService.findByAccountNumber(any(), any())).thenReturn(page(txs, 2));

    GetAccountStatementRequest request = new GetAccountStatementRequest();
    request.setAccountNumber(ACCOUNT);
    request.setPage(1);
    request.setSize(10);

    GetAccountStatementResponse response = endpoint.getAccountStatement(request);

    assertThat(response.getAccountNumber()).isEqualTo(ACCOUNT);
    assertThat(response.getTotal()).isEqualTo(2);
    assertThat(response.getTransactions()).extracting("transactionReference").containsExactly("TX-1", "TX-2");
  }

  // ── Errores ─────────────────────────────────────────────────────────────

  @Test
  void soapError_clasificaBienLasExcepciones() {
    assertThat(SoapError.from(new NotFoundException("x"))).isEqualTo(SoapError.NOT_FOUND);
    assertThat(SoapError.from(new NullPointerException())).isEqualTo(SoapError.INTERNAL_ERROR);
    assertThat(SoapError.INTERNAL_ERROR.isClientFault()).isFalse();
  }

  // ── helpers ─────────────────────────────────────────────────────────────

  private ValidateAccountRequest validateRequest(String number) {
    ValidateAccountRequest request = new ValidateAccountRequest();
    request.setAccountNumber(number);
    return request;
  }

  private AccountResponseDto account(AccountStatus status) {
    return new AccountResponseDto(1L, ACCOUNT, AccountType.SAVINGS, new BigDecimal("150.00"), "USD",
        status, 1L, NOW, NOW);
  }

  private TransactionResponseDto transaction(String reference, TransactionType type) {
    return new TransactionResponseDto(1L, reference, type, new BigDecimal("10.00"), BigDecimal.ZERO,
        ACCOUNT, OTHER, null, TransactionStatus.COMPLETED, Channel.SOAP, "pago", NOW);
  }

  private PaginationResponse<TransactionResponseDto> page(List<TransactionResponseDto> data, long total) {
    return new PaginationResponse<>(data, new PaginationMetadata(1, 20, 1, total));
  }
}
