package com.server.app.soap.model;

import java.math.BigDecimal;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "GetBalanceResponse")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "accountNumber", "balance", "currency", "status" })
public class GetBalanceResponse {
  @XmlElement(required = true)
  private String accountNumber;
  @XmlElement(required = true)
  private BigDecimal balance;
  @XmlElement(required = true)
  private String currency;
  @XmlElement(required = true)
  private String status;
}
