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

import org.eclipse.swt.graphics.Image;

/**
 * Interface for a configuration type.
 * 
 * @author Lars Ködderitzsch
 */
public interface IConfigurationType
{

    /**
     * Initializes the configuration type.
     * 
     * @param name the displayable name of the configuration type
     * @param internalName the internal name of the configuration type
     * @param implementationClass the implementation class
     * @param editorClass the properties editor class
     * @param image the image of the configuration type
     * @param definingPluginId the plugin id the configuration type was defined
     *            in
     * @param isCreatable <code>true</code> if a configuration of this type
     *            can be created by the user.
     */
    void initialize(String name, String internalName, Class implementationClass, Class editorClass,
            String image, String definingPluginId, boolean isCreatable);

    /**
     * The displayable name of the configuration type.
     * 
     * @return the displayable name
     */
    String getName();

    /**
     * Returns the internal name of the configuration type.
     * 
     * @return the internal name
     */
    String getInternalName();

    /**
     * Returns the class of the configuration type implementation. The
     * implementation class must implement <code>ICheckConfiguration</code>.
     * 
     * @return the implementation class
     */
    Class getImplementationClass();

    /**
     * Returns the class of the configuration types properties editor.
     * 
     * @return the class of the properties editor
     */
    Class getLocationEditorClass();

    /**
     * Returns an image that depicts the configuration type.
     * 
     * @return the image of the configuration type
     */
    Image getTypeImage();

    /**
     * Return if a check configuration of this type can be created by the user.
     * 
     * @return <code>true</code> if the check configuration type is creatable,
     *         otherwise <code>false</code>
     */
    boolean isCreatable();
}