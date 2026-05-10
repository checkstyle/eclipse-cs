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
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

import net.sf.eclipsecs.core.config.Module;
import net.sf.eclipsecs.core.config.meta.MetadataFactory;
import net.sf.eclipsecs.core.config.meta.RuleGroupMetadata;
import net.sf.eclipsecs.core.config.meta.RuleMetadata;
import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.Messages;

class AvailableModulesViewer extends Composite {

  private final FilteredTree treeViewer;
  private final Button addButton;
  private final Consumer<List<RuleMetadata>> newModule;

  AvailableModulesViewer(Composite parent, int style, List<Module> modules, boolean configurable,
          Consumer<List<RuleMetadata>> newModule, Consumer<Object> selectionChanged) {
    super(parent, style);
    this.newModule = newModule;

    setLayout(new FillLayout());

    Group knownModules = new Group(this, SWT.NULL);
    knownModules.setLayout(new GridLayout());
    knownModules.setText(Messages.CheckConfigurationConfigureDialog_lblAvailableModules);

    this.treeViewer = new FilteredTree(knownModules,
            SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, new ModulePatternFilter(), true,
            true);
    treeViewer.getViewer().getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
    treeViewer.getViewer().setContentProvider(new MetaDataContentProvider());
    treeViewer.getViewer().setLabelProvider(new MetaDataLabelProvider(modules));
    treeViewer.getViewer().setComparator(new ViewerComparator());
    treeViewer.getViewer().addSelectionChangedListener(event -> {
      selectionChanged.accept(event.getStructuredSelection().getFirstElement());
    });
    treeViewer.getViewer().addDoubleClickListener(event -> {
      IStructuredSelection selection = (IStructuredSelection) event.getSelection();
      Object element = selection.getFirstElement();

      if (element instanceof RuleGroupMetadata) {
        treeViewer.getViewer().setExpandedState(element, !treeViewer.getViewer().getExpandedState(element));
      } else if (configurable) {
        newModule((IStructuredSelection) event.getSelection());
      }
    });

    // filter hidden elements
    treeViewer.getViewer().addFilter(new ViewerFilter() {

      @Override
      public boolean select(Viewer viewer, Object parentElement, Object element) {
        boolean passes = true;
        if (element instanceof RuleGroupMetadata) {
          passes = !((RuleGroupMetadata) element).isHidden();
        } else if (element instanceof RuleMetadata) {
          passes = !((RuleMetadata) element).hidden();
        }
        return passes;
      }
    });

    this.addButton = new Button(knownModules, SWT.PUSH);
    addButton.setText(Messages.CheckConfigurationConfigureDialog_btnAdd);
    GridData gridData = new GridData();
    gridData.horizontalAlignment = GridData.END;
    addButton.setLayoutData(gridData);
    if (configurable) {
      treeViewer.addKeyListener(KeyListener.keyReleasedAdapter(event -> {
        if (event.keyCode == SWT.ARROW_RIGHT || event.character == ' ') {
          if (treeViewer.getViewer().getStructuredSelection()
                  .getFirstElement() instanceof RuleMetadata) {
            newModule(treeViewer.getViewer().getStructuredSelection());
          }
        }
      }));
      addButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(
              event -> newModule(treeViewer.getViewer().getStructuredSelection())));
    } else {
      addButton.setEnabled(false);
    }

