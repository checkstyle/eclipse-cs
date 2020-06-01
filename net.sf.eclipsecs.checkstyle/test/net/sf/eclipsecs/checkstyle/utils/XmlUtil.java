package net.sf.eclipsecs.checkstyle.utils;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public final class XmlUtil {

  private XmlUtil() {
  }

  public static Document getRawXml(String fileName, String code, String unserializedSource)
          throws ParserConfigurationException {
    Document rawXml = null;
    try {
      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setNamespaceAware(true);

      final DocumentBuilder builder = factory.newDocumentBuilder();

      builder.setEntityResolver(new EntityResolver() {
        @Override
        public InputSource resolveEntity(String publicId, String systemId)
                throws SAXException, IOException {
          return new InputSource(new StringReader(""));
        }
      });

      rawXml = builder.parse(new InputSource(new StringReader(code)));
    } catch (IOException | SAXException ex) {
      fail(fileName + " has invalid xml (" + ex.getMessage() + "): " + unserializedSource);
    }

    return rawXml;
  }

  public static Set<Node> getChildrenElements(Node node) {
    final Set<Node> result = new LinkedHashSet<>();

    for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child.getNodeType() != Node.TEXT_NODE) {
        result.add(child);
      }
    }

    return result;
  }

  public static Node findElementByTag(Set<Node> nodes, String tag) {
    Node result = null;

    for (Node child : nodes) {
      if (tag.equals(child.getNodeName())) {
        result = child;
        break;
      }
    }

    return result;
  }

  public static Set<Node> findElementsByTag(Set<Node> nodes, String tag) {

    return nodes.stream().filter(node -> tag.equals(node.getNodeName()))
            .collect(Collectors.toSet());
  }

}
