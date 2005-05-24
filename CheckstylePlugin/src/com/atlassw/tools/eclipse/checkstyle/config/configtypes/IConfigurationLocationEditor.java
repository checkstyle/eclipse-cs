//============================================================================
//
// Copyright (C) 2002-2005  David Schneider, Lars Ködderitzsch
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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Interface for the check configuration type specific location editor.
 * 
 * @author Lars Ködderitzsch
 */
public interface IConfigurationLocationEditor
{

    /**
     * Create the editor control.
     * 
     * @param parent the parent composite
     * @param shell the parent shell
     * @return the location editor control
     */
    Control createEditorControl(Composite parent, Shell shell);

    /**
     * The location value of the editor.
     * 
     * @return the location value
     */
    String getLocation();

    /**
     * Set the location value of the editor.
     * 
     * @param location the location value
     */
    void setLocation(String location);

    /**
     * Sets if the location editor is editable.
     * 
     * @param editable <code>true</code> if editable, otherwise
     *            <code>false</code>
     */
    void setEditable(boolean editable);
}