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
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//============================================================================

package net.sf.eclipsecs.ui.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import net.sf.eclipsecs.core.util.CheckstyleLog;

/**
 * Wrapper of the Eclipse internal browser.
 */
public final class InternalBrowser {

  private InternalBrowser() {
    // utility class
  }

  /**
   * Open a link in an external browser, independent of the Eclipse browser settings.
   */
  public static final void openLinkInExternalBrowser(String url) {
    try {
      final IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser(IWorkbenchBrowserSupport.AS_EXTERNAL, null, null, null);
      browser.openURL(new URL(url));
    } catch (PartInitException | MalformedURLException ex) {
      CheckstyleLog.log(ex);
    }
  }
}
