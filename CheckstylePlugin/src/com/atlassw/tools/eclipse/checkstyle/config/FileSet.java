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
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

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
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *  A File Set is a collection of files audited with a common set
 *  of audit rules.
 */
public class FileSet implements Cloneable
{
    //=================================================
	// Public static final variables.
	//=================================================

	//=================================================
	// Static class variables.
	//=================================================

	//=================================================
	// Instance member variables.
	//=================================================
    
    private String               mName;
    
    private CheckConfiguration   mCheckConfig;
    
    private String               mCheckConfigName;
    
    private boolean              mEnabled = true;
    
    private List                 mFileMatchPatterns = new LinkedList();

	//=================================================
	// Constructors & finalizer.
	//=================================================
    
    /**
     *  Default constructor.
     * 
     *  @param  name  The name of the <code>FileSet</code>
     * 
     *  @param checkConfig  The name of the <code>CheckConfiguration</code>
     *                      used to check this <code>FileSet</code>.
     */
    public FileSet(String name, CheckConfiguration checkConfig)
    {
        mName        = name;
        mCheckConfig = checkConfig;
        mCheckConfigName = checkConfig.getConfigName();
    }
    
    /**
     *  Create from a DOM node.
     * 
     *  @param  node The DOM node to create the <code>FileSet</code> from.
     * 
     *  @throws  CheckstylePluginException  Error during processing.
     */
    public FileSet(Node node) throws CheckstylePluginException
    {
        String temp = XMLUtil.getNodeAttributeValue(node, XMLTags.NAME_TAG);
        if (temp != null)
        {
            mName = temp.trim();
        }
        else
        {
            CheckstyleLog.warning("FileSet name is null");
        }
        
        temp = XMLUtil.getNodeAttributeValue(node, XMLTags.ENABLED_TAG);
        if (temp != null)
        {
            mEnabled = Boolean.valueOf(temp.trim()).booleanValue();
        }
        else
        {
            CheckstyleLog.warning("FileSet enabled is null");
        }
        
        temp = XMLUtil.getNodeAttributeValue(node, XMLTags.CHECK_CONFIG_NAME_TAG);
        if (temp != null)
        {
        	mCheckConfigName = temp;
            mCheckConfig = CheckConfigurationFactory.getByName(temp.trim());
        }
        else
        {
        	String msg = "FileSet check configuration name is null";
            CheckstyleLog.warning(msg);
            throw new CheckstylePluginException(msg);
        }

        NodeList children = node.getChildNodes();
        int count = children.getLength();
        for (int i = 0; i < count; i++)
        {
            Node child = children.item(i);
            if (child.getNodeName().equals(XMLTags.FILE_MATCH_PATTERN_TAG))
            {
                FileMatchPattern pattern = null;
                try
                {
                    pattern = new FileMatchPattern(child);
                }
                catch (CheckstylePluginException e)
                {
                    pattern = null;
                    CheckstyleLog.warning("Failed to create file match pattern, ignoring");
                }
                if (pattern != null)
                {
                    mFileMatchPatterns.add(pattern);
                }
            }
        }
    }

	//=================================================
	// Methods.
	//=================================================

    
	/**
	 * Returns the enabled flag.
     * 
	 * @return boolean
	 */
	public boolean isEnabled()
	{
		return mEnabled;
	}

	/**
	 * Returns a list of <code>FileMatchPattern</code> objects.
     * 
	 * @return List
	 */
	public List getFileMatchPatterns()
	{
		return mFileMatchPatterns;
	}
    
    /**
     *  Set the list of <code>FileMatchPattern</code> objects.
     * 
     *  @param list  The new list of pattern objects.
     */
    public void setFileMatchPatterns(List list)
    {
        mFileMatchPatterns = list;
    }
    
    /**
     *  Get the check configuration used by this file set.
     * 
     *  @return  The check configuration used to audit files in the file set.
     */
    public CheckConfiguration getCheckConfig()
    {
        return mCheckConfig;
    }

	/**
	 * Returns the name.
     * 
	 * @return String
	 */
	public String getName()
	{
		return mName;
	}

	/**
	 * Sets the enabled flag.
     * 
	 * @param enabled The enabled to set
	 */
	public void setEnabled(boolean enabled)
	{
		mEnabled = enabled;
	}

	/**
	 * Sets the name.
     * 
	 * @param name The name to set
	 */
	public void setName(String name)
	{
		mName = name;
	}
    
    /**
     *  Create an XML DOM node representation of the file set.
     * 
     *  @param  doc  The document to create the node within.
     */
    public Node toDOMNode(Document doc)
    {
        Element rootNode = null;
        
        try
        {
            rootNode = doc.createElement(XMLTags.FILESET_TAG);
            rootNode.setAttribute(XMLTags.NAME_TAG, mName);
            Boolean enabled = new Boolean(mEnabled);
            rootNode.setAttribute(XMLTags.ENABLED_TAG, enabled.toString());
            rootNode.setAttribute(XMLTags.CHECK_CONFIG_NAME_TAG, mCheckConfigName);
            
            Iterator iter = mFileMatchPatterns.iterator();
            while (iter.hasNext())
            {
                FileMatchPattern pattern = (FileMatchPattern)iter.next();
                Node node = pattern.toDOMNode(doc);
                if (node == null)
                {
                    CheckstyleLog.warning("FileSetPattern lost");
                }
                else
                {
                    rootNode.appendChild(node);
                }
            }
        }
        catch (DOMException e)
        {
            rootNode = null;
            CheckstyleLog.warning("Failed to create XML DOM node for FileSet", e);
        }
        
        return rootNode;
    }
    
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
    
    /**
     *  Tests a file to see if its included in the file set.
     * 
     *  @param  file   The file to test.
     * 
     *  @return  <code>true</code> = the file is included in the file set,<p>
     *            <code>false</code> = the file is not included in the file set.
     */
    public boolean includesFile(IFile file) throws CheckstylePluginException
    {
        boolean result = false;
        String filePath = file.getProjectRelativePath().toString();
        
        Iterator iter = mFileMatchPatterns.iterator();
        while (iter.hasNext())
        {
            FileMatchPattern pattern = (FileMatchPattern)iter.next();
            boolean matches = pattern.isMatch(filePath);
            if (matches)
            {
                if (pattern.isIncludePattern())
                {
                    result = true;
                }
                else
                {
                    result = false;
                }
            }
        }

        return result;
    }

    /**
     * @return
     */
    public String getCheckConfigName()
    {
        return mCheckConfigName;
    }

}
