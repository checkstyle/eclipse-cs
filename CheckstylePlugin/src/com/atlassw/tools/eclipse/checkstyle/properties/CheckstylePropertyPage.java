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

//=================================================
// Imports from java namespace
//=================================================
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.PreferencePage;
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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.dialogs.PropertyPage;

import com.atlassw.tools.eclipse.checkstyle.builder.CheckstyleBuilder;
import com.atlassw.tools.eclipse.checkstyle.config.FileSet;
import com.atlassw.tools.eclipse.checkstyle.config.FileSetFactory;
import com.atlassw.tools.eclipse.checkstyle.nature.CheckstyleNature;
import com.atlassw.tools.eclipse.checkstyle.nature.ConfigureDeconfigureNatureJob;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;

/**
 * Property page.
 */
public class CheckstylePropertyPage extends PropertyPage
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

    private IProject            mProject;

    private Composite           mComposite;

    private CheckboxTableViewer mViewer;

    private Button              mAddButton;

    private Button              mEditButton;

    private Button              mRemoveButton;

    private List                mFileSets;

    private boolean             mNeedRebuild = false;

    //=================================================
    // Constructors & finalizer.
    //=================================================

    /**
     * Constructor for SamplePropertyPage.
     */
    public CheckstylePropertyPage()
    {
        super();
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     * @see PreferencePage#createContents(Composite)
     */
    protected Control createContents(Composite parent)
    {
        mComposite = parent;

        noDefaultAndApplyButton();
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);

        //
        //  Get the project.
        //
        IResource resource = (IResource) getElement();
        if (resource.getType() == IResource.PROJECT)
        {
            mProject = (IProject) resource;
        }
        else
        {
            return parent;
        }

        //
        //  Initialize the file sets for editing.
        //
        if (!initializeFileSets())
        {
            CheckstyleLog.internalErrorDialog();
            return null;
        }

        //
        //  Create the table of file sets.
        //
        Table table = new Table(composite, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION);

        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = convertWidthInCharsToPixels(60);
        data.heightHint = convertHeightInCharsToPixels(10);
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
        mViewer.setContentProvider(new FileSetProvider());
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
     * {@inheritDoc}
     */
    public boolean performOk()
    {
        try
        {
            FileSetFactory.setFileSets(mFileSets, mProject);
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error("Failed to save FileSets: " + e.getMessage(), e);
            CheckstyleLog.internalErrorDialog();
        }

        try
        {
            addNature();
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error("Failed to add project nature: " + e.getMessage(), e);
            CheckstyleLog.internalErrorDialog();
        }

        if (mNeedRebuild)
        {
            try
            {
                CheckstyleBuilder.buildProject(mProject, mComposite.getShell());
            }
            catch (CheckstylePluginException e)
            {
                CheckstyleLog.error("Failed to rebuild project: " + e.getMessage(), e);
                CheckstyleLog.internalErrorDialog();
            }
        }

        return true;
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
                mNeedRebuild = true;
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
                mNeedRebuild = true;
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
        mNeedRebuild = true;
    }

    private void changeEnabledState(CheckStateChangedEvent event)
    {
        if (event.getElement() instanceof FileSet)
        {
            FileSet fileSet = (FileSet) event.getElement();
            fileSet.setEnabled(event.getChecked());
            mViewer.refresh();
            mNeedRebuild = true;
        }
        else
        {
            CheckstyleLog.warning("Checked element in FileSet table not a FileSet");
        }
    }

    /**
     * Add the Checkstyle nature to the project.
     */
    private void addNature() throws CheckstylePluginException
    {
        try
        {
            //
            //  Check to see if the project already has the Checkstyle nature.
            //
            if (mProject.getNature(CheckstyleNature.NATURE_ID) != null)
            {
                //
                //  The project already has the nature.
                //
                return;
            }

            //
            //  Add the nature to the project.
            //
            ConfigureDeconfigureNatureJob natureJob = new ConfigureDeconfigureNatureJob(mProject,
                    CheckstyleNature.NATURE_ID);
            natureJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
            natureJob.schedule();
        }
        catch (CoreException e)
        {
            CheckstyleLog.error("Failed to add Checkstyle nature to project", e);
            throw new CheckstylePluginException("Failed to add Checkstyle nature to project");
        }
    }

    private boolean initializeFileSets()
    {
        //
        //  Make a clone of the file sets so that the real values do not
        //  get modified until the user presses the OK button (they might
        //  press Cancel instead).
        //
        mFileSets = null;
        try
        {
            List fileSets = FileSetFactory.getFileSets(mProject);
            mFileSets = new LinkedList();
            Iterator iter = fileSets.iterator();
            while (iter.hasNext())
            {
                FileSet fileSet = (FileSet) iter.next();
                fileSet = (FileSet) fileSet.clone();
                mFileSets.add(fileSet);
            }
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error("Failed get FileSets, " + e.getMessage(), e);
            mFileSets = null;
            return false;
        }
        catch (CloneNotSupportedException e)
        {
            CheckstyleLog.error("Failed to clone FileSet, " + e.getMessage(), e);
            mFileSets = null;
            return false;
        }

        return true;
    }

}