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

package com.atlassw.tools.eclipse.checkstyle.config.configtypes;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Implementation of a location editor with only a not editable text field. This
 * is used to just show the location.
 * 
 * @author Lars Ködderitzsch
 */
public class ReadonlyLocationEditor implements IConfigurationLocationEditor
{

    //
    // attributes
    //

    /** text field containing the location. */
    private Text mLocation;

    //
    // methods
    //

    /**
     * @see IConfigurationLocationEditor#createEditorControl(java.awt.Composite)
     */
    public Control createEditorControl(Composite parent, final Shell shell)
    {

        Composite contents = new Composite(parent, SWT.NULL);
        contents.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        contents.setLayout(layout);

        mLocation = new Text(contents, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        mLocation.setLayoutData(gd);
        mLocation.setEditable(false);

        return contents;
    }

    /**
     * @see IConfigurationLocationEditor#getLocation()
     */
    public String getLocation()
    {
        return mLocation.getText();
    }

    /**
     * @see IConfigurationLocationEditor#setLocation(java.lang.String)
     */
    public void setLocation(String location)
    {
        mLocation.setText(location);
    }

    /**
     * @see IConfigurationLocationEditor#setEditable(boolean)
     */
    public void setEditable(boolean editable)
    {
    // NOOP
    }
}