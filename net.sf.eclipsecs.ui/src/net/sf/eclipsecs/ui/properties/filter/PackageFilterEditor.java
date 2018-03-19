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

package net.sf.eclipsecs.ui.properties.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.eclipsecs.core.projectconfig.filters.PackageFilter;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.Messages;

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
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Editor dialog for the package filter.
 *
 * @author Lars Ködderitzsch
 */
public class PackageFilterEditor implements IFilterEditor {

  /** the dialog for this editor. */
  private CheckedTreeSelectionDialog mDialog;

  /** the input for the editor. */
  private IProject mInputProject;

  /** the filter data. */
  private List<String> mFilterData;

  /**
   * {@inheritDoc}
   */
  @Override
  public int openEditor(Shell parent) {

    this.mDialog = new CheckedTreeSelectionDialog(parent,
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

  /**
   * {@inheritDoc}
   */
  @Override
  public void setInputProject(IProject input) {
    this.mInputProject = input;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setFilterData(List<String> filterData) {
    this.mFilterData = filterData;
  }

  /**
   * {@inheritDoc}
   */
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

        String el = mFilterData.get(i);

        if (PackageFilter.RECURSE_OFF_MARKER.equals(el)) {
          recurse = false;
          continue;
        }

        IPath path = new Path(el);

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
   * @author Lars Ködderitzsch
   */
  private class SourceFolderContentProvider implements ITreeContentProvider {

    /**
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    @Override
    public Object[] getChildren(Object parentElement) {
      List<IResource> children = null;

      if (parentElement instanceof IProject) {

        IProject project = (IProject) parentElement;
        children = handleProject(project);
      } else if (parentElement instanceof IContainer) {

        IContainer container = (IContainer) parentElement;
        children = handleContainer(container);
      } else {
        children = new ArrayList<>();
      }

      return children.toArray();
    }

    private List<IResource> handleProject(IProject project) {
      List<IResource> children = new ArrayList<>();

      if (project.isAccessible()) {

        try {

          IJavaProject javaProject = JavaCore.create(project);
          if (javaProject.exists()) {

            IPackageFragmentRoot[] packageRoots = javaProject.getAllPackageFragmentRoots();

            for (int i = 0, size = packageRoots.length; i < size; i++) {

              // special case - project itself is package root
              if (project.equals(packageRoots[i].getResource())) {

                IResource[] members = project.members();
                for (int j = 0; j < members.length; j++) {
                  if (members[j].getType() != IResource.FILE) {
                    children.add(members[j]);
                  }
                }
              } else if (!packageRoots[i].isArchive()
                      && packageRoots[i].getParent().equals(javaProject)) {
                children.add(packageRoots[i].getResource());
              }
            }
          }
        } catch (JavaModelException e) {
          CheckstyleLog.log(e);
        } catch (CoreException e) {
          // this should never happen because we call
          // #isAccessible before invoking #members
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
        } catch (CoreException e) {
          // this should never happen because we call
          // #isAccessible before invoking #members
        }
      }
      return children;
    }

    /**
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    @Override
    public Object getParent(Object element) {
      return element instanceof IResource ? ((IResource) element).getParent() : null;
    }

    /**
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    @Override
    public boolean hasChildren(Object element) {
      return getChildren(element).length > 0;
    }

    /**
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    @Override
    public Object[] getElements(Object inputElement) {
      return getChildren(inputElement);
    }

    /**
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    @Override
    public void dispose() {
      // NOOP
    }

    /**
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
     */
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
  public class CheckedTreeSelectionDialog extends SelectionStatusDialog {
    private CheckboxTreeViewer mViewer;

    private final ILabelProvider mLabelProvider;

    private final ITreeContentProvider mContentProvider;

    private Button mBtnRecurseSubPackages;

    private Object mInput;

    private boolean mIsEmpty;

    private int mWidth = 60;

    private int mHeight = 18;

    private Object[] mExpandedElements;

    private boolean mRecursivelyExcludeSubPackages = true;

    /**
     * Constructs an instance of <code>ElementTreeSelectionDialog</code>.
     *
     * @param parent
     *          The shell to parent from.
     * @param labelProvider
     *          the label provider to render the entries
     * @param contentProvider
     *          the content provider to evaluate the tree structure
     */
    public CheckedTreeSelectionDialog(Shell parent, ILabelProvider labelProvider,
            ITreeContentProvider contentProvider) {
      super(parent);
      mLabelProvider = labelProvider;
      mContentProvider = contentProvider;
      setResult(new ArrayList<>(0));
      setStatusLineAboveButtons(true);
      mExpandedElements = null;
      int shellStyle = getShellStyle();
      setShellStyle(shellStyle | SWT.MAX | SWT.RESIZE);
    }

    /**
     * Sets the initial selection. Convenience method.
     *
     * @param selection
     *          the initial selection.
     */
    public void setInitialSelection(Object selection) {
      setInitialSelections(new Object[] { selection });
    }

    /**
     * Sets the tree input.
     *
     * @param input
     *          the tree input.
     */
    public void setInput(Object input) {
      mInput = input;
    }

    /**
     * Expands elements in the tree.
     *
     * @param elements
     *          The elements that will be expanded.
     */
    public void setExpandedElements(Object[] elements) {
      mExpandedElements = elements;
    }

    /**
     * Sets the size of the tree in unit of characters.
     *
     * @param width
     *          the width of the tree.
     * @param height
     *          the height of the tree.
     */
    public void setSize(int width, int height) {
      mWidth = width;
      mHeight = height;
    }

    /**
     * Sets if subtree should be recursively excluded. Default is true.
     *
     * @param recursivelyExcludeSubTree
     *          the recursive checking state
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

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.window.Window#open()
     */
    @Override
    public int open() {
      mIsEmpty = evaluateIfTreeEmpty(mInput);
      super.open();
      return getReturnCode();
    }

    /**
     * Handles cancel button pressed event.
     */
    @Override
    protected void cancelPressed() {
      setResult(null);
      super.cancelPressed();
    }

    /*
     * @see SelectionStatusDialog#computeResult()
     */
    @Override
    protected void computeResult() {

      List<Object> checked = Arrays.asList(mViewer.getCheckedElements());

      if (!mRecursivelyExcludeSubPackages) {
        setResult(checked);
      } else {

        List<Object> grayed = Arrays.asList(mViewer.getGrayedElements());

        List<Object> pureChecked = new ArrayList<>(checked);
        pureChecked.removeAll(grayed);

        setResult(pureChecked);
      }

    }

    @Override
    protected Control createButtonBar(Composite parent) {

      Composite composite = new Composite(parent, SWT.NONE);
      GridLayout layout = new GridLayout(3, false);
      layout.marginHeight = 0;
      layout.marginWidth = 0;
      composite.setLayout(layout);
      composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      mBtnRecurseSubPackages = new Button(composite, SWT.CHECK);
      mBtnRecurseSubPackages.setText("Recursively exclude sub-packages");
      GridData gd = new GridData();
      gd.horizontalAlignment = GridData.BEGINNING;
      gd.horizontalIndent = 5;
      mBtnRecurseSubPackages.setLayoutData(gd);

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

      Control buttonBar = super.createButtonBar(composite);
      gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.horizontalAlignment = GridData.END;
      buttonBar.setLayoutData(gd);

      return composite;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets .Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
      Composite composite = (Composite) super.createDialogArea(parent);
      Label messageLabel = createMessageArea(composite);
      CheckboxTreeViewer treeViewer = createTreeViewer(composite);

      GridData data = new GridData(GridData.FILL_BOTH);
      data.widthHint = convertWidthInCharsToPixels(mWidth);
      data.heightHint = convertHeightInCharsToPixels(mHeight);
      Tree treeWidget = treeViewer.getTree();
      treeWidget.setLayoutData(data);
      treeWidget.setFont(parent.getFont());
      if (mIsEmpty) {
        messageLabel.setEnabled(false);
        treeWidget.setEnabled(false);
      }
      return composite;
    }

    /**
     * Creates the tree viewer.
     *
     * @param parent
     *          the parent composite
     * @return the tree viewer
     */
    protected CheckboxTreeViewer createTreeViewer(Composite parent) {

      mViewer = new CheckboxTreeViewer(parent, SWT.BORDER);
      mViewer.setContentProvider(mContentProvider);
      mViewer.setLabelProvider(mLabelProvider);

      mViewer.addCheckStateListener(new ICheckStateListener() {
        @Override
        public void checkStateChanged(CheckStateChangedEvent event) {

          IContainer element = (IContainer) event.getElement();

          if (isRecursivelyExcludeSubTree() && !isGrayed(element)) {
            setSubElementsGrayedChecked(element, event.getChecked());
          } else if (isRecursivelyExcludeSubTree() && isGrayed(element)) {
            mViewer.setGrayChecked(element, true);
          }
        }
      });

      mViewer.setInput(mInput);
      mViewer.setCheckedElements(getInitialElementSelections().toArray());
      adaptRecurseBehaviour();
      if (mExpandedElements != null) {
        mViewer.setExpandedElements(mExpandedElements);
      }

      return mViewer;
    }

    private boolean evaluateIfTreeEmpty(Object input) {
      Object[] elements = mContentProvider.getElements(input);

      return elements.length == 0;
    }

    private void adaptRecurseBehaviour() {

      if (isRecursivelyExcludeSubTree()) {

        Object[] checked = mViewer.getCheckedElements();
        for (Object element : checked) {
          setSubElementsGrayedChecked((IContainer) element, true);
        }
      } else {
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

    private void setSubElementsGrayedChecked(final IContainer container, final boolean checked) {

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
      } catch (CoreException e) {
        CheckstyleUIPlugin.errorDialog(getShell(), e, true);
      }

      for (IContainer grayedChild : subContainers) {
        mViewer.setGrayChecked(grayedChild, checked);
      }
    }
  }

}
