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

package net.sf.eclipsecs.ui.quickfixes;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerHelpContextProvider;

import net.sf.eclipsecs.core.builder.CheckstyleMarker;
import net.sf.eclipsecs.core.config.meta.MetadataFactory;

import org.eclipse.help.AbstractContextProvider;
import org.eclipse.help.IContext;
import org.eclipse.help.IHelpResource;

/**
 * Provides context help for checkstyle markers.
 */
public class MarkerHelpContextProvider extends AbstractContextProvider
        implements IMarkerHelpContextProvider {

  /**
   * package of the regexp checks
   */
  private static final String REGEXP_PACKAGE = "com.puppycrawl.tools.checkstyle.checks.regexp.";
  /**
   * suffix of all standard check implementations
   */
  private static final String CHECK_SUFFIX = "Check";
  /**
   * Common prefix for all Checkstyle marker help contexts. Must be same as plugin id and must end
   * with a dot.
   */
  private static final String PLUGIN_PREFIX = "net.sf.eclipsecs.ui" + ".";

  @Override
  public String getHelpContextForMarker(IMarker marker) {
    String module = getModule(marker);
    if (!module.endsWith(CHECK_SUFFIX)) {
      return null;
    }
    return PLUGIN_PREFIX + StringUtils.removeEnd(StringUtils.substringAfterLast(module, '.'), CHECK_SUFFIX);
  }

  private String getModule(IMarker marker) {
    return marker.getAttribute(CheckstyleMarker.MODULE_NAME, StringUtils.EMPTY);
  }

  @Override
  public boolean hasHelpContextForMarker(IMarker marker) {
    if (!CheckstyleMarker.isCheckstyleMarker(marker)) {
      return false;
    }
    if (getModule(marker).startsWith(REGEXP_PACKAGE)) {
      // regex rules don't provide useful help for understanding and fixing an issue
      return false;
    }
    return true;
  }

  @Override
  public IContext getContext(String id, String locale) {
    var moduleName = StringUtils.substringAfter(id, PLUGIN_PREFIX);
    return new CheckstyleHelpContext(moduleName);
  }

  @Override
  public String[] getPlugins() {
    return new String[] { "net.sf.eclipsecs.ui" };
  }

  /**
   * @param moduleName
   *          module name
   * @return online help URL
   */
  public static String getOnlineHelp(String moduleName) {
    var metadata = MetadataFactory.getRuleMetadata(moduleName);
    if (metadata == null) {
      return null;
    }
    var group = metadata.getGroup().getGroupId().toLowerCase();
    // some web pages are different to the packages in Checkstyle
    if ("indentation".equals(group)) {
      group = "misc";
    }
    var file = moduleName.toLowerCase();
    return "https://checkstyle.org/checks/" + group + "/" + file + ".html#" + moduleName;
  }

  /**
   * Help topic forwarding to the online help
   */
  private static final class CheckstyleHelpTopic implements IHelpResource {
    private final String moduleName;

    /**
     * @param moduleName
     *          module name
     */
    private CheckstyleHelpTopic(String moduleName) {
      this.moduleName = moduleName;
    }

    @Override
    public String getLabel() {
      return null;
    }

    @Override
    public String getHref() {
      return getOnlineHelp(moduleName);
    }
  }

  /**
   * Dynamically created help context for a checkstyle marker
   */
  private static final class CheckstyleHelpContext implements IContext {
    private final String moduleName;

    /**
     * @param moduleName
     */
    private CheckstyleHelpContext(String moduleName) {
      this.moduleName = moduleName;
    }

    @Override
    public IHelpResource[] getRelatedTopics() {
      IHelpResource helpResource = new CheckstyleHelpTopic(moduleName);
      return new IHelpResource[] { helpResource };
    }

    @Override
    public String getText() {
      // must be null, because the help manager will only show the URL immediately when no text is
      // defined
      return null;
    }
  }

}
