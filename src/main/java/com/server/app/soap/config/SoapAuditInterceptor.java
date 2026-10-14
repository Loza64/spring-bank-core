package com.server.app.soap.config;

import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.ws.WebServiceMessage;
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

/** Guarda en audit_logs el XML de entrada/salida de cada llamada SOAP (gobernanza). */
@Component
@RequiredArgsConstructor
public class SoapAuditInterceptor implements EndpointInterceptor {

  private static final Logger log = LoggerFactory.getLogger(SoapAuditInterceptor.class);
  private static final String REQUEST_XML = "audit.requestXml";

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
    try {
      String responseXml = null;
      int status = 200;

      if (ctx.hasResponse()) {
        WebServiceMessage response = ctx.getResponse();
        if (response instanceof SoapMessage soap && soap.getSoapBody().hasFault()) {
          status = 500;
          responseXml = "FAULT: " + soap.getSoapBody().getFault().getFaultStringOrReason();
        } else {
          responseXml = toXml(response.getPayloadSource());
        }
      } else if (ex != null) {
        status = 500;
        responseXml = "ERROR: " + ex.getMessage();
      }

      String operation = endpoint instanceof MethodEndpoint m ? m.getMethod().getName() : "unknown";
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      String client = auth != null ? auth.getName() : "anonymous";

      auditService.record(
          Channel.SOAP,
          "SOAP " + operation + " (client: " + client + ")",
          (String) ctx.getProperty(REQUEST_XML),
          responseXml,
          remoteAddress(),
          status,
          null);
    } catch (Exception e) {
      log.error("No se pudo registrar la auditoría SOAP", e);
    }
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
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      Transformer transformer = factory.newTransformer();
      StringWriter writer = new StringWriter();
      transformer.transform(source, new StreamResult(writer));
      return writer.toString();
    } catch (Exception e) {
      return "<unreadable payload>";
    }
  }
}
