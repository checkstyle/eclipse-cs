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

import java.io.File;
import java.util.ArrayList;

import net.sf.eclipsecs.core.config.CheckConfigurationFactory;
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
import net.sf.eclipsecs.ui.util.table.EnhancedTableViewer;
import net.sf.eclipsecs.ui.util.table.ITableComparableProvider;
import net.sf.eclipsecs.ui.util.table.ITableSettingsProvider;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * This class provides the editor GUI for a check configuration working set.
 *
 * @author Lars Ködderitzsch
 */
public class CheckConfigurationWorkingSetEditor {

  //
  // attributes
  //

  private EnhancedTableViewer mViewer;

  private Button mAddButton;

  private Button mEditButton;

  private Button mConfigureButton;

  private Button mCopyButton;

  private Button mRemoveButton;

  private Button mDefaultButton;

  private Button mExportButton;

  private Text mConfigurationDescription;

  private StructuredViewer mUsageView;

  private final PageController mController = new PageController();

  private final ICheckConfigurationWorkingSet mWorkingSet;

  private final boolean mIsShowUsage;

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

  /**
   * {@inheritDoc}
   */
  public Control createContents(Composite ancestor) {

    //
    // Create the check configuration section of the screen.
    //
    Composite configComposite = createCheckConfigContents(ancestor);
    return configComposite;
  }

  /**
   * Creates the content regarding the management of check configurations.
   *
   * @param parent
   *          the parent composite
   * @return the configuration area
   */
  private Composite createCheckConfigContents(Composite parent) {

    Composite configComposite = new Composite(parent, SWT.NULL);
    configComposite.setLayout(new FormLayout());

    final Control rightButtons = createButtonBar(configComposite);
    FormData fd = new FormData();
    fd.top = new FormAttachment(0);
    fd.right = new FormAttachment(100);
    fd.bottom = new FormAttachment(100);
    rightButtons.setLayoutData(fd);

    Composite tableAndDesc = new Composite(configComposite, SWT.NULL);
    tableAndDesc.setLayout(new FormLayout());
    fd = new FormData();
    fd.left = new FormAttachment(0);
    fd.top = new FormAttachment(0);
    fd.right = new FormAttachment(rightButtons, -3, SWT.LEFT);
    fd.bottom = new FormAttachment(100, 0);
    tableAndDesc.setLayoutData(fd);

    final Control table = createConfigTable(tableAndDesc);
    fd = new FormData();
    fd.left = new FormAttachment(0);
    fd.top = new FormAttachment(0);
    fd.right = new FormAttachment(100);
    fd.bottom = new FormAttachment(70);
    table.setLayoutData(fd);

    Composite descArea = new Composite(tableAndDesc, SWT.NULL);
    descArea.setLayout(new FormLayout());
    fd = new FormData();
    fd.left = new FormAttachment(0);
    fd.top = new FormAttachment(table, 0);
    fd.right = new FormAttachment(mIsShowUsage ? 60 : 100);
    fd.bottom = new FormAttachment(100);
    descArea.setLayoutData(fd);

    Label lblDescription = new Label(descArea, SWT.NULL);
    lblDescription.setText(Messages.CheckstylePreferencePage_lblDescription);
    fd = new FormData();
    fd.left = new FormAttachment(0);
    fd.top = new FormAttachment(3);
    fd.right = new FormAttachment(100);
    lblDescription.setLayoutData(fd);

    mConfigurationDescription = new Text(descArea,
            SWT.LEFT | SWT.WRAP | SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.VERTICAL);
    fd = new FormData();
    fd.left = new FormAttachment(0);
    fd.top = new FormAttachment(lblDescription);
    fd.right = new FormAttachment(100);
    fd.bottom = new FormAttachment(100);
    mConfigurationDescription.setLayoutData(fd);

    if (mIsShowUsage) {
      Composite usageArea = new Composite(tableAndDesc, SWT.NULL);
      usageArea.setLayout(new FormLayout());
      fd = new FormData();
      fd.left = new FormAttachment(60, 0);
      fd.top = new FormAttachment(table, 3);
      fd.right = new FormAttachment(100);
      fd.bottom = new FormAttachment(100);
      usageArea.setLayoutData(fd);

      Label lblUsage = new Label(usageArea, SWT.NULL);
      lblUsage.setText(Messages.CheckstylePreferencePage_lblProjectUsage);
      fd = new FormData();
      fd.left = new FormAttachment(0);
      fd.top = new FormAttachment(0);
      fd.right = new FormAttachment(100);
      lblUsage.setLayoutData(fd);

      mUsageView = new TableViewer(usageArea);
      mUsageView.getControl().setBackground(usageArea.getBackground());
      mUsageView.setContentProvider(new ArrayContentProvider());
      mUsageView.setLabelProvider(new WorkbenchLabelProvider());
      fd = new FormData();
      fd.left = new FormAttachment(0);
      fd.top = new FormAttachment(lblUsage);
      fd.right = new FormAttachment(100);
      fd.bottom = new FormAttachment(100);
      mUsageView.getControl().setLayoutData(fd);
    }

    // enforce update of button enabled state
    mController.selectionChanged(new SelectionChangedEvent(mViewer, new StructuredSelection()));

    return configComposite;
  }

