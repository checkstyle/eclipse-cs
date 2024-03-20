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

package net.sf.eclipsecs.core.projectconfig;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IFile;

import com.google.common.base.MoreObjects;

import net.sf.eclipsecs.core.config.ICheckConfiguration;

/**
 * A File Set is a collection of files audited with a common set of audit rules.
 */
public class FileSet implements Cloneable {

  private String name;

  private ICheckConfiguration checkConfig;

  private boolean enabled = true;

  private List<FileMatchPattern> fileMatchPatterns = new LinkedList<>();

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
    this.name = name;
    this.checkConfig = checkConfig;
  }

  /**
   * Returns a list of <code>FileMatchPattern</code> objects.
   *
   * @return List
   */
  public List<FileMatchPattern> getFileMatchPatterns() {
    return fileMatchPatterns;
  }

  /**
   * Set the list of <code>FileMatchPattern</code> objects.
   *
   * @param fileMatchPatterns
   *          The new list of pattern objects.
   */
  public void setFileMatchPatterns(List<FileMatchPattern> fileMatchPatterns) {
    this.fileMatchPatterns = fileMatchPatterns;
  }

  /**
   * Get the check configuration used by this file set.
   *
   * @return The check configuration used to audit files in the file set.
   */
  public ICheckConfiguration getCheckConfig() {
    return checkConfig;
  }

  /**
   * Sets the check configuration used by this file set.
   *
   * @param checkConfig
   *          the check configuration
   */
  public void setCheckConfig(ICheckConfiguration checkConfig) {
    this.checkConfig = checkConfig;
  }

  /**
   * Returns the name.
   *
   * @return String
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name
   *          The name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the enabled flag.
   *
   * @return boolean
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Sets the enabled flag.
   *
   * @param enabled
   *          The enabled to set
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
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

    for (FileMatchPattern pattern : fileMatchPatterns) {
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
      for (FileMatchPattern pattern : fileMatchPatterns) {
        clonedPatterns.add(pattern.clone());
      }
      clone.fileMatchPatterns = clonedPatterns;

      return clone;
    } catch (CloneNotSupportedException ex) {
      // should never happen
      throw new InternalError(ex);
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
    return enabled == rhs.enabled && Objects.equals(name, rhs.name)
            && Objects.equals(fileMatchPatterns, rhs.fileMatchPatterns)
            && Objects.equals(checkConfig, rhs.checkConfig);
  }

  @Override
  public int hashCode() {
    return Objects.hash(enabled, name, checkConfig, fileMatchPatterns, checkConfig);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("enabled", enabled).add("name", name)
            .add("fileMatchPatterns", fileMatchPatterns).add("checkConfig", checkConfig)
            .toString();
  }
}
