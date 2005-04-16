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

//=================================================
// Imports from java namespace
//=================================================
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.sax.TransformerHandler;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.ConfigurationTypes;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.IConfigurationType;
import com.atlassw.tools.eclipse.checkstyle.config.migration.CheckConfigurationMigrator;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.XMLUtil;

/**
 * Used to manage the life cycle of <code>CheckConfiguration</code> objects.
 */
public final class CheckConfigurationFactory
{
    //=================================================
    // Public static final variables.
    //=================================================

    //=================================================
    // Static class variables.
    //=================================================

    private static final String DEFAULT_CHECK_CONFIGS = "DefaultCheckConfigurations.xml";

    private static final String CHECKSTYLE_CONFIG_FILE = "checkstyle-config.xml";

    private static final String VERSION_5_0_0 = "5.0.0";

    private static final String CURRENT_CONFIG_FILE_FORMAT_VERSION = VERSION_5_0_0;

    /**
     * List of known check configurations. Synchronized because of possible
     * concurrend access.
     */
    private static List sConfigurations = Collections.synchronizedList(new ArrayList());

    static
    {
        try
        {
            loadFromPersistence();
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error("Error loading the internal check configurations", e);
        }
    }

    //=================================================
    // Instance member variables.
    //=================================================

    //=================================================
    // Constructors & finalizer.
    //=================================================

    private CheckConfigurationFactory()
    {}

    //=================================================
    // Methods.
    //=================================================

    /**
     * Get an <code>CheckConfiguration</code> instance by its name.
     * 
     * @param name Name of the requested instance.
     * 
     * @return The requested instance or <code>null</code> if the named
     *         instance could not be found.
     * 
     * @throws CheckstylePluginException Error during processing.
     */
    public static ICheckConfiguration getByName(String name) throws CheckstylePluginException
    {
        ICheckConfiguration config = null;
        Iterator it = sConfigurations.iterator();
        while (it.hasNext())
        {
            ICheckConfiguration tmp = (ICheckConfiguration) it.next();
            if (tmp.getName().equals(name))
            {
                config = tmp;
                break;
            }
        }

        return config;
    }

    /**
     * Get a list of the currently defined check configurations.
     * 
     * @return A list containing all instances.
     * 
     * @throws CheckstylePluginException Error during processing.
     */
    public static List getCheckConfigurations() throws CheckstylePluginException
    {
        return sConfigurations;
    }

    /**
     * Set the list of defined check configurations.
     * 
     * @param configs the configurations
     * @throws CheckstylePluginException Error while storing the configurations
     */
    public static void setCheckConfigurations(List configs) throws CheckstylePluginException
    {
        sConfigurations.clear();
        sConfigurations.addAll(configs);
        storeToPersistence(sConfigurations);
    }

    /**
     * Check to see if a check configuration is using an already existing name.
     * 
     * @param configuration The check configuration
     * 
     * @return <code>true</code>= in use, <code>false</code>= not in use.
     */
    public static boolean isNameCollision(ICheckConfiguration configuration)

