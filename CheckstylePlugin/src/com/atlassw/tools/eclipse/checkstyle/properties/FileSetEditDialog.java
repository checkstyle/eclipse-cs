//============================================================================
//
// Copyright (C) 2002-2003  David Schneider
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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfigurationFactory;
import com.atlassw.tools.eclipse.checkstyle.config.FileMatchPattern;
import com.atlassw.tools.eclipse.checkstyle.config.FileSet;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;


//=================================================
// Imports from org namespace
//=================================================
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;


/**
 *  Property page.
 */
public class FileSetEditDialog extends Dialog
{
    //=================================================
    // Public static final variables.
    //=================================================
  
    //=================================================
    // Static class variables.
    //=================================================
    
    private static final int MAX_LENGTH = 40;
    
    private static final String JAVA_SUFFIX = ".java";
    
    private static final String DEFAULT_PATTERN = "[.java]$";
    
    private static final String NOTE_TEXT =
        "Note: The last matching regular expression determines if a file is included " +
        "or excluded from the File Set.";
    
    //=================================================
    // Instance member variables.
    //=================================================
    
    private IProject                mProject;
    
    private Composite               mComposite;

    private CheckboxTableViewer     mViewer;
    
    private Text                    mFileSetNameText;

    private Button                  mAddButton;

    private Button                  mEditButton;

    private Button                  mRemoveButton;

    private Button                  mTestButton;

    private Button                  mUpButton;

    private Button                  mDownButton;
    
    private Combo                   mAuditConfigCombo;
    
    private FileSet                 mFileSet;
    
    private CheckConfiguration[]    mAuditConfigs;
    
    private List                    mFileMatchPatterns = new LinkedList();
    
    private boolean                mOkWasPressed = false;

    //=================================================
    // Constructors & finalizer.
    //=================================================

	/**
	 *  Constructor for SamplePropertyPage.
	 */
	public FileSetEditDialog(Shell parent, FileSet fileSet, IProject project)
        throws CheckstylePluginException
	{
		super(parent);
        mProject = project;
        try
        {
            if (fileSet != null)
            {
                mFileSet = (FileSet)fileSet.clone();
                mFileMatchPatterns = new LinkedList(mFileSet.getFileMatchPatterns());
            }
            else
            {
                mFileMatchPatterns.add(new FileMatchPattern(DEFAULT_PATTERN));
            }
        }
        catch (CloneNotSupportedException e)
        {
            CheckstyleLog.error("Failed to clone FileSet", e);
            throw new CheckstylePluginException("Failed to clone FileSet");
        }
	}

    //=================================================
    // Methods.
    //=================================================

