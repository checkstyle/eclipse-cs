//============================================================================
//
// Copyright (C) 2003-2023  David Schneider, Lars Ködderitzsch
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
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
//
//============================================================================

package net.sf.eclipsecs.core.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.util.StringUtils;
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
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.osgi.util.NLS;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import net.sf.eclipsecs.core.CheckstylePluginPrefs;
import net.sf.eclipsecs.core.Messages;
import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.config.Module;
import net.sf.eclipsecs.core.config.meta.MetadataFactory;
import net.sf.eclipsecs.core.config.meta.RuleMetadata;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

/**
 * Performs checking on Java source code.
 */
public final class Auditor {

    /** The interval for updating the task info. */
    private static final int MONITOR_INTERVAL = 10;

    /** The check configuration the auditor uses. */
    private final ICheckConfiguration checkConfiguration;

    /** Map containing the file resources to audit. */
    private final Map<String, IFile> filesByName = new HashMap<>();

    /** Reference to the file buffer manager. */
    private final ITextFileBufferManager fileBufferManager =
        FileBuffers.getTextFileBufferManager();

    /** The progress monitor. */
    private IProgressMonitor progressMonitor;

    /** Add the check rule name to the message. */
    private boolean addRuleName;

    /** Add the check module id to the message. */
    private boolean addModuleId;

    /**
     * Creates an auditor.
     *
     * @param checkConfiguration
     *            the check configuraton to use during audit.
     */
    public Auditor(ICheckConfiguration checkConfiguration) {
        this.checkConfiguration = checkConfiguration;

        //
        // check wether to include rule names and/or module id
        //
        addRuleName =
            CheckstylePluginPrefs.getBoolean(CheckstylePluginPrefs.PREF_INCLUDE_RULE_NAMES);
        addModuleId =
            CheckstylePluginPrefs.getBoolean(CheckstylePluginPrefs.PREF_INCLUDE_MODULE_IDS);
    }

    /**
     * Runs the audit on the files associated with the auditor.
     *
     * @param project
     *            the project is needed to build the correct classpath for the checker
     * @param monitor
     *            the progress monitor
     * @throws CheckstylePluginException
     *             error processing the audit
     */
    public void runAudit(IProject project, IProgressMonitor monitor)
            throws CheckstylePluginException {
        this.progressMonitor = monitor;

        Checker checker = null;
        CheckstyleAuditListener listener = null;

        try {

            final List<File> filesToAudit = getFilesList();

            // begin task
            monitor.beginTask(
                NLS.bind(Messages.Auditor_msgCheckingConfig, checkConfiguration.getName()),
                filesToAudit.size());

            // create checker
            checker = CheckerFactory.createChecker(checkConfiguration, project);

            // create and add listener
            listener = new CheckstyleAuditListener(project);
            checker.addListener(listener);

            // run the files through the checker
            checker.process(filesToAudit);

        }
        catch (CheckstyleException ex) {
            if (ex.getCause() instanceof OperationCanceledException) {
                // user requested cancellation, keep silent
            }
            else {
                handleCheckstyleFailure(project, ex);
            }
        }
        finally {
            monitor.done();

            if (listener != null) {
                listener.cleanup();
            }

            if (checker != null) {
                checker.removeListener(listener);
            }
        }
    }

    /**
     * Handles a failure that occurred during the checkstyle run.
     *
     * @param project
     *            the project being audited
     * @param error
     *            the thrown checkstyle exception
     * @throws CheckstylePluginException
     *             an unexpected exception occurred
     */
    private void handleCheckstyleFailure(IProject project, CheckstyleException error)
            throws CheckstylePluginException {
        try {

            CheckstyleLog.log(error);

            // remove pre-existing project level marker
            project.deleteMarkers(CheckstyleMarker.MARKER_ID, false, IResource.DEPTH_ZERO);

            final Map<String, Object> attrs = new HashMap<>();
            attrs.put(IMarker.PRIORITY, Integer.valueOf(IMarker.PRIORITY_NORMAL));
            attrs.put(IMarker.SEVERITY, Integer.valueOf(IMarker.SEVERITY_ERROR));
            attrs.put(IMarker.MESSAGE,
                NLS.bind(Messages.Auditor_msgMsgCheckstyleInternalError, null));

            final IMarker projectMarker = project.createMarker(CheckstyleMarker.MARKER_ID);
            projectMarker.setAttributes(attrs);
        }
        catch (CoreException nested) {
            CheckstylePluginException.rethrow(error);
        }
    }

    /**
     * Add a file to the audit.
     *
     * @param file
     *            the file
     */
    public void addFile(IFile file) {
        filesByName.put(file.getLocation().toString(), file);
    }

    /**
     * Determines whether files have been added to the audit.
     *
     * @return <code>true</code> if at least one file has been added, <code>false</code> otherwise
     */
    public boolean hasFiles() {
        return !filesByName.isEmpty();
    }

    /**
     * Get a file resource by the file name.
     *
     * @param fileName
     *            the file name
     * @return the file resource or <code>null</code>
     */
    private IFile getFile(String fileName) {
        return filesByName.get(new Path(fileName).toString());
    }

