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

package net.sf.eclipsecs.core.config;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.sf.eclipsecs.core.config.meta.MetadataFactory;
import net.sf.eclipsecs.core.config.meta.RuleMetadata;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.core.util.XMLUtil;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.VisitorSupport;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

/**
 * Utility class to read a checkstyle configuration and transform to the plugins module objects.
 *
 * @author Lars Ködderitzsch
 */
public final class ConfigurationReader {

  private static final Pattern PROPERTY_REF_PATTERN = Pattern.compile("^.*\\$\\{.*\\}.*$");

  //
  // Mapping from public DTD to internal dtd resource.
  // @see com.puppycrawl.tools.checkstyle.api.AbstractLoader
  //

  /** Map containing the public - internal DTD mapping. */
  private static final Map<String, String> PUBLIC2INTERNAL_DTD_MAP;

  /** The public ID for version 1_0 of the configuration dtd. */
  private static final String DTD_PUBLIC_ID_1_0 =
      "-//Puppy Crawl//DTD Check Configuration 1.0//EN";

  /** The new public ID for version 1_0 of the configuration dtd. */
  private static final String DTD_PUBLIC_CS_ID_1_0 =
      "-//Checkstyle//DTD Checkstyle Configuration 1.0//EN";

  /** The resource for version 1_0 of the configuration dtd. */
  private static final String DTD_CONFIGURATION_NAME_1_0 =
      "com/puppycrawl/tools/checkstyle/configuration_1_0.dtd";

  /** The public ID for version 1_1 of the configuration dtd. */
  private static final String DTD_PUBLIC_ID_1_1 =
      "-//Puppy Crawl//DTD Check Configuration 1.1//EN";

  /** The new public ID for version 1_1 of the configuration dtd. */
  private static final String DTD_PUBLIC_CS_ID_1_1 =
      "-//Checkstyle//DTD Checkstyle Configuration 1.1//EN";

  /** The resource for version 1_1 of the configuration dtd. */
  private static final String DTD_CONFIGURATION_NAME_1_1 =
      "com/puppycrawl/tools/checkstyle/configuration_1_1.dtd";

  /** The public ID for version 1_2 of the configuration dtd. */
  private static final String DTD_PUBLIC_ID_1_2 =
      "-//Puppy Crawl//DTD Check Configuration 1.2//EN";

  /** The new public ID for version 1_2 of the configuration dtd. */
  private static final String DTD_PUBLIC_CS_ID_1_2 =
      "-//Checkstyle//DTD Checkstyle Configuration 1.2//EN";

  /** The resource for version 1_2 of the configuration dtd. */
  private static final String DTD_CONFIGURATION_NAME_1_2 =
      "com/puppycrawl/tools/checkstyle/configuration_1_2.dtd";

  /** The public ID for version 1_3 of the configuration dtd. */
  private static final String DTD_PUBLIC_ID_1_3 =
      "-//Puppy Crawl//DTD Check Configuration 1.3//EN";

  /** The new public ID for version 1_3 of the configuration dtd. */
  private static final String DTD_PUBLIC_CS_ID_1_3 =
      "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN";

  /** The resource for version 1_3 of the configuration dtd. */
  private static final String DTD_CONFIGURATION_NAME_1_3 =
      "com/puppycrawl/tools/checkstyle/configuration_1_3.dtd";

  static {
    final Map<String, String> map = new HashMap<>();
    map.put(DTD_PUBLIC_ID_1_0, DTD_CONFIGURATION_NAME_1_0);
    map.put(DTD_PUBLIC_ID_1_1, DTD_CONFIGURATION_NAME_1_1);
    map.put(DTD_PUBLIC_ID_1_2, DTD_CONFIGURATION_NAME_1_2);
    map.put(DTD_PUBLIC_ID_1_3, DTD_CONFIGURATION_NAME_1_3);
    map.put(DTD_PUBLIC_CS_ID_1_0, DTD_CONFIGURATION_NAME_1_0);
    map.put(DTD_PUBLIC_CS_ID_1_1, DTD_CONFIGURATION_NAME_1_1);
    map.put(DTD_PUBLIC_CS_ID_1_2, DTD_CONFIGURATION_NAME_1_2);
    map.put(DTD_PUBLIC_CS_ID_1_3, DTD_CONFIGURATION_NAME_1_3);

    PUBLIC2INTERNAL_DTD_MAP = map;
  }

