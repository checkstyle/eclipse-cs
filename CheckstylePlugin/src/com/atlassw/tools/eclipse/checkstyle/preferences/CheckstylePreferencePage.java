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
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.builder.CheckstyleBuilder;
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfigConverter;
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfigurationFactory;
import com.atlassw.tools.eclipse.checkstyle.config.FileSetFactory;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;

//=================================================
// Imports from org namespace
//=================================================
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;


/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */
public class CheckstylePreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage
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

    private Composite        mParentComposite;

	private TableViewer      mViewer;

	private Button           mAddButton;

	private Button           mEditButton;

	private Button           mRemoveButton;

	private Button           mImportPluginButton;

    private Button           mExportPluginButton;

	private Button           mImportCheckstyleButton;

	private Button           mExportCheckstyleButton;
    
    private List             mAuditConfigurations;
    
    private boolean          mNeedRebuild = false;
        
    //=================================================
    // Constructors & finalizer.
    //=================================================
  
  	public CheckstylePreferencePage()
	{
		super();
		setPreferenceStore(CheckstylePlugin.getDefault().getPreferenceStore());
		setDescription("Defined Check Configurations");
		initializeDefaults();
	}

    //=================================================
    // Methods.
    //=================================================
    
	/**
	 * Sets the default values of the preferences.
	 */
	private void initializeDefaults()
	{}

	/**
	 * Creates the page contents.
	 */
	public Control createContents(Composite ancestor)
	{
		noDefaultAndApplyButton();
		Composite parent = new Composite(ancestor, SWT.NULL);
		mParentComposite = parent;

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		parent.setLayout(layout);
        
        try
        {
            initializeCheckConfigs();
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error("Failed to create preferences window, " + e.getMessage(), e);
            CheckstyleLog.internalErrorDialog();
            return ancestor;
        }

		Table table = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);

		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = convertWidthInCharsToPixels(80);
		data.heightHint = convertHeightInCharsToPixels(10);
		table.setLayoutData(data);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);

		TableColumn column1 = new TableColumn(table, SWT.NULL);
		column1.setText("Check Configuration");

		tableLayout.addColumnData(new ColumnWeightData(40));

		mViewer = new TableViewer(table);

		mViewer.setLabelProvider(new CheckConfigurationLabelProvider());
		mViewer.setContentProvider(new CheckConfigurationProvider());
		mViewer.setSorter(new CheckConfigurationViewerSorter());
        mViewer.setInput(mAuditConfigurations);

		Composite rightButtons = new Composite(parent, SWT.NULL);
		rightButtons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		rightButtons.setLayout(layout);

		mAddButton = createPushButton(rightButtons, "Add...");
		mAddButton.addListener(SWT.Selection, new Listener()
		{
			public void handleEvent(Event evt)
			{
				addCheckConfig(null);
			}
		});

		mEditButton = createPushButton(rightButtons, "Edit...");
		mEditButton.addListener(SWT.Selection, new Listener()
		{
			public void handleEvent(Event evt)
			{
				editCheckConfig();
			}
		});

		mRemoveButton = createPushButton(rightButtons, "Remove");
		mRemoveButton.addListener(SWT.Selection, new Listener()
		{
			public void handleEvent(Event evt)
			{
				removeCheckConfig();
			}
		});

        Composite bottomButtons = new Composite(parent, SWT.NULL);
        bottomButtons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.numColumns = 2;
        bottomButtons.setLayout(layout);

		mImportPluginButton = createPushButton(bottomButtons, "Import Plugin Config ...");
		mImportPluginButton.addListener(SWT.Selection, new Listener()
		{
			public void handleEvent(Event evt)
			{
				importPluginCheckConfig();
			}
		});

        mExportPluginButton = createPushButton(bottomButtons, "Export Plugin Config ...");
        mExportPluginButton.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event evt)
            {
                exportPluginCheckConfig();
            }
        });

		mExportCheckstyleButton = createPushButton(bottomButtons, "Export Checkstyle Config ...");
		mExportCheckstyleButton.addListener(SWT.Selection, new Listener()
		{
			public void handleEvent(Event evt)
			{
				exportCheckstyleCheckConfig();
			}
		});

		mImportCheckstyleButton = createPushButton(bottomButtons, "Import Checkstyle Config ...");
		mImportCheckstyleButton.addListener(SWT.Selection, new Listener()
		{
			public void handleEvent(Event evt)
			{
				importCheckstyleCheckConfig();
			}
		});

		mViewer.addDoubleClickListener(new IDoubleClickListener()
		{
			public void doubleClick(DoubleClickEvent e)
			{
				editCheckConfig();
			}
		});

		return parent;
	}

	public void init(IWorkbench workbench)
	{}
    
    public boolean performOk()
    {
        try
        {
            CheckConfigurationFactory.setCheckConfigurations(mAuditConfigurations);
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error("Failed to save AuditConfigurations, " 
                            + e.getMessage(), e);
            CheckstyleLog.internalErrorDialog();
        }
        
        if (mNeedRebuild)
        {
            try
            {
                CheckstyleBuilder.buildAllProjects(getShell());
            }
            catch (CheckstylePluginException e)
            {
                CheckstyleLog.error("Failed to rebuild projects, " 
                            + e.getMessage(), e);
                CheckstyleLog.internalErrorDialog();
            }
        }
        
        return true;
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
        Button button = new Button(parent, SWT.PUSH | SWT.WRAP);
        button.setText(label);
        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        button.setLayoutData(data);
        return button;
    }

	private void addCheckConfig(CheckConfiguration configToAdd)
	{
        CheckConfigurationEditDialog dialog = null;
        try
        {
		    dialog = new CheckConfigurationEditDialog(mParentComposite.getShell(), configToAdd);
		    dialog.open();
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error("Failed to open CheckConfigurationEditDialog, " 
                                    + e.getMessage(), e);
            CheckstyleLog.internalErrorDialog();
            return;
        }

		if (dialog.okWasPressed())
		{
			CheckConfiguration auditConfig = dialog.getFinalConfiguration();
			if (auditConfig != null)
			{
                mAuditConfigurations.add(auditConfig);
				mViewer.refresh();
			}
			else
			{
                CheckstyleLog.error("New check configuration is null");
                CheckstyleLog.internalErrorDialog();
			}
		}
	}

    private void editCheckConfig()
    {
        IStructuredSelection selection = (IStructuredSelection)mViewer.getSelection();
        CheckConfiguration auditConfig = (CheckConfiguration)selection.getFirstElement();
        if (auditConfig == null)
        {
            //
            //  Nothing is selected.
            //
            return;
        }
        
        CheckConfigurationEditDialog dialog = null;
        try
        {
            dialog = new CheckConfigurationEditDialog(mParentComposite.getShell(),
                                                      auditConfig);
            dialog.open();
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error("Failed to open AuditConfigurationEditDialog, " 
                                    + e.getMessage(), e);
            CheckstyleLog.internalErrorDialog();
            return;
        }
        
        if (dialog.okWasPressed())
        {
            CheckConfiguration editedConfig = dialog.getFinalConfiguration();
            if (editedConfig != null)
            {
                mAuditConfigurations.remove(auditConfig);
                mAuditConfigurations.add(editedConfig);
                try
                {
                    if (FileSetFactory.isCheckConfigInUse(editedConfig.getConfigName()))
                    {
                        mNeedRebuild = true;
                    }
                }
                catch (CheckstylePluginException e)
                {
                    //
                    //  Assume its in use.
                    //
                    mNeedRebuild = true;
                    CheckstyleLog.warning("Exception while checking for audit config use", e);
                }
                mViewer.refresh();
            }
            else
            {
                CheckstyleLog.error("Edited audit configuration is null");
                CheckstyleLog.internalErrorDialog();
            }
        }
    }

	private void removeCheckConfig()
	{
		IStructuredSelection selection = (IStructuredSelection) mViewer.getSelection();
		CheckConfiguration auditConfig = (CheckConfiguration) selection.getFirstElement();
		if (auditConfig == null)
		{
			//
			//  Nothing is selected.
			//
			return;
		}

		//
		//  Make sure the audit config is not in use.  Don't let it be
		//  deleted if it is.
		//
		try
		{
			if (FileSetFactory.isCheckConfigInUse(auditConfig.getConfigName()))
			{
				MessageDialog.openInformation(mParentComposite.getShell(),
                                              "Can't Delete",
                                              "The Audit Configuration '"
                                              + auditConfig.getConfigName() 
                                              + "' is currently in use by a project."
                                              + "  It must be removed from all project "
                                              + "configurations before it can be deleted.");
                return;
			}
		}
		catch (CheckstylePluginException e)
		{
			CheckstyleLog.warning("Exception while checking for audit config use", e);
            CheckstyleLog.internalErrorDialog();
            return;
		}

		boolean confirm = MessageDialog.openQuestion(mParentComposite.getShell(),
				                                      "Confirm Delete",
				                                      "Remove audit configuration '" 
                                                      + auditConfig.getConfigName() + "'?");
		if (confirm)
		{
			mAuditConfigurations.remove(auditConfig);
			mViewer.refresh();
		}
	}

    private void importPluginCheckConfig()
    {
        FileDialog dialog= new FileDialog(getShell());
        dialog.setText("Import Plug-in Check Configuration");
        String path = dialog.open();
        
        if (path == null)
        {
            return;
        }
        
        File checkConfigFile = new File(path);
        try
        {
            List newConfigs = 
                CheckConfigurationFactory.importPluginCheckConfigurations(checkConfigFile);
            Iterator iter = newConfigs.iterator();
            while (iter.hasNext())
            {
			    addCheckConfig((CheckConfiguration)iter.next());
            }
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error("Failed to import CheckConfigurations from external file");
            CheckstyleLog.internalErrorDialog();
        }
    }

    private void exportPluginCheckConfig()
    {
        List configs = new LinkedList();
        IStructuredSelection selection = (IStructuredSelection)mViewer.getSelection();
        Iterator iter = selection.iterator();
        while (iter.hasNext())
        {
            CheckConfiguration cfg = (CheckConfiguration)iter.next();
            configs.add(cfg);
        }
        
        FileDialog dialog = new FileDialog(getShell());
        dialog.setText("Export Plug-in Check Configuration");
        String path = dialog.open();
        if (path == null)
        {
            return;
        }
        File file = new File(path);

        try
        {
            CheckConfigurationFactory.exportPluginCheckConfigurations(file, configs);
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error("Failed to export CheckConfigurations to external file");
            MessageDialog.openError(mParentComposite.getShell(),
                    "Checkstyle Error",
                    "Failed to export CheckConfigurations to external file");
        }
    }

    private void exportCheckstyleCheckConfig()
    {
        List configs = new LinkedList();
        IStructuredSelection selection = (IStructuredSelection)mViewer.getSelection();
        Iterator iter = selection.iterator();
        while (iter.hasNext())
        {
            CheckConfiguration cfg = (CheckConfiguration)iter.next();
            configs.add(cfg);
        }
        
        FileDialog dialog = new FileDialog(getShell());
        dialog.setText("Export Checkstyle Check Configuration");
        String path = dialog.open();
        if (path == null)
        {
            return;
        }
        File file = new File(path);

        try
        {
            CheckConfigurationFactory.exportCheckstyleCheckConfigurations(file, configs);
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error("Failed to export CheckConfigurations to external file");
            MessageDialog.openError(mParentComposite.getShell(),
                    "Checkstyle Error",
                    "Failed to export CheckConfigurations to external file");
        }
    }

	private void importCheckstyleCheckConfig()
	{
		//
		//  Get the full path to the file to be imported.
		//
		FileDialog fileDialog = new FileDialog(getShell());
		fileDialog.setText("Import Checkstyle Check Configuration");
		String path = fileDialog.open();
		if (path == null)
		{
			return;
		}
		
        try
        {
        	//
        	//  Load the config file.
        	//
        	CheckConfigConverter converter = new CheckConfigConverter();
        	converter.loadConfig(path);
        	
        	//
        	//  Resolve property values.
        	//
        	List resolveProps = converter.getPropsToResolve();
        	if (resolveProps.size() > 0)
        	{
        	    ResolvePropertyValuesDialog resolveDialog = 
        	        new ResolvePropertyValuesDialog(getShell(), resolveProps);
			    resolveDialog.open();
        	}
            
            //
            //  Get a CheckConfiguration from the converter.
            //
            CheckConfiguration config = converter.getCheckConfiguration();
            
            //
            //  Add the config using the add dialog so the user can see what it looks like,
            //  make changes, and it will be validated.
            //
			addCheckConfig(config);
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error("Failed to import CheckConfigurations from external file");
            CheckstyleLog.internalErrorDialog();
        }
	}
    
    /**
     *  Make a local working copy of the Check Configurations in case
     *  the user exits the preferences window with the cancel button.
     *  In this case all edits will be discarded, otherwise they are
     *  saved when the user presses the OK button.
     */
    private void initializeCheckConfigs() throws CheckstylePluginException
    {
        mAuditConfigurations = new LinkedList();
        try
        {
            List configs = CheckConfigurationFactory.getCheckConfigurations();
            Iterator iter = configs.iterator();
            while (iter.hasNext())
            {
                CheckConfiguration cfg = (CheckConfiguration)iter.next();
                CheckConfiguration clone = (CheckConfiguration)cfg.clone();
                mAuditConfigurations.add(clone);
            }
        }
        catch (CloneNotSupportedException e)
        {
            CheckstyleLog.error("Failed to clone AuditConfiguration", e);
            throw new CheckstylePluginException("Failed to clone AuditConfiguration, "
                                       + e.getMessage());
        }
    }
}