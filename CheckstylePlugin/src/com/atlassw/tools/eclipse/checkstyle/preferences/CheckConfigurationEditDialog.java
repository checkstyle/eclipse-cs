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

package com.atlassw.tools.eclipse.checkstyle.preferences;

//=================================================
// Imports from java namespace
//=================================================
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfigurationFactory;
import com.atlassw.tools.eclipse.checkstyle.config.FileSetFactory;
import com.atlassw.tools.eclipse.checkstyle.config.RuleConfiguration;
import com.atlassw.tools.eclipse.checkstyle.config.RuleMetadata;
import com.atlassw.tools.eclipse.checkstyle.config.MetadataFactory;
import com.atlassw.tools.eclipse.checkstyle.config.RuleGroupMetadata;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;

//=================================================
// Imports from org namespace
//=================================================
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;

/**
 * Edit dialog for property values.
 */
public class CheckConfigurationEditDialog extends Dialog
{
	//=================================================
	// Public static final variables.
	//=================================================

	//=================================================
	// Static class variables.
	//=================================================

	private static final int MAX_LENGTH = 40;

	//=================================================
	// Instance member variables.
	//=================================================

    private Composite              mParentComposite;

	private CheckConfiguration     mCheckConfiguration;

	private TabFolder              mTabFolder;

	private TabItem[]              mTabItems;

	private TableViewer[]          mTableViewers;

	private Text                   mConfigNameText;
    
    private TextViewer             mRuleDescriptionText;
    
    private List[]                 mRuleConfigWorkingCopies;
    
    private Button                 mConfigureButton;
    
    private RuleConfigWorkingCopy  mCurrentSelection;
    
    private String                 mCheckConfigName;
    
    private boolean                mOkWasPressed = false;
    
    private List                   mCurrentCheckConfigs;

	//=================================================
	// Constructors & finalizer.
	//=================================================

	/**
	 * Constructor
	 * 
	 * @param parent        Parent shell.
	 * 
	 * @param checkConfig   Check configuration being edited.
	 * 
	 * @param currentCheckConfigs  List of the current check configs defined.
     * 
     * @throws CheckstyleException  Error during processing.
	 */
	public CheckConfigurationEditDialog(Shell parent,
	                                    CheckConfiguration checkConfig,
	                                    List currentCheckConfigs)
		throws CheckstylePluginException
	{
		super(parent);
		
		//
		//  Remimber the list of current check configs.  These are checked to make
		//  sure the check config name entered via the editor is unique.
		//
		mCurrentCheckConfigs = currentCheckConfigs;
		
		//
		//  If we were given an existing audit configuration then make a clone
		//  of it, otherwise create a new one.  A clone is used so that the
		//  original is not modified in case the where the user makes some
		//  changes and then selects the cancel button.
		//
		if (checkConfig == null)
		{
			mCheckConfiguration = CheckConfigurationFactory.getNewInstance();
		}
		else
		{
			try
			{
				mCheckConfiguration = (CheckConfiguration)checkConfig.clone();
			}
			catch (CloneNotSupportedException e)
			{
                String msg = "Failed to clone AuditConfiguration";
				CheckstyleLog.error(msg, e);
				throw new CheckstylePluginException(msg);
			}
		}

		buildRuleConfigWorkingCopies(parent);
	}

	//=================================================
	// Methods.
	//=================================================
    
    private void buildRuleConfigWorkingCopies(Shell parent)
    {
        //
        //  Create working copies for each rule group.
        //
        List groups = MetadataFactory.getRuleGroupMetadata();
        mRuleConfigWorkingCopies = new LinkedList[groups.size()];
        for (int i = 0; i < groups.size(); i++)
        {
            mRuleConfigWorkingCopies[i] = new LinkedList();
        }
        
        //
        //  If an existing audit configuration was specified update the
        //  working copy lists with the existing values.
        //
        if (mCheckConfiguration != null)
        {
            Iterator iter = mCheckConfiguration.getRuleConfigs().iterator();
            while (iter.hasNext())
            {
                RuleConfiguration ruleConfig = (RuleConfiguration)iter.next();
                try
                {
                    ruleConfig = (RuleConfiguration)ruleConfig.clone();
                }
                catch (CloneNotSupportedException e)
                {
                    CheckstyleLog.warning("Failed to clone RuleConfiguration");
                    CheckstyleLog.internalErrorDialog();
                }
                
                RuleMetadata metadata = getRuleMetadata(ruleConfig);
                RuleConfigWorkingCopy copy = new RuleConfigWorkingCopy(metadata, ruleConfig);
                int groupIndex = metadata.getGroupIndex();
                if ((groupIndex >= 0) && (groupIndex < mRuleConfigWorkingCopies.length))
                {
                    mRuleConfigWorkingCopies[groupIndex].add(copy);
                }
                else
                {
                	String msg = "Invalid group index for check rule, ignoring rule. class="
                	             + ruleConfig.getImplClassname();
					CheckstyleLog.warning(msg);
                }
            }
        }
    }

