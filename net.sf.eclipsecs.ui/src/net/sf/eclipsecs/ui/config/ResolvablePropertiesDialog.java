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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import net.sf.eclipsecs.core.config.CheckConfigurationTester;
import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.ResolvableProperty;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.util.SWTUtil;
import net.sf.eclipsecs.ui.util.table.ITableComparableProvider;
import net.sf.eclipsecs.ui.util.table.ITableSettingsProvider;

/**
 * Dialog to show/edit the properties (name, location, description) of a check
 * configuration. Also used to create new check configurations.
 *
 */
public class ResolvablePropertiesDialog extends TitleAreaDialog {

  //
  // attributes
  //

  /** the check configuration. */
  private CheckConfigurationWorkingCopy mCheckConfig;

  /** The list of properties. */
  private List<ResolvableProperty> mResolvableProperties;

  private ResolvablePropertiesDialogView dialogView;

  //
  // constructor
  //

  /**
   * Creates the properties dialog for check configurations.
   *
   * @param parent
   *          the parent shell
   * @param checkConfig
   *          the check configuration to edit
   */
  public ResolvablePropertiesDialog(Shell parent, CheckConfigurationWorkingCopy checkConfig) {
    super(parent);
    setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
    setHelpAvailable(false);
    mCheckConfig = checkConfig;
  }

  //
  // methods
  //

  /**
   * Get the check configuration from the editor.
   *
   * @return the check configuration
   */
  public CheckConfigurationWorkingCopy getCheckConfiguration() {
    return mCheckConfig;
  }

  @Override
  public void create() {
    super.create();
    initialize();

    SWTUtil.addResizeSupport(this, CheckstyleUIPlugin.getDefault().getDialogSettings(),
            ResolvablePropertiesDialog.class.getName());
  }

  /**
   * Creates the dialogs main contents.
   *
   * @param parent
   *          the parent composite
   */
  @Override
  protected Control createDialogArea(Composite parent) {
    // set the logo
    this.setTitleImage(CheckstyleUIPluginImages.PLUGIN_LOGO.getImage());
    this.setTitle(Messages.ResolvablePropertiesDialog_titleMessageArea);
    this.setMessage(Messages.ResolvablePropertiesDialog_msgAdditionalProperties);

    Composite composite = (Composite) super.createDialogArea(parent);

    this.dialogView = new ResolvablePropertiesDialogView(composite, SWT.NULL,
            this::openPropertyItemEditor, this::removePropertyItems);
    GridDataFactory.create(GridData.FILL_BOTH).applyTo(dialogView);

    return composite;
  }

