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
import org.apache.regexp.RE;
import org.apache.regexp.RECompiler;
import org.apache.regexp.REProgram;
import org.apache.regexp.RESyntaxException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 *  A file match pattern is a pattern used in a regular express to check
 *  for matching file names.
 */
public class FileMatchPattern implements Cloneable, XMLTags
{
    //=================================================
	// Public static final variables.
	//=================================================

	//=================================================
	// Static class variables.
	//=================================================
    
    private static RECompiler  sRECompiler = new RECompiler();

	//=================================================
	// Instance member variables.
	//=================================================
    
    private String       mMatchPattern;
    
    private boolean      mIsIncludePattern = true;
    
    private REProgram    mProgram;
    
    private RE           mProcessor;

	//=================================================
	// Constructors & finalizer.
	//=================================================
    
    /**
     *  Construct a new <code>FileMatchPattern</code>.
     * 
     *  @param pattern  The new pattern.
     * 
     *  @throws CheckstylePluginException  Error during processing
     */
    public FileMatchPattern(String pattern) throws CheckstylePluginException
    {
        setMatchPattern(pattern);
    }

    /**
     *  Create from a DOM node.
     * 
     *  @param  node  The DOM node to construct from.
     * 
     *  @throws CheckstylePluginException  Error during processing
     */
    public FileMatchPattern(Node node) throws CheckstylePluginException
    {
        String temp = XMLUtil.getNodeAttributeValue(node, MATCH_PATTERN_TAG);
        if (temp != null)
        {
            setMatchPattern(temp.trim());
        }
        else
        {
            CheckstyleLog.warning("FileMatchPattern pattern is null");
        }

        temp = XMLUtil.getNodeAttributeValue(node, INCLUDE_PATTERN_TAG);
        if (temp != null)
        {
            mIsIncludePattern = Boolean.valueOf(temp.trim()).booleanValue();
        }
        else
        {
            CheckstyleLog.warning("FileSet enabled is null");
        }
    }

	//=================================================
	// Methods.
	//=================================================

	/**
	 * Returns the match pattern.
     * 
	 * @return String
	 */
	public String getMatchPattern()
	{
		return mMatchPattern;
	}
    
	/**
	 *  Sets the match pattern.
     * 
	 *  @param pattern The match pattern to set
     * 
     *  @throws CheckstylePluginException  Error during processing
	 */
	public void setMatchPattern(String pattern) throws CheckstylePluginException
	{
        if ((pattern == null) || (pattern.trim().length() == 0))
        {
            throw new CheckstylePluginException("Empty or null pattern");
        }
        
        REProgram program = null;
        try
        {
            program = sRECompiler.compile(pattern);
        }
        catch (RESyntaxException e)
        {
            throw new CheckstylePluginException(e.getMessage());
        }
        mProcessor     = new RE(program);
        mProgram       = program;
		mMatchPattern  = pattern;
	}
    
    /**
     *  Tests a file name to see if it matches the pattern.
     * 
     *  @param  fileName  File name to be tested.
     * 
     *  @return  <code>true</code> = match,
     *            <code>false</code> = no match.
     */
    public boolean isMatch(String fileName)
    {
        boolean result = false;
        
        result = mProcessor.match(fileName);
        
        return result;
    }

	/**
	 * Returns the isIncludePattern.
	 * @return boolean
	 */
	public boolean isIncludePattern()
	{
		return mIsIncludePattern;
	}

	/**
	 * Sets the isIncludePattern.
	 * @param isIncludePattern The isIncludePattern to set
	 */
	public void setIsIncludePattern(boolean isIncludePattern)
	{
		mIsIncludePattern = isIncludePattern;
	}

    /**
     *  Create an XML DOM node representation of the file set.
     * 
     *  @param  doc  The document to create the node within.
     * 
     *  @return  A DOM Node representing the FileMatchPattern.
     */
    public Node toDOMNode(Document doc)
    {
        Element rootNode = null;
        
        try
        {
            rootNode = doc.createElement(FILE_MATCH_PATTERN_TAG);
            rootNode.setAttribute(MATCH_PATTERN_TAG,   mMatchPattern);
            Boolean pattern = new Boolean(mIsIncludePattern);
            rootNode.setAttribute(INCLUDE_PATTERN_TAG, pattern.toString());
        }
        catch (DOMException e)
        {
            rootNode = null;
            CheckstyleLog.warning("Failed to create XML DOM node for FileMatchPattern", e);
        }
        
        return rootNode;
    }
    
    /**
     *  Clone the object
     * 
     *  @return  The clone
     * 
     *  @throws  CloneNotSupportedException  The object can not be cloned.
     */
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
}
