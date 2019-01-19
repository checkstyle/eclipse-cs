//============================================================================
//
// Copyright (C) 2002-2016  David Schneider, Lars Ködderitzsch
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

package net.sf.eclipsecs.core.config.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.sf.eclipsecs.core.config.Severity;

/**
 * This class contains the metadata that describes a check rule.
 */
public class RuleMetadata {

  /** The display name of the module. */
  private final String mName;

  /** The internal name of the module. */
  private final String mInternalName;

  /** The internal name of the parent module. */
  private final String mParent;

  /** The description of the module. */
  private String mDescription;

  /** Determines if the module is hidden. */
  private final boolean mIsHidden;

  /** Determines if the module has a severity. */
  private final boolean mHasSeverity;

  /** Determines if the module can be deleted. */
  private final boolean mIsDeletable;

  /** The default severity. */
  private final Severity mDefaultSeverityLevel;

  /** The list of property metadata. */
  private final List<ConfigPropertyMetadata> mConfigPropMetadata = new LinkedList<>();

  /** The group. */
  private final RuleGroupMetadata mGroup;

  /** Alternative names, including the name of the Checkstyle checker class. */
  private final Collection<String> mAlternativeNames;

  /** Collection fo quick fixes for this module. */
  private final Collection<String> mQuickfixes;

  private final Collection<String> mMessageKeys;

  /** Determines if the module is a singleton. */
  private final boolean mIsSingleton;

  /**
   * Creates a rule metadata.
   * 
   * @param ruleName
   *          the name of the rule
   * @param internalName
   *          the internal name of the rule
   * @param parent
   *          the parent module name
   * @param defaultSeverity
   *          the default severity level
   * @param hidden
   *          <code>true</code> if the module should be hidden from the user
   * @param hasSeverity
   *          <code>true</code> if the module has a severity to configure
   * @param deletable
   *          <code>true</code> if the module has can be deleted from the configuration
   * @param isSingleton
   *          <code>true</code> if the module should occur only once in a checkstyle configuration
   * @param group
   *          the group the module belongs to
   */
  public RuleMetadata(String ruleName, String internalName, String parent, Severity defaultSeverity,
          boolean hidden, boolean hasSeverity, boolean deletable, boolean isSingleton,
          RuleGroupMetadata group) {
    mName = ruleName;
    mInternalName = internalName;
    mParent = parent;
    mDefaultSeverityLevel = defaultSeverity;
    mIsHidden = hidden;
    mHasSeverity = hasSeverity;
    mIsDeletable = deletable;
    mGroup = group;
    mAlternativeNames = new ArrayList<>();
    mQuickfixes = new ArrayList<>();
    mMessageKeys = new ArrayList<>();
    mIsSingleton = isSingleton;
  }

  /**
   * Adds an alternative name for this rule.
   * 
   * @param alternativeName
   *          an alternative name for this rule
   */
  public void addAlternativeName(String alternativeName) {
    mAlternativeNames.add(alternativeName);
  }

  /**
   * Returns the list of alternative names.
   * 
   * @return a collection of String
   */
  public Collection<String> getAlternativeNames() {
    return mAlternativeNames;
  }

  /**
   * Adds a quickfixfor this rule.
   * 
   * @param quickfixClassName
   *          the fully qualified classname of the quickfix
   */
  public void addQuickfix(String quickfixClassName) {
    mQuickfixes.add(quickfixClassName);
  }

  /**
   * Returns the list quickfixes for this module.
   * 
   * @return a collection of quickfix class names
   */
  public Collection<String> getQuickfixClassNames() {
    return mQuickfixes;
  }

  /**
   * Adds a message key to this module metadata.
   * 
   * @param messageKey
   *          the message key to add
   */
  public void addMessageKey(String messageKey) {
    mMessageKeys.add(messageKey);
  }

  /**
   * Returns the collection of message keys used by this module.
   * 
   * @return the list of message keys
   */
  public Collection<String> getMessageKeys() {
    return mMessageKeys;
  }

  /**
   * Returns the default severity level.
   * 
   * @return The severity level.
   */
  public Severity getDefaultSeverityLevel() {
    return mDefaultSeverityLevel;
  }

  /**
   * Returns the rule's description.
   * 
   * @return Rule description
   */
  public String getDescription() {
    return mDescription;
  }

  /**
   * Set the description for the rule.
   * 
   * @param description
   *          the description
   */
  public void setDescription(String description) {
    mDescription = description;
  }

  /**
   * Returns the rule's name.
   * 
   * @return String
   */
  public String getRuleName() {
    return mName;
  }

  /**
   * Returns the internal name of the module. The internal name is the name of the module inside the
   * checkstyle configuration file.
   * 
   * @return the internal module name
   */
  public String getInternalName() {
    return mInternalName;
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
   * Determine if the module has a severity to configure.
   * 
   * @return <code>true</code> if the module has a severity
   */
  public boolean hasSeverity() {
    return mHasSeverity;
  }

  /**
   * Determine if the module can be removed from the configuration.
   * 
   * @return <code>true</code> if the module can be removed
   */
  public boolean isDeletable() {
    return mIsDeletable;
  }

  /**
   * Determine if the module is a singleton inside a checkstyle configuration.
   * 
   * @return <code>true</code> if the module is a singleton
   */
  public boolean isSingleton() {
    return mIsSingleton;
  }

  /**
   * Gets the name of the parent module.
   * 
   * @return the parent module
   */
  public String getParentModule() {
    return mParent;
  }

  /**
   * Returns the configuration property metadata.
   * 
   * @return A list of <code>ConfigPropertyMetadata</code> objects.
   */
  public List<ConfigPropertyMetadata> getPropertyMetadata() {
    return mConfigPropMetadata;
  }

  /**
   * Returns the property meta data for a given property name.
   * 
   * @param property
   *          the property name
   * @return the coresponding property meta data or <code>null</code>
   */
  public ConfigPropertyMetadata getPropertyMetadata(String property) {

    ConfigPropertyMetadata propertyMeta = null;

    int size = mConfigPropMetadata != null ? mConfigPropMetadata.size() : 0;
    for (int i = 0; i < size; i++) {
      ConfigPropertyMetadata tmp = mConfigPropMetadata.get(i);

      if (tmp.getName().equals(property)) {
        propertyMeta = tmp;
        break;
      }
    }

    return propertyMeta;
  }

  /**
   * Returns the group this rule belongs to.
   * 
   * @return the group
   */
  public RuleGroupMetadata getGroup() {
    return mGroup;
  }
}