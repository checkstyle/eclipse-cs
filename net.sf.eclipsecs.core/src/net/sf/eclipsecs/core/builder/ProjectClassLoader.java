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

package net.sf.eclipsecs.core.builder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;

import net.sf.eclipsecs.core.Messages;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.util.NLS;

/**
 * ClassLoader to make the contents of a eclipse project accessible for the style checking process.
 * <br/>
 * After construction the classloader can be initialized with a eclipse project. Reinitialization
 * and reuse of the classloader with another project is possible.
 *
 * @author Lars Ködderitzsch
 */
public class ProjectClassLoader extends ClassLoader {

  /** the classloader delegate. */
  private ClassLoader mDelegateClassLoader;

  /** the parent classloader. */
  private final ClassLoader mParentClassLoader;

  /**
   * the URLStreamHandlerFactory to provide support for non standard protocols.
   */
  private final URLStreamHandlerFactory mStreamHandlerFactory;

  /**
   * Constructs the classloader.
   */
  public ProjectClassLoader() {
    this(null);
  }

  /**
   * Constructs the classloader and uses a parent classloader.
   *
   * @param parent
   *          the parent classloader
   */
  public ProjectClassLoader(ClassLoader parent) {
    this(parent, null);
  }

  /**
   * Constructs the classloader and uses a parent classloader and a handler to support non-standard
   * protocols.
   *
   * @param parent
   *          the parent classloader
   * @param factory
   *          the streamhandler factory
   */
  public ProjectClassLoader(ClassLoader parent, URLStreamHandlerFactory factory) {

    this.mParentClassLoader = parent;
    this.mStreamHandlerFactory = factory;
  }

