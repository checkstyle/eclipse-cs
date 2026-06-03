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

package net.sf.eclipsecs.ui.config.configtypes;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import net.sf.eclipsecs.ui.Messages;

public final class InternalConfigurationEditorView extends Composite {

  private final Text mConfigName;
  private final Text mLocation;
  private final Text mDescription;

  public InternalConfigurationEditorView(Composite parent, int style, Runnable importConfig) {
    super(parent, style);
    GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).applyTo(this);

    Label lblConfigName = new Label(this, SWT.NULL);
    lblConfigName.setText(Messages.CheckConfigurationPropertiesDialog_lblName);
    GridDataFactory.swtDefaults().applyTo(lblConfigName);

    mConfigName = new Text(this, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(mConfigName);
    mConfigName.setFocus();

    Label lblConfigLocation = new Label(this, SWT.NULL);
    lblConfigLocation.setText(Messages.CheckConfigurationPropertiesDialog_lblLocation);
    GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(lblConfigLocation);

    mLocation = new Text(this, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
    mLocation.setEditable(false);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(mLocation);

    Label lblDescription = new Label(this, SWT.NULL);
    lblDescription.setText(Messages.CheckConfigurationPropertiesDialog_lblDescription);
    GridDataFactory.swtDefaults().span(2, 1).applyTo(lblDescription);

    mDescription = new Text(this, SWT.LEFT | SWT.WRAP | SWT.MULTI | SWT.BORDER | SWT.VERTICAL);
    GridDataFactory.create(GridData.FILL_BOTH).span(2, 1).hint(300, 100).grab(true, true).applyTo(mDescription);

    Button mBtnImport = new Button(this, SWT.PUSH);
    mBtnImport.setText(Messages.InternalConfigurationEditor_btnImport);
    GridDataFactory.swtDefaults().span(2, 1).align(GridData.END, GridData.CENTER).applyTo(mBtnImport);

    mBtnImport.addSelectionListener(SelectionListener.widgetSelectedAdapter(
            event -> importConfig.run()));
  }

  public String getConfigName() {
    return mConfigName.getText();
  }

  public String getDescription() {
    return mDescription.getText();
  }

  public void setConfigName(String configName) {
    mConfigName.setText(configName);
  }

  public void setDescription(String description) {
    mDescription.setText(description);
  }

  public void setConfigLocation(String location) {
    mLocation.setText(location);
  }

}
