package com.server.app.soap.model;

import java.math.BigDecimal;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "TransferRequest")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "sourceAccountNumber", "destinationAccountNumber", "amount", "description",
    "reference" })
public class TransferRequest {

  @XmlElement(required = true)
  private String sourceAccountNumber;
  @XmlElement(required = true)
  private String destinationAccountNumber;
  @XmlElement(required = true)
  private BigDecimal amount;
  private String description;
  // clave de idempotencia (opcional)
  private String reference;
}
