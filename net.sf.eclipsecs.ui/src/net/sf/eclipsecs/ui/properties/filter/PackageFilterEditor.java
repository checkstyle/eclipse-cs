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

package net.sf.eclipsecs.ui.properties.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import net.sf.eclipsecs.core.projectconfig.filters.PackageFilter;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.Messages;

/**
 * Editor dialog for the package filter.
 *
 */
public class PackageFilterEditor implements IFilterEditor {

    /** The dialog for this editor. */
    private PackageCheckedTreeSelectionDialog mDialog;

    /** The input for the editor. */
    private IProject mInputProject;

    /** The filter data. */
    private List<String> mFilterData;

    @Override
    public int openEditor(Shell parent) {

        this.mDialog = new PackageCheckedTreeSelectionDialog(parent,
            WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider(),
            new SourceFolderContentProvider());

        // initialize the dialog with the filter data
        initCheckedTreeSelectionDialog();

        // open the dialog
        int retCode = this.mDialog.open();

        // actualize the filter data
        if (Window.OK == retCode) {
            this.mFilterData = this.getFilterDataFromDialog();

            if (!mDialog.isRecursivelyExcludeSubTree()) {
                mFilterData.add(PackageFilter.RECURSE_OFF_MARKER);
            }
        }

        return retCode;
    }

    @Override
    public void setInputProject(IProject input) {
        this.mInputProject = input;
    }

    @Override
    public void setFilterData(List<String> filterData) {
        this.mFilterData = filterData;
    }

    @Override
    public List<String> getFilterData() {
        return this.mFilterData;
    }

    /**
     * Helper method to initialize the dialog.
     */
    private void initCheckedTreeSelectionDialog() {

        this.mDialog.setTitle(Messages.PackageFilterEditor_titleFilterPackages);
        this.mDialog.setMessage(Messages.PackageFilterEditor_msgFilterPackages);
        this.mDialog.setBlockOnOpen(true);

        this.mDialog.setInput(this.mInputProject);

        // display the filter data
        if (this.mInputProject != null && this.mFilterData != null) {

            List<IResource> selectedElements = new ArrayList<>();
            List<IResource> expandedElements = new ArrayList<>();

            boolean recurse = true;

            int size = mFilterData != null ? mFilterData.size() : 0;
            for (int i = 0; i < size; i++) {

                String element = mFilterData.get(i);

                if (PackageFilter.RECURSE_OFF_MARKER.equals(element)) {
                    recurse = false;
                    continue;
                }

                IPath path = new Path(element);

                IResource selElement = this.mInputProject.findMember(path);
                if (selElement != null) {
                    selectedElements.add(selElement);
                }

                // get all parent elements to expand
                while (path.segmentCount() > 0) {
                    path = path.removeLastSegments(1);

                    IResource expElement = this.mInputProject.findMember(path);
                    if (expElement != null) {
                        expandedElements.add(expElement);
                    }
                }
            }

            this.mDialog.setInitialSelections(selectedElements.toArray());
            this.mDialog.setExpandedElements(expandedElements.toArray());
            this.mDialog.setRecursivelyExcludeSubTree(recurse);
        }
    }

    /**
     * Helper method to extract the edited data from the dialog.
     *
     * @return the filter data
     */
    private List<String> getFilterDataFromDialog() {

        Object[] checked = this.mDialog.getResult();

        List<String> result = new ArrayList<>();
        for (int i = 0; i < checked.length; i++) {

            if (checked[i] instanceof IResource) {
                result.add(((IResource) checked[i]).getProjectRelativePath().toString());
            }
        }
        return result;
    }

    /**
     * Content provider that provides the source folders of a project and their container members.
     *
     */
    private static final class SourceFolderContentProvider implements ITreeContentProvider {

        @Override
        public Object[] getChildren(Object parentElement) {
            List<IResource> children = null;

            if (parentElement instanceof IProject) {

                IProject project = (IProject) parentElement;
                children = handleProject(project);
            }
            else if (parentElement instanceof IContainer) {

                IContainer container = (IContainer) parentElement;
                children = handleContainer(container);
            }
            else {
                children = new ArrayList<>();
            }

            return children.toArray();
        }

        private List<IResource> handleProject(IProject project) {
            List<IResource> children = new ArrayList<>();
            if (project.isAccessible()) {
                IJavaProject javaProject = JavaCore.create(project);
                if (javaProject.exists()) {
                    try {
                        IPackageFragmentRoot[] packageRoots =
                            javaProject.getAllPackageFragmentRoots();
                        for (IPackageFragmentRoot packageRoot : packageRoots) {
                            // special case - project itself is package root
                            if (project.equals(packageRoot.getResource())) {
                                Arrays.stream(project.members())
                                    .filter(member -> member.getType() != IResource.FILE)
                                    .forEach(children::add);
                            }
                            else if (!packageRoot.isArchive()
                                && packageRoot.getParent().equals(javaProject)) {
                                children.add(packageRoot.getResource());
                            }
                        }

                    }
                    catch (JavaModelException ex) {
                        CheckstyleLog.log(ex);
                    }
                    catch (CoreException ex) {
                        // this should never happen because we call
                        // #isAccessible before invoking #members
                    }
                }
            }
            return children;
        }

        private List<IResource> handleContainer(IContainer container) {
            List<IResource> children = new ArrayList<>();
            if (container.isAccessible()) {
                try {
                    IResource[] members = container.members();
                    for (int i = 0; i < members.length; i++) {
                        if (members[i].getType() != IResource.FILE) {
                            children.add(members[i]);
                        }
                    }
                }
                catch (CoreException ex) {
                    // this should never happen because we call
                    // #isAccessible before invoking #members
                }
            }
            return children;
        }

