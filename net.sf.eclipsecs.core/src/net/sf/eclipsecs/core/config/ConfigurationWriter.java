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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.sf.eclipsecs.core.Messages;
import net.sf.eclipsecs.core.config.savefilter.SaveFilters;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.core.util.XMLUtil;

import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Writes the modules of a checkstyle configuration to an output stream.
 *
 * @author Lars Ködderitzsch
 */
public final class ConfigurationWriter {

  /** Hidden default constructor. */
  private ConfigurationWriter() {

  }

  /**
   * Writes a new checkstyle configuration to the output stream.
   *
   * @param out
   *          the output stream to write to
   * @param checkConfig
   *          the Check configuration object
   * @throws CheckstylePluginException
   *           error writing the checkstyle configuration
   */
  public static void writeNewConfiguration(OutputStream out, ICheckConfiguration checkConfig)
          throws CheckstylePluginException {

    // write an empty list of modules
    // mandatory modules are added automatically
    write(out, new ArrayList<Module>(), checkConfig);
  }

  /**
   * Writes the modules of the configuration to the output stream.
   *
   * @param out
   *          the ouput stream.
   * @param modules
   *          the modules
   * @param checkConfig
   *          the Check configuration object
   * @throws CheckstylePluginException
   *           error writing the checkstyle configuration
   */
  public static void write(OutputStream out, List<Module> modules, ICheckConfiguration checkConfig)
          throws CheckstylePluginException {

    try {
      // pass the configured modules through the save filters
      SaveFilters.process(modules);

      Document doc = DocumentHelper.createDocument();
      doc.addDocType(XMLTags.MODULE_TAG, "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN",
              "https://checkstyle.org/dtds/configuration_1_3.dtd");

      String lineSeperator = System.getProperty("line.separator"); //$NON-NLS-1$

      String comment = lineSeperator
              + "    This configuration file was written by the eclipse-cs plugin configuration editor" //$NON-NLS-1$
              + lineSeperator;
      doc.addComment(comment);

      // write out name and description as comment
      String description = lineSeperator + "    Checkstyle-Configuration: " //$NON-NLS-1$
              + checkConfig.getName() + lineSeperator + "    Description: " //$NON-NLS-1$
              + (Strings.emptyToNull(checkConfig.getDescription()) != null
                      ? lineSeperator + checkConfig.getDescription() + lineSeperator
                      : "none" + lineSeperator); //$NON-NLS-1$
      doc.addComment(description);

      // find the root module (Checker)
      // the root module is the only module that has no parent
      List<Module> rootModules = getChildModules(null, modules);
      if (rootModules.size() < 1) {
        throw new CheckstylePluginException(Messages.errorNoRootModule);
      }

      if (rootModules.size() > 1) {
        throw new CheckstylePluginException(Messages.errorMoreThanOneRootModule);
      }

      writeModule(rootModules.get(0), doc, null, modules);

      out.write(XMLUtil.toByteArray(doc));
    } catch (IOException e) {
      CheckstylePluginException.rethrow(e);
    }
  }

  /**
   * Writes a module to the transformer handler.
   *
   * @param module
   *          the module to write
   * @param parent
   *          the parent element
   * @param parentSeverity
   *          the severity of the parent module
   * @param remainingModules
   *          the list of remaining (possibly child) modules
   */
  private static void writeModule(Module module, Branch parent, Severity parentSeverity,
          List<Module> remainingModules) {

    Severity severity = parentSeverity;

    // remove this module from the list of modules to write
    remainingModules.remove(module);

    final List<Module> childs = getChildModules(module, remainingModules);

    // Start the module
    Element moduleEl = parent.addElement(XMLTags.MODULE_TAG);
    moduleEl.addAttribute(XMLTags.NAME_TAG, module.getMetaData().getInternalName());

    // Write comment
    if (Strings.emptyToNull(module.getComment()) != null) {

      Element metaEl = moduleEl.addElement(XMLTags.METADATA_TAG);
      metaEl.addAttribute(XMLTags.NAME_TAG, XMLTags.COMMENT_ID);
      metaEl.addAttribute(XMLTags.VALUE_TAG, module.getComment());
    }

    // Write severity only if it differs from the parents severity
    if (module.getSeverity() != null && !Severity.inherit.equals(module.getSeverity())) {

      Element propertyEl = moduleEl.addElement(XMLTags.PROPERTY_TAG);
      propertyEl.addAttribute(XMLTags.NAME_TAG, XMLTags.SEVERITY_TAG);
      propertyEl.addAttribute(XMLTags.VALUE_TAG, module.getSeverity().name());

      // set the parent severity for child modules
      severity = module.getSeverity();
    }

    // write module id
    if (Strings.emptyToNull(module.getId()) != null) {

      Element propertyEl = moduleEl.addElement(XMLTags.PROPERTY_TAG);
      propertyEl.addAttribute(XMLTags.NAME_TAG, XMLTags.ID_TAG);
      propertyEl.addAttribute(XMLTags.VALUE_TAG, module.getId());
    }

    // write properties of the module
    for (ConfigProperty property : module.getProperties()) {

      // write property only if it differs from the default value
      String value = Strings.emptyToNull(property.getValue());
      if (value != null && !Objects.equals(value, property.getMetaData().getDefaultValue())) {

        Element propertyEl = moduleEl.addElement(XMLTags.PROPERTY_TAG);
        propertyEl.addAttribute(XMLTags.NAME_TAG, property.getMetaData().getName());
        propertyEl.addAttribute(XMLTags.VALUE_TAG, property.getValue());
      }
    }

    // write custom messages
    for (Map.Entry<String, String> entry : module.getCustomMessages().entrySet()) {

      Element metaEl = moduleEl.addElement(XMLTags.MESSAGE_TAG);
      metaEl.addAttribute(XMLTags.KEY_TAG, entry.getKey());
      metaEl.addAttribute(XMLTags.VALUE_TAG, entry.getValue());
    }

    // write custom metadata
    for (Map.Entry<String, String> entry : module.getCustomMetaData().entrySet()) {

      Element metaEl = moduleEl.addElement(XMLTags.METADATA_TAG);
      metaEl.addAttribute(XMLTags.NAME_TAG, entry.getKey());
      metaEl.addAttribute(XMLTags.VALUE_TAG, entry.getValue());
    }

    // Write last enabled severity level
    if (module.getLastEnabledSeverity() != null) {

      Element metaEl = moduleEl.addElement(XMLTags.METADATA_TAG);
      metaEl.addAttribute(XMLTags.NAME_TAG, XMLTags.LAST_ENABLED_SEVERITY_ID);
      metaEl.addAttribute(XMLTags.VALUE_TAG, module.getLastEnabledSeverity().name());
    }

    // write child modules recursively
    for (Module child : childs) {
      writeModule(child, moduleEl, severity, remainingModules);
    }
  }

  /**
   * Returns a list of child modules outgoing from a module.
   *
   * @param module
   *          the parent module
   * @param remainingModules
   *          the list of modules that are yet not written
   * @return the list of child modules
   */
  private static List<Module> getChildModules(Module module, List<Module> remainingModules) {

    List<Module> childModules = new ArrayList<>();

    for (Module tmp : remainingModules) {

      String parentInternalName = module != null ? module.getMetaData().getInternalName() : null;
      String childParent = tmp.getMetaData().getParentModule();

      // only the checker module has no parent
      if (parentInternalName == null && childParent.equals("Root")) {
        childModules.add(tmp);
      } else if (childParent.equals(parentInternalName)) {
        childModules.add(tmp);
      }
    }

    return childModules;
  }
}
