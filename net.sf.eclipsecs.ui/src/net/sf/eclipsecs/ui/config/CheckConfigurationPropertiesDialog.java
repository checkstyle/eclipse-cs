//============================================================================
//
// Copyright (C) 2002-2016  David Schneider, Lars Ködderitzsch
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
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//============================================================================

package net.sf.eclipsecs.ui.config;

import java.util.List;

import net.sf.eclipsecs.core.config.CheckConfigurationTester;
import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.config.ICheckConfigurationWorkingSet;
import net.sf.eclipsecs.core.config.ResolvableProperty;
import net.sf.eclipsecs.core.config.configtypes.ConfigurationTypes;
import net.sf.eclipsecs.core.config.configtypes.IConfigurationType;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.configtypes.ConfigurationTypesUI;
import net.sf.eclipsecs.ui.config.configtypes.ICheckConfigurationEditor;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog to show/edit the properties (name, location, description) of a check
 * configuration. Also used to create new check configurations.
 * 
 * @author Lars Ködderitzsch
 */
public class CheckConfigurationPropertiesDialog extends TitleAreaDialog {

  /** Button to add the additional properties dialog. */
  private Button mBtnProperties;

  /** the working set. */
  private ICheckConfigurationWorkingSet mWorkingSet;

  /** the check configuration. */
  private CheckConfigurationWorkingCopy mCheckConfig;

  /** the combo box containing the config type. */
  private ComboViewer mConfigType;

  /** place holder for the location editor. */
  private Composite mEditorPlaceHolder;

  /** the editor for the configuration location. */
  private ICheckConfigurationEditor mConfigurationEditor;

  /** The template configuration for a new config. */
  private ICheckConfiguration mTemplate;

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

  /**
   * @see org.eclipse.jface.dialogs.Dialog#create()
   */
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
    this.setTitleImage(CheckstyleUIPluginImages.getImage(CheckstyleUIPluginImages.PLUGIN_LOGO));

    Composite composite = (Composite) super.createDialogArea(parent);

    Composite contents = new Composite(composite, SWT.NULL);
    contents.setLayout(new GridLayout(2, false));
    GridData fd = new GridData(GridData.FILL_BOTH);
    contents.setLayoutData(fd);

    Label lblConfigType = new Label(contents, SWT.NULL);
    lblConfigType.setText(Messages.CheckConfigurationPropertiesDialog_lblConfigType);
    fd = new GridData();

    // this is a weird hack to find the longest label
    // this is done to have a nice ordered appearance of the this label
    // and the labels below
    // this is very difficult to do, because they belong to different
    // layouts
    GC gc = new GC(lblConfigType);
    int nameSize = gc.textExtent(Messages.CheckConfigurationPropertiesDialog_lblName).x;
    int locationsSize = gc.textExtent(Messages.CheckConfigurationPropertiesDialog_lblLocation).x;
    int max = Math.max(nameSize, locationsSize);
    gc.dispose();

    fd.widthHint = max;
    lblConfigType.setLayoutData(fd);

