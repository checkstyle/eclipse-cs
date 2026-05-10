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

import java.io.File;
import java.util.ArrayList;
import java.util.function.Consumer;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.GlobalCheckConfigurationWorkingSet;
import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.config.ICheckConfigurationWorkingSet;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationFactory;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.util.table.ITableComparableProvider;
import net.sf.eclipsecs.ui.util.table.ITableSettingsProvider;
import net.sf.eclipsecs.ui.util.table.TableViewerEnhancer;

/**
 * This class provides the editor GUI for a check configuration working set.
 *
 */
public final class CheckConfigurationWorkingSetEditor {

  //
  // attributes
  //

  private final ICheckConfigurationWorkingSet mWorkingSet;
  private final boolean mIsShowUsage;
  private ButtonBar buttonBar;
  private ConfigTable configTable;
  private Text mConfigurationDescription;
  private TableViewer mUsageView;

  //
  // constructors
  //

  /**
   * Creates the configuration working set editor.
   *
   * @param workingSet
   *          the configuration working set to edit
   * @param showUsage
   *          determines if the usage area should be shown
   */
  public CheckConfigurationWorkingSetEditor(ICheckConfigurationWorkingSet workingSet,
          boolean showUsage) {
    mWorkingSet = workingSet;
    mIsShowUsage = showUsage;
  }

  //
  // methods
  //

  public Composite createContents(Composite parent) {
    Composite configComposite = new Composite(parent, SWT.NULL);
    configComposite.setLayout(new FormLayout());

    boolean useDefaultColumn = mWorkingSet instanceof GlobalCheckConfigurationWorkingSet;

    ButtonBarActions buttonBarActions = new ButtonBarActions(this::addCheckConfig,
            this::editCheckConfig, this::configureCheckConfig, this::copyCheckConfig,
            this::removeCheckConfig, this::setDefaultCheckConfig,
            this::exportCheckstyleCheckConfig);
    this.buttonBar = new ButtonBar(configComposite, SWT.NULL, useDefaultColumn, buttonBarActions);
    FormData formData = new FormData();
    formData.top = new FormAttachment(0);
    formData.right = new FormAttachment(100);
    formData.bottom = new FormAttachment(100);
    buttonBar.setLayoutData(formData);

    Composite tableAndDesc = new Composite(configComposite, SWT.NULL);
    tableAndDesc.setLayout(new FormLayout());
    formData = new FormData();
    formData.left = new FormAttachment(0);
    formData.top = new FormAttachment(0);
    formData.right = new FormAttachment(buttonBar, -3, SWT.LEFT);
    formData.bottom = new FormAttachment(100, 0);
    tableAndDesc.setLayoutData(formData);

    final ConfigurationLabelProvider multiProvider = new ConfigurationLabelProvider(mWorkingSet);
    this.configTable = new ConfigTable(tableAndDesc, SWT.NULL, useDefaultColumn,
            mWorkingSet.getWorkingCopies(), multiProvider, this::configureCheckConfig,
            this::handleSelectionChanged);
    formData = new FormData();
    formData.left = new FormAttachment(0);
    formData.top = new FormAttachment(0);
    formData.right = new FormAttachment(100);
    formData.bottom = new FormAttachment(70);
    this.configTable.setLayoutData(formData);

    Composite descArea = new Composite(tableAndDesc, SWT.NULL);
    descArea.setLayout(new FormLayout());
    formData = new FormData();
    formData.left = new FormAttachment(0);
    formData.top = new FormAttachment(this.configTable, 0);
    formData.right = new FormAttachment(mIsShowUsage ? 60 : 100);
    formData.bottom = new FormAttachment(100);
    descArea.setLayoutData(formData);

    Label lblDescription = new Label(descArea, SWT.NULL);
    lblDescription.setText(Messages.CheckstylePreferencePage_lblDescription);
    formData = new FormData();
    formData.left = new FormAttachment(0);
    formData.top = new FormAttachment(3);
    formData.right = new FormAttachment(100);
    lblDescription.setLayoutData(formData);

    mConfigurationDescription = new Text(descArea,
            SWT.LEFT | SWT.WRAP | SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.VERTICAL);
    formData = new FormData();
    formData.left = new FormAttachment(0);
    formData.top = new FormAttachment(lblDescription);
    formData.right = new FormAttachment(100);
    formData.bottom = new FormAttachment(100);
    mConfigurationDescription.setLayoutData(formData);

    if (mIsShowUsage) {
      Composite usageArea = new Composite(tableAndDesc, SWT.NULL);
      usageArea.setLayout(new FormLayout());
      formData = new FormData();
      formData.left = new FormAttachment(60, 0);
      formData.top = new FormAttachment(this.configTable, 3);
      formData.right = new FormAttachment(100);
      formData.bottom = new FormAttachment(100);
      usageArea.setLayoutData(formData);

      Label lblUsage = new Label(usageArea, SWT.NULL);
      lblUsage.setText(Messages.CheckstylePreferencePage_lblProjectUsage);
      formData = new FormData();
      formData.left = new FormAttachment(0);
      formData.top = new FormAttachment(0);
      formData.right = new FormAttachment(100);
      lblUsage.setLayoutData(formData);

      mUsageView = new TableViewer(usageArea);
      mUsageView.getControl().setBackground(usageArea.getBackground());
      mUsageView.setContentProvider(new ArrayContentProvider());
      mUsageView.setLabelProvider(new WorkbenchLabelProvider());
      formData = new FormData();
      formData.left = new FormAttachment(0);
      formData.top = new FormAttachment(lblUsage);
      formData.right = new FormAttachment(100);
      formData.bottom = new FormAttachment(100);
      mUsageView.getControl().setLayoutData(formData);
    }

    // enforce update of button enabled state
    handleSelectionChanged(null);

    return configComposite;
  }