    /**
     * @see Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent)
    {
        mComposite = parent;
        Composite composite = (Composite)super.createDialogArea(parent);
        
        Composite dialog = new Composite(composite, SWT.NONE);
        
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        dialog.setLayout(layout);
        
        createFileSetNamePart(dialog);
        createAuditConfigSelectionPart(dialog);
        createFileMatchPatternPart(dialog);

        dialog.layout();
        return composite;
    }
    
    private void createFileSetNamePart(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        
        Label nameLabel = new Label(composite, SWT.NULL);
        nameLabel.setText("File Set Name:");

        mFileSetNameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = true;
        data.verticalAlignment = GridData.CENTER;
        data.grabExcessVerticalSpace = false;
        data.widthHint = convertWidthInCharsToPixels(MAX_LENGTH);
        data.heightHint = convertHeightInCharsToPixels(1);
        mFileSetNameText.setLayoutData(data);
        mFileSetNameText.setFont(parent.getFont());
        
        //
        //  Populate with the existing name if one exists.
        //
        if (mFileSet != null)
        {
            mFileSetNameText.setText(mFileSet.getName());
        }
    }
    
	private void createAuditConfigSelectionPart(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		Label nameLabel = new Label(composite, SWT.NULL);
		nameLabel.setText("Check Configuration:");

		//
		//  Create a combo box for selecting a value from the enumeration.
		//
        List configList = null;
        try
        {
		    configList = CheckConfigurationFactory.getCheckConfigurations();
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error("Failed to get list of CheckConfiguration objects, " 
                                + e.getMessage(), e);
            CheckstyleLog.internalErrorDialog();
            return;
        }
        Collections.sort(configList);
		mAuditConfigs = new CheckConfiguration[configList.size()];
        String labels[] = new String[configList.size()];
        
        //
        //  Find the current check config to position the combo box at.
        //
		int initialIndex = -1;
		Iterator iter = configList.iterator();
		for (int i = 0; iter.hasNext(); i++)
		{
			CheckConfiguration config = (CheckConfiguration)iter.next();
            mAuditConfigs[i] = config;
			labels[i] = config.getConfigName();
            if (mFileSet != null)
            {
                if (mFileSet.getCheckConfig().getConfigName().equals(config.getConfigName()))
                {
                    initialIndex = i;
                }
            }
		}
		mAuditConfigCombo = new Combo(composite, SWT.NONE | SWT.DROP_DOWN | SWT.READ_ONLY);
		mAuditConfigCombo.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		mAuditConfigCombo.setItems(labels);
		
		//
		//  Even though the Javadoc for the Combo.select() method says indecies out of range
		//  are ignored a bug have been reported on the Mac platform showing an
		//  IllegalArgumentException exception being thrown with a message of
		//  "Index out of bounds" from this method.
		//
		if ((initialIndex >= 0) && (initialIndex < labels.length))
		{
		    mAuditConfigCombo.select(initialIndex);
		}
	} 
      
    private void createFileMatchPatternPart(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);        
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        
        Table table = new Table(composite, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION);
        TableLayout tableLayout = new TableLayout();
        table.setLayout(tableLayout);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = convertWidthInCharsToPixels(80);
        data.heightHint = convertHeightInCharsToPixels(10);
        table.setLayoutData(data);

        TableColumn column1 = new TableColumn(table, SWT.NONE);
        column1.setText("Include");
        column1.setResizable(false);
        tableLayout.addColumnData(new ColumnWeightData(10));

        TableColumn column2 = new TableColumn(table, SWT.NONE);
        column2.setText("File Matching Regular Expression");
        tableLayout.addColumnData(new ColumnWeightData(40));

        mViewer = new CheckboxTableViewer(table);
        mViewer.setLabelProvider(new FileMatchPatternLabelProvider());
        mViewer.setContentProvider(new FileMatchPatternProvider());
        mViewer.setSorter(new FileMatchPatternViewerSorter());
        
        //
        //  Create the table items.
        //
        Iterator iter = mFileMatchPatterns.iterator();
        mViewer.setInput(mFileMatchPatterns);
        while(iter.hasNext())
        {
            FileMatchPattern pattern = (FileMatchPattern)iter.next();
            mViewer.setChecked(pattern, pattern.isIncludePattern());
        }

        mViewer.addDoubleClickListener(new IDoubleClickListener()
        {
            public void doubleClick(DoubleClickEvent e)
            {
                editFileMatchPattern();
            }
        });
        
        mViewer.addCheckStateListener(new ICheckStateListener()
        {
            public void checkStateChanged(CheckStateChangedEvent event)
            {
                changeIncludeState(event);
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
                addFileMatchPattern();
            }
        });

        mEditButton = createPushButton(buttons, "Edit...");
        mEditButton.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event evt)
            {
                editFileMatchPattern();
            }
        });

        mRemoveButton = createPushButton(buttons, "Remove");
        mRemoveButton.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event evt)
            {
                removeFileMatchPattern();
            }
        });

        mTestButton = createPushButton(buttons, "Test...");
        mTestButton.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event evt)
            {
                testFileMatchPattern();
            }
        });

        mUpButton = createPushButton(buttons, " Up ");
        mUpButton.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event evt)
            {
                upFileMatchPattern();
            }
        });

        mDownButton = createPushButton(buttons, " Down ");
        mDownButton.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event evt)
            {
                downFileMatchPattern();
            }
        });

        //
        //  Add the note text.
        //
        Text noteText = new Text(composite, SWT.WRAP|SWT.READ_ONLY|SWT.MULTI);
        data = new GridData();
        data.horizontalAlignment = GridData.BEGINNING;
        data.grabExcessHorizontalSpace = false;
        data.widthHint = convertWidthInCharsToPixels(80);
        data.verticalAlignment = GridData.CENTER;
        data.grabExcessVerticalSpace = false;
        noteText.setLayoutData(data);
        noteText.setFont(parent.getFont());
        noteText.setText(NOTE_TEXT);
    }

    protected void okPressed()
    {
        //
        //  Get the FileSet name.
        //
        String name = mFileSetNameText.getText();
        if ((name == null) || (name.trim().length() <= 0))
        {
            MessageDialog.openError(mComposite.getShell(),
                        "Validation Error",
                        "A FileSet name must be provided");
            return;
        }

        //
        //  Get the CheckConfiguration.
        //
        int index = mAuditConfigCombo.getSelectionIndex();
        if ((index < 0) || (index >= mAuditConfigs.length))
        {
            MessageDialog.openError(mComposite.getShell(),
                        "Validation Error",
                        "An Audit Configuration must be selected");
            return;
        }
        
        //
        //  Create a new FileSet.
        //
        boolean enabledState = true;
        if (mFileSet != null)
        {
            enabledState = mFileSet.isEnabled();
        }
        mFileSet = new FileSet(name, mAuditConfigs[index]);
        mFileSet.setFileMatchPatterns(mFileMatchPatterns);
        mFileSet.setEnabled(enabledState);
        
        mOkWasPressed = true;
        super.okPressed();
    }
        
    /**
     * Utility method that creates a push button instance
     * and sets the default layout data.
     *
     * @param parent  the parent for the new button
     * @param label  the label for the new button
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

	private void addFileMatchPattern()
	{
		FileMatchPatternEditDialog dialog = 
            new FileMatchPatternEditDialog(mComposite.getShell(), null);
		dialog.open();
		if (dialog.okWasPressed())
		{
			String patternString = dialog.getPattern();
            try
            {
                FileMatchPattern pattern = new FileMatchPattern(patternString);
                mFileMatchPatterns.add(pattern);
                mViewer.refresh();
                mViewer.setChecked(pattern, pattern.isIncludePattern());
            }
            catch (CheckstylePluginException e)
            {
                CheckstyleLog.error("Failed to create FileMatchPattern object <"
                                + patternString +">, " + e.getMessage(), e);
                CheckstyleLog.internalErrorDialog();
            }
		}
	}

    private void editFileMatchPattern()
    {
        IStructuredSelection selection = (IStructuredSelection)mViewer.getSelection();
        FileMatchPattern pattern = (FileMatchPattern)selection.getFirstElement();
        if (pattern == null)
        {
            //
            //  Nothing is selected.
            //
            return;
        }
        
        FileMatchPatternEditDialog dialog = 
            new FileMatchPatternEditDialog(mComposite.getShell(), pattern.getMatchPattern());
        dialog.open();
        if (dialog.okWasPressed())
        {
            String patternString = dialog.getPattern();
            try
            {
                FileMatchPattern editedPattern = new FileMatchPattern(patternString);
                mFileMatchPatterns.remove(pattern);
                mFileMatchPatterns.add(editedPattern);                
                mViewer.refresh();
                mViewer.setChecked(editedPattern, editedPattern.isIncludePattern());
            }
            catch (CheckstylePluginException e)
            {
                CheckstyleLog.error("Failed to create FileMatchPattern object <"
                                + patternString +">, " + e.getMessage(), e);
                CheckstyleLog.internalErrorDialog();
            }
        }
    }

    private void removeFileMatchPattern()
    {
        IStructuredSelection selection = (IStructuredSelection)mViewer.getSelection();
        FileMatchPattern pattern = (FileMatchPattern)selection.getFirstElement();
        if (pattern == null)
        {
            //
            //  Nothing is selected.
            //
            return;
        }
        
        mFileMatchPatterns.remove(pattern);
        mViewer.refresh();
    }
    
    private void changeIncludeState(CheckStateChangedEvent event)
    {
        if (event.getElement() instanceof FileMatchPattern)
        {
            FileMatchPattern pattern = (FileMatchPattern)event.getElement();
            pattern.setIsIncludePattern(event.getChecked());
            mViewer.refresh();
        }
        else
        {
            CheckstyleLog.warning("Checked element in FileMatchPattern table not"
                              +"  a FileMatchPattern");
        }
    }
    
    /**
     *  Test the file set to see what files it matches in the project.
     */
	private void testFileMatchPattern()
	{
		List includedFiles = new LinkedList();
		try
		{
			List files = getFiles(mProject);
			Iterator iter = files.iterator();
            FileSet temp = new FileSet(null, null);
            temp.setFileMatchPatterns(mFileMatchPatterns);
			while (iter.hasNext())
			{
				IFile file = (IFile) iter.next();
				if (temp.includesFile(file))
				{
					includedFiles.add(file);
				}
			}
		}
		catch (CheckstylePluginException e)
		{
			CheckstyleLog.error("Failed to generate FileSet test results", e);
			CheckstyleLog.internalErrorDialog();
            return;
		}
		catch (CoreException e)
		{
            CheckstyleLog.error("Failed to generate FileSet test results", e);
            CheckstyleLog.internalErrorDialog();
            return;
		}

		TestResultsDialog dialog = new TestResultsDialog(mComposite.getShell(), includedFiles);
		dialog.open();
	}

