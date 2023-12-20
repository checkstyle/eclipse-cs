//============================================================================
//
// Copyright (C) 2003-2023 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
//
//============================================================================

package net.sf.eclipsecs.checkstyle.utils;

import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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

}
