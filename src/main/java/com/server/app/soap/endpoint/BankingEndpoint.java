package com.server.app.soap.endpoint;

import static com.server.app.soap.SoapConstants.NAMESPACE;

import org.springframework.data.domain.PageRequest;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.server.app.service.AccountService;
import com.server.app.service.TransactionService;
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

import lombok.RequiredArgsConstructor;

/**
 * Adaptador SOAP: traduce y delega, nada más. Las reglas de negocio viven en
 * los mismos servicios que usa REST.
 */
@Endpoint
@RequiredArgsConstructor
public class BankingEndpoint {

  // el XSD limita size a 100; aquí solo van los valores por defecto
  private static final int DEFAULT_PAGE = 1;
  private static final int DEFAULT_SIZE = 20;

  private final AccountService accountService;
  private final TransactionService transactionService;
  private final BankingSoapMapper mapper;

  @PayloadRoot(namespace = NAMESPACE, localPart = "GetBalanceRequest")
  @ResponsePayload
  public GetBalanceResponse getBalance(@RequestPayload GetBalanceRequest request) {
    return mapper.toBalanceResponse(accountService.findByAccountNumber(request.getAccountNumber()));
  }

  @PayloadRoot(namespace = NAMESPACE, localPart = "ValidateAccountRequest")
  @ResponsePayload
  public ValidateAccountResponse validateAccount(@RequestPayload ValidateAccountRequest request) {
    return accountService.findOptionalByAccountNumber(request.getAccountNumber())
        .map(mapper::toValidateAccountResponse)
        .orElseGet(mapper::toAccountNotFoundResponse);
  }

  @PayloadRoot(namespace = NAMESPACE, localPart = "TransferRequest")
  @ResponsePayload
  public TransferResponse transfer(@RequestPayload TransferRequest request) {
    return mapper.toTransferResponse(transactionService.transfer(mapper.toTransferCommand(request)));
  }

  @PayloadRoot(namespace = NAMESPACE, localPart = "GetTransactionStatusRequest")
  @ResponsePayload
  public GetTransactionStatusResponse getTransactionStatus(@RequestPayload GetTransactionStatusRequest request) {
    return mapper.toTransactionStatusResponse(transactionService.findByReference(request.getReference()));
  }

  @PayloadRoot(namespace = NAMESPACE, localPart = "GetAccountStatementRequest")
  @ResponsePayload
  public GetAccountStatementResponse getAccountStatement(@RequestPayload GetAccountStatementRequest request) {
    int page = request.getPage() != null ? request.getPage() : DEFAULT_PAGE;
    int size = request.getSize() != null ? request.getSize() : DEFAULT_SIZE;

    // en SOAP la página arranca en 1 (igual que la paginación del REST); Spring cuenta desde 0
    var result = transactionService.findByAccountNumber(request.getAccountNumber(), PageRequest.of(page - 1, size));
    return mapper.toStatementResponse(request.getAccountNumber(), result);
  }
}
