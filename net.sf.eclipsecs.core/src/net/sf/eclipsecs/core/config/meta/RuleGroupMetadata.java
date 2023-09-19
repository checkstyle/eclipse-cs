//============================================================================
//
// Copyright (C) 2003-2023  David Schneider, Lars Ködderitzsch
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
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
//
//============================================================================

package net.sf.eclipsecs.core.config.meta;

import java.util.LinkedList;
import java.util.List;

/**
 * This class describes a collection of check rules that are logicaly grouped together.
 */
public class RuleGroupMetadata {

  /**
   * group id, used for online help URL calculation
   */
  private final String mGroupId;

  /** The name of the group. */
  private String mGroupName;

  /** The description of the group. */
  private String mDescription;

  /** Determines if the group is hidden. */
  private boolean mIsHidden;

  /** The priority of the group. */
  private int mPriority;

  /** The list of modules belonging to the group. */
  private List<RuleMetadata> mRuleMetadata = new LinkedList<>();

  RuleGroupMetadata(String groupId, String groupName, String groupDesc, boolean hidden, int priority) {
    mGroupId = groupId;
    mGroupName = groupName;
    mDescription = groupDesc;
    mIsHidden = hidden;
    mPriority = priority;
  }

  /**
   * Returns the group's name.
   *
   * @return Group name
   */
  public final String getGroupName() {
    return mGroupName;
  }

  /**
   * Returns the description of the group.
   *
   * @return the description
   */
  public String getDescription() {
    return mDescription;
  }

  /**
   * Determine if the module is to be hidden from the users sight.
   *
   * @return <code>true</code> if the module is hidden
   */
  public boolean isHidden() {
    return mIsHidden;
  }

  /**
   * Returns the priority of the group.
   *
   * @return the priority
   */
  public int getPriority() {
    return mPriority;
  }

  /**
   * Returns a list of the group's rule metadata.
   *
   * @return List of <code>RuleMetadata</code> objects.
   */
  public final List<RuleMetadata> getRuleMetadata() {
    return mRuleMetadata;
  }

  /**
   * @return the group id
   */
  public String getGroupId() {
    return mGroupId;
  }
}