  /** Hidden default constructor to prevent instantiation. */
  private ConfigurationReader() {
    // NOOP
  }

  /**
   * Reads the checkstyle configuration from the given stream and returns a list of all modules within
   * this configuration.
   *
   * @param in
   *          the stream the configuration is loaded from
   * @return the list of modules
   * @throws CheckstylePluginException
   *           error while reading the configuration
   */
  public static List<Module> read(InputSource in) throws CheckstylePluginException {

    List<Module> rules = null;
    try {

      final SAXReader reader = new SAXReader();
      reader.setEntityResolver(new XMLUtil.InternalDtdEntityResolver(PUBLIC2INTERNAL_DTD_MAP));
      final Document document = reader.read(in);

      rules = getModules(document);
    } catch (final DocumentException ex) {
      CheckstylePluginException.rethrow(ex);
    }

    return rules != null ? rules : new ArrayList<>();
  }

  private static List<Module> getModules(final Document document) {

    final List<Module> modules = new ArrayList<>();

    document.accept(new VisitorSupport() {

      @Override
      public void visit(final Element node) {

        if (XMLTags.MODULE_TAG.equals(node.getName())) {

          final String name = node.attributeValue(XMLTags.NAME_TAG);

          final RuleMetadata metadata = MetadataFactory.getRuleMetadata(name);
          Module module = null;
          if (metadata != null) {
            module = new Module(metadata, true);
          } else {
            module = new Module(name);
          }

          addProperties(node, module);
          addMessages(node, module);
          addMetadata(node, module);

          // if the module has not metadata attached we create some
          // generic metadata
          if (module.getMetaData() == null) {
            MetadataFactory.createGenericMetadata(module);
          }

          modules.add(module);
        }
      }
    });
    return modules;
  }

  private static void addProperties(final Element moduleEl, final Module module) {

    final List<Element> propertyEls = moduleEl.elements(XMLTags.PROPERTY_TAG);

    for (final Element propertyEl : propertyEls) {

      final String name = propertyEl.attributeValue(XMLTags.NAME_TAG);
      final String value = propertyEl.attributeValue(XMLTags.VALUE_TAG);

      final boolean isPropertyRef = (value != null)
              && PROPERTY_REF_PATTERN.matcher(value).matches();

      if (name.equals(XMLTags.SEVERITY_TAG) && (module.getMetaData() != null)
              && module.getMetaData().hasSeverity()) {
        try {
          module.setSeverity(Severity.valueOf(value));
        } catch (final IllegalArgumentException e) {
          module.setSeverity(Severity.inherit);
        }
      } else if (name.equals(XMLTags.ID_TAG)) {
        module.setId(Strings.emptyToNull(value));
      } else if (module.getMetaData() != null) {

        final ConfigProperty property = module.getProperty(name);
        if (property != null) {
          property.setValue(value);
          property.setPropertyReference(isPropertyRef);
        }

        // properties that are not within the meta data are omitted
      } else {
        // if module has no meta data defined create property
        final ConfigProperty property = new ConfigProperty(name, value);
        property.setPropertyReference(isPropertyRef);
        module.getProperties().add(property);
      }
    }
  }

  private static void addMessages(final Element moduleEl, final Module module) {

    final List<Element> messageEls = moduleEl.elements(XMLTags.MESSAGE_TAG);

    for (final Element messageEl : messageEls) {

      final String key = messageEl.attributeValue(XMLTags.KEY_TAG);
      final String value = messageEl.attributeValue(XMLTags.VALUE_TAG);

      module.getCustomMessages().put(key, value);
    }
  }

  private static void addMetadata(final Element moduleEl, final Module module) {

    final List<Element> metaEls = moduleEl.elements(XMLTags.METADATA_TAG);

    for (final Element metaEl : metaEls) {

      final String name = metaEl.attributeValue(XMLTags.NAME_TAG);
      final String value = metaEl.attributeValue(XMLTags.VALUE_TAG);

      if (XMLTags.COMMENT_ID.equals(name)) {
        module.setComment(value);
      } else if (XMLTags.LAST_ENABLED_SEVERITY_ID.equals(name)) {
        module.setLastEnabledSeverity(Severity.valueOf(value));
      } else {
        module.getCustomMetaData().put(name, value);
      }
    }
  }
}
