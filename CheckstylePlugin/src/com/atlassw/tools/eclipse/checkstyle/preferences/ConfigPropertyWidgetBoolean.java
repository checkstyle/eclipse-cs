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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;


/**
 *  typecomment
 */
public class ConfigPropertyWidgetBoolean extends ConfigPropertyWidgetAbstractBase
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
    
    private Combo    mComboItem;

	//=================================================
	// Constructors & finalizer.
	//=================================================

	//=================================================
	// Methods.
	//=================================================
    
    ConfigPropertyWidgetBoolean(Composite parent, 
                                ConfigProperty prop,
                                ConfigPropertyMetadata metadata)
	{
		super(ConfigPropertyType.BOOLEAN, parent, prop, metadata);
        
        addPropertyLabel(SWT.NULL);

		//
		//  Create a combo box for selecting true or false.
		//
		String[] valueLabels = new String[2];
		valueLabels[0] = Boolean.TRUE.toString();
		valueLabels[1] = Boolean.FALSE.toString();
		int initialIndex = 0;
        String initValue = getInitValue();
		if (initValue.equalsIgnoreCase(Boolean.FALSE.toString()))
		{
			initialIndex = 1;
		}

		mComboItem = new Combo(parent, SWT.NONE | SWT.DROP_DOWN | SWT.READ_ONLY);
		mComboItem.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		mComboItem.setItems(valueLabels);
		mComboItem.select(initialIndex);
        
        addDescriptionButton(SWT.NULL);
	}
    
	/**
	 * @see com.atlassw.tools.eclipse.checkstyle.preferences.ConfigPropertyWidgetAbstractBase#getValue()
	 */
	public String getValue()
	{
		String result = mComboItem.getItem(mComboItem.getSelectionIndex());
        return result;
	}
}
