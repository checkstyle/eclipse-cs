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
import com.google.common.io.Files;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.sf.eclipsecs.core.CheckstylePlugin;
import net.sf.eclipsecs.core.Messages;
import net.sf.eclipsecs.core.config.configtypes.IConfigurationType;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.osgi.util.NLS;
import org.xml.sax.InputSource;

/**
 * This class acts as wrapper around check configurations to add editing aspects. Check
 * configurations by themself are not editable.
 *
 * @author Lars Ködderitzsch
 */
public class CheckConfigurationWorkingCopy implements ICheckConfiguration, Cloneable {

  /** The source check configuration of the working copy. */
  private final ICheckConfiguration mCheckConfiguration;

  /** The working set this working copy belongs to. */
  private final ICheckConfigurationWorkingSet mWorkingSet;

  /** The edited name of the configuration. */
  private String mEditedName;

  /** The edited location of the configuration. */
  private String mEditedLocation;

  /** The edited description of the configuration. */
  private String mEditedDescription;

  /** The list of resolvable properties. */
  private List<ResolvableProperty> mProperties = new ArrayList<>();

  /** The map of additional data for this configuration. */
  private Map<String, String> mAdditionalData = new HashMap<>();

  /** flags if the configuration is dirty. */
  private boolean mHasConfigChanged;

  /**
   * Creates a new working copy from an existing check configuration.
   *
   * @param checkConfigToEdit
   *          the existing check configuration
   * @param workingSet
   *          the working set this working copy belongs to
   */
  public CheckConfigurationWorkingCopy(ICheckConfiguration checkConfigToEdit,
          ICheckConfigurationWorkingSet workingSet) {
    mCheckConfiguration = checkConfigToEdit;
    mWorkingSet = workingSet;

    mAdditionalData.putAll(checkConfigToEdit.getAdditionalData());

    List<ResolvableProperty> props = checkConfigToEdit.getResolvableProperties();
    for (ResolvableProperty prop : props) {
      mProperties.add(prop.clone());
    }
  }

  /**
   * Creates a working copy for a new check configuration.
   *
   * @param configType
   *          the type of the new configuration
   * @param workingSet
   *          the working set this working copy belongs to
   * @param global
   *          <code>true</code> if the new configuration is a global configuration
   */
  public CheckConfigurationWorkingCopy(IConfigurationType configType,
          ICheckConfigurationWorkingSet workingSet, boolean global) {

    mWorkingSet = workingSet;
    mCheckConfiguration = new CheckConfiguration(null, null, null, configType, global, null, null);
  }

  /**
   * Returns the source check configuration of this working copy.
   *
   * @return the source check configuration
   */
  public ICheckConfiguration getSourceCheckConfiguration() {
    return mCheckConfiguration;
  }

  /**
   * Changes the name of the check configuration.
   *
   * @param name
   *          the new name
   * @throws CheckstylePluginException
   *           if name is <code>null</code> or empty or a name collision with an existing check
   *           configuration exists
   */
  public void setName(String name) throws CheckstylePluginException {

    if (name == null || name.trim().length() == 0) {
      throw new CheckstylePluginException(Messages.errorConfigNameEmpty);
    }

    String oldName = getName();
    if (!name.equals(oldName)) {

      mEditedName = name;

      // Check if the new name is in use
      if (mWorkingSet.isNameCollision(this)) {
        mEditedName = oldName;
        throw new CheckstylePluginException(NLS.bind(Messages.errorConfigNameInUse, name));
      }
    }
  }

  /**
   * Changes the location of the Checkstyle configuration file.
   *
   * @param location
   *          the new location of Checkstyle configuration file
   * @throws CheckstylePluginException
   *           if location is <code>null</code> or empty or the Checkstyle configuration file cannot
   *           be resolved
   */
  public void setLocation(String location) throws CheckstylePluginException {
    if (location == null || location.trim().length() == 0) {
      throw new CheckstylePluginException(Messages.errorLocationEmpty);
    }

    String oldLocation = getLocation();
    if (!location.equals(oldLocation)) {

      try {
        mEditedLocation = location;

        // test if configuration file exists
        getCheckstyleConfiguration();
      } catch (Exception e) {
        mEditedLocation = oldLocation;
        CheckstylePluginException.rethrow(e,
                NLS.bind(Messages.errorResolveConfigLocation, location, e.getLocalizedMessage()));
      }
    }
  }

  /**
   * Sets a new description for the check configuration.
   *
   * @param description
   *          the new description
   */
  public void setDescription(String description) {
    String oldDescription = getDescription();
    if (description == null || !description.equals(oldDescription)) {
      mEditedDescription = description;
    }
  }

  /**
   * Flags if the working copy changed compared to the original check configuration and needs to be
   * saved.
   *
   * @return <code>true</code> if the working copy has changes over the original check configuration
   */
  public boolean isDirty() {
    return !this.equals(mCheckConfiguration);
  }

