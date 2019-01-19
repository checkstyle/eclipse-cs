//============================================================================
//
// Copyright (C) 2002-2016  David Schneider, Lars Ködderitzsch
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

package net.sf.eclipsecs.ui.properties.filter;

import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.eclipsecs.core.Messages;
import net.sf.eclipsecs.core.builder.CheckstyleMarker;
import net.sf.eclipsecs.core.jobs.RunCheckstyleOnFilesJob;
import net.sf.eclipsecs.core.nature.CheckstyleNature;
import net.sf.eclipsecs.core.projectconfig.IProjectConfiguration;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationFactory;
import net.sf.eclipsecs.core.projectconfig.filters.IFilter;
import net.sf.eclipsecs.core.projectconfig.filters.UnOpenedFilesFilter;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.part.FileEditorInput;

/**
 * PartListener implementation that listens for opening editor parts and runs Checkstyle on the
 * opened file if the UnOpenedFileFilter is active.
 *
 * @see <a href="https://sourceforge.net/p/eclipse-cs/feature-requests/93/">feature request</a>
 * @author Lars Ködderitzsch
 */
public class CheckFileOnOpenPartListener implements IPartListener2 {

  /**
   * Register multiple parts as opened at once. Used during workspace startup.
   *
   * @param parts
   *          the opened parts
   */
  public void partsOpened(Collection<IWorkbenchPartReference> parts) {

    new PartsOpenedJob(parts).schedule();
  }

  private class PartsOpenedJob extends WorkspaceJob implements ISchedulingRule {

    private Collection<IWorkbenchPartReference> mParts;

    public PartsOpenedJob(Collection<IWorkbenchPartReference> parts) {
      super(Messages.RunCheckstyleOnFilesJob_title);
      this.mParts = parts;
    }

    @Override
    public boolean contains(ISchedulingRule rule) {
      return false;
    }

    @Override
    public boolean isConflicting(ISchedulingRule rule) {
      return false;
    }

