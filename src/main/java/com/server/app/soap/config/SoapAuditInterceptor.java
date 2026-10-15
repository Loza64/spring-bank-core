package com.server.app.soap.config;

import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpServletConnection;

import com.server.app.domain.model.enums.Channel;
import com.server.app.service.AuditService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Guarda en audit_logs el XML que entra y sale de cada llamada SOAP. */
@Slf4j
@Component
@RequiredArgsConstructor
public class SoapAuditInterceptor implements EndpointInterceptor {

  private static final String REQUEST_XML = "audit.requestXml";
  private static final String UNREADABLE = "<unreadable payload>";

  private final AuditService auditService;

  @Override
  public boolean handleRequest(MessageContext ctx, Object endpoint) {
    ctx.setProperty(REQUEST_XML, toXml(ctx.getRequest().getPayloadSource()));
    return true;
  }

  @Override
  public boolean handleResponse(MessageContext ctx, Object endpoint) {
    return true;
  }

  @Override
  public boolean handleFault(MessageContext ctx, Object endpoint) {
    return true;
  }

  @Override
  public void afterCompletion(MessageContext ctx, Object endpoint, Exception ex) {
    // la auditoría nunca debe tumbar la llamada, por eso el try grande
    try {
      int status = 200;
      String responseXml = null;

      if (ctx.hasResponse()) {
        if (ctx.getResponse() instanceof SoapMessage soap && soap.getSoapBody().hasFault()) {
          status = 500;
          responseXml = "FAULT: " + soap.getSoapBody().getFault().getFaultStringOrReason();
        } else {
          responseXml = toXml(ctx.getResponse().getPayloadSource());
        }
      } else if (ex != null) {
        status = 500;
        responseXml = "ERROR: " + ex.getMessage();
      }

      auditService.record(
          Channel.SOAP,
          "SOAP " + operationName(endpoint) + " (client: " + clientName() + ")",
          (String) ctx.getProperty(REQUEST_XML),
          responseXml,
          remoteAddress(),
          status,
          null);
    } catch (Exception e) {
      log.error("No se pudo registrar la auditoría SOAP", e);
    }
  }

  private String operationName(Object endpoint) {
    return endpoint instanceof MethodEndpoint m ? m.getMethod().getName() : "unknown";
  }

  private String clientName() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth != null ? auth.getName() : "anonymous";
  }

  private String remoteAddress() {
    TransportContext tc = TransportContextHolder.getTransportContext();
    if (tc != null && tc.getConnection() instanceof HttpServletConnection c) {
      return c.getHttpServletRequest().getRemoteAddr();
    }
    return null;
  }

  private String toXml(Source source) {
    if (source == null) {
      return null;
    }
    try {
      TransformerFactory factory = TransformerFactory.newInstance();
      // anti-XXE: sin DTDs ni hojas de estilo externas
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

      Transformer transformer = factory.newTransformer();
      StringWriter writer = new StringWriter();
      transformer.transform(source, new StreamResult(writer));
      return writer.toString();
    } catch (Exception e) {
      return UNREADABLE;
    }
  }
}
