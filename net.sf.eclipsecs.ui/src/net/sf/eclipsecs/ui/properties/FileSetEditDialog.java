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

package net.sf.eclipsecs.ui.properties;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.projectconfig.FileMatchPattern;
import net.sf.eclipsecs.core.projectconfig.FileSet;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationConfigureDialog;
import net.sf.eclipsecs.ui.properties.FileMatchPatternTable.FileMatchPatternTableCallbacks;
import net.sf.eclipsecs.ui.util.SWTUtil;

/**
 * Property page.
 */
public final class FileSetEditDialog extends TitleAreaDialog {

  private static final String DEFAULT_PATTERN = ".java$"; //$NON-NLS-1$

  private final PropertyPageContext propertyPageContext;
  private FileSetEditDialogCommonArea commonArea;
  private FileMatchPatternTable fileMatchPatternTable;
  private FileSetEditDialogMatchedFilesPreview matchArea;
  private FileSet mFileSet;
  private List<IFile> mProjectFiles;
  private boolean mIsCreatingNewFileset;

  /**
   * Constructor for SamplePropertyPage.
   */
  FileSetEditDialog(Shell parent, FileSet fileSet, PropertyPageContext propertyPageContext)
          throws CheckstylePluginException {
    super(parent);
    setShellStyle(getShellStyle() | SWT.RESIZE);
    setHelpAvailable(false);
    mFileSet = fileSet;
    this.propertyPageContext = propertyPageContext;

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

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite composite = (Composite) super.createDialogArea(parent);
    composite.setLayoutData(new GridData(GridData.FILL_BOTH));
    Composite dialog = new Composite(composite, SWT.NONE);
    dialog.setLayout(new GridLayout(1, false));
    dialog.setLayoutData(new GridData(GridData.FILL_BOTH));

    this.commonArea = new FileSetEditDialogCommonArea(dialog, SWT.NONE, mFileSet::setCheckConfig,
            this::configureFileSetConfig);
    commonArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    SashForm sashForm = new SashForm(dialog, SWT.VERTICAL);
    GridData gridData = new GridData(GridData.FILL_BOTH);
    gridData.widthHint = 500;
    gridData.heightHint = 400;
    sashForm.setLayoutData(gridData);
    sashForm.setLayout(new GridLayout());

    this.fileMatchPatternTable = new FileMatchPatternTable(sashForm, SWT.NONE,
            new FileMatchPatternTableCallbacks(this::editFileMatchPattern, this::updateMatchView,
                    this::addFileMatchPattern, this::removeFileMatchPattern,
                    this::upFileMatchPattern, this::downFileMatchPattern));
    fileMatchPatternTable.setLayoutData(new GridData(GridData.FILL_BOTH));

    this.matchArea = new FileSetEditDialogMatchedFilesPreview(sashForm, SWT.NONE, mFileSet);
    matchArea.setLayoutData(new GridData(GridData.FILL_BOTH));

    sashForm.setWeights(new int[] { 50, 50 });

    // init the data
    initializeControls();

    return composite;
  }

  /**
   * Initializes the controls with their data.
   */
  private void initializeControls() {

    // init the check configuration combo
    commonArea.setInput(propertyPageContext.configuration());

    this.setTitleImage(CheckstyleUIPluginImages.PLUGIN_LOGO.getImage());
    this.setMessage(Messages.FileSetEditDialog_message);

    if (mIsCreatingNewFileset) {
      this.setTitle(Messages.FileSetEditDialog_titleCreate);
    } else {
      this.setTitle(Messages.FileSetEditDialog_titleEdit);
    }

    // intitialize the name
    commonArea.setText(mFileSet.getName() != null ? mFileSet.getName() : ""); //$NON-NLS-1$

    // init the check configuration combo
    if (mFileSet.getCheckConfig() != null) {
      commonArea.setSelection(mFileSet.getCheckConfig());
    }

    // init the pattern area
    fileMatchPatternTable.setInput(mFileSet.getFileMatchPatterns());
    for (FileMatchPattern pattern : mFileSet.getFileMatchPatterns()) {
      fileMatchPatternTable.setChecked(pattern, pattern.isIncludePattern());
    }

    getShell().getDisplay().asyncExec(() -> {
      try {
        mProjectFiles = getFiles(propertyPageContext.project());
      } catch (CoreException ex) {
        CheckstyleLog.log(ex);
      }
      matchArea.setInput(mProjectFiles); // init the test area
      updateMatchView();
    });

  }

  private void updateMatchView() {
    matchArea.refresh();
    matchArea.setText(itemCount -> NLS.bind(Messages.FileSetEditDialog_titleTestResult,
            new String[] { propertyPageContext.project().getName(), Integer.toString(itemCount),
                Integer.toString(mProjectFiles.size()) }));
  }

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

  @Override
  protected void okPressed() {
    //
    // Get the FileSet name.
    //
    String name = commonArea.getText();
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

  private void addFileMatchPattern() {
    FileMatchPatternEditDialog dialog = new FileMatchPatternEditDialog(getShell(), null);
    if (Window.OK == dialog.open()) {

      FileMatchPattern pattern = dialog.getPattern();

      mFileSet.getFileMatchPatterns().add(pattern);
      fileMatchPatternTable.refresh();
      fileMatchPatternTable.setChecked(pattern, pattern.isIncludePattern());
    }
  }

  private void editFileMatchPattern(FileMatchPattern pattern) {
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
      fileMatchPatternTable.refresh();
      fileMatchPatternTable.setChecked(editedPattern, editedPattern.isIncludePattern());
    }
  }

  private void removeFileMatchPattern(FileMatchPattern pattern) {
    if (pattern == null) {
      //
      // Nothing is selected.
      //
      return;
    }

    mFileSet.getFileMatchPatterns().remove(pattern);
    fileMatchPatternTable.refresh();
  }

  private void upFileMatchPattern(FileMatchPattern pattern) {
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
      fileMatchPatternTable.refresh();
    }
  }

  private void downFileMatchPattern(FileMatchPattern pattern) {
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

      fileMatchPatternTable.refresh();
    }
  }

  private void configureFileSetConfig() {
    CheckConfigurationWorkingCopy config = (CheckConfigurationWorkingCopy) mFileSet.getCheckConfig();
    if (config != null) {
      try {
        config.getCheckstyleConfiguration();

        CheckConfigurationConfigureDialog dialog = new CheckConfigurationConfigureDialog(
                getShell(), config);
        dialog.setBlockOnOpen(true);
        dialog.open();

      } catch (CheckstylePluginException ex) {
        CheckstyleUIPlugin.warningDialog(getShell(),
                NLS.bind(Messages.CheckstylePreferencePage_msgProjectRelativeConfigNoFound,
                        propertyPageContext.project(), config.getLocation()),
                ex);
      }
    }
  }

  private static List<IFile> getFiles(IContainer container) throws CoreException {
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
}