  /**
   * Create a new Check configuration.
   */
  private void addCheckConfig(Shell shell) {
    CheckConfigurationPropertiesDialog dialog = new CheckConfigurationPropertiesDialog(shell, null,
            mWorkingSet);
    dialog.setBlockOnOpen(true);
    if (Window.OK == dialog.open()) {

      CheckConfigurationWorkingCopy newConfig = dialog.getCheckConfiguration();
      mWorkingSet.addCheckConfiguration(newConfig);

      configTable.setConfigs(mWorkingSet.getWorkingCopies());
      configTable.refresh();
      configTable.setSelection(newConfig);
    }
  }

  /**
   * Edit the properties of a check configuration.
   */
  private void editCheckConfig(Shell shell) {
    CheckConfigurationWorkingCopy config = configTable.getSelection();

    if (config != null) {
      CheckConfigurationPropertiesDialog dialog = new CheckConfigurationPropertiesDialog(shell,
              config, mWorkingSet);
      dialog.setBlockOnOpen(true);
      if (Window.OK == dialog.open()) {
        configTable.refresh();
      }
    }
  }

  private void configureCheckConfig(Shell shell) {
    CheckConfigurationWorkingCopy config = configTable.getSelection();

    if (config != null) {

      try {
        // test if file exists
        config.getCheckstyleConfiguration();

        CheckConfigurationConfigureDialog dialog = new CheckConfigurationConfigureDialog(shell,
                config);
        dialog.setBlockOnOpen(true);
        dialog.open();
      } catch (CheckstylePluginException ex) {
        CheckstyleUIPlugin.warningDialog(shell, NLS.bind(Messages.errorCannotResolveCheckLocation,
                config.getLocation(), config.getName()), ex);
      }
    }
  }

  /**
   * Copy an existing config.
   */
  private void copyCheckConfig(Shell shell) {
    ICheckConfiguration sourceConfig = configTable.getSelection();
    if (sourceConfig == null) {
      //
      // Nothing is selected.
      //
      return;
    }

    try {

      // Open the properties dialog to change default name and description
      CheckConfigurationPropertiesDialog dialog = new CheckConfigurationPropertiesDialog(shell,
              null, mWorkingSet);
      dialog.setTemplateConfiguration(sourceConfig);

      dialog.setBlockOnOpen(true);
      if (Window.OK == dialog.open()) {

        CheckConfigurationWorkingCopy newConfig = dialog.getCheckConfiguration();

        // Copy the source configuration into the new internal config
        sourceConfig.copyConfiguration(newConfig);

        mWorkingSet.addCheckConfiguration(newConfig);

        configTable.setConfigs(mWorkingSet.getWorkingCopies());
        configTable.refresh();
      }
    } catch (CheckstylePluginException ex) {
      CheckstyleUIPlugin.errorDialog(shell, ex, true);
    }
  }

