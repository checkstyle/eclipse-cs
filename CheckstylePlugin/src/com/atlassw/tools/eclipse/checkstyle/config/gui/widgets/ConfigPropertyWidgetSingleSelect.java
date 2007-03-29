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

package com.atlassw.tools.eclipse.checkstyle.config.gui.widgets;

import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.atlassw.tools.eclipse.checkstyle.config.ConfigProperty;
import com.atlassw.tools.eclipse.checkstyle.config.meta.ConfigPropertyMetadata;

/**
 * Configuration widget that allows for selecting one value from a set of
 * values.
 */
public class ConfigPropertyWidgetSingleSelect extends ConfigPropertyWidgetAbstractBase
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

    private Combo mComboItem;

    //=================================================
    // Constructors & finalizer.
    //=================================================

    /**
     * Creates the widget.
     * 
     * @param parent the parent composite
     * @param prop the property
     */
    public ConfigPropertyWidgetSingleSelect(Composite parent, ConfigProperty prop)
    {
        super(parent, prop);
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     * @see ConfigPropertyWidgetAbstractBase#getValueWidget(org.eclipse.swt.widgets.Composite)
     */
    protected Control getValueWidget(Composite parent)
    {

        if (mComboItem == null)
        {

            //
            //  Create a combo box for selecting a value from the enumeration.
            //
            List valueList = getConfigProperty().getMetaData().getPropertyEnumeration();
            String[] valueLabels = new String[valueList.size()];
            int initialIndex = 0;
            String initValue = getInitValue();
            Iterator iter = valueList.iterator();
            for (int i = 0; iter.hasNext(); i++)
            {
                String value = (String) iter.next();
                valueLabels[i] = value;
                if ((initValue != null) && (initValue.equals(value)))
                {
                    initialIndex = i;
                }
            }
            mComboItem = new Combo(parent, SWT.NONE | SWT.DROP_DOWN | SWT.READ_ONLY);
            mComboItem.setLayoutData(new GridData());
            mComboItem.setItems(valueLabels);
            mComboItem.select(initialIndex);
        }

        return mComboItem;
    }

    /**
     * {@inheritDoc}
     */
    public String getValue()
    {
        String result = mComboItem.getItem(mComboItem.getSelectionIndex());
        return result;
    }

    /**
     * @see ConfigPropertyWidgetAbstractBase#restorePropertyDefault()
     */
    public void restorePropertyDefault()
    {
        ConfigPropertyMetadata metadata = getConfigProperty().getMetaData();
        String defaultValue = metadata.getOverrideDefault() != null ? metadata.getOverrideDefault()
                : metadata.getDefaultValue();
        if (defaultValue == null)
        {
            mComboItem.select(0);
        }
        else
        {
            mComboItem.select(mComboItem.indexOf(defaultValue));
        }
    }
}