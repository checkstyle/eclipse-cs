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

package com.atlassw.tools.eclipse.checkstyle.builder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.Messages;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigurationReader;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.config.Module;
import com.atlassw.tools.eclipse.checkstyle.config.meta.MetadataFactory;
import com.atlassw.tools.eclipse.checkstyle.config.meta.RuleMetadata;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.CustomLibrariesClassLoader;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Filter;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

/**
 * Performs checking on Java source code.
 */
public class Auditor
{
    // =================================================
    // Public static final variables.
    // =================================================

    // =================================================
    // Static class variables.
    // =================================================

    /** The interval for updating the task info. */
    private static final int MONITOR_INTERVAL = 10;

    // =================================================
    // Instance member variables.
    // =================================================

    /** The check configuration the auditor uses. */
    private ICheckConfiguration mCheckConfiguration;

    /** The progress monitor. */
    private IProgressMonitor mMonitor;

    /** Map containing the file resources to audit. */
    private Map mFiles = new HashMap();

    /** Add the check rule name to the message? */
    private boolean mAddRuleName = false;

    /** Add the check module id to the message? */
    private boolean mAddModuleId = false;

    /** Reference to the file buffer manager. */
    private final ITextFileBufferManager mFileBufferManager = FileBuffers
            .getTextFileBufferManager();

    // =================================================
    // Constructors & finalizer.
    // =================================================

    /**
     * Creates an auditor.
     * 
     * @param checkConfiguration the check configuraton to use during audit.
     */
    public Auditor(ICheckConfiguration checkConfiguration)
    {
        mCheckConfiguration = checkConfiguration;

        //
        // check wether to include rule names
        //
        IPreferencesService prefs = Platform.getPreferencesService();
        mAddRuleName = prefs.getBoolean(CheckstylePlugin.PLUGIN_ID,
                CheckstylePlugin.PREF_INCLUDE_RULE_NAMES, false, null);
        mAddModuleId = prefs.getBoolean(CheckstylePlugin.PLUGIN_ID,
                CheckstylePlugin.PREF_INCLUDE_MODULE_IDS, false, null);
    }

    // =================================================
    // Methods.
    // =================================================

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

        // System.out.println("----> Auditing: " + mFiles.size());

        // skip if there are no files to check
        if (mFiles.isEmpty() || project == null)
        {
            return;
        }

        mMonitor = monitor;

        Checker checker = null;
        AuditListener listener = null;
        Filter runtimeExceptionFilter = null;

        // store the current context class loader
        ClassLoader contextClassloader = Thread.currentThread().getContextClassLoader();

