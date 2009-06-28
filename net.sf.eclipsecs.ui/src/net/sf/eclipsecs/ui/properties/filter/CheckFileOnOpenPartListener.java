//============================================================================
//
// Copyright (C) 2002-2009  David Schneider, Lars Ködderitzsch
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

package net.sf.eclipsecs.ui.properties.filter;

import java.util.List;

import net.sf.eclipsecs.core.builder.CheckstyleMarker;
import net.sf.eclipsecs.core.jobs.RunCheckstyleOnFilesJob;
import net.sf.eclipsecs.core.nature.CheckstyleNature;
import net.sf.eclipsecs.core.projectconfig.IProjectConfiguration;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationFactory;
import net.sf.eclipsecs.core.projectconfig.filters.IFilter;
import net.sf.eclipsecs.core.projectconfig.filters.UnOpenedFilesFilter;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.FileEditorInput;

/**
 * PartListener implementation that listenes for opening editor parts and runs
 * Checkstyle on the opened file if the UnOpenedFileFilter is active.
 * 
 * @see https://sourceforge.net/tracker/index.php?func=detail&aid=1647245&group_id=80344&atid=559497).
 * @author Lars Ködderitzsch
 */
public class CheckFileOnOpenPartListener implements IPartListener2 {

    /**
     * {@inheritDoc}
     */
    public void partOpened(IWorkbenchPartReference partRef) {

        IFile editorFile = getEditorFile(partRef);
        if (editorFile != null) {
            UnOpenedFilesFilter.addOpenedFile(editorFile);
        }

        // check if the opened part is a editor
        // and the editors file need to be checked
        if (editorFile != null && isFileAffected(editorFile)) {
            RunCheckstyleOnFilesJob job = new RunCheckstyleOnFilesJob(editorFile);
            job.schedule();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void partClosed(IWorkbenchPartReference partRef) {

        IFile editorFile = getEditorFile(partRef);
        if (editorFile != null) {
            UnOpenedFilesFilter.removeOpenedFile(editorFile);
        }

        // if the UnOpenedFilesFilter is active and the editor closes
        // the markers of the current file need to be removed
        if (editorFile != null && isFileAffected(editorFile)) {
            try {
                editorFile.deleteMarkers(CheckstyleMarker.MARKER_ID, true, IFile.DEPTH_INFINITE);
            }
            catch (CoreException e) {
                CheckstyleLog.log(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void partActivated(IWorkbenchPartReference partRef) {
    // NOOP
    }

    /**
     * {@inheritDoc}
     */
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
    // NOOP
    }

    /**
     * {@inheritDoc}
     */
    public void partDeactivated(IWorkbenchPartReference partRef) {
    // NOOP
    }

    /**
     * {@inheritDoc}
     */
    public void partHidden(IWorkbenchPartReference partRef) {
    // NOOP
    }

    /**
     * {@inheritDoc}
     */
    public void partInputChanged(IWorkbenchPartReference partRef) {
    // NOOP
    }

    /**
     * {@inheritDoc}
     */
    public void partVisible(IWorkbenchPartReference partRef) {
    // NOOP
    }

    /**
     * Returns the file behind the referenced workbench part.
     * 
     * @param partRef the workbench part in question
     * @return the editors file or <code>null</code> if the workbench part is
     *         no file based editor
     */
    private IFile getEditorFile(IWorkbenchPartReference partRef) {

        IFile file = null;
        IWorkbenchPart part = partRef.getPart(true);

        if (part instanceof IEditorPart) {

            IEditorPart editor = (IEditorPart) part;

            if (editor.getEditorInput() instanceof FileEditorInput) {

                file = ((FileEditorInput) editor.getEditorInput()).getFile();
            }
        }
        return file;
    }

    /**
     * Checks if the given file is affected by the UnOpenedFilesFilter and needs
     * to be handled on editor open/close.
     * 
     * @param file the file to check
     * @return <code>true</code> if the file is affected, <code>false</code>
     *         otherwise
     */
    private boolean isFileAffected(IFile file) {

        boolean affected = false;

        IProject project = file.getProject();

        try {
            // check if checkstyle is enabled on the project
            if (project.isAccessible() && project.hasNature(CheckstyleNature.NATURE_ID)) {

                IProjectConfiguration config = ProjectConfigurationFactory
                        .getConfiguration(project);

                // now check if the UnOpenedFilesFilter is active
                boolean unOpenedFilesFilterActive = false;
                boolean filtered = false;
                List<IFilter> filters = config.getFilters();
                for (IFilter filter : filters) {

                    if (filter instanceof UnOpenedFilesFilter
                            && ((UnOpenedFilesFilter) filter).isEnabled()) {
                        unOpenedFilesFilterActive = true;
                    }

                    // check if the file would be filtered out
                    if (filter.isEnabled() && !(filter instanceof UnOpenedFilesFilter)) {
                        filtered = filtered || !filter.accept(file);
                    }
                }

                affected = unOpenedFilesFilterActive && !filtered;
            }
        }
        catch (CoreException e) {
            // should never happen, since editor cannot be open
            // when project isn't
            CheckstyleLog.log(e);
        }
        catch (CheckstylePluginException e) {
            CheckstyleLog.log(e);
        }

        return affected;
    }
}
