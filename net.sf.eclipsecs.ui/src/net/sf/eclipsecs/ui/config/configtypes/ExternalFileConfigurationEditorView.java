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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import net.sf.eclipsecs.ui.Messages;

public final class ExternalFileConfigurationEditorView extends Composite {

  private final Text mConfigName;
  private final Text location;
  private final Text mDescription;
  private final Button mChkProtectConfig;

  public ExternalFileConfigurationEditorView(Composite parent, int style) {
    super(parent, style);
    GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).margins(0, 0).applyTo(this);

    Label lblConfigName = new Label(this, SWT.NULL);
    lblConfigName.setText(Messages.CheckConfigurationPropertiesDialog_lblName);
    GridDataFactory.swtDefaults().applyTo(lblConfigName);

    mConfigName = new Text(this, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(mConfigName);
    mConfigName.setFocus();

    location = createLocationSection(this, parent.getShell());

    Label lblDescription = new Label(this, SWT.NULL);
    lblDescription.setText(Messages.CheckConfigurationPropertiesDialog_lblDescription);
    GridDataFactory.swtDefaults().span(2, 1).applyTo(lblDescription);

    mDescription = new Text(this, SWT.LEFT | SWT.WRAP | SWT.MULTI | SWT.BORDER | SWT.VERTICAL);
    GridDataFactory.create(GridData.FILL_BOTH).span(2, 1).hint(300, 100).grab(true, true).applyTo(mDescription);

    Group advancedGroup = new Group(this, SWT.NULL);
    advancedGroup.setText(Messages.RemoteConfigurationEditor_titleAdvancedOptions);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).span(2, 1).applyTo(advancedGroup);
    GridLayoutFactory.swtDefaults().numColumns(2).applyTo(advancedGroup);

    mChkProtectConfig = new Button(advancedGroup, SWT.CHECK);
    mChkProtectConfig.setText(Messages.ExternalFileConfigurationEditor_btnProtectConfigFile);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).span(2, 1).applyTo(mChkProtectConfig);
  }

  public String getConfigName() {
    return mConfigName.getText();
  }

  public String getDescription() {
    return mDescription.getText();
  }

  public String getConfigLocation() {
    return location.getText();
  }

  public boolean getProtectConfig() {
    return mChkProtectConfig.getSelection();
  }

  public void setConfigName(String configName) {
    mConfigName.setText(configName);
  }

  public void setDescription(String description) {
    mDescription.setText(description);
  }

  public void setConfigLocation(String strLocation) {
    location.setText(strLocation);
  }

  public void setProtectConfig(boolean protectConfig) {
    mChkProtectConfig.setSelection(protectConfig);
  }

  private static Text createLocationSection(Composite parent, Shell shell) {
    Label lblConfigLocation = new Label(parent, SWT.NULL);
    lblConfigLocation.setText(Messages.CheckConfigurationPropertiesDialog_lblLocation);
    GridDataFactory.swtDefaults().applyTo(lblConfigLocation);

    Composite locationComposite = new Composite(parent, SWT.NULL);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(locationComposite);
    GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).margins(0, 0).applyTo(locationComposite);

    Text location = new Text(locationComposite, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(location);

    Button btnBrowse = new Button(locationComposite, SWT.PUSH);
    btnBrowse.setText(Messages.FileConfigurationLocationEditor_btnBrowse);
    GridDataFactory.swtDefaults().applyTo(btnBrowse);

    btnBrowse.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
      FileDialog fileDialog = new FileDialog(shell);
      fileDialog.setFileName(location.getText());

      String file = fileDialog.open();
      if (file != null) {
        location.setText(file);
      }
    }));

    return location;
  }

}
