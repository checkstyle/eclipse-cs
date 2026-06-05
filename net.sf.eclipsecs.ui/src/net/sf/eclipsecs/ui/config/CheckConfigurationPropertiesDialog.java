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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import net.sf.eclipsecs.core.config.CheckConfigurationTester;
import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.config.ICheckConfigurationWorkingSet;
import net.sf.eclipsecs.core.config.configtypes.ConfigurationTypes;
import net.sf.eclipsecs.core.config.configtypes.IConfigurationType;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.configtypes.ConfigurationTypesUI;
import net.sf.eclipsecs.ui.config.configtypes.ICheckConfigurationEditor;

/**
 * Dialog to show/edit the properties (name, location, description) of a check
 * configuration. Also used to create new check configurations.
 *
 */
public class CheckConfigurationPropertiesDialog extends TitleAreaDialog {

  /** Button to add the additional properties dialog. */
  private Button mBtnProperties;

  /** the working set. */
  private ICheckConfigurationWorkingSet mWorkingSet;

  /** the check configuration. */
  private CheckConfigurationWorkingCopy mCheckConfig;

  /** the editor for the configuration location. */
  private ICheckConfigurationEditor mConfigurationEditor;

  /** The template configuration for a new config. */
  private ICheckConfiguration mTemplate;

  private CheckConfigurationPropertiesDialogView dialogView;

  //
  // constructor
  //

  /**
   * Creates the properties dialog for check configurations.
   *
   * @param parent
   *          the parent shell
   * @param checkConfig
   *          the check configuration or <code>null</code> if a new check config
   *          should be created
   * @param workingSet
   *          the working set the check config is changed in
   */
  public CheckConfigurationPropertiesDialog(Shell parent, CheckConfigurationWorkingCopy checkConfig,
          ICheckConfigurationWorkingSet workingSet) {
    super(parent);
    setHelpAvailable(false);
    mWorkingSet = workingSet;
    mCheckConfig = checkConfig;
  }

  //
  // methods
  //

  /**
   * Returns the working set this dialog is operating on.
   *
   * @return the check configuration working set
   */
  public ICheckConfigurationWorkingSet getCheckConfigurationWorkingSet() {
    return mWorkingSet;
  }

  /**
   * Sets the template for a new check configuration.
   *
   * @param template
   *          the template configuration
   */
  public void setTemplateConfiguration(ICheckConfiguration template) {
    this.mTemplate = template;
  }

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

    Composite composite = (Composite) super.createDialogArea(parent);

    dialogView = new CheckConfigurationPropertiesDialogView(composite, SWT.NULL, this::changeSelectedConfigurationType);
    GridDataFactory.create(GridData.FILL_BOTH).applyTo(dialogView);

