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
public class Auditor {

  /** The interval for updating the task info. */
  private static final int MONITOR_INTERVAL = 10;

  /** The check configuration the auditor uses. */
  private final ICheckConfiguration mCheckConfiguration;

  /** Map containing the file resources to audit. */
  private final Map<String, IFile> mFiles = new HashMap<>();

  /** Reference to the file buffer manager. */
  private final ITextFileBufferManager mFileBufferManager = FileBuffers.getTextFileBufferManager();

  /** The progress monitor. */
  private IProgressMonitor mMonitor;

  /** Add the check rule name to the message. */
  private boolean mAddRuleName;

  /** Add the check module id to the message. */
  private boolean mAddModuleId;

  /**
   * Creates an auditor.
   *
   * @param checkConfiguration
   *          the check configuraton to use during audit.
   */
  public Auditor(ICheckConfiguration checkConfiguration) {
    mCheckConfiguration = checkConfiguration;

    //
    // check wether to include rule names and/or module id
    //
    mAddRuleName = CheckstylePluginPrefs.getBoolean(CheckstylePluginPrefs.PREF_INCLUDE_RULE_NAMES);
    mAddModuleId = CheckstylePluginPrefs.getBoolean(CheckstylePluginPrefs.PREF_INCLUDE_MODULE_IDS);
  }

  /**
   * Runs the audit on the files associated with the auditor.
   *
   * @param project
   *          the project is needed to build the correct classpath for the checker
   * @param monitor
   *          the progress monitor
   * @throws CheckstylePluginException
   *           error processing the audit
   */
  public void runAudit(IProject project, IProgressMonitor monitor)
          throws CheckstylePluginException {

    // System.out.println("----> Auditing: " + mFiles.size());

    // skip if there are no files to check
    if (project == null || mFiles.isEmpty()) {
      return;
    }

    mMonitor = monitor;

    Checker checker = null;
    CheckstyleAuditListener listener = null;

    try {

      List<File> filesToAudit = getFilesList();

      // begin task
      monitor.beginTask(NLS.bind(Messages.Auditor_msgCheckingConfig, mCheckConfiguration.getName()),
              filesToAudit.size());

      // create checker
      checker = CheckerFactory.createChecker(mCheckConfiguration, project);

      // create and add listener
      listener = new CheckstyleAuditListener(project);
      checker.addListener(listener);

      // run the files through the checker
      checker.process(filesToAudit);

    } catch (CheckstyleException ex) {
      if (ex.getCause() instanceof OperationCanceledException) {
        // user requested cancellation, keep silent
      } else {
        handleCheckstyleFailure(project, ex);
      }
    } catch (RuntimeException ex) {
      if (listener != null) {
        listener.cleanup();
      }
      throw ex;
    } finally {
      monitor.done();

      // Cleanup listener and filter
      if (checker != null) {
        checker.removeListener(listener);
      }
    }
  }

  private void handleCheckstyleFailure(IProject project, CheckstyleException error)
          throws CheckstylePluginException {
    try {

      CheckstyleLog.log(error);

      // remove pre-existing project level marker
      project.deleteMarkers(CheckstyleMarker.MARKER_ID, false, IResource.DEPTH_ZERO);

      Map<String, Object> attrs = new HashMap<>();
      attrs.put(IMarker.PRIORITY, Integer.valueOf(IMarker.PRIORITY_NORMAL));
      attrs.put(IMarker.SEVERITY, Integer.valueOf(IMarker.SEVERITY_ERROR));
      attrs.put(IMarker.MESSAGE, NLS.bind(Messages.Auditor_msgMsgCheckstyleInternalError, null));

      IMarker projectMarker = project.createMarker(CheckstyleMarker.MARKER_ID);
      projectMarker.setAttributes(attrs);
    } catch (CoreException nested) {
      CheckstylePluginException.rethrow(error);
    }
  }

  /**
   * Add a file to the audit.
   *
   * @param file
   *          the file
   */
  public void addFile(IFile file) {
    mFiles.put(file.getLocation().toString(), file);
  }

  /**
   * Get a file resource by the file name.
   *
   * @param fileName
   *          the file name
   * @return the file resource or <code>null</code>
   */
  private IFile getFile(String fileName) {
    return mFiles.get(new Path(fileName).toString());
  }

