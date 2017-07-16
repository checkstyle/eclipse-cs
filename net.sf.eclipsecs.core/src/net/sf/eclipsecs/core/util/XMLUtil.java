//============================================================================
//
// Copyright (C) 2002-2016  David Schneider, Lars Ködderitzsch
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
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//============================================================================

package net.sf.eclipsecs.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Provides utility methods for XML manipulations.
 */
public final class XMLUtil {

  /**
   * Private constructor to prevent instances.
   */
  private XMLUtil() {
  }

  /**
   * Creates a pretty printed representation of the document as a byte array.
   * 
   * @param document
   *          the document
   * @return the document as a byte array (UTF-8)
   * @throws IOException
   *           Exception while serializing the document
   */
  public static byte[] toByteArray(Document document) throws IOException {

    ByteArrayOutputStream byteOut = new ByteArrayOutputStream(512);

    // Pretty print the document to System.out
    OutputFormat format = OutputFormat.createPrettyPrint();
    XMLWriter writer = new XMLWriter(byteOut, format);
    writer.write(document);

    return byteOut.toByteArray();
  }

  /**
   * Entity resolver which handles mapping public DTDs to internal DTD resource.
   * 
   * @author Lars Ködderitzsch
   */
  public static class InternalDtdEntityResolver implements EntityResolver {

    private final Map<String, String> mPublic2InternalDtdMap;

    /**
     * Creates the entity resolver using a mapping of public DTD name to internal DTD resource.
     * 
     * @param public2InternalDtdMap
     *          the public2internal DTD mapping
     */
    public InternalDtdEntityResolver(Map<String, String> public2InternalDtdMap) {
      mPublic2InternalDtdMap = public2InternalDtdMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException {

      if (mPublic2InternalDtdMap.containsKey(publicId)) {

        final String dtdResourceName = mPublic2InternalDtdMap.get(publicId);

        final InputStream dtdIS = getClass().getClassLoader().getResourceAsStream(dtdResourceName);
        if (dtdIS == null) {
          throw new SAXException("Unable to load internal dtd " + dtdResourceName); //$NON-NLS-1$
        }
        return new InputSource(dtdIS);
      }
      return null;
    }
  }
}