  @Override
  protected Control createButtonBar(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.swtDefaults().numColumns(3).margins(0, 0).applyTo(composite);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(composite);

    Button mBtnFind = new Button(composite, SWT.PUSH);
    mBtnFind.setText(Messages.ResolvablePropertiesDialog_btnFind);
    GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).indent(5, 0).applyTo(mBtnFind);
    mBtnFind.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> findPropertyItems()));

    Control buttonBar = super.createButtonBar(composite);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).align(SWT.END, SWT.CENTER).applyTo(buttonBar);

    return composite;
  }

  @Override
  protected void configureShell(Shell newShell) {

    super.configureShell(newShell);
    newShell.setText(Messages.ResolvablePropertiesDialog_titleDialog);
  }

  @Override
  protected Point getInitialSize() {
    return new Point(650, 500);
  }

  @Override
  protected void okPressed() {
    // check for properties without value - these must be fixed before
    // OK'ing
    boolean error = false;
    for (ResolvableProperty prop : mResolvableProperties) {
      if (StringUtils.isBlank(prop.getValue())) {
        this.setErrorMessage(NLS.bind(Messages.ResolvablePropertiesDialog_msgMissingPropertyValue,
                prop.getPropertyName()));
        error = true;
        break;
      }
    }

    if (!error) {
      mCheckConfig.getResolvableProperties().clear();
      mCheckConfig.getResolvableProperties().addAll(mResolvableProperties);
      super.okPressed();
    }
  }

  /**
   * Initialize the dialogs controls with the data.
   */
  private void initialize() {
    // clone the properties so that changes don't directly reflect back into
    // the configuration
    mResolvableProperties = new ArrayList<>();
    for (ResolvableProperty prop : mCheckConfig.getResolvableProperties()) {
      mResolvableProperties.add(prop.clone());
    }
    dialogView.setResolvableProperties(mResolvableProperties);
  }

  private void openPropertyItemEditor(ResolvableProperty prop) {

    if (prop == null) {
      ResolvableProperty newProp = new ResolvableProperty(null, null);

      ResolvablePropertyEditDialog dialog = new ResolvablePropertyEditDialog(getShell(), newProp);
      if (Window.OK == dialog.open()) {
        mResolvableProperties.add(newProp);
        dialogView.refresh();
      }
    } else {
      ResolvablePropertyEditDialog dialog = new ResolvablePropertyEditDialog(getShell(), prop);
      if (Window.OK == dialog.open()) {
        dialogView.refresh();
      }
    }
  }

  private void removePropertyItems(List<ResolvableProperty> resolvableProperties) {
    boolean confirm = MessageDialog.openQuestion(getShell(),
            Messages.ResolvablePropertiesDialog_titleRemoveConfirmation,
            Messages.ResolvablePropertiesDialog_msgRemoveConfirmation);
    if (confirm) {
      mResolvableProperties.removeAll(resolvableProperties);
      dialogView.refresh();
    }
  }

  private void findPropertyItems() {
    CheckConfigurationWorkingCopy clone = mCheckConfig.clone();
    clone.getResolvableProperties().clear();
    clone.getResolvableProperties().addAll(mResolvableProperties);

    try {
      List<ResolvableProperty> unresolvedProps = CheckConfigurationTester
              .getUnresolvedProperties(clone);

      // filter props already in the dialogs list
      Iterator<ResolvableProperty> iter = unresolvedProps.iterator();
      while (iter.hasNext()) {

        ResolvableProperty prop = iter.next();

        Iterator<ResolvableProperty> it2 = mResolvableProperties.iterator();
        while (it2.hasNext()) {

          if (prop.getPropertyName().equals(it2.next().getPropertyName())) {
            // remove the current entry
            iter.remove();
            break;
          }
        }
      }

      if (!unresolvedProps.isEmpty()) {

        StringBuilder buf = new StringBuilder();
        iter = unresolvedProps.iterator();
        while (iter.hasNext()) {
          buf.append("\t${").append(iter.next().getPropertyName()).append("}\n");
        }

        boolean confirm = MessageDialog.openQuestion(getShell(),
                Messages.ResolvablePropertiesDialog_titleFoundProperties,
                NLS.bind(Messages.ResolvablePropertiesDialog_msgFoundProperties, buf));
        if (confirm) {
          mResolvableProperties.addAll(unresolvedProps);
          dialogView.refresh();
        }
      } else {
        MessageDialog.openInformation(getShell(),
                Messages.ResolvablePropertiesDialog_titleNoUnresolvedProps,
                Messages.ResolvablePropertiesDialog_msgNoUnresolvedProps);
      }
    } catch (CheckstylePluginException ex) {
      CheckstyleUIPlugin.errorDialog(getShell(), ex, true);
    }
  }

  /**
   * Label provider for the check configuration table. Implements also support
   * for table sorting and storing of the table settings.
   *
   */
  public static final class PropertiesLabelProvider extends LabelProvider
          implements ITableLabelProvider, ITableComparableProvider, ITableSettingsProvider {

    public static final PropertiesLabelProvider INSTANCE = new PropertiesLabelProvider();

    private PropertiesLabelProvider() {

    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
      String result = element.toString();
      if (element instanceof ResolvableProperty) {
        ResolvableProperty prop = (ResolvableProperty) element;
        if (columnIndex == 0) {
          result = prop.getPropertyName();
        }
        if (columnIndex == 1) {
          result = prop.getValue();
        }
      }
      return result;
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      return columnIndex == 0 ? getImage(element) : null;
    }

    @Override
    public Comparable<String> getComparableValue(Object element, int col) {
      return getColumnText(element, col);
    }

    @Override
    public IDialogSettings getTableSettings() {
      String concreteViewId = ResolvablePropertiesDialog.class.getName();

      IDialogSettings workbenchSettings = CheckstyleUIPlugin.getDefault().getDialogSettings();
      IDialogSettings settings = workbenchSettings.getSection(concreteViewId);

      if (settings == null) {
        settings = workbenchSettings.addNewSection(concreteViewId);
      }

      return settings;
    }
  }
}