        try
        {

            // get the classloader that is able to load classes from custom jars
            // within the extension-libraries dir
            ClassLoader customClassLoader = CustomLibrariesClassLoader.get();
            Thread.currentThread().setContextClassLoader(customClassLoader);

            File[] filesToAudit = getFileArray();

            // begin task
            monitor.beginTask(NLS.bind(Messages.Auditor_msgCheckingConfig, mCheckConfiguration
                    .getName()), filesToAudit.length);

            // create checker
            checker = CheckerFactory.createChecker(mCheckConfiguration, project);

            // get the additional data
            ConfigurationReader.AdditionalConfigData additionalData = CheckerFactory
                    .getAdditionalData(mCheckConfiguration, project);

            // create and add listener
            listener = new CheckstyleAuditListener(project, additionalData);
            checker.addListener(listener);

            // reconfigure the shared classloader for the current
            // project
            if (project.hasNature(JavaCore.NATURE_ID))
            {
                CheckerFactory.getSharedClassLoader().intializeWithProject(project);
            }

            // run the files through the checker
            checker.process(filesToAudit);

        }
        catch (IOException e)
        {
            CheckstylePluginException.rethrow(e);
        }
        catch (CoreException e)
        {
            CheckstylePluginException.rethrow(e);
        }
        catch (CheckstyleException e)
        {
            CheckstylePluginException.rethrow(e);
        }
        finally
        {
            monitor.done();

            // Cleanup listener and filter
            if (checker != null)
            {
                checker.removeListener(listener);
                checker.removeFilter(runtimeExceptionFilter);
            }

            // restore the original classloader
            Thread.currentThread().setContextClassLoader(contextClassloader);
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

        /** Additional data about the Checkstyle configuration. */
        private ConfigurationReader.AdditionalConfigData mAdditionalConfigData;

        /** the project. */
        private IProject mProject;

        /** The file currently being checked. */
        private IResource mResource;

        /** Document model of the current file. */
        private IDocument mDocument;

        /** internal counter used to time to actualisation of the monitor. */
        private int mMonitorCounter;

        /** map containing the marker data. */
        private Map mMarkerAttributes = new HashMap();

        /** flags if the amount of markers should be limited. */
        private boolean mLimitMarkers;

        /** the max amount of markers per resource. */
        private int mMarkerLimit;

        /** the count of markers generated for the current resource. */
        private int mMarkerCount;

        public CheckstyleAuditListener(IProject project,
                ConfigurationReader.AdditionalConfigData additionalData)
        {
            mProject = project;

            mAdditionalConfigData = additionalData;

            // init the marker limitation
            IPreferencesService prefStore = Platform.getPreferencesService();
            mLimitMarkers = prefStore.getBoolean(CheckstylePlugin.PLUGIN_ID,
                    CheckstylePlugin.PREF_LIMIT_MARKERS_PER_RESOURCE, false, null);
            mMarkerLimit = prefStore.getInt(CheckstylePlugin.PLUGIN_ID,
                    CheckstylePlugin.PREF_MARKER_AMOUNT_LIMIT, CheckstylePlugin.MARKER_LIMIT, null);
        }

        public void fileStarted(AuditEvent event)
        {
            // get the current IFile reference
            mResource = getFile(event.getFileName());
            mMarkerCount = 0;

            if (mResource != null)
            {

                // begin subtask
                if (mMonitorCounter == 0)
                {
                    mMonitor.subTask(NLS
                            .bind(Messages.Auditor_msgCheckingFile, mResource.getName()));
                }

                // increment monitor-counter
                this.mMonitorCounter++;

                // invalidate the last document
                mDocument = null;

            }
            else
            {

                IPath filePath = new Path(event.getFileName());
                IPath dirPath = filePath.removeFileExtension().removeLastSegments(1);

                IPath projectPath = mProject.getLocation();
                if (projectPath.isPrefixOf(dirPath))
                {
                    // find the resource with a project relative path
                    mResource = mProject.findMember(dirPath.removeFirstSegments(projectPath
                            .segmentCount()));
                }
                else
                {
                    // if the resource is not inside the project, take project
                    // as resource - this should not happen
                    mResource = mProject;
                }
            }
        }

        public void addError(AuditEvent error)
        {
            try
            {
                if (!mLimitMarkers || mMarkerCount < mMarkerLimit)
                {

                    SeverityLevel severity = error.getSeverityLevel();

                    if (!severity.equals(SeverityLevel.IGNORE) && mResource != null)
                    {

                        RuleMetadata metaData = MetadataFactory.getRuleMetadata(error
                                .getSourceName());

                        // create generic metadata if none can be found
                        if (metaData == null)
                        {
                            Module module = new Module(error.getSourceName());
                            metaData = MetadataFactory.createGenericMetadata(module);
                        }

                        mMarkerAttributes.put(CheckstyleMarker.MODULE_NAME, metaData
                                .getInternalName());
                        mMarkerAttributes.put(CheckstyleMarker.MESSAGE_KEY, getMessageKey(error));
                        mMarkerAttributes.put(IMarker.PRIORITY,
                                new Integer(IMarker.PRIORITY_NORMAL));
                        mMarkerAttributes.put(IMarker.SEVERITY, new Integer(
                                getSeverityValue(severity)));

                        MarkerUtilities.setLineNumber(mMarkerAttributes, error.getLine());
                        MarkerUtilities.setMessage(mMarkerAttributes, getMessage(error));

                        // calculate offset for editor annotations
                        calculateMarkerOffset(error, mMarkerAttributes);

                        // enables own category under Java Problem Type
                        // setting for Problems view (RFE 1530366)
                        mMarkerAttributes.put("categoryId", new Integer(999)); //$NON-NLS-1$

                        // create a marker for the actual resource
                        MarkerUtilities.createMarker(mResource, mMarkerAttributes,
                                CheckstyleMarker.MARKER_ID);
                        mMarkerCount++;

                        // clear the marker attributes to reuse the map for the
                        // next error
                        mMarkerAttributes.clear();
                    }
                }
            }
            catch (CoreException e)
            {
                CheckstyleLog.log(e);
            }
        }

        public void addException(AuditEvent event, Throwable throwable)
        {
            CheckstyleLog.log(throwable);
        }

        public void fileFinished(AuditEvent event)
        {
            // update monitor according to the monitor interval
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

        /**
         * Calculates the offset information for the editor annotations.
         * 
         * @param error the audit error
         * @param markerAttributes the marker attributes
         * @throws CoreException
         */
        private void calculateMarkerOffset(AuditEvent error, Map markerAttributes)
            throws CoreException
        {

            // lazy create the document for the current file
            if (mDocument == null && mResource instanceof IFile)
            {
                IPath path = mResource.getFullPath();
                mFileBufferManager.connect(path, new NullProgressMonitor());
                mDocument = mFileBufferManager.getTextFileBuffer(path).getDocument();
            }

            // Provide offset information for the marker to make
            // annotated source code possible
            if (mDocument != null)
            {
                try
                {

                    int line = error.getLine();

                    IRegion lineInformation = mDocument
                            .getLineInformation(line == 0 ? 0 : line - 1);
                    int lineOffset = lineInformation.getOffset();
                    int lineLength = lineInformation.getLength();

                    String lineData = mDocument.get(lineOffset, lineLength);

                    // annotate from the error column until the end of
                    // the line
                    int offset = getOffsetFromColumn(lineData, error.getColumn());

                    MarkerUtilities.setCharStart(markerAttributes, lineOffset + offset);
                    MarkerUtilities.setCharEnd(markerAttributes, lineOffset + lineLength);
                }
                catch (BadLocationException e)
                {
                    // seems to happen quite often so its no use to log since we
                    // can't do anything about it
                    // CheckstyleLog.log(e);
                }
            }
        }

        /**
         * Calculates the offset for the given column within this line. This is
         * done to get a correct offset if tab characters are used within this
         * line.
         * 
         * @param line the line as string
         * @param column the column
         * @return the true offset of this column within the line
         */
        private int getOffsetFromColumn(String line, int column)
        {

            int calculatedColumn = 0;

            int lineLength = line.length();
            for (int i = 0; i < lineLength; i++)
            {
                char c = line.charAt(i);
                if (c == '\t')
                {
                    calculatedColumn += mAdditionalConfigData.getTabWidth();
                }
                else
                {
                    calculatedColumn++;
                }

                if (calculatedColumn >= column)
                {
                    return i;
                }
            }
            return lineLength;
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

            String moduleId = error.getModuleId();

            if (moduleId == null)
            {
                RuleMetadata metaData = MetadataFactory.getRuleMetadata(error.getSourceName());
                if (metaData != null)
                {
                    moduleId = metaData.getInternalName();
                }
            }

            String message = (String) mAdditionalConfigData.getCustomMessages().get(moduleId);
            if (StringUtils.trimToNull(message) == null)
            {
                message = error.getMessage();
            }

            StringBuffer prefix = new StringBuffer();
            if (mAddRuleName)
            {
                prefix.append(getRuleName(error));
            }
            if (mAddModuleId && error.getModuleId() != null)
            {
                if (prefix.length() > 0)
                {
                    prefix.append(" - "); //$NON-NLS-1$
                }
                prefix.append(error.getModuleId());
            }

            StringBuffer buf = new StringBuffer();
            if (prefix.length() > 0)
            {
                buf.append(prefix).append(": "); //$NON-NLS-1$
            }
            buf.append(message);

            return buf.toString();
        }

        private String getMessageKey(AuditEvent error)
        {

            String moduleId = error.getModuleId();

            if (moduleId == null)
            {
                RuleMetadata metaData = MetadataFactory.getRuleMetadata(error.getSourceName());
                if (metaData != null)
                {
                    moduleId = metaData.getInternalName();
                }
            }

            String messageKey = (String) mAdditionalConfigData.getCustomMessages().get(moduleId);
            if (StringUtils.trimToNull(messageKey) == null)
            {
                messageKey = error.getLocalizedMessage().getKey();
            }

            return messageKey;
        }

        private String getRuleName(AuditEvent error)
        {
            RuleMetadata metaData = MetadataFactory.getRuleMetadata(error.getSourceName());
            if (metaData == null)
            {
                return Messages.Auditor_txtUnknownModule;
            }
            return metaData.getRuleName();
        }
    }
}