  /**
   * Creates the table viewer to show the existing check configurations.
   *
   * @param parent
   *          the parent composite
   * @return the table control
   */
  private Control createConfigTable(Composite parent) {
    Table table = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);

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

    if (mWorkingSet instanceof GlobalCheckConfigurationWorkingSet) {
      TableColumn column4 = new TableColumn(table, SWT.NULL);
      column4.setText(Messages.CheckstylePreferencePage_colDefault);
      tableLayout.addColumnData(new ColumnWeightData(12));
    }

    mViewer = new EnhancedTableViewer(table);
    ConfigurationLabelProvider multiProvider = new ConfigurationLabelProvider();
    mViewer.setLabelProvider(multiProvider);
    mViewer.setTableComparableProvider(multiProvider);
    mViewer.setTableSettingsProvider(multiProvider);
    mViewer.installEnhancements();

    mViewer.setContentProvider(new ArrayContentProvider());
    mViewer.setInput(mWorkingSet.getWorkingCopies());
    mViewer.addDoubleClickListener(mController);
    mViewer.addSelectionChangedListener(mController);

    return table;
  }

  /**
   * Creates the button bar.
   *
   * @param parent
   *          the parent composite
   * @return the button bar composite
   */
  private Control createButtonBar(Composite parent) {

    Composite rightButtons = new Composite(parent, SWT.NULL);
    rightButtons.setLayout(new FormLayout());

    mAddButton = new Button(rightButtons, SWT.PUSH);
    mAddButton.setText(Messages.CheckstylePreferencePage_btnNew);
    mAddButton.addSelectionListener(mController);
    FormData fd = new FormData();
    fd.left = new FormAttachment(0);
    fd.top = new FormAttachment(0);
    fd.right = new FormAttachment(100);
    mAddButton.setLayoutData(fd);

    mEditButton = new Button(rightButtons, SWT.PUSH);
    mEditButton.setText(Messages.CheckstylePreferencePage_btnProperties);
    mEditButton.addSelectionListener(mController);
    fd = new FormData();
    fd.left = new FormAttachment(0);
    fd.top = new FormAttachment(mAddButton, 3, SWT.BOTTOM);
    fd.right = new FormAttachment(100);
    mEditButton.setLayoutData(fd);

    mConfigureButton = new Button(rightButtons, SWT.PUSH);
    mConfigureButton.setText(Messages.CheckstylePreferencePage_btnConfigure);
    mConfigureButton.addSelectionListener(mController);
    fd = new FormData();
    fd.left = new FormAttachment(0);
    fd.top = new FormAttachment(mEditButton, 3, SWT.BOTTOM);
    fd.right = new FormAttachment(100);
    mConfigureButton.setLayoutData(fd);

    mCopyButton = new Button(rightButtons, SWT.PUSH);
    mCopyButton.setText(Messages.CheckstylePreferencePage_btnCopy);
    mCopyButton.addSelectionListener(mController);
    fd = new FormData();
    fd.left = new FormAttachment(0);
    fd.top = new FormAttachment(mConfigureButton, 3, SWT.BOTTOM);
    fd.right = new FormAttachment(100);
    mCopyButton.setLayoutData(fd);

    mRemoveButton = new Button(rightButtons, SWT.PUSH);
    mRemoveButton.setText(Messages.CheckstylePreferencePage_btnRemove);
    mRemoveButton.addSelectionListener(mController);
    fd = new FormData();
    fd.left = new FormAttachment(0);
    fd.top = new FormAttachment(mCopyButton, 3, SWT.BOTTOM);
    fd.right = new FormAttachment(100);
    mRemoveButton.setLayoutData(fd);

    mDefaultButton = new Button(rightButtons, SWT.PUSH);
    mDefaultButton.setText(Messages.CheckstylePreferencePage_btnDefault);
    mDefaultButton.addSelectionListener(mController);
    mDefaultButton.setToolTipText(Messages.CheckstylePreferencePage_txtDefault);
    if (mWorkingSet instanceof GlobalCheckConfigurationWorkingSet) {
      fd = new FormData();
      fd.left = new FormAttachment(0);
      fd.top = new FormAttachment(mRemoveButton, 3, SWT.BOTTOM);
      fd.right = new FormAttachment(100);
      mDefaultButton.setLayoutData(fd);
    }

    mExportButton = new Button(rightButtons, SWT.PUSH);
    mExportButton.setText(Messages.CheckstylePreferencePage_btnExport);
    mExportButton.addSelectionListener(mController);
    fd = new FormData();
    fd.left = new FormAttachment(0);
    fd.right = new FormAttachment(100);
    fd.bottom = new FormAttachment(100);
    mExportButton.setLayoutData(fd);

    return rightButtons;
  }

  private Shell getShell() {
    return mViewer.getControl().getShell();
  }

  /**
   * Controller for this page.
   *
   * @author Lars Ködderitzsch
   */
  private class PageController
          implements SelectionListener, IDoubleClickListener, ISelectionChangedListener {

    /**
     * @see SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    @Override
    public void widgetSelected(SelectionEvent e) {

      if (mAddButton == e.widget) {
        addCheckConfig();
      } else if (mEditButton == e.widget
              && mViewer.getSelection() instanceof IStructuredSelection) {
        editCheckConfig();
      } else if (mConfigureButton == e.widget
              && mViewer.getSelection() instanceof IStructuredSelection) {
        configureCheckConfig();
      } else if (mCopyButton == e.widget
              && mViewer.getSelection() instanceof IStructuredSelection) {
        copyCheckConfig();
      } else if (mRemoveButton == e.widget
              && mViewer.getSelection() instanceof IStructuredSelection) {
        removeCheckConfig();
      } else if (mDefaultButton == e.widget
              && mViewer.getSelection() instanceof IStructuredSelection) {
        setDefaultCheckConfig();
      } else if (mExportButton == e.widget
              && mViewer.getSelection() instanceof IStructuredSelection) {
        exportCheckstyleCheckConfig();
      }
    }

    /**
     * @see sSelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
     */
    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
      // NOOP
    }

    /**
     * @see IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
     */
    @Override
    public void doubleClick(DoubleClickEvent event) {
      configureCheckConfig();
    }

    /**
     * @see ISelectionChangedListener#selectionChanged(
     *      org.eclipse.jface.viewers.SelectionChangedEvent)
     */
    @Override
    public void selectionChanged(SelectionChangedEvent event) {
      if (event.getSource() == mViewer && event.getSelection() instanceof IStructuredSelection) {
        CheckConfigurationWorkingCopy config = (CheckConfigurationWorkingCopy) ((IStructuredSelection) event
                .getSelection()).getFirstElement();
        boolean configSelected = config != null;
        if (configSelected) {
          mConfigurationDescription
                  .setText(config.getDescription() != null ? config.getDescription() : ""); //$NON-NLS-1$

          if (mIsShowUsage) {
            try {
              mUsageView.setInput(ProjectConfigurationFactory
                      .getProjectsUsingConfig(config.getSourceCheckConfiguration()));
            } catch (CheckstylePluginException e) {
              CheckstyleLog.log(e);
            }
          }
        } else {
          mConfigurationDescription.setText(""); //$NON-NLS-1$
          if (mIsShowUsage) {
            mUsageView.setInput(new ArrayList<IProject>());
          }
        }
        mEditButton.setEnabled(configSelected);
        mConfigureButton.setEnabled(configSelected);
        mCopyButton.setEnabled(configSelected);
        mRemoveButton.setEnabled(configSelected && config.isEditable());
        mDefaultButton.setEnabled(configSelected);
        mExportButton.setEnabled(configSelected);
      }
    }
  }

  /**
   * Create a new Check configuration.
   */
  private void addCheckConfig() {
    CheckConfigurationPropertiesDialog dialog = new CheckConfigurationPropertiesDialog(getShell(),
            null, mWorkingSet);
    dialog.setBlockOnOpen(true);
    if (Window.OK == dialog.open()) {

      CheckConfigurationWorkingCopy newConfig = dialog.getCheckConfiguration();
      mWorkingSet.addCheckConfiguration(newConfig);

      mViewer.setInput(mWorkingSet.getWorkingCopies());
      mViewer.refresh(true);
      mViewer.setSelection(new StructuredSelection(newConfig));
    }
  }

  /**
   * Edit the properties of a check configuration.
   */
  private void editCheckConfig() {
    CheckConfigurationWorkingCopy config = (CheckConfigurationWorkingCopy) ((IStructuredSelection) mViewer
            .getSelection()).getFirstElement();

    if (config != null) {
      CheckConfigurationPropertiesDialog dialog = new CheckConfigurationPropertiesDialog(getShell(),
              config, mWorkingSet);
      dialog.setBlockOnOpen(true);
      if (Window.OK == dialog.open()) {
        mViewer.refresh(true);
      }
    }
  }

  private void configureCheckConfig() {
    CheckConfigurationWorkingCopy config = (CheckConfigurationWorkingCopy) ((IStructuredSelection) mViewer
            .getSelection()).getFirstElement();

    if (config != null) {

      try {
        // test if file exists
        config.getCheckstyleConfiguration();

        CheckConfigurationConfigureDialog dialog = new CheckConfigurationConfigureDialog(getShell(),
                config);
        dialog.setBlockOnOpen(true);
        dialog.open();
      } catch (CheckstylePluginException e) {
        CheckstyleUIPlugin.warningDialog(getShell(),
                NLS.bind(Messages.errorCannotResolveCheckLocation, config.getLocation(),
                        config.getName()),
                e);
      }
    }
  }

  /**
   * Copy an existing config.
   */
  private void copyCheckConfig() {
    IStructuredSelection selection = (IStructuredSelection) mViewer.getSelection();
    ICheckConfiguration sourceConfig = (ICheckConfiguration) selection.getFirstElement();
    if (sourceConfig == null) {
      //
      // Nothing is selected.
      //
      return;
    }

    try {

      // Open the properties dialog to change default name and description
      CheckConfigurationPropertiesDialog dialog = new CheckConfigurationPropertiesDialog(getShell(),
              null, mWorkingSet);
      dialog.setTemplateConfiguration(sourceConfig);

      dialog.setBlockOnOpen(true);
      if (Window.OK == dialog.open()) {

        CheckConfigurationWorkingCopy newConfig = dialog.getCheckConfiguration();

        // Copy the source configuration into the new internal config
        CheckConfigurationFactory.copyConfiguration(sourceConfig, newConfig);

        mWorkingSet.addCheckConfiguration(newConfig);

        mViewer.setInput(mWorkingSet.getWorkingCopies());
        mViewer.refresh();
      }
    } catch (CheckstylePluginException e) {
      CheckstyleUIPlugin.errorDialog(getShell(), e, true);
    }
  }

  /**
   * Remove a config.
   */
  private void removeCheckConfig() {
    IStructuredSelection selection = (IStructuredSelection) mViewer.getSelection();
    CheckConfigurationWorkingCopy checkConfig = (CheckConfigurationWorkingCopy) selection
            .getFirstElement();
    if (checkConfig == null || !checkConfig.isEditable()) {
      //
      // Nothing is selected.
      //
      return;
    }

    boolean confirm = MessageDialog.openQuestion(getShell(),
            Messages.CheckstylePreferencePage_titleDelete,
            NLS.bind(Messages.CheckstylePreferencePage_msgDelete, checkConfig.getName()));
    if (confirm) {

      //
      // Make sure the check config is not in use. Don't let it be
      // deleted if it is.
      //
      if (mWorkingSet.removeCheckConfiguration(checkConfig)) {

        mViewer.setInput(mWorkingSet.getWorkingCopies());
        mViewer.refresh();
      } else {
        MessageDialog.openInformation(getShell(), Messages.CheckstylePreferencePage_titleCantDelete,
                NLS.bind(Messages.CheckstylePreferencePage_msgCantDelete, checkConfig.getName()));
        return;
      }
    }
  }

  private void setDefaultCheckConfig() {
    IStructuredSelection selection = (IStructuredSelection) mViewer.getSelection();
    CheckConfigurationWorkingCopy checkConfig = (CheckConfigurationWorkingCopy) selection
            .getFirstElement();
    if (checkConfig == null) {
      //
      // Nothing is selected.
      //
      return;
    }

    if (mWorkingSet instanceof GlobalCheckConfigurationWorkingSet) {
      ((GlobalCheckConfigurationWorkingSet) mWorkingSet).setDefaultCheckConfig(checkConfig);
    }

    mViewer.refresh();
  }

  /**
   * Export a configuration.
   */
  private void exportCheckstyleCheckConfig() {
    IStructuredSelection selection = (IStructuredSelection) mViewer.getSelection();
    ICheckConfiguration config = (ICheckConfiguration) selection.getFirstElement();
    if (config == null) {
      //
      // Nothing is selected.
      //
      return;
    }

    FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
    dialog.setText(Messages.CheckstylePreferencePage_titleExportConfig);
    String path = dialog.open();
    if (path == null) {
      return;
    }
    File file = new File(path);

    try {
      CheckConfigurationFactory.exportConfiguration(file, config);
    } catch (CheckstylePluginException e) {
      CheckstyleUIPlugin.errorDialog(getShell(), Messages.msgErrorFailedExportConfig, e, true);
    }
  }

  /**
   * Label provider for the check configuration table. Implements also support for table sorting and
   * storing of the table settings.
   *
   * @author Lars Ködderitzsch
   */
  private class ConfigurationLabelProvider extends CheckConfigurationLabelProvider
          implements ITableLabelProvider, ITableComparableProvider, ITableSettingsProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getColumnText(Object element, int columnIndex) {
      String result = element.toString();
      if (element instanceof ICheckConfiguration) {
        ICheckConfiguration cfg = (ICheckConfiguration) element;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      Image image = null;
      switch (columnIndex) {
        case 0:
          image = getImage(element);
          break;
        case 3:
          ICheckConfiguration cfg = (ICheckConfiguration) element;
          if (mWorkingSet instanceof GlobalCheckConfigurationWorkingSet) {

            if (((GlobalCheckConfigurationWorkingSet) mWorkingSet).getDefaultCheckConfig() == cfg) {
              image = CheckstyleUIPluginImages.getImage(CheckstyleUIPluginImages.TICK_ICON);
            }
          }
          break;
        default:
          break;
      }
      return image;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Comparable<String> getComparableValue(Object element, int col) {
      return getColumnText(element, col);
    }

    /**
     * {@inheritDoc}
     */
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