  /**
   * Remove a config.
   */
  private void removeCheckConfig(Shell shell) {
    CheckConfigurationWorkingCopy checkConfig = configTable.getSelection();
    if (checkConfig == null || !checkConfig.isEditable()) {
      //
      // Nothing is selected.
      //
      return;
    }

    boolean confirm = MessageDialog.openQuestion(shell,
            Messages.CheckstylePreferencePage_titleDelete,
            NLS.bind(Messages.CheckstylePreferencePage_msgDelete, checkConfig.getName()));
    if (confirm) {

      //
      // Make sure the check config is not in use. Don't let it be
      // deleted if it is.
      //
      if (mWorkingSet.removeCheckConfiguration(checkConfig)) {

        configTable.setConfigs(mWorkingSet.getWorkingCopies());
        configTable.refresh();
      } else {
        MessageDialog.openInformation(shell, Messages.CheckstylePreferencePage_titleCantDelete,
                NLS.bind(Messages.CheckstylePreferencePage_msgCantDelete, checkConfig.getName()));
        return;
      }
    }
  }

  private void setDefaultCheckConfig() {
    CheckConfigurationWorkingCopy checkConfig = configTable.getSelection();
    if (checkConfig == null) {
      //
      // Nothing is selected.
      //
      return;
    }

    if (mWorkingSet instanceof GlobalCheckConfigurationWorkingSet) {
      ((GlobalCheckConfigurationWorkingSet) mWorkingSet).setDefaultCheckConfig(checkConfig);
    }

    configTable.refresh();
  }

  /**
   * Export a configuration.
   */
  private void exportCheckstyleCheckConfig(Shell shell) {
    ICheckConfiguration config = configTable.getSelection();
    if (config == null) {
      //
      // Nothing is selected.
      //
      return;
    }

    FileDialog dialog = new FileDialog(shell, SWT.SAVE);
    dialog.setText(Messages.CheckstylePreferencePage_titleExportConfig);
    String path = dialog.open();
    if (path == null) {
      return;
    }
    File file = new File(path);

    try {
      config.exportConfiguration(file);
    } catch (CheckstylePluginException ex) {
      CheckstyleUIPlugin.errorDialog(shell, Messages.msgErrorFailedExportConfig, ex, true);
    }
  }

  public void handleSelectionChanged(CheckConfigurationWorkingCopy config) {
    boolean configSelected = config != null;
    if (configSelected) {
      mConfigurationDescription
              .setText(config.getDescription() != null ? config.getDescription() : ""); //$NON-NLS-1$

      if (mIsShowUsage) {
        try {
          mUsageView.setInput(ProjectConfigurationFactory
                  .getProjectsUsingConfig(config.getSourceCheckConfiguration()));
        } catch (CheckstylePluginException ex) {
          CheckstyleLog.log(ex);
        }
      }
    } else {
      mConfigurationDescription.setText(""); //$NON-NLS-1$
      if (mIsShowUsage) {
        mUsageView.setInput(new ArrayList<>());
      }
    }
    boolean configDefault = false;
    if (mWorkingSet instanceof GlobalCheckConfigurationWorkingSet) {
      CheckConfigurationWorkingCopy defaultConfig = ((GlobalCheckConfigurationWorkingSet) mWorkingSet)
              .getDefaultCheckConfig();
      configDefault = defaultConfig != null && defaultConfig.equals(config);
    }
    buttonBar.setSelectionState(configSelected, configSelected && config.isEditable(),
            configDefault);
  }

  private static class ConfigTable extends Composite {

    private final TableViewer tableViewer;

