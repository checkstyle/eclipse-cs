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

package net.sf.eclipsecs.ui.properties;

import java.util.LinkedList;
import java.util.List;

import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.projectconfig.FileMatchPattern;
import net.sf.eclipsecs.core.projectconfig.FileSet;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationConfigureDialog;
import net.sf.eclipsecs.ui.config.CheckConfigurationLabelProvider;
import net.sf.eclipsecs.ui.config.CheckConfigurationViewerSorter;
import net.sf.eclipsecs.ui.util.SWTUtil;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Property page.
 */
public class FileSetEditDialog extends TitleAreaDialog {

  private static final String DEFAULT_PATTERN = ".java$"; //$NON-NLS-1$

  private final IProject mProject;

  private Text mFileSetNameText;

  private ComboViewer mComboViewer;

  private CheckboxTableViewer mPatternViewer;

  private TableViewer mMatchesViewer;

  private Group mMatchGroup;

  private Button mConfigureButton;

  private Button mAddButton;

  private Button mEditButton;

  private Button mRemoveButton;

  private Button mUpButton;

  private Button mDownButton;

  private final Controller mController = new Controller();

  private FileSet mFileSet;

  private List<IFile> mProjectFiles;

  private boolean mIsCreatingNewFileset;

  private final CheckstylePropertyPage mPropertyPage;

  /**
   * Constructor for SamplePropertyPage.
   */
  FileSetEditDialog(Shell parent, FileSet fileSet, final IProject project,
          CheckstylePropertyPage propsPage) throws CheckstylePluginException {
    super(parent);
    setShellStyle(getShellStyle() | SWT.RESIZE);
    mProject = project;
    mFileSet = fileSet;
    mPropertyPage = propsPage;

    if (mFileSet == null) {
      mFileSet = new FileSet();
      mFileSet.getFileMatchPatterns().add(new FileMatchPattern(DEFAULT_PATTERN));
      mIsCreatingNewFileset = true;
    }

  }

  /**
   * Returns the file set edited by the dialog.
   *
   * @return the edited file set
   */
  public FileSet getFileSet() {
    return mFileSet;
  }