    return composite;
  }

  private void changeSelectedConfigurationType(IConfigurationType type, boolean isComboEnabled) {
    if (isComboEnabled) {
      String oldName = mCheckConfig.getName();
      String oldDescr = mCheckConfig.getDescription();
      mCheckConfig = mWorkingSet.newWorkingCopy(type);
      try {
        mCheckConfig.setName(oldName);
      } catch (CheckstylePluginException ex) {
        // NOOP
      }
      mCheckConfig.setDescription(oldDescr);
    }
    createConfigurationEditor(mCheckConfig);
  }

  @Override
  protected Control createButtonBar(Composite parent) {

    Composite composite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.swtDefaults().numColumns(3).margins(0, 0).applyTo(composite);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(composite);

    mBtnProperties = new Button(composite, SWT.PUSH);
    mBtnProperties.setText(Messages.CheckConfigurationPropertiesDialog_btnAdditionalProps);
    GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).indent(5, 0).applyTo(mBtnProperties);

    mBtnProperties.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
      try {
        mConfigurationEditor.getEditedWorkingCopy();
        ResolvablePropertiesDialog dialog = new ResolvablePropertiesDialog(getShell(),
                mCheckConfig);
        dialog.open();
      } catch (CheckstylePluginException ex) {
        setErrorMessage(ex.getLocalizedMessage());
      }
    }));

    Control buttonBar = super.createButtonBar(composite);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).align(SWT.END, SWT.CENTER).applyTo(buttonBar);

    return composite;
  }

  @Override
  protected void configureShell(Shell newShell) {

    super.configureShell(newShell);
    newShell.setText(Messages.CheckConfigurationPropertiesDialog_titleCheckProperties);
  }

  @Override
  protected void okPressed() {
    try {
      // Check if the configuration is valid
      mCheckConfig = mConfigurationEditor.getEditedWorkingCopy();

      int numUnresolvedProps = CheckConfigurationTester.getUnresolvedProperties(mCheckConfig).size();

      if (numUnresolvedProps > 0) {

        MessageDialog dialog = new MessageDialog(getShell(),
                Messages.CheckConfigurationPropertiesDialog_titleUnresolvedProps, null,
                NLS.bind(Messages.CheckConfigurationPropertiesDialog_msgUnresolvedProps,
                        Integer.toString(numUnresolvedProps)),
                MessageDialog.WARNING,
                new String[] {
                    Messages.CheckConfigurationPropertiesDialog_btnEditProps,
                    Messages.CheckConfigurationPropertiesDialog_btnContinue,
                    Messages.CheckConfigurationPropertiesDialog_btnCancel,
                },
                0);
        int result = dialog.open();

        if (result == 0) {
          ResolvablePropertiesDialog propsDialog = new ResolvablePropertiesDialog(getShell(),
                  mCheckConfig);
          propsDialog.open();
        } else if (result == 1) {
          super.okPressed();
        }
      } else {
        super.okPressed();
      }
    } catch (CheckstylePluginException ex) {
      CheckstyleLog.log(ex);
      this.setErrorMessage(ex.getLocalizedMessage());
    }
  }

  /**
   * Creates the configuration type specific location editor.
   *
   * @param configType
   *          the configuration type
   */
  private void createConfigurationEditor(CheckConfigurationWorkingCopy config) {

    try {
      mConfigurationEditor = ConfigurationTypesUI.getNewEditor(config.getType());
      mConfigurationEditor.initialize(config, this);

      dialogView.bindEditor(mConfigurationEditor);

      Point initialSize = this.getInitialSize();
      getShell().setSize(initialSize);

      mBtnProperties.setEnabled(mCheckConfig.getType().isEditable());
    } catch (Exception ex) {
      CheckstyleUIPlugin.errorDialog(getShell(), ex, true);
    }
  }

  /**
   * Initialize the dialogs controls with the data.
   */
  private void initialize() {
    IConfigurationType[] types;
    if (mCheckConfig == null) {
      types = mTemplate != null
              ? ConfigurationTypes.getConfigurableConfigTypes()
              : ConfigurationTypes.getCreatableConfigTypes();

      mCheckConfig = mWorkingSet.newWorkingCopy(types[0]);

      if (mTemplate != null) {

        this.setTitle(NLS.bind(Messages.CheckConfigurationPropertiesDialog_titleCopyConfiguration,
                mTemplate.getName()));
        this.setMessage(Messages.CheckConfigurationPropertiesDialog_msgCopyConfiguration);

        String nameProposal = NLS.bind(Messages.CheckConfigurationPropertiesDialog_CopyOfAddition,
                mTemplate.getName());
        setUniqueName(mCheckConfig, nameProposal);
        mCheckConfig.setDescription(mTemplate.getDescription());
        mCheckConfig.getResolvableProperties().addAll(mTemplate.getResolvableProperties());
      } else {
        this.setTitle(Messages.CheckConfigurationPropertiesDialog_titleCheckConfig);
        this.setMessage(Messages.CheckConfigurationPropertiesDialog_msgCreateNewCheckConfig);
      }
    } else {
      this.setTitle(Messages.CheckConfigurationPropertiesDialog_titleCheckConfig);
      this.setMessage(Messages.CheckConfigurationPropertiesDialog_msgEditCheckConfig);
      dialogView.disable();
      types = new IConfigurationType[] {
          mCheckConfig.getType(),
      };
    }
    dialogView.initConfigType(types);
    createConfigurationEditor(mCheckConfig);
  }

  /**
   * Creates a non conflicting name out of a name proposal.
   *
   * @param config
   *          the working copy to set the name on
   * @param checkConfigName
   *          the name proposal
   */
  private static void setUniqueName(CheckConfigurationWorkingCopy config, String checkConfigName) {
    String uniqueName = checkConfigName;

    int counter = 2;
    while (true) {
      try {
        config.setName(uniqueName);
        break;
      } catch (CheckstylePluginException ex) {
        uniqueName = checkConfigName + " (" + counter + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        counter++;
      }
    }
  }
}
