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
import java.util.regex.Pattern;

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
import net.sf.eclipsecs.ui.util.InternalBrowser;

public final class CheckConfigurationConfigureDialogView extends Composite {

  private static final Pattern PATTERN_INLINE_CODE = Pattern.compile(Pattern.quote("{@code ") + "([^}]*?)" + Pattern.quote("}"));

  private final Browser mBrowserDescription;
  private final AvailableModulesViewer availableModulesViewer;
  private final ConfiguredModules configuredModules;

  public CheckConfigurationConfigureDialogView(Composite parent, int style,
          CheckConfigurationConfigureDialogViewCallbacks callbacks, List<Module> mModules,
          boolean configurable) {
    super(parent, style);
    GridLayoutFactory.swtDefaults().applyTo(this);

    SashForm sashForm = new SashForm(this, SWT.NONE);
    GridDataFactory.fillDefaults().hint(700, 400).applyTo(sashForm);
    GridLayoutFactory.swtDefaults().applyTo(sashForm);

    Label lblDescription = new Label(this, SWT.NULL);
    lblDescription.setText(Messages.CheckConfigurationConfigureDialog_lblDescription);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(lblDescription);

    mBrowserDescription = new Browser(this, SWT.BORDER);
    GridDataFactory.create(GridData.FILL_BOTH).hint(SWT.DEFAULT, 100).applyTo(mBrowserDescription);
    mBrowserDescription.addLocationListener(LocationListener.changingAdapter(event -> {
      String url = event.location;
      if (url != null && url.startsWith("http")) {
        InternalBrowser.openLinkInExternalBrowser(url);
        event.doit = false;
      }
    }));

    this.availableModulesViewer = new AvailableModulesViewer(sashForm, SWT.NULL,
            new AvailableModulesViewerLabelProvider(mModules), configurable, callbacks.newModule,
            this::changeAvailableModuleSelection);
    GridDataFactory.create(GridData.FILL_BOTH).applyTo(availableModulesViewer);

    this.configuredModules = new ConfiguredModules(sashForm, SWT.NONE, configurable,
            mModules, new ConfiguredModulesCallbacks(callbacks.openModule, callbacks.removeModule,
                    this::setBrowserDescription, callbacks.checkStateChanged));
    GridDataFactory.fillDefaults().applyTo(configuredModules);

    sashForm.setWeights(new int[] {
        30,
        70,
    });
  }

  public void setBrowserDescription(String description) {
    mBrowserDescription.setText(description);
  }

  public void refreshConfiguredModules() {
    configuredModules.refresh();
  }

  public void refreshAvailableModules() {
    availableModulesViewer.refresh();
  }

  public void selectFirstAvailableGroup() {
    availableModulesViewer.selectFirstGroup();
  }

  public void focusAvailableModules() {
    availableModulesViewer.focus();
  }

  private void changeAvailableModuleSelection(Object selection) {
    String description = null;
    if (selection instanceof RuleGroupMetadata group) {
      description = group.getDescription();
      configuredModules.setCurrentGroup(group);
    } else if (selection instanceof RuleMetadata rule) {
      description = rule.identity().description();
      configuredModules.setCurrentGroup(rule.identity().group());
    }
    setBrowserDescription(getDescriptionHtml(description));
  }

  /**
   * Convert a module description to HTML for use with a browser component.
   * @param description module description
   * @return HTML converted description
   */
  public static String getDescriptionHtml(String description) {
    StringBuilder buf = new StringBuilder();
    buf.append("<html><body style=\"margin: 3px; font-size: 11px; ");
    buf.append("font-family: verdana, 'trebuchet MS', helvetica, sans-serif;\">");
    buf.append(description != null ? convertInlineCodeTags(description)
            : Messages.CheckConfigurationConfigureDialog_txtNoDescription);
    buf.append("</body></html>");
    return buf.toString();
  }

  private static String convertInlineCodeTags(String html) {
    return PATTERN_INLINE_CODE.matcher(html).replaceAll("<code>$1</code>");
  }

  public static final record CheckConfigurationConfigureDialogViewCallbacks(
          Consumer<List<RuleMetadata>> newModule, Consumer<Module> openModule,
          Consumer<List<Module>> removeModule, BiConsumer<Module, Boolean> checkStateChanged) {

  }

}
