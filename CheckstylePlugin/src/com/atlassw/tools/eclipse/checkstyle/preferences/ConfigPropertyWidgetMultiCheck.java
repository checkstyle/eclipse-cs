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

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.atlassw.tools.eclipse.checkstyle.config.ConfigProperty;

/**
 * Configuration widget for selecting multiple values with check boxes.
 */
public class ConfigPropertyWidgetMultiCheck extends ConfigPropertyWidgetAbstractBase
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

    private CheckboxTableViewer mTable;

    //=================================================
    // Constructors & finalizer.
    //=================================================

    //=================================================
    // Methods.
    //=================================================

    ConfigPropertyWidgetMultiCheck(Composite parent, ConfigProperty prop)
    {
        super(parent, prop);
    }

    /**
     * @see ConfigPropertyWidgetAbstractBase#getValueWidget(org.eclipse.swt.widgets.Composite)
     */
    protected Control getValueWidget(Composite parent)
    {
        if (mTable == null)
        {

            mTable = CheckboxTableViewer.newCheckList(parent, SWT.V_SCROLL | SWT.BORDER);
            mTable.setContentProvider(new ArrayContentProvider());
            mTable.setInput(getMetadata());
            mTable.setCheckedElements(getInitialValues().toArray());

            GridData gd = new GridData(GridData.FILL_BOTH);
            gd.heightHint = 150;
            mTable.getControl().setLayoutData(gd);
        }

        return mTable.getControl();
    }

    /**
     * {@inheritDoc}
     */
    public String getValue()
    {
        StringBuffer buffer = new StringBuffer("");
        boolean first = true;

        Object[] checkedElements = mTable.getCheckedElements();

        for (int i = 0; i < checkedElements.length; i++)
        {

            if (i > 0)
            {
                buffer.append(",");
            }
            buffer.append(checkedElements[i]);
        }
        return buffer.toString();
    }

    private List getInitialValues()
    {
        List result = new LinkedList();
        StringTokenizer tokenizer = new StringTokenizer(getInitValue(), ",");
        while (tokenizer.hasMoreTokens())
        {
            result.add(tokenizer.nextToken().trim());
        }

        return result;
    }

    /**
     * @see IConfigPropertyWidget#restorePropertyDefault()
     */
    public void restorePropertyDefault()
    {
        String defaultValue = getConfigProperty().getMetaData().getDefaultValue();
        List result = new LinkedList();

        if (defaultValue != null)
        {
            StringTokenizer tokenizer = new StringTokenizer(defaultValue, ",");
            while (tokenizer.hasMoreTokens())
            {
                result.add(tokenizer.nextToken().trim());
            }
        }

        //clear current checked state
        mTable.setCheckedElements(new Object[0]);

        mTable.setCheckedElements(result.toArray());
    }

}