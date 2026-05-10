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

import java.util.function.Consumer;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationWorkingCopy;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationLabelProvider;
import net.sf.eclipsecs.ui.config.CheckConfigurationViewerSorter;

public final class FileSetEditDialogCommonArea extends Composite {

  private final Text mFileSetNameText;
  private final ComboViewer mComboViewer;

  public FileSetEditDialogCommonArea(Composite parent, int style,
          Consumer<ICheckConfiguration> selectionChanged, Runnable configureFileSetConfig) {
    super(parent, style);
    setLayout(new FillLayout());

    Composite composite = new Composite(this, SWT.NONE);
    GridLayout layout = new GridLayout(2, false);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    composite.setLayout(layout);

    Label nameLabel = new Label(composite, SWT.NULL);
    nameLabel.setText(Messages.FileSetEditDialog_lblName);

    mFileSetNameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
    mFileSetNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Label lblConfiguration = new Label(composite, SWT.NULL);
    lblConfiguration.setText(Messages.FileSetEditDialog_lblCheckConfig);

    final Composite comboComposite = new Composite(composite, SWT.NONE);
    layout = new GridLayout(2, false);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    comboComposite.setLayout(layout);
    comboComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    mComboViewer = new ComboViewer(comboComposite);
    mComboViewer.getCombo().setVisibleItemCount(10);
    mComboViewer.setContentProvider(new CheckConfigurationContentProvider());
    mComboViewer.setLabelProvider(new CheckConfigurationLabelProvider());
    mComboViewer.setComparator(new CheckConfigurationViewerSorter());
    mComboViewer.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    mComboViewer.addSelectionChangedListener(event -> selectionChanged
            .accept((ICheckConfiguration) event.getStructuredSelection().getFirstElement()));

    Button mConfigureButton = new Button(comboComposite, SWT.PUSH);
    mConfigureButton.setText(Messages.FileSetEditDialog_btnConfigure);
    mConfigureButton.addSelectionListener(
            SelectionListener.widgetSelectedAdapter(event -> configureFileSetConfig.run()));
    mConfigureButton.setLayoutData(new GridData());
  }

  public String getText() {
    return this.mFileSetNameText.getText();
  }

  public void setInput(ProjectConfigurationWorkingCopy configuration) {
    this.mComboViewer.setInput(configuration);
  }

  public void setText(String text) {
    this.mFileSetNameText.setText(text);
  }

  public void setSelection(ICheckConfiguration iCheckConfiguration) {
    this.mComboViewer.setSelection(new StructuredSelection(iCheckConfiguration));
  }
}
