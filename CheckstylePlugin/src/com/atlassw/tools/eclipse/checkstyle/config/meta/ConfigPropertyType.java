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

package com.atlassw.tools.eclipse.checkstyle.config.meta;

//=================================================
// Imports from java namespace
//=================================================

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import org.eclipse.osgi.util.NLS;

import com.atlassw.tools.eclipse.checkstyle.ErrorMessages;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;

//=================================================
// Imports from org namespace
//=================================================

/**
 * This represents the possible data types for a rule's configuration property.
 */
public final class ConfigPropertyType
{
    // =================================================
    // Public static final variables.
    // =================================================

    /** A String. */
    public static final ConfigPropertyType STRING = new ConfigPropertyType(10, "String"); //$NON-NLS-1$

    /** An array of strings. */
    public static final ConfigPropertyType STRING_ARRAY = new ConfigPropertyType(20, "StringArray"); //$NON-NLS-1$

    /** An integer. */
    public static final ConfigPropertyType INTEGER = new ConfigPropertyType(30, "Integer"); //$NON-NLS-1$

    /** Select a single items from a list. */
    public static final ConfigPropertyType SINGLE_SELECT = new ConfigPropertyType(40,
            "SingleSelect"); //$NON-NLS-1$

    /** Boolean value. */
    public static final ConfigPropertyType BOOLEAN = new ConfigPropertyType(50, "Boolean"); //$NON-NLS-1$

    /** Select multiple items from a set. */
    public static final ConfigPropertyType MULTI_CHECK = new ConfigPropertyType(60, "MultiCheck"); //$NON-NLS-1$

    /** A value that is not configured. */
    public static final ConfigPropertyType HIDDEN = new ConfigPropertyType(70, "Hidden"); //$NON-NLS-1$

    /** A value that contains a file name. */
    public static final ConfigPropertyType FILE = new ConfigPropertyType(80, "File"); //$NON-NLS-1$

    /** A value that contains a regular expression. */
    public static final ConfigPropertyType REGEX = new ConfigPropertyType(90, "Regex"); //$NON-NLS-1$

    // =================================================
    // Static class variables.
    // =================================================

    // =================================================
    // Instance member variables.
    // =================================================

    /** The string representation. */
    private String mLabel;

    /** The internal value. */
    private int mValue;

    // =================================================
    // Constructors & finalizer.
    // =================================================

    /**
     * Private constructor to prevent instantiation.
     */
    private ConfigPropertyType(int value, String label)
    {
        mValue = value;
        mLabel = label;
    }

    // =================================================
    // Methods.
    // =================================================

    /**
     * The equals method.
     * 
     * @param obj The object to test against.
     * 
     * @return <code>true</code> means equal, <code>false</code> means not
     *         equal.
     */
    public boolean equals(Object obj)
    {
        boolean result = false;

        if ((obj instanceof ConfigPropertyType)
                && (((ConfigPropertyType) obj).mValue == this.mValue))
        {
            result = true;
        }

        return result;
    }

    /**
     * The hashCode method.
     * 
     * @return has code for the object.
     */
    public int hashCode()
    {
        return mValue;
    }

    /**
     * Get the label string.
     * 
     * @return The label.
     */
    public String getLabel()
    {
        return mLabel;
    }

    /**
     * Get a config property type from a type label string.
     * 
     * @param label The label to look up.
     * 
     * @return Matching config property type.
     * 
     * @throws CheckstylePluginException The string did not match any known
     *             types.
     */
    public static ConfigPropertyType getConfigPropertyType(String label)
        throws CheckstylePluginException
    {
        ConfigPropertyType result = null;

        if (label.equalsIgnoreCase(STRING.mLabel))
        {
            result = STRING;
        }
        else if (label.equalsIgnoreCase(STRING_ARRAY.mLabel))
        {
            result = STRING_ARRAY;
        }
        else if (label.equalsIgnoreCase(INTEGER.mLabel))
        {
            result = INTEGER;
        }
        else if (label.equalsIgnoreCase(SINGLE_SELECT.mLabel))
        {
            result = SINGLE_SELECT;
        }
        else if (label.equalsIgnoreCase(BOOLEAN.mLabel))
        {
            result = BOOLEAN;
        }
        else if (label.equalsIgnoreCase(MULTI_CHECK.mLabel))
        {
            result = MULTI_CHECK;
        }
        else if (label.equalsIgnoreCase(HIDDEN.mLabel))
        {
            result = HIDDEN;
        }
        else if (label.equalsIgnoreCase(FILE.mLabel))
        {
            result = FILE;
        }
        else if (label.equalsIgnoreCase(REGEX.mLabel))
        {
            result = REGEX;
        }
        else
        {
            throw new CheckstylePluginException(NLS.bind(ErrorMessages.errorUnknownPropertyType,
                    label));
        }

        return result;
    }
}