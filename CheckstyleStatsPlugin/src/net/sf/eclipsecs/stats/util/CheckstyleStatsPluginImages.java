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

package net.sf.eclipsecs.stats.util;

import java.util.HashMap;
import java.util.Map;

import net.sf.eclipsecs.stats.StatsCheckstylePlugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * Manages and caches images for the plugin.
 * 
 * @author Lars Ködderitzsch
 */
public final class CheckstyleStatsPluginImages
{

    /** Image descriptor for the filter icon. */
    public static final ImageDescriptor FILTER_ICON;

    /** Image descriptor for the Checkstyle violation view icon. */
    public static final ImageDescriptor LIST_VIEW_ICON;

    /** Image descriptor for the graph view icon. */
    public static final ImageDescriptor GRAPH_VIEW_ICON;

    /** Image cache. */
    private static final Map CACHED_IMAGES = new HashMap();

    static
    {
        FILTER_ICON = StatsCheckstylePlugin.imageDescriptorFromPlugin(
            StatsCheckstylePlugin.PLUGIN_ID, "icons/filter_16.gif"); //$NON-NLS-1$
        LIST_VIEW_ICON = StatsCheckstylePlugin.imageDescriptorFromPlugin(
            StatsCheckstylePlugin.PLUGIN_ID, "icons/listingView.gif"); //$NON-NLS-1$
        GRAPH_VIEW_ICON = StatsCheckstylePlugin.imageDescriptorFromPlugin(
            StatsCheckstylePlugin.PLUGIN_ID, "icons/graphView.gif"); //$NON-NLS-1$
    }

    /** Hidden default constructor. */
    private CheckstyleStatsPluginImages()
    {
        // NOOP
    }

    /**
     * Gets an image from a given descriptor.
     * 
     * @param descriptor
     *            the descriptor
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