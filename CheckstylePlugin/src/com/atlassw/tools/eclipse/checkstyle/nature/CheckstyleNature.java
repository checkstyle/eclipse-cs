//============================================================================
//
// Copyright (C) 2002-2004  David Schneider
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

package com.atlassw.tools.eclipse.checkstyle.nature;

//=================================================
// Imports from java namespace
//=================================================
import java.util.Vector;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.builder.CheckstyleBuilder;
import com.atlassw.tools.eclipse.checkstyle.builder.CheckstyleMarker;

/**
 * Checkstyle project nature.
 */
public class CheckstyleNature implements IProjectNature
{
    // =================================================
    // Public static final variables.
    // =================================================

    /** ID for the Checkstyle project nature. */
    public static final String NATURE_ID = CheckstylePlugin.PLUGIN_ID + ".CheckstyleNature"; //$NON-NLS-1$

    // =================================================
    // Static class variables.
    // =================================================

    // =================================================
    // Instance member variables.
    // =================================================

    /** The project. */
    private IProject mProject;

    // =================================================
    // Constructors & finalizer.
    // =================================================

    // =================================================
    // Methods.
    // =================================================

    /**
     * {@inheritDoc}
     */
    public void configure() throws CoreException
    {

        //
        // Add the builder to the project.
        //
        IProjectDescription description = mProject.getDescription();
        ICommand[] commands = description.getBuildSpec();
        boolean found = false;
        for (int i = 0; i < commands.length; ++i)
        {
            if (commands[i].getBuilderName().equals(CheckstyleBuilder.BUILDER_ID))
            {
                found = true;
                break;
            }
        }

        if (!found)
        {
            // add builder to project
            ICommand command = description.newCommand();
            command.setBuilderName(CheckstyleBuilder.BUILDER_ID);
            ICommand[] newCommands = new ICommand[commands.length + 1];

            // Add it before other builders.
            System.arraycopy(commands, 0, newCommands, 0, commands.length);
            newCommands[commands.length] = command;
            description.setBuildSpec(newCommands);
            mProject.setDescription(description, null);
        }
    }

    /**
     * Remove the nature from the project.
     * 
     * @see org.eclipse.core.resources.IProjectNature#deconfigure()
     */
    public void deconfigure() throws CoreException
    {

        //
        // Remove the builder from the project.
        //
        IProjectDescription description = mProject.getDescription();
        ICommand[] commands = description.getBuildSpec();
        Vector newCommandsVec = new Vector(0);
        for (int i = 0; i < commands.length; ++i)
        {
            if (commands[i].getBuilderName().equals(CheckstyleBuilder.BUILDER_ID))
            {
                continue;
            }
            else
            {
                newCommandsVec.add(commands[i]);
            }
        }

        ICommand[] newCommands = new ICommand[newCommandsVec.size()];
        for (int i = 0; i < newCommandsVec.size(); i++)
        {
            newCommands[i] = (ICommand) newCommandsVec.elementAt(i);
        }
        description.setBuildSpec(newCommands);
        mProject.setDescription(description, new NullProgressMonitor());

        // remove checkstyle markers from the project
        getProject().deleteMarkers(CheckstyleMarker.MARKER_ID, true, IResource.DEPTH_INFINITE);

    }

    /**
     * @see org.eclipse.core.resources.IProjectNature#getProject()
     */
    public IProject getProject()
    {
        return mProject;
    }

    /**
     * @see org.eclipse.core.resources.IProjectNature#setProject(IProject)
     */
    public void setProject(IProject project)
    {
        mProject = project;
    }

}