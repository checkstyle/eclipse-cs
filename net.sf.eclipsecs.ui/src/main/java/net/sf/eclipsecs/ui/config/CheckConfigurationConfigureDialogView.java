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

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import net.sf.eclipsecs.core.config.Module;
import net.sf.eclipsecs.core.config.meta.RuleGroupMetadata;
import net.sf.eclipsecs.core.config.meta.RuleMetadata;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.AvailableModulesViewer.AvailableModulesViewerLabelProvider;
import net.sf.eclipsecs.ui.config.ConfiguredModules.ConfiguredModulesCallbacks;
import net.sf.eclipsecs.ui.util.HtmlUtil;
import net.sf.eclipsecs.ui.util.InternalBrowser;

/**
 * Composite building the layout of the module configuration dialog, combining the available modules
 * tree, the configured modules panel and a browser showing module descriptions.
 *
 */
public final class CheckConfigurationConfigureDialogView extends Composite {

    /** Max height of the available and configured module panels. */
    private static final int MAX_HEIGHT = 400;

    /** The browser displaying module descriptions. */
    private final Browser mBrowserDescription;
    /** The available modules viewer. */
    private final AvailableModulesViewer availableModulesViewer;
    /** The configured modules panel. */
    private final ConfiguredModules configuredModules;

    /**
     * Creates the module configuration dialog view.
     *
     * @param parent
     *            the parent composite
     * @param style
     *            the SWT style bits
     * @param callbacks
     *            the callbacks used by the dialog view
     * @param mModules
     *            the list of configured modules
     * @param configurable
     *            whether the configuration is editable
     */
    public CheckConfigurationConfigureDialogView(Composite parent, int style,
        CheckConfigurationConfigureDialogViewCallbacks callbacks, List<Module> mModules,
        boolean configurable) {
        super(parent, style);
        GridLayoutFactory.swtDefaults().applyTo(this);

        final SashForm sashForm = new SashForm(this, SWT.NONE);
        GridDataFactory.fillDefaults().hint(SWT.DEFAULT, MAX_HEIGHT).applyTo(sashForm);
        GridLayoutFactory.swtDefaults().applyTo(sashForm);

        final Label lblDescription = new Label(this, SWT.NULL);
        lblDescription.setText(Messages.CheckConfigurationConfigureDialog_lblDescription);
        GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(lblDescription);

        mBrowserDescription = new Browser(this, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(mBrowserDescription);
        mBrowserDescription.addLocationListener(LocationListener.changingAdapter(event -> {
            final String url = event.location;
            if (url != null && url.startsWith("http")) {
                InternalBrowser.openLinkInExternalBrowser(url);
                event.doit = false;
            }
        }));

        this.availableModulesViewer = new AvailableModulesViewer(sashForm, SWT.NULL,
            new AvailableModulesViewerLabelProvider(mModules), configurable, callbacks.newModule,
            this::changeAvailableModuleSelection);
        GridDataFactory.fillDefaults().applyTo(availableModulesViewer);

        this.configuredModules = new ConfiguredModules(sashForm, SWT.NONE, configurable, mModules,
            new ConfiguredModulesCallbacks(callbacks.openModule, callbacks.removeModule,
                this::setBrowserDescription, callbacks.checkStateChanged));
        GridDataFactory.fillDefaults().applyTo(configuredModules);

        sashForm.setWeights(new int[] {
            1, 2,
        });
    }

    /**
     * Sets the text shown in the module description browser.
     *
     * @param description
     *            the description to display
     */
    public void setBrowserDescription(String description) {
        mBrowserDescription.setText(description);
    }

    /**
     * Refreshes the display of the configured modules.
     */
    public void refreshConfiguredModules() {
        configuredModules.refresh();
    }

    /**
     * Refreshes the display of the available modules.
     */
    public void refreshAvailableModules() {
        availableModulesViewer.refresh();
    }

    /**
     * Selects the first group in the available modules tree.
     */
    public void selectFirstAvailableGroup() {
        availableModulesViewer.selectFirstGroup();
    }

    /**
     * Sets focus to the available modules viewer.
     */
    public void focusAvailableModules() {
        availableModulesViewer.focus();
    }

    /**
     * Updates the browser description and configured group when the available module selection
     * changes.
     *
     * @param selection
     *            the newly selected module element
     */
    private void changeAvailableModuleSelection(Object selection) {
        String description = null;
        if (selection instanceof RuleGroupMetadata group) {
            description = group.getDescription();
            configuredModules.setCurrentGroup(group);
        }
        else if (selection instanceof RuleMetadata rule) {
            description = rule.identity().description();
            configuredModules.setCurrentGroup(rule.identity().group());
        }
        setBrowserDescription(HtmlUtil.getDescriptionHtml(description));
    }

    /**
     * Record containing the callbacks used by the module configuration dialog view.
     *
     * @param newModule
     *          action to add the modules selected in the available modules tree
     * @param openModule
     *          action to edit the module selected in the configured modules table
     * @param removeModule
     *          action to delete the modules selected in the configured modules table
     * @param checkStateChanged
     *          callback invoked when the checked state of a module changes
     */
    public record CheckConfigurationConfigureDialogViewCallbacks(
        Consumer<List<RuleMetadata>> newModule, Consumer<Module> openModule,
        Consumer<List<Module>> removeModule, BiConsumer<Module, Boolean> checkStateChanged) {

    }

}