    mConfigType = new ComboViewer(contents);
    fd = new GridData();
    mConfigType.getCombo().setLayoutData(fd);
    mConfigType.setContentProvider(new ArrayContentProvider());
    mConfigType.setLabelProvider(new LabelProvider() {
      /**
       * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
       */
      @Override
      public String getText(Object element) {
        return ((IConfigurationType) element).getName();
      }

      /**
       * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
       */
      @Override
      public Image getImage(Object element) {
        return ConfigurationTypesUI.getConfigurationTypeImage((IConfigurationType) element);
      }
    });
    mConfigType.addSelectionChangedListener(new ISelectionChangedListener() {
      /**
       * @see ISelectionChangedListener#selectionChanged(
       *      org.eclipse.jface.viewers.SelectionChangedEvent)
       */
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        if (event.getSelection() instanceof IStructuredSelection) {
          IConfigurationType type = (IConfigurationType) ((IStructuredSelection) event
                  .getSelection()).getFirstElement();

          if (mConfigType.getCombo().isEnabled()) {

            String oldName = mCheckConfig.getName();
            String oldDescr = mCheckConfig.getDescription();

            mCheckConfig = mWorkingSet.newWorkingCopy(type);
            try {
              mCheckConfig.setName(oldName);
            } catch (CheckstylePluginException e) {
              // NOOP
            }
            mCheckConfig.setDescription(oldDescr);
          }

          createConfigurationEditor(mCheckConfig);
        }
      }
    });

    mEditorPlaceHolder = new Composite(contents, SWT.NULL);
    GridLayout layout = new GridLayout(1, true);
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    mEditorPlaceHolder.setLayout(layout);
    fd = new GridData(GridData.FILL_HORIZONTAL);
    fd.horizontalSpan = 2;
    mEditorPlaceHolder.setLayoutData(fd);

    return composite;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Control createButtonBar(Composite parent) {

    Composite composite = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout(3, false);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    composite.setLayout(layout);
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    mBtnProperties = new Button(composite, SWT.PUSH);
    mBtnProperties.setText(Messages.CheckConfigurationPropertiesDialog_btnAdditionalProps);
    GridData gd = new GridData();
    gd.horizontalAlignment = GridData.BEGINNING;
    gd.horizontalIndent = 5;
    mBtnProperties.setLayoutData(gd);

    mBtnProperties.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent e) {

        try {
          mConfigurationEditor.getEditedWorkingCopy();

          ResolvablePropertiesDialog dialog = new ResolvablePropertiesDialog(getShell(),
                  mCheckConfig);
          dialog.open();
        } catch (CheckstylePluginException ex) {
          setErrorMessage(ex.getLocalizedMessage());
        }
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        // NOOP
      }
    });

    Control buttonBar = super.createButtonBar(composite);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalAlignment = GridData.END;
    buttonBar.setLayoutData(gd);

    return composite;
  }

  /**
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  @Override
  protected void configureShell(Shell newShell) {

    super.configureShell(newShell);
    newShell.setText(Messages.CheckConfigurationPropertiesDialog_titleCheckProperties);
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  @Override
  protected void okPressed() {
    try {
      // Check if the configuration is valid
      mCheckConfig = mConfigurationEditor.getEditedWorkingCopy();

      CheckConfigurationTester tester = new CheckConfigurationTester(mCheckConfig);
      List<ResolvableProperty> unresolvedProps = tester.getUnresolvedProperties();

      if (!unresolvedProps.isEmpty()) {

        MessageDialog dialog = new MessageDialog(getShell(),
                Messages.CheckConfigurationPropertiesDialog_titleUnresolvedProps, null,
                NLS.bind(Messages.CheckConfigurationPropertiesDialog_msgUnresolvedProps,
                        "" + unresolvedProps.size()), //$NON-NLS-1$
                MessageDialog.WARNING,
                new String[] { Messages.CheckConfigurationPropertiesDialog_btnEditProps,
                    Messages.CheckConfigurationPropertiesDialog_btnContinue,
                    Messages.CheckConfigurationPropertiesDialog_btnCancel },
                0);
        int result = dialog.open();

        if (0 == result) {
          ResolvablePropertiesDialog propsDialog = new ResolvablePropertiesDialog(getShell(),
                  mCheckConfig);
          propsDialog.open();
          return;
        } else if (1 == result) {
          super.okPressed();
        } else if (2 == result) {
          return;
        }
      } else {
        super.okPressed();
      }
    } catch (CheckstylePluginException e) {
      CheckstyleLog.log(e);
      this.setErrorMessage(e.getLocalizedMessage());
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

      // remove old editor
      Control[] controls = mEditorPlaceHolder.getChildren();
      for (int i = 0; i < controls.length; i++) {
        controls[i].dispose();
      }

      mConfigurationEditor.createEditorControl(mEditorPlaceHolder, getShell());

      mEditorPlaceHolder.redraw();
      mEditorPlaceHolder.update();
      mEditorPlaceHolder.layout();

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

    if (mCheckConfig == null) {

      IConfigurationType[] types = mTemplate != null
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

      mConfigType.setInput(types);
      mConfigType.setSelection(new StructuredSelection(types[0]), true);

      createConfigurationEditor(mCheckConfig);
    } else {
      this.setTitle(Messages.CheckConfigurationPropertiesDialog_titleCheckConfig);
      this.setMessage(Messages.CheckConfigurationPropertiesDialog_msgEditCheckConfig);

      mConfigType.getCombo().setEnabled(false);
      mConfigType.setInput(new IConfigurationType[] { mCheckConfig.getType() });

      // type of existing configs cannot be changed
      mConfigType.setSelection(new StructuredSelection(mCheckConfig.getType()), true);
      createConfigurationEditor(mCheckConfig);
    }
  }

  /**
   * Creates a non conflicting name out of a name proposal.
   * 
   * @param config
   *          the working copy to set the name on
   * @param checkConfigName
   *          the name proposal
   */
  private void setUniqueName(CheckConfigurationWorkingCopy config, String checkConfigName) {
    String uniqueName = checkConfigName;

    int counter = 2;
    while (true) {
      try {
        config.setName(uniqueName);
        break;
      } catch (CheckstylePluginException e) {
        uniqueName = checkConfigName + " (" + counter + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        counter++;
      }
    }
  }
}
