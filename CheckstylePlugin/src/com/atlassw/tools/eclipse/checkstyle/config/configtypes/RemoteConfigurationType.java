//============================================================================
//
// Copyright (C) 2002-2007  David Schneider, Lars Ködderitzsch
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

package com.atlassw.tools.eclipse.checkstyle.config.configtypes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.Messages;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.puppycrawl.tools.checkstyle.PropertyResolver;

/**
 * Implementation of a check configuration that uses an exteral checkstyle
 * configuration file.
 * 
 * @author Lars Ködderitzsch
 */
public class RemoteConfigurationType extends ConfigurationType
{

    /** Key to access the information if the configuration should be cached. */
    public static final String KEY_CACHE_CONFIG = "cache-file"; //$NON-NLS-1$

    /** Key to access the path of the cached configuration file. */
    public static final String KEY_CACHE_FILE_LOCATION = "cache-file-location"; //$NON-NLS-1$

    /** Key to access the username. */
    public static final String KEY_USERNAME = "username"; //$NON-NLS-1$

    /** Key to access the password. */
    public static final String KEY_PASSWORD = "password"; //$NON-NLS-1$

    /**
     * {@inheritDoc}
     */
    public InputStream openConfigurationFileStream(ICheckConfiguration checkConfiguration)
        throws CheckstylePluginException
    {

        InputStream inStream = null;

        boolean useCacheFile = Boolean.valueOf(
                (String) checkConfiguration.getAdditionalData().get(KEY_CACHE_CONFIG))
                .booleanValue();

        if (useCacheFile)
        {
            synchronizeCacheFile(checkConfiguration);
        }

        try
        {
            inStream = getStreamFromOriginalLocation(checkConfiguration);
        }
        catch (CheckstylePluginException e)
        {
            if (useCacheFile)
            {
                inStream = getStreamFromCacheFile(checkConfiguration);
            }
            else
            {
                throw e;
            }
        }

        return inStream;
    }