	/**
	 * @see Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = (Composite)super.createDialogArea(parent);

		Composite dialog = new Composite(composite, SWT.NONE);
        mParentComposite = dialog;
        
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		dialog.setLayout(layout);

		buildConfigNameField(dialog);
		buildRuleGroupTabs(dialog);
        buildButtons(dialog);
        buildDescriptionArea(dialog);

		dialog.layout();
		return composite;
	}

	/**
	 * OK button was selected.
	 */
	protected void okPressed()
	{
        String checkConfigName = mConfigNameText.getText().trim();
        
        //
        //  Make sure a name was entered for the check configuration.
        //
        if ((checkConfigName == null) || (checkConfigName.length() == 0))
        {
            MessageDialog.openError(mParentComposite.getShell(), 
                                    "Invalid Value",
                                    "An check configuration name must be entered");
            return;
        }
        
        //
        //  See if the name was modified.
        //
		if (!checkConfigName.equals(mCheckConfiguration.getConfigName()))
		{
            //
            //  The name was changed.
            //  Make sure the name is not already in use by another check configuration.
            //  Check configuration names must be unique within the workspace.
            //
			boolean nameInUse = isNameInUse(checkConfigName);
			if (nameInUse)
			{
				MessageDialog.openError(
					mParentComposite.getShell(),
					"Invalid Value",
					"The check configuration name '"
						+ checkConfigName
						+ "' is already in use, please choose a different name.");
				return;
			}
            
            //
            //  Make sure the orignal name is not part of an existing file set.
            //
            try
            {
                nameInUse = 
                    FileSetFactory.isCheckConfigInUse(mCheckConfiguration.getConfigName());
            }
            catch (CheckstylePluginException e)
            {
				CheckstyleLog.warning("Error checking CheckConfiguration name in use", e);
				CheckstyleLog.internalErrorDialog(mParentComposite.getShell());
            }
            if (nameInUse)
            {
                MessageDialog.openError(
                    mParentComposite.getShell(),
                    "Invalid Value",
                    "The check configuration name '"
                        + checkConfigName
                        + "' is referenced by a project File Set and can not be changed.");
                return;
            }
		}
        mCheckConfigName = checkConfigName;
        
        mOkWasPressed = true;
		super.okPressed();
	}
    
    /**
     *  Get the final <code>CheckConfiguration</code> after all edits completed.
     *  The object returned here is only valid if the <code>okPressed()</code>
     *  method returned <code>true</code>.
     * 
     *  @return  The final <code>CheckConfiguration</code> object.
     */
	CheckConfiguration getFinalConfiguration()
	{
        mCheckConfiguration.setName(mCheckConfigName);
        
        List rules = new LinkedList();
        for (int i = 0; i < mRuleConfigWorkingCopies.length; i++)
        {
            Iterator iter = mRuleConfigWorkingCopies[i].iterator();
            while (iter.hasNext())
            {
                RuleConfigWorkingCopy workingCopy = (RuleConfigWorkingCopy)iter.next();
                RuleConfiguration ruleConfig = workingCopy.getRuleConfig();
                rules.add(ruleConfig);
            }
        }
        mCheckConfiguration.setRuleConfigs(rules);
        
		return mCheckConfiguration;
	}

	private void buildConfigNameField(Composite parent)
	{
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		comp.setLayout(layout);

		Label configNameLabel = new Label(comp, SWT.NULL);
		configNameLabel.setText("Audit Configuration Name:");

        mConfigNameText = new Text(comp, SWT.SINGLE | SWT.BORDER);
        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = true;
        data.verticalAlignment = GridData.CENTER;
        data.grabExcessVerticalSpace = false;
        data.widthHint = convertWidthInCharsToPixels(MAX_LENGTH);
        data.heightHint = convertHeightInCharsToPixels(1);
        mConfigNameText.setLayoutData(data);
        mConfigNameText.setFont(parent.getFont());
        String name = mCheckConfiguration.getConfigName();
        if (name != null)
        {
            mConfigNameText.setText(name);
        }
	}

