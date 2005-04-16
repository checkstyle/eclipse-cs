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
import java.util.ArrayList;
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
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.config.meta.MetadataFactory;
import com.atlassw.tools.eclipse.checkstyle.config.meta.RuleMetadata;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

/**
 * Performs checking on Java source code.
 */
public class Auditor
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

    /** The check configuration the auditor uses. */
    private ICheckConfiguration mCheckConfiguration;

    /** The progress monitor. */
    private IProgressMonitor mMonitor;

    /** Map containing the file resources to audit. */
    private Map mFiles = new HashMap();

    /** Add the check rule name to the message? */
    private boolean mAddRuleName = false;

    //=================================================
    // Constructors & finalizer.
    //=================================================

    public Auditor(ICheckConfiguration checkConfiguration)
    {
        mCheckConfiguration = checkConfiguration;

        //
        // check wether to include rule names
        //
        Preferences prefs = CheckstylePlugin.getDefault().getPluginPreferences();
        mAddRuleName = prefs.getBoolean(CheckstylePlugin.PREF_INCLUDE_RULE_NAMES);
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     * Runs the audit on the files associated with the auditor.
     * 
     * @param project the project is needed to build the correct classpath for
     *            the checker
     * @param monitor the progress monitor
     * @throws CheckstylePluginException error processing the audit
     */
    public void runAudit(IProject project, IProgressMonitor monitor)
        throws CheckstylePluginException
    {

        mMonitor = monitor;

        Checker checker = null;
        AuditListener listener = null;

        try
        {

            File[] filesToAudit = getFileArray();

            //begin task
            monitor.beginTask("Checking '" + mCheckConfiguration.getName() + "'",
                    filesToAudit.length);

            //create checker
            checker = CheckerFactory.createChecker(mCheckConfiguration);

            //create and add listener
            listener = new CheckstyleAuditListener(project);
            checker.addListener(listener);

            //reconfigure the shared classloader for the current
            // project
            CheckerFactory.getSharedClassLoader().intializeWithProject(project);

            //run the files through the checker
            checker.process(filesToAudit);

        }
        catch (IOException e)
        {
            throw new CheckstylePluginException(e.getLocalizedMessage(), e);
        }
        catch (CheckstyleException e)
        {
            throw new CheckstylePluginException(e.getLocalizedMessage(), e);
        }
        finally
        {
            monitor.done();

            //Cleanup listener
            if (checker != null)
            {
                checker.removeListener(listener);
            }
        }
    }

    /**
     * Add a file to the audit.
     * 
     * @param file the file
     */
    public void addFile(IFile file)
    {
        mFiles.put(file.getLocation().toOSString(), file);
    }

    /**
     * Get a file resource by the file name.
     * 
     * @param fileName the file name
     * @return the file resource or <code>null</code>
     */
    private IFile getFile(String fileName)
    {
        return (IFile) mFiles.get(fileName);
    }

    /**
     * Helper method to get an array of java.io.Files. This array gets passed to
     * the checker.
     * 
     * @return
     */
    private File[] getFileArray()
    {
        List files = new ArrayList();

        Collection iFiles = mFiles.values();
        Iterator it = iFiles.iterator();
        while (it.hasNext())
        {
            files.add(((IFile) it.next()).getLocation().toFile());
        }

        return (File[]) files.toArray(new File[files.size()]);
    }

    /**
     * Implementation of the audit listener. This listener creates markers on
     * the file resources if checkstyle messages are reported.
     * 
     * @author David Schneider
     * @author Lars Ködderitzsch
     */
    private class CheckstyleAuditListener implements AuditListener
    {

        /** the project. */
        private IProject mProject;

        /** The file currently being checked. */
        private IResource mResource;

        /** Contains a offset information for all lines of the file. */
        private LineModel mLineModel;

        /** internal counter used to time to actualisation of the monitor. */
        private int mMonitorCounter;

        public CheckstyleAuditListener(IProject project)
        {
            mProject = project;
        }

        public void fileStarted(AuditEvent event)
        {
            //get the current IFile reference
            mResource = getFile(event.getFileName());

            if (mResource != null)
            {

                //begin subtask
                if (mMonitorCounter == 0)
                {
                    mMonitor.subTask(CheckstylePlugin.getResourceString("taskCheckstyleStep")
                            + mResource.getName());
                }

                //increment monitor-counter
                this.mMonitorCounter++;

                //invalidate the last line model
                mLineModel = null;
            }
            else
            {

                IPath filePath = new Path(event.getFileName());
                IPath dirPath = filePath.removeFileExtension().removeLastSegments(1);

                IPath projectPath = mProject.getLocation();
                if (projectPath.isPrefixOf(dirPath))
                {
                    //find the resource with a project relative path
                    mResource = mProject.findMember(dirPath.removeFirstSegments(projectPath
                            .segmentCount()));
                }
                else
                {
                    //if the resource is not inside the project, take project
                    // as resource this should not happen
                    mResource = mProject;
                }
            }
        }

        public void addError(AuditEvent error)
        {
            try
            {
                SeverityLevel severity = error.getSeverityLevel();

                if (!severity.equals(SeverityLevel.IGNORE) && mResource != null)
                {

                    //set attributes of the marker
                    Map attributes = new HashMap();
                    attributes.put(IMarker.PRIORITY, new Integer(IMarker.PRIORITY_NORMAL));
                    attributes.put(IMarker.SEVERITY, new Integer(getSeverityValue(severity)));
                    MarkerUtilities.setLineNumber(attributes, error.getLine());
                    MarkerUtilities.setMessage(attributes, getMessage(error));

                    //lazy create the line model for the current file
                    if (mLineModel == null && mResource instanceof IFile)
                    {

                        //create the file's line offset information
                        try
                        {
                            mLineModel = new LineModel(mResource.getLocation().toFile());
                        }
                        catch (IOException e)
                        {
                            CheckstyleLog.error(e.getLocalizedMessage(), e);
                        }
                    }

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
                            MarkerUtilities.setCharStart(attributes, lineOffset.getStartOffset()
                                    + indent);
                            MarkerUtilities.setCharEnd(attributes, lineOffset.getEndOffset());
                        }
                    }

                    //create a marker for the actual resource
                    MarkerUtilities.createMarker(mResource, attributes, CheckstyleMarker.MARKER_ID);
                }
            }
            catch (CoreException e)
            {
                CheckstyleLog.error("Exception while adding Checkstyle marker to file", e);
            }
        }

        public void addException(AuditEvent event, Throwable throwable)
        {
            CheckstyleLog.warning("Exception while auditing, file=" + mResource.getName()
                    + " exception=" + throwable.getMessage());
        }

        public void fileFinished(AuditEvent event)
        {
            //update monitor according to the monitor interval
            if (mMonitorCounter == MONITOR_INTERVAL)
            {
                mMonitor.worked(MONITOR_INTERVAL);
                mMonitorCounter = 0;
            }
        }

        public void auditFinished(AuditEvent event)
        {}

        public void auditStarted(AuditEvent event)
        {}

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
            RuleMetadata metaData = MetadataFactory.getRuleMetadata(error.getSourceName());
            if (metaData == null)
            {
                return "Unknown";
            }
            return metaData.getRuleName();
        }
    }
}