//============================================================================
//
// Copyright (C) 2002-2003  David Schneider
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

//=================================================
// Imports from java namespace
//=================================================
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.config.FileSet;
import com.atlassw.tools.eclipse.checkstyle.config.FileSetFactory;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

//=================================================
// Imports from org namespace
//=================================================
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;


/**
 *  Performs checking on Java source code.
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
    
    private IProject mProject;
    
    private FileSet[] mFileSets;

    //=================================================
    // Constructors & finalizer.
    //=================================================

    /**
     *  Construct an <code>Auditor</code> for the indicated project.
     *
     *  @param  project   The project the auditor is associated with.
     * 
     *  @throws CheckstyleException  Error during processing.
     */
    Auditor(IProject project) throws CheckstylePluginException
    {
        mProject = project;
        loadFileSets();
    }

    //=================================================
    // Methods.
    //=================================================
    
    private void loadFileSets() throws CheckstylePluginException
    {
        List fileSets = FileSetFactory.getFileSets(mProject);
        mFileSets = new FileSet[fileSets.size()];

        Iterator iter = fileSets.iterator();
        for (int i = 0; iter.hasNext(); i++)
        {
            mFileSets[i] = (FileSet)iter.next();
        }
    }

    /**
     *  Check a collection of files.
     *
     *  @param files    The collection of <code>IFile</code> objects to be audited.
     * 
     *  @param monitor  Progress monitor to update with progress.
     * 
     *  @throws CheckstyleException  Error during processing.
     * 
     *  @throws CoreException  Error during processing.
     */
    void checkFiles(Collection files, ClassLoader classLoader, IProgressMonitor monitor)
        throws CheckstylePluginException, CoreException
    {
        //
        //  Build a checkstyle checker for each file set that is enabled.
        //
        Checker[] checker = new Checker[mFileSets.length];
        CheckstyleAuditListener auditListener = new CheckstyleAuditListener();

        for (int i = 0; i < checker.length; i++)
        {
            if (mFileSets[i].isEnabled())
            {
                try
                {
                    checker[i] = new Checker();
                    checker[i].addListener(auditListener);
                    CheckConfiguration checkConfig = mFileSets[i].getCheckConfig();
                    if (checkConfig == null)
                    {
                    	String msg = "CheckConfig '" 
                    	             + mFileSets[i].getCheckConfigName() 
                    	             + "' not found";
						CheckstyleLog.error(msg);
						throw new CheckstylePluginException(msg);
                    }
					checker[i].setClassloader(classLoader);
                    checker[i].configure(checkConfig);
                }
                catch (com.puppycrawl.tools.checkstyle.api.CheckstyleException e)
                {
                    CheckstyleLog.error("Failed to create Checkstyle Checker", e);
                    throw new CheckstylePluginException("Failed to create Checkstyle Checker");
                }
            }
        }

        //
        //  Iterate through the files and audit any files that match the file set.
        //
        int currentCount = 0;
        File[] checkFile = new File[1];
        Iterator iter = files.iterator();

        while (iter.hasNext())
        {
            IFile file = (IFile)iter.next();

            file.deleteMarkers(CheckstyleMarker.MARKER_ID, true, IResource.DEPTH_INFINITE);

            for (int i = 0; i < mFileSets.length; i++)
            {
                if (mFileSets[i].isEnabled() && mFileSets[i].includesFile(file))
                {
                    String fileName = file.getLocation().toOSString();

                    checkFile[0] = new File(fileName);
                    auditListener.setFile(file);
                    checker[i].process(checkFile);
                }
            }

            ++currentCount;

            if ((monitor != null) && ((currentCount % MONITOR_INTERVAL) == 0))
            {
                monitor.worked(MONITOR_INTERVAL);

                IPath path = file.getFullPath();
                int segCount = path.segmentCount();

                path = path.uptoSegment(segCount - 1);

                String label = path.toString();

                label = label.substring(1, label.length());
                monitor.subTask("Checking " + label);

                if (monitor.isCanceled())
                {
                    break;
                }
            }
        }
    }

    private static class CheckstyleAuditListener implements AuditListener
    {
        private IFile mFile;

        public void addError(AuditEvent error)
        {
            try
            {
                SeverityLevel severity = error.getSeverityLevel();

                if (!severity.equals(SeverityLevel.IGNORE))
                {
                    IMarker marker = mFile.createMarker(CheckstyleMarker.MARKER_ID);

                    marker.setAttribute(IMarker.LINE_NUMBER, error.getLine());
                    marker.setAttribute(IMarker.MESSAGE, error.getMessage());
                    marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
                    marker.setAttribute(IMarker.SEVERITY, getSeverityValue(severity));
                }
            }
            catch (CoreException e)
            {
                CheckstyleLog.error("Exception while adding Checkstyle marker to file", e);
            }
        }

        public void addException(AuditEvent event, Throwable throwable)
        {
        }

        public void auditFinished(AuditEvent event)
        {
        }

        public void auditStarted(AuditEvent event)
        {
        }

        public void fileFinished(AuditEvent event)
        {
        }

        public void fileStarted(AuditEvent event)
        {
        }

        public IFile getFile()
        {
            return mFile;
        }

        public void setFile(IFile file)
        {
            mFile = file;
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
    }
}