    treeViewer.getViewer().setInput(MetadataFactory.getRuleGroupMetadata());
  }

  private void newModule(IStructuredSelection selection) {
    List<RuleMetadata> rules = new ArrayList<>();
    for (Object element : selection) {
      if (element instanceof RuleGroupMetadata group) {
        // if group is selected add all modules from this group
        rules.addAll(group.getRuleMetadata());

      } else if (element instanceof RuleMetadata rule) {
        rules.add(rule);
      }
    }
    newModule.accept(rules);
  }

  public void refresh() {
    treeViewer.getViewer().refresh();
  }

  public void focus() {
    treeViewer.getViewer().getTree().forceFocus();
  }

  public void setSelection(ISelection selection) {
    treeViewer.getViewer().setSelection(selection);
  }

  public void selectFirstGroup() {
    List<RuleGroupMetadata> groups = MetadataFactory.getRuleGroupMetadata();
    if (!groups.isEmpty()) {
      treeViewer.getViewer().setSelection(new StructuredSelection(groups.get(0)), true);
    }
  }

  /**
   * Filter implementation that filters the module tree with respect of a filter text field to input
   * a search word.
   *
   */
  private static class ModulePatternFilter extends PatternFilter {

    private ModulePatternFilter() {
      setIncludeLeadingWildcard(true);
    }

    @Override
    protected boolean isLeafMatch(Viewer viewer, Object element) {
      return element instanceof RuleMetadata rule
              && (wordMatches(rule.identity().ruleName())
                      || wordMatches(rule.identity().internalName())
                      || wordMatches(rule.identity().description()));
    }
  }

  /**
   * TreeContentProvider that provides the structure of the rule metadata.
   *
   */
  static class MetaDataContentProvider implements ITreeContentProvider {

    @Override
    public Object[] getElements(Object inputElement) {
      Object[] ruleGroups = null;
      if (inputElement instanceof List) {
        ruleGroups = ((List<?>) inputElement).toArray();
      }
      return ruleGroups;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
      Object[] children = null;
      if (parentElement instanceof List) {
        children = getElements(parentElement);
      } else if (parentElement instanceof RuleGroupMetadata) {
        children = ((RuleGroupMetadata) parentElement).getRuleMetadata().toArray();
      }

      return children;
    }

    @Override
    public Object getParent(Object element) {
      Object parent = null;
      if (element instanceof RuleMetadata) {
        parent = ((RuleMetadata) element).identity().group();
      }
      return parent;
    }

    @Override
    public boolean hasChildren(Object element) {
      boolean hasChildren = false;

      if (element instanceof RuleGroupMetadata) {
        hasChildren = ((RuleGroupMetadata) element).getRuleMetadata().size() > 0;
      } else if (element instanceof RuleMetadata) {
        hasChildren = false;
      }
      return hasChildren;
    }

    @Override
    public void dispose() {
      // NOOP
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      // NOOP
    }
  }

  /**
   * Label-provider for meta data information.
   *
   */
  private static class MetaDataLabelProvider extends LabelProvider {

    private final List<Module> modules;

    public MetaDataLabelProvider(List<Module> modules) {
      this.modules = modules;
    }

    @Override
    public String getText(Object element) {
      String text = null;
      if (element instanceof RuleGroupMetadata) {
        text = ((RuleGroupMetadata) element).getGroupName();
      } else if (element instanceof RuleMetadata) {
        text = ((RuleMetadata) element).identity().ruleName();
      }
      return text;
    }

    @Override
    public Image getImage(Object element) {
      Image image = null;

      if (element instanceof RuleGroupMetadata) {
        image = isGroupUsed((RuleGroupMetadata) element)
                ? CheckstyleUIPluginImages.MODULEGROUP_TICKED_ICON.getImage()
                : CheckstyleUIPluginImages.MODULEGROUP_ICON.getImage();
      } else if (element instanceof RuleMetadata) {

        image = isMetadataUsed((RuleMetadata) element)
                ? CheckstyleUIPluginImages.MODULE_TICKED_ICON.getImage()
                : CheckstyleUIPluginImages.MODULE_ICON.getImage();
      }
      return image;
    }

    private boolean isGroupUsed(RuleGroupMetadata group) {
      boolean used = true;

      for (RuleMetadata metadata : group.getRuleMetadata()) {

        if (!isMetadataUsed(metadata)) {
          used = false;
          break;
        }
      }
      return used;
    }

    private boolean isMetadataUsed(RuleMetadata metadata) {
      boolean used = false;
      if (modules != null) {
        for (Module module : modules) {

          if (metadata.equals(module.getMetaData())) {
            used = true;
            break;
          }
        }
      }

      return used;
    }
  }
}
