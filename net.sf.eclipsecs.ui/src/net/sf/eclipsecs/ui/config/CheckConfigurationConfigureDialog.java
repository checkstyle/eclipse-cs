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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.prefs.BackingStoreException;

import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.Module;
import net.sf.eclipsecs.core.config.Severity;
import net.sf.eclipsecs.core.config.meta.MetadataFactory;
import net.sf.eclipsecs.core.config.meta.RuleGroupMetadata;
import net.sf.eclipsecs.core.config.meta.RuleMetadata;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.CheckstyleUIPluginPrefs;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.util.InternalBrowser;
import net.sf.eclipsecs.ui.util.SWTUtil;
import net.sf.eclipsecs.ui.util.table.EnhancedCheckBoxTableViewer;
import net.sf.eclipsecs.ui.util.table.ITableComparableProvider;
import net.sf.eclipsecs.ui.util.table.ITableSettingsProvider;

/**
 * Enhanced checkstyle configuration editor.
 *
 */
public class CheckConfigurationConfigureDialog extends TitleAreaDialog {

  private static final Pattern PATTERN_INLINE_CODE = Pattern.compile(Pattern.quote("{@code ") + "([^}]*?)" + Pattern.quote("}"));

  /** The current check configuration. */
  private final CheckConfigurationWorkingCopy mConfiguration;

  /** Filter for the table viewer to show only element of the selected group. */
  private final RuleGroupModuleFilter mGroupFilter = new RuleGroupModuleFilter();

  /** Controller for this Dialog. */
  private final PageController mController = new PageController();

  /** The default text for the filter text field. */
  private final String mDefaultFilterText = Messages.CheckConfigurationConfigureDialog_defaultFilterText;

  /** The tree filter. */
  private final TreeFilter mTreeFilter = new TreeFilter();

  /** Flags if the Check configuration can be modified. */
  private boolean mConfigurable;

  /** Text field used to filter the module tree. */
  private Text mTxtTreeFilter;

  /** TreeViewer showing the known modules from the meta data. */
  private TreeViewer mTreeViewer;

  /** Button to add a module. */
  private Button mAddButton;

  /** The table viewer showing the configured modules. */
  private EnhancedCheckBoxTableViewer mTableViewer;

  /** Button to remove a module. */
  private Button mRemoveButton;

  /** Button to edit a module. */
  private Button mEditButton;

  /** Group containing the table viewer. */
  private Group mConfiguredModulesGroup;

  private Browser mBrowserDescription;

  /** Checkbox handling if the module editor is opened on add action. */
  private Button mBtnOpenModuleOnAdd;

  /** the list of modules. */
  private List<Module> mModules;

  /** Flags if the check configuration was changed. */
  private boolean mIsDirty;

  //
  // constructors
  //

  /**
   * Creates the configuration dialog.
   *
   * @param parentShell
   *          the parent shell
   * @param config
   *          the check configuration
   */
  public CheckConfigurationConfigureDialog(Shell parentShell,
          CheckConfigurationWorkingCopy config) {
    super(parentShell);
    setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
    setHelpAvailable(false);
    mConfiguration = config;
  }

  //
  // methods
  //

  /**
   * Creates the dialogs main contents.
   *
   * @param parent
   *          the parent composite
   */
  @Override
  protected Control createDialogArea(Composite parent) {

    Composite composite = (Composite) super.createDialogArea(parent);

    Composite contents = new Composite(composite, SWT.NULL);
    contents.setLayoutData(new GridData(GridData.FILL_BOTH));
    contents.setLayout(new GridLayout());

    SashForm sashForm = new SashForm(contents, SWT.NULL);
    GridData gridData = new GridData(GridData.FILL_BOTH);
    gridData.widthHint = 700;
    gridData.heightHint = 400;
    sashForm.setLayoutData(gridData);
    sashForm.setLayout(new GridLayout());

    Control treeControl = createTreeViewer(sashForm);
    treeControl.setLayoutData(new GridData(GridData.FILL_BOTH));

    Control tableControl = createTableViewer(sashForm);
    tableControl.setLayoutData(new GridData(GridData.FILL_BOTH));

    sashForm.setWeights(new int[] { 30, 70 });

    Label lblDescription = new Label(contents, SWT.NULL);
    lblDescription.setText(Messages.CheckConfigurationConfigureDialog_lblDescription);
    lblDescription.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    mBrowserDescription = new Browser(contents, SWT.BORDER);
    gridData = new GridData(GridData.FILL_BOTH);
    gridData.heightHint = 100;
    mBrowserDescription.setLayoutData(gridData);
    mBrowserDescription.addLocationListener(new LocationAdapter() {
      @Override
      public void changing(LocationEvent event) {
        String url = event.location;
        if (url == null || !url.startsWith("http")) {
          return;
        }
        InternalBrowser.openLinkInExternalBrowser(url);
        event.doit = false;
      }
    });

    // initialize the data
    initialize();

    return contents;
  }

