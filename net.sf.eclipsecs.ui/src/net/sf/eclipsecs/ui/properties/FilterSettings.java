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

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
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

  private final IProject project;

  public FilterSettings(Composite parent, int style, IProject project, List<IFilter> filters,
          Runnable markDirty) {
    super(parent, style);
    this.project = project;

    setLayout(new FillLayout());

    // group composite containing the filter settings
    Group filterArea = new Group(this, SWT.NULL);
    filterArea.setText(Messages.CheckstylePropertyPage_titleFilterGroup);

    filterArea.setLayout(new FormLayout());

    Button btnEditFilter = new Button(filterArea, SWT.PUSH);

    FormData formData = new FormData();
    formData.left = new FormAttachment(0, 3);
    formData.top = new FormAttachment(0, 3);
    formData.right = new FormAttachment(btnEditFilter, -3, SWT.LEFT);
    formData.bottom = new FormAttachment(60, -3);
    CheckboxTableViewer filterList = CheckboxTableViewer.newCheckList(filterArea, SWT.BORDER);
    filterList.getTable().setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(0, 3);
    formData.right = new FormAttachment(100, -3);
    btnEditFilter.setLayoutData(formData);

    // Description
    Label lblDesc = new Label(filterArea, SWT.LEFT);
    lblDesc.setText(Messages.CheckstylePropertyPage_lblDescription);
    formData = new FormData();
    formData.left = new FormAttachment(0, 3);
    formData.top = new FormAttachment(filterList.getTable(), 3, SWT.BOTTOM);
    formData.right = new FormAttachment(100, -3);
    lblDesc.setLayoutData(formData);

    formData = new FormData();
    formData.left = new FormAttachment(0, 3);
    formData.top = new FormAttachment(lblDesc, 3, SWT.BOTTOM);
    formData.right = new FormAttachment(100, -3);
    formData.bottom = new FormAttachment(100, -3);
    Text txtFilterDescription = new Text(filterArea,
            SWT.LEFT | SWT.WRAP | SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.VERTICAL);
    txtFilterDescription.setLayoutData(formData);

    filterList.setLabelProvider(new LabelProvider() {

      @Override
      public String getText(Object element) {

        StringBuilder buf = new StringBuilder();

        if (element instanceof IFilter) {

          IFilter filter = (IFilter) element;

          buf.append(filter.getName());
          if (filter.getPresentableFilterData() != null) {
            buf.append(": ").append(filter.getPresentableFilterData()); //$NON-NLS-1$
          }
        } else {
          buf.append(super.getText(element));
        }

        return buf.toString();
      }
    });
    filterList.setContentProvider(new ArrayContentProvider());
    filterList.addSelectionChangedListener(event -> {
      if (event.getSelection() instanceof IStructuredSelection selection) {
        if (selection.getFirstElement() instanceof IFilter filterDef) {
          txtFilterDescription.setText(filterDef.getDescription());
          // activate edit button
          btnEditFilter.setEnabled(PluginFilterEditors.hasEditor(filterDef));
        }
      }
    });
    filterList.addDoubleClickListener(event -> openFilterEditor(event.getSelection(), filterList));
    filterList.addCheckStateListener(event -> {
      if (event.getElement() instanceof IFilter filter) {
        if (filter.isReadonly()) {
          event.getCheckable().setChecked(event.getElement(), true);
        } else {
          filter.setEnabled(event.getChecked());
        }
      }
    });

    btnEditFilter.setText(Messages.CheckstylePropertyPage_btnChangeFilter);
    btnEditFilter.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
      ISelection selection = filterList.getSelection();
      openFilterEditor(selection, filterList);
      markDirty.run();
    }));

    // intialize filter list
    List<IFilter> filterDefs = filters;
    filterList.setInput(filterDefs);

    // set the checked state
    for (int i = 0; i < filterDefs.size(); i++) {
      IFilter filter = filterDefs.get(i);
      filterList.setChecked(filter, filter.isEnabled());
    }

    // set the readonly state
    for (int i = 0; i < filterDefs.size(); i++) {
      IFilter filter = filterDefs.get(i);
      filterList.setGrayed(filter, filter.isReadonly());
    }

    btnEditFilter.setEnabled(false);
  }

  /**
   * Open the filter editor on a given selection of the list.
   *
   * @param selection
   *          the selection
   */
  private void openFilterEditor(ISelection selection, CheckboxTableViewer filterList) {
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
}
