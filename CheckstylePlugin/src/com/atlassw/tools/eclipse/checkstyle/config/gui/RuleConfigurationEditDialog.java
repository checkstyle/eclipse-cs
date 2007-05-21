//============================================================================
//
// Copyright (C) 2002-2007  David Schneider, Lars Ködderitzsch
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

package com.atlassw.tools.eclipse.checkstyle.config.gui;

//=================================================
// Imports from java namespace
//=================================================
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.prefs.BackingStoreException;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.Messages;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigProperty;
import com.atlassw.tools.eclipse.checkstyle.config.Module;
import com.atlassw.tools.eclipse.checkstyle.config.gui.widgets.ConfigPropertyWidgetFactory;
import com.atlassw.tools.eclipse.checkstyle.config.gui.widgets.IConfigPropertyWidget;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginImages;
import com.atlassw.tools.eclipse.checkstyle.util.SWTUtil;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

/**
 * Edit dialog for property values.
 */
public class RuleConfigurationEditDialog extends TitleAreaDialog
{
    // =================================================
    // Public static final variables.
    // =================================================

    // =================================================
    // Static class variables.
    // =================================================

    private static final SeverityLevel[] SEVERITY_LEVELS = { SeverityLevel.IGNORE,
        SeverityLevel.INFO, SeverityLevel.WARNING, SeverityLevel.ERROR };

    // =================================================
    // Instance member variables.
    // =================================================

    private Module mRule;

    private Text mCommentText;

    private Text mIdText;

    private Text mCustomMessageText;

    private ComboViewer mSeverityCombo;

    private IConfigPropertyWidget[] mConfigPropertyWidgets;

    private Button mBtnTranslate;

    private Button mBtnSort;

    private boolean mReadonly = false;

    private String mTitle;

    // =================================================
    // Constructors & finalizer.
    // =================================================

    /**
     * Constructor.
     * 
     * @param parent Parent shell.
     * @param rule Rule being edited.
     * @throws CheckstylePluginException Error during processing.
     */
    RuleConfigurationEditDialog(Shell parent, Module rule, boolean readonly, String title)
        throws CheckstylePluginException
    {
        super(parent);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        mRule = rule;
        mReadonly = readonly;
        mTitle = title;
    }

