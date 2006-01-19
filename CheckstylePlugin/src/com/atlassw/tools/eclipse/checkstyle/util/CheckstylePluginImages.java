//============================================================================
//
// Copyright (C) 2002-2006  David Schneider, Lars Ködderitzsch
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

package com.atlassw.tools.eclipse.checkstyle.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;

/**
 * Manages and caches images for the plugin.
 * 
 * @author Lars Ködderitzsch
 */
public abstract class CheckstylePluginImages
{

    /** Image descriptor for the plugin logo. */
    public static final ImageDescriptor PLUGIN_LOGO;

    /** Image descriptor for the error marker. */
    public static final ImageDescriptor MARKER_ERROR;

    /** Image descriptor for the warning marker. */
    public static final ImageDescriptor MARKER_WARNING;

    /** Image descriptor for the info marker. */
    public static final ImageDescriptor MARKER_INFO;

    /** Image descriptor for the help icon. */
    public static final ImageDescriptor HELP_ICON;

    /** Image cache. */
    private static final Map CACHED_IMAGES = new HashMap();

    static
    {

        PLUGIN_LOGO = CheckstylePlugin.imageDescriptorFromPlugin(CheckstylePlugin.PLUGIN_ID,
                "icons/eclipse-cs_inverted.png"); //$NON-NLS-1$
        MARKER_ERROR = CheckstylePlugin.imageDescriptorFromPlugin(CheckstylePlugin.PLUGIN_ID,
                "icons/checkstyle_error.gif"); //$NON-NLS-1$
        MARKER_WARNING = CheckstylePlugin.imageDescriptorFromPlugin(CheckstylePlugin.PLUGIN_ID,
                "icons/checkstyle_warning.gif"); //$NON-NLS-1$
        MARKER_INFO = CheckstylePlugin.imageDescriptorFromPlugin(CheckstylePlugin.PLUGIN_ID,
                "icons/checkstyle_info.gif"); //$NON-NLS-1$
        HELP_ICON = CheckstylePlugin.imageDescriptorFromPlugin(CheckstylePlugin.PLUGIN_ID,
                "icons/help.gif"); //$NON-NLS-1$
    }

    /**
     * Hidden default constructor.
     */
    private CheckstylePluginImages()
    {
    // NOOP
    }

    /**
     * Gets an image from a given descriptor.
     * 
     * @param descriptor the descriptor
     * @return the image
     */
    public static Image getImage(ImageDescriptor descriptor)
    {

        Image image = (Image) CACHED_IMAGES.get(descriptor);
        if (image == null)
        {
            image = descriptor.createImage();
            CACHED_IMAGES.put(descriptor, image);
        }
        return image;
    }
}