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

//=================================================
// Imports from org namespace
//=================================================
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 *  A string property configuration widget.
 */
public class ConfigPropertyWidgetString extends ConfigPropertyWidgetAbstractBase
{
    //=================================================
    // Public static final variables.
    //=================================================

    //=================================================
    // Static class variables.
    //=================================================

    private static final int MAX_INPUT_LENGTH = 40;

    //=================================================
    // Instance member variables.
    //=================================================

    private Text mTextWidget;

    //=================================================
    // Constructors & finalizer.
    //=================================================

    //=================================================
    // Methods.
    //=================================================

    ConfigPropertyWidgetString(
        Composite parent,
        ConfigProperty prop,
        ConfigPropertyMetadata metadata)
    {
        super(ConfigPropertyType.STRING, parent, prop, metadata);

        addPropertyLabel(SWT.NULL);

        //
        //  Create a text entry field.
        //
        mTextWidget = new Text(parent, SWT.SINGLE | SWT.BORDER);
        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = true;
        data.verticalAlignment = GridData.CENTER;
        data.grabExcessVerticalSpace = false;

        GC gc = new GC(parent);
        gc.setFont(parent.getFont());
        FontMetrics fontMetrics = gc.getFontMetrics();
        data.widthHint = fontMetrics.getAverageCharWidth() * MAX_INPUT_LENGTH;
        data.heightHint = fontMetrics.getHeight();

        mTextWidget.setLayoutData(data);
        mTextWidget.setFont(parent.getFont());

        String initValue = getInitValue();
        if (initValue != null)
        {
            mTextWidget.setText(initValue);
        }

        addDescriptionButton(SWT.NULL);
    }

    /**
     * {@inheritDoc}
     */
    public String getValue()
    {
        String result = mTextWidget.getText();
        if (result == null)
        {
            result = "";
        }
        return result;
    }
}
