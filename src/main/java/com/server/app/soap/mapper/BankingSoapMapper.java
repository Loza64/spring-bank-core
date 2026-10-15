package com.server.app.soap.mapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.stereotype.Component;

import com.server.app.common.pagination.PaginationResponse;
import com.server.app.domain.dto.account.AccountResponseDto;
import com.server.app.domain.dto.transaction.TransactionResponseDto;
import com.server.app.domain.model.enums.Channel;
import com.server.app.service.TransactionService.TransferCommand;
import com.server.app.soap.model.GetAccountStatementResponse;
import com.server.app.soap.model.GetBalanceResponse;
import com.server.app.soap.model.GetTransactionStatusResponse;
import com.server.app.soap.model.TransactionInfo;
import com.server.app.soap.model.TransferRequest;
import com.server.app.soap.model.TransferResponse;
import com.server.app.soap.model.ValidateAccountResponse;

/** Traduce entre los objetos XML (JAXB) y los DTOs/commands de los servicios. */
@Component
public class BankingSoapMapper {

  private final DatatypeFactory datatypeFactory;

  public BankingSoapMapper() {
    try {
      this.datatypeFactory = DatatypeFactory.newInstance();
    } catch (DatatypeConfigurationException e) {
      throw new IllegalStateException("No se pudo crear el DatatypeFactory", e);
    }
  }

  public GetBalanceResponse toBalanceResponse(AccountResponseDto account) {
    GetBalanceResponse response = new GetBalanceResponse();
    response.setAccountNumber(account.accountNumber());
    response.setBalance(account.balance());
    response.setCurrency(account.currency());
    response.setStatus(account.status().name());
    return response;
  }

  public TransferCommand toTransferCommand(TransferRequest request) {
    // máquina a máquina: sin usuario humano, por eso initiator null y sin ownership
    return new TransferCommand(
        request.getSourceAccountNumber(),
        request.getDestinationAccountNumber(),
        request.getAmount(),
        request.getDescription(),
        request.getReference(),
        Channel.SOAP,
        null,
        false);
  }

  public TransferResponse toTransferResponse(TransactionResponseDto tx) {
    TransferResponse response = new TransferResponse();
    response.setTransactionReference(tx.transactionReference());
    response.setStatus(tx.status().name());
    response.setAmount(tx.amount());
    response.setCreatedAt(toXml(tx.createdAt()));
    return response;
  }

  public ValidateAccountResponse toValidateAccountResponse(AccountResponseDto account) {
    ValidateAccountResponse response = new ValidateAccountResponse();
    response.setExists(true);
    response.setStatus(account.status().name());
    response.setCurrency(account.currency());
    return response;
  }

  /** Cuenta inexistente no es un error aquí: es justo lo que el cliente quiere saber. */
  public ValidateAccountResponse toAccountNotFoundResponse() {
    ValidateAccountResponse response = new ValidateAccountResponse();
    response.setExists(false);
    return response;
  }

  public GetTransactionStatusResponse toTransactionStatusResponse(TransactionResponseDto tx) {
    GetTransactionStatusResponse response = new GetTransactionStatusResponse();
    response.setTransaction(toTransactionInfo(tx));
    return response;
  }

  public GetAccountStatementResponse toStatementResponse(String accountNumber,
      PaginationResponse<TransactionResponseDto> page) {
    GetAccountStatementResponse response = new GetAccountStatementResponse();
    response.setAccountNumber(accountNumber);
    response.setPage(page.pagination().page());
    response.setPageSize(page.pagination().pageSize());
    response.setPageCount(page.pagination().pageCount());
    response.setTotal(page.pagination().total());
    page.data().forEach(tx -> response.getTransactions().add(toTransactionInfo(tx)));
    return response;
  }

  private TransactionInfo toTransactionInfo(TransactionResponseDto tx) {
    TransactionInfo info = new TransactionInfo();
    info.setTransactionReference(tx.transactionReference());
    info.setType(tx.type().name());
    info.setStatus(tx.status().name());
    info.setAmount(tx.amount());
    info.setSourceAccountNumber(tx.sourceAccountNumber());
    info.setDestinationAccountNumber(tx.destinationAccountNumber());
    info.setChannel(tx.channel().name());
    info.setDescription(tx.description());
    info.setCreatedAt(toXml(tx.createdAt()));
    return info;
  }

  private XMLGregorianCalendar toXml(LocalDateTime dateTime) {
    GregorianCalendar calendar = GregorianCalendar.from(dateTime.atZone(ZoneId.systemDefault()));
    return datatypeFactory.newXMLGregorianCalendar(calendar);
  }
}
