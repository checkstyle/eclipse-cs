//============================================================================
//
// Copyright (C) 2003-2023  Lukas Frena
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

package net.sf.eclipsecs.core.transformer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import net.sf.eclipsecs.core.config.XMLTags;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.XMLUtil;

/**
 * Class for writing the checkstyle configuration to a xml-file.
 *
 *
 */
public final class CheckstyleFileWriter {

    /** An object containing all settings for the checkstyle-file. */
    private final CheckstyleSetting mCheckstyleSetting;
    /** The output file path. */
    private final String file;

    /**
     * Creates new instance of class CheckstyleFileWriter.
     *
     * @param setting
     *            The settings for the checkstyle-file.
     * @param file
     *            Path where the checkstyle-file should be stored.
     */
    public CheckstyleFileWriter(final CheckstyleSetting setting, final String file) {
        mCheckstyleSetting = setting;
        this.file = file;
    }

    public void writeXmlFile() {
        try (FileOutputStream fw = new FileOutputStream(file)) {
            writeXMLFile(fw);
        }
        catch (final IOException ex) {
            CheckstyleLog.log(ex);
        }
    }

    /**
     * Method for writing the xml-file.
     *
     * @param outStream
     *            BufferedWriter to outputfile.
     * @throws IOException
     *             an I/O exception occurred
     */
    private void writeXMLFile(final OutputStream outStream) throws IOException {
        final Document document = DocumentHelper.createDocument();

        final Element checkerElement = document.addElement(XMLTags.MODULE_TAG)
            .addAttribute(XMLTags.NAME_TAG, XMLTags.CHECKER_MODULE);
        checkerElement.addElement(XMLTags.PROPERTY_TAG).addAttribute(XMLTags.NAME_TAG,
            XMLTags.SEVERITY_TAG).addAttribute(XMLTags.VALUE_TAG, "warning");

        writeModules(mCheckstyleSetting.getmCheckerModules(), checkerElement);

        final Element treeWalkerElement = checkerElement.addElement(XMLTags.MODULE_TAG)
            .addAttribute(XMLTags.NAME_TAG, XMLTags.TREEWALKER_MODULE);

        writeModules(mCheckstyleSetting.getmTreeWalkerModules(), treeWalkerElement);

        outStream.write(XMLUtil.toByteArray(document));
    }

    /**
     * Method for writing all modules to a parent module element.
     *
     * @param modules
     *            the modules to write
     * @param parentElement
     *            the parent module element to add the modules to
     */
    private static void writeModules(final Map<String, Map<String, String>> modules,
        final Element parentElement) {

        for (Map.Entry<String, Map<String, String>> module : modules.entrySet()) {
            final Element moduleElement = parentElement.addElement(XMLTags.MODULE_TAG);
            moduleElement.addAttribute(XMLTags.NAME_TAG, module.getKey());

            if (module.getValue() != null) {
                writeProperty(module.getValue(), moduleElement);
            }
        }
    }

    /**
     * Method for writing all properties to a module element.
     *
     * @param properties
     *            A HashMap containing all properties.
     * @param moduleElement
     *            the module element to add the properties to
     */
    private static void writeProperty(final Map<String, String> properties,
        final Element moduleElement) {

        for (Map.Entry<String, String> property : properties.entrySet()) {
            final Element propertyElement = moduleElement.addElement(XMLTags.PROPERTY_TAG);
            propertyElement.addAttribute(XMLTags.NAME_TAG, property.getKey());
            propertyElement.addAttribute(XMLTags.VALUE_TAG, property.getValue());
        }
    }
}
