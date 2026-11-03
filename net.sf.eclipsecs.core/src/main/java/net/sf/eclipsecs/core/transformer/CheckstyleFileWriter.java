///////////////////////////////////////////////////////////////////////////////////////////////
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
///////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.eclipsecs.core.transformer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import net.sf.eclipsecs.core.config.XmlTags;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.XmlUtil;

/**
 * Class for writing the checkstyle configuration to a xml-file.
 *
 *
 */
public final class CheckstyleFileWriter {

    /** An object containing all settings for the checkstyle-file. */
    private final CheckstyleSetting checkstyleSetting;
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
        checkstyleSetting = setting;
        this.file = file;
    }

    /**
     * Writes the configured checkstyle settings to an XML file.
     */
    public void writeXmlFile() {
        try (FileOutputStream fw = new FileOutputStream(file)) {
            writeXmlFile(fw);
        }
        catch (final IOException exception) {
            CheckstyleLog.log(exception);
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
    private void writeXmlFile(final OutputStream outStream) throws IOException {
        final Document document = DocumentHelper.createDocument();

        final Element checkerElement = document.addElement(XmlTags.MODULE_TAG)
            .addAttribute(XmlTags.NAME_TAG, XmlTags.CHECKER_MODULE);
        checkerElement.addElement(XmlTags.PROPERTY_TAG).addAttribute(XmlTags.NAME_TAG,
            XmlTags.SEVERITY_TAG).addAttribute(XmlTags.VALUE_TAG, "warning");

        writeModules(checkstyleSetting.getmCheckerModules(), checkerElement);

        final Element treeWalkerElement = checkerElement.addElement(XmlTags.MODULE_TAG)
            .addAttribute(XmlTags.NAME_TAG, XmlTags.TREEWALKER_MODULE);

        writeModules(checkstyleSetting.getmTreeWalkerModules(), treeWalkerElement);

        outStream.write(XmlUtil.toByteArray(document));
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
            final Element moduleElement = parentElement.addElement(XmlTags.MODULE_TAG);
            moduleElement.addAttribute(XmlTags.NAME_TAG, module.getKey());

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
            final Element propertyElement = moduleElement.addElement(XmlTags.PROPERTY_TAG);
            propertyElement.addAttribute(XmlTags.NAME_TAG, property.getKey());
            propertyElement.addAttribute(XmlTags.VALUE_TAG, property.getValue());
        }
    }

}
