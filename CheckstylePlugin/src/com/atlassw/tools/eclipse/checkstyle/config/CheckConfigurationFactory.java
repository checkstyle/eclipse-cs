//============================================================================
//
// Copyright (C) 2002-2003  David Schneider
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
import java.io.FileWriter;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *  Used to manage the life cycle of <code>CheckConfiguration</code>
 *  objects.
 */
public class CheckConfigurationFactory implements XMLTags
{
    //=================================================
	// Public static final variables.
	//=================================================

	//=================================================
	// Static class variables.
	//=================================================
    
    private static final String CHECKSTYLE_CONFIG_FILE = "checkstyle-config.xml";
    
    private static final String CURRENT_CONFIG_FILE_FORMAT_VERSION = "1.0.0";
    
	//=================================================
	// Instance member variables.
	//=================================================

	//=================================================
	// Constructors & finalizer.
	//=================================================
    
    private CheckConfigurationFactory()
    {
    }

	//=================================================
	// Methods.
	//=================================================
    
    /**
     *  Get a new <CheckConfiguration</code> instance.
     * 
     *  @return  A new instance.
     * 
     *  @throws CheckstyleException  Error during processing.
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
     *  @throws  CheckstyleException  Error during processing.
     */
    public static CheckConfiguration getByName(String name) throws CheckstylePluginException
    {
        CheckConfiguration result = null;
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
        return result;
    }
    
    /**
     *  Get a list of the currently defined check configurations.
     * 
     *  @return  A list containing all instances.
     * 
     *  @throws  CheckstyleException  Error during processing.
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
     *  @throws  CheckstyleException  Error during processing.
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
     *  @throws  CheckstyleException  Error during processing.
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
     *  @throws  CheckstyleException  Error during processing.
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
     *  @throws  CheckstyleException  Error during processing.
     */
    public static List importPluginCheckConfigurations(File file)
        throws CheckstylePluginException
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
     *  @param  configs  List of check configurations to write out.
     * 
     *  @throws  CheckstyleException  Error during processing.
     */
    public static void exportPluginCheckConfigurations(File file, List configs)
        throws CheckstylePluginException
    {
        writeFile(file, configs);
    }
    
    /**
     *  Write check configurations to an external file in standard
     *  Checkstyle format.
     * 
     *  @param  file     File to write too.
     * 
     *  @param  configs  List of check configurations to write out.
     * 
     *  @throws  CheckstylePluginException  Error during processing.
     */
    public static void exportCheckstyleCheckConfigurations(File file, List configs)
        throws CheckstylePluginException
    {
        writeCSFile(file, configs);
    }
    
    private static void initialize() throws CheckstylePluginException
    {
        loadFromPersistence();
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
            storeToPersistence(new LinkedList());
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
        List checkConfigs = new LinkedList();
		FileInputStream inStream = null;
		try
		{
			inStream = new FileInputStream(file);
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
				if (node.getNodeName().equals(CHECK_CONFIG_TAG))
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
	
	
	
	private static List loadCSFile(File file) throws CheckstylePluginException
	{
        List checkConfigs = null;
		FileInputStream inStream = null;
		try
		{
			inStream = new FileInputStream(file);
			Document configDoc = XMLUtil.newDocument(inStream);
			if (configDoc == null)
			{
                String message = "Failed to read and parse check configurations";
                CheckstyleLog.warning(message);
				throw new CheckstylePluginException(message);
			}

			checkConfigs = CheckstyleConfigurationSerializer.deserialize(configDoc);

			
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
            Element root = configDoc.createElement(CHECKSTYLE_ROOT_TAG);
            configDoc.appendChild(root);
            root.setAttribute(FORMAT_VERSION_TAG, CURRENT_CONFIG_FILE_FORMAT_VERSION);
            
            Iterator iter = configs.iterator();
            while (iter.hasNext())
            {
                CheckConfiguration checkConfig = (CheckConfiguration)iter.next();
                Node node = checkConfig.toDOMNode(configDoc);
                root.appendChild(node);
            }
            
            String xml = XMLUtil.serializeDocument(configDoc, true);

            FileWriter writer = null;
            try
            {
                writer = new FileWriter(file);
                writer.write(xml);
            }
            catch (Exception e)
            {
                String message = "Failed to write check configuration file: " +
                                 e.getMessage();
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
            String message = "Failed to write check configuration file: " +
                             e.getMessage();
            CheckstyleLog.warning(message, e);
            throw new CheckstylePluginException(message);
        }
    }
    
        /**
     *  Write a collection of check configurations to a file.
     * 
     *  @param  file      The file to write too.
     * 
     *  @param  configs   The list of check configurations to write to the file.
     */
    private static void writeCSFile(File file, List configs)
        throws CheckstylePluginException
    {
        if (configs.size() != 1)
        {
            String message = "Too many confis for checkstyle export, count=" +
                             configs.size();
            CheckstyleLog.warning(message);
            throw new CheckstylePluginException(message);
        }
        
        try
        {
            CheckConfiguration config = (CheckConfiguration)configs.get(0);
            String xml = CheckstyleConfigurationSerializer.serialize(config);
            FileWriter writer = null;
            try
            {
                writer = new FileWriter(file);
                writer.write(xml);
            }
            catch (Exception e)
            {
                String message = "Failed to write check configuration file: " +
                                 e.getMessage();
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
            String message = "Failed to write check configuration file: " +
                             e.getMessage();
            CheckstyleLog.warning(message, e);
            throw new CheckstylePluginException(message);
        }
    }
    
    /**
     *  Checks to see if the file format version is out of date.  If it is
     *  then the DOM is updated to the new format and the new root node if
     *  returned.
     */
    private static Node checkFileFormatVersion(Document doc)
    {
        return doc.getDocumentElement();
    }
    
    /**
     *  Check to see if a check configuration is already using a given name.
     * 
     *  @param  name  Name of the configuration in question.
     * 
     *  @return  <code>true</code> = in use, <code>false</code> = not in use.
     * 
     *  @throws CheckstyleException  Error during processing.
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
}