  /**
   * @see org.eclipse.swt.widgets.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea(Composite parent) {

    Composite composite = (Composite) super.createDialogArea(parent);
    composite.setLayoutData(new GridData(GridData.FILL_BOTH));
    Composite dialog = new Composite(composite, SWT.NONE);
    dialog.setLayout(new GridLayout(1, false));
    dialog.setLayoutData(new GridData(GridData.FILL_BOTH));

    Control commonArea = createCommonArea(dialog);
    commonArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    SashForm sashForm = new SashForm(dialog, SWT.VERTICAL);
    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.widthHint = 500;
    gd.heightHint = 400;
    sashForm.setLayoutData(gd);
    sashForm.setLayout(new GridLayout());

    Control patternArea = createFileMatchPatternPart(sashForm);
    patternArea.setLayoutData(new GridData(GridData.FILL_BOTH));

    Control matchArea = createTestArea(sashForm);
    matchArea.setLayoutData(new GridData(GridData.FILL_BOTH));

    sashForm.setWeights(new int[] { 50, 50 });

    // init the data
    initializeControls();

    return composite;
  }

  private Control createCommonArea(Composite parent) {

    Composite composite = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout(2, false);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    composite.setLayout(layout);

    Label nameLabel = new Label(composite, SWT.NULL);
    nameLabel.setText(Messages.FileSetEditDialog_lblName);

    mFileSetNameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
    mFileSetNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Label lblConfiguration = new Label(composite, SWT.NULL);
    lblConfiguration.setText(Messages.FileSetEditDialog_lblCheckConfig);

    final Composite comboComposite = new Composite(composite, SWT.NONE);
    layout = new GridLayout(2, false);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    comboComposite.setLayout(layout);
    comboComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    mComboViewer = new ComboViewer(comboComposite);
    mComboViewer.getCombo().setVisibleItemCount(10);
    mComboViewer.setContentProvider(new CheckConfigurationContentProvider());
    mComboViewer.setLabelProvider(new CheckConfigurationLabelProvider());
    mComboViewer.setComparator(new CheckConfigurationViewerSorter());
    mComboViewer.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    mComboViewer.addSelectionChangedListener(mController);

    mConfigureButton = new Button(comboComposite, SWT.PUSH);
    mConfigureButton.setText(Messages.FileSetEditDialog_btnConfigure);
    mConfigureButton.addSelectionListener(mController);
    mConfigureButton.setLayoutData(new GridData());

    return composite;
  }

  private Control createFileMatchPatternPart(Composite parent) {

    Group composite = new Group(parent, SWT.NONE);
    composite.setText(Messages.FileSetEditDialog_titlePatternsTable);
    composite.setLayout(new FormLayout());

    final Composite buttons = new Composite(composite, SWT.NULL);
    FormData fd = new FormData();
    fd.top = new FormAttachment(0, 3);
    fd.right = new FormAttachment(100, -3);
    fd.bottom = new FormAttachment(100, -3);

    buttons.setLayoutData(fd);
    GridLayout layout = new GridLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    buttons.setLayout(layout);

    final Table table = new Table(composite, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION);
    fd = new FormData();
    fd.left = new FormAttachment(0, 3);
    fd.top = new FormAttachment(0, 3);
    fd.right = new FormAttachment(buttons, -3, SWT.LEFT);
    fd.bottom = new FormAttachment(100, -3);
    table.setLayoutData(fd);

    TableLayout tableLayout = new TableLayout();
    table.setLayout(tableLayout);
    table.setHeaderVisible(true);
    table.setLinesVisible(true);

    TableColumn column1 = new TableColumn(table, SWT.NONE);
    column1.setText(Messages.FileSetEditDialog_colInclude);

    tableLayout.addColumnData(new ColumnWeightData(11));

    TableColumn column2 = new TableColumn(table, SWT.NONE);
    column2.setText(Messages.FileSetEditDialog_colRegex);
    tableLayout.addColumnData(new ColumnWeightData(89));

    mPatternViewer = new CheckboxTableViewer(table);

    mPatternViewer.setLabelProvider(new FileMatchPatternLabelProvider());
    mPatternViewer.setContentProvider(new ArrayContentProvider());
    mPatternViewer.addDoubleClickListener(mController);
    mPatternViewer.addCheckStateListener(mController);

    //
    // Build the buttons.
    //

    mAddButton = createPushButton(buttons, Messages.FileSetEditDialog_btnAdd);
    mAddButton.addSelectionListener(mController);

    mEditButton = createPushButton(buttons, Messages.FileSetEditDialog_btnEdit);
    mEditButton.addSelectionListener(mController);

    mRemoveButton = createPushButton(buttons, Messages.FileSetEditDialog_btnRemove);
    mRemoveButton.addSelectionListener(mController);

    mUpButton = createPushButton(buttons, Messages.FileSetEditDialog_btnUp);
    mUpButton.addSelectionListener(mController);

    mDownButton = createPushButton(buttons, Messages.FileSetEditDialog_btnDown);
    mDownButton.addSelectionListener(mController);

    return composite;
  }

  private Control createTestArea(Composite parent) {

    mMatchGroup = new Group(parent, SWT.NONE);
    mMatchGroup.setLayout(new GridLayout(1, false));

    mMatchesViewer = new TableViewer(mMatchGroup);
    mMatchesViewer.setContentProvider(new ArrayContentProvider());
    mMatchesViewer.setLabelProvider(new LabelProvider() {

      private final WorkbenchLabelProvider mDelegate = new WorkbenchLabelProvider();

      /**
       * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
       */
      @Override
      public String getText(Object element) {
        String text = ""; //$NON-NLS-1$
        if (element instanceof IFile) {
          text = ((IFile) element).getProjectRelativePath().toString();
        }
        return text;
      }

