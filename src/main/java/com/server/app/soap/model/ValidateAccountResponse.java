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
@XmlRootElement(name = "ValidateAccountResponse")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "exists", "status", "currency" })
public class ValidateAccountResponse {

  @XmlElement(required = true)
  private boolean exists;
  // status y currency solo vienen si la cuenta existe
  private String status;
  private String currency;
}
