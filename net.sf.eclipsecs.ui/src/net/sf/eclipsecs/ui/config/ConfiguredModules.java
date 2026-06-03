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
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import net.sf.eclipsecs.core.config.Module;
import net.sf.eclipsecs.core.config.Severity;
import net.sf.eclipsecs.core.config.meta.RuleGroupMetadata;
import net.sf.eclipsecs.core.config.meta.RuleMetadata;
import net.sf.eclipsecs.ui.Messages;

public final class ConfiguredModules extends Composite {

  private final Group configuredModulesGroup;
  private final ConfiguredModulesTable table;

  private RuleGroupMetadata currentGroup;

  public ConfiguredModules(Composite parent, int style, boolean configurable,
          List<Module> modules, ConfiguredModulesCallbacks callbacks) {
    super(parent, style);
    GridLayoutFactory.fillDefaults().applyTo(this);

    this.configuredModulesGroup = new Group(this, SWT.NONE);
    GridDataFactory.fillDefaults().grab(true, true).applyTo(configuredModulesGroup);
    GridLayoutFactory.fillDefaults().applyTo(configuredModulesGroup);
    configuredModulesGroup.setText("\0");

    this.table = new ConfiguredModulesTable(configuredModulesGroup, SWT.NONE, new RuleGroupModuleFilter(), configurable,
            new TableCheckStateProvider(configurable), callbacks, modules);
    GridDataFactory.fillDefaults().grab(true, true).applyTo(table);

    Runnable removeModule = () -> callbacks.removeModule.accept(table.getSelectedModules());
    Runnable openModule = () -> {
      if (!table.getSelectedModules().isEmpty()) {
        callbacks.openModule.accept(table.getSelectedModules().getFirst());
      }
    };

    Composite buttons = new ConfiguredModulesButtons(configuredModulesGroup, SWT.NONE,
            configurable, removeModule, openModule);
    GridDataFactory.swtDefaults().applyTo(buttons);
  }

  public void refresh() {
    table.refresh();
  }

  public void setCurrentGroup(RuleGroupMetadata currentGroup) {
    this.currentGroup = currentGroup;
    this.configuredModulesGroup
            .setText(NLS.bind(Messages.CheckConfigurationConfigureDialog_lblConfiguredModules,
                    currentGroup.getGroupName()));
    table.refresh();
  }

  public record ConfiguredModulesCallbacks(Consumer<Module> openModule,
          Consumer<List<Module>> removeModule, Consumer<String> updateDescription,
          BiConsumer<Module, Boolean> checkStateChanged) {

  }

  /**
   * Viewer filter that includes all modules that belong to the currently selected group.
   *
   */
  private final class RuleGroupModuleFilter extends ViewerFilter {

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
      RuleMetadata rule = ((Module) element).getMetaData();
      return rule == null
              || currentGroup != null
                      && !rule.hidden()
                      && currentGroup.getGroupName().equals(rule.identity().group().getGroupName());

    }
  }

  private static final class TableCheckStateProvider implements ICheckStateProvider {

    private final boolean configurable;

    private TableCheckStateProvider(boolean configurable) {
      this.configurable = configurable;
    }

    @Override
    public boolean isGrayed(Object element) {
      return !configurable;
    }

    @Override
    public boolean isChecked(Object element) {
      Module module = (Module) element;
      return !Severity.IGNORE.equals(module.getSeverity()) || !module.getMetaData().hasSeverity();
    }
  }
}
