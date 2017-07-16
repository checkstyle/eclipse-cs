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

package net.sf.eclipsecs.core.projectconfig;

import com.google.common.base.MoreObjects;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import net.sf.eclipsecs.core.config.ICheckConfiguration;

import org.eclipse.core.resources.IFile;

/**
 * A File Set is a collection of files audited with a common set of audit rules.
 */
public class FileSet implements Cloneable {

  private String mName;

  private ICheckConfiguration mCheckConfig;

  private boolean mEnabled = true;

  private List<FileMatchPattern> mFileMatchPatterns = new LinkedList<>();

  /**
   * Default constructor.
   */
  public FileSet() {

  }

  /**
   * Default constructor.
   *
   * @param name
   *          The name of the <code>FileSet</code>
   * @param checkConfig
   *          The name of the <code>CheckConfiguration</code> used to check this
   *          <code>FileSet</code>.
   */
  public FileSet(String name, ICheckConfiguration checkConfig) {
    setName(name);
    setCheckConfig(checkConfig);
  }

  /**
   * Returns a list of <code>FileMatchPattern</code> objects.
   *
   * @return List
   */
  public List<FileMatchPattern> getFileMatchPatterns() {
    return mFileMatchPatterns;
  }

  /**
   * Set the list of <code>FileMatchPattern</code> objects.
   *
   * @param list
   *          The new list of pattern objects.
   */
  public void setFileMatchPatterns(List<FileMatchPattern> list) {
    mFileMatchPatterns = list;
  }

  /**
   * Get the check configuration used by this file set.
   *
   * @return The check configuration used to audit files in the file set.
   */
  public ICheckConfiguration getCheckConfig() {
    return mCheckConfig;
  }

  /**
   * Sets the check configuration used by this file set.
   *
   * @param checkConfig
   *          the check configuration
   */
  public void setCheckConfig(ICheckConfiguration checkConfig) {
    mCheckConfig = checkConfig;
  }

  /**
   * Returns the name.
   *
   * @return String
   */
  public String getName() {
    return mName;
  }

  /**
   * Sets the name.
   *
   * @param name
   *          The name to set
   */
  public void setName(String name) {
    mName = name;
  }

  /**
   * Returns the enabled flag.
   *
   * @return boolean
   */
  public boolean isEnabled() {
    return mEnabled;
  }

  /**
   * Sets the enabled flag.
   *
   * @param enabled
   *          The enabled to set
   */
  public void setEnabled(boolean enabled) {
    mEnabled = enabled;
  }

  /**
   * Tests a file to see if its included in the file set.
   *
   * @param file
   *          The file to test.
   * @return <code>true</code>= the file is included in the file set, <code>false</code>= the file
   *         is not included in the file set.
   */
  public boolean includesFile(IFile file) {
    boolean result = false;
    String filePath = file.getProjectRelativePath().toString();

    for (FileMatchPattern pattern : mFileMatchPatterns) {
      boolean matches = pattern.isMatch(filePath);
      if (matches) {
        if (pattern.isIncludePattern()) {
          result = true;
        } else {
          result = false;
        }
      }
    }

    return result;
  }

  @Override
  public FileSet clone() {
    try {
      FileSet clone = (FileSet) super.clone();

      // clone filesets
      List<FileMatchPattern> clonedPatterns = new LinkedList<>();
      for (FileMatchPattern pattern : mFileMatchPatterns) {
        clonedPatterns.add(pattern.clone());
      }
      clone.mFileMatchPatterns = clonedPatterns;

      return clone;
    } catch (CloneNotSupportedException e) {
      throw new InternalError(); // should never happen
    }
  }

  @Override
  public boolean equals(Object obj) {

    if (obj == null || !(obj instanceof FileSet)) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    FileSet rhs = (FileSet) obj;
    return mEnabled == rhs.mEnabled && Objects.equals(mName, rhs.mName)
            && Objects.equals(mFileMatchPatterns, rhs.mFileMatchPatterns)
            && Objects.equals(mCheckConfig, rhs.mCheckConfig);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mEnabled, mName, mCheckConfig, mFileMatchPatterns, mCheckConfig);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("enabled", mEnabled).add("name", mName)
            .add("fileMatchPatterns", mFileMatchPatterns).add("checkConfig", mCheckConfig)
            .toString();
  }
}
