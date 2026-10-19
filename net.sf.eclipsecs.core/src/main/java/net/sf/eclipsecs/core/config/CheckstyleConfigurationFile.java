///////////////////////////////////////////////////////////////////////////////////////////////
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
///////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.eclipsecs.core.config;

import java.io.ByteArrayInputStream;
import java.net.URL;

import org.xml.sax.InputSource;

import com.puppycrawl.tools.checkstyle.PropertyResolver;

/**
 * Simple object containing all sort of data of a Checkstyle configuration. This is done to not
 * access the Checkstyle configuration file too many times to get small bits of information.
 *
 */
public class CheckstyleConfigurationFile {

    /** The checkstyle configuration file bytes. */
    private byte[] checkConfigFileBytes;

    /** The additional property bundle bytes. */
    private byte[] additionalPropertyBundleBytes;

    /** The modification timestamp of the configuration file. */
    private long modificationStamp;

    /** The resolved configuration file URL. */
    private URL resolvedConfigFileURL;

    /** The property resolver. */
    private PropertyResolver propertyResolver;

    /**
     * Creates a new instance.
     */
    public CheckstyleConfigurationFile() {
    }

    /**
     * Returns an input stream containing the contents of the Checkstyle configuration file.
     *
     * @return the input stream containing the Checkstyle configuration file
     */
    public ByteArrayInputStream getCheckConfigFileStream() {
        return new ByteArrayInputStream(checkConfigFileBytes);
    }

    /**
     * Returns a SAX input source of the Checkstyle configuration file. The resolved URL of the
     * configuration file will be set as SystemId to allow for parser resolution of relative
     * entities.
     *
     * @return a SAX input source of the
     */
    public InputSource getCheckConfigFileInputSource() {

        final InputSource input = new InputSource(getCheckConfigFileStream());
        input.setSystemId(getResolvedConfigFileURL().toString());

        return input;
    }

    /**
     * Sets the content of the Checkstyle configuration file as a byte array.
     *
     * @param checkConfigFileBytes
     *            the Checkstyle configuration file contents
     */
    public void setCheckConfigFileBytes(byte[] checkConfigFileBytes) {
        this.checkConfigFileBytes = checkConfigFileBytes;
    }

    /**
     * Returns an <code>ByteArrayInputStream</code> of the addional property bundle.
     *
     * @return the stream containing the bundle data
     */
    public ByteArrayInputStream getAdditionalPropertiesBundleStream() {
        ByteArrayInputStream stream = null;
        if (additionalPropertyBundleBytes != null) {
            stream = new ByteArrayInputStream(additionalPropertyBundleBytes);
        }
        return stream;
    }

    /**
     * Sets the content of an addtional property bundle which contains values for property
     * references in the Checkstyle configuration file.
     *
     * @param additionalPropertyBundleBytes
     *            the content of the additional property bundle
     */
    public void setAdditionalPropertyBundleBytes(byte[] additionalPropertyBundleBytes) {
        this.additionalPropertyBundleBytes = additionalPropertyBundleBytes;
    }

    /**
     * Returns the modification timestamp or 0 if none can be determined.
     *
     * @return the modification timestamp of the Checkstyle configuration file
     */
    public long getModificationStamp() {
        return modificationStamp;
    }

    /**
     * Sets the modification timestamp of the Checkstyle configuration file.
     *
     * @param modificationStamp
     *            the modification timestamp
     */
    public void setModificationStamp(long modificationStamp) {
        this.modificationStamp = modificationStamp;
    }

    /**
     * Returns the resolved URL of the Checkstyle configuration file. Clients are expected to
     * <b>not</b> use this to access the underlying Checkstyle configuration file
     *
     * @return the resolved URL
     */
    public URL getResolvedConfigFileURL() {
        return resolvedConfigFileURL;
    }

    /**
     * Sets the resolved URL of the Checkstyle configuration file.
     *
     * @param resolvedConfigFileURL
     *            the resolved URL
     */
    public void setResolvedConfigFileURL(URL resolvedConfigFileURL) {
        this.resolvedConfigFileURL = resolvedConfigFileURL;
    }

    /**
     * Returns the property resolver or <code>null</code> if none has been set.
     *
     * @return the property resolver
     */
    public PropertyResolver getPropertyResolver() {
        return propertyResolver;
    }

    /**
     * Sets the property resolver for this Checkstyle configuration.
     *
     * @param propertyResolver
     *            the property resolver
     */
    public void setPropertyResolver(PropertyResolver propertyResolver) {
        this.propertyResolver = propertyResolver;
    }

}
