package com.server.app.soap.endpoint;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.server.app.common.exceptions.ServerException;
import com.server.app.domain.dto.account.AccountResponseDto;
import com.server.app.domain.dto.transaction.TransactionResponseDto;
import com.server.app.domain.model.enums.Channel;
import com.server.app.service.AccountService;
import com.server.app.service.TransactionService;
import com.server.app.service.TransactionService.TransferCommand;
import com.server.app.soap.model.GetBalanceRequest;
import com.server.app.soap.model.GetBalanceResponse;
import com.server.app.soap.model.TransferRequest;
import com.server.app.soap.model.TransferResponse;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador SOAP: solo traduce XML ⇄ DTO y delega en los MISMOS servicios que
 * usa REST. Ninguna regla de negocio vive aquí.
 */
@Endpoint
@RequiredArgsConstructor
public class BankingEndpoint {

  public static final String NAMESPACE = "http://server.com/banking/ws";

  private final AccountService accountService;
  private final TransactionService transactionService;

  @PayloadRoot(namespace = NAMESPACE, localPart = "GetBalanceRequest")
  @ResponsePayload
  public GetBalanceResponse getBalance(@RequestPayload GetBalanceRequest request) {
    AccountResponseDto account = accountService.findByAccountNumber(request.getAccountNumber());

    GetBalanceResponse response = new GetBalanceResponse();
    response.setAccountNumber(account.accountNumber());
    response.setBalance(account.balance());
    response.setCurrency(account.currency());
    response.setStatus(account.status().name());
    return response;
  }

  @PayloadRoot(namespace = NAMESPACE, localPart = "TransferRequest")
  @ResponsePayload
  public TransferResponse transfer(@RequestPayload TransferRequest request) {
    // Cliente máquina-a-máquina: sin usuario humano ni verificación de propiedad
    TransferCommand cmd = new TransferCommand(
        request.getSourceAccountNumber(),
        request.getDestinationAccountNumber(),
        request.getAmount(),
        request.getDescription(),
        request.getReference(),
        Channel.SOAP,
        null,
        false);

    TransactionResponseDto tx = transactionService.transfer(cmd);

    TransferResponse response = new TransferResponse();
    response.setTransactionReference(tx.transactionReference());
    response.setStatus(tx.status().name());
    response.setAmount(tx.amount());
    response.setCreatedAt(toXml(tx.createdAt()));
    return response;
  }

  private XMLGregorianCalendar toXml(java.time.LocalDateTime dateTime) {
    try {
      GregorianCalendar calendar = GregorianCalendar.from(dateTime.atZone(java.time.ZoneId.systemDefault()));
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
    } catch (DatatypeConfigurationException e) {
      throw new ServerException("No se pudo convertir la fecha a XML");
    }
  }
}
