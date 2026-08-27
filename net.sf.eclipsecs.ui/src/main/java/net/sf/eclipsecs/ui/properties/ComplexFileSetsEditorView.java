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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import net.sf.eclipsecs.core.projectconfig.FileSet;
import net.sf.eclipsecs.ui.Messages;

/**
 * View composite of the advanced file sets editor, showing the file sets table and the buttons to
 * add, edit and remove file sets.
 *
 */
public final class ComplexFileSetsEditorView extends Composite {

    /** The table viewer for file sets. */
    private final ComplexFileSetsEditorTableView mViewer;

    /**
     * Creates the file sets editor view.
     *
     * @param parent
     *            the parent composite
     * @param style
     *            the widget style
     * @param changeEnabledState
     *            the listener notified when the enabled state of a file set changes
     * @param editFileSet
     *            the consumer invoked to edit a file set
     * @param addFileSet
     *            the runnable invoked to add a file set
     * @param removeFileSet
     *            the consumer invoked to remove a file set
     * @param mFileSets
     *            the list of file sets to display
     */
    public ComplexFileSetsEditorView(Composite parent, int style,
        ICheckStateListener changeEnabledState, Consumer<FileSet> editFileSet, Runnable addFileSet,
        Consumer<FileSet> removeFileSet, List<FileSet> mFileSets) {
        super(parent, style);
        setLayout(new FillLayout());

        final Group composite = new Group(this, SWT.NONE);
        composite.setText(Messages.ComplexFileSetsEditor_titleAdvancedFilesetEditor);
        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(composite);

        mViewer = new ComplexFileSetsEditorTableView(composite, SWT.NONE, changeEnabledState,
            editFileSet, mFileSets);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(mViewer);

        //
        // Build the buttons.
        //
        final Composite buttons = new Composite(composite, SWT.NULL);
        GridDataFactory.create(GridData.VERTICAL_ALIGN_BEGINNING).applyTo(buttons);
        GridLayoutFactory.swtDefaults().margins(0, 0).applyTo(buttons);

        final Button mAddButton = createPushButton(buttons, Messages.ComplexFileSetsEditor_btnAdd);
        mAddButton.addListener(SWT.Selection, event -> addFileSet.run());

        final Button mEditButton =
            createPushButton(buttons, Messages.ComplexFileSetsEditor_btnEdit);
        mEditButton.addListener(SWT.Selection,
            event -> editFileSet.accept(mViewer.getSelectedFileSet()));

        final Button mRemoveButton =
            createPushButton(buttons, Messages.ComplexFileSetsEditor_btnRemove);
        mRemoveButton.addListener(SWT.Selection,
            event -> removeFileSet.accept(mViewer.getSelectedFileSet()));
    }

    /**
     * Refreshes the file sets table.
     */
    public void refresh() {
        mViewer.refresh();
    }

    /**
     * Sets the checked state of the given file set.
     *
     * @param fileSet
     *            the file set to check
     * @param enabled
     *            true if the file set is enabled, false otherwise
     */
    public void setChecked(FileSet fileSet, boolean enabled) {
        mViewer.setChecked(fileSet, enabled);
    }

    /**
     * Creates a push button with the given label.
     *
     * @param parent
     *            the parent composite
     * @param label
     *            the button label
     * @return the created button
     */
    private static Button createPushButton(Composite parent, String label) {
        final Button button = new Button(parent, SWT.PUSH);
        button.setText(label);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(button);
        return button;
    }

}