      /**
       * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
       */
      @Override
      public Image getImage(Object element) {
        return mDelegate.getImage(element);
      }
    });
    mMatchesViewer.addFilter(new ViewerFilter() {

      @Override
      public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (element instanceof IFile) {
          return mFileSet.includesFile((IFile) element);
        }
        return false;
      }
    });
    mMatchesViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

    return mMatchGroup;
  }

  /**
   * Initializes the controls with their data.
   */
  private void initializeControls() {

    // init the check configuration combo
    mComboViewer.setInput(mPropertyPage.getProjectConfigurationWorkingCopy());

    this.setTitleImage(CheckstyleUIPluginImages.getImage(CheckstyleUIPluginImages.PLUGIN_LOGO));
    this.setMessage(Messages.FileSetEditDialog_message);

    if (mIsCreatingNewFileset) {
      this.setTitle(Messages.FileSetEditDialog_titleCreate);
    } else {
      this.setTitle(Messages.FileSetEditDialog_titleEdit);
    }

    // intitialize the name
    mFileSetNameText.setText(mFileSet.getName() != null ? mFileSet.getName() : ""); //$NON-NLS-1$

    // init the check configuration combo
    if (mFileSet.getCheckConfig() != null) {
      mComboViewer.setSelection(new StructuredSelection(mFileSet.getCheckConfig()));
    }

    // init the pattern area
    mPatternViewer.setInput(mFileSet.getFileMatchPatterns());
    for (FileMatchPattern pattern : mFileSet.getFileMatchPatterns()) {
      mPatternViewer.setChecked(pattern, pattern.isIncludePattern());
    }

    getShell().getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {

        mMatchGroup.setText(Messages.FileSetEditDialog_msgBuildTestResults);

        try {
          mProjectFiles = getFiles(mProject);
        } catch (CoreException e) {
          CheckstyleLog.log(e);
        }

        // init the test area
        mMatchesViewer.setInput(mProjectFiles);
        updateMatchView();
      }
    });

  }

  private void updateMatchView() {
    mMatchesViewer.refresh();
    mMatchGroup.setText(NLS.bind(Messages.FileSetEditDialog_titleTestResult,
            new String[] { mProject.getName(), "" + mMatchesViewer.getTable().getItemCount(), //$NON-NLS-1$
                "" + mProjectFiles.size() })); //$NON-NLS-1$
  }

  /**
   * @see org.eclipse.jface.window.Window#create()
   */
  @Override
  public void create() {
    super.create();

    SWTUtil.addResizeSupport(this, CheckstyleUIPlugin.getDefault().getDialogSettings(),
            FileSetEditDialog.class.getName());
  }

  /**
   * Over-rides method from Window to configure the shell (e.g. the enclosing window).
   */
  @Override
  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    shell.setText(Messages.FileSetEditDialog_titleFilesetEditor);
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  @Override
  protected void okPressed() {
    //
    // Get the FileSet name.
    //
    String name = mFileSetNameText.getText();
    if ((name == null) || (name.trim().length() <= 0)) {
      this.setErrorMessage(Messages.FileSetEditDialog_msgNoFilesetName);
      return;
    }

    //
    // Get the CheckConfiguration.
    //
    if (mFileSet.getCheckConfig() == null) {
      this.setErrorMessage(Messages.FileSetEditDialog_noCheckConfigSelected);
      return;
    }

    mFileSet.setName(name);

    super.okPressed();
  }

  /**
   * Utility method that creates a push button instance and sets the default layout data.
   *
   * @param parent
   *          the parent for the new button
   * @param label
   *          the label for the new button
   * @return the newly-created button
   */
  private Button createPushButton(Composite parent, String label) {
    Button button = new Button(parent, SWT.PUSH);
    button.setText(label);
    GridData data = new GridData();
    data.horizontalAlignment = GridData.FILL;
    button.setLayoutData(data);
    return button;
  }

  private void addFileMatchPattern() {
    FileMatchPatternEditDialog dialog = new FileMatchPatternEditDialog(getShell(), null);
    if (Window.OK == dialog.open()) {

      FileMatchPattern pattern = dialog.getPattern();

      mFileSet.getFileMatchPatterns().add(pattern);
      mPatternViewer.refresh();
      mPatternViewer.setChecked(pattern, pattern.isIncludePattern());
    }
  }

  private void editFileMatchPattern() {
    IStructuredSelection selection = (IStructuredSelection) mPatternViewer.getSelection();
    FileMatchPattern pattern = (FileMatchPattern) selection.getFirstElement();
    if (pattern == null) {
      //
      // Nothing is selected.
      //
      return;
    }

    FileMatchPatternEditDialog dialog = new FileMatchPatternEditDialog(getShell(), pattern.clone());

    if (Window.OK == dialog.open()) {

      FileMatchPattern editedPattern = dialog.getPattern();
      mFileSet.getFileMatchPatterns().set(mFileSet.getFileMatchPatterns().indexOf(pattern),
              editedPattern);
      mPatternViewer.refresh();
      mPatternViewer.setChecked(editedPattern, editedPattern.isIncludePattern());
    }
  }

  private void removeFileMatchPattern() {
    IStructuredSelection selection = (IStructuredSelection) mPatternViewer.getSelection();
    FileMatchPattern pattern = (FileMatchPattern) selection.getFirstElement();
    if (pattern == null) {
      //
      // Nothing is selected.
      //
      return;
    }

    mFileSet.getFileMatchPatterns().remove(pattern);
    mPatternViewer.refresh();
  }

  private void upFileMatchPattern() {
    IStructuredSelection selection = (IStructuredSelection) mPatternViewer.getSelection();
    FileMatchPattern pattern = (FileMatchPattern) selection.getFirstElement();
    if (pattern == null) {
      //
      // Nothing is selected.
      //
      return;
    }

    int index = mFileSet.getFileMatchPatterns().indexOf(pattern);
    if (index > 0) {
      mFileSet.getFileMatchPatterns().remove(pattern);
      mFileSet.getFileMatchPatterns().add(index - 1, pattern);
      mPatternViewer.refresh();
    }
  }

  private void downFileMatchPattern() {
    IStructuredSelection selection = (IStructuredSelection) mPatternViewer.getSelection();
    FileMatchPattern pattern = (FileMatchPattern) selection.getFirstElement();
    if (pattern == null) {
      //
      // Nothing is selected.
      //
      return;
    }

    int index = mFileSet.getFileMatchPatterns().indexOf(pattern);
    if ((index >= 0) && (index < mFileSet.getFileMatchPatterns().size() - 1)) {
      mFileSet.getFileMatchPatterns().remove(pattern);
      if (index < mFileSet.getFileMatchPatterns().size() - 1) {
        mFileSet.getFileMatchPatterns().add(index + 1, pattern);
      } else {
        mFileSet.getFileMatchPatterns().add(pattern);
      }

      mPatternViewer.refresh();
    }
  }

  private List<IFile> getFiles(IContainer container) throws CoreException {
    LinkedList<IFile> files = new LinkedList<>();
    LinkedList<IFolder> folders = new LinkedList<>();

    IResource[] children = container.members();
    for (int i = 0; i < children.length; i++) {
      IResource child = children[i];
      int childType = child.getType();
      if (childType == IResource.FILE) {
        files.add((IFile) child);
      } else if (childType == IResource.FOLDER) {
        folders.add((IFolder) child);
      }
    }

    //
    // Get the files from the sub-folders.
    //
    for (IFolder folder : folders) {
      files.addAll(getFiles(folder));
    }

    return files;
  }

  /**
   * Controller of this dialog.
   *
   * @author Lars Ködderitzsch
   */
  private class Controller implements SelectionListener, IDoubleClickListener, ICheckStateListener,
          ISelectionChangedListener {

    /**
     * @see SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    @Override
    public void widgetSelected(SelectionEvent e) {
      if (e.widget == mAddButton) {
        addFileMatchPattern();
        updateMatchView();
      } else if (e.widget == mEditButton) {
        editFileMatchPattern();
        updateMatchView();
      } else if (e.widget == mRemoveButton) {
        removeFileMatchPattern();
        updateMatchView();
      } else if (e.widget == mUpButton) {
        upFileMatchPattern();
        updateMatchView();
      } else if (e.widget == mDownButton) {
        downFileMatchPattern();
        updateMatchView();
      } else if (e.widget == mConfigureButton) {
        ICheckConfiguration config = mFileSet.getCheckConfig();

        if (config != null) {
          IProject project = (IProject) mPropertyPage.getElement();

          try {
            config.getCheckstyleConfiguration();

            CheckConfigurationWorkingCopy workingCopy = (CheckConfigurationWorkingCopy) config;

            CheckConfigurationConfigureDialog dialog = new CheckConfigurationConfigureDialog(
                    getShell(), workingCopy);
            dialog.setBlockOnOpen(true);
            dialog.open();

          } catch (CheckstylePluginException ex) {
            CheckstyleUIPlugin.warningDialog(mPropertyPage.getShell(),
                    NLS.bind(Messages.CheckstylePreferencePage_msgProjectRelativeConfigNoFound,
                            project, config.getLocation()),
                    ex);
          }
        }
      }
    }

    /**
     * @see SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
     */
    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
      // NOOP
    }

    /**
     * @see IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
     */
    @Override
    public void doubleClick(DoubleClickEvent event) {
      editFileMatchPattern();
      updateMatchView();
    }

    @Override
    public void checkStateChanged(CheckStateChangedEvent event) {
      if (event.getElement() instanceof FileMatchPattern) {
        FileMatchPattern pattern = (FileMatchPattern) event.getElement();
        pattern.setIsIncludePattern(event.getChecked());
        mPatternViewer.refresh();
        updateMatchView();
      }
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
      IStructuredSelection selection = (IStructuredSelection) event.getSelection();
      ICheckConfiguration config = (ICheckConfiguration) selection.getFirstElement();
      mFileSet.setCheckConfig(config);
    }
  }

  /**
   * Provides the labels for the FileSet list display.
   */
  class FileMatchPatternLabelProvider extends LabelProvider implements ITableLabelProvider {

    @Override
    public String getColumnText(Object element, int columnIndex) {
      String result = element.toString();
      if (element instanceof FileMatchPattern) {
        FileMatchPattern pattern = (FileMatchPattern) element;
        switch (columnIndex) {
          case 0:
            result = new String();
            break;

          case 1:
            result = pattern.getMatchPattern();
            break;

          default:
            break;
        }
      }
      return result;
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }
  }
}
