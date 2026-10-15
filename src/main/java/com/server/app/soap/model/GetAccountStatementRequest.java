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
@XmlRootElement(name = "GetAccountStatementRequest")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "accountNumber", "page", "size" })
public class GetAccountStatementRequest {

  @XmlElement(required = true)
  private String accountNumber;
  // opcionales: si no vienen se usan 1 y 20
  private Integer page;
  private Integer size;
}
