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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Properties;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.XMLUtil;

//=================================================
// Imports from org namespace
//=================================================
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *  Used to manage the life cycle of <code>CheckConfiguration</code>
 *  objects.
 */
public final class CheckConfigurationFactory
{
    //=================================================
    // Public static final variables.
    //=================================================

    //=================================================
    // Static class variables.
    //=================================================

    private static final String DEFAULT_CHECK_CONFIGS = "DefaultChecks.xml";

    private static final String CHECKSTYLE_CONFIG_FILE = "checkstyle-config.xml";

    private static final String VERSION_1_0_0 = "1.0.0";

    private static final String VERSION_3_2_0 = "3.2.0";
    
    private static final String VERSION_4_0_0 = "4.0.0";

    private static final String CURRENT_CONFIG_FILE_FORMAT_VERSION = VERSION_4_0_0;

    private static final String CLASSNAMES_V3_2_0_UPDATE =
        "com/atlassw/tools/eclipse/checkstyle/config/classnames_v3.2.0_update.properties";

    private static final String CLASSNAMES_V4_0_0_UPDATE =
       "com/atlassw/tools/eclipse/checkstyle/config/classnames_v4.0.0_update.properties";

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
     *  Get a new <code>CheckConfiguration</code> instance.
     * 
     *  @return  A new instance.
     * 
     *  @throws CheckstylePluginException  Error during processing.
     */
    public static CheckConfiguration getNewInstance() throws CheckstylePluginException
    {
        CheckConfiguration config = new CheckConfiguration();
        return config;
    }

    /**
     *  Get an <code>CheckConfiguration</code> instance by its name.
     * 
     *  @param  name  Name of the requested instance.
     * 
     *  @return  The requested instance or <code>null</code> if the named instance
     *            could not be found.
     * 
     *  @throws  CheckstylePluginException  Error during processing.
     */
    public static CheckConfiguration getByName(String name) throws CheckstylePluginException
    {
        CheckConfiguration result = null;
        if (name != null)
        {
            List configurations = loadFromPersistence();
            Iterator iter = configurations.iterator();
            while (iter.hasNext())
            {
                CheckConfiguration config = (CheckConfiguration)iter.next();
                if (config.getConfigName().equals(name))
                {
                    result = config;
                    break;
                }
            }
        }
        return result;
    }

    /**
     *  Get a list of the currently defined check configurations.
     * 
     *  @return  A list containing all instances.
     * 
     *  @throws  CheckstylePluginException  Error during processing.
     */
    public static List getCheckConfigurations() throws CheckstylePluginException
    {
        return loadFromPersistence();
    }

    /**
     *  Set the new collection of check configurations.
     * 
     *  @param  configs  List of <code>CheckConfiguration</code> objects.
     * 
     *  @throws  CheckstylePluginException  Error during processing.
     */
    public static void setCheckConfigurations(List configs) throws CheckstylePluginException
    {
        storeToPersistence(configs);
    }

    /**
     *  Add a new <code>CheckConfiguration</code> to the workspace.
     *
     *  @param  checkConfig  The check configuration to add.
     * 
     *  @throws  CheckstylePluginException  Error during processing.
     */
    public static void addCheckConfiguration(CheckConfiguration checkConfig)
        throws CheckstylePluginException
    {
        List configurations = loadFromPersistence();
        configurations.add(checkConfig);
        storeToPersistence(configurations);
    }

    /**
     *  Remove an <code>CheckConfiguration</code> from the workspace.
     *
     *  @param  checkConfig  The check configuration to remove.
     * 
     *  @throws  CheckstylePluginException  Error during processing.
     */
    public static void removeCheckConfiguration(CheckConfiguration checkConfig)
        throws CheckstylePluginException
    {
        List configurations = loadFromPersistence();
        configurations.remove(checkConfig);
        storeToPersistence(configurations);
    }