	private void buildRuleGroupTabs(Composite parent)
	{
		mTabFolder = new TabFolder(parent, SWT.NONE);
		mTabFolder.setLayout(new TabFolderLayout());

		List ruleGroups = MetadataFactory.getRuleGroupMetadata();
		mTabItems = new TabItem[ruleGroups.size()];
		mTableViewers = new TableViewer[ruleGroups.size()];
        
        Iterator iter = ruleGroups.iterator();
		for (int index = 0; iter.hasNext(); index++)
		{
			//
			//  Create the tab item.
			//
			TabItem tab = new TabItem(mTabFolder, SWT.NONE);
			mTabItems[index] = tab;

			//
			//  Set the tab label.
			//
			RuleGroupMetadata ruleGroup = (RuleGroupMetadata)iter.next();
			tab.setText(ruleGroup.getGroupName());

			//
			//  Create the composite inside the tab item.
			//
			Composite comp = createTabItemComposite(mTabFolder, ruleGroup, index);
			tab.setControl(comp);
		}
        
        //
        //  Set the first tab as the selected tab.
        //
		mTabFolder.setSelection(0);

        //
        //  Add a selection listener.
        //
        mTabFolder.addSelectionListener(new TabSelectionListener());
	}

	private Composite createTabItemComposite(Composite parent,
		                                      RuleGroupMetadata ruleGroup,
		                                      int index)
	{
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		comp.setLayout(layout);

		Table table = new Table(comp, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = convertWidthInCharsToPixels(80);
		data.heightHint = convertHeightInCharsToPixels(20);
		table.setLayoutData(data);

		table.addSelectionListener(new TableSelectionListener());

		TableColumn column1 = new TableColumn(table, SWT.NULL);
		column1.setText("Severity");
		tableLayout.addColumnData(new ColumnWeightData(10));

        TableColumn column2 = new TableColumn(table, SWT.NULL);
        column2.setText("Rule");
        tableLayout.addColumnData(new ColumnWeightData(30));

        TableColumn column3 = new TableColumn(table, SWT.NULL);
        column3.setText("Comment");
        tableLayout.addColumnData(new ColumnWeightData(40));

		TableViewer viewer = new TableViewer(table);
		mTableViewers[index] = viewer;

		viewer.setLabelProvider(new RuleConfigurationLabelProvider());
		viewer.setContentProvider(new RuleConfigurationProvider());
		viewer.setInput(mRuleConfigWorkingCopies[index]);
		viewer.setSorter(new RuleConfigurationViewerSorter());

        viewer.addDoubleClickListener(new IDoubleClickListener()
        {
            public void doubleClick(DoubleClickEvent e)
            {
                editRule();
            }
        });

		return comp;
	}

    private void ruleSelected(SelectionEvent e)
    {
        int tabIndex = mTabFolder.getSelectionIndex();
        TableViewer viewer = mTableViewers[tabIndex];
        IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
        mCurrentSelection = (RuleConfigWorkingCopy)selection.getFirstElement();
        
        Document doc = new Document(mCurrentSelection.getRuleDescription());
        mRuleDescriptionText.setDocument(doc);
    }

    private void tabSelected(SelectionEvent e)
    {
        int tabIndex = mTabFolder.getSelectionIndex();
        TableViewer viewer = mTableViewers[tabIndex];
        viewer.setSelection(StructuredSelection.EMPTY, true);
        
        Document doc = new Document("");
        mRuleDescriptionText.setDocument(doc);
    }
    
    private void buildButtons(Composite parent)
    {
        Composite comp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        comp.setLayout(layout);
        
        buildAddButton(comp);
        buildEditButton(comp);
        buildDeleteButton(comp);
    }
    
    private void buildAddButton(Composite parent)
    {
        mConfigureButton = new Button(parent, SWT.PUSH);
        mConfigureButton.setText("Add Rule");
        mConfigureButton.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event evt)
            {
                addRule();
            }
        });
    }
    
    private void buildEditButton(Composite parent)
    {
        mConfigureButton = new Button(parent, SWT.PUSH);
        mConfigureButton.setText("Edit Rule");
        mConfigureButton.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event evt)
            {
                editRule();
            }
        });
    }
    
    private void buildDeleteButton(Composite parent)
    {
        mConfigureButton = new Button(parent, SWT.PUSH);
        mConfigureButton.setText("Delete Rule");
        mConfigureButton.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event evt)
            {
                deleteRule();
            }
        });
    }
    
    private void buildDescriptionArea(Composite parent)
    {
        Label label = new Label(parent, SWT.NULL);
        label.setText("Rule Description");
        
        mRuleDescriptionText = new TextViewer(parent, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        
        Control control = mRuleDescriptionText.getControl();
        GridData data = new GridData(GridData.FILL_BOTH);
        data.horizontalAlignment = GridData.FILL;
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = true;
        data.verticalAlignment = GridData.CENTER;
        data.grabExcessVerticalSpace = false;
        data.widthHint = convertWidthInCharsToPixels(MAX_LENGTH);
        data.heightHint = convertHeightInCharsToPixels(5);
        control.setLayoutData(data);
    }
    
    private void addRule()
    {
        int groupIndex = mTabFolder.getSelectionIndex();
        RuleGroupMetadata groupMeta = 
            (RuleGroupMetadata)MetadataFactory.getRuleGroupMetadata().get(groupIndex);
        List ruleMetadataList = groupMeta.getRuleMetadata();
        
        if (ruleMetadataList.size() <= 0)
        {
        	return;
        }
        
        RuleSelectionDialog dialog = null;
        try
        {
            dialog = new RuleSelectionDialog(mParentComposite.getShell(),
                                             ruleMetadataList);
            dialog.open();
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error("Failed to open RuleSelectionDialog, " 
                                    + e.getMessage(), e);
            CheckstyleLog.internalErrorDialog();
            return;
        }
        
        RuleMetadata ruleMetadata = dialog.getSelectedRule();
        if (ruleMetadata != null)
        {
            mCurrentSelection = new RuleConfigWorkingCopy(ruleMetadata, null);
            editRule();
        }
    }
    
    private void editRule()
    {
        if (mCurrentSelection == null)
        {
            return;
        }
        
        RuleConfigurationEditDialog dialog = null;
        try
        {
            dialog = new RuleConfigurationEditDialog(mParentComposite.getShell(),
                                                     mCurrentSelection);
            dialog.open();
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error("Failed to open RuleConfigurationEditDialog, " 
                                    + e.getMessage(), e);
            CheckstyleLog.internalErrorDialog();
            return;
        }
        
        if (dialog.okWasPressed())
        {
            //
            //  Get the updated rule configuration.
            //
            RuleConfigWorkingCopy rule = dialog.getFinalRule();
            int groupIndex = mTabFolder.getSelectionIndex();
            
            mRuleConfigWorkingCopies[groupIndex].remove(mCurrentSelection);
            mRuleConfigWorkingCopies[groupIndex].add(rule);
            mCurrentSelection = rule;
            
            mTableViewers[groupIndex].setSelection(new StructuredSelection(rule), true);
            mTableViewers[groupIndex].refresh(true);
        }
    }
    
    private void deleteRule()
    {
        if (mCurrentSelection == null)
        {
            return;
        }
        
        boolean confirm = MessageDialog.openQuestion(mParentComposite.getShell(),
                                                      "Confirm Delete",
                                                      "Delete rule '" 
                                                      + mCurrentSelection.getRuleName() 
                                                      + "'?");
        if (confirm)
        {
            int groupIndex = mTabFolder.getSelectionIndex();
            
            mRuleConfigWorkingCopies[groupIndex].remove(mCurrentSelection);
            mCurrentSelection = null;            
            mTableViewers[groupIndex].refresh(true);
        }
    }
    
    /**
     *  Indicates if the OK button was pressed rather then the Cancel button.
     * 
     *  @return  <code>true</code> = OK button was pressed,<br>
     *            <code>false</code> = OK was not pressed.
     */
    public boolean okWasPressed()
    {
        return mOkWasPressed;
    }
    
    /**
     *  Over-rides method from Window to configure the 
     *  shell (e.g. the enclosing window).
     * 
     *  @param shell  The shell to configure.
     */
    protected void configureShell(Shell shell)
    {
        super.configureShell(shell);
        shell.setText("Checkstyle Check Configuration Editor");
    }
    
    /**
     *  Listener for selections in the table of rules per rule group.
     */
    private class TableSelectionListener implements SelectionListener
    {
        public void widgetSelected(SelectionEvent e)
        {
            ruleSelected(e);
        }

        public void widgetDefaultSelected(SelectionEvent e)
        {
            widgetSelected(e);
        }
    }
    
    /**
     *  Listener for selection of a rule group tab.
     */
    private class TabSelectionListener implements SelectionListener
    {
        public void widgetSelected(SelectionEvent e)
        {
            tabSelected(e);
        }

        public void widgetDefaultSelected(SelectionEvent e)
        {
            widgetSelected(e);
        }
    }
    
    private RuleMetadata getRuleMetadata(RuleConfiguration ruleConfig)
    {
    	return MetadataFactory.getRuleMetadata(ruleConfig);
    }
    
    private boolean isNameInUse(String name)
    {
    	boolean result = false;
    	for (Iterator iter = mCurrentCheckConfigs.iterator(); iter.hasNext();)
    	{
    		CheckConfiguration config = (CheckConfiguration)iter.next();
    		if (config.getName().equals(name))
    		{
    			result = true;
    			break;
    		}
    	}
    	return result;
    }
}