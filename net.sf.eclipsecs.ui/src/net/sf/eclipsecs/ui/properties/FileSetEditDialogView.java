//============================================================================
//
// Copyright (C) 2003-2023 the original author or authors.
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

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import net.sf.eclipsecs.core.config.CheckConfiguration;
import net.sf.eclipsecs.core.projectconfig.FileSet;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationWorkingCopy;
import net.sf.eclipsecs.ui.properties.FileMatchPatternControl.FileMatchPatternControlCallbacks;
import net.sf.eclipsecs.ui.properties.FileSetEditDialogMatchedFilesPreview.FileSetEditDialogMatchedFilesPreviewFilter;

public final class FileSetEditDialogView extends Composite {

  private final FileSetEditDialogCommonArea commonArea;
  private final FileMatchPatternControl fileMatchPatternTable;
  private final FileSetEditDialogMatchedFilesPreview matchArea;

  public FileSetEditDialogView(Composite parent, int style,
          FileMatchPatternControlCallbacks fileMatchPatternTableCallbacks,
          FileSetEditDialogMatchedFilesPreviewFilter previewFilter,
          String projectName,
          Consumer<CheckConfiguration> checkConfigSelectionChanged,
          Runnable configureFileSetConfig) {
    super(parent, style);
    GridLayoutFactory.swtDefaults().applyTo(this);

    this.commonArea = new FileSetEditDialogCommonArea(this, SWT.NONE, checkConfigSelectionChanged,
            configureFileSetConfig);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(commonArea);

    SashForm sashForm = new SashForm(this, SWT.VERTICAL);
    GridDataFactory.create(GridData.FILL_BOTH).hint(500, 400).applyTo(sashForm);
    GridLayoutFactory.swtDefaults().applyTo(sashForm);

    this.fileMatchPatternTable = new FileMatchPatternControl(sashForm, SWT.NONE,
            fileMatchPatternTableCallbacks);
    GridDataFactory.create(GridData.FILL_BOTH).applyTo(fileMatchPatternTable);

    this.matchArea = new FileSetEditDialogMatchedFilesPreview(sashForm, SWT.NONE, previewFilter,
            projectName);
    GridDataFactory.create(GridData.FILL_BOTH).applyTo(matchArea);

    sashForm.setWeights(new int[] {
        50,
        50,
    });
  }

  public void refreshMatchArea() {
    matchArea.refresh();
  }

  public void setProjectFiles(List<IFile> projectFiles) {
    matchArea.setInput(projectFiles);
  }

  public void refreshFileMatchPatternTable() {
    fileMatchPatternTable.refresh();
    matchArea.refresh();
  }

  public void setProjectConfiguration(ProjectConfigurationWorkingCopy configuration) {
    commonArea.setInput(configuration);
  }

  public void setFileSet(FileSet fileSet) {
    // intitialize the name
    commonArea.setText(fileSet.getName() != null ? fileSet.getName() : ""); //$NON-NLS-1$

    // init the check configuration combo
    if (fileSet.getCheckConfig() != null) {
      commonArea.setSelection(fileSet.getCheckConfig());
    }

    // init the pattern area
    fileMatchPatternTable.setInput(fileSet.getFileMatchPatterns());
  }

  public String getFileSetName() {
    return commonArea.getText();
  }

}
