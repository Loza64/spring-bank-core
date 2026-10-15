package com.server.app.soap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.server.app.domain.dto.account.AccountResponseDto;
import com.server.app.domain.dto.transaction.TransactionResponseDto;
import com.server.app.domain.model.enums.AccountStatus;
import com.server.app.domain.model.enums.AccountType;
import com.server.app.domain.model.enums.Channel;
import com.server.app.domain.model.enums.TransactionStatus;
import com.server.app.service.AccountService;
import com.server.app.service.TransactionService;
import com.server.app.service.TransactionService.TransferCommand;
import com.server.app.soap.endpoint.BankingEndpoint;
import com.server.app.soap.fault.SoapError;
import com.server.app.soap.mapper.BankingSoapMapper;
import com.server.app.soap.model.GetBalanceRequest;
import com.server.app.soap.model.GetBalanceResponse;
import com.server.app.soap.model.TransferRequest;
import com.server.app.soap.model.TransferResponse;
import com.server.app.common.exceptions.NotFoundException;

@ExtendWith(MockitoExtension.class)
class BankingEndpointTest {

  private static final String ACCOUNT = "024907063042";

  @Mock
  private AccountService accountService;
  @Mock
  private TransactionService transactionService;

  private BankingEndpoint endpoint;

  @BeforeEach
  void setUp() {
    endpoint = new BankingEndpoint(accountService, transactionService, new BankingSoapMapper());
  }

  @Test
  void getBalance_devuelveLosDatosDeLaCuenta() {
    when(accountService.findByAccountNumber(ACCOUNT)).thenReturn(new AccountResponseDto(
        1L, ACCOUNT, AccountType.SAVINGS, new BigDecimal("150.00"), "USD",
        AccountStatus.ACTIVE, 1L, LocalDateTime.now(), LocalDateTime.now()));

    GetBalanceRequest request = new GetBalanceRequest();
    request.setAccountNumber(ACCOUNT);

    GetBalanceResponse response = endpoint.getBalance(request);

    assertThat(response.getAccountNumber()).isEqualTo(ACCOUNT);
    assertThat(response.getBalance()).isEqualByComparingTo("150.00");
    assertThat(response.getStatus()).isEqualTo("ACTIVE");
  }

  @Test
  void transfer_usaCanalSoapYNoExigeDuenio() {
    when(transactionService.transfer(any())).thenReturn(new TransactionResponseDto(
        1L, "TX-1", null, new BigDecimal("10.00"), null, ACCOUNT, "111111111111",
        null, TransactionStatus.COMPLETED, Channel.SOAP, "pago", LocalDateTime.of(2026, 10, 14, 12, 0)));

    TransferRequest request = new TransferRequest();
    request.setSourceAccountNumber(ACCOUNT);
    request.setDestinationAccountNumber("111111111111");
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

  @Test
  void soapError_clasificaBienLasExcepciones() {
    assertThat(SoapError.from(new NotFoundException("x"))).isEqualTo(SoapError.NOT_FOUND);
    assertThat(SoapError.from(new NullPointerException())).isEqualTo(SoapError.INTERNAL_ERROR);
    assertThat(SoapError.INTERNAL_ERROR.isClientFault()).isFalse();
  }
}
