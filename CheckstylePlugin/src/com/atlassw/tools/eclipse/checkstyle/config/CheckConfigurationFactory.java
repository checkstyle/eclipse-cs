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

//=================================================
// Imports from java namespace
//=================================================
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.sax.TransformerHandler;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.ErrorMessages;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.BuiltInCheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.ConfigurationTypes;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.IConfigurationType;
import com.atlassw.tools.eclipse.checkstyle.config.migration.CheckConfigurationMigrator;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.FileSet;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.ProjectConfiguration;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.ProjectConfigurationFactory;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.XMLUtil;

/**
 * Used to manage the life cycle of <code>CheckConfiguration</code> objects.
 */
public final class CheckConfigurationFactory
{
    // =================================================
    // Public static final variables.
    // =================================================

    // =================================================
    // Static class variables.
    // =================================================

    /** Name of the internal file storing the plugin check configurations. */
    private static final String CHECKSTYLE_CONFIG_FILE = "checkstyle-config.xml"; //$NON-NLS-1$

    /** Name of the actual config file version. */
    private static final String VERSION_5_0_0 = "5.0.0"; //$NON-NLS-1$

    /** The current file version. */
    private static final String CURRENT_CONFIG_FILE_FORMAT_VERSION = VERSION_5_0_0;

    /** constant for the extension point id. */
    private static final String CONFIGS_EXTENSION_POINT = CheckstylePlugin.PLUGIN_ID
            + ".configurations"; //$NON-NLS-1$

    /**
     * List of known check configurations. Synchronized because of possible
     * concurrend access.
     */
    private static List sConfigurations = Collections.synchronizedList(new ArrayList());

    static
    {
        try
        {
            loadBuiltinConfigurations();
            loadFromPersistence();
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.log(e);
        }
    }

    // =================================================
    // Instance member variables.
    // =================================================

    // =================================================
    // Constructors & finalizer.
    // =================================================

    private CheckConfigurationFactory()
    {}

    // =================================================
    // Methods.
    // =================================================

    /**
     * Get an <code>CheckConfiguration</code> instance by its name.
     * 
     * @param name Name of the requested instance.
     * 
     * @return The requested instance or <code>null</code> if the named
     *         instance could not be found.
     */
    public static ICheckConfiguration getByName(String name)
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
     */
    public static List getCheckConfigurations()
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
        updateProjectConfigurations(configs);
        storeToPersistence(configs);

        sConfigurations.clear();