    private void upFileMatchPattern()
    {
        IStructuredSelection selection = (IStructuredSelection)mViewer.getSelection();
        FileMatchPattern pattern = (FileMatchPattern)selection.getFirstElement();
        if (pattern == null)
        {
            //
            //  Nothing is selected.
            //
            return;
        }
        
        int index = mFileMatchPatterns.indexOf(pattern);
        if (index > 0)
        {
            mFileMatchPatterns.remove(pattern);
            mFileMatchPatterns.add(index-1, pattern);
            mViewer.refresh();
        }        
    }

    private void downFileMatchPattern()
    {
        IStructuredSelection selection = (IStructuredSelection)mViewer.getSelection();
        FileMatchPattern pattern = (FileMatchPattern)selection.getFirstElement();
        if (pattern == null)
        {
            //
            //  Nothing is selected.
            //
            return;
        }
        
        int index = mFileMatchPatterns.indexOf(pattern);
        if ((index >= 0)  && (index < mFileMatchPatterns.size()-1))
        {
            mFileMatchPatterns.remove(pattern);
            if (index < mFileMatchPatterns.size()-1)
            {
                mFileMatchPatterns.add(index+1, pattern);
            }
            else
            {
                mFileMatchPatterns.add(pattern);
            }
            
            mViewer.refresh();
        }        
    }
    
    public boolean okWasPressed()
    {
        return mOkWasPressed;
    }
    
    public FileSet getFileSet()
    {
        return mFileSet;
    }

    final private List getFiles(IContainer container)
        throws CoreException
    {
        LinkedList files   = new LinkedList();
        LinkedList folders = new LinkedList();
        
        IResource children[] = container.members();
        for (int i = 0; i < children.length; i++)
        {
            IResource child = children[i];
            int childType = child.getType();
            if (childType == IResource.FILE)
            {
                if (child.getName().endsWith(JAVA_SUFFIX))
                {
                    files.add(child);
                }
            }
            else if (childType == IResource.FOLDER)
            {
                folders.add(child);
            }
        }
        
        //
        //  Get the files from the sub-folders.
        //
        Iterator iter = folders.iterator();
        while (iter.hasNext())
        {
            files.addAll(getFiles((IContainer)iter.next()));
        }
        
        return files;
    }
    
	/**
	 *  Over-rides method from Window to configure the 
	 *  shell (e.g. the enclosing window).
	 */
	protected void configureShell(Shell shell)
	{
		super.configureShell(shell);
		shell.setText("Checkstyle File Set Editor");
	}
}
