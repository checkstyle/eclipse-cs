//============================================================================
//
// Copyright (C) 2002-2004  David Schneider
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

package com.atlassw.tools.eclipse.checkstyle.properties;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.FileSet;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;

/**
 * Property page.
 */
public class ComplexFileSetsEditor implements IFileSetsEditor
{
    //=================================================
    // Public static final variables.
    //=================================================

    //=================================================
    // Static class variables.
    //=================================================

    //=================================================
    // Instance member variables.
    //=================================================

    private IProject mProject;

    private Composite mComposite;

    private CheckboxTableViewer mViewer;

    private Button mAddButton;

    private Button mEditButton;

    private Button mRemoveButton;

    private List mFileSets;

    private CheckstylePropertyPage mPropertyPage;

    //=================================================
    // Constructors & finalizer.
    //=================================================

    /**
     * Creates the ComplexFileSetsEditor.
     * 
     * @param propsPage the property page
     */
    public ComplexFileSetsEditor(CheckstylePropertyPage propsPage)
    {
        mPropertyPage = propsPage;
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     * @see IFileSetsEditor#setFileSets(java.util.List)
     */
    public void setFileSets(List fileSets)
    {
        mFileSets = fileSets;

    }

    /**
     * @see IFileSetsEditor#getFileSets()
     */
    public List getFileSets()
    {
        return mFileSets;
    }

    /**
     * @see IFileSetsEditor#createContents(Composite)
     */
    public Control createContents(Composite parent) throws CheckstylePluginException
    {

        mComposite = parent;

        Group composite = new Group(parent, SWT.NONE);
        composite.setText(CheckstylePlugin.getResourceString("ComplexFileSetsEditor.title"));

        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);

        //
        //  Create the table of file sets.
        //
        Table table = new Table(composite, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION);

        GridData data = new GridData(GridData.FILL_BOTH);
        table.setLayoutData(data);

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableLayout tableLayout = new TableLayout();
        table.setLayout(tableLayout);

        TableColumn column1 = new TableColumn(table, SWT.NONE);
        column1.setText("Enabled");
        column1.setResizable(false);

        TableColumn column2 = new TableColumn(table, SWT.NONE);
        column2.setText("File Set");

        tableLayout.addColumnData(new ColumnWeightData(12));
        tableLayout.addColumnData(new ColumnWeightData(48));

        mViewer = new CheckboxTableViewer(table);
        mViewer.setLabelProvider(new FileSetLabelProvider());
        mViewer.setContentProvider(new ArrayContentProvider());
        mViewer.setSorter(new FileSetViewerSorter());
        mViewer.setInput(mFileSets);

        //
        //  Set checked state
        //
        Iterator iter = mFileSets.iterator();
        while (iter.hasNext())
        {
            FileSet fileSet = (FileSet) iter.next();
            mViewer.setChecked(fileSet, fileSet.isEnabled());
        }

        mViewer.addDoubleClickListener(new IDoubleClickListener()
        {
            public void doubleClick(DoubleClickEvent e)
            {
                editFileSet();
            }
        });

        mViewer.addCheckStateListener(new ICheckStateListener()
        {
            public void checkStateChanged(CheckStateChangedEvent event)
            {
                changeEnabledState(event);
            }
        });

        //
        //  Build the buttons.
        //
        Composite buttons = new Composite(composite, SWT.NULL);
        buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        buttons.setLayout(layout);

        mAddButton = createPushButton(buttons, "Add...");
        mAddButton.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event evt)
            {
                addFileSet();
            }
        });

        mEditButton = createPushButton(buttons, "Edit...");
        mEditButton.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event evt)
            {
                editFileSet();
            }
        });

        mRemoveButton = createPushButton(buttons, "Remove");
        mRemoveButton.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event evt)
            {
                removeFileSet();
            }
        });

        return composite;
    }

    /**
     * Utility method that creates a push button instance and sets the default
     * layout data.
     * 
     * @param parent the parent for the new button
     * @param label the label for the new button
     * @return the newly-created button
     */
    private Button createPushButton(Composite parent, String label)
    {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(label);
        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        button.setLayoutData(data);
        return button;
    }

    private void addFileSet()
    {
        try
        {
            FileSetEditDialog dialog = new FileSetEditDialog(mComposite.getShell(), null, mProject);
            dialog.open();
            if (dialog.okWasPressed())
            {
                FileSet fileSet = dialog.getFileSet();
                mFileSets.add(fileSet);
                mViewer.refresh();
                mViewer.setChecked(fileSet, fileSet.isEnabled());

                mPropertyPage.getContainer().updateButtons();
            }
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error("Failed to add FileSet: " + e.getMessage(), e);
            CheckstyleLog.internalErrorDialog();
        }
    }

    private void editFileSet()
    {
        IStructuredSelection selection = (IStructuredSelection) mViewer.getSelection();
        FileSet fileSet = (FileSet) selection.getFirstElement();
        if (fileSet == null)
        {
            //
            //  Nothing is selected.
            //
            return;
        }

        try
        {
            FileSetEditDialog dialog = new FileSetEditDialog(mComposite.getShell(), fileSet,
                    mProject);
            dialog.open();
            if (dialog.okWasPressed())
            {
                FileSet newFileSet = dialog.getFileSet();
                mFileSets.remove(fileSet);
                mFileSets.add(newFileSet);
                mViewer.refresh();
                mViewer.setChecked(newFileSet, newFileSet.isEnabled());

                mPropertyPage.getContainer().updateButtons();
            }
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error("Failed to edit FileSet: " + e.getMessage(), e);
            CheckstyleLog.internalErrorDialog();
        }
    }

    private void removeFileSet()
    {
        IStructuredSelection selection = (IStructuredSelection) mViewer.getSelection();
        FileSet fileSet = (FileSet) selection.getFirstElement();
        if (fileSet == null)
        {
            //
            //  Nothing is selected.
            //
            return;
        }

        mFileSets.remove(fileSet);
        mViewer.refresh();
        mPropertyPage.getContainer().updateButtons();
    }

    private void changeEnabledState(CheckStateChangedEvent event)
    {
        if (event.getElement() instanceof FileSet)
        {
            FileSet fileSet = (FileSet) event.getElement();
            fileSet.setEnabled(event.getChecked());
            mViewer.refresh();
        }
        else
        {
            CheckstyleLog.warning("Checked element in FileSet table not a FileSet");
        }
    }

}