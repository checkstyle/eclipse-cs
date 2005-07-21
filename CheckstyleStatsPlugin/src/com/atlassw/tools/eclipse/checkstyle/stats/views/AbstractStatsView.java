//============================================================================
//
// Copyright (C) 2002-2005  David Schneider, Lars Ködderitzsch, Fabrice Bellingard
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
package com.atlassw.tools.eclipse.checkstyle.stats.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.tasklist.ITaskListResourceAdapter;

import com.atlassw.tools.eclipse.checkstyle.stats.StatsCheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.stats.data.MarkerStat;
import com.atlassw.tools.eclipse.checkstyle.stats.views.internal.CheckstyleMarkerFilter;
import com.atlassw.tools.eclipse.checkstyle.stats.views.internal.CheckstyleMarkerFilterDialog;

/**
 * Abstract view that gathers common behaviour for the stats views.
 * 
 * @author Fabrice BELLINGARD
 */

public abstract class AbstractStatsView extends ViewPart
{
    /**
     * Label de description.
     */
    private Label mDescLabel;

    /**
     * Viewer.
     */
    private TableViewer mViewer;

    /** The filter for this stats view. */
    private CheckstyleMarkerFilter mFilter;

    /** The focused resources. */
    private IResource[] mFocusedResources;

    /** The listener reacting to selection changes in the workspace. */
    private ISelectionListener mFocusListener = new ISelectionListener()
    {
        public void selectionChanged(IWorkbenchPart part, ISelection selection)
        {
            AbstractStatsView.this.focusSelectionChanged(part, selection);
        }
    };

    public void dispose()
    {
        super.dispose();

        getSite().getPage().removeSelectionListener(mFocusListener);
    }

    /**
     * @param part
     * @param selection
     */
    private void focusSelectionChanged(IWorkbenchPart part, ISelection selection)
    {

        List resources = new ArrayList();
        if (part instanceof IEditorPart)
        {
            IEditorPart editor = (IEditorPart) part;
            IFile file = ResourceUtil.getFile(editor.getEditorInput());
            if (file != null)
            {
                resources.add(file);
            }
        }
        else
        {
            if (selection instanceof IStructuredSelection)
            {
                for (Iterator iterator = ((IStructuredSelection) selection)
                    .iterator(); iterator.hasNext();)
                {
                    Object object = iterator.next();
                    if (object instanceof IAdaptable)
                    {
                        IResource resource = (IResource) ((IAdaptable) object)
                            .getAdapter(IResource.class);

                        if (resource == null)
                        {
                            resource = (IResource) ((IAdaptable) object)
                                .getAdapter(IFile.class);
                        }

                        if (resource != null)
                        {
                            resources.add(resource);
                        }
                    }
                }
            }
        }

        IResource[] focus = new IResource[resources.size()];
        resources.toArray(focus);
        updateFocusResource(focus);
    }

    private void updateFocusResource(IResource[] resources)
    {
        boolean updateNeeded = updateNeeded(mFocusedResources, resources);
        if (updateNeeded)
        {
            mFocusedResources = resources;
            getFilter().setFocusResource(resources);
            refresh();
        }
    }

    private boolean updateNeeded(IResource[] oldResources,
        IResource[] newResources)
    {
        // determine if an update if refiltering is required
        CheckstyleMarkerFilter filter = getFilter();
        if (!filter.isEnabled())
        {
            return false;
        }

        int onResource = filter.getOnResource();
        if (onResource == CheckstyleMarkerFilter.ON_ANY_RESOURCE
            || onResource == CheckstyleMarkerFilter.ON_WORKING_SET)
        {
            return false;
        }
        if (newResources == null || newResources.length < 1)
        {
            return false;
        }
        if (oldResources == null || oldResources.length < 1)
        {
            return true;
        }
        if (Arrays.equals(oldResources, newResources))
        {
            return false;
        }
        if (onResource == CheckstyleMarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT)
        {
            Collection oldProjects = CheckstyleMarkerFilter
                .getProjectsAsCollection(oldResources);
            Collection newProjects = CheckstyleMarkerFilter
                .getProjectsAsCollection(newResources);

            if (oldProjects.size() == newProjects.size())
            {
                return !newProjects.containsAll(oldProjects);
            }
            else
            {
                return true;
            }
        }

        return true;
    }

