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

package com.atlassw.tools.eclipse.checkstyle.builder;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.config.FileSet;
import com.atlassw.tools.eclipse.checkstyle.config.MetadataFactory;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

/**
 * Performs checking on Java source code.
 */
class Auditor
{
    //=================================================
    // Public static final variables.
    //=================================================

    //=================================================
    // Static class variables.
    //=================================================

    private static final int MONITOR_INTERVAL = 10;

    //=================================================
    // Instance member variables.
    //=================================================

    private IProject         mProject;

    private FileSet[]        mFileSets;

    //=================================================
    // Constructors & finalizer.
    //=================================================

    /**
     * Construct an <code>Auditor</code> for the indicated project.
     * 
     * @param project The project the auditor is associated with.
     * 
     * @param fileSets The list of file sets to use in the audit.
     */
    Auditor(IProject project, List fileSets)
    {
        mProject = project;
        mFileSets = (FileSet[]) fileSets.toArray(new FileSet[fileSets.size()]);
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     * Check a collection of files.
     * 
     * @param files The collection of <code>IFile</code> objects to be
     *            audited.
     * 
     * @param monitor Progress monitor to update with progress.
     * 
     * @throws CheckstylePluginException Error during processing.
     * 
     * @throws CoreException Error during processing.
     */
    void checkFiles(Collection files, ClassLoader classLoader, IProgressMonitor monitor)
            throws CheckstylePluginException, CoreException
    {
        //
        //  Delete any existing project level markers.
        //
        mProject.deleteMarkers(CheckstyleMarker.MARKER_ID, true, IResource.DEPTH_ZERO);

        //
        //  If the project does not have any enabled file sets then return
        // without
        //  doing anything else.
        //
        if (mFileSets.length <= 0)
        {
            //
            //  Remove any markers that may be present in the project.
            //
            mProject.deleteMarkers(CheckstyleMarker.MARKER_ID, true, IResource.DEPTH_INFINITE);
            return;
        }

        //
        //  Build an audit listener to receive audit violations from Checkstyle.
        //
        CheckstyleAuditListener auditListener = new CheckstyleAuditListener();
        Preferences prefs = CheckstylePlugin.getDefault().getPluginPreferences();
        boolean includeRuleNames = prefs.getBoolean(CheckstylePlugin.PREF_INCLUDE_RULE_NAMES);
        auditListener.setAddRuleName(includeRuleNames);

        //
        //  Build a checkstyle checker for each file set.
        //
        Checker[] checker = new Checker[mFileSets.length];
        for (int i = 0; i < checker.length; i++)
        {
            try
            {
                checker[i] = new Checker();
                checker[i].addListener(auditListener);
                CheckConfiguration checkConfig = mFileSets[i].getCheckConfig();
                if (checkConfig == null)
                {
                    String msg = "Checkstyle CheckConfig '" + mFileSets[i].getCheckConfigName()
                            + "' not found";

                    IMarker marker = mProject.createMarker(CheckstyleMarker.MARKER_ID);
                    marker.setAttribute(IMarker.MESSAGE, msg);
                    marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
                    marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);

                    continue;
                }
                checker[i].setClassloader(classLoader);
                checker[i].configure(checkConfig);
            }
            catch (com.puppycrawl.tools.checkstyle.api.CheckstyleException e)
            {
                e.printStackTrace();
                CheckstyleLog.error("Failed to create Checkstyle Checker", e);
                throw new CheckstylePluginException("Failed to create Checkstyle Checker");
            }
        }

        //
        //  Iterate through the files and audit any files that match the file
        // set.
        //
        int currentCount = 0;
        File[] checkFile = new File[1];
        Iterator iter = files.iterator();
        while (iter.hasNext())
        {
            IFile file = (IFile) iter.next();

            //
            //  Remove any markers on the file.
            //
            file.deleteMarkers(CheckstyleMarker.MARKER_ID, true, IResource.DEPTH_INFINITE);

            //
            //  Run through the file sets.
            //
            for (int i = 0; i < mFileSets.length; i++)
            {
                if (mFileSets[i].includesFile(file))
                {
                    String fileName = file.getLocation().toOSString();
                    checkFile[0] = new File(fileName);
                    auditListener.setFile(file);
                    checker[i].process(checkFile);
                }
            }
            ++currentCount;

            //
            //  Update the progress monitor.
            //
            if ((monitor != null) && ((currentCount % MONITOR_INTERVAL) == 0))
            {
                monitor.worked(MONITOR_INTERVAL);

                //
                //  Build a label for the progress monitor.
                //
                IPath path = file.getFullPath();
                int segCount = path.segmentCount();
                path = path.uptoSegment(segCount - 1);
                String label = path.toString();
                label = label.substring(1, label.length());
                monitor.subTask("Checking " + label);

                //
                //  Check to see if the user has cancled the built. If so, break
                // out
                //  of the loop.
                //
                if (monitor.isCanceled())
                {
                    break;
                }
            }
        }
    }

