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
 * @author Lars Ködderitzsch
 */
public class ExtensionClassLoader extends ClassLoader {

  private List<Bundle> mBundles;

  /**
   * Creates the extension classloader.
   *
   * @param sourceBundle
   *          the source bundle defining the extension
   * @param extensionPointId
   *          the extension point id
   */
  public ExtensionClassLoader(Bundle sourceBundle, String extensionPointId) {

    mBundles = new ArrayList<>();

    mBundles.add(sourceBundle);

    IExtensionRegistry pluginRegistry = Platform.getExtensionRegistry();
    IExtensionPoint extPt = pluginRegistry.getExtensionPoint(extensionPointId);

    IExtension[] extensions = extPt.getExtensions();

    for (IExtension ext : extensions) {
      String contributorId = ext.getContributor().getName();
      Bundle extensionBundle = Platform.getBundle(contributorId);

      if (extensionBundle != null) {
        mBundles.add(extensionBundle);
      }
    }
  }

  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException {

    Class<?> cl = null;

    for (Bundle bundle : mBundles) {

      try {
        cl = bundle.loadClass(name);
        if (cl != null) {
          break;
        }
      } catch (ClassNotFoundException e) {
        // try next
      }
    }

    if (cl == null) {
      throw new ClassNotFoundException(name);
    }

    return cl;
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

    List<URL> resources = new ArrayList<>();

    for (Bundle bundle : mBundles) {
      Enumeration<URL> bundleResources = bundle.getResources(name);
      if (bundleResources != null) {
        resources.addAll(Collections.list(bundleResources));
      }
    }
    return Collections.enumeration(resources);

  }
}
