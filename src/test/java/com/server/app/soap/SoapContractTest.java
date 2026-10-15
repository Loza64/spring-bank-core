package com.server.app.soap;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.server.app.domain.model.enums.AccountStatus;
import com.server.app.domain.model.enums.Channel;
import com.server.app.domain.model.enums.TransactionStatus;
import com.server.app.domain.model.enums.TransactionType;

/** Que los enums del XSD no se desincronicen de los de Java. */
class SoapContractTest {

  @Test
  void enumsDelXsdCoincidenConLosDeJava() throws Exception {
    Document xsd = loadXsd();

    assertThat(enumValues(xsd, "AccountStatus")).containsExactlyInAnyOrderElementsOf(names(AccountStatus.values()));
    assertThat(enumValues(xsd, "TransactionType")).containsExactlyInAnyOrderElementsOf(names(TransactionType.values()));
    assertThat(enumValues(xsd, "TransactionStatus")).containsExactlyInAnyOrderElementsOf(names(TransactionStatus.values()));
    assertThat(enumValues(xsd, "Channel")).containsExactlyInAnyOrderElementsOf(names(Channel.values()));
  }

  private Document loadXsd() throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    try (InputStream in = getClass().getClassLoader().getResourceAsStream(SoapConstants.XSD_PATH)) {
      return factory.newDocumentBuilder().parse(in);
    }
  }

  private List<String> enumValues(Document xsd, String simpleTypeName) {
    NodeList types = xsd.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "simpleType");
    for (int i = 0; i < types.getLength(); i++) {
      Element type = (Element) types.item(i);
      if (simpleTypeName.equals(type.getAttribute("name"))) {
        NodeList values = type.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "enumeration");
        List<String> result = new ArrayList<>();
        for (int j = 0; j < values.getLength(); j++) {
          result.add(((Element) values.item(j)).getAttribute("value"));
        }
        return result;
      }
    }
    throw new IllegalStateException("No existe el simpleType " + simpleTypeName + " en el XSD");
  }

  private List<String> names(Enum<?>[] values) {
    return Arrays.stream(values).map(Enum::name).toList();
  }
}
