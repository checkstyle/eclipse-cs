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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.atlassw.tools.eclipse.checkstyle.config.ConfigProperty;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigPropertyEnumerationMetadata;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigPropertyMetadata;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigPropertyType;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigPropertyValueMetadata;

//=================================================
// Imports from org namespace
//=================================================
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;


/**
 *  Configuration widget for selecting multiple values with check boxes.
 */
public class ConfigPropertyWidgetMultiCheck extends ConfigPropertyWidgetAbstractBase
{
    //=================================================
	// Public static final variables.
	//=================================================

	//=================================================
	// Static class variables.
	//=================================================
    
    private static final int  COLUMN_THRESHOLD = 12;

	//=================================================
	// Instance member variables.
	//=================================================
    
    private Composite    mCheckboxCompisite;
    
    private String[]     mLabels;
    
    private Button[]     mButtons;
    
	//=================================================
	// Constructors & finalizer.
	//=================================================

	//=================================================
	// Methods.
	//=================================================
    
    ConfigPropertyWidgetMultiCheck(Composite parent, 
                                   ConfigProperty prop,
                                   ConfigPropertyMetadata metadata)
    {
		super(ConfigPropertyType.MULTI_CHECK, parent, prop, metadata);
        
        addPropertyLabel(SWT.TOP);
        
        //
        //  Get the data to build the check boxes.
        //
        ConfigPropertyEnumerationMetadata enum = metadata.getPropertyEnumeration();
        List valueList = (List)enum.getValueMetadata();
        mLabels = new String[valueList.size()];
        mButtons = new Button[valueList.size()];
                
		//
		//  Create a new composite to hold the check boxes.
        //
        int numColumns = 1;
        if (valueList.size() > COLUMN_THRESHOLD)
        {
            numColumns = valueList.size() / COLUMN_THRESHOLD;
        }
        mCheckboxCompisite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = numColumns;
        layout.marginWidth = 0;
        mCheckboxCompisite.setLayout(layout);
        
        List currentValues = getInitialValues();
        
        Iterator iter = valueList.iterator();
        for (int i = 0; iter.hasNext(); i++)
        {
            ConfigPropertyValueMetadata value = (ConfigPropertyValueMetadata)iter.next();
            mLabels[i] = value.getValue();
            mButtons[i] = new Button(mCheckboxCompisite, SWT.CHECK | SWT.LEFT);
            mButtons[i].setText(mLabels[i]);
            mButtons[i].setLayoutData(new GridData());
            
            boolean checked = false;
            if (currentValues.contains(mLabels[i]))
            {
                checked = true;
            }
            mButtons[i].setSelection(checked);
        }
        
        addDescriptionButton(SWT.TOP);
    }

    /**
     * {@inheritDoc}
     */
    public String getValue()
    {
        StringBuffer buffer = new StringBuffer("");
        boolean first = true;
        
        for (int i = 0; i < mButtons.length; i++)
        {
            if (mButtons[i].getSelection())
            {
                //
                //  Add a comma unless this is the first element.
                //
                if (first)
                {
                    first = false;
                }
                else
                {
                    buffer.append(", ");
                }
                buffer.append(mLabels[i]);
            }
        }
        return buffer.toString();
    }
    
    private List getInitialValues()
    {
        List result = new LinkedList();
        StringTokenizer tokenizer = new StringTokenizer(getInitValue(), " ,");
        while (tokenizer.hasMoreTokens())
        {
            result.add(tokenizer.nextToken());
        }
        
        return result;
    }
    
}
