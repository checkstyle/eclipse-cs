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

package net.sf.eclipsecs.ui.preferences;

import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;

import com.puppycrawl.tools.checkstyle.Main;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.util.InternalBrowser;

public class CheckstylePreferencePageHeader extends Composite {

  private static final String CHECKSTYLE_VERSION = Main.class.getPackage().getImplementationVersion();

  public CheckstylePreferencePageHeader(Composite parent, int style) {
    super(parent, style);
    RowLayoutFactory.fillDefaults().applyTo(this);

    Link link = new Link(this, SWT.NONE);
    var text = NLS.bind(Messages.CheckstylePreferencePage_version, "<a>" + CHECKSTYLE_VERSION + "</a>");
    text = text.replace("Checkstyle", "<a>Checkstyle</a>");
    link.setText(text);
    link.addListener(SWT.Selection, this::linkClicked);
  }

  private void linkClicked(Event event) {
    String url = "https://checkstyle.org";
    if (Character.isDigit(event.text.charAt(0))) {
      url = url + "/releasenotes.html#Release_" + CHECKSTYLE_VERSION;
    }
    InternalBrowser.openLinkInExternalBrowser(url);
  }

}