    private static class CheckstyleAuditListener implements AuditListener
    {
        //  The file currently being checked.
        private IFile     mFile;

        //  Add the check rule name to the message?
        private boolean   mAddRuleName    = false;

        private Map       mClassToNameMap = MetadataFactory.getClassToNameMap();

        // Contains a offset information for all lines of the file
        private LineModel mLineModel;

        public void addError(AuditEvent error)
        {
            try
            {
                SeverityLevel severity = error.getSeverityLevel();

                if (!severity.equals(SeverityLevel.IGNORE))
                {

                    //set attributes of the marker
                    Map attributes = new HashMap();
                    attributes.put(IMarker.PRIORITY, new Integer(IMarker.PRIORITY_NORMAL));
                    attributes.put(IMarker.SEVERITY, new Integer(getSeverityValue(severity)));
                    MarkerUtilities.setLineNumber(attributes, error.getLine());
                    MarkerUtilities.setMessage(attributes, getMessage(error));

                    //Provide offset information for the marker to make
                    // annotated source code possible
                    if (mLineModel != null)
                    {

                        //Offset must be file based, not line based
                        LineModel.LineOffset lineOffset = mLineModel.getLineOffset(error.getLine());

                        if (lineOffset != null)
                        {

                            //annotate from the error column until the end of
                            // the line
                            int indent = error.getColumn() == 0 ? 0 : error.getColumn() - 1;
                            MarkerUtilities.setCharStart(attributes, lineOffset.mStartOffset
                                    + indent);
                            MarkerUtilities.setCharEnd(attributes, lineOffset.mEndOffset);
                        }
                    }

                    //create a marker for the actual resource
                    MarkerUtilities.createMarker(mFile, attributes, CheckstyleMarker.MARKER_ID);
                }
            }
            catch (CoreException e)
            {
                CheckstyleLog.error("Exception while adding Checkstyle marker to file", e);
            }
        }

        public void addException(AuditEvent event, Throwable throwable)
        {
            CheckstyleLog.warning("Exception while auditing, file=" + mFile.getName()
                    + " exception=" + throwable.getMessage());
        }

        public void auditFinished(AuditEvent event)
        {}

        public void auditStarted(AuditEvent event)
        {}

        public void fileFinished(AuditEvent event)
        {}

        public void fileStarted(AuditEvent event)
        {}

        public IFile getFile()
        {
            return mFile;
        }

        public void setFile(IFile file)
        {
            mFile = file;

            //create the file's line offset information
            try
            {
                mLineModel = new LineModel(mFile.getLocation().toFile());
            }
            catch (IOException e)
            {
                CheckstyleLog.error(e.getLocalizedMessage(), e);
            }
        }

        private int getSeverityValue(SeverityLevel severity)
        {
            int result = IMarker.SEVERITY_WARNING;

            if (severity.equals(SeverityLevel.INFO))
            {
                result = IMarker.SEVERITY_INFO;
            }
            else if (severity.equals(SeverityLevel.WARNING))
            {
                result = IMarker.SEVERITY_WARNING;
            }
            else if (severity.equals(SeverityLevel.ERROR))
            {
                result = IMarker.SEVERITY_ERROR;
            }

            return result;
        }

        private String getMessage(AuditEvent error)
        {
            String message = error.getMessage();
            if (mAddRuleName)
            {
                StringBuffer buffer = new StringBuffer(getRuleName(error));
                buffer.append(": ").append(message);
                message = buffer.toString();
            }
            return message;
        }

        private String getRuleName(AuditEvent error)
        {
            String ruleName = (String) mClassToNameMap.get(error.getSourceName());
            if (ruleName == null)
            {
                ruleName = "Unknown";
            }
            return ruleName;
        }

        public boolean getAddRuleName()
        {
            return mAddRuleName;
        }

        public void setAddRuleName(boolean b)
        {
            mAddRuleName = b;
        }
    }

}