  /**
   * Helper method to get an array of java.io.Files. This array gets passed to the checker.
   */
  private List<File> getFilesList() {
    List<File> files = new ArrayList<>();
    for (IFile file : mFiles.values()) {
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

    /** the project. */
    private final IProject mProject;

    /** map containing the marker data. */
    private final Map<String, Object> mMarkerAttributes = new HashMap<>();

    /** flags if the amount of markers should be limited. */
    private final boolean mLimitMarkers;

    /** the max amount of markers per resource. */
    private final int mMarkerLimit;

    /** The file currently being checked. */
    private IResource mResource;

    /** Document model of the current file. */
    private IDocument mDocument;

    /** internal counter used to time to actualisation of the monitor. */
    private int mMonitorCounter;

    /** the count of markers generated for the current resource. */
    private int mMarkerCount;

    /**
     * keep track which file paths have been connected with the BufferManager.
     */
    private Set<IPath> mConnectedFileBufferPaths = new HashSet<>();

    public CheckstyleAuditListener(IProject project) {
      mProject = project;

      // init the marker limitation
      mLimitMarkers = CheckstylePluginPrefs
              .getBoolean(CheckstylePluginPrefs.PREF_LIMIT_MARKERS_PER_RESOURCE);
      mMarkerLimit = CheckstylePluginPrefs.getInt(CheckstylePluginPrefs.PREF_MARKER_AMOUNT_LIMIT);
    }

    @Override
    public void fileStarted(AuditEvent event) {

      if (mMonitor.isCanceled()) {
        throw new OperationCanceledException();
      }

      // get the current IFile reference
      mResource = getFile(event.getFileName());
      mMarkerCount = 0;

      if (mResource != null) {

        // begin subtask
        if (mMonitorCounter == 0) {
          mMonitor.subTask(NLS.bind(Messages.Auditor_msgCheckingFile, mResource.getName()));
        }

        // increment monitor-counter
        this.mMonitorCounter++;
      } else {

        IPath filePath = new Path(event.getFileName());
        IPath dirPath = filePath.removeFileExtension().removeLastSegments(1);

        IPath projectPath = mProject.getLocation();
        if (projectPath.isPrefixOf(dirPath)) {
          // find the resource with a project relative path
          mResource = mProject.findMember(dirPath.removeFirstSegments(projectPath.segmentCount()));
        } else {
          // if the resource is not inside the project, take project
          // as resource - this should not happen
          mResource = mProject;
        }
      }
    }

    @Override
    public void addError(AuditEvent error) {
      try {
        if (!mLimitMarkers || mMarkerCount < mMarkerLimit) {

          SeverityLevel severity = error.getSeverityLevel();

          if (mResource != null && !severity.equals(SeverityLevel.IGNORE)) {

            RuleMetadata metaData = MetadataFactory.getRuleMetadata(error.getSourceName());

            // create generic metadata if none can be found
            if (metaData == null) {
              Module module = new Module(error.getSourceName());
              metaData = MetadataFactory.createGenericMetadata(module);
            }

            mMarkerAttributes.put(CheckstyleMarker.MODULE_NAME, metaData.getInternalName());
            mMarkerAttributes.put(CheckstyleMarker.MESSAGE_KEY,
                    error.getViolation().getKey());
            String moduleId = error.getModuleId();
            if (StringUtils.isNotBlank(moduleId)) {
              mMarkerAttributes.put(CheckstyleMarker.MODULE_ID, moduleId);
            }
            mMarkerAttributes.put(IMarker.PRIORITY, Integer.valueOf(IMarker.PRIORITY_NORMAL));
            mMarkerAttributes.put(IMarker.SEVERITY, Integer.valueOf(getSeverityValue(severity)));
            mMarkerAttributes.put(IMarker.LINE_NUMBER, Integer.valueOf(error.getLine()));
            mMarkerAttributes.put(IMarker.MESSAGE, getMessage(error));

            // calculate offset for editor annotations
            calculateMarkerOffset(error, mMarkerAttributes);

            // enables own category under Java Problem Type
            // setting for Problems view (RFE 1530366)
            mMarkerAttributes.put("categoryId", Integer.valueOf(999)); //$NON-NLS-1$

            // create a marker for the actual resource
            IMarker marker = mResource.createMarker(CheckstyleMarker.MARKER_ID);
            marker.setAttributes(mMarkerAttributes);

            mMarkerCount++;

            // clear the marker attributes to reuse the map for the
            // next error
            mMarkerAttributes.clear();
          }
        }
      } catch (CoreException ex) {
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
      if (mMonitorCounter == MONITOR_INTERVAL) {
        mMonitor.worked(MONITOR_INTERVAL);
        mMonitorCounter = 0;
      }

      disconnectFileBuffer(mResource);
      mDocument = null;
    }

    @Override
    public void auditFinished(AuditEvent event) {
      cleanup();
    }

    @Override
    public void auditStarted(AuditEvent event) {
    }

    public void cleanup() {

      mDocument = null;

      // disconnect any leftover buffer paths, in case of an unexpected abortion
      for (IPath p : mConnectedFileBufferPaths) {
        disconnectFileBuffer(p);
      }
    }

    /**
     * Calculates the offset information for the editor annotations.
     *
     * @param error
     *          the audit error
     * @param markerAttributes
     *          the marker attributes
     */
    private void calculateMarkerOffset(AuditEvent error, Map<String, Object> markerAttributes) {

      // lazy create the document for the current file
      if (mDocument == null) {
        mDocument = connectFileBuffer(mResource);
      }

      // Provide offset information for the marker to make
      // annotated source code possible
      if (mDocument != null) {
        try {

          int line = error.getLine();

          IRegion lineInformation = mDocument.getLineInformation(line == 0 ? 0 : line - 1);
          int lineOffset = lineInformation.getOffset();
          int lineLength = lineInformation.getLength();

          // annotate from the error column until the end of
          // the line
          final int offset = error.getViolation().getColumnCharIndex();

          markerAttributes.put(IMarker.CHAR_START, Integer.valueOf(lineOffset + offset));
          markerAttributes.put(IMarker.CHAR_END, Integer.valueOf(lineOffset + lineLength));
        } catch (BadLocationException ex) {
          // seems to happen quite often so its no use to log since we
          // can't do anything about it
          // CheckstyleLog.log(e);
        }
      }
    }

    private IDocument connectFileBuffer(IResource resource) {

      if (!(resource instanceof IFile)) {
        return null;
      }

      IDocument document = null;

      try {
        IPath path = resource.getFullPath();
        mFileBufferManager.connect(path, new NullProgressMonitor());

        mConnectedFileBufferPaths.add(path);
        document = mFileBufferManager.getTextFileBuffer(path).getDocument();
      } catch (CoreException ex) {
        CheckstyleLog.log(ex);
      }
      return document;
    }

    private void disconnectFileBuffer(IResource resource) {

      if (!(resource instanceof IFile)) {
        return;
      }

      IPath path = mResource.getFullPath();
      disconnectFileBuffer(path);
    }

    private void disconnectFileBuffer(IPath path) {

      try {

        if (mConnectedFileBufferPaths.contains(path)) {
          mFileBufferManager.disconnect(path, new NullProgressMonitor());
          mConnectedFileBufferPaths.remove(path);
        }
      } catch (CoreException ex) {
        CheckstyleLog.log(ex);
      }
    }

    private int getSeverityValue(SeverityLevel severity) {
      int result = IMarker.SEVERITY_WARNING;

      if (severity.equals(SeverityLevel.INFO)) {
        result = IMarker.SEVERITY_INFO;
      } else if (severity.equals(SeverityLevel.WARNING)) {
        result = IMarker.SEVERITY_WARNING;
      } else if (severity.equals(SeverityLevel.ERROR)) {
        result = IMarker.SEVERITY_ERROR;
      }

      return result;
    }

    private String getMessage(AuditEvent error) {

      String moduleId = error.getModuleId();

      if (moduleId == null) {
        RuleMetadata metaData = MetadataFactory.getRuleMetadata(error.getSourceName());
        if (metaData != null) {
          moduleId = metaData.getInternalName();
        }
      }

      final String message = error.getMessage();

      StringBuilder prefix = new StringBuilder();
      if (mAddRuleName) {
        prefix.append(getRuleName(error));
      }
      if (mAddModuleId && error.getModuleId() != null) {
        if (prefix.length() > 0) {
          prefix.append(" - "); //$NON-NLS-1$
        }
        prefix.append(error.getModuleId());
      }

      StringBuilder buf = new StringBuilder();
      if (prefix.length() > 0) {
        buf.append(prefix).append(": "); //$NON-NLS-1$
      }
      buf.append(message);

      return buf.toString();
    }

    private String getRuleName(AuditEvent error) {
      RuleMetadata metaData = MetadataFactory.getRuleMetadata(error.getSourceName());
      if (metaData == null) {
        return Messages.Auditor_txtUnknownModule;
      }
      return metaData.getInternalName();
    }
  }
}
