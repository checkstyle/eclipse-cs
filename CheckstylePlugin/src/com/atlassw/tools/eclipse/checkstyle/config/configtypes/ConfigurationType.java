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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;

/**
 * Implementation of <code>IConfigurationType</code>.
 * 
 * @author Lars Ködderitzsch
 */
public class ConfigurationType implements IConfigurationType
{

    //
    // attributes
    //

    /** The name. */
    private String mName;

    /** The internal name. */
    private String mInternalName;

    /** The implementation class. */
    private Class mImplementationClass;

    /** The editor class. */
    private Class mEditorClass;

    /** The icon image. */
    private Image mIcon;

    /** Flag if creatable. */
    private boolean mIsCreatable;

    //
    // methods
    //

    /**
     * @see IConfigurationType#initialize(java.lang.String, java.lang.String,
     *      java.lang.Class, java.lang.Class, java.lang.String)
     */
    public void initialize(String name, String internalName, Class implementationClass,
            Class editorClass, String image, String definingPluginId, boolean creatable)
    {
        mName = name;
        mInternalName = internalName;
        mImplementationClass = implementationClass;
        mEditorClass = editorClass;
        mIsCreatable = creatable;

        if (image != null && definingPluginId != null)
        {
            ImageDescriptor imageDescriptor = CheckstylePlugin.imageDescriptorFromPlugin(
                    definingPluginId, image);
            mIcon = imageDescriptor.createImage(false);
        }
    }

    /**
     * @see IConfigurationType#getName()
     */
    public String getName()
    {
        return mName;
    }

    /**
     * @see IConfigurationType#getInternalName()
     */
    public String getInternalName()
    {
        return mInternalName;
    }

    /**
     * @see IConfigurationType#getImplementationClass()
     */
    public Class getImplementationClass()
    {
        return mImplementationClass;
    }

    /**
     * @see IConfigurationType#getLocationEditorClass()
     */
    public Class getLocationEditorClass()
    {
        return mEditorClass;
    }

    /**
     * @see IConfigurationType#getTypeImage()
     */
    public Image getTypeImage()
    {
        return mIcon;
    }

    /**
     * @see IConfigurationType#isCreatable()
     */
    public boolean isCreatable()
    {
        return mIsCreatable;
    }
}