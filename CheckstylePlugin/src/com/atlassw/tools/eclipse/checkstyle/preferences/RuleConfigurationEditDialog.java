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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.atlassw.tools.eclipse.checkstyle.config.ConfigProperty;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigPropertyMetadata;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigPropertyType;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

//=================================================
// Imports from org namespace
//=================================================
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;


/**
 * Edit dialog for property values.
 */
public class RuleConfigurationEditDialog extends Dialog
{
	//=================================================
	// Public static final variables.
	//=================================================

	//=================================================
	// Static class variables.
	//=================================================
    
    private static final int MAX_INPUT_LENGTH = 40;

    private static final String[] SEVERITY_LABELS =
	{
		SeverityLevel.IGNORE.getName(),
		SeverityLevel.INFO.getName(),
		SeverityLevel.WARNING.getName(),
		SeverityLevel.ERROR.getName()
    };

	//=================================================
	// Instance member variables.
	//=================================================

    private Composite                 mParentComposite;

    private RuleConfigWorkingCopy     mRule;

    private RuleConfigWorkingCopy     mFinalRule;

    private Text                      mCommentText;
    
    private Combo                     mSeverityCombo;
    
    private ConfigPropertyWidget[]    mConfigPropertyWidgets;
    
    private boolean                  mOkWasPressed = false;

	//=================================================
	// Constructors & finalizer.
	//=================================================