        loadBuiltinConfigurations();
        loadFromPersistence();
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
            if (!(tmp == configuration || tmp == configuration.getOriginalCheckConfig())
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
        // use the export function ;-)
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

            // Just copy the checkstyle configuration
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
            CheckstylePluginException.rethrow(e);
        }
        finally
        {

            try
            {
                in.close();
            }
            catch (Exception e1)
            {
                // NOOP
            }
            try
            {
                out.close();
            }
            catch (Exception e2)
            {
                // NOOP
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
            // Make sure the files exists, it might not.
            //
            if (!configFile.exists())
            {
                return;
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
            CheckstylePluginException.rethrow(e, ErrorMessages.errorLoadingConfigFile);
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
                    // Nothing can be done about it.
                }
            }
        }
    }

    /**
     * Loads the built-in check configurations defined in plugin.xml or custom
     * fragments.
     */
    private static void loadBuiltinConfigurations()
    {

        IExtensionRegistry pluginRegistry = Platform.getExtensionRegistry();

        IConfigurationElement[] elements = pluginRegistry
                .getConfigurationElementsFor(CONFIGS_EXTENSION_POINT);

        for (int i = 0; i < elements.length; i++)
        {
            String name = elements[i].getAttribute(XMLTags.NAME_TAG);
            String description = elements[i].getAttribute(XMLTags.DESCRIPTION_TAG);
            String location = elements[i].getAttribute(XMLTags.LOCATION_TAG);

            IConfigurationType configType = ConfigurationTypes.getByInternalName("builtin");

            try
            {
                ICheckConfiguration checkConfig = (ICheckConfiguration) configType
                        .getImplementationClass().newInstance();

                checkConfig.initialize(name, location, configType, description);
                sConfigurations.add(checkConfig);
            }
            catch (InstantiationException e)
            {
                CheckstyleLog.log(e);
            }
            catch (IllegalAccessException e)
            {
                CheckstyleLog.log(e);
            }
            catch (CheckstylePluginException e)
            {
                CheckstyleLog.log(e);
            }
        }
    }

    private static void migrate() throws CheckstylePluginException
    {

        InputStream inStream = null;
        InputStream defaultConfigStream = null;

        try
        {

            // load builtin configurations to make duplicate name detection
            // possible
            loadBuiltinConfigurations();

            // get inputstream to the current oldstyle config
            IPath configPath = CheckstylePlugin.getDefault().getStateLocation();
            configPath = configPath.append(CHECKSTYLE_CONFIG_FILE);
            File configFile = configPath.toFile();
            inStream = new BufferedInputStream(new FileInputStream(configFile));

            List migratedConfigs = CheckConfigurationMigrator.getMigratedConfigurations(inStream);

            // store all configurations
            setCheckConfigurations(migratedConfigs);

        }
        catch (Exception e)
        {
            CheckstylePluginException.rethrow(e, ErrorMessages.errorMigratingConfig);
        }

        finally
        {

            try
            {
                inStream.close();
            }
            catch (Exception e)
            {
                // Nothing can be done about it.
            }
            try
            {
                defaultConfigStream.close();
            }
            catch (Exception e)
            {
                // Nothing can be done about it.
            }
        }
    }

    /**
     * Updates the project configurations that use the changed check
     * configurations.
     * 
     * @param configurations the check configurations
     * @throws CheckstylePluginException an unexpected exception occurred
     */
    private static void updateProjectConfigurations(List configurations)
        throws CheckstylePluginException
    {
        Iterator it = configurations.iterator();
        while (it.hasNext())
        {

            ICheckConfiguration checkConfig = (ICheckConfiguration) it.next();

            ICheckConfiguration original = checkConfig.getOriginalCheckConfig();

            // only if the name of the check config differs from the original
            if (original != null)
            {

                List projects = ProjectConfigurationFactory.getProjectsUsingConfig(original
                        .getName());
                Iterator it2 = projects.iterator();

                while (it2.hasNext())
                {

                    IProject project = (IProject) it2.next();
                    ProjectConfiguration projectConfig = ProjectConfigurationFactory
                            .getConfiguration(project);

                    List fileSets = projectConfig.getFileSets();
                    Iterator it3 = fileSets.iterator();
                    while (it3.hasNext())
                    {
                        FileSet fileSet = (FileSet) it3.next();

                        // Check if the fileset uses the check config
                        if (original.getName().equals(fileSet.getCheckConfigName()))
                        {
                            // set the new check configuration
                            fileSet.setCheckConfig(checkConfig);
                        }
                    }

                    // store the project configuration
                    ProjectConfigurationFactory.setConfiguration(projectConfig, project);
                }
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

            // Write the configuration document by pushing sax events through
            // the transformer handler
            TransformerHandler xmlOut = XMLUtil.writeWithSax(byteOut);

            writeConfigurations(xmlOut, configurations);

            // write to the file after the serialization was successful
            // prevents corrupted files in case of error
            out = new BufferedOutputStream(new FileOutputStream(configFile));
            out.write(byteOut.toByteArray());
        }
        catch (Exception e)
        {
            CheckstylePluginException.rethrow(e, ErrorMessages.errorWritingConfigFile);
        }
        finally
        {
            try
            {
                byteOut.close();
            }
            catch (Exception e1)
            {
                // can nothing do about it
            }
            try
            {
                out.close();
            }
            catch (Exception e1)
            {
                // can nothing do about it
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
    private static void writeConfigurations(TransformerHandler handler, List configurations)
        throws SAXException
    {

        handler.startDocument();
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute(new String(), XMLTags.VERSION_TAG, XMLTags.VERSION_TAG, null,
                CURRENT_CONFIG_FILE_FORMAT_VERSION);

        handler.startElement(new String(), XMLTags.CHECKSTYLE_ROOT_TAG,
                XMLTags.CHECKSTYLE_ROOT_TAG, attrs);
        handler.ignorableWhitespace(new char[] { '\n' }, 0, 1);

        Iterator it = configurations.iterator();
        while (it.hasNext())
        {

            ICheckConfiguration config = (ICheckConfiguration) it.next();

            // don't store built-in configurations to persistence
            if (config instanceof BuiltInCheckConfiguration)
            {
                continue;
            }

            attrs = new AttributesImpl();
            attrs.addAttribute(new String(), XMLTags.NAME_TAG, XMLTags.NAME_TAG, null, config
                    .getName());
            attrs.addAttribute(new String(), XMLTags.LOCATION_TAG, XMLTags.LOCATION_TAG, null,
                    config.getLocation());
            attrs.addAttribute(new String(), XMLTags.TYPE_TAG, XMLTags.TYPE_TAG, null, config
                    .getType().getInternalName());
            if (config.getDescription() != null)
            {
                attrs.addAttribute(new String(), XMLTags.DESCRIPTION_TAG, XMLTags.DESCRIPTION_TAG,
                        null, config.getDescription());
            }

            handler.startElement(new String(), XMLTags.CHECK_CONFIG_TAG, XMLTags.CHECK_CONFIG_TAG,
                    attrs);
            handler.endElement(new String(), XMLTags.CHECK_CONFIG_TAG, XMLTags.CHECK_CONFIG_TAG);
            handler.ignorableWhitespace(new char[] { '\n' }, 0, 1);
        }

        handler.endElement(new String(), XMLTags.CHECKSTYLE_ROOT_TAG, XMLTags.CHECKSTYLE_ROOT_TAG);
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