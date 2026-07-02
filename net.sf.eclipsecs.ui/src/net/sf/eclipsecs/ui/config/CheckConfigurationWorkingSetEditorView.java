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
import java.util.Optional;

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
import net.sf.eclipsecs.ui.config.CheckConfigurationWorkingSetEditor.CheckConfigurationWorkingSetEditorModel;
import net.sf.eclipsecs.ui.config.CheckConfigurationWorkingSetEditorButtonBar.ButtonBarActions;

public final class CheckConfigurationWorkingSetEditorView extends Composite {

    /** The config table. */
    private final CheckConfigurationWorkingSetEditorConfigTable configTable;
    /** The button bar. */
    private final CheckConfigurationWorkingSetEditorButtonBar buttonBar;
    /** The text field for the configuration description. */
    private final Text mConfigurationDescription;
    /** The table viewer for project usage. */
    private final TableViewer mUsageView;
    /** The model for this view. */
    private final CheckConfigurationWorkingSetEditorModel model;

    public CheckConfigurationWorkingSetEditorView(Composite parent, int style,
        CheckConfigurationWorkingSetEditorModel model, ButtonBarActions buttonBarActions) {
        super(parent, style);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(this);

        this.model = model;

        this.configTable = new CheckConfigurationWorkingSetEditorConfigTable(this, SWT.NULL, model,
            buttonBarActions.configureCheckConfig(), this::handleSelectionChanged);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(configTable);

        this.buttonBar = new CheckConfigurationWorkingSetEditorButtonBar(this, SWT.NULL,
            model.global(), buttonBarActions);
        GridDataFactory.fillDefaults().span(1, 2).applyTo(buttonBar);

        final Composite descAndUsageArea = new Composite(this, SWT.NULL);
        GridLayoutFactory.fillDefaults().numColumns(model.global() ? 2 : 1)
            .applyTo(descAndUsageArea);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(descAndUsageArea);

        final Composite descArea = new Composite(descAndUsageArea, SWT.NULL);
        GridLayoutFactory.fillDefaults().applyTo(descArea);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(descArea);

        final Label lblDescription = new Label(descArea, SWT.NULL);
        lblDescription.setText(Messages.CheckstylePreferencePage_lblDescription);
        GridDataFactory.fillDefaults().applyTo(lblDescription);

        mConfigurationDescription = new Text(descArea,
            SWT.LEFT | SWT.WRAP | SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.VERTICAL);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(mConfigurationDescription);

        mUsageView = makeUsageView(descAndUsageArea, model.global()).orElse(null);

        handleSelectionChanged(null);
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

    private static Optional<TableViewer> makeUsageView(Composite parent, boolean global) {
        TableViewer usageView = null;
        if (global) {
            final Composite usageArea = new Composite(parent, SWT.NULL);
            GridLayoutFactory.fillDefaults().applyTo(usageArea);
            GridDataFactory.fillDefaults().applyTo(usageArea);

            final Label lblUsage = new Label(usageArea, SWT.NULL);
            lblUsage.setText(Messages.CheckstylePreferencePage_lblProjectUsage);
            GridDataFactory.fillDefaults().applyTo(lblUsage);

            usageView = new TableViewer(usageArea);
            usageView.getControl().setBackground(usageArea.getBackground());
            usageView.setContentProvider(ArrayContentProvider.getInstance());
            usageView.setLabelProvider(new WorkbenchLabelProvider());
            GridDataFactory.fillDefaults().grab(true, true).applyTo(usageView.getControl());
        }
        return Optional.ofNullable(usageView);
    }

    private void handleSelectionChanged(CheckConfigurationWorkingCopy config) {
        final boolean configSelected = config != null;
        if (configSelected) {
            mConfigurationDescription
                .setText(config.getDescription() != null ? config.getDescription() : "");

            if (model.global()) {
                try {
                    mUsageView.setInput(ProjectConfigurationFactory
                        .getProjectsUsingConfig(config.getSourceCheckConfiguration()));
                } catch (CheckstylePluginException ex) {
                    CheckstyleLog.log(ex);
                }
            }
        } else {
            mConfigurationDescription.setText("");
            if (model.global()) {
                mUsageView.setInput(new ArrayList<>());
            }
        }
        final boolean configDefault = model.isDefaultConfig().test(config);
        buttonBar.setSelectionState(configSelected, configSelected && config.isEditable(),
            configDefault);
    }

}
