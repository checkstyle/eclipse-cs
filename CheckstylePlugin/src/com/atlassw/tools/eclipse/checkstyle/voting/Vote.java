//============================================================================
//
// Copyright (C) 2002-2007  David Schneider, Lars Ködderitzsch
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

package com.atlassw.tools.eclipse.checkstyle.voting;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 * Does a vote at EclipsePluginCentral.
 * 
 * @author Lars Koedderitzsch
 */
public class Vote
{

    private static final String ECLIPSE_PLUGIN_CENTRAL_URL = "http://www.eclipseplugincentral.com/Web_Links+main.html"; //$NON-NLS-1$

    private static final String PLUGIN_ID = "376"; //$NON-NLS-1$

    private int mRating;

    private String mComment;

    /**
     * Creates the vote instance.
     * 
     * @param rating the selected rating
     * @param comment the (optional) comment
     */
    public Vote(int rating, String comment)
    {
        if (rating < 1 || rating > 10)
        {
            throw new IllegalArgumentException("Rating must be between 1 and 10"); //$NON-NLS-1$
        }

        this.mRating = rating;
        this.mComment = comment;
    }

    /**
     * Casts the vote.
     * 
     * @throws IOException error transmitting voting data to EPC
     */
    public void cast() throws IOException
    {
        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(ECLIPSE_PLUGIN_CENTRAL_URL);

        // Configure the form parameters
        method.addParameter("ratinglid", PLUGIN_ID); //$NON-NLS-1$
        method.addParameter("ratinguser", "outside"); //$NON-NLS-1$ //$NON-NLS-2$
        method.addParameter("req", "addrating"); //$NON-NLS-1$ //$NON-NLS-2$
        method.addParameter("rating", "" + mRating); //$NON-NLS-1$ //$NON-NLS-2$
        if (mComment != null)
        {
            method.addParameter("ratingcomments", mComment); //$NON-NLS-1$
        }

        // Execute the POST method
        client.executeMethod(method);
    }
}