  /**
   * Initializes this classloader with a given eclipse project.
   *
   * @param project
   *          the project
   */
  public void intializeWithProject(IProject project) {

    // Optimization if the project is the same as last
    // if (project == this.mRecentProject)
    // {
    // return;
    // }

    URL[] projClassPath = getProjectClassPath(project);

    // // log the complete classpath to track down these pesky
    // // NoClassDefFound-Errors
    // StringBuffer buf = new StringBuffer();
    // buf.append("Checkstyle Classpath for
    // project\"").append(project.getName()).append("\":");
    // for (int i = 0; i < projClassPath.length; i++)
    // {
    // buf.append("\n").append(projClassPath[i].toExternalForm());
    // }
    // IStatus status = new Status(IStatus.INFO, CheckstylePlugin.PLUGIN_ID,
    // IStatus.OK, buf
    // .toString(), null);
    // CheckstylePlugin.getDefault().getLog().log(status);

    this.mDelegateClassLoader = new URLClassLoader(projClassPath, this.mParentClassLoader,
            this.mStreamHandlerFactory);
  }

  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    return this.mDelegateClassLoader.loadClass(name);
  }

  @Override
  public URL getResource(String name) {
    return this.mDelegateClassLoader.getResource(name);
  }

  @Override
  public InputStream getResourceAsStream(String name) {
    return this.mDelegateClassLoader.getResourceAsStream(name);
  }

  /**
   * Since java.lang.ClassLoader#getResources(java.lang.String) is final (why?) this method is
   * overridden as a workaround.
   *
   * @see java.lang.ClassLoader#findResources(java.lang.String)
   */
  @Override
  protected Enumeration<URL> findResources(String name) throws IOException {
    return this.mDelegateClassLoader.getResources(name);
  }

  /**
   * Gets the complete classpath for a given project.
   *
   * @param project
   *          the project
   * @return the classpath
   */
  private static URL[] getProjectClassPath(IProject project) {

    // List to contain the classpath urls
    List<URL> cpURLs = new ArrayList<>();

    // add the projects contents to the classpath
    addToClassPath(project, cpURLs, false, new HashSet<IProject>());

    URL[] urls = cpURLs.toArray(new URL[cpURLs.size()]);

    return urls;
  }

  /**
   * Adds the contents of a project to list of URLs.
   *
   * @param project
   *          the project
   * @param cpURLs
   *          the resulting list
   * @param isReferenced
   *          true if a referenced project is processed
   */
  private static void addToClassPath(IProject project, List<URL> cpURLs, boolean isReferenced,
          Collection<IProject> processedProjects) {

    try {

      // this project has already been added
      if (processedProjects.contains(project)) {
        return;
      } else {
        processedProjects.add(project);
      }

      // get the java project
      IJavaProject javaProject = JavaCore.create(project);

      // get the resolved classpath of the project
      IClasspathEntry[] cpEntries = javaProject.getResolvedClasspath(true);

      // iterate over classpath to create classpath urls
      int size = cpEntries.length;
      for (int i = 0; i < size; i++) {

        int entryKind = cpEntries[i].getEntryKind();

        // handle a source path
        if (IClasspathEntry.CPE_SOURCE == entryKind) {
          handleSourcePath(project, cpURLs, cpEntries[i], javaProject);
        } else if (IClasspathEntry.CPE_PROJECT == entryKind) {
          // handle a project reference
          handleRefProject(cpURLs, cpEntries[i], processedProjects);
        } else if (IClasspathEntry.CPE_LIBRARY == entryKind) {
          // handle a library entry
          handleLibrary(project, cpURLs, cpEntries[i]);
        } else { // cannot happen since we use a resolved classpath

          // log as exception
          CheckstylePluginException ex = new CheckstylePluginException(
                  NLS.bind(Messages.errorUnknownClasspathEntry, cpEntries[i].getPath()));
          CheckstyleLog.log(ex);
        }
      }
    } catch (JavaModelException jme) {
      CheckstyleLog.log(jme);
    }
  }

  /**
   * Helper method to handle a source path.
   *
   * @param project
   *          the original project
   * @param cpURLs
   *          the list that is to contain the projects classpath
   * @param entry
   *          the actually processed classpath entry
   * @param javapProject
   *          the java project
   * @throws JavaModelException
   *           an exception with the java project occured
   */
  private static void handleSourcePath(IProject project, List<URL> cpURLs, IClasspathEntry entry,
          IJavaProject javapProject) throws JavaModelException {

    IPath sourcePath = entry.getPath();

    // check for if the output path is different to the source path
    IPath outputPath = entry.getOutputLocation();

    if (outputPath == null) {
      sourcePath = javapProject.getOutputLocation();
    } else if (!outputPath.equals(sourcePath)) {

      // make the output path the relevant path since it contains the
      // class files
      sourcePath = outputPath;
    }

    // check if the sourcepath is relative to the project
    IPath projPath = project.getFullPath();

    if (!projPath.equals(sourcePath) && sourcePath.matchingFirstSegments(projPath) > 0) {

      // remove the project part from the source path
      sourcePath = sourcePath.removeFirstSegments(projPath.segmentCount());

      // get the folder for the path
      IFolder sourceFolder = project.getFolder(sourcePath);

      // get the absolute path for the folder
      sourcePath = sourceFolder.getLocation();
    } else if (projPath.equals(sourcePath)) {
      sourcePath = project.getLocation();
    }

    // try to add the path to the classpath
    handlePath(sourcePath, cpURLs);
  }

  /**
   * Helper method to handle a referenced project for the classpath.
   *
   * @param cpURLs
   *          the list that is to contain the projects classpath
   * @param entry
   *          the actually processed classpath entry
   */
  private static void handleRefProject(List<URL> cpURLs, IClasspathEntry entry,
          Collection<IProject> processedProjects) {

    // get the referenced project from the workspace
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IProject referencedProject = root.getProject(entry.getPath().toString());

    // add the referenced projects contents
    if (referencedProject.exists()) {
      addToClassPath(referencedProject, cpURLs, true, processedProjects);
    }
  }

  /**
   * Helper method to handle a library for the classpath.
   *
   * @param project
   *          the original project
   * @param cpURLs
   *          the list that is to contain the projects classpath
   * @param entry
   *          the actually processed classpath entry
   */
  private static void handleLibrary(IProject project, List<URL> cpURLs, IClasspathEntry entry) {

    IPath libPath = entry.getPath();

    // check if the library path is relative to the project
    // can happen if the library is contained within the project
    IPath projPath = project.getFullPath();
    if (libPath.matchingFirstSegments(projPath) > 0) {

      // remove the project part from the source path
      libPath = libPath.removeFirstSegments(projPath.segmentCount());

      // fixes 1422937 - Thanks to Peter Hendriks
      if (!libPath.isEmpty()) { // added check

        // get the file handle for the library
        IFile file = project.getFile(libPath);

        // get the absolute path for the library file
        libPath = file.getLocation();

      } else {
        // fallback to project root when libPath is empty
        libPath = project.getLocation();
      }
    } else {
      // Check if the resource is otherwise relative to the workspace
      IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(libPath);
      if (resource != null && resource.exists()) {
        libPath = resource.getLocation();
      }
    }

    // try to add the path to the classpath
    handlePath(libPath, cpURLs);
  }

  /**
   * Helper method to handle an absolute path for the classpath.
   *
   * @param absolutePath
   *          the absolute path
   * @param cpURLs
   *          the list that is to contain the projects classpath
   */
  private static void handlePath(IPath absolutePath, List<URL> cpURLs) {

    if (absolutePath != null) {

      File file = absolutePath.toFile();

      // check if the file exists
      if (file != null && file.exists()) {

        try {

          URL url = file.toURI().toURL();
          if (!cpURLs.contains(url)) {
            cpURLs.add(url);
          }
        } catch (MalformedURLException mfe) {
          // log the exception although this should not happen
          CheckstyleLog.log(mfe, mfe.getLocalizedMessage());
        }
      }
    }
  }
}
