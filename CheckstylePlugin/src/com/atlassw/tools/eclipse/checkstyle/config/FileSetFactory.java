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
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.XMLUtil;

//=================================================
// Imports from org namespace
//=================================================
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *  Used to manage the life cycle of FileSet objects.
 */
public final class FileSetFactory
{
    //=================================================
	// Public static final variables.
	//=================================================

	//=================================================
	// Static class variables.
	//=================================================
    
    private static final String FILESET_FILE = ".checkstyle";
    
    private static final String CURRENT_FILE_FORMAT_VERSION = "1.0.0";

	//=================================================
	// Instance member variables.
	//=================================================

	//=================================================
	// Constructors & finalizer.
	//=================================================
    
    private FileSetFactory()
    {}

	//=================================================
	// Methods.
	//=================================================
    
    /**
     *  Get a list of <code>FileSet</code> objects for the specified project.
     * 
     *  @param  project  The project to get <code>FileSet</code>'s for.
     * 
     *  @return  A list of <code>FileSet</code> instances.
     * 
     *  @throws CheckstylePluginException  Error during processing.
     */
    public static List getFileSets(IProject project) throws CheckstylePluginException
    {
        List result = loadFromPersistence(project);
        return result;
    }
    
    /**
     *  Get a list of enabled <code>FileSet</code> objects for the specified project.
     * 
     *  @param  project  The project to get <code>FileSet</code>'s for.
     * 
     *  @return  A list of enabled <code>FileSet</code> instances.
     * 
     *  @throws CheckstylePluginException  Error during processing.
     */
    public static List getEnabledFileSets(IProject project) throws CheckstylePluginException
    {
        List fileSets = loadFromPersistence(project);
        List result = new LinkedList();
        for (Iterator iter = fileSets.iterator(); iter.hasNext();)
        {
            FileSet fileSet = (FileSet)iter.next();
            if (fileSet.isEnabled())
            {
                result.add(fileSet);
            }
        }
        
        return result;
    }
    
    /**
     *  Add a <code>FileSet</code> to a project.
     * 
     *  @param fileSets  The list of <code>FileSet</code> objects to set.
     * 
     *  @param project  The project to add it too.
     * 
     *  @throws CheckstylePluginException  Error during processing.
     */
    public static void setFileSets(List fileSets, IProject project)
        throws CheckstylePluginException
    {
        storeToPersistence(fileSets, project);
    }
    
    /**
     *  Load the audit configurations from the persistent state storage.
     */
    private static List loadFromPersistence(IProject project) throws CheckstylePluginException
    {
        List fileSets = new LinkedList();
                
        //
        //  Make sure the files exists, it might not.
        //
        IFile file = project.getFile(FILESET_FILE);
        boolean exists = file.exists();
        if (!exists)
        {
            return fileSets;
        }
        
        InputStream inStream = null;
        try
        {
            inStream = file.getContents();
            Document doc = XMLUtil.newDocument(inStream);
            if (doc == null)
            {
                String message = "Failed to read and parse FileSets";
                CheckstyleLog.warning(message);
                throw new CheckstylePluginException(message);
            }

            Node rootNode = checkFileFormatVersion(doc);
            NodeList children = rootNode.getChildNodes();
            int count = children.getLength();
            for (int i = 0; i < count; i++)
            {
                Node node = children.item(i);
                if (node.getNodeName().equals(XMLTags.FILESET_TAG))
                {
                    FileSet fileSet = new FileSet(node);
                    if (fileSet == null)
                    {
                        CheckstyleLog.warning("Failed to load FileSet, ignoring");
                    }
                    else
                    {
                        fileSets.add(fileSet);
                    }
                }
            }
        }
        catch (CoreException e)
        {
            String message = "Failed to read FileSets: " + e.getMessage();
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
                	//  Nothing can be done about it.
                }
            }
        }
        
        return fileSets;
    }
    
    /**
     *  Store the audit configurations to the persistent state storage.
     */
    private static void storeToPersistence(List fileSets, IProject project)
        throws CheckstylePluginException
    {
        try
        {
            Document doc = XMLUtil.newDocument();
            Element root = doc.createElement(XMLTags.FILESET_CONFIG_TAG);
            doc.appendChild(root);
            root.setAttribute(XMLTags.FORMAT_VERSION_TAG, CURRENT_FILE_FORMAT_VERSION);
            
            Iterator iter = fileSets.iterator();
            while (iter.hasNext())
            {
                FileSet fileSet = (FileSet)iter.next();
                Node node = fileSet.toDOMNode(doc);
                root.appendChild(node);
            }
            
            String xml = XMLUtil.serializeDocument(doc, true);

            IFile file = project.getFile(FILESET_FILE);
            ByteArrayInputStream inStream = null;
            try
            {
                inStream = new ByteArrayInputStream(xml.getBytes("utf-8"));
                if (!file.exists())
                {
                    file.create(inStream, true, null);
                    file.setLocal(true, IResource.DEPTH_INFINITE, null);
                }
                else
                {
                    file.setContents(inStream, true, true, null);
                }
            }
            catch (CoreException e)
            {
                String message = "Failed to write FileSet file: "
                                 + e.getMessage();
                CheckstyleLog.warning(message, e);
                throw new CheckstylePluginException(message);
            }
            finally
            {
                inStream.close();
            }
        }
        catch (Exception e)
        {
            String message = "Failed to write audit configuration file: "
                             + e.getMessage();
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
     *  Check to see if a check configuration is currently in use
     *  by any projects.
     * 
     *  @param configName  The configuration name to check for.
     * 
     *  @return  <code>true</code> = in use, <code>false</code> = not in use.
     * 
     *  @throws CheckstylePluginException  Error during processing.
     */
    public static boolean isCheckConfigInUse(String configName)
        throws CheckstylePluginException
    {
        boolean result = false;
        
        if (configName == null)
        {
        	return result;
        }

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject[] projects = workspace.getRoot().getProjects();
        for (int i = 0; (i < projects.length) && !result; i++)
        {
            List fileSets = FileSetFactory.getFileSets(projects[i]);
            Iterator iter = fileSets.iterator();
            while (iter.hasNext())
            {
                FileSet fileSet = (FileSet)iter.next();
                if (configName.equals(fileSet.getCheckConfigName()))
                {
                    result = true;
                    break;
                }
            }
        }
        
        return result;
    }
}