    /**
     * Helper method to get an array of java.io.Files. This array gets passed to the checker.
     *
     * @return the list of files
     */
    private List<File> getFilesList() {
        final List<File> files = new ArrayList<>();
        for (IFile file : filesByName.values()) {
            files.add(file.getLocation().toFile());
        }
        return files;
    }

    /**
     * Implementation of the audit listener. This listener creates markers on the file resources if
     * checkstyle messages are reported.
     *
     */
    private class CheckstyleAuditListener implements AuditListener {

        /** The project. */
        private final IProject project;

        /** Map containing the marker data. */
        private final Map<String, Object> markerAttributes = new HashMap<>();

        /** Flags if the amount of markers should be limited. */
        private final boolean limitMarkers;

        /** The max amount of markers per resource. */
        private final int markerLimit;

        /** The file currently being checked. */
        private IResource resource;

        /** Document model of the current file. */
        private IDocument document;

        /** Internal counter used to time the actualisation of the monitor. */
        private int monitorCounter;

        /** The count of markers generated for the current resource. */
        private int markerCount;

        /**
         * Keep track which file paths have been connected with the BufferManager.
         */
        private Set<IPath> connectedFileBufferPaths = new HashSet<>();

        /**
         * Creates a listener for the checkstyle audit.
         *
         * @param project
         *            the project to audit
         */
        CheckstyleAuditListener(IProject project) {
            this.project = project;

            // init the marker limitation
            limitMarkers = CheckstylePluginPrefs
                .getBoolean(CheckstylePluginPrefs.PREF_LIMIT_MARKERS_PER_RESOURCE);
            markerLimit =
                CheckstylePluginPrefs.getInt(CheckstylePluginPrefs.PREF_MARKER_AMOUNT_LIMIT);
        }

