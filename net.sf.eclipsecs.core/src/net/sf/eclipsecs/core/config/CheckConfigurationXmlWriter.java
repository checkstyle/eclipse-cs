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

package net.sf.eclipsecs.core.config;

import java.util.Map;

import org.dom4j.Element;

/**
 * Utility for serializing check configurations to XML.
 */
public final class CheckConfigurationXmlWriter {

  private CheckConfigurationXmlWriter() {
  }

  /**
   * Writes a check configuration as an XML child element under the given parent.
   *
   * @param parentElement
   *          the parent XML element
   * @param checkConfig
   *          the check configuration to write
   * @param location
   *          the resolved location string
   * @param elementName
   *          the XML element name to use
   */
  public static void writeCheckConfiguration(Element parentElement, ICheckConfiguration checkConfig,
          String location, String elementName) {
    Element configEl = parentElement.addElement(elementName);
    configEl.addAttribute(XMLTags.NAME_TAG, checkConfig.getName());
    configEl.addAttribute(XMLTags.LOCATION_TAG, location);
    configEl.addAttribute(XMLTags.TYPE_TAG, checkConfig.getType().getInternalName());
    if (checkConfig.getDescription() != null) {
      configEl.addAttribute(XMLTags.DESCRIPTION_TAG, checkConfig.getDescription());
    }
    for (ResolvableProperty prop : checkConfig.getResolvableProperties()) {
      Element propEl = configEl.addElement(XMLTags.PROPERTY_TAG);
      propEl.addAttribute(XMLTags.NAME_TAG, prop.getPropertyName());
      propEl.addAttribute(XMLTags.VALUE_TAG, prop.getValue());
    }
    for (Map.Entry<String, String> entry : checkConfig.getAdditionalData().entrySet()) {
      Element addEl = configEl.addElement(XMLTags.ADDITIONAL_DATA_TAG);
      addEl.addAttribute(XMLTags.NAME_TAG, entry.getKey());
      addEl.addAttribute(XMLTags.VALUE_TAG, entry.getValue());
    }
  }
}
