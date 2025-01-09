//============================================================================
//
// Copyright (C) 2003-2023 the original author or authors.
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

package net.sf.eclipsecs.ui;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import net.sf.eclipsecs.core.jobs.AbstractCheckJob;
import net.sf.eclipsecs.ui.properties.filter.CheckFileOnOpenPartListener;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

/**
 * Event handler being called when the eclipse application has started.
 */
@Component(property = EventConstants.EVENT_TOPIC + "=" + UIEvents.UILifeCycle.APP_STARTUP_COMPLETE)
public class ApplicationStartedHandler implements EventHandler {

  private final CheckFileOnOpenPartListener mPartListener = new CheckFileOnOpenPartListener();

  private final IWindowListener mWindowListener = new IWindowListener() {

    @Override
    public void windowOpened(IWorkbenchWindow window) {
      window.getPartService().addPartListener(mPartListener);
    }

    @Override
    public void windowActivated(IWorkbenchWindow window) {
    }

    @Override
    public void windowClosed(IWorkbenchWindow window) {
      window.getPartService().removePartListener(mPartListener);

    }

    @Override
    public void windowDeactivated(IWorkbenchWindow window) {
    }

  };

  @Override
  public void handleEvent(org.osgi.service.event.Event event) {
    if (!UIEvents.UILifeCycle.APP_STARTUP_COMPLETE.equals(event.getTopic())) {
      return;
    }
    registerListener();
    registerProgressIcon();
  }

  private void registerListener() {
    // add listeners for the Check-On-Open support
    final IWorkbench workbench = PlatformUI.getWorkbench();
    workbench.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {

        IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();

        for (IWorkbenchWindow window : windows) {

          if (window != null) {

            // collect open editors and have then run against Checkstyle if
            // appropriate
            Collection<IWorkbenchPartReference> parts = new HashSet<>();

            // add already opened files to the filter
            // bugfix for 2923044
            IWorkbenchPage[] pages = window.getPages();
            for (IWorkbenchPage page : pages) {

              IEditorReference[] editorRefs = page.getEditorReferences();
              Collections.addAll(parts, editorRefs);
            }

            mPartListener.partsOpened(parts);

            // remove listener first for safety, we don't want
            // register the same listener twice accidently
            window.getPartService().removePartListener(mPartListener);
            window.getPartService().addPartListener(mPartListener);
          }
        }

        workbench.addWindowListener(mWindowListener);
      }
    });
  }

  protected void registerProgressIcon() {
    IProgressService service = PlatformUI.getWorkbench().getProgressService();
    if (service == null) {
      return;
    }
    service.registerIconForFamily(CheckstyleUIPluginImages.CHECKSTYLE_ICON.getImageDescriptor(),
            AbstractCheckJob.CHECKSTYLE_JOB_FAMILY);
  }

}
