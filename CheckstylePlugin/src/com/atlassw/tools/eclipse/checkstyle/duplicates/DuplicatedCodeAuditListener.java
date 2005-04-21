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

package com.atlassw.tools.eclipse.checkstyle.duplicates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.AutomaticBean;

/**
 * Duplicated code audit listener that gathers information to display it in the
 * duplicated code view.
 * 
 * @author Fabrice BELLINGARD
 */
public class DuplicatedCodeAuditListener extends AutomaticBean implements AuditListener
{

    /**
     * The view to notify with changes.
     */
    private DuplicatedCodeView mDuplicatedCodeView;

    /**
     * Map that contains :
     * <ul>
     * <li>as a key : a IFile</li>
     * <li>as a value : a Collection of DuplicatedCode objects related to that
     * file.</li>
     * </ul>
     */
    private Map mMap;

    /**
     * The file currently being processed.
     */
    private IFile mCurrentFile;

    /**
     * For the current file, the current collection of the DuplicatedCode
     * objects.
     */
    private Collection mCurrentDuplicatedCodeCollection;

    /**
     * The workspace root (variable used to prevent from having too many calls
     * during the processing).
     */
    private IWorkspaceRoot mWorkspaceRoot;

    /**
     * Frequency for the listener to notify the viewer with changes. This is a
     * number of processed files.
     */
    private static final int REFRESH_FREQUENCY = 15;

    /**
     * Counter used for the refresh frequency.
     * 
     * @see DuplicatedCodeAuditListener#REFRESH_FREQUENCY
     */
    private int mRefreshCounter;

    /**
     * Constructor.
     * 
     * @param duplicatedCodeView : the view to notify with changes
     */
    public DuplicatedCodeAuditListener(DuplicatedCodeView duplicatedCodeView)
    {
        super();
        mMap = new HashMap();
        mWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        mRefreshCounter = 0;
        this.mDuplicatedCodeView = duplicatedCodeView;
    }

    /**
     * Cf. method below.
     * 
     * @see com.puppycrawl.tools.checkstyle.api.AuditListener#auditStarted(com.puppycrawl.tools.checkstyle.api.AuditEvent)
     */
    public void auditStarted(AuditEvent event)
    {
        // empty map to initialize the viewer
        mDuplicatedCodeView.setReport(new HashMap());
    }

    /**
     * Cf. method below.
     * 
     * @see com.puppycrawl.tools.checkstyle.api.AuditListener#auditFinished(com.puppycrawl.tools.checkstyle.api.AuditEvent)
     */
    public void auditFinished(AuditEvent event)
    {
        mDuplicatedCodeView.setReport(mMap);
    }

    /**
     * Cf. method below.
     * 
     * @see com.puppycrawl.tools.checkstyle.api.AuditListener#fileStarted(com.puppycrawl.tools.checkstyle.api.AuditEvent)
     */
    public void fileStarted(AuditEvent event)
    {
        mCurrentFile = mWorkspaceRoot.getFile(new Path(event.getFileName()));
        if (!mCurrentFile.exists())
        {
            CheckstyleLog.log(null, "The file \"" + event.getFileName()
                    + "\" has not been found in the workspace.");
            mCurrentFile = null;
            return;
        }
        mCurrentDuplicatedCodeCollection = new ArrayList();
        mMap.put(mCurrentFile, mCurrentDuplicatedCodeCollection);
    }

    /**
     * Cf. method below.
     * 
     * @see com.puppycrawl.tools.checkstyle.api.AuditListener#fileFinished(com.puppycrawl.tools.checkstyle.api.AuditEvent)
     */
    public void fileFinished(AuditEvent event)
    {
        if (mCurrentDuplicatedCodeCollection == null)
        {
            mCurrentFile = null;
            return;
        }
        if (mCurrentDuplicatedCodeCollection.isEmpty())
        {
            mMap.remove(mCurrentFile);
        }
        mCurrentFile = null;
        mCurrentDuplicatedCodeCollection = null;

        // increment the refresh counter
        mRefreshCounter++;
        if (mRefreshCounter >= REFRESH_FREQUENCY)
        {
            // notify the viewer with changes
            mDuplicatedCodeView.setReport(mMap);
            // and set the counter back to zero
            mRefreshCounter = 0;
        }
    }

    /**
     * Cf. method below.
     * 
     * @see com.puppycrawl.tools.checkstyle.api.AuditListener#addError(com.puppycrawl.tools.checkstyle.api.AuditEvent)
     */
    public void addError(AuditEvent event)
    {
        mCurrentDuplicatedCodeCollection.add(new DuplicatedCode(mCurrentFile, event.getLine(),
                event.getMessage()));
    }

    /**
     * Cf. method below.
     * 
     * @see com.puppycrawl.tools.checkstyle.api.AuditListener#addException(com.puppycrawl.tools.checkstyle.api.AuditEvent,
     *      java.lang.Throwable)
     */
    public void addException(AuditEvent event, Throwable arg1)
    {}

}