  @Override
  public void create() {
    super.create();

    SWTUtil.addResizeSupport(this, CheckstyleUIPlugin.getDefault().getDialogSettings(),
            CheckConfigurationConfigureDialog.class.getName());
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(Messages.CheckConfigurationConfigureDialog_titleCheckConfigurationDialog);
  }

  @Override
  protected void okPressed() {

    try {
      // only write the modules back if the config is configurable
      // and was actually changed
      if (mConfigurable && mIsDirty) {
        mConfiguration.setModules(mModules);
      }
    } catch (CheckstylePluginException ex) {
      CheckstyleUIPlugin.errorDialog(getShell(), ex, true);
    }

    super.okPressed();
  }

  private Control createTreeViewer(Composite parent) {

    Group knownModules = new Group(parent, SWT.NULL);
    knownModules.setLayout(new GridLayout());
    knownModules.setText(Messages.CheckConfigurationConfigureDialog_lblAvailableModules);

    mTxtTreeFilter = new Text(knownModules, SWT.SINGLE | SWT.BORDER);
    mTxtTreeFilter.setMessage(mDefaultFilterText);
    mTxtTreeFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    mTxtTreeFilter.addModifyListener(mController);
    mTxtTreeFilter.addKeyListener(mController);

    mTreeViewer = new TreeViewer(knownModules,
            SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    mTreeViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
    mTreeViewer.setContentProvider(new MetaDataContentProvider());
    mTreeViewer.setLabelProvider(new MetaDataLabelProvider());
    mTreeViewer.setComparator(new ViewerComparator());
    mTreeViewer.addSelectionChangedListener(mController);
    mTreeViewer.addDoubleClickListener(mController);
    mTreeViewer.getTree().addKeyListener(mController);

    // filter hidden elements
    mTreeViewer.addFilter(new ViewerFilter() {

      @Override
      public boolean select(Viewer viewer, Object parentElement, Object element) {
        boolean passes = true;
        if (element instanceof RuleGroupMetadata) {
          passes = !((RuleGroupMetadata) element).isHidden();
        } else if (element instanceof RuleMetadata) {
          passes = !((RuleMetadata) element).isHidden();
        }
        return passes;
      }
    });

    mAddButton = new Button(knownModules, SWT.PUSH);
    mAddButton.setText(Messages.CheckConfigurationConfigureDialog_btnAdd);
    GridData gridData = new GridData();
    gridData.horizontalAlignment = GridData.END;
    mAddButton.setLayoutData(gridData);
    mAddButton.addSelectionListener(mController);

    return knownModules;
  }

  private Control createTableViewer(Composite parent) {

    mConfiguredModulesGroup = new Group(parent, SWT.NULL);
    mConfiguredModulesGroup.setLayout(new GridLayout());
    mConfiguredModulesGroup.setText("\0"); //$NON-NLS-1$

    Table table = new Table(mConfiguredModulesGroup,
            SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
    table.setLayoutData(new GridData(GridData.FILL_BOTH));
    table.setHeaderVisible(true);
    table.setLinesVisible(true);

    TableLayout tableLayout = new TableLayout();
    table.setLayout(tableLayout);

    TableColumn column1 = new TableColumn(table, SWT.NULL);
    column1.setAlignment(SWT.CENTER);
    column1.setText(Messages.CheckConfigurationConfigureDialog_colEnabled);
    tableLayout.addColumnData(new ColumnWeightData(15));

    TableColumn column2 = new TableColumn(table, SWT.NULL);
    column2.setText(Messages.CheckConfigurationConfigureDialog_colModule);
    tableLayout.addColumnData(new ColumnWeightData(30));

    TableColumn column3 = new TableColumn(table, SWT.NULL);
    column3.setText(Messages.CheckConfigurationConfigureDialog_colSeverity);
    tableLayout.addColumnData(new ColumnWeightData(20));

    TableColumn column4 = new TableColumn(table, SWT.NULL);
    column4.setText(Messages.CheckConfigurationConfigureDialog_colComment);
    tableLayout.addColumnData(new ColumnWeightData(35));

    mTableViewer = new EnhancedCheckBoxTableViewer(table);
    ModuleLabelProvider multiProvider = new ModuleLabelProvider();
    mTableViewer.setLabelProvider(multiProvider);
    mTableViewer.setTableComparableProvider(multiProvider);
    mTableViewer.setTableSettingsProvider(multiProvider);
    mTableViewer.setContentProvider(new ArrayContentProvider());
    mTableViewer.addFilter(mGroupFilter);
    mTableViewer.installEnhancements();
    // by default the table viewer sorts on column 0, but we want to sort by the module label
    mTableViewer.setSortedColumnIndex(1);

    mTableViewer.addDoubleClickListener(mController);
    mTableViewer.addSelectionChangedListener(mController);
    mTableViewer.addCheckStateListener(mController);
    mTableViewer.getTable().addKeyListener(mController);

    Composite buttons = new Composite(mConfiguredModulesGroup, SWT.NULL);
    GridLayout layout = new GridLayout(2, true);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    buttons.setLayout(layout);
    buttons.setLayoutData(new GridData());

    mRemoveButton = new Button(buttons, SWT.PUSH);
    mRemoveButton.setText(Messages.CheckConfigurationConfigureDialog_btnRemove);
    mRemoveButton.setLayoutData(new GridData());
    mRemoveButton.addSelectionListener(mController);

    mEditButton = new Button(buttons, SWT.PUSH);
    mEditButton.setText(Messages.CheckConfigurationConfigureDialog_btnOpen);
    mEditButton.setLayoutData(new GridData());
    mEditButton.addSelectionListener(mController);

    return mConfiguredModulesGroup;
  }

  @Override
  protected Control createButtonBar(Composite parent) {

    Composite composite = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout(2, false);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    composite.setLayout(layout);
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    mBtnOpenModuleOnAdd = new Button(composite, SWT.CHECK);
    mBtnOpenModuleOnAdd.setText(Messages.CheckConfigurationConfigureDialog_btnOpenModuleOnAdd);
    GridData gridData = new GridData();
    gridData.horizontalAlignment = GridData.BEGINNING;
    gridData.horizontalIndent = 5;
    mBtnOpenModuleOnAdd.setLayoutData(gridData);

    // Init the translate tokens preference
    mBtnOpenModuleOnAdd.setSelection(
            CheckstyleUIPluginPrefs.getBoolean(CheckstyleUIPluginPrefs.PREF_OPEN_MODULE_EDITOR));
    mBtnOpenModuleOnAdd.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent event) {
        // store translation preference
        try {
          CheckstyleUIPluginPrefs.setBoolean(CheckstyleUIPluginPrefs.PREF_OPEN_MODULE_EDITOR,
                  ((Button) event.widget).getSelection());
        } catch (BackingStoreException ex) {
          CheckstyleLog.log(ex);
        }
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        // NOOP
      }
    });

