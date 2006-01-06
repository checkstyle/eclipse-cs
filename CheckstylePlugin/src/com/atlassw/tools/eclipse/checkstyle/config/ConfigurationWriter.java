//============================================================================
//
// Copyright (C) 2002-2005  David Schneider, Lars Ködderitzsch
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

package com.atlassw.tools.eclipse.checkstyle.config;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.atlassw.tools.eclipse.checkstyle.ErrorMessages;
import com.atlassw.tools.eclipse.checkstyle.config.savefilter.SaveFilters;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.XMLUtil;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

/**
 * Writes the modules of a checkstyle configuration to an output stream.
 * 
 * @author Lars Ködderitzsch
 */
public final class ConfigurationWriter
{
    //
    // constants
    //

    //
    // constructors
    //

    /** Hidden default constructor. */
    private ConfigurationWriter()
    {

    }

    //
    // methods
    //

    /**
     * Writes a new checkstyle configuration to the output stream.
     * 
     * @param out the output stream to write to
     * @param checkConfig the Check configuration object
     * @throws CheckstylePluginException error writing the checkstyle
     *             configuration
     */
    public static void writeNewConfiguration(OutputStream out, ICheckConfiguration checkConfig)
        throws CheckstylePluginException
    {

        // write an empty list of modules
        // mandatory modules are added automatically
        write(out, new ArrayList(), checkConfig);
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
    public static void write(OutputStream out, List modules, ICheckConfiguration checkConfig)
        throws CheckstylePluginException
    {

        try
        {
            // pass the configured modules through the save filters
            SaveFilters.process(modules);

            TransformerHandler xmlOut = XMLUtil.writeWithSax(out);
            xmlOut.startDocument();

            String comment = "This configuration file was written by the eclipse-cs plugin configuration editor";
            xmlOut.comment(comment.toCharArray(), 0, comment.length());
            xmlOut.ignorableWhitespace(new char[] { '\n' }, 0, 1);
            xmlOut.startDTD(XMLTags.MODULE_TAG, "-//Puppy Crawl//DTD Check Configuration 1.2//EN", //$NON-NLS-1$
                    "http://www.puppycrawl.com/dtds/configuration_1_2.dtd"); //$NON-NLS-1$
            xmlOut.endDTD();
            xmlOut.ignorableWhitespace(new char[] { '\n' }, 0, 1);

            // write out name and description as comment
            String description = "\n" + checkConfig.getName() + ":\n"
                    + checkConfig.getDescription() + "\n";
            xmlOut.comment(description.toCharArray(), 0, description.length());
            xmlOut.ignorableWhitespace(new char[] { '\n' }, 0, 1);

            // find the root module (Checker)
            // the root module is the only module that has no parent
            List rootModules = getChildModules(null, modules);
            if (rootModules.size() < 1)
            {
                throw new CheckstylePluginException(ErrorMessages.errorNoRootModule);
            }

            if (rootModules.size() > 1)
            {
                throw new CheckstylePluginException(ErrorMessages.errorMoreThanOneRootModule);
            }

            writeModule((Module) rootModules.get(0), xmlOut, null, modules);

            xmlOut.endDocument();
        }
        catch (TransformerConfigurationException e)
        {
            CheckstylePluginException.rethrow(e);
        }
        catch (SAXException e)
        {
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
            SeverityLevel parentSeverity, List remainingModules) throws SAXException
    {

        SeverityLevel severity = parentSeverity;

        // remove this module from the list of modules to write
        remainingModules.remove(module);

        List childs = getChildModules(module, remainingModules);

        // Start the module
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute(new String(), XMLTags.NAME_TAG, XMLTags.NAME_TAG, null, module
                .getMetaData().getInternalName());
        xmlOut.startElement(new String(), XMLTags.MODULE_TAG, XMLTags.MODULE_TAG, attr);
        xmlOut.ignorableWhitespace(new char[] { '\n' }, 0, 1);

        // Write comment
        if (module.getComment() != null && module.getComment().trim().length() != 0)
        {
            attr = new AttributesImpl();
            attr.addAttribute(new String(), XMLTags.NAME_TAG, XMLTags.NAME_TAG, null,
                    XMLTags.COMMENT_ID);
            attr.addAttribute(new String(), XMLTags.VALUE_TAG, XMLTags.VALUE_TAG, null, module
                    .getComment());
            xmlOut.startElement(new String(), XMLTags.METADATA_TAG, XMLTags.METADATA_TAG, attr);
            xmlOut.endElement(new String(), XMLTags.METADATA_TAG, XMLTags.METADATA_TAG);
            xmlOut.ignorableWhitespace(new char[] { '\n' }, 0, 1);
        }

        // Write last enabled severity level
        if (module.getLastEnabledSeverity() != null)
        {
            attr = new AttributesImpl();
            attr.addAttribute(new String(), XMLTags.NAME_TAG, XMLTags.NAME_TAG, null,
                    XMLTags.LAST_ENABLED_SEVERITY_ID);
            attr.addAttribute(new String(), XMLTags.VALUE_TAG, XMLTags.VALUE_TAG, null, module
                    .getLastEnabledSeverity().getName());
            xmlOut.startElement(new String(), XMLTags.METADATA_TAG, XMLTags.METADATA_TAG, attr);
            xmlOut.endElement(new String(), XMLTags.METADATA_TAG, XMLTags.METADATA_TAG);
            xmlOut.ignorableWhitespace(new char[] { '\n' }, 0, 1);
        }

        // write custom metadata
        Iterator keys = module.getCustomMetaData().keySet().iterator();
        while (keys.hasNext())
        {

            String name = (String) keys.next();
            String value = (String) module.getCustomMetaData().get(name);

            attr = new AttributesImpl();
            attr.addAttribute(new String(), XMLTags.NAME_TAG, XMLTags.NAME_TAG, null, name);
            attr.addAttribute(new String(), XMLTags.VALUE_TAG, XMLTags.VALUE_TAG, null, value);
            xmlOut.startElement(new String(), XMLTags.METADATA_TAG, XMLTags.METADATA_TAG, attr);
            xmlOut.endElement(new String(), XMLTags.METADATA_TAG, XMLTags.METADATA_TAG);
            xmlOut.ignorableWhitespace(new char[] { '\n' }, 0, 1);
        }

        // Write severity only if it differs from the parents severity
        if (module.getSeverity() != null && !module.getSeverity().equals(parentSeverity))
        {

            attr = new AttributesImpl();
            attr.addAttribute(new String(), XMLTags.NAME_TAG, XMLTags.NAME_TAG, null,
                    XMLTags.SEVERITY_TAG);
            attr.addAttribute(new String(), XMLTags.VALUE_TAG, XMLTags.VALUE_TAG, null, module
                    .getSeverity().getName());

            xmlOut.startElement(new String(), XMLTags.PROPERTY_TAG, XMLTags.PROPERTY_TAG, attr);
            xmlOut.endElement(new String(), XMLTags.PROPERTY_TAG, XMLTags.PROPERTY_TAG);
            xmlOut.ignorableWhitespace(new char[] { '\n' }, 0, 1);

            // set the parent severity for child modules
            severity = module.getSeverity();
        }

        // write properties of the module
        List properties = module.getProperties();
        Iterator it = properties.iterator();
        while (it.hasNext())
        {
            ConfigProperty property = (ConfigProperty) it.next();
            // write property only if it differs from the default value
            if (property.getValue() != null && property.getValue().trim().length() != 0
                    && !property.getValue().equals(property.getMetaData().getDefaultValue()))
            {
                attr = new AttributesImpl();
                attr.addAttribute(new String(), XMLTags.NAME_TAG, XMLTags.NAME_TAG, null, property
                        .getMetaData().getName());
                attr.addAttribute(new String(), XMLTags.VALUE_TAG, XMLTags.VALUE_TAG, null,
                        property.getValue());

                xmlOut.startElement(new String(), XMLTags.PROPERTY_TAG, XMLTags.PROPERTY_TAG, attr);
                xmlOut.endElement(new String(), XMLTags.PROPERTY_TAG, XMLTags.PROPERTY_TAG);
                xmlOut.ignorableWhitespace(new char[] { '\n' }, 0, 1);
            }
        }

        // write child modules recursivly
        it = childs.iterator();
        while (it.hasNext())
        {
            writeModule((Module) it.next(), xmlOut, severity, remainingModules);
        }

        xmlOut.endElement(new String(), XMLTags.MODULE_TAG, XMLTags.MODULE_TAG);
        xmlOut.ignorableWhitespace(new char[] { '\n' }, 0, 1);
    }

    /**
     * Returns a list of child modules outgoing from a module.
     * 
     * @param module the parent module
     * @param remainingModules the list of modules that are yet not written
     * @return the list of child modules
     */
    private static List getChildModules(Module module, List remainingModules)
    {

        List childModules = new ArrayList();

        Iterator it = remainingModules.iterator();
        while (it.hasNext())
        {

            Module tmp = (Module) it.next();

            String parentInternalName = module != null ? module.getMetaData().getInternalName()
                    : null;
            String childParent = tmp.getMetaData().getParentModule();

            // only the checker module has no parent
            if (parentInternalName == null && childParent.equals("Root"))
            {
                childModules.add(tmp);
            }
            else if (childParent.equals(parentInternalName))
            {
                childModules.add(tmp);
            }
        }

        return childModules;
    }
}