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

package net.sf.eclipsecs.core.nature;

import java.util.ArrayList;
import java.util.List;

import net.sf.eclipsecs.core.CheckstylePlugin;
import net.sf.eclipsecs.core.builder.CheckstyleBuilder;
import net.sf.eclipsecs.core.builder.CheckstyleMarker;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;

/**
 * Checkstyle project nature.
 */
public class CheckstyleNature implements IProjectNature {

  /** ID for the Checkstyle project nature. */
  public static final String NATURE_ID = CheckstylePlugin.PLUGIN_ID + ".CheckstyleNature"; //$NON-NLS-1$

  /** The project. */
  private IProject mProject;

  /**
   * {@inheritDoc}
   */
  @Override
  public void configure() throws CoreException {

    //
    // Add the builder to the project.
    //
    IProjectDescription description = mProject.getDescription();
    ICommand[] commands = description.getBuildSpec();
    boolean found = false;
    for (int i = 0; i < commands.length; ++i) {
      if (commands[i].getBuilderName().equals(CheckstyleBuilder.BUILDER_ID)) {
        found = true;
        break;
      }
    }

    if (!found) {
      // add builder to project
      ICommand command = description.newCommand();
      command.setBuilderName(CheckstyleBuilder.BUILDER_ID);
      ICommand[] newCommands = new ICommand[commands.length + 1];

      // Add it after the other builders.
      System.arraycopy(commands, 0, newCommands, 0, commands.length);
      newCommands[commands.length] = command;
      description.setBuildSpec(newCommands);

      ensureProjectFileWritable();

      mProject.setDescription(description, null);
    }
  }

  private void ensureProjectFileWritable() throws CoreException {
    IFile projectFile = mProject.getFile(".project");
    if (projectFile.isReadOnly()) {
      ResourceAttributes attrs = ResourceAttributes.fromFile(projectFile.getFullPath().toFile());
      attrs.setReadOnly(true);
      projectFile.setResourceAttributes(attrs);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deconfigure() throws CoreException {

    //
    // Remove the builder from the project.
    //
    IProjectDescription description = mProject.getDescription();
    ICommand[] commands = description.getBuildSpec();
    List<ICommand> newCommandsVec = new ArrayList<>();
    for (int i = 0; i < commands.length; ++i) {
      if (commands[i].getBuilderName().equals(CheckstyleBuilder.BUILDER_ID)) {
        continue;
      } else {
        newCommandsVec.add(commands[i]);
      }
    }

    ICommand[] newCommands = newCommandsVec.toArray(new ICommand[newCommandsVec.size()]);
    description.setBuildSpec(newCommands);

    ensureProjectFileWritable();

    mProject.setDescription(description, new NullProgressMonitor());

    // remove checkstyle markers from the project
    getProject().deleteMarkers(CheckstyleMarker.MARKER_ID, true, IResource.DEPTH_INFINITE);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IProject getProject() {
    return mProject;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setProject(IProject project) {
    mProject = project;
  }

  /**
   * Checks if the ordering of the builders of the given project is correct, more specifically if
   * the CheckstyleBuilder is set to run after the JavaBuilder.
   *
   * @param project
   *          the project to check
   * @return <code>true</code> if the builder order for this project is correct, <code>false</code>
   *         otherwise
   * @throws CoreException
   *           error getting project description
   */
  public static boolean hasCorrectBuilderOrder(IProject project) throws CoreException {
    IProjectDescription description = project.getDescription();
    ICommand[] commands = description.getBuildSpec();

    int javaBuilderIndex = -1;
    int checkstyleBuilderIndex = -1;

    for (int i = 0; i < commands.length; i++) {

      if (commands[i].getBuilderName().equals(CheckstyleBuilder.BUILDER_ID)) {
        checkstyleBuilderIndex = i;
      } else if (commands[i].getBuilderName().equals(JavaCore.BUILDER_ID)) {
        javaBuilderIndex = i;
      }
    }
    return javaBuilderIndex < checkstyleBuilderIndex;
  }

}