    Control buttonBar = super.createButtonBar(composite);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.horizontalAlignment = GridData.END;
    buttonBar.setLayoutData(gridData);

    return composite;
  }

  /**
   * Initialize the dialogs controls with the data.
   */
  private void initialize() {

    mConfigurable = mConfiguration.isConfigurable();
    try {
      new ProgressMonitorDialog(getShell()).run(true, false, monitor -> {
        // this takes quite long the first time due to class loading etc. of Checkstyle
        monitor.beginTask("Loading Checkstyle metadata", IProgressMonitor.UNKNOWN);
        loadModules();
      });
    } catch (InvocationTargetException | InterruptedException ex) {
      CheckstyleLog.log(ex);
    }
    mTableViewer.setInput(mModules);

    this.setTitle(NLS.bind(Messages.CheckConfigurationConfigureDialog_titleMessageArea,
            mConfiguration.getType().getName(), mConfiguration.getName()));

    if (mConfigurable) {
      this.setMessage(Messages.CheckConfigurationConfigureDialog_msgEditConfig);
    } else {
      this.setMessage(Messages.CheckConfigurationConfigureDialog_msgReadonlyConfig);
    }

    // set the logo
    this.setTitleImage(CheckstyleUIPluginImages.PLUGIN_LOGO.getImage());

    mAddButton.setEnabled(mConfigurable);
    mRemoveButton.setEnabled(mConfigurable);

    mTreeViewer.setInput(MetadataFactory.getRuleGroupMetadata());

    List<RuleGroupMetadata> checkGroups = MetadataFactory.getRuleGroupMetadata();
    if (!checkGroups.isEmpty()) {
      ISelection initialSelection = new StructuredSelection(checkGroups.get(0));
      mTreeViewer.setSelection(initialSelection);
    }
  }

  private void loadModules() {
    try {
      mModules = mConfiguration.getModules();
    } catch (CheckstylePluginException ex) {
      mModules = new ArrayList<>();
      CheckstyleUIPlugin.errorDialog(getShell(), ex, true);
    }
  }

  /**
   * Convert a module description to HTML for use with a browser component.
   * @param description module description
   * @return HTML converted description
   */
  public static String getDescriptionHtml(String description) {
    StringBuilder buf = new StringBuilder();
    buf.append("<html><body style=\"margin: 3px; font-size: 11px; ");
    buf.append("font-family: verdana, 'trebuchet MS', helvetica, sans-serif;\">");
    buf.append(description != null ? convertInlineCodeTags(description)
            : Messages.CheckConfigurationConfigureDialog_txtNoDescription);
    buf.append("</body></html>");
    return buf.toString();
  }

  private static String convertInlineCodeTags(String html) {
    return PATTERN_INLINE_CODE.matcher(html).replaceAll("<code>$1</code>");
  }

  /**
   * Controller for this page.
   *
   */
  private class PageController implements ISelectionChangedListener, ICheckStateListener,
          IDoubleClickListener, SelectionListener, KeyListener, ModifyListener {

    @Override
    public void doubleClick(DoubleClickEvent event) {
      if (event.getViewer() == mTableViewer) {
        openModule(event.getSelection());
      } else if (event.getViewer() == mTreeViewer) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        Object element = selection.getFirstElement();

        if (element instanceof RuleGroupMetadata) {
          mTreeViewer.setExpandedState(element, !mTreeViewer.getExpandedState(element));
        } else {
          newModule(event.getSelection());
        }
      }
    }

    @Override
    public void widgetSelected(SelectionEvent e) {

      if (mEditButton == e.widget) {
        openModule(mTableViewer.getSelection());
      } else if (mAddButton == e.widget) {
        newModule(mTreeViewer.getSelection());
      } else if (mRemoveButton == e.widget) {
        removeModule(mTableViewer.getSelection());
      }
    }

    @Override
    public void keyReleased(KeyEvent e) {
      if (e.widget == mTableViewer.getTable()) {
        if (e.character == SWT.DEL || e.keyCode == SWT.ARROW_LEFT) {
          removeModule(mTableViewer.getSelection());
        }
      } else if (e.widget == mTreeViewer.getTree()) {
        if (e.keyCode == SWT.ARROW_RIGHT || e.character == ' ') {

          IStructuredSelection selection = (IStructuredSelection) mTreeViewer.getSelection();
          Object element = selection.getFirstElement();

          if (element instanceof RuleMetadata) {
            newModule(mTreeViewer.getSelection());
          }
        }
      } else if (e.widget == mTxtTreeFilter && e.keyCode == SWT.ARROW_DOWN) {
        mTreeViewer.getTree().forceFocus();
      }
    }

    @Override
    public void modifyText(ModifyEvent e) {
      mTreeViewer.getControl().setRedraw(false);
      try {
        if (StringUtils.isNotBlank(mTxtTreeFilter.getText())) {

          if (!Arrays.asList(mTableViewer.getFilters()).contains(mTreeFilter)) {
            mTreeViewer.addFilter(mTreeFilter);
          }

          mTreeViewer.refresh();
          mTreeViewer.expandAll();
        } else {
          mTreeViewer.removeFilter(mTreeFilter);
          mTreeViewer.refresh();
        }
      } finally {
        mTreeViewer.getControl().setRedraw(true);
      }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
      // NOOP
    }

    @Override
    public void keyPressed(KeyEvent e) {
      // NOOP
    }

    @Override
    public void checkStateChanged(CheckStateChangedEvent event) {
      if (mConfigurable) {
        Module module = (Module) event.getElement();

        if (event.getChecked()) {
          // restore last severity before setting to ignore
          Severity lastEnabled = module.getLastEnabledSeverity();
          if (lastEnabled != null) {
            module.setSeverity(lastEnabled);
          } else {
            module.setSeverity(module.getMetaData().getDefaultSeverityLevel());
          }
        } else {
          module.setSeverity(Severity.ignore);
        }
        mIsDirty = true;
        mTableViewer.refresh(module, true);
      }
      refreshTableViewerState();
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {

      IStructuredSelection selection = (IStructuredSelection) event.getSelection();

      Object element = selection.getFirstElement();
      String description = null;

      if (element instanceof RuleGroupMetadata) {

        RuleGroupMetadata group = (RuleGroupMetadata) element;
        description = group.getDescription();
        mGroupFilter.setCurrentGroup(group);
        mConfiguredModulesGroup
                .setText(NLS.bind(Messages.CheckConfigurationConfigureDialog_lblConfiguredModules,
                        group.getGroupName()));
        mTableViewer.refresh();

        refreshTableViewerState();
      } else if (element instanceof RuleMetadata) {

        RuleMetadata rule = (RuleMetadata) element;

        description = rule.getDescription();
        mGroupFilter.setCurrentGroup(rule.getGroup());
        mConfiguredModulesGroup
                .setText(NLS.bind(Messages.CheckConfigurationConfigureDialog_lblConfiguredModules,
                        rule.getGroup().getGroupName()));
        mTableViewer.refresh();
        refreshTableViewerState();

      } else if (element instanceof Module) {
        RuleMetadata meta = ((Module) element).getMetaData();
        if (meta != null) {
          description = meta.getDescription();
        }
      }

      String buf = getDescriptionHtml(description);
      mBrowserDescription.setText(buf);
    }

    /**
     * Opens the module editor for the current selection.
     *
     * @param selection
     *          the selection
     */
    private void openModule(ISelection selection) {

      Module module = (Module) ((IStructuredSelection) selection).getFirstElement();
      if (module != null) {

        Module workingCopy = module.clone();

        RuleConfigurationEditDialog dialog = new RuleConfigurationEditDialog(getShell(),
                workingCopy, !mConfigurable,
                Messages.CheckConfigurationConfigureDialog_titleModuleConfigEditor);
        if (Window.OK == dialog.open() && mConfigurable) {
          mModules.set(mModules.indexOf(module), workingCopy);
          mIsDirty = true;
          mTableViewer.refresh(true);
          refreshTableViewerState();
        }
      }
    }

    /**
     * Creates a module editor for the current selection.
     *
     * @param selection
     *          the selection
     * @return whether configuration was successful
     */
    private boolean newModule(ISelection selection) {
      if (!mConfigurable) {
        return true;
      }
      boolean openOnAdd = CheckstyleUIPluginPrefs
              .getBoolean(CheckstyleUIPluginPrefs.PREF_OPEN_MODULE_EDITOR);

      Iterator<?> iter = ((IStructuredSelection) selection).iterator();
      while (iter.hasNext()) {
        Object selectedElement = iter.next();
        if (selectedElement instanceof RuleGroupMetadata) {
          // if group is selected add all modules from this group
          List<RuleMetadata> rules = ((RuleGroupMetadata) selectedElement).getRuleMetadata();

          IStructuredSelection allRulesOfGroupSelection = new StructuredSelection(rules);
          if (!newModule(allRulesOfGroupSelection)) {
            return false;
          }
        } else if (selectedElement instanceof RuleMetadata) {

          RuleMetadata metadata = (RuleMetadata) selectedElement;

          // check if the module is a singleton and already
          // configured
          if (metadata.isSingleton() && isAlreadyConfigured(metadata)) {
            return true;
          }

          Module workingCopy = new Module(metadata, false);

          if (openOnAdd) {

            RuleConfigurationEditDialog dialog = new RuleConfigurationEditDialog(getShell(),
                    workingCopy, !mConfigurable,
                    Messages.CheckConfigurationConfigureDialog_titleNewModule);
            if (mConfigurable) {
              int dialogResult = dialog.open();
              if (Window.OK == dialogResult) {
                mModules.add(workingCopy);
                mIsDirty = true;
                mTableViewer.refresh(true);
                refreshTableViewerState();
                mTreeViewer.refresh();
                mTreeViewer.getTree().forceFocus();
              }
              if (Window.CANCEL == dialogResult) {
                // stop showing more dialogs and also don't add any further rules
                return false;
              }
            }
          } else {
            mModules.add(workingCopy);
            mIsDirty = true;
            mTableViewer.refresh(true);
            refreshTableViewerState();
            mTreeViewer.refresh();
          }
        }
      }
      return true;
    }

    /**
     * Creates a module editor for the current selection.
     *
     * @param selection
     *          the selection
     */
    private void removeModule(ISelection selection) {

      if (mConfigurable && !selection.isEmpty()) {

        if (MessageDialog.openConfirm(getShell(),
                Messages.CheckConfigurationConfigureDialog_titleRemoveModules,
                Messages.CheckConfigurationConfigureDialog_msgRemoveModules)) {

          @SuppressWarnings("unchecked")
          Iterator<Module> iter = ((IStructuredSelection) selection).iterator();
          while (iter.hasNext()) {
            Module module = iter.next();
            if (module.getMetaData().isDeletable()) {
              mModules.remove(module);
              mIsDirty = true;
              mTableViewer.refresh(true);
              refreshTableViewerState();
              mTreeViewer.refresh();
            }
          }
        }
      }
    }

    /**
     * Restores the checked state of the table items.
     */
    private void refreshTableViewerState() {

      // set selected modules (Modules where severity is not Ignore).
      int size = mModules != null ? mModules.size() : 0;
      for (int i = 0; i < size; i++) {
        Module module = mModules.get(i);
        if (mConfigurable) {
          mTableViewer.setChecked(module, !Severity.ignore.equals(module.getSeverity())
                  || !module.getMetaData().hasSeverity());
        } else {
          mTableViewer.setChecked(module, !Severity.ignore.equals(module.getSeverity())
                  || !module.getMetaData().hasSeverity());
          mTableViewer.setGrayed(module, !Severity.ignore.equals(module.getSeverity()));
        }
      }
    }

    /**
     * Checks if a certain module is already contained in the configuration.
     */
    private boolean isAlreadyConfigured(RuleMetadata metadata) {
      String internalName = metadata.getInternalName();
      boolean containsModule = false;
      for (int i = 0, size = mModules.size(); i < size; i++) {

        Module module = mModules.get(i);

        if (internalName.equals(module.getMetaData().getInternalName())) {
          containsModule = true;
          break;
        }

      }
      return containsModule;
    }

  }

  /**
   * TreeContentProvider that provides the structure of the rule metadata.
   *
   */
  private static class MetaDataContentProvider implements ITreeContentProvider {

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
        parent = ((RuleMetadata) element).getGroup();
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
  private class MetaDataLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {
      String text = null;
      if (element instanceof RuleGroupMetadata) {
        text = ((RuleGroupMetadata) element).getGroupName();
      } else if (element instanceof RuleMetadata) {
        text = ((RuleMetadata) element).getRuleName();
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
      if (mModules != null) {
        for (Module module : mModules) {

          if (metadata.equals(module.getMetaData())) {
            used = true;
            break;
          }
        }
      }

      return used;
    }
  }

  /**
   * Label provider for the table showing the configured modules.
   *
   */
  private static class ModuleLabelProvider extends LabelProvider
          implements ITableLabelProvider, ITableComparableProvider, ITableSettingsProvider {

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
      String text = null;

      if (element instanceof Module) {

        Module module = (Module) element;
        switch (columnIndex) {

          case 0:
            text = "";
            break;
          case 1:
            text = module.getName() != null ? module.getName() : "";
            break;
          case 2:
            text = module.getSeverity() != null ? module.getSeverity().name() : "";
            break;
          case 3:
            text = module.getComment() != null ? module.getComment() : "";
            break;
          default:
            text = "";
            break;
        }
      }
      return text;
    }

    @Override
    public Comparable<?> getComparableValue(Object element, int col) {
      if (col == 0 && element instanceof Module) {
        return Severity.ignore.equals(((Module) element).getSeverity()) ? Integer.valueOf(0)
                : Integer.valueOf(1);
      }

      return getColumnText(element, col);
    }

    @Override
    public IDialogSettings getTableSettings() {
      String concreteViewId = CheckConfigurationConfigureDialog.class.getName();

      IDialogSettings workbenchSettings = CheckstyleUIPlugin.getDefault().getDialogSettings();
      IDialogSettings settings = workbenchSettings.getSection(concreteViewId);

      if (settings == null) {
        settings = workbenchSettings.addNewSection(concreteViewId);
      }

      return settings;
    }
  }

  /**
   * Viewer filter that includes all modules that belong to the currently selected group.
   *
   */
  private static class RuleGroupModuleFilter extends ViewerFilter {

    /** the current rule group. */
    private RuleGroupMetadata mCurrentGroup;

    /**
     * Sets the current rule group.
     *
     * @param groupMetaData the group metadata
     */
    public void setCurrentGroup(RuleGroupMetadata groupMetaData) {
      mCurrentGroup = groupMetaData;
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
      boolean result = false;

      Module module = (Module) element;
      RuleMetadata metaData = module.getMetaData();

      if (metaData == null) {
        return true;
      }

      RuleGroupMetadata moduleGroup = metaData.getGroup();

      if (mCurrentGroup == null || metaData.isHidden()) {
        result = false;
      } else if (mCurrentGroup == moduleGroup) {
        result = true;
      }

      return result;
    }
  }

  /**
   * Filter implementation that filters the module tree with respect of a filter text field to input
   * a search word.
   *
   */
  private class TreeFilter extends ViewerFilter {

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
      boolean result = true;

      String filterText = mTxtTreeFilter.getText();

      if (element instanceof RuleMetadata) {
        result = selectRule((RuleMetadata) element, filterText);
      } else if (element instanceof RuleGroupMetadata) {
        result = selectGroup((RuleGroupMetadata) element, filterText);
      }

      return result;
    }

    private boolean selectRule(RuleMetadata element, String filterText) {

      Pattern matchPattern = Pattern.compile(Pattern.quote(filterText), Pattern.CASE_INSENSITIVE);

      String ruleName = element.getRuleName();
      String internalName = element.getInternalName();
      String description = element.getDescription();
      return (ruleName != null && matchPattern.matcher(ruleName).find())
              || (internalName != null && matchPattern.matcher(internalName).find())
              || (description != null && matchPattern.matcher(description).find());
    }

    private boolean selectGroup(RuleGroupMetadata group, String filterText) {
      boolean hasAtLeastOneMatchingChild = false;

      for (RuleMetadata element : group.getRuleMetadata()) {
        if (selectRule(element, filterText)) {
          hasAtLeastOneMatchingChild = true;
          break;
        }
      }

      return hasAtLeastOneMatchingChild;
    }
  }

}