  /**
   * Determines if the checkstyle configuration of this working copy changed. This is used to
   * determine if specific projects need to rebuild afterwards.
   *
   * @return <code>true</code> if the checkstyle configuration changed.
   */
  public boolean hasConfigurationChanged() {
    return mHasConfigChanged || !(Objects.equals(getLocation(), mCheckConfiguration.getLocation())
            && Objects.equals(getResolvableProperties(),
                    mCheckConfiguration.getResolvableProperties())
            && Objects.equals(getAdditionalData(), mCheckConfiguration.getAdditionalData()));
  }

  /**
   * Reads the Checkstyle configuration file and builds the list of configured modules. Elements are
   * of type <code>net.sf.eclipsecs.core.config.Module</code>.
   *
   * @return the list of configured modules in this Checkstyle configuration
   * @throws CheckstylePluginException
   *           error when reading the Checkstyle configuration file
   */
  public List<Module> getModules() throws CheckstylePluginException {
    List<Module> result = null;

    InputSource in = null;

    try {
      in = getCheckstyleConfiguration().getCheckConfigFileInputSource();
      result = ConfigurationReader.read(in);
    } finally {
      Closeables.closeQuietly(in.getByteStream());
    }

    return result;
  }

  /**
   * Stores the (edited) list of modules to the Checkstyle configuration file.
   *
   * @param modules
   *          the list of modules to store into the Checkstyle configuration file
   * @throws CheckstylePluginException
   *           error storing the Checkstyle configuration
   */
  public void setModules(List<Module> modules) throws CheckstylePluginException {

    try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();) {

      // First write to a byte array outputstream
      // because otherwise in an error case the original
      // file would be destroyed
      ConfigurationWriter.write(byteOut, modules, this);

      // all went ok, write to the file
      File configFile = URIUtil.toFile(getResolvedConfigurationFileURL().toURI());
      Files.write(byteOut.toByteArray(), configFile);

      // refresh the files if within the workspace
      // Bug 1251194 - Resource out of sync after performing changes to
      // config
      IPath path = new Path(configFile.toString());
      IFile[] files = CheckstylePlugin.getWorkspace().getRoot().findFilesForLocation(path);
      for (int i = 0; i < files.length; i++) {
        try {
          files[i].refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
        } catch (CoreException e) {
          // NOOP - just ignore
        }
      }

      mHasConfigChanged = true;

      // throw away the cached Checkstyle configurations
      CheckConfigurationFactory.refresh();
    } catch (IOException | URISyntaxException e) {
      CheckstylePluginException.rethrow(e);
    }
  }

  @Override
  public String getName() {
    return mEditedName != null ? mEditedName : getSourceCheckConfiguration().getName();
  }

  @Override
  public String getDescription() {
    return mEditedDescription != null ? mEditedDescription
            : getSourceCheckConfiguration().getDescription();
  }

  @Override
  public String getLocation() {
    return mEditedLocation != null ? mEditedLocation : getSourceCheckConfiguration().getLocation();
  }

  @Override
  public IConfigurationType getType() {
    return getSourceCheckConfiguration().getType();
  }

  @Override
  public Map<String, String> getAdditionalData() {
    return mAdditionalData;
  }

  @Override
  public List<ResolvableProperty> getResolvableProperties() {
    return mProperties;
  }

  @Override
  public URL getResolvedConfigurationFileURL() throws CheckstylePluginException {
    return getType().getResolvedConfigurationFileURL(this);
  }

  @Override
  public CheckstyleConfigurationFile getCheckstyleConfiguration() throws CheckstylePluginException {
    return getType().getCheckstyleConfiguration(this);
  }

  @Override
  public boolean isEditable() {
    return getType().isEditable();
  }

  @Override
  public boolean isConfigurable() {
    return getType().isConfigurable(this);
  }

  @Override
  public boolean isGlobal() {
    return mCheckConfiguration.isGlobal();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof ICheckConfiguration)) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    ICheckConfiguration rhs = (ICheckConfiguration) obj;
    return Objects.equals(getName(), rhs.getName())
            && Objects.equals(getLocation(), rhs.getLocation())
            && Objects.equals(getDescription(), rhs.getDescription())
            && Objects.equals(getType(), rhs.getType()) && isGlobal() == rhs.isGlobal()
            && Objects.equals(getResolvableProperties(), rhs.getResolvableProperties())
            && Objects.equals(getAdditionalData(), rhs.getAdditionalData());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getLocation(), getDescription(), getType(), isGlobal(),
            getResolvableProperties(), getAdditionalData());
  }

  @Override
  public CheckConfigurationWorkingCopy clone() {

    CheckConfigurationWorkingCopy clone = null;

    try {
      clone = (CheckConfigurationWorkingCopy) super.clone();

      clone.mAdditionalData = new HashMap<>();
      clone.mAdditionalData.putAll(this.mAdditionalData);

      clone.mProperties = new ArrayList<>();

      for (ResolvableProperty prop : mProperties) {
        clone.mProperties.add(prop.clone());
      }
    } catch (CloneNotSupportedException e) {
      throw new InternalError(); // this should never happen
    }
    return clone;
  }
}
