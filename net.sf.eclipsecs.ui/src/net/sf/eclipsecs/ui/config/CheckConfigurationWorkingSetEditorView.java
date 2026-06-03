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

package net.sf.eclipsecs.ui.config;

import java.util.ArrayList;
import java.util.function.Predicate;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationFactory;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationWorkingSetEditor.ConfigurationLabelProvider;
import net.sf.eclipsecs.ui.config.CheckConfigurationWorkingSetEditorButtonBar.ButtonBarActions;

public final class CheckConfigurationWorkingSetEditorView extends Composite {

  private final CheckConfigurationWorkingSetEditorConfigTable configTable;
  private final CheckConfigurationWorkingSetEditorButtonBar buttonBar;
  private final Text mConfigurationDescription;
  private final TableViewer mUsageView;

  private final boolean global;

  public CheckConfigurationWorkingSetEditorView(Composite parent, int style,
          CheckConfigurationWorkingCopy[] configs, boolean global,
          ButtonBarActions buttonBarActions,
          Predicate<CheckConfigurationWorkingCopy> isDefaultConfig,
          ConfigurationLabelProvider configurationLabelProvider) {
    super(parent, style);
    GridLayoutFactory.fillDefaults().numColumns(3).applyTo(this);

    this.global = global;

    this.configTable = new CheckConfigurationWorkingSetEditorConfigTable(this, SWT.NULL, global,
            configs, configurationLabelProvider, buttonBarActions.configureCheckConfig(),
            config -> handleSelectionChanged(config, isDefaultConfig));
    GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(configTable);

    this.buttonBar = new CheckConfigurationWorkingSetEditorButtonBar(this, SWT.NULL, global,
            buttonBarActions);
    GridDataFactory.fillDefaults().span(1, 2).applyTo(buttonBar);

    Composite descArea = new Composite(this, SWT.NULL);
    GridLayoutFactory.fillDefaults().applyTo(descArea);
    GridDataFactory.fillDefaults().grab(true, true).span(global ? 1 : 2, 1).applyTo(descArea);

    Label lblDescription = new Label(descArea, SWT.NULL);
    lblDescription.setText(Messages.CheckstylePreferencePage_lblDescription);
    GridDataFactory.fillDefaults().applyTo(lblDescription);

    mConfigurationDescription = new Text(descArea,
            SWT.LEFT | SWT.WRAP | SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.VERTICAL);
    GridDataFactory.fillDefaults().grab(true, true).applyTo(mConfigurationDescription);

    if (global) {
      Composite usageArea = new Composite(this, SWT.NULL);
      GridLayoutFactory.fillDefaults().applyTo(usageArea);
      GridDataFactory.fillDefaults().applyTo(usageArea);

      Label lblUsage = new Label(usageArea, SWT.NULL);
      lblUsage.setText(Messages.CheckstylePreferencePage_lblProjectUsage);
      GridDataFactory.fillDefaults().applyTo(lblUsage);

      mUsageView = new TableViewer(usageArea);
      mUsageView.getControl().setBackground(usageArea.getBackground());
      mUsageView.setContentProvider(ArrayContentProvider.getInstance());
      mUsageView.setLabelProvider(new WorkbenchLabelProvider());
      GridDataFactory.fillDefaults().grab(true, true).applyTo(mUsageView.getControl());
    } else {
      mUsageView = null;
    }

    handleSelectionChanged(null, isDefaultConfig);
  }

  public CheckConfigurationWorkingCopy getSelectedConfig() {
     return configTable.getSelection();
  }

  public void setConfigs(CheckConfigurationWorkingCopy[] configs) {
    configTable.setConfigs(configs);
  }

  public void setSelection(CheckConfigurationWorkingCopy config) {
    configTable.setSelection(config);
  }

  public void refresh() {
    configTable.refresh();
  }

  private void handleSelectionChanged(CheckConfigurationWorkingCopy config,
          Predicate<CheckConfigurationWorkingCopy> isDefaultConfig) {
    boolean configSelected = config != null;
    if (configSelected) {
      mConfigurationDescription
              .setText(config.getDescription() != null ? config.getDescription() : ""); //$NON-NLS-1$

      if (global) {
        try {
          mUsageView.setInput(ProjectConfigurationFactory
                  .getProjectsUsingConfig(config.getSourceCheckConfiguration()));
        } catch (CheckstylePluginException ex) {
          CheckstyleLog.log(ex);
        }
      }
    } else {
      mConfigurationDescription.setText(""); //$NON-NLS-1$
      if (global) {
        mUsageView.setInput(new ArrayList<>());
      }
    }
    boolean configDefault = isDefaultConfig.test(config);
    buttonBar.setSelectionState(configSelected, configSelected && config.isEditable(),
            configDefault);
  }

}
