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

package com.atlassw.tools.eclipse.checkstyle.projectconfig;

import org.apache.regexp.RE;
import org.apache.regexp.RECompiler;
import org.apache.regexp.REProgram;
import org.apache.regexp.RESyntaxException;

import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;

/**
 * A file match pattern is a pattern used in a regular express to check for
 * matching file names.
 */
public class FileMatchPattern implements Cloneable
{
    //=================================================
    // Public static final variables.
    //=================================================

    //=================================================
    // Static class variables.
    //=================================================

    private static RECompiler sRECompiler       = new RECompiler();

    //=================================================
    // Instance member variables.
    //=================================================

    private String            mMatchPattern;

    private boolean           mIsIncludePattern = true;

    private REProgram         mProgram;

    private RE                mProcessor;

    //=================================================
    // Constructors & finalizer.
    //=================================================

    /**
     * Construct a new <code>FileMatchPattern</code>.
     * 
     * @param pattern The new pattern.
     * 
     * @throws CheckstylePluginException Error during processing
     */
    public FileMatchPattern(String pattern) throws CheckstylePluginException
    {
        setMatchPattern(pattern);
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
     * Sets the match pattern.
     * 
     * @param pattern The match pattern to set
     * 
     * @throws CheckstylePluginException Error during processing
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
        mProcessor = new RE(program);
        mProgram = program;
        mMatchPattern = pattern;
    }

    /**
     * Tests a file name to see if it matches the pattern.
     * 
     * @param fileName File name to be tested.
     * 
     * @return <code>true</code>= match, <code>false</code>= no match.
     */
    public boolean isMatch(String fileName)
    {
        boolean result = false;

        result = mProcessor.match(fileName);

        return result;
    }

    /**
     * Returns the isIncludePattern.
     * 
     * @return boolean
     */
    public boolean isIncludePattern()
    {
        return mIsIncludePattern;
    }

    /**
     * Sets the isIncludePattern.
     * 
     * @param isIncludePattern The isIncludePattern to set
     */
    public void setIsIncludePattern(boolean isIncludePattern)
    {
        mIsIncludePattern = isIncludePattern;
    }

    /**
     * Clone the object.
     * 
     * @return The clone
     * 
     * @throws CloneNotSupportedException The object can not be cloned.
     */
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof FileMatchPattern))
        {
            return false;
        }
        if (this == obj)
        {
            return true;
        }
        FileMatchPattern otherPattern = (FileMatchPattern) obj;
        if (!mMatchPattern.equals(otherPattern.mMatchPattern)
                || mIsIncludePattern != otherPattern.mIsIncludePattern)
        {
            return false;
        }

        return true;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        //a "nice" prime number, see Java Report, April 2000
        final int prime = 1000003;

        int result = 1;
        result = (result * prime) + Boolean.valueOf(mIsIncludePattern).hashCode();
        result = (result * prime) + (mMatchPattern != null ? mMatchPattern.hashCode() : 0);

        return result;
    }

}