package com.server.app.soap.model;

import java.math.BigDecimal;

import javax.xml.datatype.XMLGregorianCalendar;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
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
  @XmlSchemaType(name = "dateTime")
  private XMLGregorianCalendar createdAt;
}
