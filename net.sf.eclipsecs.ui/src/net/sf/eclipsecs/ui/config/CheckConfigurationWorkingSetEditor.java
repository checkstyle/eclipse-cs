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

package net.sf.eclipsecs.ui.config;

import java.io.File;
import java.util.function.Predicate;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.GlobalCheckConfigurationWorkingSet;
import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.config.ICheckConfigurationWorkingSet;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationWorkingSetEditorButtonBar.ButtonBarActions;

/**
 * This class provides the editor GUI for a check configuration working set.
 *
 */
public final class CheckConfigurationWorkingSetEditor extends Composite {

    //
    // attributes
    //

    /** The working set being edited. */
    private final ICheckConfigurationWorkingSet mWorkingSet;
    /** The editor view. */
    private final CheckConfigurationWorkingSetEditorView editorView;

    //
    // constructors
    //

    /**
     * Creates the configuration working set editor.
     *
     * @param parent
     *            the parent composite
     * @param style
     *            the style bits
     * @param workingSet
     *            the configuration working set to edit
     */
    public CheckConfigurationWorkingSetEditor(Composite parent, int style,
        ICheckConfigurationWorkingSet workingSet) {
        super(parent, style);

        mWorkingSet = workingSet;

        GridLayoutFactory.fillDefaults().applyTo(this);

        boolean global = mWorkingSet instanceof GlobalCheckConfigurationWorkingSet;

        final CheckConfigurationWorkingSetEditorModel model =
            new CheckConfigurationWorkingSetEditorModel(mWorkingSet.getWorkingCopies(), global,
                this::isDefaultConfig, getTableSettings());

        editorView = new CheckConfigurationWorkingSetEditorView(this, SWT.NONE, model,
            new ButtonBarActions(this::addCheckConfig, this::editCheckConfig,
                this::configureCheckConfig, this::copyCheckConfig, this::removeCheckConfig,
                this::setDefaultCheckConfig, this::exportCheckstyleCheckConfig));
        GridDataFactory.fillDefaults().grab(true, true).applyTo(editorView);
    }

    private boolean isDefaultConfig(CheckConfigurationWorkingCopy config) {
        boolean configDefault = false;
        if (mWorkingSet instanceof GlobalCheckConfigurationWorkingSet globalWorkingSet) {
            CheckConfigurationWorkingCopy defaultConfig = globalWorkingSet.getDefaultCheckConfig();
            configDefault = defaultConfig != null && defaultConfig.equals(config);
        }
        return configDefault;
    }

    /**
     * Create a new Check configuration.
     */
    private void addCheckConfig() {
        CheckConfigurationPropertiesDialog dialog =
            new CheckConfigurationPropertiesDialog(getShell(), null, mWorkingSet);
        dialog.setBlockOnOpen(true);
        if (Window.OK == dialog.open()) {

            CheckConfigurationWorkingCopy newConfig = dialog.getCheckConfiguration();
            mWorkingSet.addCheckConfiguration(newConfig);

            editorView.setConfigs(mWorkingSet.getWorkingCopies());
            editorView.setSelection(newConfig);
        }
    }

    /**
     * Edit the properties of a check configuration.
     */
    private void editCheckConfig() {
        CheckConfigurationWorkingCopy config = editorView.getSelectedConfig();
        if (config != null) {
            CheckConfigurationPropertiesDialog dialog =
                new CheckConfigurationPropertiesDialog(getShell(), config, mWorkingSet);
            dialog.setBlockOnOpen(true);
            if (Window.OK == dialog.open()) {
                editorView.refresh();
            }
        }
    }

    private void configureCheckConfig() {
        CheckConfigurationWorkingCopy config = editorView.getSelectedConfig();

        if (config != null) {

            try {
                // test if file exists
                config.getCheckstyleConfiguration();

                CheckConfigurationConfigureDialog dialog =
                    new CheckConfigurationConfigureDialog(getShell(), config);
                dialog.setBlockOnOpen(true);
                dialog.open();
            }
            catch (CheckstylePluginException ex) {
                CheckstyleUIPlugin.warningDialog(getShell(),
                    NLS.bind(Messages.errorCannotResolveCheckLocation, config.getLocation(),
                        config.getName()),
                    ex);
            }
        }
    }

