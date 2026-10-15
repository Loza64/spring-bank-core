package com.server.app.soap;

/** Valores compartidos del canal SOAP, para no repetir strings por todos lados. */
public final class SoapConstants {

  public static final String NAMESPACE = "http://server.com/banking/ws";
  public static final String PREFIX = "bk";
  public static final String BASE_PATH = "/ws";
  public static final String XSD_PATH = "xsd/banking.xsd";

  private SoapConstants() {
  }
}
