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

package net.sf.eclipsecs.core.config;

import com.google.common.io.Closeables;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader.IgnoredModulesOptions;
import com.puppycrawl.tools.checkstyle.PropertyResolver;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.eclipsecs.core.config.configtypes.IContextAware;
import net.sf.eclipsecs.core.config.configtypes.MultiPropertyResolver;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

import org.eclipse.core.resources.IProject;
import org.xml.sax.InputSource;

/**
 * This class is used to test check configurations for obvious problems such as:
 * <ul>
 * <li>Checkstyle configuration file cannot be found</li>
 * <li>Checkstyle configuration file contains unresolved properties</li>
 * </ul>
 * .
 *
 * @author Lars Ködderitzsch
 */
public class CheckConfigurationTester {

  /** The check configuration to test. */
  private ICheckConfiguration mCheckConfiguration;

  private IProject mContextProject;

  /**
   * Creates a tester for the given check configuration.
   *
   * @param checkConfiguration
   *          the check configuration to test
   */
  public CheckConfigurationTester(ICheckConfiguration checkConfiguration) {
    mCheckConfiguration = checkConfiguration;
  }

  /**
   * Tests a configuration if there are unresolved properties.
   *
   * @return the list of unresolved properties as ResolvableProperty values.
   * @throws CheckstylePluginException
   *           most likely the configuration file could not be found
   */
  public List<ResolvableProperty> getUnresolvedProperties() throws CheckstylePluginException {

    CheckstyleConfigurationFile configFile = mCheckConfiguration.getCheckstyleConfiguration();

    PropertyResolver resolver = configFile.getPropertyResolver();

    // set the project context if the property resolver needs the
    // context
    if (mContextProject != null && resolver instanceof IContextAware) {
      ((IContextAware) resolver).setProjectContext(mContextProject);
    }

    MissingPropertyCollector collector = new MissingPropertyCollector();

    if (resolver instanceof MultiPropertyResolver) {
      ((MultiPropertyResolver) resolver).addPropertyResolver(collector);
    } else {
      MultiPropertyResolver multiResolver = new MultiPropertyResolver();
      multiResolver.addPropertyResolver(resolver);
      multiResolver.addPropertyResolver(collector);
      resolver = multiResolver;
    }

    InputSource in = null;
    try {
      in = configFile.getCheckConfigFileInputSource();
      ConfigurationLoader.loadConfiguration(in, resolver, IgnoredModulesOptions.EXECUTE);
    } catch (CheckstyleException e) {
      CheckstylePluginException.rethrow(e);
    } finally {
      Closeables.closeQuietly(in.getByteStream());
    }

    return collector.getUnresolvedProperties();
  }

  /**
   * A property resolver that itself does not resolve properties but collects properties that are
   * not being resolved by a given other property resolver. This is used to find unresolved
   * properties after all other property reolvers have been asked.
   *
   * @author Lars Ködderitzsch
   */
  private static class MissingPropertyCollector implements PropertyResolver {

    /**
     * Properties that will be ignored, because they can always be resolved when the configuration
     * is used in the context of a project.
     */
    private static final List<String> IGNORE_PROPS = Arrays.asList("basedir", //$NON-NLS-1$
            "project_loc"); //$NON-NLS-1$

    /** The list of unresolved properties. */
    private List<ResolvableProperty> mUnresolvedProperties = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve(String aName) {

      if (!IGNORE_PROPS.contains(aName)) {
        ResolvableProperty prop = new ResolvableProperty(aName, null);

        // rule out duplicates
        if (!mUnresolvedProperties.contains(prop)) {
          mUnresolvedProperties.add(prop);
        }
      }
      // return warning to prevent hiccups with properties used in module
      // severity. Bad hack, I know :-(
      return "warning"; //$NON-NLS-1$
    }

    /**
     * The list of unresolved properties containing ResolvableProperty items.
     *
     * @return the list of unresolved properties.
     */
    public List<ResolvableProperty> getUnresolvedProperties() {
      return mUnresolvedProperties;
    }
  }
}