    /**
     * {@inheritDoc}
     */
    public URL resolveLocation(ICheckConfiguration checkConfiguration)
        throws CheckstylePluginException
    {
        try
        {
            return new URL(checkConfiguration.getLocation());
        }
        catch (MalformedURLException e)
        {
            CheckstylePluginException.rethrow(e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void notifyCheckConfigRemoved(ICheckConfiguration checkConfiguration)
        throws CheckstylePluginException
    {
        super.notifyCheckConfigRemoved(checkConfiguration);

        // remove authentication info
        RemoteConfigAuthenticator.removeCachedAuthInfo(checkConfiguration);

        boolean useCacheFile = Boolean.valueOf(
                (String) checkConfiguration.getAdditionalData().get(KEY_CACHE_CONFIG))
                .booleanValue();

        if (useCacheFile)
        {
            // remove the cahced configuration file from the workspace metadata
            String cacheFileLocation = (String) checkConfiguration.getAdditionalData().get(
                    KEY_CACHE_FILE_LOCATION);

            IPath cacheFilePath = CheckstylePlugin.getDefault().getStateLocation();
            cacheFilePath = cacheFilePath.append(cacheFileLocation);
            File cacheFile = cacheFilePath.toFile();
            cacheFile.delete();
        }
    }

    /**
     * {@inheritDoc}
     */
    public PropertyResolver getPropertyResolver(ICheckConfiguration checkConfiguration)
        throws CheckstylePluginException
    {

        String location = checkConfiguration.getLocation();

        MultiPropertyResolver multiResolver = new MultiPropertyResolver();
        multiResolver.addPropertyResolver(new ResolvablePropertyResolver(checkConfiguration));
        multiResolver.addPropertyResolver(new StandardPropertyResolver(location));
        multiResolver.addPropertyResolver(new ClasspathVariableResolver());
        multiResolver.addPropertyResolver(new SystemPropertyResolver());

        ResourceBundle bundle = getBundle(location);
        if (bundle != null)
        {
            multiResolver.addPropertyResolver(new ResourceBundlePropertyResolver(bundle));
        }

        return multiResolver;
    }

    /**
     * Method to get the input stream for the remote location config file.
     * 
     * @param checkConfig the check configuration
     * @return the input stream
     * @throws CheckstylePluginException error getting the stream (remote
     *             location not reachable)
     */
    private InputStream getStreamFromOriginalLocation(ICheckConfiguration checkConfig)
        throws CheckstylePluginException
    {

        InputStream inStream = null;

        try
        {
            Authenticator.setDefault(new RemoteConfigAuthenticator(checkConfig));

            URL configURL = resolveLocation(checkConfig);
            inStream = new BufferedInputStream(configURL.openStream());
        }
        catch (IOException e)
        {
            CheckstylePluginException.rethrow(e);
        }

        return inStream;
    }

    /**
     * Method to get an input stream to the cached configuration file.
     * 
     * @param checkConfig the check configuration
     * @return the input stream
     * @throws CheckstylePluginException error getting the stream (file does not
     *             exist)
     */
    private InputStream getStreamFromCacheFile(ICheckConfiguration checkConfig)
        throws CheckstylePluginException
    {
        String cacheFileLocation = (String) checkConfig.getAdditionalData().get(
                KEY_CACHE_FILE_LOCATION);

        IPath cacheFilePath = CheckstylePlugin.getDefault().getStateLocation();
        cacheFilePath = cacheFilePath.append(cacheFileLocation);
        File cacheFile = cacheFilePath.toFile();

        InputStream inStream = null;

        try
        {
            URL configURL = cacheFile.toURL();
            inStream = new BufferedInputStream(configURL.openStream());
        }
        catch (IOException e)
        {
            CheckstylePluginException.rethrow(e);
        }

        return inStream;
    }

    /**
     * Method to synchronize the cached configuration file from the remote
     * location.
     * 
     * @param checkConfig the check configuration
     * @return <code>true</code> if the synchronization was successful,
     *         <code>false</code> otherwise
     */
    private boolean synchronizeCacheFile(ICheckConfiguration checkConfig)
    {

        String cacheFileLocation = (String) checkConfig.getAdditionalData().get(
                KEY_CACHE_FILE_LOCATION);

        IPath cacheFilePath = CheckstylePlugin.getDefault().getStateLocation();
        cacheFilePath = cacheFilePath.append(cacheFileLocation);
        File cacheFile = cacheFilePath.toFile();

        OutputStream outStream = null;
        InputStream inStream = null;

        try
        {

            // fetch the original configuration file and store into the cache
            // file
            inStream = getStreamFromOriginalLocation(checkConfig);

            // temporary output stream to ensure the input is fully fetched
            // before writing to the actual cache file
            ByteArrayOutputStream tmpOut = new ByteArrayOutputStream();

            byte[] buf = new byte[512];
            int len = 0;
            while ((len = inStream.read(buf)) > -1)
            {
                tmpOut.write(buf, 0, len);
            }

            // finally write to the cache file
            outStream = new BufferedOutputStream(new FileOutputStream(cacheFile));
            outStream.write(tmpOut.toByteArray());
            outStream.flush();
        }
        catch (IOException e)
        {
            CheckstyleLog.log(e, NLS.bind(Messages.RemoteConfigurationType_msgRemoteCachingFailed,
                    checkConfig.getName(), checkConfig.getLocation()));
            return false;
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.log(e, NLS.bind("Could not cache remote configuration {0} ({1})", //$NON-NLS-1$
                    checkConfig.getName(), checkConfig.getLocation()));
            return false;
        }
        finally
        {
            IOUtils.closeQuietly(inStream);
            IOUtils.closeQuietly(outStream);
        }
        return true;
    }

    /**
     * Helper method to get the resource bundle for this configuration.
     * 
     * @param location the configuration file location
     * @return the resource bundle or <code>null</code> if no bundle exists
     */
    private static ResourceBundle getBundle(String location)
    {

        ResourceBundle bundle = null;
        InputStream in = null;
        try
        {

            // Strip file extension
            String propsLocation = null;

            int lastPointIndex = location.lastIndexOf("."); //$NON-NLS-1$
            if (lastPointIndex > -1)
            {
                propsLocation = location.substring(0, lastPointIndex);
            }
            else
            {
                propsLocation = location;
            }

            URL propUrl = new URL(propsLocation + ".properties"); //$NON-NLS-1$

            in = new BufferedInputStream(propUrl.openStream());
            bundle = new PropertyResourceBundle(in);
        }
        catch (IOException ioe)
        {
            // we won't load the bundle then
            CheckstyleLog.log(ioe);
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }

        return bundle;
    }

    /**
     * Support for http authentication.
     * 
     * @author Lars Ködderitzsch
     */
    protected static class RemoteConfigAuthenticator extends Authenticator
    {
        /** Map to store the authentication info for this session. */
        private static Map sAuthInfoSessionCache = Collections.synchronizedMap(new HashMap());

        /** The check configuration. */
        private ICheckConfiguration mConfiguration;

        /**
         * Creates the authenticator.
         * 
         * @param checkConfiguration the check configuration
         */
        public RemoteConfigAuthenticator(ICheckConfiguration checkConfiguration)
        {
            mConfiguration = checkConfiguration;
        }

        /**
         * Removes the authentication info from the session cache.
         * 
         * @param checkConfiguration the check configuration
         */
        public static void removeCachedAuthInfo(ICheckConfiguration checkConfiguration)
            throws CheckstylePluginException
        {
            sAuthInfoSessionCache.remove(checkConfiguration.getLocation());
            try
            {
                if (checkConfiguration.getLocation() != null)
                {
                    Platform.flushAuthorizationInfo(checkConfiguration.getType().resolveLocation(
                            checkConfiguration), "", ""); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            catch (CoreException e)
            {
                CheckstylePluginException.rethrow(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        protected PasswordAuthentication getPasswordAuthentication()
        {

            PasswordAuthentication auth = null;

            try
            {

                // check if already authenticated in this session
                auth = (PasswordAuthentication) sAuthInfoSessionCache.get(mConfiguration
                        .getLocation());

                // load from internal keyring
                if (auth == null)
                {

                    URL url = new URL(mConfiguration.getLocation());

                    Map authInfo = Platform.getAuthorizationInfo(url, "", ""); //$NON-NLS-1$ //$NON-NLS-2$

                    if (authInfo == null || authInfo.get(KEY_PASSWORD) == null)
                    {

                        // Display display = Display.getDefault();
                        Shell shell = new Shell((Display) null);
                        AuthenticationDialog authDialog = new AuthenticationDialog(shell,
                                mConfiguration.getLocation(), authInfo != null ? (String) authInfo
                                        .get(KEY_USERNAME) : null);

                        if (Dialog.OK == authDialog.open())
                        {
                            authInfo = new HashMap();

                            authInfo.put(KEY_USERNAME, authDialog.getUsername());

                            if (authDialog.savePassword())
                            {
                                authInfo.put(KEY_PASSWORD, authDialog.getPassword());
                            }

                            // store authorization info to the internal key ring
                            Platform.addAuthorizationInfo(url, "", "", authInfo); //$NON-NLS-1$ //$NON-NLS-2$

                            authInfo.put(KEY_PASSWORD, authDialog.getPassword());

                            auth = new PasswordAuthentication((String) authInfo.get(KEY_USERNAME),
                                    ((String) authInfo.get(KEY_PASSWORD)).toCharArray());
                        }
                        else
                        {
                            auth = null;
                        }
                    }
                    else
                    {
                        auth = new PasswordAuthentication((String) authInfo.get(KEY_USERNAME),
                                ((String) authInfo.get(KEY_PASSWORD)).toCharArray());
                    }
                }

                // put into cache
                if (auth != null)
                {
                    sAuthInfoSessionCache.put(mConfiguration.getLocation(), auth);
                }
            }
            catch (MalformedURLException e)
            {
                CheckstyleLog.log(e);
            }
            catch (CoreException e)
            {
                CheckstyleLog.log(e);
            }
            return auth;
        }

        /**
         * The dialog to input authentication.
         * 
         * @author Lars Ködderitzsch
         */
        private class AuthenticationDialog extends TitleAreaDialog
        {

            private Text mTxtUserName;

            private Text mTxtPassword;

            private Button mChkSavePassword;

            private String mRemoteURL;

            private String mUsername;

            private String mPassword;

            private boolean mSavePassword;

            /**
             * Creates the authentication dialog.
             * 
             * @param parentShell the shell
             */
            protected AuthenticationDialog(Shell parentShell, String remoteURL, String userName)
            {
                super(parentShell);
                mRemoteURL = remoteURL;
                mUsername = userName;
            }

            /**
             * {@inheritDoc}
             */
            protected Control createDialogArea(Composite parent)
            {

                Composite composite = (Composite) super.createDialogArea(parent);

                Composite mainConposite = new Composite(composite, SWT.NULL);
                mainConposite.setLayoutData(new GridData(GridData.FILL_BOTH));
                mainConposite.setLayout(new GridLayout(2, false));

                Label lblUserName = new Label(mainConposite, SWT.NULL);
                lblUserName.setText(Messages.RemoteConfigurationType_lblUserName);
                lblUserName.setLayoutData(new GridData());

                mTxtUserName = new Text(mainConposite, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
                mTxtUserName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                if (mUsername != null)
                {
                    mTxtUserName.setText(mUsername);
                }

                Label lblPassword = new Label(mainConposite, SWT.NULL);
                lblPassword.setText(Messages.RemoteConfigurationType_lblPassword);
                lblPassword.setLayoutData(new GridData());

                mTxtPassword = new Text(mainConposite, SWT.LEFT | SWT.PASSWORD | SWT.BORDER);
                mTxtPassword.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

                mChkSavePassword = new Button(mainConposite, SWT.CHECK);
                mChkSavePassword.setText(Messages.RemoteConfigurationType_btnSavePassword);
                GridData gd = new GridData(GridData.FILL_HORIZONTAL);
                gd.horizontalSpan = 2;
                mChkSavePassword.setLayoutData(gd);

                this.setTitle(Messages.RemoteConfigurationType_titleRemoteAuth);
                this.setMessage(NLS
                        .bind(Messages.RemoteConfigurationType_msgRemoteAuth, mRemoteURL));
                // this.setTitleImage(CheckstylePluginImages
                // .getImage(CheckstylePluginImages.PLUGIN_LOGO));
                return mainConposite;
            }

            /**
             * {@inheritDoc}
             */
            protected void okPressed()
            {

                mUsername = mTxtUserName.getText();
                mPassword = mTxtPassword.getText();
                mSavePassword = mChkSavePassword.getSelection();

                super.okPressed();
            }

            /**
             * {@inheritDoc}
             */
            protected void configureShell(Shell newShell)
            {
                newShell.setText(Messages.RemoteConfigurationType_titleAuthentication);
                super.configureShell(newShell);
            }

            public String getUsername()
            {
                return mUsername;
            }

            public String getPassword()
            {
                return mPassword;
            }

            public boolean savePassword()
            {
                return mSavePassword;
            }
        }
    }
}