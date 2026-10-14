package com.server.app.soap.config;

import java.util.List;
import java.util.Properties;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurer;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;
import org.springframework.ws.soap.server.endpoint.interceptor.PayloadValidatingInterceptor;

import com.server.app.common.exceptions.BadRequestException;
import com.server.app.common.exceptions.ConfictException;
import com.server.app.common.exceptions.ForbiddenException;
import com.server.app.common.exceptions.NotFoundException;
import com.server.app.common.exceptions.ServerException;

import lombok.RequiredArgsConstructor;

@EnableWs
@Configuration
@RequiredArgsConstructor
public class WebServiceConfig implements WsConfigurer {

  private static final String NAMESPACE = "http://server.com/banking/ws";

  private final SoapAuditInterceptor auditInterceptor;

  @Bean
  ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext context) {
    MessageDispatcherServlet servlet = new MessageDispatcherServlet();
    servlet.setApplicationContext(context);
    servlet.setTransformWsdlLocations(true);
    return new ServletRegistrationBean<>(servlet, "/ws/*");
  }

  @Bean
  XsdSchema bankingSchema() {
    return new SimpleXsdSchema(new ClassPathResource("xsd/banking.xsd"));
  }

  /** Publica el contrato en /ws/banking.wsdl (derivado del XSD). */
  @Bean(name = "banking")
  DefaultWsdl11Definition bankingWsdl(XsdSchema bankingSchema) {
    DefaultWsdl11Definition wsdl = new DefaultWsdl11Definition();
    wsdl.setPortTypeName("BankingPort");
    wsdl.setLocationUri("/ws");
    wsdl.setTargetNamespace(NAMESPACE);
    wsdl.setSchema(bankingSchema);
    return wsdl;
  }

  @Override
  public void addInterceptors(List<EndpointInterceptor> interceptors) {
    // 1º auditoría (para registrar también las peticiones inválidas), 2º validación contra el XSD
    interceptors.add(auditInterceptor);

    PayloadValidatingInterceptor validator = new PayloadValidatingInterceptor();
    validator.setSchema(new ClassPathResource("xsd/banking.xsd"));
    validator.setValidateRequest(true);
    validator.setValidateResponse(false);
    interceptors.add(validator);
  }

  /** Errores de negocio → SOAP Fault (Client = petición incorrecta, Server = fallo interno). */
  @Bean
  SoapFaultMappingExceptionResolver soapFaultResolver() {
    SoapFaultMappingExceptionResolver resolver = new SoapFaultMappingExceptionResolver();
    Properties mappings = new Properties();
    mappings.setProperty(NotFoundException.class.getName(), "CLIENT");
    mappings.setProperty(BadRequestException.class.getName(), "CLIENT");
    mappings.setProperty(ConfictException.class.getName(), "CLIENT");
    mappings.setProperty(ForbiddenException.class.getName(), "CLIENT");
    mappings.setProperty(ServerException.class.getName(), "SERVER");
    resolver.setExceptionMappings(mappings);
    SoapFaultDefinition defaultFault = new SoapFaultDefinition();
    defaultFault.setFaultCode(SoapFaultDefinition.SERVER);
    defaultFault.setFaultStringOrReason("Error interno del servidor");
    resolver.setDefaultFault(defaultFault);
    resolver.setOrder(1);
    return resolver;
  }
}
