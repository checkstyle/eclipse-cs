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

import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.projectconfig.FileSet;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationWorkingCopy;
import net.sf.eclipsecs.ui.properties.FileMatchPatternControl.FileMatchPatternControlCallbacks;
import net.sf.eclipsecs.ui.properties.FileSetEditDialogMatchedFilesPreview.FileSetEditDialogMatchedFilesPreviewFilter;

/**
 * View composite of the file set edit dialog, laying out the common name and configuration area,
 * the file match pattern editor and the matched files preview.
 *
 */
public final class FileSetEditDialogView extends Composite {

    /** The common area with name and config selection. */
    private final FileSetEditDialogCommonArea commonArea;
    /** The file match pattern table. */
    private final FileMatchPatternControl fileMatchPatternTable;
    /** The matched files preview area. */
    private final FileSetEditDialogMatchedFilesPreview matchArea;

    /**
     * Constructor building the file set edit dialog view, laying out the common area, the file
     * match pattern editor and the matched files preview.
     *
     * @param parent
     *            the parent composite
     * @param style
     *            the SWT style bits to apply to this composite
     * @param fileMatchPatternTableCallbacks
     *            the callbacks for the file match pattern table
     * @param previewFilter
     *            the filter used by the matched files preview
     * @param projectName
     *            the name of the project
     * @param checkConfigSelectionChanged
     *            the callback invoked when the check config selection changes
     * @param configureFileSetConfig
     *            the callback invoked to configure the file set config
     */
    public FileSetEditDialogView(Composite parent, int style,
        FileMatchPatternControlCallbacks fileMatchPatternTableCallbacks,
        FileSetEditDialogMatchedFilesPreviewFilter previewFilter, String projectName,
        Consumer<ICheckConfiguration> checkConfigSelectionChanged,
        Runnable configureFileSetConfig) {
        super(parent, style);
        GridLayoutFactory.swtDefaults().applyTo(this);

        this.commonArea = new FileSetEditDialogCommonArea(this, SWT.NONE,
            checkConfigSelectionChanged, configureFileSetConfig);
        GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(commonArea);

        final SashForm sashForm = new SashForm(this, SWT.VERTICAL);
        GridDataFactory.create(GridData.FILL_BOTH).applyTo(sashForm);
        GridLayoutFactory.swtDefaults().applyTo(sashForm);

        this.fileMatchPatternTable =
            new FileMatchPatternControl(sashForm, SWT.NONE, fileMatchPatternTableCallbacks);
        GridDataFactory.create(GridData.FILL_BOTH).applyTo(fileMatchPatternTable);

        this.matchArea = new FileSetEditDialogMatchedFilesPreview(sashForm, SWT.NONE, previewFilter,
            projectName);
        GridDataFactory.create(GridData.FILL_BOTH).applyTo(matchArea);

        sashForm.setWeights(new int[] {
            1, 1,
        });
    }

    /**
     * Refreshes the matched files preview area.
     */
    public void refreshMatchArea() {
        matchArea.refresh();
    }

    /**
     * Sets the project files to be shown in the matched files preview.
     *
     * @param projectFiles
     *            the project files to show
     */
    public void setProjectFiles(List<IFile> projectFiles) {
        matchArea.setInput(projectFiles);
    }

    /**
     * Refreshes the file match pattern table and the matched files preview.
     */
    public void refreshFileMatchPatternTable() {
        fileMatchPatternTable.refresh();
        matchArea.refresh();
    }

    /**
     * Sets the project configuration to be shown in the common area.
     *
     * @param configuration
     *            the project configuration to show
     */
    public void setProjectConfiguration(ProjectConfigurationWorkingCopy configuration) {
        commonArea.setInput(configuration);
    }

    /**
     * Sets the given file set, initializing the name, the check configuration selection and the
     * file match patterns.
     *
     * @param fileSet
     *            the file set to display
     */
    public void setFileSet(FileSet fileSet) {
        // intitialize the name
        final String name;
        if (fileSet.getName() != null) {
            name = fileSet.getName();
        }
        else {
            name = "";
        }
        commonArea.setText(name);

        // init the check configuration combo
        if (fileSet.getCheckConfig() != null) {
            commonArea.setSelection(fileSet.getCheckConfig());
        }

        // init the pattern area
        fileMatchPatternTable.setInput(fileSet.getFileMatchPatterns());
    }

    /**
     * Returns the file set name entered in the common area.
     *
     * @return the file set name
     */
    public String getFileSetName() {
        return commonArea.getText();
    }

}
