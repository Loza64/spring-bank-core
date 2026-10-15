package com.server.app.soap.fault;

import static com.server.app.soap.SoapConstants.NAMESPACE;
import static com.server.app.soap.SoapConstants.PREFIX;

import java.util.Locale;

import javax.xml.namespace.QName;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointExceptionResolver;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * Convierte excepciones en SOAP Faults con un errorCode en el detail.
 * Errores del cliente: se devuelve el mensaje. Errores nuestros: mensaje
 * genérico al cliente y stack trace en el log (nada interno sale por el cable).
 */
@Slf4j
@Component
public class SoapFaultResolver implements EndpointExceptionResolver, Ordered {

  private static final Locale LOCALE = Locale.forLanguageTag("es");
  private static final String GENERIC_MESSAGE = "Error interno del servidor";
  private static final QName ERROR_CODE = new QName(NAMESPACE, "errorCode", PREFIX);

  @Override
  public boolean resolveException(MessageContext ctx, Object endpoint, Exception ex) {
    if (!(ctx.getResponse() instanceof SoapMessage response)) {
      return false;
    }

    SoapError error = SoapError.from(ex);
    String reason = reasonFor(error, ex);

    SoapBody body = response.getSoapBody();
    SoapFault fault = error.isClientFault()
        ? body.addClientOrSenderFault(reason, LOCALE)
        : body.addServerOrReceiverFault(reason, LOCALE);

    fault.addFaultDetail().addFaultDetailElement(ERROR_CODE).addText(error.name());
    return true;
  }

  private String reasonFor(SoapError error, Exception ex) {
    if (error.isClientFault()) {
      log.warn("SOAP {}: {}", error, ex.getMessage());
      return ex.getMessage() != null ? ex.getMessage() : error.name();
    }
    log.error("SOAP error inesperado", ex);
    return GENERIC_MESSAGE;
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