    /**
     * Copy an existing config.
     */
    private void copyCheckConfig() {
        ICheckConfiguration sourceConfig = editorView.getSelectedConfig();
        if (sourceConfig != null) {
            try {

                // Open the properties dialog to change default name and description
                CheckConfigurationPropertiesDialog dialog =
                    new CheckConfigurationPropertiesDialog(getShell(), null, mWorkingSet);
                dialog.setTemplateConfiguration(sourceConfig);

                dialog.setBlockOnOpen(true);
                if (Window.OK == dialog.open()) {

                    CheckConfigurationWorkingCopy newConfig = dialog.getCheckConfiguration();

                    // Copy the source configuration into the new internal config
                    sourceConfig.copyConfiguration(newConfig);

                    mWorkingSet.addCheckConfiguration(newConfig);

                    editorView.setConfigs(mWorkingSet.getWorkingCopies());
                }
            }
            catch (CheckstylePluginException ex) {
                CheckstyleUIPlugin.errorDialog(getShell(), ex, true);
            }
        }
    }

    /**
     * Remove a config.
     */
    private void removeCheckConfig() {
        CheckConfigurationWorkingCopy checkConfig = editorView.getSelectedConfig();
        if (checkConfig != null && checkConfig.isEditable()) {
            boolean confirm = MessageDialog.openQuestion(getShell(),
                Messages.CheckstylePreferencePage_titleDelete,
                NLS.bind(Messages.CheckstylePreferencePage_msgDelete, checkConfig.getName()));
            if (confirm) {

                //
                // Make sure the check config is not in use. Don't let it be
                // deleted if it is.
                //
                if (mWorkingSet.removeCheckConfiguration(checkConfig)) {

                    editorView.setConfigs(mWorkingSet.getWorkingCopies());
                }
                else {
                    MessageDialog.openInformation(getShell(),
                        Messages.CheckstylePreferencePage_titleCantDelete,
                        NLS.bind(Messages.CheckstylePreferencePage_msgCantDelete,
                            checkConfig.getName()));
                }
            }
        }
    }

    private void setDefaultCheckConfig() {
        CheckConfigurationWorkingCopy checkConfig = editorView.getSelectedConfig();
        if (checkConfig != null) {
            if (mWorkingSet instanceof GlobalCheckConfigurationWorkingSet) {
                ((GlobalCheckConfigurationWorkingSet) mWorkingSet)
                    .setDefaultCheckConfig(checkConfig);
            }

            editorView.refresh();
        }
    }

    /**
     * Export a configuration.
     */
    private void exportCheckstyleCheckConfig() {
        ICheckConfiguration config = editorView.getSelectedConfig();
        if (config != null) {
            FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
            dialog.setText(Messages.CheckstylePreferencePage_titleExportConfig);
            String path = dialog.open();
            if (path != null) {
                File file = new File(path);

                try {
                    config.exportConfiguration(file);
                }
                catch (CheckstylePluginException ex) {
                    CheckstyleUIPlugin.errorDialog(getShell(), Messages.msgErrorFailedExportConfig,
                        ex, true);
                }
            }
        }
    }

    private IDialogSettings getTableSettings() {
        final String concreteViewId = mWorkingSet.getClass().getName();

        final IDialogSettings workbenchSettings =
            CheckstyleUIPlugin.getDefault().getDialogSettings();
        IDialogSettings settings = workbenchSettings.getSection(concreteViewId);

        if (settings == null) {
            settings = workbenchSettings.addNewSection(concreteViewId);
        }

        return settings;
    }

    public record CheckConfigurationWorkingSetEditorModel(
        CheckConfigurationWorkingCopy[] configs,
        boolean global,
        Predicate<CheckConfigurationWorkingCopy> isDefaultConfig,
        IDialogSettings tableSettings) {
    }
}
