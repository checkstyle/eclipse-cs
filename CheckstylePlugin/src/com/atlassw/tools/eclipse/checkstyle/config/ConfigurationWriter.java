//============================================================================
//
// Copyright (C) 2002-2004  David Schneider
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.atlassw.tools.eclipse.checkstyle.config.meta.MetadataFactory;
import com.atlassw.tools.eclipse.checkstyle.config.meta.RuleMetadata;
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

    /** Array containing the internal names of the mandatory modules. */
    private static final String[] MANDATORY_MODULES = new String[] { XMLTags.CHECKER_MODULE,
        XMLTags.TREEWALKER_MODULE, XMLTags.FILECONTENTSHOLDER_MODULE };

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
     * @param out the output stream to write to *
     * @throws CheckstylePluginException error writing the checkstyle
     *             configuration
     */
    public static void writeNewConfiguration(OutputStream out) throws CheckstylePluginException
    {

        //write an empty list of modules
        //mandatory modules are added automatically
        write(out, new ArrayList());
    }

    /**
     * Writes the modules of the configuration to the output stream.
     * 
     * @param out the ouput stream.
     * @param modules the modules
     * @throws CheckstylePluginException error writing the checkstyle
     *             configuration
     */
    public static void write(OutputStream out, List modules) throws CheckstylePluginException
    {

        try
        {
            TransformerHandler xmlOut = XMLUtil.writeWithSax(out);
            xmlOut.startDocument();
            xmlOut.startDTD(XMLTags.MODULE_TAG, "-//Puppy Crawl//DTD Check Configuration 1.2//EN",
                    "http://www.puppycrawl.com/dtds/configuration_1_2.dtd");
            xmlOut.endDTD();
            xmlOut.ignorableWhitespace(new char[] { '\n' }, 0, 1);

            //check for mandatory modules if they are not present
            //they are automatically added
            checkForAndAddMandatoryModules(modules);

            //Sort modules because of
            //Checkstyle bug #1183749
            Collections.sort(modules, new ModuleComparator());

            //find the root module (Checker)
            //the root module is the only module that has no parent
            List rootModules = getChildModules(null, modules);
            if (rootModules.size() < 1)
            {
                throw new CheckstylePluginException("No root module found.");
            }

            if (rootModules.size() > 1)
            {
                throw new CheckstylePluginException("More than one root module found.");
            }

            writeModule((Module) rootModules.get(0), xmlOut, null, modules);

            xmlOut.endDocument();
        }
        catch (TransformerConfigurationException e)
        {
            throw new CheckstylePluginException(e.getLocalizedMessage(), e);
        }
        catch (SAXException e)
        {
            Exception ex = e.getException() != null ? e.getException() : e;
            throw new CheckstylePluginException(ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Checks if all mandatory modules are present. If not they are added.
     * 
     * @param modules the list of modules.
     */
    private static void checkForAndAddMandatoryModules(List modules)
    {
        for (int i = 0; i < MANDATORY_MODULES.length; i++)
        {

            if (!containsModule(modules, MANDATORY_MODULES[i]))
            {
                RuleMetadata metadata = MetadataFactory.getRuleMetadata(MANDATORY_MODULES[i]);
                modules.add(0, new Module(metadata));
            }
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

        //remove this module from the list of modules to write
        remainingModules.remove(module);

        //Start the module
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", XMLTags.NAME_TAG, XMLTags.NAME_TAG, null, module.getMetaData()
                .getInternalName());
        xmlOut.startElement("", XMLTags.MODULE_TAG, XMLTags.MODULE_TAG, attr);
        xmlOut.ignorableWhitespace(new char[] { '\n' }, 0, 1);

        //Write comment
        if (module.getComment() != null && module.getComment().trim().length() != 0)
        {
            attr = new AttributesImpl();
            attr.addAttribute("", XMLTags.NAME_TAG, XMLTags.NAME_TAG, null, XMLTags.COMMENT_ID);
            attr.addAttribute("", XMLTags.VALUE_TAG, XMLTags.VALUE_TAG, null, module.getComment());
            xmlOut.startElement("", XMLTags.METADATA_TAG, XMLTags.METADATA_TAG, attr);
            xmlOut.endElement("", XMLTags.METADATA_TAG, XMLTags.METADATA_TAG);
            xmlOut.ignorableWhitespace(new char[] { '\n' }, 0, 1);
        }

        //Write severity only if it differs from the parents severity
        if (module.getSeverity() != null && !module.getSeverity().equals(parentSeverity))
        {

            attr = new AttributesImpl();
            attr.addAttribute("", XMLTags.NAME_TAG, XMLTags.NAME_TAG, null, XMLTags.SEVERITY_TAG);
            attr.addAttribute("", XMLTags.VALUE_TAG, XMLTags.VALUE_TAG, null, module.getSeverity()
                    .getName());

            xmlOut.startElement("", XMLTags.PROPERTY_TAG, XMLTags.PROPERTY_TAG, attr);
            xmlOut.endElement("", XMLTags.PROPERTY_TAG, XMLTags.PROPERTY_TAG);
            xmlOut.ignorableWhitespace(new char[] { '\n' }, 0, 1);

            //set the parent severity for child modules
            severity = module.getSeverity();
        }

        //write properties of the module
        List properties = module.getProperties();
        Iterator it = properties.iterator();
        while (it.hasNext())
        {
            ConfigProperty property = (ConfigProperty) it.next();
            //write property only if it differs from the default value
            if (property.getValue() != null && property.getValue().trim().length() != 0
                    && !property.getValue().equals(property.getMetaData().getDefaultValue()))
            {
                attr = new AttributesImpl();
                attr.addAttribute("", XMLTags.NAME_TAG, XMLTags.NAME_TAG, null, property
                        .getMetaData().getName());
                attr.addAttribute("", XMLTags.VALUE_TAG, XMLTags.VALUE_TAG, null, property
                        .getValue());

                xmlOut.startElement("", XMLTags.PROPERTY_TAG, XMLTags.PROPERTY_TAG, attr);
                xmlOut.endElement("", XMLTags.PROPERTY_TAG, XMLTags.PROPERTY_TAG);
                xmlOut.ignorableWhitespace(new char[] { '\n' }, 0, 1);
            }
        }

        //write child modules recursivly
        List childs = getChildModules(module, remainingModules);
        it = childs.iterator();
        while (it.hasNext())
        {
            writeModule((Module) it.next(), xmlOut, severity, remainingModules);
        }

        xmlOut.endElement("", XMLTags.MODULE_TAG, XMLTags.MODULE_TAG);
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

            //only the checker module has no parent
            if (parentInternalName == null && childParent == null)
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

    /**
     * Check if the modules contains a certain module.
     * 
     * @param modules the modules
     * @param moduleName the internal name of the module to look for
     * @return <code>true</code> if the FileContentsHolder module is contained
     */
    private static boolean containsModule(List modules, String moduleName)
    {

        boolean result = false;

        Iterator it = modules.iterator();
        while (it.hasNext())
        {

            Module m = (Module) it.next();

            if (moduleName.equals(m.getMetaData().getInternalName()))
            {

                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Comparator to sort modules so that Checker and TreeWalker come first.
     * This is done because of a bug in SuppressionCommentFilter.
     * 
     * @author Lars Ködderitzsch
     */
    private static class ModuleComparator implements Comparator
    {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object o1, Object o2)
        {

            String internalName1 = ((Module) o1).getMetaData().getInternalName();
            String internalName2 = ((Module) o2).getMetaData().getInternalName();

            if (XMLTags.CHECKER_MODULE.equals(internalName1)
                    || XMLTags.TREEWALKER_MODULE.equals(internalName1)
                    || XMLTags.FILECONTENTSHOLDER_MODULE.equals(internalName1))
            {
                return -1;
            }
            else if (XMLTags.CHECKER_MODULE.equals(internalName2)
                    || XMLTags.TREEWALKER_MODULE.equals(internalName2)
                    || XMLTags.FILECONTENTSHOLDER_MODULE.equals(internalName2))
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }

    }
}