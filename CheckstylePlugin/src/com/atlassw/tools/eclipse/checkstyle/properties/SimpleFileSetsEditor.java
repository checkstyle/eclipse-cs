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

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfigurationFactory;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.FileMatchPattern;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.FileSet;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;

/**
 * Simple file sets editor producing only one file set that contains all files.
 * Only the check configuration can be chosen.
 * 
 * @author Lars Ködderitzsch
 */
public class SimpleFileSetsEditor implements IFileSetsEditor
{

    //
    // attributes
    //

    /** viewer to display the known checkstyle configurations. */
    private Combo mConfigList;

    /** used to display the config description. */
    private Text mTxtConfigDescription;

    /** button to open the check configuration preferences page. */
    private Button mBtnManageConfigs;

    private List mFileSets;

    private FileSet mDefaultFileSet;

    //
    // methods
    //

    /**
     * @see IFileSetsEditor#setFileSets(java.util.List)
     */
    public void setFileSets(List fileSets) throws CheckstylePluginException
    {
        mFileSets = fileSets;

        CheckConfiguration config = null;
        if (mFileSets.size() > 0)
        {
            config = ((FileSet) mFileSets.get(0)).getCheckConfig();
        }

        if (config == null)
        {
            List allConfigs = CheckConfigurationFactory.getCheckConfigurations();
            if (allConfigs.size() > 0)
            {
                config = (CheckConfiguration) allConfigs.get(0);
            }
        }

        //TODO make this better
        mDefaultFileSet = new FileSet("all", config);
        mDefaultFileSet.getFileMatchPatterns().add(new FileMatchPattern(".java$"));
        mFileSets.clear();
        mFileSets.add(mDefaultFileSet);
    }

    /**
     * @see IFileSetsEditor#getFileSets()
     */
    public List getFileSets()
    {
        return mFileSets;
    }

    /**
     * @see IFileSetsEditor#createContents(org.eclipse.swt.widgets.Composite)
     */
    public Control createContents(Composite parent) throws CheckstylePluginException
    {

        //group composite containing the config settings
        Group configArea = new Group(parent, SWT.NULL);
        configArea.setText(CheckstylePlugin.getResourceString("SimpleFileSetsEditor.grpConfig"));
        configArea.setLayout(new FormLayout());

        //        this.mBtnManageConfigs = new Button(configArea, SWT.PUSH);
        //        this.mBtnManageConfigs.setText(CheckstylePlugin
        //                .getResourceString("SimpleFileSetsEditor.btnManageConfigs"));
        //        FormData fd = new FormData();
        //        fd.top = new FormAttachment(0, 3);
        //        fd.right = new FormAttachment(100, -3);
        //        this.mBtnManageConfigs.setLayoutData(fd);

        this.mConfigList = new Combo(configArea, SWT.DROP_DOWN | SWT.READ_ONLY);
        FormData fd = new FormData();
        fd.left = new FormAttachment(0, 3);
        fd.top = new FormAttachment(0, 3);
        //        fd.right = new FormAttachment(mBtnManageConfigs, -3, SWT.LEFT);
        fd.right = new FormAttachment(100, -3);
        this.mConfigList.setLayoutData(fd);

        this.mConfigList.addSelectionListener(new SelectionListener()
        {
            /**
             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(
             *      org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetSelected(SelectionEvent e)
            {
                String configName = mConfigList.getItem(mConfigList.getSelectionIndex());

                try
                {
                    CheckConfiguration config = CheckConfigurationFactory.getByName(configName);
                    mDefaultFileSet.setCheckConfig(config);
                    mTxtConfigDescription.setText(config.getConfigDecription());
                }
                catch (CheckstylePluginException e1)
                {
                    CheckstyleLog.error(e1.getLocalizedMessage(), e1);
                    CheckstyleLog.errorDialog(e1.getLocalizedMessage());
                }
            }

            /**
             * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(
             *      org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetDefaultSelected(SelectionEvent e)
            {

            }
        });

        // Description
        Label lblConfigDesc = new Label(configArea, SWT.LEFT);
        lblConfigDesc.setText(CheckstylePlugin.getResourceString("SimpleFileSetsEditor.lblDesc"));
        fd = new FormData();
        fd.left = new FormAttachment(0, 3);
        fd.top = new FormAttachment(this.mConfigList, 3, SWT.BOTTOM);
        fd.right = new FormAttachment(100, -3);
        lblConfigDesc.setLayoutData(fd);

        this.mTxtConfigDescription = new Text(configArea, SWT.LEFT | SWT.WRAP | SWT.MULTI
                | SWT.READ_ONLY | SWT.BORDER | SWT.VERTICAL);
        fd = new FormData();
        fd.left = new FormAttachment(0, 3);
        fd.top = new FormAttachment(lblConfigDesc, 0, SWT.BOTTOM);
        fd.right = new FormAttachment(100, -3);
        fd.bottom = new FormAttachment(100, -3);
        this.mTxtConfigDescription.setLayoutData(fd);

        initialize();

        return configArea;
    }

    /**
     * Initializes the editor.
     * 
     *  
     */
    private void initialize() throws CheckstylePluginException
    {

        List configurations = CheckConfigurationFactory.getCheckConfigurations();
        String[] items = new String[configurations.size()];

        for (int i = 0; i < items.length; i++)
        {
            items[i] = ((CheckConfiguration) configurations.get(i)).getConfigName();
        }

        mConfigList.setItems(items);

        CheckConfiguration config = mDefaultFileSet.getCheckConfig();
        if (config != null)
        {
            mConfigList.select(mConfigList.indexOf(config.getConfigName()));
            mTxtConfigDescription.setText(config.getConfigDecription());
        }
        else if (items.length > 0)
        {
            mConfigList.select(0);
            mTxtConfigDescription.setText(((CheckConfiguration) configurations.get(0))
                    .getConfigDecription());
        }
    }
}