	/**
	 * Constructor
	 * 
	 * @param parent     Parent shell.
	 * 
	 * @param filter     Filter being edited.
	 */
	public RuleConfigurationEditDialog(Shell parent, RuleConfigWorkingCopy rule)
        throws CheckstylePluginException
	{
		super(parent);
		mRule = rule;
        try
        {
            mFinalRule = (RuleConfigWorkingCopy)rule.clone();
        }
        catch (CloneNotSupportedException e)
        {
            CheckstyleLog.error("Failed to clone RuleConfigWorkingCopy", e);
            throw new CheckstylePluginException("Failed to clone RuleConfigWorkingCopy");
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
		Composite composite = (Composite) super.createDialogArea(parent);

		Composite dialog = new Composite(composite, SWT.NONE);
        mParentComposite = dialog;
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		dialog.setLayout(layout);
        
        createRuleNameLable(dialog);
        createComment(dialog);
        createSeveritySelection(dialog);
        createConfigPropertyEntries(dialog);

		dialog.layout();
		return composite;
	}

	/**
	 *  OK button was selected.
	 */
	protected void okPressed()
	{
        //
        //  Get the selected severity level.
        //
        SeverityLevel severity = mRule.getSeverityLevel();
        try
        {
            String severityLabel = mSeverityCombo.getItem(mSeverityCombo.getSelectionIndex());
            severity = SeverityLevel.getInstance(severityLabel);
        }
        catch (IllegalArgumentException e)
        {
            CheckstyleLog.warning("Invalid severity level found, using original value", e);
        }
        
        //
        //  Get the comment.
        //
        String comment = mCommentText.getText();
        
        //
        //  Build a new collection of configuration properties.
        //
        //  Note: if the rule does not have any configuration properties then
        //        skip over the populating of the config property hash map.
        //
        HashMap configProps = new HashMap();
        if (mConfigPropertyWidgets != null)
        {
            for (int i = 0; i < mConfigPropertyWidgets.length; i++)
            {
                ConfigPropertyWidget widget = mConfigPropertyWidgets[i];
                ConfigProperty property = buildConfigProperty(widget);
                if (property == null)
                {
                    return;
                }
                else
                {
                    configProps.put(property.getName(), property);
                }
            }
            
        }
        
        //
        //  If we made it this far then all of the user input validated and we can
        //  update the final rule with the values the user entered.
        //
        mFinalRule.setConfigItems(configProps);
        mFinalRule.setSeverityLevel(severity);
        mFinalRule.setRuleComment(comment);
        
        mOkWasPressed = true;
		super.okPressed();
	}
    
    private ConfigProperty buildConfigProperty(ConfigPropertyWidget widget)
    {
        String value = widget.getValue();
        boolean isValid = validatePropertyValue(value, widget.getMetadata());
        if (!isValid)
        {
            String message = "Invalid value for property " 
                             + widget.getMetadata().getName();
            MessageDialog.openError(mParentComposite.getShell(), 
                                    "Invalid Property Value",
                                    message);
            return null;
        }
        
        ConfigProperty prop = new ConfigProperty(widget.getMetadata().getName(), value);
        return prop;
    }

    private void createRuleNameLable(Composite parent)
    {
        Label label = new Label(parent, SWT.NULL);
        label.setText("Rule: " + mRule.getRuleName());
    }

    private void createComment(Composite parent)
    {
        Composite comp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = 0;
        comp.setLayout(layout);

        Label commentLabel = new Label(comp, SWT.NULL);
        commentLabel.setText("Comment:");

        mCommentText = new Text(comp, SWT.SINGLE | SWT.BORDER);
        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = true;
        data.verticalAlignment = GridData.CENTER;
        data.grabExcessVerticalSpace = false;
        data.widthHint = convertWidthInCharsToPixels(MAX_INPUT_LENGTH);
        data.heightHint = convertHeightInCharsToPixels(1);
        mCommentText.setLayoutData(data);
        mCommentText.setFont(parent.getFont());
        String comment = mRule.getRuleComment();
        if (comment != null)
        {
            mCommentText.setText(comment);
        }
    }
    
    private void createSeveritySelection(Composite parent)
    {
        Composite comp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = 0;
        comp.setLayout(layout);
        
        Label label = new Label(comp, SWT.NULL);
        label.setText("Severity: ");
                    
        mSeverityCombo = new Combo(comp, SWT.NONE | SWT.DROP_DOWN | SWT.READ_ONLY);
        mSeverityCombo.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        mSeverityCombo.setItems(SEVERITY_LABELS);
        mSeverityCombo.select(severityToComboPosition(mRule.getSeverityLevel()));
    }
    
    private void createConfigPropertyEntries(Composite parent)
    {
        List configItemMetadata = mRule.getConfigItemMetadata();
        if (configItemMetadata.size() <= 0)
        {
            return;
        }
        
        Label label = new Label(parent, SWT.NULL);
        label.setText("Properties:");

        Composite comp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 4;
        layout.marginWidth = 0;
        comp.setLayout(layout);
        
        mConfigPropertyWidgets = new ConfigPropertyWidget[configItemMetadata.size()];
        Iterator iter = configItemMetadata.iterator();
        for (int i = 0; iter.hasNext(); i++)
        {
            ConfigPropertyMetadata cfgPropMetadata = (ConfigPropertyMetadata)iter.next();
            ConfigProperty prop = 
                (ConfigProperty)mRule.getConfigProperty(cfgPropMetadata.getName());
            
			//
            //  Add an input widget for the properties value.
            //
            mConfigPropertyWidgets[i] =
                ConfigPropertyWidgetFactory.createWidget(comp, cfgPropMetadata, prop);
        }
    }
    
    private int severityToComboPosition(SeverityLevel severity)
    {
        int result = 0;
        
        String label = severity.getName();
        for (int i = 0; i < SEVERITY_LABELS.length; i++)
        {
            if (label.equals(SEVERITY_LABELS[i]))
            {
                result = i;
                break;
            }
        }
        
        return result;
    }
    
    RuleConfigWorkingCopy getFinalRule()
    {
        return mFinalRule;
    }
    
    
    private boolean validatePropertyValue(String value, ConfigPropertyMetadata metadata)
    {
        boolean result = true;
        if (value == null)
        {
            return false;
        }
        
        //
        //  What datatype is this property?
        //
        ConfigPropertyType type = metadata.getDatatype();
        if (type.equals(ConfigPropertyType.STRING))
        {
            //
            //  Anything, including nothing, is valid.
            //
            result = true;
        }
        else if (type.equals(ConfigPropertyType.INTEGER))
        {
            try
            {
                //
                //  Parse the value to see if an exception gets thrown.
                //
                Integer.parseInt(value);
            }
            catch (NumberFormatException e)
            {
                //
                //  If an exception was thrown then consider the value to be invalid.
                //
                result = false;
            }
        }
        else if (type.equals(ConfigPropertyType.SINGLE_SELECT))
        {
            //  Assume valid since the user can't enter a value.
        }
        else
        {
            CheckstyleLog.warning("Unknown property type: " + type.toString());
        }
        
        return result;
    }
    
    public boolean okWasPressed()
    {
        return mOkWasPressed;
    }
    
    /**
     *  Over-rides method from Window to configure the 
     *  shell (e.g. the enclosing window).
     */
    protected void configureShell(Shell shell)
    {
        super.configureShell(shell);
        shell.setText("Checkstyle Rule Configuration Editor");
    }
}