    @Override
    public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {

      List<IFile> filesToCheck = new ArrayList<>();

      for (IWorkbenchPartReference partRef : mParts) {

        IFile editorFile = getEditorFile(partRef);
        if (editorFile != null) {
          UnOpenedFilesFilter.addOpenedFile(editorFile);
        }

        // check if the opened part is a editor
        // and the editors file need to be checked
        if (editorFile != null && isFileAffected(editorFile)) {
          filesToCheck.add(editorFile);
        }
      }

      RunCheckstyleOnFilesJob job = new RunCheckstyleOnFilesJob(filesToCheck);
      job.schedule();

      return Status.OK_STATUS;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void partOpened(IWorkbenchPartReference partRef) {

    partsOpened(Collections.singleton(partRef));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void partClosed(IWorkbenchPartReference partRef) {

    IFile editorFile = getEditorFile(partRef);
    if (editorFile != null) {
      UnOpenedFilesFilter.removeOpenedFile(editorFile);
    }

    // if the UnOpenedFilesFilter is active and the editor closes
    // the markers of the current file need to be removed
    if (editorFile != null && isFileAffected(editorFile)) {
      try {
        editorFile.deleteMarkers(CheckstyleMarker.MARKER_ID, true, IResource.DEPTH_INFINITE);
      } catch (CoreException e) {
        CheckstyleLog.log(e);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void partActivated(IWorkbenchPartReference partRef) {
    // NOOP
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void partBroughtToTop(IWorkbenchPartReference partRef) {
    // NOOP
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void partDeactivated(IWorkbenchPartReference partRef) {
    // NOOP
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void partHidden(IWorkbenchPartReference partRef) {
    // NOOP
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void partInputChanged(IWorkbenchPartReference partRef) {
    // NOOP
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void partVisible(IWorkbenchPartReference partRef) {
    // NOOP
  }

  /**
   * Returns the file behind the referenced workbench part.
   *
   * @param partRef
   *          the workbench part in question
   * @return the editors file or <code>null</code> if the workbench part is no file based editor
   */
  private IFile getEditorFile(IWorkbenchPartReference partRef) {

    if (!(partRef instanceof IEditorReference)) {
      return null;
    }

    IFile file = null;
    IWorkbenchPart part = partRef.getPart(false); // fix for 3522695
    // do *NOT* restore the part here to prevent startup issues with large
    // number of opened files
    // instead use a different path the rip the input file reference

    IEditorInput input = null;

    if (part != null && part instanceof IEditorPart) {

      IEditorPart editor = (IEditorPart) part;
      input = editor.getEditorInput();
    } else {

      // fix for 3522695 - rip input file from editor ref without initializing
      // the actual part
      IEditorReference editRef = (IEditorReference) partRef;
      input = getRestoredInput(editRef);
    }

    if (input instanceof FileEditorInput) {
      file = ((FileEditorInput) input).getFile();
    }

    return file;
  }

  private IEditorInput getRestoredInput(IEditorReference e) {

    IMemento editorMem = null;
    if (CheckstyleUIPlugin.isE3()) {
      editorMem = getMementoE3(e);
    } else {
      editorMem = getMementoE4(e);
    }

    if (editorMem == null) {
      return null;
    }
    IMemento inputMem = editorMem.getChild(IWorkbenchConstants.TAG_INPUT);
    String factoryID = null;
    if (inputMem != null) {
      factoryID = inputMem.getString(IWorkbenchConstants.TAG_FACTORY_ID);
    }
    if (factoryID == null) {
      return null;
    }
    IAdaptable input = null;

    IElementFactory factory = PlatformUI.getWorkbench().getElementFactory(factoryID);
    if (factory == null) {
      return null;
    }

    input = factory.createElement(inputMem);
    if (input == null) {
      return null;
    }

    if (!(input instanceof IEditorInput)) {
      return null;
    }
    return (IEditorInput) input;
  }

  private IMemento getMementoE4(IEditorReference e) {

    try {

      // can't use this as long as were still supporting E3
      // org.eclipse.e4.ui.model.application.MApplicationElement model =
      // e.getModel();
      // Map<String, String> state = model.getPersistedState()

      Method getModelMethod = e.getClass().getMethod("getModel", new Class<?>[0]);
      getModelMethod.setAccessible(true);

      Object model = getModelMethod.invoke(e, (Object[]) null);

      Method getPersistedStateMethod = model.getClass().getMethod("getPersistedState",
              new Class<?>[0]);
      getPersistedStateMethod.setAccessible(true);

      @SuppressWarnings("unchecked")
      Map<String, String> state = (Map<String, String>) getPersistedStateMethod.invoke(model,
              (Object[]) null);

      String memento = state.get("memento");

      if (memento != null) {

        try {
          return XMLMemento.createReadRoot(new StringReader(memento));
        } catch (WorkbenchException e1) {
          CheckstyleLog.log(e1);
        }
      }
    } catch (NoSuchMethodException e1) {
      CheckstyleLog.log(e1);
    } catch (SecurityException e1) {
      CheckstyleLog.log(e1);
    } catch (IllegalAccessException e1) {
      CheckstyleLog.log(e1);
    } catch (InvocationTargetException e1) {
      CheckstyleLog.log(e1);
    }

    return null;

  }

  private IMemento getMementoE3(IEditorReference e) {

    try {

      // the direct method vanished from E4 EditorReference class
      // in order to build on E4 we need to do this the dirty way via reflection
      Method getMementoMethod = e.getClass().getMethod("getMemento", new Class<?>[0]);
      getMementoMethod.setAccessible(true);

      IMemento memento = (IMemento) getMementoMethod.invoke(e, (Object[]) null);
      return memento;
    } catch (NoSuchMethodException e1) {
      CheckstyleLog.log(e1);
    } catch (SecurityException e1) {
      CheckstyleLog.log(e1);
    } catch (IllegalAccessException e1) {
      CheckstyleLog.log(e1);
    } catch (InvocationTargetException e1) {
      CheckstyleLog.log(e1);
    }
    return null;
  }

  /**
   * Checks if the given file is affected by the UnOpenedFilesFilter and needs to be handled on
   * editor open/close.
   *
   * @param file
   *          the file to check
   * @return <code>true</code> if the file is affected, <code>false</code> otherwise
   */
  private boolean isFileAffected(IFile file) {

    boolean affected = false;

    IProject project = file.getProject();

    try {
      // check if checkstyle is enabled on the project
      if (project.isAccessible() && project.hasNature(CheckstyleNature.NATURE_ID)) {

        IProjectConfiguration config = ProjectConfigurationFactory.getConfiguration(project);

        // now check if the UnOpenedFilesFilter is active
        boolean unOpenedFilesFilterActive = false;
        boolean filtered = false;
        List<IFilter> filters = config.getFilters();
        for (IFilter filter : filters) {

          if (filter instanceof UnOpenedFilesFilter && ((UnOpenedFilesFilter) filter).isEnabled()) {
            unOpenedFilesFilterActive = true;
          }

          // check if the file would be filtered out
          if (filter.isEnabled() && !(filter instanceof UnOpenedFilesFilter)) {
            filtered = filtered || !filter.accept(file);
          }
        }

        affected = unOpenedFilesFilterActive && !filtered;
      }
    } catch (CoreException e) {
      // should never happen, since editor cannot be open
      // when project isn't
      CheckstyleLog.log(e);
    } catch (CheckstylePluginException e) {
      CheckstyleLog.log(e);
    }

    return affected;
  }
}
