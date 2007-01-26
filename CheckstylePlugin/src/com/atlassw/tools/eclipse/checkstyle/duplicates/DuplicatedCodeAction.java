//============================================================================
//
// Copyright (C) 2002-2007  David Schneider, Lars Ködderitzsch, Fabrice Bellingard
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

package com.atlassw.tools.eclipse.checkstyle.duplicates;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.checks.duplicates.StrictDuplicateCodeCheck;

/**
 * Action that launches the duplicated code check on a project.
 * 
 * @author Fabrice BELLINGARD
 */

public class DuplicatedCodeAction implements IObjectActionDelegate
{

    /** The project that is currently selected. */
    private ISelection mCurrentSelection;

    /** The workbench part. */
    private IWorkbenchPart mWorkbenchPart;

    /**
     * Constructor.
     */
    public DuplicatedCodeAction()
    {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart)
    {
        mWorkbenchPart = targetPart;
    }

    /**
     * {@inheritDoc}
     */
    public void run(IAction action)
    {
        if (mCurrentSelection == null)
        {
            return;
        }

        final Set filesToScan = computeFileSetToScan();

        final Checker checker = createChecker();
        if (checker == null)
        {
            return;
        }

        Job job = new Job("Searching for duplicated code...")
        {
            /**
             * Cf. method below.
             * 
             * @see org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime.IProgressMonitor)
             */
            protected IStatus run(IProgressMonitor monitor)
            {
                File[] files = new File[filesToScan.size()];
                filesToScan.toArray(files);
                checker.process(files);
                checker.destroy();
                return Status.OK_STATUS;
            }
        };
        job.setPriority(Job.LONG);
        job.schedule();
    }

    /**
     * Creates a new initalized checker.
     * 
     * @return the checker if successfully created, or NULL otherwise
     */
    private Checker createChecker()
    {
        Checker checker = null;

        Preferences prefs = CheckstylePlugin.getDefault().getPluginPreferences();
        int minimumNumberOfLines = prefs.getInt(CheckstylePlugin.PREF_DUPLICATED_CODE_MIN_LINES);

        StrictDuplicateCodeCheck check = new StrictDuplicateCodeCheck();
        check.setMin(minimumNumberOfLines);

        DuplicatedCodeView duplicatedCodeView = findDuplicatedCodeView();
        // if null, we can't do anything
        if (duplicatedCodeView != null)
        {
            try
            {
                checker = new Checker();
                checker.setBasedir(ResourcesPlugin.getWorkspace().getRoot().getLocation()
                        .toString());
                checker.addFileSetCheck(check);
                // checker.addListener(new
                // DuplicatedCodeAuditListener(duplicatedCodeView));
            }
            catch (CheckstyleException e)
            {
                CheckstyleLog.errorDialog(mWorkbenchPart.getSite().getShell(),
                        "Unable to launch the duplicated code analyser.", e, true);
            }
        }

        return checker;
    }

    /**
     * Tries to retrive the duplicated code view.
     * 
     * @return the view if it was found, NULL otherwise
     */
    private DuplicatedCodeView findDuplicatedCodeView()
    {
        DuplicatedCodeView duplicatedCodeView = null;
        try
        {
            duplicatedCodeView = (DuplicatedCodeView) PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getActivePage()
                    .showView(DuplicatedCodeView.VIEW_ID);
        }
        catch (PartInitException e)
        {
            CheckstyleLog.errorDialog(mWorkbenchPart.getSite().getShell(),
                    "Error opening the duplicated code view '" + DuplicatedCodeView.VIEW_ID + "'.",
                    e, true);
        }
        return duplicatedCodeView;
    }

    /**
     * Computes the file set to scan for duplicated code.
     * 
     * @return the file set, a collection of java.io.File objects.
     */
    private Set computeFileSetToScan()
    {
        Set filesToScan = new HashSet();
        StructuredSelection selection = (StructuredSelection) mCurrentSelection;
        for (Iterator iter = selection.iterator(); iter.hasNext();)
        {
            IResource resource = (IResource) iter.next();
            if (resource instanceof IFile && ((IFile) resource).getFileExtension().equals("java"))
            {
                filesToScan.add(new File(resource.getLocation().toString()));
            }
            else if (resource instanceof IContainer)
            {
                addJavaFilesToSet(filesToScan, (IContainer) resource);
            }
        }
        return filesToScan;
    }

    /**
     * Adds to the set all the java files found in the container.
     * 
     * @param filesToScan : the set
     * @param container : the container to scan
     */
    private void addJavaFilesToSet(Set filesToScan, IContainer container)
    {
        try
        {
            IResource[] resources = container.members(true);
            for (int i = 0; i < resources.length; i++)
            {
                IResource resource = resources[i];
                if (resource instanceof IFile
                        && ((IFile) resource).getFileExtension().equals("java"))
                {
                    filesToScan.add(new File(resource.getLocation().toString()));
                }
                else if (resource instanceof IContainer)
                {
                    addJavaFilesToSet(filesToScan, (IContainer) resource);
                }
            }
        }
        catch (CoreException e)
        {
            // we can't do anything : just log the pbm...
            CheckstyleLog.log(e, "Error while scanning files for the duplication code analysis.");
        }

    }

    /**
     * {@inheritDoc}
     */
    public void selectionChanged(IAction action, ISelection selection)
    {
        mCurrentSelection = selection;
    }

}