    {
        boolean result = false;
        Iterator it = sConfigurations.iterator();
        while (it.hasNext())
        {
            ICheckConfiguration tmp = (ICheckConfiguration) it.next();
            if (tmp.getId() != configuration.getId()
                    && tmp.getName().equals(configuration.getName()))
            {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Copy the checkstyle configuration of a check configuration into another
     * configuration.
     * 
     * @param source the source check configuration
     * @param target the target check configuartion
     * @throws CheckstylePluginException Error copying the configuration
     */
    public static void copyConfiguration(ICheckConfiguration source, ICheckConfiguration target)
        throws CheckstylePluginException
    {
        //use the export function ;-)
        String targetFile = target.getCheckstyleConfigurationURL().getFile();
        exportConfiguration(new File(targetFile), source);
    }

    /**
     * Write check configurations to an external file in standard Checkstyle
     * format.
     * 
     * @param file File to write too.
     * 
     * @param config List of check configurations to write out.
     * 
     * @throws CheckstylePluginException Error during export.
     */
    public static void exportConfiguration(File file, ICheckConfiguration config)
        throws CheckstylePluginException
    {

        InputStream in = null;
        OutputStream out = null;

        try
        {

            //Just copy the checkstyle configuration
            URL configUrl = config.getCheckstyleConfigurationURL();

            in = new BufferedInputStream(configUrl.openStream());
            out = new BufferedOutputStream(new FileOutputStream(file));

            byte[] buf = new byte[512];
            int i = 0;
            while ((i = in.read(buf)) > -1)
            {
                out.write(buf, 0, i);
            }
        }
        catch (Exception e)
        {
            throw new CheckstylePluginException(e.getLocalizedMessage(), e);
        }
        finally
        {

            try
            {
                in.close();
            }
            catch (IOException e1)
            {
                //NOOP
            }
            try
            {
                out.close();
            }
            catch (IOException e2)
            {
                //NOOP
            }
        }
    }

    /**
     * Load the check configurations from the persistent state storage.
     */
    private static void loadFromPersistence() throws CheckstylePluginException
    {

        InputStream inStream = null;

        try
        {

            IPath configPath = CheckstylePlugin.getDefault().getStateLocation();
            configPath = configPath.append(CHECKSTYLE_CONFIG_FILE);
            File configFile = configPath.toFile();

            //
            //  Make sure the files exists, it might not.
            //
            if (!configFile.exists())
            {
                //load from plugins default configuration
                Path defaultsPath = new Path(DEFAULT_CHECK_CONFIGS);
                inStream = CheckstylePlugin.getDefault().openStream(defaultsPath);
            }
            else
            {
                inStream = new BufferedInputStream(new FileInputStream(configFile));
            }

            CheckConfigurationsFileHandler handler = new CheckConfigurationsFileHandler();
            XMLUtil.parseWithSAX(inStream, handler);

            if (handler.isOldFileFormat())
            {
                migrate();
            }
            else
            {
                sConfigurations.addAll(handler.getConfigurations());
            }
        }
        catch (Exception e)
        {
            String message = "Failed to read internal check configuration file";
            throw new CheckstylePluginException(message, e);
        }

        finally
        {
            if (inStream != null)
            {
                try
                {
                    inStream.close();
                }
                catch (Exception e)
                {
                    //  Nothing can be done about it.
                }
            }
        }
    }

    private static void migrate() throws CheckstylePluginException
    {

        InputStream inStream = null;
        InputStream defaultConfigStream = null;

        try
        {

            //get input stream to the default configuration
            Path defaultsPath = new Path(DEFAULT_CHECK_CONFIGS);
            defaultConfigStream = CheckstylePlugin.getDefault().openStream(defaultsPath);

            CheckConfigurationsFileHandler handler = new CheckConfigurationsFileHandler();
            XMLUtil.parseWithSAX(defaultConfigStream, handler);
            List defaultConfigs = handler.getConfigurations();

            //set configurations to make duplicate name detection possible
            sConfigurations.addAll(defaultConfigs);

            //get inputstream to the current oldstyle config
            IPath configPath = CheckstylePlugin.getDefault().getStateLocation();
            configPath = configPath.append(CHECKSTYLE_CONFIG_FILE);
            File configFile = configPath.toFile();
            inStream = new BufferedInputStream(new FileInputStream(configFile));

            List migratedConfigs = CheckConfigurationMigrator.getMigratedConfigurations(inStream);

            //store all configurations
            defaultConfigs.addAll(migratedConfigs);
            setCheckConfigurations(defaultConfigs);

        }
        catch (Exception e)
        {
            String message = "Failed to migrate old check configurations.";
            throw new CheckstylePluginException(message, e);
        }

        finally
        {

            try
            {
                inStream.close();
            }
            catch (Exception e)
            {
                //  Nothing can be done about it.
            }
            try
            {
                defaultConfigStream.close();
            }
            catch (Exception e)
            {
                //  Nothing can be done about it.
            }
        }
    }

    /**
     * Store the check configurations to the persistent state storage.
     */
    private static void storeToPersistence(List configurations) throws CheckstylePluginException
    {

        BufferedOutputStream out = null;
        ByteArrayOutputStream byteOut = null;
        try
        {

            IPath configPath = CheckstylePlugin.getDefault().getStateLocation();
            configPath = configPath.append(CHECKSTYLE_CONFIG_FILE);
            File configFile = configPath.toFile();

            byteOut = new ByteArrayOutputStream();

            //Write the configuration document by pushing sax events through
            // the transformer handler
            TransformerHandler xmlOut = XMLUtil.writeWithSax(byteOut);

            writeConfigurations(xmlOut);

            //write to the file after the serialization was successful
            //prevents corrupted files in case of error
            out = new BufferedOutputStream(new FileOutputStream(configFile));
            out.write(byteOut.toByteArray());
        }
        catch (Exception e)
        {
            String message = "Failed to write check configurations file";
            throw new CheckstylePluginException(message, e);
        }
        finally
        {
            try
            {
                byteOut.close();
            }
            catch (Exception e1)
            {
                //can nothing do about it
            }
            try
            {
                out.close();
            }
            catch (Exception e1)
            {
                //can nothing do about it
            }
        }
    }

    /**
     * Writes to check configurations through the transformer handler by passing
     * SAX events to it.
     * 
     * @param handler the transformer handler
     * @throws SAXException error writing the configurations
     */
    private static void writeConfigurations(TransformerHandler handler) throws SAXException
    {

        handler.startDocument();
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", XMLTags.VERSION_TAG, XMLTags.VERSION_TAG, null,
                CURRENT_CONFIG_FILE_FORMAT_VERSION);

        handler.startElement("", XMLTags.CHECKSTYLE_ROOT_TAG, XMLTags.CHECKSTYLE_ROOT_TAG, attrs);
        handler.ignorableWhitespace(new char[] { '\n' }, 0, 1);

        Iterator it = sConfigurations.iterator();
        while (it.hasNext())
        {

            ICheckConfiguration config = (ICheckConfiguration) it.next();

            attrs = new AttributesImpl();
            attrs.addAttribute("", XMLTags.NAME_TAG, XMLTags.NAME_TAG, null, config.getName());
            attrs.addAttribute("", XMLTags.LOCATION_TAG, XMLTags.LOCATION_TAG, null, config
                    .getLocation());
            attrs.addAttribute("", XMLTags.TYPE_TAG, XMLTags.TYPE_TAG, null, config.getType()
                    .getInternalName());
            if (config.getDescription() != null)
            {
                attrs.addAttribute("", XMLTags.DESCRIPTION_TAG, XMLTags.DESCRIPTION_TAG, null,
                        config.getDescription());
            }

            handler.startElement("", XMLTags.CHECK_CONFIG_TAG, XMLTags.CHECK_CONFIG_TAG, attrs);
            handler.endElement("", XMLTags.CHECK_CONFIG_TAG, XMLTags.CHECK_CONFIG_TAG);
            handler.ignorableWhitespace(new char[] { '\n' }, 0, 1);
        }

        handler.endElement("", XMLTags.CHECKSTYLE_ROOT_TAG, XMLTags.CHECKSTYLE_ROOT_TAG);
        handler.endDocument();
    }

    /**
     * SAX-DefaultHandler for parsing the check-configurations file.
     * 
     * @author Lars Ködderitzsch
     */
    private static class CheckConfigurationsFileHandler extends DefaultHandler
    {
        /** the configurations read from the xml. */
        private List mConfigurations = new ArrayList();

        /** Flags if the old plugin configuration file format was detected. */
        private boolean mOldFileFormat;

        /**
         * Return the configurations this handler built.
         * 
         * @return the configurations
         */
        public List getConfigurations()
        {
            return mConfigurations;
        }

        /**
         * Returns if the old plugin file format was detected.
         * 
         * @return <code>true</code> if the old file format was detected
         */
        public boolean isOldFileFormat()
        {
            return mOldFileFormat;
        }

        /**
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
         *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException
        {

            if (XMLTags.CHECKSTYLE_ROOT_TAG.equals(qName))
            {
                String version = attributes.getValue(XMLTags.VERSION_TAG);

                if (!CURRENT_CONFIG_FILE_FORMAT_VERSION.equals(version))
                {
                    mOldFileFormat = true;
                }
            }

            else if (!mOldFileFormat && XMLTags.CHECK_CONFIG_TAG.equals(qName))
            {
                try
                {
                    String name = attributes.getValue(XMLTags.NAME_TAG);
                    String description = attributes.getValue(XMLTags.DESCRIPTION_TAG);
                    String location = attributes.getValue(XMLTags.LOCATION_TAG);
                    String type = attributes.getValue(XMLTags.TYPE_TAG);

                    IConfigurationType configType = ConfigurationTypes.getByInternalName(type);

                    ICheckConfiguration checkConfig = (ICheckConfiguration) configType
                            .getImplementationClass().newInstance();
                    checkConfig.initialize(name, location, configType, description);
                    mConfigurations.add(checkConfig);
                }
                catch (Exception e)
                {
                    throw new SAXException(e);
                }
            }
        }
    }
}