        @Override
        public void fileStarted(AuditEvent event) {

            if (progressMonitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            // get the current IFile reference
            resource = getFile(event.getFileName());
            markerCount = 0;

            if (resource != null) {

                // begin subtask
                if (monitorCounter == 0) {
                    progressMonitor
                        .subTask(NLS.bind(Messages.Auditor_msgCheckingFile, resource.getName()));
                }

                // increment monitor-counter
                this.monitorCounter++;
            }
            else {

                final IPath filePath = new Path(event.getFileName());
                final IPath dirPath = filePath.removeFileExtension().removeLastSegments(1);

                final IPath projectPath = project.getLocation();
                if (projectPath.isPrefixOf(dirPath)) {
                    // find the resource with a project relative path
                    resource = project
                        .findMember(dirPath.removeFirstSegments(projectPath.segmentCount()));
                }
                else {
                    // if the resource is not inside the project, take project
                    // as resource - this should not happen
                    resource = project;
                }
            }
        }

        @Override
        public void addError(AuditEvent error) {
            try {
                if (!limitMarkers || markerCount < markerLimit) {

                    final SeverityLevel severity = error.getSeverityLevel();

                    if (resource != null && !severity.equals(SeverityLevel.IGNORE)) {

                        RuleMetadata metaData =
                            MetadataFactory.getRuleMetadata(error.getSourceName());

                        // create generic metadata if none can be found
                        if (metaData == null) {
                            final Module module = new Module(error.getSourceName());
                            metaData = MetadataFactory.createGenericMetadata(module);
                        }

                        markerAttributes.put(CheckstyleMarker.MODULE_NAME,
                            metaData.identity().internalName());
                        markerAttributes.put(CheckstyleMarker.MESSAGE_KEY,
                            error.getViolation().getKey());
                        final String moduleId = error.getModuleId();
                        if (StringUtils.isNotBlank(moduleId)) {
                            markerAttributes.put(CheckstyleMarker.MODULE_ID, moduleId);
                        }
                        markerAttributes.put(IMarker.PRIORITY,
                            Integer.valueOf(IMarker.PRIORITY_NORMAL));
                        markerAttributes.put(IMarker.SEVERITY,
                            Integer.valueOf(getSeverityValue(severity)));
                        markerAttributes.put(IMarker.LINE_NUMBER,
                            Integer.valueOf(error.getLine()));
                        markerAttributes.put(IMarker.MESSAGE, getMessage(error));

                        // calculate offset for editor annotations
                        calculateMarkerOffset(error);

                        // enables own category under Java Problem Type
                        // setting for Problems view (RFE 1530366)
                        markerAttributes.put("categoryId", CheckstyleMarker.CATEGORY_ID);

                        // create a marker for the actual resource
                        final IMarker marker = resource.createMarker(CheckstyleMarker.MARKER_ID);
                        marker.setAttributes(markerAttributes);

                        markerCount++;

                        // clear the marker attributes to reuse the map for the
                        // next error
                        markerAttributes.clear();
                    }
                }
            }
            catch (CoreException ex) {
                CheckstyleLog.log(ex);
            }
        }

        @Override
        public void addException(AuditEvent event, Throwable throwable) {
            CheckstyleLog.log(throwable);
        }

        @Override
        public void fileFinished(AuditEvent event) {
            // update monitor according to the monitor interval
            if (monitorCounter == MONITOR_INTERVAL) {
                progressMonitor.worked(MONITOR_INTERVAL);
                monitorCounter = 0;
            }

            disconnectFileBuffer();
            document = null;
        }

        @Override
        public void auditFinished(AuditEvent event) {
            cleanup();
        }

        @Override
        public void auditStarted(AuditEvent event) {
        }

        /**
         * Cleans up the state of the listener.
         */
        public void cleanup() {

            document = null;

            // disconnect any leftover buffer paths, in case of an unexpected abortion
            connectedFileBufferPaths.forEach(this::disconnectFileBuffer);
        }

        /**
         * Calculates the offset information for the editor annotations.
         *
         * @param error
         *            the audit error
         */
        private void calculateMarkerOffset(AuditEvent error) {

            // lazy create the document for the current file
            if (document == null) {
                connectFileBuffer();
            }

            // Provide offset information for the marker to make
            // annotated source code possible
            if (document != null) {
                try {

                    final int line = error.getLine();

                    final IRegion lineInformation =
                        document.getLineInformation(Math.max(line - 1, 0));
                    final int lineOffset = lineInformation.getOffset();
                    final int lineLength = lineInformation.getLength();

                    // annotate from the error column until the end of
                    // the line
                    final int offset = error.getViolation().getColumnCharIndex();

                    markerAttributes.put(IMarker.CHAR_START, Integer.valueOf(lineOffset + offset));
                    markerAttributes.put(IMarker.CHAR_END,
                        Integer.valueOf(lineOffset + lineLength));
                }
                catch (BadLocationException ex) {
                    // seems to happen quite often so its no use to log since we
                    // can't do anything about it
                    // CheckstyleLog.log(e);
                }
            }
        }

        /**
         * Connects the file buffer for the given resource.
         */
        private void connectFileBuffer() {
            if (resource instanceof IFile) {
                try {
                    final IPath path = resource.getFullPath();
                    fileBufferManager.connect(path, new NullProgressMonitor());

                    connectedFileBufferPaths.add(path);
                    document = fileBufferManager.getTextFileBuffer(path).getDocument();
                }
                catch (CoreException ex) {
                    CheckstyleLog.log(ex);
                }
            }
        }

        /**
         * Disconnects the file buffer for the given resource.
         */
        private void disconnectFileBuffer() {
            if (resource instanceof IFile) {
                final IPath path = resource.getFullPath();
                disconnectFileBuffer(path);
            }
        }

        /**
         * Disconnects the file buffer for the given path.
         *
         * @param path
         *            the path to disconnect
         */
        private void disconnectFileBuffer(IPath path) {

            try {

                if (connectedFileBufferPaths.contains(path)) {
                    fileBufferManager.disconnect(path, new NullProgressMonitor());
                    connectedFileBufferPaths.remove(path);
                }
            }
            catch (CoreException ex) {
                CheckstyleLog.log(ex);
            }
        }

        /**
         * Converts a severity level into the corresponding marker severity value.
         *
         * @param severity
         *            the severity level
         * @return the marker severity value
         */
        private int getSeverityValue(SeverityLevel severity) {
            int result = IMarker.SEVERITY_WARNING;

            if (severity.equals(SeverityLevel.INFO)) {
                result = IMarker.SEVERITY_INFO;
            }
            else if (severity.equals(SeverityLevel.WARNING)) {
                result = IMarker.SEVERITY_WARNING;
            }
            else if (severity.equals(SeverityLevel.ERROR)) {
                result = IMarker.SEVERITY_ERROR;
            }

            return result;
        }

        /**
         * Builds the message shown for the given audit event.
         *
         * @param error
         *            the audit event
         * @return the assembled message
         */
        private String getMessage(AuditEvent error) {

            String moduleId = error.getModuleId();

            if (moduleId == null) {
                final RuleMetadata metaData =
                    MetadataFactory.getRuleMetadata(error.getSourceName());
                if (metaData != null) {
                    moduleId = metaData.identity().internalName();
                }
            }

            final String message = error.getMessage();

            final StringBuilder prefix = new StringBuilder();
            if (addRuleName) {
                prefix.append(getRuleName(error));
            }
            if (addModuleId && error.getModuleId() != null) {
                if (prefix.length() > 0) {
                    prefix.append(" - ");
                }
                prefix.append(error.getModuleId());
            }

            final StringBuilder buf = new StringBuilder();
            if (prefix.length() > 0) {
                buf.append(prefix).append(": ");
            }
            buf.append(message);

            return buf.toString();
        }

        /**
         * Determines the rule name for the given audit event.
         *
         * @param error
         *            the audit event
         * @return the rule name
         */
        private String getRuleName(AuditEvent error) {
            final String ruleName;
            final RuleMetadata metaData = MetadataFactory.getRuleMetadata(error.getSourceName());
            if (metaData == null) {
                ruleName = Messages.Auditor_txtUnknownModule;
            }
            else {
                ruleName = metaData.identity().internalName();
            }
            return ruleName;
        }
    }

}
