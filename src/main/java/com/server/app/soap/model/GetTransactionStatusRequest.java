package com.server.app.soap.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "GetTransactionStatusRequest")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "reference" })
public class GetTransactionStatusRequest {

  @XmlElement(required = true)
  private String reference;
}
