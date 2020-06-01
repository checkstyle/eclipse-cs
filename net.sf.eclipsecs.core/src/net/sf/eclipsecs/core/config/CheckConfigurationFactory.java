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

import com.google.common.io.ByteStreams;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.sf.eclipsecs.core.CheckstylePlugin;
import net.sf.eclipsecs.core.Messages;
import net.sf.eclipsecs.core.config.configtypes.BuiltInConfigurationType;
import net.sf.eclipsecs.core.config.configtypes.ConfigurationTypes;
import net.sf.eclipsecs.core.config.configtypes.IConfigurationType;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;

/**
 * Used to manage the life cycle of <code>CheckConfiguration</code> objects.
 */
public final class CheckConfigurationFactory {

  /** Name of the internal file storing the plugin check configurations. */
  protected static final String CHECKSTYLE_CONFIG_FILE = "checkstyle-config.xml"; //$NON-NLS-1$

  /** Name of the actual config file version. */
  private static final String VERSION_5_0_0 = "5.0.0"; //$NON-NLS-1$

  /** The current file version. */
  protected static final String CURRENT_CONFIG_FILE_FORMAT_VERSION = VERSION_5_0_0;

  /** constant for the extension point id. */
  private static final String CONFIGS_EXTENSION_POINT = CheckstylePlugin.PLUGIN_ID
          + ".configurations"; //$NON-NLS-1$

  /**
   * List of known check configurations. Synchronized because of possible concurrend access.
   */
  private static List<ICheckConfiguration> sConfigurations = Collections
          .synchronizedList(new ArrayList<ICheckConfiguration>());

  private static ICheckConfiguration sDefaultCheckConfig;

  private static ICheckConfiguration sDefaultBuiltInConfig;

  static {
    refresh();
  }

  private CheckConfigurationFactory() {
  }

  /**
   * Get an <code>CheckConfiguration</code> instance by its name.
   *
   * @param name
   *          Name of the requested instance.
   * @return The requested instance or <code>null</code> if the named instance could not be found.
   */
  public static ICheckConfiguration getByName(String name) {

    for (ICheckConfiguration config : sConfigurations) {
      if (config.getName().equals(name)) {
        return config;
      }
    }

    return null;
  }

  /**
   * Get a list of the currently defined check configurations.
   *
   * @return A list containing all instances.
   */
  public static List<ICheckConfiguration> getCheckConfigurations() {
    return Collections.unmodifiableList(sConfigurations);
  }

  /**
   * Returns the default check configuration if one is set, if none is set the Sun Checks built-in
   * configuration will be returned.
   *
   * @return the default check configuration to use with unconfigured projects
   */
  public static ICheckConfiguration getDefaultCheckConfiguration() {
    if (sDefaultCheckConfig != null) {
      return sDefaultCheckConfig;
    } else if (sDefaultBuiltInConfig != null) {
      return sDefaultBuiltInConfig;
    } else if (sConfigurations.size() > 0) {
      return sConfigurations.get(0);
    } else {
      return null;
    }
  }

  /**
   * Creates a new working set from the existing configurations.
   *
   * @return a new configuration working set
   */
  public static ICheckConfigurationWorkingSet newWorkingSet() {
    return new GlobalCheckConfigurationWorkingSet(sConfigurations, getDefaultCheckConfiguration(),
            sDefaultBuiltInConfig);
  }

  /**
   * Refreshes the check configurations from the persistent store.
   */
  public static void refresh() {
    try {
      sDefaultCheckConfig = null;
      sDefaultBuiltInConfig = null;
      sConfigurations.clear();
      loadBuiltinConfigurations();
      loadFromPersistence();

    } catch (CheckstylePluginException e) {
      CheckstyleLog.log(e);
    }
  }

  /**
   * Copy the checkstyle configuration of a check configuration into another configuration.
   *
   * @param source
   *          the source check configuration
   * @param target
   *          the target check configuartion
   * @throws CheckstylePluginException
   *           Error copying the configuration
   */
  public static void copyConfiguration(ICheckConfiguration source, ICheckConfiguration target)
          throws CheckstylePluginException {

    try {

      // use the export function ;-)
      File targetFile;

      targetFile = URIUtil.toFile(target.getResolvedConfigurationFileURL().toURI());

      File sourceFile = URIUtil.toFile(source.getResolvedConfigurationFileURL().toURI());

      // copying from a file to the same file will destroy it.
      if (Objects.equals(targetFile, sourceFile)) {
        return;
      }

      exportConfiguration(targetFile, source);
    } catch (URISyntaxException e) {
      CheckstylePluginException.rethrow(e);
    }
  }

