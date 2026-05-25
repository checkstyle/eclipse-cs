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

package net.sf.eclipsecs.ui.properties;

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import net.sf.eclipsecs.core.projectconfig.filters.IFilter;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.properties.filter.IFilterEditor;
import net.sf.eclipsecs.ui.properties.filter.PluginFilterEditors;

public class FilterSettings extends Composite {

  public FilterSettings(Composite parent, int style, IProject project, List<IFilter> filters,
          Runnable markDirty) {
    super(parent, style);
    setLayout(new FillLayout());

    Group group = new Group(this, style);

    group.setText(Messages.CheckstylePropertyPage_titleFilterGroup);
    group.setLayout(new FormLayout());

    Button btnEditFilter = new Button(group, SWT.PUSH);

    CheckboxTableViewer filterList = createFilterList(group, project);
    filterList.getTable().setLayoutData(formData(formData -> {
      formData.left = new FormAttachment(0, 3);
      formData.top = new FormAttachment(0, 3);
      formData.right = new FormAttachment(btnEditFilter, -3, SWT.LEFT);
      formData.bottom = new FormAttachment(60, -3);
    }));

    btnEditFilter.setLayoutData(formData(formData -> {
      formData.top = new FormAttachment(0, 3);
      formData.right = new FormAttachment(100, -3);
    }));

    // Description
    Label lblDesc = new Label(group, SWT.LEFT);
    lblDesc.setText(Messages.CheckstylePropertyPage_lblDescription);
    lblDesc.setLayoutData(formData(formData -> {
      formData.left = new FormAttachment(0, 3);
      formData.top = new FormAttachment(filterList.getTable(), 3, SWT.BOTTOM);
      formData.right = new FormAttachment(100, -3);
    }));

    Text txtFilterDescription = new Text(group,
            SWT.LEFT | SWT.WRAP | SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.VERTICAL);
    txtFilterDescription.setLayoutData(formData(formData -> {
      formData.left = new FormAttachment(0, 3);
      formData.top = new FormAttachment(lblDesc, 3, SWT.BOTTOM);
      formData.right = new FormAttachment(100, -3);
      formData.bottom = new FormAttachment(100, -3);
    }));

    filterList.addSelectionChangedListener(event -> {
      if (event.getStructuredSelection().getFirstElement() instanceof IFilter filterDef) {
        txtFilterDescription.setText(filterDef.getDescription());
        // activate edit button
        btnEditFilter.setEnabled(PluginFilterEditors.hasEditor(filterDef));
      }
    });

    btnEditFilter.setText(Messages.CheckstylePropertyPage_btnChangeFilter);
    btnEditFilter.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
      openFilterEditor(filterList.getSelection(), filterList, project);
      markDirty.run();
    }));

    // intialize filter list
    filterList.setInput(filters);

    btnEditFilter.setEnabled(false);
  }

  private static FormData formData(Consumer<FormData> custom) {
    FormData formData = new FormData();
    custom.accept(formData);
    return formData;
  }

  private CheckboxTableViewer createFilterList(Group group, IProject project) {
    CheckboxTableViewer filterList = CheckboxTableViewer.newCheckList(group, SWT.BORDER);
    filterList.setLabelProvider(new FilterListLabelProvider());
    filterList.setContentProvider(new ArrayContentProvider());
    filterList.setCheckStateProvider(new FilterListCheckStateProvider());
    filterList.addDoubleClickListener(event -> openFilterEditor(event.getSelection(), filterList, project));
    filterList.addCheckStateListener(event -> {
      if (event.getElement() instanceof IFilter filter) {
        if (filter.isReadonly()) {
          event.getCheckable().setChecked(event.getElement(), true);
        } else {
          filter.setEnabled(event.getChecked());
        }
      }
    });
    return filterList;
  }

  /**
   * Open the filter editor on a given selection of the list.
   *
   * @param selection
   *          the selection
   */
  private void openFilterEditor(ISelection selection, CheckboxTableViewer filterList, IProject project) {
    if (selection instanceof IStructuredSelection) {
      Object selectedElement = ((IStructuredSelection) selection).getFirstElement();

      if (selectedElement instanceof IFilter) {

        try {

          IFilter aFilterDef = (IFilter) selectedElement;

          if (!PluginFilterEditors.hasEditor(aFilterDef)) {
            return;
          }

          IFilterEditor editableFilter = PluginFilterEditors.getNewEditor(aFilterDef);
          editableFilter.setInputProject(project);
          editableFilter.setFilterData(aFilterDef.getFilterData());

          if (Window.OK == editableFilter.openEditor(getShell())) {

            aFilterDef.setFilterData(editableFilter.getFilterData());
            filterList.refresh();
          }
        } catch (CheckstylePluginException ex) {
          CheckstyleUIPlugin.errorDialog(getShell(), ex, true);
        }
      }
    }
  }

  private static final class FilterListLabelProvider extends LabelProvider {
    @Override
    public String getText(Object element) {
      StringBuilder buf = new StringBuilder();
      if (element instanceof IFilter filter) {
        buf.append(filter.getName());
        if (filter.getPresentableFilterData() != null) {
          buf.append(": ").append(filter.getPresentableFilterData());
        }
      } else {
        buf.append(super.getText(element));
      }
      return buf.toString();
    }
  }

  private static final class FilterListCheckStateProvider implements ICheckStateProvider {

    @Override
    public boolean isChecked(Object element) {
      return ((IFilter) element).isEnabled();
    }

    @Override
    public boolean isGrayed(Object element) {
      return ((IFilter) element).isReadonly();
    }

  }
}