    /**
     *  Import check configurations from an external file in plug-in format.
     * 
     *  @param  file  File to load from.
     * 
     *  @return A list containing the import configurations.
     * 
     *  @throws  CheckstylePluginException  Error during processing.
     */
    public static List importPluginCheckConfigurations(File file) throws CheckstylePluginException
    {
        List result = null;
        if (file.exists())
        {
            result = loadFile(file);
        }
        return result;
    }

    /**
     *  Write check configurations to an external file in plug-in format.
     * 
     *  @param  file     File to write too.
     * 
     *  @param  config   The check configuration to write out.
     * 
     *  @throws  CheckstylePluginException  Error during processing.
     */
    public static void exportPluginCheckConfigurations(File file, CheckConfiguration config)
        throws CheckstylePluginException
    {
        LinkedList configs = new LinkedList();
        configs.add(config);
        writeFile(file, configs);
    }

    /**
     *  Write check configurations to an external file in standard
     *  Checkstyle format.
     * 
     *  @param  file     File to write too.
     * 
     *  @param  config  List of check configurations to write out.
     * 
     *  @throws  CheckstylePluginException  Error during processing.
     */
    public static void exportCheckstyleCheckConfigurations(File file, CheckConfiguration config)
        throws CheckstylePluginException
    {
        writeCSFile(file, config);
    }

    /**
     *  Load the check configurations from the persistent state storage.
     */
    private static List loadFromPersistence() throws CheckstylePluginException
    {
        List configurations = new LinkedList();

        IPath configPath = CheckstylePlugin.getDefault().getStateLocation();
        configPath = configPath.append(CHECKSTYLE_CONFIG_FILE);
        File configFile = configPath.toFile();

        //
        //  Make sure the files exists, it might not if this is a new workspace.
        //
        if (configFile.exists())
        {
            configurations.addAll(loadFile(configFile));
        }
        else
        {
            //
            //  Create an empty config file for next time.
            //
            configurations = loadDefaultCheckConfigs();
            storeToPersistence(configurations);
        }

        return configurations;
    }