  /**
   * Write check configurations to an external file in standard Checkstyle format.
   *
   * @param file
   *          File to write too.
   * @param config
   *          List of check configurations to write out.
   * @throws CheckstylePluginException
   *           Error during export.
   */
  public static void exportConfiguration(File file, ICheckConfiguration config)
          throws CheckstylePluginException {

    try (InputStream in = config.getCheckstyleConfiguration().getCheckConfigFileStream();
            OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {

      // Just copy the checkstyle configuration
      ByteStreams.copy(in, out);
    } catch (Exception e) {
      CheckstylePluginException.rethrow(e);
    }

  }

  /**
   * Transfers the internal checkstyle settings and configurations to a new workspace.
   *
   * @param targetWorkspaceRoot
   *          the target workspace root
   * @throws CheckstylePluginException
   *           if the transfer failed
   */
  public static void transferInternalConfiguration(IPath targetWorkspaceRoot)
          throws CheckstylePluginException {

    IPath targetStateLocation = getTargetStateLocation(targetWorkspaceRoot);

    File targetLocationFile = targetStateLocation.toFile();

    try {

      targetLocationFile.mkdirs();

      final Path sourcePath = CheckstylePlugin.getDefault().getStateLocation().toFile().toPath();
      final Path targetPath = targetLocationFile.toPath();

      // copy the entire directory contents
      Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
                throws IOException {
          Files.createDirectories(targetPath.resolve(sourcePath.relativize(dir)));
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                throws IOException {
          Files.copy(file, targetPath.resolve(sourcePath.relativize(file)));
          return FileVisitResult.CONTINUE;
        }
      });
    } catch (IllegalStateException e) {
      CheckstylePluginException.rethrow(e);
    } catch (IOException e) {
      CheckstylePluginException.rethrow(e);
    }
  }

  private static IPath getTargetStateLocation(IPath newWorkspaceRoot) {

    IPath currentWorkspaceRoot = Platform.getLocation();
    IPath currentStateLocation = CheckstylePlugin.getDefault().getStateLocation();

    if (currentStateLocation == null) {
      return null;
    }
    int segmentsToRemove = currentStateLocation.matchingFirstSegments(currentWorkspaceRoot);

    // Strip it down to the extension
    currentStateLocation = currentStateLocation.removeFirstSegments(segmentsToRemove);

    // Now add to the target workspace root
    IPath targetStateLocation = newWorkspaceRoot.append(currentStateLocation);
    return targetStateLocation;

  }

  /**
   * Load the check configurations from the persistent state storage.
   */
  private static void loadFromPersistence() throws CheckstylePluginException {

    File configFile = getInternalConfigurationFile();

    //
    // Make sure the files exists, it might not.
    //
    if (!configFile.exists()) {
      return;
    }

    try (InputStream inStream = new BufferedInputStream(new FileInputStream(configFile))) {

      SAXReader reader = new SAXReader();
      Document document = reader.read(inStream);

      Element root = document.getRootElement();

      String version = root.attributeValue(XMLTags.VERSION_TAG);
      if (!CURRENT_CONFIG_FILE_FORMAT_VERSION.equals(version)) {

        // the old (pre 4.0.0) configuration files aren't supported
        // anymore
        CheckstyleLog.log(null,
                "eclipse-cs version 3.x type configuration files are not supported anymore.");
        return;
      }

      String defaultConfigName = root.attributeValue(XMLTags.DEFAULT_CHECK_CONFIG_TAG);

      sConfigurations.addAll(getGlobalCheckConfigurations(root));

      for (ICheckConfiguration config : sConfigurations) {
        if (config.getName().equals(defaultConfigName) && config != sDefaultBuiltInConfig) {
          sDefaultCheckConfig = config;
        }
      }
    } catch (IOException | DocumentException e) {
      CheckstylePluginException.rethrow(e, Messages.errorLoadingConfigFile);
    }
  }

