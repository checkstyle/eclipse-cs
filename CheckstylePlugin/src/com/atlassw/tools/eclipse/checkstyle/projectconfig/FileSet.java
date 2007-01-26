//============================================================================
//
// Copyright (C) 2002-2007  David Schneider, Lars Ködderitzsch
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.eclipse.core.resources.IFile;

import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;

/**
 * A File Set is a collection of files audited with a common set of audit rules.
 */
public class FileSet implements Cloneable
{
    // =================================================
    // Public static final variables.
    // =================================================

    // =================================================
    // Static class variables.
    // =================================================

    // =================================================
    // Instance member variables.
    // =================================================

    private String mName;

    private ICheckConfiguration mCheckConfig;

    private boolean mEnabled = true;

    private List mFileMatchPatterns = new LinkedList();

    // =================================================
    // Constructors & finalizer.
    // =================================================

    /**
     * Default constructor.
     */
    public FileSet()
    {

    }

    /**
     * Default constructor.
     * 
     * @param name The name of the <code>FileSet</code>
     * 
     * @param checkConfig The name of the <code>CheckConfiguration</code> used
     *            to check this <code>FileSet</code>.
     */
    public FileSet(String name, ICheckConfiguration checkConfig)
    {
        setName(name);
        setCheckConfig(checkConfig);
    }

    // =================================================
    // Methods.
    // =================================================

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
     * Set the list of <code>FileMatchPattern</code> objects.
     * 
     * @param list The new list of pattern objects.
     */
    public void setFileMatchPatterns(List list)
    {
        mFileMatchPatterns = list;
    }

    /**
     * Get the check configuration used by this file set.
     * 
     * @return The check configuration used to audit files in the file set.
     */
    public ICheckConfiguration getCheckConfig()
    {
        return mCheckConfig;
    }

    /**
     * Sets the check configuration used by this file set.
     * 
     * @param checkConfig the check configuration
     */
    public void setCheckConfig(ICheckConfiguration checkConfig)
    {
        mCheckConfig = checkConfig;
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
     * Sets the name.
     * 
     * @param name The name to set
     */
    public void setName(String name)
    {
        mName = name;
    }

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
     * Sets the enabled flag.
     * 
     * @param enabled The enabled to set
     */
    public void setEnabled(boolean enabled)
    {
        mEnabled = enabled;
    }

    /**
     * Tests a file to see if its included in the file set.
     * 
     * @param file The file to test.
     * 
     * @return <code>true</code>= the file is included in the file set,
     *         <p>
     *         <code>false</code>= the file is not included in the file set.
     */
    public boolean includesFile(IFile file)
    {
        boolean result = false;
        String filePath = file.getProjectRelativePath().toOSString();

        Iterator iter = mFileMatchPatterns.iterator();
        while (iter.hasNext())
        {
            FileMatchPattern pattern = (FileMatchPattern) iter.next();
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
     * {@inheritDoc}
     */
    public Object clone()
    {
        try
        {
            FileSet clone = (FileSet) super.clone();

            // clone filesets
            List clonedPatterns = new LinkedList();
            Iterator it = mFileMatchPatterns.iterator();
            while (it.hasNext())
            {
                clonedPatterns.add(((FileMatchPattern) it.next()).clone());
            }
            clone.mFileMatchPatterns = clonedPatterns;

            return clone;
        }
        catch (CloneNotSupportedException e)
        {
            throw new InternalError(); // should never happen
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj)
    {

        if (obj == null || !(obj instanceof FileSet))
        {
            return false;
        }
        if (this == obj)
        {
            return true;
        }
        FileSet rhs = (FileSet) obj;
        return new EqualsBuilder().append(mEnabled, rhs.mEnabled).append(mName, rhs.mName).append(
                mFileMatchPatterns, rhs.mFileMatchPatterns).append(mCheckConfig, rhs.mCheckConfig)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        return new HashCodeBuilder(987349, 1000003).append(mEnabled).append(mName).append(
                mCheckConfig).append(mFileMatchPatterns).toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}