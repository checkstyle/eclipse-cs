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

package com.atlassw.tools.eclipse.checkstyle.projectconfig.filters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.atlassw.tools.eclipse.checkstyle.Messages;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;

/**
 * Editor dialog for the package filter.
 * 
 * @author Lars Ködderitzsch
 */
public class PackageFilterEditor implements IFilterEditor
{

    //
    // constants
    //

    //
    // attributes
    //

    /** the dialog for this editor. */
    private CheckedTreeSelectionDialog mDialog;

    /** the input for the editor. */
    private IProject mInputProject;

    /** the filter data. */
    private List mFilterData;

    //
    // methods
    //

    /**
     * {@inheritDoc}
     */
    public int openEditor(Shell parent)
    {

        this.mDialog = new CheckedTreeSelectionDialog(parent, WorkbenchLabelProvider
                .getDecoratingWorkbenchLabelProvider(), new SourceFolderContentProvider());

        // initialize the dialog with the filter data
        initCheckedTreeSelectionDialog();

        // open the dialog
        int retCode = this.mDialog.open();

        // actualize the filter data
        if (Window.OK == retCode)
        {
            this.mFilterData = this.getFilterDataFromDialog();
        }

        return retCode;
    }

    /**
     * {@inheritDoc}
     */
    public void setInputProject(IProject input)
    {
        this.mInputProject = input;
    }

    /**
     * {@inheritDoc}
     */
    public void setFilterData(List filterData)
    {
        this.mFilterData = filterData;
    }

    /**
     * {@inheritDoc}
     */
    public List getFilterData()
    {
        return this.mFilterData;
    }

    /**
     * Helper method to initialize the dialog.
     */
    private void initCheckedTreeSelectionDialog()
    {

        this.mDialog.setTitle(Messages.PackageFilterEditor_titleFilterPackages);
        this.mDialog.setMessage(Messages.PackageFilterEditor_msgFilterPackages);
        this.mDialog.setBlockOnOpen(true);

        this.mDialog.setInput(this.mInputProject);

        // display the filter data
        if (this.mInputProject != null && this.mFilterData != null)
        {

            List selectedElements = new ArrayList();
            List expandedElements = new ArrayList();

            int size = mFilterData != null ? mFilterData.size() : 0;
            for (int i = 0; i < size; i++)
            {

                IPath path = new Path((String) mFilterData.get(i));

                IResource selElement = this.mInputProject.findMember(path);
                if (selElement != null)
                {
                    selectedElements.add(selElement);
                }

                // get all parent elements to expand
                while (path.segmentCount() > 0)
                {
                    path = path.removeLastSegments(1);

                    IResource expElement = this.mInputProject.findMember(path);
                    if (expElement != null)
                    {
                        expandedElements.add(expElement);
                    }
                }
            }

            this.mDialog.setInitialSelections(selectedElements.toArray());
            this.mDialog.setExpandedElements(expandedElements.toArray());
        }
    }

    /**
     * Helper method to extract the edited data from the dialog.
     * 
     * @return the filter data
     */
    private List getFilterDataFromDialog()
    {

        Object[] checked = this.mDialog.getResult();

        List result = new ArrayList();
        for (int i = 0; i < checked.length; i++)
        {

            if (checked[i] instanceof IResource)
            {
                result.add(((IResource) checked[i]).getProjectRelativePath().toString());
            }
        }
        return result;
    }

    /**
     * Content provider that provides the source folders of a project and their
     * container members.
     * 
     * @author Lars Ködderitzsch
     */
    private class SourceFolderContentProvider implements ITreeContentProvider
    {

        /**
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
         */
        public Object[] getChildren(Object parentElement)
        {
            List children = null;

            if (parentElement instanceof IProject)
            {

                IProject project = (IProject) parentElement;
                children = handleProject(project);
            }
            else if (parentElement instanceof IContainer)
            {

                IContainer container = (IContainer) parentElement;
                children = handleContainer(container);
            }
            else
            {
                children = new ArrayList();
            }

            return children.toArray();
        }

        private List handleProject(IProject project)
        {
            List children = new ArrayList();

            if (project.isAccessible())
            {

                try
                {

                    IJavaProject javaProject = JavaCore.create(project);
                    if (javaProject.exists())
                    {

                        IPackageFragmentRoot[] packageRoots = javaProject
                                .getAllPackageFragmentRoots();

                        for (int i = 0, size = packageRoots.length; i < size; i++)
                        {

                            // special case - project itself is package root
                            if (project.equals(packageRoots[i].getResource()))
                            {

                                IResource[] members = project.members();
                                for (int j = 0; j < members.length; j++)
                                {
                                    if (members[j].getType() != IResource.FILE)
                                    {
                                        children.add(members[j]);
                                    }
                                }
                            }
                            else if (!packageRoots[i].isArchive()
                                    && packageRoots[i].getParent().equals(javaProject))
                            {
                                children.add(packageRoots[i].getResource());
                            }
                        }
                    }
                }
                catch (JavaModelException e)
                {
                    CheckstyleLog.log(e);
                }
                catch (CoreException e)
                {
                    // this should never happen because we call
                    // #isAccessible before invoking #members
                }
            }
            return children;
        }

        private List handleContainer(IContainer container)
        {
            List children;
            children = new ArrayList();
            if (container.isAccessible())
            {
                try
                {
                    IResource[] members = container.members();
                    for (int i = 0; i < members.length; i++)
                    {
                        if (members[i].getType() != IResource.FILE)
                        {
                            children.add(members[i]);
                        }
                    }
                }
                catch (CoreException e)
                {
                    // this should never happen because we call
                    // #isAccessible before invoking #members
                }
            }
            return children;
        }

        /**
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
         */
        public Object getParent(Object element)
        {
            return element instanceof IResource ? ((IResource) element).getParent() : null;
        }

        /**
         * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
         */
        public boolean hasChildren(Object element)
        {
            return getChildren(element).length > 0;
        }

        /**
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        public Object[] getElements(Object inputElement)
        {
            return getChildren(inputElement);
        }

        /**
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose()
        {
        // NOOP
        }

        /**
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
         *      java.lang.Object, java.lang.Object)
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
        {
        // NOOP
        }
    }
}