  private static File getInternalConfigurationFile() {
    IPath configPath = CheckstylePlugin.getDefault().getStateLocation();
    configPath = configPath.append(CHECKSTYLE_CONFIG_FILE);
    File configFile = configPath.toFile();
    return configFile;
  }

  /**
   * Loads the built-in check configurations defined in plugin.xml or custom fragments.
   */
  private static void loadBuiltinConfigurations() {

    IExtensionRegistry pluginRegistry = Platform.getExtensionRegistry();

    IConfigurationElement[] elements = pluginRegistry
            .getConfigurationElementsFor(CONFIGS_EXTENSION_POINT);

    int currentMaxDefaultWeight = -1;

    ICheckConfiguration defaultBuiltInCheckConfig = null;

    for (int i = 0; i < elements.length; i++) {
      String name = elements[i].getAttribute(XMLTags.NAME_TAG);
      String description = elements[i].getAttribute(XMLTags.DESCRIPTION_TAG);
      String location = elements[i].getAttribute(XMLTags.LOCATION_TAG);

      String defaultWeightAsString = elements[i].getAttribute(XMLTags.DEFAULT_WEIGHT);
      final int defaultWeight = defaultWeightAsString != null
              ? Integer.parseInt(defaultWeightAsString)
              : 0;

      IConfigurationType configType = ConfigurationTypes.getByInternalName("builtin");

      Map<String, String> additionalData = new HashMap<>();
      additionalData.put(BuiltInConfigurationType.CONTRIBUTOR_KEY,
              elements[i].getContributor().getName());

      List<ResolvableProperty> props = new ArrayList<>();
      IConfigurationElement[] propEls = elements[i].getChildren(XMLTags.PROPERTY_TAG);
      for (IConfigurationElement propEl : propEls) {
        props.add(new ResolvableProperty(propEl.getAttribute(XMLTags.NAME_TAG),
                propEl.getAttribute(XMLTags.VALUE_TAG)));
      }

      ICheckConfiguration checkConfig = new CheckConfiguration(name, location, description,
              configType, true, props, additionalData);
      sConfigurations.add(checkConfig);

      if (defaultWeight > currentMaxDefaultWeight) {
        currentMaxDefaultWeight = defaultWeight;
        defaultBuiltInCheckConfig = checkConfig;
      }
    }

    sDefaultBuiltInConfig = defaultBuiltInCheckConfig;
  }

  /**
   * Gets the check configurations from the configuration file document.
   *
   * @param root
   *          the root element of the plugins central configuration file
   * @return the global check configurations configured therein
   */
  private static List<ICheckConfiguration> getGlobalCheckConfigurations(Element root) {

    List<ICheckConfiguration> configs = new ArrayList<>();

    List<Element> configElements = root.elements(XMLTags.CHECK_CONFIG_TAG);

    for (Element configEl : configElements) {

      String name = configEl.attributeValue(XMLTags.NAME_TAG);
      String description = configEl.attributeValue(XMLTags.DESCRIPTION_TAG);
      String location = configEl.attributeValue(XMLTags.LOCATION_TAG);

      String type = configEl.attributeValue(XMLTags.TYPE_TAG);
      IConfigurationType configType = ConfigurationTypes.getByInternalName(type);

      // get resolvable properties
      List<ResolvableProperty> props = new ArrayList<>();
      List<Element> propertiesElements = configEl.elements(XMLTags.PROPERTY_TAG);
      for (Element propsEl : propertiesElements) {

        ResolvableProperty prop = new ResolvableProperty(propsEl.attributeValue(XMLTags.NAME_TAG),
                propsEl.attributeValue(XMLTags.VALUE_TAG));
        props.add(prop);
      }

      // get additional data
      Map<String, String> additionalData = new HashMap<>();
      List<Element> dataElements = configEl.elements(XMLTags.ADDITIONAL_DATA_TAG);
      for (Element dataEl : dataElements) {

        additionalData.put(dataEl.attributeValue(XMLTags.NAME_TAG),
                dataEl.attributeValue(XMLTags.VALUE_TAG));
      }

      ICheckConfiguration checkConfig = new CheckConfiguration(name, location, description,
              configType, true, props, additionalData);
      configs.add(checkConfig);
    }
    return configs;
  }
}
