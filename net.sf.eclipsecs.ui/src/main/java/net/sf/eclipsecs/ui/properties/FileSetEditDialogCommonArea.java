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

import java.util.function.Consumer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationWorkingCopy;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationLabelProvider;
import net.sf.eclipsecs.ui.config.CheckConfigurationViewerSorter;

/**
 * Composite containing the common area of the file set edit dialog, holding the file set name text
 * field and the check configuration selection.
 *
 */
public final class FileSetEditDialogCommonArea extends Composite {

    /** The text field for the file set name. */
    private final Text mFileSetNameText;
    /** The combo viewer for check configuration selection. */
    private final ComboViewer mComboViewer;

    /**
     * Creates the common area of the file set edit dialog.
     *
     * @param parent
     *            the parent composite
     * @param style
     *            the widget style
     * @param selectionChanged
     *            the consumer invoked when the check configuration selection changes
     * @param configureFileSetConfig
     *            the runnable to configure the file set configuration
     */
    public FileSetEditDialogCommonArea(Composite parent, int style,
        Consumer<ICheckConfiguration> selectionChanged, Runnable configureFileSetConfig) {
        super(parent, style);
        setLayout(new FillLayout());

        final Composite composite = new Composite(this, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).applyTo(composite);

        final Label nameLabel = new Label(composite, SWT.NULL);
        nameLabel.setText(Messages.FileSetEditDialog_lblName);

        mFileSetNameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(mFileSetNameText);

        final Label lblConfiguration = new Label(composite, SWT.NULL);
        lblConfiguration.setText(Messages.FileSetEditDialog_lblCheckConfig);

        final Composite comboComposite = new Composite(composite, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).applyTo(comboComposite);
        GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(comboComposite);

        mComboViewer = new ComboViewer(comboComposite);
        mComboViewer.setContentProvider(CheckConfigurationContentProvider.INSTANCE);
        mComboViewer.setLabelProvider(CheckConfigurationLabelProvider.INSTANCE);
        mComboViewer.setComparator(CheckConfigurationViewerSorter.INSTANCE);
        GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(mComboViewer.getControl());
        mComboViewer.addSelectionChangedListener(event -> selectionChanged
            .accept((ICheckConfiguration) event.getStructuredSelection().getFirstElement()));

        final Button mConfigureButton = new Button(comboComposite, SWT.PUSH);
        mConfigureButton.setText(Messages.FileSetEditDialog_btnConfigure);
        mConfigureButton.addSelectionListener(
            SelectionListener.widgetSelectedAdapter(event -> configureFileSetConfig.run()));
        GridDataFactory.swtDefaults().applyTo(mConfigureButton);
    }

    /**
     * Returns the file set name entered in the text field.
     *
     * @return the file set name
     */
    public String getText() {
        return this.mFileSetNameText.getText();
    }

    /**
     * Sets the input for the check configuration combo viewer.
     *
     * @param configuration
     *            the project configuration working copy
     */
    public void setInput(ProjectConfigurationWorkingCopy configuration) {
        this.mComboViewer.setInput(configuration);
    }

    /**
     * Sets the text of the file set name text field.
     *
     * @param text
     *            the file set name
     */
    public void setText(String text) {
        this.mFileSetNameText.setText(text);
    }

    /**
     * Selects the given check configuration in the combo viewer.
     *
     * @param iCheckConfiguration
     *            the check configuration to select
     */
    public void setSelection(ICheckConfiguration iCheckConfiguration) {
        this.mComboViewer.setSelection(new StructuredSelection(iCheckConfiguration));
    }
}
