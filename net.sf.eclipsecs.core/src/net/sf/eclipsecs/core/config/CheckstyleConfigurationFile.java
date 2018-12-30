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

import com.puppycrawl.tools.checkstyle.PropertyResolver;

import java.io.ByteArrayInputStream;
import java.net.URL;

import org.xml.sax.InputSource;

/**
 * Simple object containing all sort of data of a Checkstyle configuration. This is done to not
 * access the Checkstyle configuration file too many times to get small bits of information.
 * 
 * @author Lars Ködderitzsch
 */
public class CheckstyleConfigurationFile {

  private byte[] mCheckConfigFileBytes;

  private byte[] mAdditionalPropertyBundleBytes;

  private long mModificationStamp;

  private URL mResolvedConfigFileURL;

  private PropertyResolver mPropertyResolver;

  /**
   * Returns an input stream containing the contents of the Checkstyle configuration file.
   * 
   * @return the input stream containing the Checkstyle configuration file
   */
  public ByteArrayInputStream getCheckConfigFileStream() {
    return new ByteArrayInputStream(mCheckConfigFileBytes);
  }

  /**
   * Returns a SAX input source of the Checkstyle configuration file. The resolved URL of the
   * configuration file will be set as SystemId to allow for parser resolution of relative entities.
   * 
   * @return a SAX input source of the
   */
  public InputSource getCheckConfigFileInputSource() {

    InputSource in = new InputSource(getCheckConfigFileStream());
    in.setSystemId(getResolvedConfigFileURL().toString());

    return in;
  }

  /**
   * Sets the content of the Checkstyle configuration file as a byte array.
   * 
   * @param checkConfigFileBytes
   *          the Checkstyle configuration file contents
   */
  public void setCheckConfigFileBytes(byte[] checkConfigFileBytes) {
    mCheckConfigFileBytes = checkConfigFileBytes;
  }

  /**
   * Returns an <code>ByteArrayInputStream</code> of the addional property bundle.
   * 
   * @return the stream containing the bundle data
   */
  public ByteArrayInputStream getAdditionalPropertiesBundleStream() {
    if (mAdditionalPropertyBundleBytes != null) {
      return new ByteArrayInputStream(mAdditionalPropertyBundleBytes);
    }
    return null;
  }

  /**
   * Sets the content of an addtional property bundle which contains values for property references
   * in the Checkstyle configuration file.
   * 
   * @param additionalPropertyBundleBytes
   *          the content of the additional property bundle
   */
  public void setAdditionalPropertyBundleBytes(byte[] additionalPropertyBundleBytes) {
    mAdditionalPropertyBundleBytes = additionalPropertyBundleBytes;
  }

  /**
   * Returns the modification timestamp or 0 if none can be determined.
   * 
   * @return the modification timestamp of the Checkstyle configuration file
   */
  public long getModificationStamp() {
    return mModificationStamp;
  }

  /**
   * Sets the modification timestamp of the Checkstyle configuration file.
   * 
   * @param modificationStamp
   *          the modification timestamp
   */
  public void setModificationStamp(long modificationStamp) {
    this.mModificationStamp = modificationStamp;
  }

  /**
   * Returns the resolved URL of the Checkstyle configuration file. Clients are expected to
   * <b>not</b> use this to access the underlying Checkstyle configuration file
   * 
   * @return the resolved URL
   */
  public URL getResolvedConfigFileURL() {
    return mResolvedConfigFileURL;
  }

  /**
   * Sets the resolved URL of the Checkstyle configuration file.
   * 
   * @param resolvedConfigFileURL
   *          the resolved URL
   */
  public void setResolvedConfigFileURL(URL resolvedConfigFileURL) {
    this.mResolvedConfigFileURL = resolvedConfigFileURL;
  }

  /**
   * Returns the property resolver or <code>null</code> if none has been set.
   * 
   * @return the property resolver
   */
  public PropertyResolver getPropertyResolver() {
    return mPropertyResolver;
  }

  /**
   * Sets the property resolver for this Checkstyle configuration.
   * 
   * @param propertyResolver
   *          the property resolver
   */
  public void setPropertyResolver(PropertyResolver propertyResolver) {
    mPropertyResolver = propertyResolver;
  }

}
