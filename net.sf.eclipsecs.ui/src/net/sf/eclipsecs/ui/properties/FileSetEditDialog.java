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
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.projectconfig.FileMatchPattern;
import net.sf.eclipsecs.core.projectconfig.FileSet;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUiPlugin;
import net.sf.eclipsecs.ui.CheckstyleUiPluginImages;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationConfigureDialog;
import net.sf.eclipsecs.ui.properties.FileMatchPatternControl.FileMatchPatternControlCallbacks;
import net.sf.eclipsecs.ui.properties.FileSetEditDialogMatchedFilesPreview.FileSetEditDialogMatchedFilesPreviewFilter;
import net.sf.eclipsecs.ui.util.SwtUtil;

public final class FileSetEditDialog extends TitleAreaDialog {

  private static final String DEFAULT_PATTERN = ".java$"; //$NON-NLS-1$

  private final PropertyPageContext propertyPageContext;
  private FileSet mFileSet;
  private boolean mIsCreatingNewFileset;
  private FileSetEditDialogView dialogView;

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
    GridDataFactory.create(GridData.FILL_BOTH).applyTo(composite);
    FileMatchPatternControlCallbacks callbacks = new FileMatchPatternControlCallbacks(
            this::editFileMatchPattern, this::refreshMatchArea, this::addFileMatchPattern,
            this::removeFileMatchPattern, this::upFileMatchPattern, this::downFileMatchPattern);
    dialogView = new FileSetEditDialogView(composite, SWT.NONE, callbacks,
            new FileSetEditDialogMatchedFilesPreviewFilter(mFileSet),
            propertyPageContext.project().getName(), mFileSet::setCheckConfig,
            this::configureFileSetConfig);
    GridDataFactory.create(GridData.FILL_BOTH).applyTo(dialogView);

    // init the data
    initializeControls();

    return composite;
  }

  /**
   * Initializes the controls with their data.
   */
  private void initializeControls() {

    // init the check configuration combo
    dialogView.setProjectConfiguration(propertyPageContext.configuration());

    this.setTitleImage(CheckstyleUiPluginImages.PLUGIN_LOGO.getImage());
    this.setMessage(Messages.FileSetEditDialog_message);

    if (mIsCreatingNewFileset) {
      this.setTitle(Messages.FileSetEditDialog_titleCreate);
    } else {
      this.setTitle(Messages.FileSetEditDialog_titleEdit);
    }

    dialogView.setFileSet(mFileSet);

    getShell().getDisplay().asyncExec(() -> {
      List<IFile> mProjectFiles;
      try {
        mProjectFiles = getFiles(propertyPageContext.project());
        dialogView.setProjectFiles(mProjectFiles); // init the test area
      } catch (CoreException ex) {
        CheckstyleLog.log(ex);
      }
    });

  }

  @Override
  public void create() {
    super.create();

    SwtUtil.addResizeSupport(this, CheckstyleUiPlugin.getDefault().getDialogSettings(),
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
    String name = dialogView.getFileSetName();
    if (name == null || name.trim().length() <= 0) {
      this.setErrorMessage(Messages.FileSetEditDialog_msgNoFilesetName);
    } else {
      //
      // Get the CheckConfiguration.
      //
      if (mFileSet.getCheckConfig() == null) {
        this.setErrorMessage(Messages.FileSetEditDialog_noCheckConfigSelected);
      } else {
        mFileSet.setName(name);
        super.okPressed();
      }
    }
  }

  private void addFileMatchPattern() {
    FileMatchPatternEditDialog dialog = new FileMatchPatternEditDialog(getShell(), null);
    if (Window.OK == dialog.open()) {

      FileMatchPattern pattern = dialog.getPattern();

      mFileSet.getFileMatchPatterns().add(pattern);
      dialogView.refreshFileMatchPatternTable();
    }
  }

  private void editFileMatchPattern(FileMatchPattern pattern) {
    if (pattern != null) {
      FileMatchPatternEditDialog dialog = new FileMatchPatternEditDialog(getShell(), pattern.clone());

      if (Window.OK == dialog.open()) {

        FileMatchPattern editedPattern = dialog.getPattern();
        mFileSet.getFileMatchPatterns().set(mFileSet.getFileMatchPatterns().indexOf(pattern),
                editedPattern);
        dialogView.refreshFileMatchPatternTable();
      }
    }
  }

  private void removeFileMatchPattern(FileMatchPattern pattern) {
    if (pattern != null) {
      mFileSet.getFileMatchPatterns().remove(pattern);
      dialogView.refreshFileMatchPatternTable();
    }
  }

  private void upFileMatchPattern(FileMatchPattern pattern) {
    if (pattern != null) {
      int index = mFileSet.getFileMatchPatterns().indexOf(pattern);
      if (index > 0) {
        mFileSet.getFileMatchPatterns().remove(pattern);
        mFileSet.getFileMatchPatterns().add(index - 1, pattern);
        dialogView.refreshFileMatchPatternTable();
      }
    }
  }

  private void downFileMatchPattern(FileMatchPattern pattern) {
    if (pattern != null) {
      int index = mFileSet.getFileMatchPatterns().indexOf(pattern);
      if (index >= 0 && index < mFileSet.getFileMatchPatterns().size() - 1) {
        mFileSet.getFileMatchPatterns().remove(pattern);
        if (index < mFileSet.getFileMatchPatterns().size() - 1) {
          mFileSet.getFileMatchPatterns().add(index + 1, pattern);
        } else {
          mFileSet.getFileMatchPatterns().add(pattern);
        }

        dialogView.refreshFileMatchPatternTable();
      }
    }
  }

  private void refreshMatchArea() {
    dialogView.refreshMatchArea();
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
        CheckstyleUiPlugin.warningDialog(getShell(),
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
