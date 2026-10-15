package com.server.app.soap.mapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.stereotype.Component;

import com.server.app.domain.dto.account.AccountResponseDto;
import com.server.app.domain.dto.transaction.TransactionResponseDto;
import com.server.app.domain.model.enums.Channel;
import com.server.app.service.TransactionService.TransferCommand;
import com.server.app.soap.model.GetBalanceResponse;
import com.server.app.soap.model.TransferRequest;
import com.server.app.soap.model.TransferResponse;

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

  private XMLGregorianCalendar toXml(LocalDateTime dateTime) {
    GregorianCalendar calendar = GregorianCalendar.from(dateTime.atZone(ZoneId.systemDefault()));
    return datatypeFactory.newXMLGregorianCalendar(calendar);
  }
}