    private ConfigTable(Composite parent, int style, boolean useDefaultColumn,
            CheckConfigurationWorkingCopy[] configs, ConfigurationLabelProvider multiProvider,
            Consumer<Shell> configureCheckConfig,
            Consumer<CheckConfigurationWorkingCopy> handleSelectionChanged) {
      super(parent, style);
      setLayout(new FillLayout());
      Table table = new Table(this, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);

      table.setHeaderVisible(true);
      table.setLinesVisible(true);

      TableLayout tableLayout = new TableLayout();
      table.setLayout(tableLayout);

      TableColumn column1 = new TableColumn(table, SWT.NULL);
      column1.setText(Messages.CheckstylePreferencePage_colCheckConfig);
      tableLayout.addColumnData(new ColumnWeightData(40));

      TableColumn column2 = new TableColumn(table, SWT.NULL);
      column2.setText(Messages.CheckstylePreferencePage_colLocation);
      tableLayout.addColumnData(new ColumnWeightData(30));

      TableColumn column3 = new TableColumn(table, SWT.NULL);
      column3.setText(Messages.CheckstylePreferencePage_colType);
      tableLayout.addColumnData(new ColumnWeightData(30));

      if (useDefaultColumn) {
        TableColumn column4 = new TableColumn(table, SWT.NULL);
        column4.setText(Messages.CheckstylePreferencePage_colDefault);
        tableLayout.addColumnData(new ColumnWeightData(12));
      }

      tableViewer = new TableViewer(table);
      tableViewer.setLabelProvider(multiProvider);
      tableViewer.setContentProvider(new ArrayContentProvider());
      tableViewer.setInput(configs);
      tableViewer.addDoubleClickListener(event -> configureCheckConfig.accept(getShell()));
      tableViewer.addSelectionChangedListener(event -> {
        CheckConfigurationWorkingCopy checkConfig = (CheckConfigurationWorkingCopy) tableViewer
                .getStructuredSelection().getFirstElement();
        handleSelectionChanged.accept(checkConfig);
      });
      TableViewerEnhancer.enhance(tableViewer, multiProvider);
    }

    private void refresh() {
      tableViewer.refresh(true);
    }

    private CheckConfigurationWorkingCopy getSelection() {
      return (CheckConfigurationWorkingCopy) tableViewer.getStructuredSelection().getFirstElement();
    }

    private void setConfigs(CheckConfigurationWorkingCopy[] configs) {
      this.tableViewer.setInput(configs);
    }

    private void setSelection(CheckConfigurationWorkingCopy config) {
      this.tableViewer.setSelection(new StructuredSelection(config));
    }
  }

  private record ButtonBarActions(Consumer<Shell> addCheckConfig, Consumer<Shell> editCheckConfig,
          Consumer<Shell> configureCheckConfig, Consumer<Shell> copyCheckConfig,
          Consumer<Shell> removeCheckConfig, Runnable setDefaultCheckConfig,
          Consumer<Shell> exportCheckstyleCheckConfig) {

  }

  private static class ButtonBar extends Composite {

    private final boolean useDefaultButton;
    private final Button mEditButton;
    private final Button mConfigureButton;
    private final Button mCopyButton;
    private final Button mRemoveButton;
    private final Button mDefaultButton;
    private final Button mExportButton;

    private ButtonBar(Composite parent, int style, boolean useDefaultButton, ButtonBarActions actions) {
      super(parent, style);

      this.useDefaultButton = useDefaultButton;

      setLayout(new FormLayout());

      Button mAddButton = new Button(this, SWT.PUSH);
      mAddButton.setText(Messages.CheckstylePreferencePage_btnNew);
      mAddButton.addSelectionListener(SelectionListener
              .widgetSelectedAdapter(event -> actions.addCheckConfig.accept(getShell())));
      FormData formData = new FormData();
      formData.left = new FormAttachment(0);
      formData.top = new FormAttachment(0);
      formData.right = new FormAttachment(100);
      mAddButton.setLayoutData(formData);

      mEditButton = new Button(this, SWT.PUSH);
      mEditButton.setText(Messages.CheckstylePreferencePage_btnProperties);
      mEditButton.addSelectionListener(SelectionListener
              .widgetSelectedAdapter(event -> actions.editCheckConfig.accept(getShell())));
      formData = new FormData();
      formData.left = new FormAttachment(0);
      formData.top = new FormAttachment(mAddButton, 3, SWT.BOTTOM);
      formData.right = new FormAttachment(100);
      mEditButton.setLayoutData(formData);

      mConfigureButton = new Button(this, SWT.PUSH);
      mConfigureButton.setText(Messages.CheckstylePreferencePage_btnConfigure);
      mConfigureButton.addSelectionListener(SelectionListener
              .widgetSelectedAdapter(event -> actions.configureCheckConfig.accept(getShell())));
      formData = new FormData();
      formData.left = new FormAttachment(0);
      formData.top = new FormAttachment(mEditButton, 3, SWT.BOTTOM);
      formData.right = new FormAttachment(100);
      mConfigureButton.setLayoutData(formData);

      mCopyButton = new Button(this, SWT.PUSH);
      mCopyButton.setText(Messages.CheckstylePreferencePage_btnCopy);
      mCopyButton.addSelectionListener(SelectionListener
              .widgetSelectedAdapter(event -> actions.copyCheckConfig.accept(getShell())));
      formData = new FormData();
      formData.left = new FormAttachment(0);
      formData.top = new FormAttachment(mConfigureButton, 3, SWT.BOTTOM);
      formData.right = new FormAttachment(100);
      mCopyButton.setLayoutData(formData);

      mRemoveButton = new Button(this, SWT.PUSH);
      mRemoveButton.setText(Messages.CheckstylePreferencePage_btnRemove);
      mRemoveButton.addSelectionListener(SelectionListener
              .widgetSelectedAdapter(event -> actions.removeCheckConfig.accept(getShell())));
      formData = new FormData();
      formData.left = new FormAttachment(0);
      formData.top = new FormAttachment(mCopyButton, 3, SWT.BOTTOM);
      formData.right = new FormAttachment(100);
      mRemoveButton.setLayoutData(formData);

      if (useDefaultButton) {
        mDefaultButton = new Button(this, SWT.PUSH);
        mDefaultButton.setText(Messages.CheckstylePreferencePage_btnDefault);
        mDefaultButton.addSelectionListener(SelectionListener
                .widgetSelectedAdapter(event -> actions.setDefaultCheckConfig.run()));
        mDefaultButton.setToolTipText(Messages.CheckstylePreferencePage_txtDefault);

        formData = new FormData();
        formData.left = new FormAttachment(0);
        formData.top = new FormAttachment(mRemoveButton, 3, SWT.BOTTOM);
        formData.right = new FormAttachment(100);
        mDefaultButton.setLayoutData(formData);
      } else {
        mDefaultButton = null;
      }

      mExportButton = new Button(this, SWT.PUSH);
      mExportButton.setText(Messages.CheckstylePreferencePage_btnExport);
      mExportButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(
              event -> actions.exportCheckstyleCheckConfig.accept(getShell())));
      formData = new FormData();
      formData.left = new FormAttachment(0);
      formData.right = new FormAttachment(100);
      formData.bottom = new FormAttachment(100);
      mExportButton.setLayoutData(formData);
    }