    // =================================================
    // Methods.
    // =================================================

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent)
    {
        Composite composite = (Composite) super.createDialogArea(parent);

        Composite dialog = new Composite(composite, SWT.NONE);
        dialog.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout(2, false);
        dialog.setLayout(layout);

        // Build comment
        Label commentLabel = new Label(dialog, SWT.NULL);
        commentLabel.setText(Messages.RuleConfigurationEditDialog_lblComment);
        commentLabel.setLayoutData(new GridData());

        mCommentText = new Text(dialog, SWT.SINGLE | SWT.BORDER);
        mCommentText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label idLabel = new Label(dialog, SWT.NULL);
        idLabel.setText(Messages.RuleConfigurationEditDialog_lblId);
        idLabel.setLayoutData(new GridData());

        mIdText = new Text(dialog, SWT.SINGLE | SWT.BORDER);
        mIdText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label customMessageLabel = new Label(dialog, SWT.NULL);
        customMessageLabel.setText(Messages.RuleConfigurationEditDialog_lblCustomMessage);
        customMessageLabel.setLayoutData(new GridData());

        mCustomMessageText = new Text(dialog, SWT.SINGLE | SWT.BORDER);
        mCustomMessageText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Build severity
        Label lblSeverity = new Label(dialog, SWT.NULL);
        lblSeverity.setText(Messages.RuleConfigurationEditDialog_lblSeverity);
        lblSeverity.setLayoutData(new GridData());

        mSeverityCombo = new ComboViewer(dialog);
        mSeverityCombo.setContentProvider(new ArrayContentProvider());
        mSeverityCombo.setLabelProvider(new LabelProvider()
        {
            /**
             * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
             */
            public String getText(Object element)
            {
                return ((SeverityLevel) element).getName();
            }
        });
        mSeverityCombo.getControl().setLayoutData(new GridData());

        Group properties = new Group(dialog, SWT.NULL);
        properties.setLayout(new GridLayout(3, false));
        properties.setText(Messages.RuleConfigurationEditDialog_lblProperties);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        properties.setLayoutData(gd);

        createConfigPropertyEntries(properties);

        if (mConfigPropertyWidgets == null || mConfigPropertyWidgets.length == 0)
        {

            properties.dispose();
        }

        initialize();
        return composite;
    }

    protected Control createButtonBar(Composite parent)
    {

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        mBtnTranslate = new Button(composite, SWT.CHECK);
        mBtnTranslate.setText(Messages.RuleConfigurationEditDialog_btnTranslateTokens);
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.BEGINNING;
        gd.horizontalIndent = 5;
        mBtnTranslate.setLayoutData(gd);

        // Init the translate tokens preference
        IPreferencesService prefStore = Platform.getPreferencesService();
        mBtnTranslate.setSelection(prefStore.getBoolean(CheckstylePlugin.PLUGIN_ID,
                CheckstylePlugin.PREF_TRANSLATE_TOKENS, true, null));
        mBtnTranslate.addSelectionListener(new SelectionListener()
        {

            public void widgetSelected(SelectionEvent e)
            {
                // store translation preference
                IEclipsePreferences prefStore = new InstanceScope()
                        .getNode(CheckstylePlugin.PLUGIN_ID);
                prefStore.putBoolean(CheckstylePlugin.PREF_TRANSLATE_TOKENS, ((Button) e.widget)
                        .getSelection());
                try
                {
                    prefStore.flush();
                }
                catch (BackingStoreException e1)
                {
                    CheckstyleLog.log(e1);
                }
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {
            // NOOP
            }
        });

        mBtnSort = new Button(composite, SWT.CHECK);
        mBtnSort.setText(Messages.RuleConfigurationEditDialog_btnSortTokens);
        gd = new GridData();
        gd.horizontalAlignment = GridData.BEGINNING;
        gd.horizontalIndent = 5;
        mBtnSort.setLayoutData(gd);

        // Init the sort tokens preference
        mBtnSort.setSelection(prefStore.getBoolean(CheckstylePlugin.PLUGIN_ID,
                CheckstylePlugin.PREF_SORT_TOKENS, false, null));
        mBtnSort.addSelectionListener(new SelectionListener()
        {

            public void widgetSelected(SelectionEvent e)
            {
                // store translation preference
                IEclipsePreferences prefStore = new InstanceScope()
                        .getNode(CheckstylePlugin.PLUGIN_ID);
                prefStore.putBoolean(CheckstylePlugin.PREF_SORT_TOKENS, ((Button) e.widget)
                        .getSelection());
                try
                {
                    prefStore.flush();
                }
                catch (BackingStoreException e1)
                {
                    CheckstyleLog.log(e1);
                }
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {
            // NOOP
            }
        });

        Control buttonBar = super.createButtonBar(composite);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.END;
        buttonBar.setLayoutData(gd);

        return composite;
    }

    protected void createButtonsForButtonBar(Composite parent)
    {

        Button defautlt = createButton(parent, IDialogConstants.BACK_ID,
                Messages.RuleConfigurationEditDialog_btnDefaul, false);
        defautlt.setEnabled(!mReadonly);

        // create OK and Cancel buttons by default
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    private void initialize()
    {

        this.setTitle(NLS.bind(Messages.RuleConfigurationEditDialog_titleRuleConfigEditor, mRule
                .getName()));
        if (!mReadonly)
        {
            this.setMessage(Messages.RuleConfigurationEditDialog_msgEditRuleConfig);
        }
        else
        {
            this.setMessage(Messages.RuleConfigurationEditDialog_msgReadonlyModule);
        }

        String comment = mRule.getComment();
        if (comment != null)
        {
            mCommentText.setText(comment);
        }

        String id = mRule.getId();
        if (id != null)
        {
            mIdText.setText(id);
        }

        String customMessage = mRule.getCustomMessage();
        if (customMessage != null)
        {
            mCustomMessageText.setText(customMessage);
        }

        mCommentText.setEditable(!mReadonly);

        mSeverityCombo.setInput(SEVERITY_LEVELS);
        mSeverityCombo.getCombo().setEnabled(!mReadonly);
        if (mRule.getMetaData().hasSeverity())
        {
            mSeverityCombo.setSelection(new StructuredSelection(mRule.getSeverity()));
        }
        else
        {
            mSeverityCombo.getCombo().setEnabled(false);
        }

        // set the logo
        this.setTitleImage(CheckstylePluginImages.getImage(CheckstylePluginImages.PLUGIN_LOGO));

    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    protected void buttonPressed(int buttonId)
    {
        if (IDialogConstants.BACK_ID == buttonId)
        {

            if (MessageDialog.openConfirm(getShell(),
                    Messages.RuleConfigurationEditDialog_titleRestoreDefault,
                    Messages.RuleConfigurationEditDialog_msgRestoreDefault))
            {

                if (mRule.getMetaData().hasSeverity())
                {
                    mSeverityCombo.setSelection(new StructuredSelection(mRule.getMetaData()
                            .getDefaultSeverityLevel()));
                    mCommentText.setText(new String());
                }

                // restore the default value for the properties
                int size = mConfigPropertyWidgets != null ? mConfigPropertyWidgets.length : 0;
                for (int i = 0; i < size; i++)
                {
                    mConfigPropertyWidgets[i].restorePropertyDefault();
                }
            }
        }
        else
        {
            super.buttonPressed(buttonId);
        }
    }

    /**
     * OK button was selected.
     */
    protected void okPressed()
    {
        //
        // Get the selected severity level.
        //
        SeverityLevel severity = mRule.getSeverity();
        try
        {
            severity = (SeverityLevel) ((IStructuredSelection) mSeverityCombo.getSelection())
                    .getFirstElement();
        }
        catch (IllegalArgumentException e)
        {
            CheckstyleLog.log(e);
        }

        // Get the comment.
        String comment = StringUtils.trimToNull(mCommentText.getText());

        // Get the id
        String id = StringUtils.trimToNull(mIdText.getText());

        // Get the custom message
        String customMessage = StringUtils.trimToNull(mCustomMessageText.getText());

        //
        // Build a new collection of configuration properties.
        //
        // Note: if the rule does not have any configuration properties then
        // skip over the populating of the config property hash map.
        //
        if (mConfigPropertyWidgets != null)
        {
            for (int i = 0; i < mConfigPropertyWidgets.length; i++)
            {
                IConfigPropertyWidget widget = mConfigPropertyWidgets[i];
                ConfigProperty property = widget.getConfigProperty();

                try
                {
                    widget.validate();
                }
                catch (CheckstylePluginException e)
                {
                    String message = NLS.bind(
                            Messages.RuleConfigurationEditDialog_msgInvalidPropertyValue, property
                                    .getMetaData().getName());
                    this.setErrorMessage(message);
                    return;
                }
                property.setValue(widget.getValue());
            }
        }

        //
        // If we made it this far then all of the user input validated and we
        // can
        // update the final rule with the values the user entered.
        //
        mRule.setSeverity(severity);
        mRule.setComment(comment);
        mRule.setId(id);
        mRule.setCustomMessage(customMessage);

        super.okPressed();

    }

    private void createConfigPropertyEntries(Composite parent)
    {
        List configItemMetadata = mRule.getProperties();
        if (configItemMetadata.size() <= 0)
        {
            return;
        }

        mConfigPropertyWidgets = new IConfigPropertyWidget[configItemMetadata.size()];
        Iterator iter = configItemMetadata.iterator();
        for (int i = 0; iter.hasNext(); i++)
        {
            ConfigProperty prop = (ConfigProperty) iter.next();

            //
            // Add an input widget for the properties value.
            //
            mConfigPropertyWidgets[i] = ConfigPropertyWidgetFactory.createWidget(parent, prop,
                    getShell());
            mConfigPropertyWidgets[i].setEnabled(!mReadonly);
        }
    }

    /**
     * @see org.eclipse.jface.window.Window#create()
     */
    public void create()
    {
        super.create();

        // add resize support - for each different module the settings will be
        // stored separately
        SWTUtil.addResizeSupport(this, CheckstylePlugin.getDefault().getDialogSettings(),
                RuleConfigurationEditDialog.class.getName() + "#" //$NON-NLS-1$
                        + mRule.getMetaData().getInternalName());
    }

    /**
     * Over-rides method from Window to configure the shell (e.g. the enclosing
     * window).
     */
    protected void configureShell(Shell shell)
    {
        super.configureShell(shell);

        shell.setText(mTitle);
    }
}