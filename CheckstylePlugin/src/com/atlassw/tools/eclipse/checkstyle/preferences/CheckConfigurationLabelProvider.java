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
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfiguration;

//=================================================
// Imports from org namespace
//=================================================
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;


/**
 *  Provides the labels for the audit configuration list display.
 */
class CheckConfigurationLabelProvider
	implements ITableLabelProvider
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

	//=================================================
	// Constructors & finalizer.
	//=================================================

	//=================================================
	// Methods.
	//=================================================
    
	/**
	 * @see IBaseLabelProvider#removeListener(ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) 
	{ }

	/**
	 * @see IBaseLabelProvider#isLabelProperty(Object, String)
	 */
	public boolean isLabelProperty(Object element, String property) 
	{
		return false;
	}

	/**
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose() 
	{ }

	/**
	 * @see IBaseLabelProvider#addListener(ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) 
	{ }

	/**
	 * @see ITableLabelProvider#getColumnText(Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) 
	{
		String result = element.toString();
		if (element instanceof CheckConfiguration) 
		{
			CheckConfiguration cfg = (CheckConfiguration)element;
			result = cfg.getConfigName();
		}
		return result;
	}

	/**
	 * @see ITableLabelProvider#getColumnImage(Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) 
	{
		return null;
	}
}