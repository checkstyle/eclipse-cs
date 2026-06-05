//============================================================================
//
// Copyright (C) 2003-2023 the original author or authors.
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

package net.sf.eclipsecs.ui.util;

import java.util.regex.Pattern;

import net.sf.eclipsecs.ui.Messages;

/**
 * Utility class for HTML formatting operations.
 */
public final class HtmlUtil {

  private static final Pattern PATTERN_INLINE_CODE = Pattern
          .compile(Pattern.quote("{@code ") + "([^}]*?)" + Pattern.quote("}"));

  private HtmlUtil() {
  }

  /**
   * Convert a module description to HTML for use with a browser component.
   * @param description module description
   * @return HTML converted description
   */
  public static String getDescriptionHtml(String description) {
    StringBuilder buf = new StringBuilder();
    buf.append("<html><body style=\"margin: 3px; font-size: 11px; ");
    buf.append("font-family: verdana, 'trebuchet MS', helvetica, sans-serif;\">");
    buf.append(description != null ? convertInlineCodeTags(description)
            : Messages.CheckConfigurationConfigureDialog_txtNoDescription);
    buf.append("</body></html>");
    return buf.toString();
  }

  private static String convertInlineCodeTags(String html) {
    return PATTERN_INLINE_CODE.matcher(html).replaceAll("<code>$1</code>");
  }
}
