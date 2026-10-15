package com.server.app.soap.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "GetAccountStatementResponse")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "accountNumber", "page", "pageSize", "pageCount", "total", "transactions" })
public class GetAccountStatementResponse {

  @XmlElement(required = true)
  private String accountNumber;
  private int page;
  private int pageSize;
  private int pageCount;
  private long total;
  @XmlElement(name = "transaction")
  private List<TransactionInfo> transactions = new ArrayList<>();
}
