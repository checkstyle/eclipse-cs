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

package com.atlassw.tools.eclipse.checkstyle.preferences;

//=================================================
// Imports from java namespace
//=================================================

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.atlassw.tools.eclipse.checkstyle.config.ConfigProperty;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigPropertyMetadata;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigPropertyType;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

//=================================================
// Imports from org namespace
//=================================================

/**
 *  Base class for all configuration property input widget classes.
 */
public abstract class ConfigPropertyWidgetAbstractBase implements IConfigPropertyWidget
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

    private ConfigPropertyType mType;

    private ConfigProperty mProp;

    private ConfigPropertyMetadata mMetadata;

    private Composite mParent;

    //=================================================
    // Constructors.
    //=================================================

    protected ConfigPropertyWidgetAbstractBase(
        ConfigPropertyType type,
        Composite parent,
        ConfigProperty prop,
        ConfigPropertyMetadata metadata)
    {
        mParent = parent;
        mType = type;
        mProp = prop;
        mMetadata = metadata;
    }

    //=================================================
    // Methods.
    //=================================================
    
    /**
     * @return  The type of the configuration property.
     */
    public ConfigPropertyType getConfigPropertyType()
    {
        return mType;
    }
    
    /**
     * @return  The property's value.
     */
    public abstract String getValue();

    protected String getInitValue()
    {
        //
        //  Figure out an initial value for the property.  This will be,
        //  in order of precidents:
        //
        //     1) the existing value
        //     2) the default value, if specified
        //     3) blank
        //
        String initValue = null;
        if (mProp != null)
        {
            initValue = mProp.getValue();
        }
        if (initValue == null)
        {
            initValue = mMetadata.getDefaultValue();
        }
        if (initValue == null)
        {
            initValue = "";
        }

        return initValue;
    }
    
    /**
     * @return The configuration property.
     */
    public ConfigProperty getConfigProperty()
    {
        return mProp;
    }
    
    /**
     * @return Configuration property metadata.
     */
    public ConfigPropertyMetadata getMetadata()
    {
        return mMetadata;
    }

    protected void addPropertyLabel(int style)
    {
        //
        //  Add some spaces just to indent the property configurations.
        //
        Label label = new Label(mParent, style);
        label.setText("   ");

        //
        //  Add the property's name.
        //
        label = new Label(mParent, style);
        label.setText(mMetadata.getName());
    }

    protected void addDescriptionButton(int style)
    {
        Button button = new Button(mParent, style | SWT.PUSH);
        button.setText("Description");
        button.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event evt)
            {
                propDescription(evt);
            }
        });
    }

    private void propDescription(Event event)
    {
        MessageDialog.openInformation(
            mParent.getShell(),
            "Description",
            mMetadata.getDescription());
    }
}