    private void setSelectionState(boolean configSelected, boolean configEditable,
            boolean configDefault) {
      mEditButton.setEnabled(configSelected);
      mConfigureButton.setEnabled(configSelected);
      mCopyButton.setEnabled(configSelected);
      mExportButton.setEnabled(configSelected);
      mRemoveButton.setEnabled(configSelected && configEditable);
      if (useDefaultButton) {
        mDefaultButton.setEnabled(configSelected && !configDefault);
      }
    }

  }

  /**
   * Label provider for the check configuration table. Implements also support for table sorting and
   * storing of the table settings.
   *
   */
  private static class ConfigurationLabelProvider extends CheckConfigurationLabelProvider
          implements ITableLabelProvider, ITableComparableProvider, ITableSettingsProvider {

    private final ICheckConfigurationWorkingSet mWorkingSet;

    private ConfigurationLabelProvider(ICheckConfigurationWorkingSet mWorkingSet) {
      this.mWorkingSet = mWorkingSet;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
      String result = element.toString();
      if (element instanceof ICheckConfiguration cfg) {
        if (columnIndex == 0) {
          result = cfg.getName();
        }
        if (columnIndex == 1) {
          result = cfg.getLocation();
        }
        if (columnIndex == 2) {
          result = cfg.getType().getName();
        }
        if (columnIndex == 3) {
          result = "";
        }
      }
      return result;
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      return switch (columnIndex) {
        case 0 -> getImage(element);
        case 3 -> {
          ICheckConfiguration cfg = (ICheckConfiguration) element;
          if (mWorkingSet instanceof GlobalCheckConfigurationWorkingSet globalWorkingSet
                  && globalWorkingSet.getDefaultCheckConfig() == cfg) {
            yield CheckstyleUIPluginImages.TICK_ICON.getImage();
          }
          yield null;
        }
        default -> null;
      };
    }

    @Override
    public Comparable<String> getComparableValue(Object element, int col) {
      return getColumnText(element, col);
    }

    @Override
    public IDialogSettings getTableSettings() {
      String concreteViewId = mWorkingSet.getClass().getName();

      IDialogSettings workbenchSettings = CheckstyleUIPlugin.getDefault().getDialogSettings();
      IDialogSettings settings = workbenchSettings.getSection(concreteViewId);

      if (settings == null) {
        settings = workbenchSettings.addNewSection(concreteViewId);
      }

      return settings;
    }
  }
}