    /**
     * @return  List of default check configurations.
     */
    private static List loadDefaultCheckConfigs()
    {
        //
        //  Empty list in coase of an error loading the defaults.
        //
        List configurations = new LinkedList();
        Path defaultsPath = new Path(DEFAULT_CHECK_CONFIGS);
        InputStream inStream = null;
        try
        {
            inStream = CheckstylePlugin.getDefault().openStream(defaultsPath);
            configurations = loadInputStream(inStream);
        }
        catch (IOException e)
        {
            CheckstyleLog.warning("Failed to load default check configurations", e);
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.warning("Failed to load default check configurations", e);
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
                    CheckstyleLog.warning("Error closing default checks file", e);
                }
            }
        }

        return configurations;
    }

    /**
     *  Store the check configurations to the persistent state storage.
     */
    private static void storeToPersistence(List configurations) throws CheckstylePluginException
    {
        IPath configPath = CheckstylePlugin.getDefault().getStateLocation();
        configPath = configPath.append(CHECKSTYLE_CONFIG_FILE);
        File configFile = configPath.toFile();
        writeFile(configFile, configurations);
    }

    private static List loadFile(File file) throws CheckstylePluginException
    {
        List checkConfigs = null;
        FileInputStream inStream = null;
        try
        {
            inStream = new FileInputStream(file);
            checkConfigs = loadInputStream(inStream);
        }
        catch (FileNotFoundException e)
        {
            String message = "Failed to read check configurations: " + e.getMessage();
            CheckstyleLog.warning(message, e);
            throw new CheckstylePluginException(message);
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
                    CheckstyleLog.warning("Error closing input file", e);
                }
            }
        }

        return checkConfigs;
    }

    private static List loadInputStream(InputStream inStream) throws CheckstylePluginException
    {
        List checkConfigs = new LinkedList();
        Document configDoc = XMLUtil.newDocument(inStream);
        if (configDoc == null)
        {
            String message = "Failed to read and parse check configurations";
            CheckstyleLog.warning(message);
            throw new CheckstylePluginException(message);
        }

        Node rootNode = checkFileFormatVersion(configDoc);
        NodeList children = rootNode.getChildNodes();
        int count = children.getLength();
        for (int i = 0; i < count; i++)
        {
            Node node = children.item(i);
            if (node.getNodeName().equals(XMLTags.CHECK_CONFIG_TAG))
            {
                CheckConfiguration config = new CheckConfiguration(node);
                if (config == null)
                {
                    CheckstyleLog.warning("Failed to create CheckConfiguration, ignoring");
                }
                else
                {
                    checkConfigs.add(config);
                }
            }
        }

        return checkConfigs;
    }

    /**
     *  Write a collection of check configurations to a file.
     * 
     *  @param  file      The file to write too.
     * 
     *  @param  configs   The list of check configurations to write to the file.
     */
    private static void writeFile(File file, List configs) throws CheckstylePluginException
    {
        try
        {
            Document configDoc = XMLUtil.newDocument();
            Element root = configDoc.createElement(XMLTags.CHECKSTYLE_ROOT_TAG);
            configDoc.appendChild(root);
            root.setAttribute(XMLTags.FORMAT_VERSION_TAG, CURRENT_CONFIG_FILE_FORMAT_VERSION);

            Iterator iter = configs.iterator();
            while (iter.hasNext())
            {
                CheckConfiguration checkConfig = (CheckConfiguration)iter.next();
                Node node = checkConfig.toDOMNode(configDoc);
                root.appendChild(node);
            }

            String xml = XMLUtil.serializeDocument(configDoc, true);

            OutputStreamWriter writer = null;
            try
            {
                writer = new OutputStreamWriter(new FileOutputStream(file), "utf-8");
                writer.write(xml);
            }
            catch (Exception e)
            {
                String message = "Failed to write check configuration file: " + e.getMessage();
                CheckstyleLog.warning(message, e);
                throw new CheckstylePluginException(message);
            }
            finally
            {
                if (writer != null)
                {
                    writer.close();
                }
            }
        }
        catch (Exception e)
        {
            String message = "Failed to write check configuration file: " + e.getMessage();
            CheckstyleLog.warning(message, e);
            throw new CheckstylePluginException(message);
        }
    }

    /**
     *  Write a collection of check configurations to a file.
     * 
     *  @param  file      The file to write too.
     * 
     *  @param  config   The check configuration to write to the file.
     */
    private static void writeCSFile(File file, CheckConfiguration config)
        throws CheckstylePluginException
    {
        try
        {
            String xml = CheckstyleConfigurationSerializer.serialize(config);
            OutputStreamWriter writer = null;
            try
            {
                writer = new OutputStreamWriter(new FileOutputStream(file), "utf-8");
                writer.write(xml);
            }
            catch (Exception e)
            {
                String message = "Failed to write check configuration file: " + e.getMessage();
                CheckstyleLog.warning(message, e);
                throw new CheckstylePluginException(message);
            }
            finally
            {
                if (writer != null)
                {
                    writer.close();
                }
            }
        }
        catch (Exception e)
        {
            String message = "Failed to write check configuration file: " + e.getMessage();
            CheckstyleLog.warning(message, e);
            throw new CheckstylePluginException(message);
        }
    }

    /**
     *  Checks to see if the file format version is out of date.  If it is
     *  then the DOM is updated to the new format and the new root node if
     *  returned.
     */
    private static Node checkFileFormatVersion(Document doc) throws CheckstylePluginException
    {
        Node result = doc.getDocumentElement();
        String fileVersion = XMLUtil.getNodeAttributeValue(result, XMLTags.FORMAT_VERSION_TAG);
        if (fileVersion.equals(VERSION_1_0_0) || fileVersion.equals(VERSION_3_2_0))
        {
            //
            //  The package names of the check rules changed going from
            //  Checkstyle v3.1 (file format 1.0.0) to Checkstyle 3.2
            //  (file format 3.2.0).  Update the check rule class names
            //  to the correct version 3.2.0 names.
            //  They have also changed from 3.2.0 to 4.0.0 this will update to the latest
            //
            result = updateCheckClassnamesTo_Latest(result);
        }
        return result;
    }

    /**
     *  Check to see if a check configuration is already using a given name.
     * 
     *  @param  name  Name of the configuration in question.
     * 
     *  @return  <code>true</code> = in use, <code>false</code> = not in use.
     * 
     *  @throws CheckstylePluginException  Error during processing.
     */
    public static boolean isNameInUse(String name) throws CheckstylePluginException
    {
        boolean result = true;
        if (getByName(name) == null)
        {
            result = false;
        }
        return result;
    }

    /**
     * Updates check rule class names based on a package renaming that occured 
     * with the release of Checkstyle v3.2.0.
     * 
     * @param  rootNode  The check configuration XML document.
     * 
     * @return  A modified check configuration XML document.
     */
    private static Node updateCheckClassnamesTo_v3_2_0(Node rootNode)
        throws CheckstylePluginException
    {
       return updateCheckClassnames(rootNode, CLASSNAMES_V3_2_0_UPDATE);
    }
    
    /**
     * Updates check rule class names based on a package renaming that occured 
     * with the release of Checkstyle v4.0.0.
     * 
     * @param  rootNode  The check configuration XML document.
     * 
     * @return  A modified check configuration XML document.
     */
    private static Node updateCheckClassnamesTo_v4_0_0(Node rootNode)
        throws CheckstylePluginException
    {
       return updateCheckClassnames(updateCheckClassnamesTo_v3_2_0(rootNode), CLASSNAMES_V4_0_0_UPDATE);
    }

    /**
     * Updates check rule class names based on a package renaming that occured 
     * since the release of Checkstyle v3.2.0.
     * 
     * @param  rootNode  The check configuration XML document.
     * 
     * @return  A modified check configuration XML document.
     */
    private static Node updateCheckClassnamesTo_Latest(Node rootNode)
        throws CheckstylePluginException
    {
       return updateCheckClassnamesTo_v4_0_0(rootNode);
    }

    /**
     * Updates check rule class names based on a package renaming that occured 
     * based on the passed in update file
     * 
     * @param  rootNode  The check configuration XML document.
     * 
     * @return  A modified check configuration XML document.
     */
    private static Node updateCheckClassnames(Node rootNode, String updateFile)
        throws CheckstylePluginException
    {
        //
        //  Load the classname mapping from the v3.2.0 update.
        //
        Properties classnameMap = new Properties();
        ClassLoader loader = CheckConfigurationFactory.class.getClassLoader();
        InputStream inStream = loader.getResourceAsStream(updateFile);
        if (inStream == null)
        {
            throw new CheckstylePluginException("Failed to load check classname update map");
        }

        try
        {
            classnameMap.load(inStream);
        }
        catch (IOException e)
        {
            throw new CheckstylePluginException("Failed to load check classname update map");
        }
        finally
        {
            try
            {
                inStream.close();
            }
            catch (IOException e)
            {
                //  Nothing can be done about it.
            }
        }

        //
        //  Iterate through the check configurations.
        //
        NodeList checkConfigs = rootNode.getChildNodes();
        int numConfigs = checkConfigs.getLength();
        for (int i = 0; i < numConfigs; i++)
        {
            Node config = checkConfigs.item(i);
            if (config.getNodeName().equals(XMLTags.CHECK_CONFIG_TAG))
            {
                NodeList checkRules = config.getChildNodes();
                int numRules = checkRules.getLength();
                for (int j = 0; j < numRules; j++)
                {
                    Node rule = checkRules.item(j);
                    if (rule.getNodeName().equals(XMLTags.RULE_CONFIG_TAG))
                    {
                        //
                        //  This is a rule configuration.  Get it's classname
                        //  attribute.
                        //
                        NamedNodeMap attrMap = rule.getAttributes();
                        Attr attr = (Attr)attrMap.getNamedItem(XMLTags.CLASSNAME_TAG);
                        String currentClassname = attr.getValue();
                        String newClassname =
                            classnameMap.getProperty(currentClassname, currentClassname);
                        if (!newClassname.equals(currentClassname))
                        {
                            attr.setValue(newClassname);
                        }
                    }
                }
            }
        }
        return rootNode;
    }
}
