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

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.google.common.base.MoreObjects;

import net.sf.eclipsecs.core.Messages;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

/**
 * A file match pattern is a pattern used in a regular express to check for matching file names.
 */
public class FileMatchPattern implements Cloneable {

  private boolean isIncludePattern = true;

  private Pattern regexPattern;

  private String patternString;

  /**
   * Construct a new <code>FileMatchPattern</code>.
   *
   * @param pattern
   *          The new pattern.
   * @throws CheckstylePluginException
   *           Error during processing
   */
  public FileMatchPattern(String pattern) throws CheckstylePluginException {
    setMatchPattern(pattern);
  }

  /**
   * Returns the match pattern.
   *
   * @return String
   */
  public String getMatchPattern() {
    return regexPattern.pattern();
  }

  /**
   * Sets the match pattern.
   *
   * @param pattern
   *          The match pattern to set
   * @throws CheckstylePluginException
   *           Error during processing
   */
  public final void setMatchPattern(String pattern) throws CheckstylePluginException {
    if ((pattern == null) || (pattern.trim().length() == 0)) {
      throw new CheckstylePluginException(Messages.errorEmptyPattern);
    }
    try {
      regexPattern = Pattern.compile(pattern);
      patternString = pattern;
    } catch (PatternSyntaxException ex) {
      // wrap the exception
      CheckstylePluginException.rethrow(ex);
    }
  }

  /**
   * Tests a file name to see if it matches the pattern.
   *
   * @param fileName
   *          File name to be tested.
   * @return <code>true</code>= match, <code>false</code>= no match.
   */
  public boolean isMatch(String fileName) {
    boolean result = false;

    Matcher matcher = regexPattern.matcher(fileName);
    result = matcher.find();

    return result;
  }

  /**
   * Returns the isIncludePattern.
   *
   * @return boolean
   */
  public boolean isIncludePattern() {
    return isIncludePattern;
  }

  /**
   * Sets the isIncludePattern.
   *
   * @param isIncludePattern
   *          The isIncludePattern to set
   */
  public void setIsIncludePattern(boolean isIncludePattern) {
    this.isIncludePattern = isIncludePattern;
  }

  /**
   * Clone the object.
   *
   * @return The clone
   */
  @Override
  public FileMatchPattern clone() {
    try {
      return (FileMatchPattern) super.clone();
    } catch (CloneNotSupportedException ex) {
      // should never happen
      throw new InternalError(ex);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof FileMatchPattern)) {
      return false;
    }
    if (this == obj) {
      return true;
    }

    FileMatchPattern rhs = (FileMatchPattern) obj;
    return Objects.equals(isIncludePattern, rhs.isIncludePattern)
            && Objects.equals(patternString, rhs.patternString);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isIncludePattern, patternString);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("patternString", patternString)
            .add("isIncludePattern", isIncludePattern).toString();
  }
}
