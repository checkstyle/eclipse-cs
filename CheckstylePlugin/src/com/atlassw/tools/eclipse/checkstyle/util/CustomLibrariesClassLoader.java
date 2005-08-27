
package com.atlassw.tools.eclipse.checkstyle.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;

/**
 * This class constructs a classloader that is able to load from .jar archives
 * located in the extension-libraries folder inside the plugin directory.
 * 
 * @author Lars Ködderitzsch
 */
public final class CustomLibrariesClassLoader
{
    /** the internal classloader. */
    private static ClassLoader sLibrariesClassLoader;

    //
    // constructors
    //

    /** Hidden default constructor. */
    private CustomLibrariesClassLoader()
    {
    // NOOP
    }

    //
    // methods
    //

    /**
     * Invalidates the classloader. It will be reconstructed the next time it is
     * requested.
     */
    public static synchronized void invalidate()
    {
        sLibrariesClassLoader = null;
    }

    /**
     * Returns the classloader used to load custom libraries.
     * 
     * @return the classloader
     * @throws CheckstylePluginException when accessing the content of the
     *             extension-libraries directory
     */
    public static synchronized ClassLoader get() throws CheckstylePluginException
    {
        if (sLibrariesClassLoader == null)
        {
            try
            {
                build();
            }
            catch (IOException e)
            {
                CheckstylePluginException.rethrow(e);
            }
        }
        return sLibrariesClassLoader;
    }

    /**
     * Builds the classloader used to load custom libraries.
     */
    private static void build() throws IOException
    {

        ClassLoader contextClassloader = CustomLibrariesClassLoader.class.getClassLoader();

        URL[] libraryURLs = getCustomLibraryURLs();
        sLibrariesClassLoader = new URLClassLoader(libraryURLs, contextClassloader);
    }

    /**
     * Locates the jar files inside the extension-libraries folder.
     * 
     * @return the URLs to the extension libaries
     * @throws IOException error accession the extension-libraries folder
     */
    private static URL[] getCustomLibraryURLs() throws IOException
    {
        URL extensionLibsURL = CheckstylePlugin.getDefault().find(new Path("extension-libraries"));
        extensionLibsURL = Platform.resolve(extensionLibsURL);

        File extensionLibsDir = new File(extensionLibsURL.getFile());

        File[] jarFiles = extensionLibsDir.listFiles(new JarFileFilter());

        URL[] jarURLs = new URL[jarFiles.length];
        for (int i = 0; i < jarFiles.length; i++)
        {
            jarURLs[i] = jarFiles[i].toURL();
        }

        return jarURLs;
    }

    /**
     * File filter that accepts .jar files.
     * 
     * @author Lars Ködderitzsch
     */
    private static class JarFileFilter implements FileFilter
    {

        /**
         * @see java.io.FileFilter#accept(java.io.File)
         */
        public boolean accept(File pathname)
        {
            return pathname.getAbsolutePath().endsWith(".jar");
        }

    }
}
