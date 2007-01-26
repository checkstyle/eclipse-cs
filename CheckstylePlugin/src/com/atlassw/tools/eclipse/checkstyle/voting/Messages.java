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

import org.eclipse.osgi.util.NLS;

// CHECKSTYLE:OFF

public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "com.atlassw.tools.eclipse.checkstyle.voting.messages"; //$NON-NLS-1$

    public static String VotingPreferencePage_btnVote;

    public static String VotingPreferencePage_lblComment;

    public static String VotingPreferencePage_lblDescription;

    public static String VotingPreferencePage_lblRating;

    public static String VotingPreferencePage_msgVoteRegistered;

    public static String VotingPreferencePage_ratingBest;

    public static String VotingPreferencePage_ratingWorst;

    public static String VotingPreferencePage_titleVoteRegistered;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {}
}

// CHECKSTYLE:ON