        @Override
        public Object getParent(Object element) {
            return element instanceof IResource ? ((IResource) element).getParent() : null;
        }

        @Override
        public boolean hasChildren(Object element) {
            return getChildren(element).length > 0;
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }

        @Override
        public void dispose() {
            // NOOP
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // NOOP
        }
    }

    /**
     * A class to select elements out of a tree structure.
     *
     * @since 2.0
     */
    public class PackageCheckedTreeSelectionDialog extends CheckedTreeSelectionDialog {

        /** The checkbox tree viewer. */
        private CheckboxTreeViewer mViewer;

        /** The recurse sub-packages checkbox. */
        private Button mBtnRecurseSubPackages;

        /** Flag for recursive exclusion of sub-packages. */
        private boolean mRecursivelyExcludeSubPackages = true;

        /**
         * Constructs an instance of <code>ElementTreeSelectionDialog</code>.
         *
         * @param parent
         *            The shell to parent from.
         * @param labelProvider
         *            the label provider to render the entries
         * @param contentProvider
         *            the content provider to evaluate the tree structure
         */
        public PackageCheckedTreeSelectionDialog(Shell parent, ILabelProvider labelProvider,
            ITreeContentProvider contentProvider) {
            super(parent, labelProvider, contentProvider);
            setHelpAvailable(false);
            setStatusLineAboveButtons(true);
            setShellStyle(getShellStyle() | SWT.MAX | SWT.RESIZE);
        }

        /**
         * Sets if subtree should be recursively excluded. Default is true.
         *
         * @param recursivelyExcludeSubTree
         *            the recursive checking state
         */
        public void setRecursivelyExcludeSubTree(boolean recursivelyExcludeSubTree) {

            mRecursivelyExcludeSubPackages = recursivelyExcludeSubTree;
        }

        /**
         * Returns if the subtrees should be recursively excluded.
         *
         * @return <code>true</code> if subtrees should be excluded
         */
        protected boolean isRecursivelyExcludeSubTree() {
            return mRecursivelyExcludeSubPackages;
        }

        @Override
        protected CheckboxTreeViewer createTreeViewer(Composite parent) {

            mViewer = super.createTreeViewer(parent);

            mViewer.addCheckStateListener(event -> {
                final IContainer element = (IContainer) event.getElement();

                if (isRecursivelyExcludeSubTree() && !isGrayed(element)) {
                    setSubElementsGrayedChecked(element, event.getChecked());
                }
                else if (isRecursivelyExcludeSubTree() && isGrayed(element)) {
                    mViewer.setGrayChecked(element, true);
                }
            });

            adaptRecurseBehaviour();

            return mViewer;
        }

        @Override
        protected void computeResult() {

            List<Object> checked = Arrays.asList(mViewer.getCheckedElements());

            if (mRecursivelyExcludeSubPackages) {
                List<Object> grayed = Arrays.asList(mViewer.getGrayedElements());

                List<Object> pureChecked = new ArrayList<>(checked);
                pureChecked.removeAll(grayed);

                setResult(pureChecked);
            }
            else {
                setResult(checked);
            }

        }

        @Override
        protected Control createButtonBar(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);

            mBtnRecurseSubPackages = new Button(composite, SWT.CHECK);
            mBtnRecurseSubPackages.setText("Recursively exclude sub-packages");
            GridDataFactory.fillDefaults()
                .align(SWT.BEGINNING, SWT.CENTER)
                .indent(convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN),
                    convertHorizontalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN) * 2)
                .applyTo(mBtnRecurseSubPackages);

            mBtnRecurseSubPackages.setSelection(mRecursivelyExcludeSubPackages);
            mBtnRecurseSubPackages.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    mRecursivelyExcludeSubPackages = mBtnRecurseSubPackages.getSelection();
                    adaptRecurseBehaviour();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    // NOOP
                }
            });

            final Composite buttonBar = (Composite) super.createButtonBar(composite);
            GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER)
                .grab(true, false)
                .applyTo(buttonBar);

            return composite;
        }

        private void adaptRecurseBehaviour() {

            if (isRecursivelyExcludeSubTree()) {

                Object[] checked = mViewer.getCheckedElements();
                for (Object element : checked) {
                    setSubElementsGrayedChecked((IContainer) element, true);
                }
            }
            else {
                Object[] grayed = mViewer.getGrayedElements();
                for (Object element : grayed) {
                    mViewer.setGrayChecked(element, false);
                }
            }
        }

        private boolean isGrayed(Object element) {

            Object[] grayed = mViewer.getGrayedElements();
            return Arrays.asList(grayed).contains(element);
        }

        private void setSubElementsGrayedChecked(final IContainer container,
            final boolean checked) {

            final List<IContainer> subContainers = new ArrayList<>();

            try {
                container.accept(new IResourceVisitor() {
                    @Override
                    public boolean visit(IResource resource) {
                        if (!resource.equals(container) && resource instanceof IContainer) {
                            subContainers.add((IContainer) resource);
                        }
                        return true;
                    }
                });
            }
            catch (CoreException ex) {
                CheckstyleUIPlugin.errorDialog(getShell(), ex, true);
            }

            for (IContainer grayedChild : subContainers) {
                mViewer.setGrayChecked(grayedChild, checked);
            }
        }
    }

}
