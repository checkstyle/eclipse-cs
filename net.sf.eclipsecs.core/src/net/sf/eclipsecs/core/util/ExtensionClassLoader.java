//============================================================================
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
//============================================================================

package net.sf.eclipsecs.core.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * Classloader implementation which can load classes and resources from bundles implementing a
 * specific extension point.
 *
 */
public class ExtensionClassLoader extends ClassLoader {

    /** The bundles contributing to this classloader. */
    private List<Bundle> mBundles;

    /**
     * Creates the extension classloader.
     *
     * @param sourceBundle
     *            the source bundle defining the extension
     * @param extensionPointId
     *            the extension point id
     */
    public ExtensionClassLoader(Bundle sourceBundle, String extensionPointId) {

        mBundles = new ArrayList<>();

        mBundles.add(sourceBundle);

        final IExtensionRegistry pluginRegistry = Platform.getExtensionRegistry();
        final IExtensionPoint extPt = pluginRegistry.getExtensionPoint(extensionPointId);

        final IExtension[] extensions = extPt.getExtensions();

        for (IExtension ext : extensions) {
            final String contributorId = ext.getContributor().getName();
            final Bundle extensionBundle = Platform.getBundle(contributorId);

            if (extensionBundle != null) {
                mBundles.add(extensionBundle);
            }
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {

        Class<?> clazz = null;

        for (Bundle bundle : mBundles) {

            try {
                clazz = bundle.loadClass(name);
                if (clazz != null) {
                    break;
                }
            } catch (ClassNotFoundException ex) {
                // try next
            }
        }

        if (clazz == null) {
            throw new ClassNotFoundException(name);
        }

        return clazz;
    }

    @Override
    public URL getResource(String name) {

        URL resource = null;

        for (Bundle bundle : mBundles) {
            resource = bundle.getResource(name);
            if (resource != null) {
                break;
            }
        }
        return resource;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {

        final List<URL> resources = new ArrayList<>();

        for (Bundle bundle : mBundles) {
            final Enumeration<URL> bundleResources = bundle.getResources(name);
            if (bundleResources != null) {
                resources.addAll(Collections.list(bundleResources));
            }
        }
        return Collections.enumeration(resources);

    }
}
