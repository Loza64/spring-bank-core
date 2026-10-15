package com.server.app.soap.endpoint;

import static com.server.app.soap.SoapConstants.NAMESPACE;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.server.app.service.AccountService;
import com.server.app.service.TransactionService;
import com.server.app.soap.mapper.BankingSoapMapper;
import com.server.app.soap.model.GetBalanceRequest;
import com.server.app.soap.model.GetBalanceResponse;
import com.server.app.soap.model.TransferRequest;
import com.server.app.soap.model.TransferResponse;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador SOAP: traduce y delega, nada más. Las reglas de negocio viven en
 * los mismos servicios que usa REST.
 */
@Endpoint
@RequiredArgsConstructor
public class BankingEndpoint {

  private final AccountService accountService;
  private final TransactionService transactionService;
  private final BankingSoapMapper mapper;

  @PayloadRoot(namespace = NAMESPACE, localPart = "GetBalanceRequest")
  @ResponsePayload
  public GetBalanceResponse getBalance(@RequestPayload GetBalanceRequest request) {
    return mapper.toBalanceResponse(accountService.findByAccountNumber(request.getAccountNumber()));
  }

  @PayloadRoot(namespace = NAMESPACE, localPart = "TransferRequest")
  @ResponsePayload
  public TransferResponse transfer(@RequestPayload TransferRequest request) {
    return mapper.toTransferResponse(transactionService.transfer(mapper.toTransferCommand(request)));
  }
}
