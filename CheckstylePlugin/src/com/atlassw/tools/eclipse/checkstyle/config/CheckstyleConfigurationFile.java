
package com.atlassw.tools.eclipse.checkstyle.config;

import java.io.ByteArrayInputStream;
import java.net.URL;

import com.puppycrawl.tools.checkstyle.PropertyResolver;

public class CheckstyleConfigurationFile
{

    private byte[] mCheckConfigFileBytes;

    private byte[] mAdditionalPropertyBundleBytes;

    private long mModificationStamp;

    private URL mResolvedConfigFileURL;

    private PropertyResolver mPropertyResolver;

    public ByteArrayInputStream getCheckConfigFileStream()
    {
        return new ByteArrayInputStream(mCheckConfigFileBytes);
    }

    public void setCheckConfigFileBytes(byte[] checkConfigFileBytes)
    {
        mCheckConfigFileBytes = checkConfigFileBytes;
    }

    public ByteArrayInputStream getAdditionalPropertiesBundleStream()
    {
        if (mAdditionalPropertyBundleBytes != null)
        {
            return new ByteArrayInputStream(mAdditionalPropertyBundleBytes);
        }
        return null;
    }

    public void setAdditionalPropertyBundleBytes(byte[] additionalPropertyBundleBytes)
    {
        mAdditionalPropertyBundleBytes = additionalPropertyBundleBytes;
    }

    public long getModificationStamp()
    {
        return mModificationStamp;
    }

    public void setModificationStamp(long modificationStamp)
    {
        this.mModificationStamp = modificationStamp;
    }

    public URL getResolvedConfigFileURL()
    {
        return mResolvedConfigFileURL;
    }

    public void setResolvedConfigFileURL(URL resolvedConfigFileURL)
    {
        this.mResolvedConfigFileURL = resolvedConfigFileURL;
    }

    public PropertyResolver getPropertyResolver()
    {
        return mPropertyResolver;
    }

    public void setPropertyResolver(PropertyResolver propertyResolver)
    {
        mPropertyResolver = propertyResolver;
    }

}
