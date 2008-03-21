//============================================================================
//
// Copyright (C) 2002-2007  David Schneider, Lars Ködderitzsch
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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.TransformerHandler;

import net.sf.eclipsecs.core.Messages;
import net.sf.eclipsecs.core.config.savefilter.SaveFilters;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.core.util.XMLUtil;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

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
     * @param out the output stream to write to
     * @param checkConfig the Check configuration object
     * @throws CheckstylePluginException error writing the checkstyle
     *             configuration
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
     * @param out the ouput stream.
     * @param modules the modules
     * @param checkConfig the Check configuration object
     * @throws CheckstylePluginException error writing the checkstyle
     *             configuration
     */
    public static void write(OutputStream out, List<Module> modules, ICheckConfiguration checkConfig)
        throws CheckstylePluginException {

        try {
            // pass the configured modules through the save filters
            SaveFilters.process(modules);

            TransformerHandler xmlOut = XMLUtil.writeWithSax(out,
                    "-//Puppy Crawl//DTD Check Configuration 1.2//EN", //$NON-NLS-1$
                    "http://www.puppycrawl.com/dtds/configuration_1_2.dtd"); //$NON-NLS-1$
            xmlOut.startDocument();

            String lineSeperator = System.getProperty("line.separator"); //$NON-NLS-1$

            String comment = lineSeperator
                    + "    This configuration file was written by the eclipse-cs plugin configuration editor" + lineSeperator; //$NON-NLS-1$
            xmlOut.comment(comment.toCharArray(), 0, comment.length());

            // write out name and description as comment
            String description = lineSeperator
                    + "    Checkstyle-Configuration: " //$NON-NLS-1$
                    + checkConfig.getName()
                    + lineSeperator
                    + "    Description: " //$NON-NLS-1$
                    + (StringUtils.trimToNull(checkConfig.getDescription()) != null ? lineSeperator
                            + checkConfig.getDescription() + lineSeperator : "none" + lineSeperator); //$NON-NLS-1$
            xmlOut.comment(description.toCharArray(), 0, description.length());

            xmlOut.ignorableWhitespace(new char[] { '\n' }, 0, 1);

            // find the root module (Checker)
            // the root module is the only module that has no parent
            List<Module> rootModules = getChildModules(null, modules);
            if (rootModules.size() < 1) {
                throw new CheckstylePluginException(Messages.errorNoRootModule);
            }

            if (rootModules.size() > 1) {
                throw new CheckstylePluginException(Messages.errorMoreThanOneRootModule);
            }

            writeModule(rootModules.get(0), xmlOut, null, modules);

            xmlOut.endDocument();
        }
        catch (TransformerConfigurationException e) {
            CheckstylePluginException.rethrow(e);
        }
        catch (SAXException e) {
            Exception ex = e.getException() != null ? e.getException() : e;
            CheckstylePluginException.rethrow(ex);
        }
    }

    /**
     * Writes a module to the transformer handler.
     * 
     * @param module the module to write
     * @param xmlOut the transformer handler
     * @param parentSeverity the severity of the parent module
     * @param remainingModules the list of remaining (possibly child) modules
     * @throws SAXException error producing the sax events
     */
    private static void writeModule(Module module, TransformerHandler xmlOut,
            Severity parentSeverity, List<Module> remainingModules) throws SAXException {

        Severity severity = parentSeverity;

        // remove this module from the list of modules to write
        remainingModules.remove(module);

        List<Module> childs = getChildModules(module, remainingModules);

        String emptyString = new String();

        // Start the module
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute(emptyString, XMLTags.NAME_TAG, XMLTags.NAME_TAG, emptyString, module
                .getMetaData().getInternalName());
        xmlOut.startElement(emptyString, XMLTags.MODULE_TAG, XMLTags.MODULE_TAG, attr);

        // Write comment
        if (StringUtils.trimToNull(module.getComment()) != null) {
            attr = new AttributesImpl();
            attr.addAttribute(emptyString, XMLTags.NAME_TAG, XMLTags.NAME_TAG, emptyString,
                    XMLTags.COMMENT_ID);
            attr.addAttribute(emptyString, XMLTags.VALUE_TAG, XMLTags.VALUE_TAG, emptyString,
                    module.getComment());
            xmlOut.startElement(emptyString, XMLTags.METADATA_TAG, XMLTags.METADATA_TAG, attr);
            xmlOut.endElement(emptyString, XMLTags.METADATA_TAG, XMLTags.METADATA_TAG);
        }

        // Write custom message
        if (StringUtils.trimToNull(module.getCustomMessage()) != null) {
            attr = new AttributesImpl();
            attr.addAttribute(emptyString, XMLTags.NAME_TAG, XMLTags.NAME_TAG, emptyString,
                    XMLTags.CUSTOM_MESSAGE_ID);
            attr.addAttribute(emptyString, XMLTags.VALUE_TAG, XMLTags.VALUE_TAG, emptyString,
                    module.getCustomMessage());
            xmlOut.startElement(emptyString, XMLTags.METADATA_TAG, XMLTags.METADATA_TAG, attr);
            xmlOut.endElement(emptyString, XMLTags.METADATA_TAG, XMLTags.METADATA_TAG);
        }

        // Write last enabled severity level
        if (module.getLastEnabledSeverity() != null) {
            attr = new AttributesImpl();
            attr.addAttribute(emptyString, XMLTags.NAME_TAG, XMLTags.NAME_TAG, emptyString,
                    XMLTags.LAST_ENABLED_SEVERITY_ID);
            attr.addAttribute(emptyString, XMLTags.VALUE_TAG, XMLTags.VALUE_TAG, emptyString,
                    module.getLastEnabledSeverity().name());
            xmlOut.startElement(emptyString, XMLTags.METADATA_TAG, XMLTags.METADATA_TAG, attr);
            xmlOut.endElement(emptyString, XMLTags.METADATA_TAG, XMLTags.METADATA_TAG);
        }

        // write custom metadata
        for (Map.Entry<String, String> entry : module.getCustomMetaData().entrySet()) {

            String name = entry.getKey();
            String value = entry.getValue();

            attr = new AttributesImpl();
            attr.addAttribute(emptyString, XMLTags.NAME_TAG, XMLTags.NAME_TAG, emptyString, name);
            attr
                    .addAttribute(emptyString, XMLTags.VALUE_TAG, XMLTags.VALUE_TAG, emptyString,
                            value);
            xmlOut.startElement(emptyString, XMLTags.METADATA_TAG, XMLTags.METADATA_TAG, attr);
            xmlOut.endElement(emptyString, XMLTags.METADATA_TAG, XMLTags.METADATA_TAG);
        }

        // Write severity only if it differs from the parents severity
        if (module.getSeverity() != null && !module.getSeverity().equals(parentSeverity)) {

            attr = new AttributesImpl();
            attr.addAttribute(emptyString, XMLTags.NAME_TAG, XMLTags.NAME_TAG, emptyString,
                    XMLTags.SEVERITY_TAG);
            attr.addAttribute(emptyString, XMLTags.VALUE_TAG, XMLTags.VALUE_TAG, emptyString,
                    module.getSeverity().name());

            xmlOut.startElement(emptyString, XMLTags.PROPERTY_TAG, XMLTags.PROPERTY_TAG, attr);
            xmlOut.endElement(emptyString, XMLTags.PROPERTY_TAG, XMLTags.PROPERTY_TAG);

            // set the parent severity for child modules
            severity = module.getSeverity();
        }

        // write module id
        if (StringUtils.trimToNull(module.getId()) != null) {
            attr = new AttributesImpl();
            attr.addAttribute(emptyString, XMLTags.NAME_TAG, XMLTags.NAME_TAG, emptyString,
                    XMLTags.ID_TAG);
            attr.addAttribute(emptyString, XMLTags.VALUE_TAG, XMLTags.VALUE_TAG, emptyString,
                    module.getId());

            xmlOut.startElement(emptyString, XMLTags.PROPERTY_TAG, XMLTags.PROPERTY_TAG, attr);
            xmlOut.endElement(emptyString, XMLTags.PROPERTY_TAG, XMLTags.PROPERTY_TAG);
        }

        // write properties of the module
        for (ConfigProperty property : module.getProperties()) {

            // write property only if it differs from the default value
            String value = StringUtils.trimToNull(property.getValue());
            if (value != null
                    && !ObjectUtils.equals(value, property.getMetaData().getDefaultValue())) {
                attr = new AttributesImpl();
                attr.addAttribute(emptyString, XMLTags.NAME_TAG, XMLTags.NAME_TAG, emptyString,
                        property.getMetaData().getName());
                attr.addAttribute(emptyString, XMLTags.VALUE_TAG, XMLTags.VALUE_TAG, emptyString,
                        property.getValue());

                xmlOut.startElement(emptyString, XMLTags.PROPERTY_TAG, XMLTags.PROPERTY_TAG, attr);
                xmlOut.endElement(emptyString, XMLTags.PROPERTY_TAG, XMLTags.PROPERTY_TAG);
            }
        }

        // write child modules recursivly
        for (Module child : childs) {
            writeModule(child, xmlOut, severity, remainingModules);
        }

        xmlOut.endElement(emptyString, XMLTags.MODULE_TAG, XMLTags.MODULE_TAG);
    }

    /**
     * Returns a list of child modules outgoing from a module.
     * 
     * @param module the parent module
     * @param remainingModules the list of modules that are yet not written
     * @return the list of child modules
     */
    private static List<Module> getChildModules(Module module, List<Module> remainingModules) {

        List<Module> childModules = new ArrayList<Module>();

        for (Module tmp : remainingModules) {

            String parentInternalName = module != null ? module.getMetaData().getInternalName()
                    : null;
            String childParent = tmp.getMetaData().getParentModule();

            // only the checker module has no parent
            if (parentInternalName == null && childParent.equals("Root")) //$NON-NLS-1$
            {
                childModules.add(tmp);
            }
            else if (childParent.equals(parentInternalName)) {
                childModules.add(tmp);
            }
        }

        return childModules;
    }
}