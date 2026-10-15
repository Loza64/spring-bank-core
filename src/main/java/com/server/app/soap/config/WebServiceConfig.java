package com.server.app.soap.config;

import static com.server.app.soap.SoapConstants.BASE_PATH;
import static com.server.app.soap.SoapConstants.NAMESPACE;
import static com.server.app.soap.SoapConstants.XSD_PATH;

import java.util.List;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurer;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.server.endpoint.interceptor.PayloadValidatingInterceptor;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import lombok.RequiredArgsConstructor;

/** Contract-first: el XSD manda, de ahí sale el WSDL y la validación. */
@EnableWs
@Configuration
@RequiredArgsConstructor
public class WebServiceConfig implements WsConfigurer {

  private final SoapAuditInterceptor auditInterceptor;

  @Bean
  ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext context) {
    MessageDispatcherServlet servlet = new MessageDispatcherServlet();
    servlet.setApplicationContext(context);
    servlet.setTransformWsdlLocations(true);
    return new ServletRegistrationBean<>(servlet, BASE_PATH + "/*");
  }

  @Bean
  XsdSchema bankingSchema() {
    return new SimpleXsdSchema(new ClassPathResource(XSD_PATH));
  }

  /** Publica el WSDL en /ws/banking.wsdl (el nombre del bean es el nombre del archivo). */
  @Bean(name = "banking")
  DefaultWsdl11Definition bankingWsdl(XsdSchema bankingSchema) {
    DefaultWsdl11Definition wsdl = new DefaultWsdl11Definition();
    wsdl.setPortTypeName("BankingPort");
    wsdl.setLocationUri(BASE_PATH);
    wsdl.setTargetNamespace(NAMESPACE);
    wsdl.setSchema(bankingSchema);
    return wsdl;
  }

  // Tiene que ser @Bean: con "new" suelto Spring no lo inicializa y revienta con NPE
  @Bean
  PayloadValidatingInterceptor bankingPayloadValidator() {
    PayloadValidatingInterceptor validator = new PayloadValidatingInterceptor();
    validator.setSchema(new ClassPathResource(XSD_PATH));
    validator.setValidateRequest(true);
    validator.setValidateResponse(false);
    return validator;
  }

  @Override
  public void addInterceptors(List<EndpointInterceptor> interceptors) {
    // el orden importa: primero auditoría para guardar también los requests inválidos
    interceptors.add(auditInterceptor);
    interceptors.add(bankingPayloadValidator());
  }
}
