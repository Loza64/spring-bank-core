package com.server.app.soap.model;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "GetBalanceRequest")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "accountNumber" })
public class GetBalanceRequest {
  @XmlElement(required = true)
  private String accountNumber;
}