    /**
     * See method below.
     * 
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent)
    {
        // layout du père
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        parent.setLayout(layout);

        // la label
        mDescLabel = new Label(parent, SWT.NONE);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        mDescLabel.setLayoutData(gridData);

        // le tableau
        mViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
            | SWT.V_SCROLL | SWT.SINGLE);
        gridData = new GridData(GridData.FILL_BOTH);
        mViewer.getControl().setLayoutData(gridData);

        // on crée les colonnes
        Table table = mViewer.getTable();
        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        createColumns(table);

        // Les providers
        mViewer.setContentProvider(new ArrayContentProvider());
        mViewer.setLabelProvider(createLabelProvider());

        makeActions();
    }

    /**
     * Adds the actions to the tableviewer context menu.
     * 
     * @param actions
     *            a collection of IAction objets
     */
    protected final void hookContextMenu(final Collection actions)
    {
        MenuManager menuMgr = new MenuManager();
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener()
        {
            public void menuAboutToShow(IMenuManager manager)
            {
                for (Iterator iter = actions.iterator(); iter.hasNext();)
                {
                    manager.add((IAction) iter.next());
                }
                manager.add(new Separator(
                    IWorkbenchActionConstants.MB_ADDITIONS));
            }
        });
        Menu menu = menuMgr.createContextMenu(mViewer.getControl());
        mViewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, mViewer);
    }

    /**
     * Specifies which action will be run when double clicking on the viewer.
     * 
     * @param action
     *            the IAction to add
     */
    protected final void hookDoubleClickAction(final IAction action)
    {
        mViewer.addDoubleClickListener(new IDoubleClickListener()
        {
            public void doubleClick(DoubleClickEvent event)
            {
                action.run();
            }
        });
    }

    /**
     * Opens the filters dialog for the specific stats view.
     */
    public void openFiltersDialog()
    {

        CheckstyleMarkerFilterDialog dialog = new CheckstyleMarkerFilterDialog(
            getViewer().getControl().getShell(),
            (CheckstyleMarkerFilter) getFilter().clone());

        if (dialog.open() == Window.OK)
        {
            CheckstyleMarkerFilter filter = dialog.getFilter();
            filter.saveState(getDialogSettings());

            mFilter = filter;

            refresh();
        }
    }

    /**
     * Cf. méthode surchargée.
     * 
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus()
    {
        mViewer.getControl().setFocus();
    }

    /**
     * Returns the table viewer.
     * 
     * @return Returns the mViewer.
     */
    public TableViewer getViewer()
    {
        return mViewer;
    }

    /**
     * Returns the main label of the view.
     * 
     * @return Returns the mDescLabel.
     */
    public Label getDescLabel()
    {
        return mDescLabel;
    }

    /**
     * Returns the view id of the concrete view. This is used to make separate
     * filter settings (stored in dialog settings) for different concrete views
     * possible.
     * 
     * @return the view id
     */
    protected abstract String getViewId();

    /**
     * Returns the filter of this view.
     * 
     * @return the filter
     */
    protected CheckstyleMarkerFilter getFilter()
    {
        if (mFilter == null)
        {
            mFilter = new CheckstyleMarkerFilter();
            mFilter.restoreState(getDialogSettings());
        }

        return mFilter;
    }

    /**
     * Returns the dialog settings for this view.
     * 
     * @return the dialog settings
     */
    protected final IDialogSettings getDialogSettings()
    {

        String concreteViewId = getViewId();

        IDialogSettings workbenchSettings = StatsCheckstylePlugin.getDefault()
            .getDialogSettings();
        IDialogSettings settings = workbenchSettings.getSection(concreteViewId);

        if (settings == null)
        {
            settings = workbenchSettings.addNewSection(concreteViewId);
        }

        return settings;
    }

    /**
     * Causes the view to re-sync its contents with the workspace. Note that
     * changes will be scheduled in a background job, and may not take effect
     * immediately.
     */
    protected void refresh()
    {

        // rebuild statistics data

        // if (uiJob == null)
        // createUIJob();
        //
        // if (refreshJob == null)
        // {
        //
        // refreshJob = new RestartableJob(Messages.format(
        // "MarkerView.refreshTitle", new Object[] { getTitle() }),//$NON-NLS-1$
        // new IRunnableWithProgress()
        // {
        // public void run(IProgressMonitor monitor)
        // throws InvocationTargetException, InterruptedException
        // {
        // internalRefresh(monitor);
        // }
        // }, getProgressService());
        // }
        //
        // refreshJob.restart();
    }

    /**
     * Returns an appropriate LabelProvider for the elements being displayed in
     * the table viewer.
     * 
     * @return a label provider
     */
    protected abstract IBaseLabelProvider createLabelProvider();

    /**
     * Crée les colonnes du tableau.
     * 
     * @param table :
     *            le tableau
     */
    protected abstract void createColumns(Table table);

    /**
     * Create the wiewer actions. hookContextMenu and hookDoubleClickAction can
     * be called inside this method.
     * 
     * @see AbstractStatsView#hookContextMenu(Collection)
     * @see AbstractStatsView#hookDoubleClickAction(IAction)
     */
    protected abstract void makeActions();

    /**
     * Class used to listen to table viewer column header clicking to sort the
     * different kind of values.
     */
    protected class SorterSelectionListener extends SelectionAdapter
    {
        private ViewerSorter mSorter;

        private ViewerSorter mReverseSorter;

        private ViewerSorter mCurrentSorter;

        /**
         * Constructor.
         * 
         * @param sorter :
         *            the sorter to use
         */
        public SorterSelectionListener(ViewerSorter sorter)
        {
            mSorter = sorter;
            mReverseSorter = ((AbstractStatsSorter) sorter).getReverseSorter();
        }

        public void widgetSelected(SelectionEvent e)
        {
            if (mCurrentSorter == mReverseSorter)
            {
                mCurrentSorter = mSorter;
            }
            else
            {
                mCurrentSorter = mReverseSorter;
            }
            mViewer.setSorter(mCurrentSorter);
        }
    }

    /**
     * Abstract reverse sorter.
     */
    protected abstract class AbstractStatsSorter extends ViewerSorter implements
        Cloneable
    {
        /**
         * pour faire un tri dans l'autre sens.
         */
        private boolean mReverse;

        /**
         * constructeur.
         */
        public AbstractStatsSorter()
        {
            this.mReverse = false;
        }

        /**
         * @return Returns the mReverse.
         */
        public boolean isReverse()
        {
            return mReverse;
        }

        /**
         * Returns the reverse sorter.
         * 
         * @return the reverse sorter, or null if a problem occured
         */
        public AbstractStatsSorter getReverseSorter()
        {
            AbstractStatsSorter sorter = null;
            try
            {
                sorter = (AbstractStatsSorter) this.clone();
                sorter.mReverse = true;
            }
            catch (CloneNotSupportedException e)
            {
                // shouldn't happen. If so, let's return null
            }
            return sorter;
        }

        /**
         * See method below.
         * 
         * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
         *      java.lang.Object, java.lang.Object)
         */
        public abstract int compare(Viewer viewer, Object e1, Object e2);
    }

    /**
     * Sorter for Strings.
     */
    protected class NameSorter extends AbstractStatsSorter
    {
        /**
         * See method below.
         * 
         * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
         *      java.lang.Object, java.lang.Object)
         */
        public int compare(Viewer viewer, Object e1, Object e2)
        {
            ITableLabelProvider provider = (ITableLabelProvider) ((TableViewer) viewer)
                .getLabelProvider();

            String label1 = provider.getColumnText(e1, 0);
            String label2 = provider.getColumnText(e2, 0);

            return (isReverse()) ? label1.compareTo(label2) : label2
                .compareTo(label1);
        }
    }

    /**
     * Sorter for Integers.
     */
    protected class CountSorter extends AbstractStatsSorter
    {
        /**
         * See method below.
         * 
         * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
         *      java.lang.Object, java.lang.Object)
         */
        public int compare(Viewer viewer, Object e1, Object e2)
        {
            int count1 = ((MarkerStat) e1).getCount();
            int count2 = ((MarkerStat) e2).getCount();

            return (isReverse()) ? count1 - count2 : count2 - count1;
        }
    }

}