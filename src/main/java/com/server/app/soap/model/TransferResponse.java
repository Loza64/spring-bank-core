package com.server.app.soap.model;

import java.math.BigDecimal;

import javax.xml.datatype.XMLGregorianCalendar;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "TransferResponse")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "transactionReference", "status", "amount", "createdAt" })
public class TransferResponse {
  @XmlElement(required = true)
  private String transactionReference;
  @XmlElement(required = true)
  private String status;
  @XmlElement(required = true)
  private BigDecimal amount;
  @XmlElement(required = true)
  private XMLGregorianCalendar createdAt;
}
