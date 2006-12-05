////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2005  Oliver Burn
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
////////////////////////////////////////////////////////////////////////////////

package com.atlassw.tools.eclipse.checkstyle.builder;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.puppycrawl.tools.checkstyle.api.AbstractLoader;

/**
 * Loads a list of package names from a package name XML file.
 * 
 * @author Rick Giles
 * @version 4-Dec-2002
 */
public final class PackageNamesLoader extends AbstractLoader
{
    /** the public ID for the configuration dtd. */
    private static final String DTD_PUBLIC_ID = "-//Puppy Crawl//DTD Package Names 1.0//EN"; //$NON-NLS-1$

    /** the resource for the configuration dtd. */
    private static final String DTD_RESOURCE_NAME = "com/puppycrawl/tools/checkstyle/packages_1_0.dtd"; //$NON-NLS-1$

    /**
     * Name of default checkstyle package names resource file. The file must be
     * in the classpath.
     */
    private static final String DEFAULT_PACKAGES = "com/puppycrawl/tools/checkstyle/checkstyle_packages.xml"; //$NON-NLS-1$

    /** The loaded package names. */
    private Stack mPackageStack = new Stack();

    private static List sPackages;

    /**
     * Creates a new <code>PackageNamesLoader</code> instance.
     * 
     * @throws ParserConfigurationException if an error occurs
     * @throws SAXException if an error occurs
     */
    private PackageNamesLoader() throws ParserConfigurationException, SAXException
    {
        super(DTD_PUBLIC_ID, DTD_RESOURCE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    public void startElement(String aNamespaceURI, String aLocalName, String aQName,
            Attributes aAtts) throws SAXException
    {
        if (aQName.equals("package")) //$NON-NLS-1$
        {
            // push package name
            final String name = aAtts.getValue("name"); //$NON-NLS-1$
            if (name == null)
            {
                throw new SAXException("missing package name"); //$NON-NLS-1$
            }
            mPackageStack.push(name);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void endElement(String aNamespaceURI, String aLocalName, String aQName)
    {
        if (aQName.equals("package")) //$NON-NLS-1$
        {

            String packageName = getPackageName();
            if (!sPackages.contains(packageName))
            {
                sPackages.add(packageName);
            }
            mPackageStack.pop();
        }
    }

    /**
     * Creates a full package name from the package names on the stack.
     * 
     * @return the full name of the current package.
     */
    private String getPackageName()
    {
        final StringBuffer buf = new StringBuffer();
        final Iterator it = mPackageStack.iterator();
        while (it.hasNext())
        {
            final String subPackage = (String) it.next();
            buf.append(subPackage);
            if (!subPackage.endsWith(".")) //$NON-NLS-1$
            {
                buf.append("."); //$NON-NLS-1$
            }
        }
        return buf.toString();
    }

    /**
     * Returns the default list of package names.
     * 
     * @param aClassLoader the class loader that gets the default package names.
     * @return the default list of package names.
     * @throws CheckstylePluginException if an error occurs.
     */
    public static List getPackageNames(ClassLoader aClassLoader) throws CheckstylePluginException
    {

        if (sPackages == null)
        {
            sPackages = new ArrayList();

            PackageNamesLoader nameLoader = null;

            try
            {
                nameLoader = new PackageNamesLoader();
                final InputStream stream = aClassLoader.getResourceAsStream(DEFAULT_PACKAGES);
                InputSource source = new InputSource(stream);
                nameLoader.parseInputSource(source);
            }
            catch (ParserConfigurationException e)
            {
                CheckstylePluginException.rethrow(e, "unable to parse " + DEFAULT_PACKAGES); //$NON-NLS-1$
            }
            catch (SAXException e)
            {
                CheckstylePluginException.rethrow(e, "unable to parse " + DEFAULT_PACKAGES + " - " //$NON-NLS-1$ //$NON-NLS-2$
                        + e.getMessage());
            }
            catch (IOException e)
            {
                CheckstylePluginException.rethrow(e, "unable to read " + DEFAULT_PACKAGES); //$NON-NLS-1$
            }

            // load custom package files
            try
            {
                Enumeration packageFiles = aClassLoader.getResources("checkstyle_packages.xml"); //$NON-NLS-1$

                while (packageFiles.hasMoreElements())
                {

                    URL aPackageFile = (URL) packageFiles.nextElement();
                    InputStream iStream = null;

                    try
                    {

                        iStream = new BufferedInputStream(aPackageFile.openStream());
                        InputSource source = new InputSource(iStream);
                        nameLoader.parseInputSource(source);
                    }
                    catch (SAXException e)
                    {
                        CheckstyleLog.log(e, "unable to parse " + aPackageFile.toExternalForm() //$NON-NLS-1$
                                + " - " + e.getLocalizedMessage()); //$NON-NLS-1$
                    }
                    catch (IOException e)
                    {
                        CheckstyleLog.log(e, "unable to read " + aPackageFile.toExternalForm()); //$NON-NLS-1$
                    }
                    finally
                    {
                        IOUtils.closeQuietly(iStream);
                    }
                }
            }
            catch (IOException e1)
            {
                CheckstylePluginException.rethrow(e1);
            }
        }

        return sPackages;
    }

    /**
     * Refreshes the cached package names.
     */
    public static void refresh()
    {
        sPackages = null;
    }
}