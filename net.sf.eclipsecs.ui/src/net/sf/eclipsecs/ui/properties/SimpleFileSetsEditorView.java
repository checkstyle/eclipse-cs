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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import net.sf.eclipsecs.core.config.CheckConfiguration;
import net.sf.eclipsecs.core.projectconfig.FileSet;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationLabelProvider;
import net.sf.eclipsecs.ui.config.CheckConfigurationViewerSorter;

public final class SimpleFileSetsEditorView extends Composite {

  private final ComboViewer mComboViewer;

  public SimpleFileSetsEditorView(Composite parent, int style, Runnable manageConfig, FileSet mDefaultFileSet,
          PropertyPageContext propertyPageContext) {
    super(parent, style);
    GridLayoutFactory.fillDefaults().applyTo(this);

    Group configArea = new Group(this, SWT.NULL);
    GridDataFactory.fillDefaults().grab(true, true).applyTo(configArea);
    configArea.setText(Messages.SimpleFileSetsEditor_titleSimpleConfig);
    GridLayoutFactory.fillDefaults().numColumns(2).applyTo(configArea);

    mComboViewer = new ComboViewer(configArea);
    mComboViewer.getCombo().setVisibleItemCount(10);
    mComboViewer.setContentProvider(CheckConfigurationContentProvider.INSTANCE);
    mComboViewer.setLabelProvider(CheckConfigurationLabelProvider.INSTANCE);
    mComboViewer.setComparator(CheckConfigurationViewerSorter.INSTANCE);
    GridDataFactory.fillDefaults().grab(true, false).applyTo(mComboViewer.getControl());

    Button mBtnManageConfigs = new Button(configArea, SWT.PUSH);
    mBtnManageConfigs.setText(Messages.SimpleFileSetsEditor_btnManageConfigs);
    mBtnManageConfigs
            .addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> manageConfig.run()));
    GridDataFactory.fillDefaults().applyTo(mBtnManageConfigs);

    // Description
    Label lblConfigDesc = new Label(configArea, SWT.LEFT);
    lblConfigDesc.setText(Messages.SimpleFileSetsEditor_lblDescription);
    GridDataFactory.fillDefaults().span(2, 1).applyTo(lblConfigDesc);

    Text mTxtConfigDescription = new Text(configArea,
            SWT.LEFT | SWT.WRAP | SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.V_SCROLL);
    GridDataFactory.fillDefaults().span(2, 1).hint(SWT.DEFAULT, 100).grab(true, true)
            .applyTo(mTxtConfigDescription);

    mComboViewer.addSelectionChangedListener(event -> {
      CheckConfiguration config = (CheckConfiguration) event.getStructuredSelection().getFirstElement();
      if (config != null) {
        mDefaultFileSet.setCheckConfig(config);
        mTxtConfigDescription.setText(config.getDescription() != null ? config.getDescription() : "");
      } else {
        mComboViewer.setSelection(new StructuredSelection(mComboViewer.getElementAt(0)));
      }
      propertyPageContext.updateButtons();
    });

    // init the check configuration combo
    mComboViewer.setInput(propertyPageContext.configuration());
    if (mDefaultFileSet.getCheckConfig() != null) {
      mComboViewer.setSelection(new StructuredSelection(mDefaultFileSet.getCheckConfig()));
    }
  }

  public void refresh() {
    mComboViewer.refresh();
  }

}
