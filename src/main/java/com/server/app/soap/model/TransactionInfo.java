package com.server.app.soap.model;

import java.math.BigDecimal;

import javax.xml.datatype.XMLGregorianCalendar;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter;

/** Un movimiento. Lo reutilizan GetTransactionStatus y GetAccountStatement. */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TransactionInfo", propOrder = { "transactionReference", "type", "status", "amount",
    "sourceAccountNumber", "destinationAccountNumber", "channel", "description", "createdAt" })
public class TransactionInfo {

  @XmlElement(required = true)
  private String transactionReference;
  @XmlElement(required = true)
  private String type;
  @XmlElement(required = true)
  private String status;
  @XmlElement(required = true)
  private BigDecimal amount;
  // un depósito no tiene origen y un retiro no tiene destino
  private String sourceAccountNumber;
  private String destinationAccountNumber;
  @XmlElement(required = true)
  private String channel;
  private String description;
  @XmlElement(required = true)
  @XmlSchemaType(name = "dateTime")
  private XMLGregorianCalendar createdAt;
}
