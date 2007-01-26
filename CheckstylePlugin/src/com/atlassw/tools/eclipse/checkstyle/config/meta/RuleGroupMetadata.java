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

import java.util.LinkedList;
import java.util.List;

/**
 * This class describes a collection of check rules that are logicaly grouped
 * together.
 */
public class RuleGroupMetadata
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

    /** The name of the group. */
    private String mGroupName;

    /** Determines if the group is hidden. */
    private boolean mIsHidden;

    /** The priority of the group. */
    private int mPriority;

    /** The list of modules belonging to the group. */
    private List mRuleMetadata = new LinkedList();

    // =================================================
    // Constructors & finalizer.
    // =================================================

    RuleGroupMetadata(String groupName, boolean hidden, int priority)
    {
        mGroupName = groupName;
        mIsHidden = hidden;
        mPriority = priority;
    }

    // =================================================
    // Methods.
    // =================================================

    /**
     * Returns the group's name.
     * 
     * @return Group name
     */
    public final String getGroupName()
    {
        return mGroupName;
    }

    /**
     * Determine if the module is to be hidden from the users sight.
     * 
     * @return <code>true</code> if the module is hidden
     */
    public boolean isHidden()
    {
        return mIsHidden;
    }

    /**
     * Returns the priority of the group.
     * 
     * @return the priority
     */
    public int getPriority()
    {
        return mPriority;
    }

    /**
     * Returns a list of the group's rule metadata.
     * 
     * @return List of <code>RuleMetadata</code> objects.
     */
    public final List getRuleMetadata()
    {
        return mRuleMetadata;
    }
}